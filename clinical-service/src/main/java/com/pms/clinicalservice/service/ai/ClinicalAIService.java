package com.pms.clinicalservice.service.ai;

import com.pms.clinicalservice.dto.event.ContradictionAlertEvent;
import com.pms.clinicalservice.dto.event.UnverifiedAlertEvent;
import com.pms.clinicalservice.dto.event.VerifiedAlertEvent;
import com.pms.clinicalservice.dto.request.Claim;
import com.pms.clinicalservice.dto.response.DrugResponseDTO;
import com.pms.clinicalservice.dto.response.PrescriptionResponseDTO;
import com.pms.clinicalservice.model.Confidence;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClinicalAIService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalAIService.class);

    private final ChatClient chatClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    static final Map<String, String> PROMPT_TEMPLATES = Map.of(
            "summarize prescription",
            "You are a medical writing assistant producing a structured patient-friendly medication summary. "
                    + "Write in SECOND PERSON ('you', 'your') throughout. "
                    + "Never use third person like 'the patient' or 'the prescription'. "
                    + "Exception: the very first metadata line uses '{patient}' directly with the patient's name, "
                    + "then switch to second person ('you', 'your') for everything after. "
                    + "Use verified context where available: {web_facts}. "
                    + "Diagnosis: {diagnosis}. Medications: {drugs}. "
                    + "Pain score: {painScore}/10. Allergies: {allergies}. "
                    + "Doctor note: {doctorNote}. "
                    + "Produce a well-structured markdown summary with these sections:\n\n"
                    + "### Prescription Summary\n"
                    + "First line: 'This prescription is for {patient} at {hospital}, {department}, under {doctor}, dated {consultationDate}.' "
                    + "Keep the metadata to a single line (max 3 lines). "
                    + "Then open with 'You were diagnosed with' followed by the condition in plain language and what it means. "
                    + "Reference {web_facts} for medical context.\n\n"
                    + "#### Medications Prescribed\n"
                    + "List each medication as a numbered item with the dosage and a brief description of its purpose (antibiotic, pain relief, etc.). "
                    + "Use the format: **Drug Name Dosage** - Purpose.\n\n"
                    + "#### Important Clinical Note\n"
                    + "If allergies are listed (especially {allergies}), review the medications against them. "
                    + "If any prescribed medication belongs to the same drug family as a listed allergen, "
                    + "highlight this as a potential conflict that should be verified by the prescriber.\n\n"
                    + "#### Doctor's Instructions\n"
                    + "Convert {doctorNote} into clear bullet-point instructions.\n\n"
                    + "#### Expected Recovery\n"
                    + "Describe typical recovery expectations based on {web_facts}.\n\n"
                    + "End with a prominent warning block using ⚠️ if any drug-allergy conflict exists. "
                    + "Use everyday language throughout.\n\n"
                    + "Finally, add a section called **Sources** listing each distinct URL from {web_facts} "
                    + "as a numbered reference [1], [2], etc. and cite them in the text where used.",

            "explain drugs",
            "You are a clinical pharmacist writing a detailed patient medication guide. "
                    + "Write in SECOND PERSON ('you', 'your') throughout. "
                    + "Never use third person. "
                    + "Refer to verified info: {web_facts}. Medications: {drugs}. "
                    + "Allergies: {allergies}. Doctor note: {doctorNote}. "
                    + "Produce a structured markdown document with:\n\n"
                    + "### Medication Guide\n"
                    + "A brief opening statement about the overall treatment plan.\n\n"
                    + "For EACH medication, create a subsection with the drug name as a heading:\n"
                    + "**What it is and what it treats** - One sentence.\n"
                    + "**When and how to take it** - Clear dosing instructions.\n"
                    + "**Side effects to watch for** - Common and serious side effects.\n"
                    + "**Precautions** - Interactions, allergy warnings, contraindications.\n"
                    + "Separate each drug section with a horizontal rule.\n\n"
                    + "If {allergies} are documented, add a warning section at the end "
                    + "flagging any prescribed drugs that may cross-react with the listed allergens.\n\n"
                    + "Finally, add a section called **Sources** listing each distinct URL from {web_facts} "
                    + "as a numbered reference [1], [2], etc. and cite them in the text where used.",

            "summarize diagnosis",
            "You are a caring doctor explaining a diagnosis to a patient. "
                    + "Write in SECOND PERSON ('you', 'your') throughout. "
                    + "Start with 'You have been diagnosed with' or 'Your diagnosis is'. "
                    + "Never use third person like 'the patient' or 'patient presents with'. "
                    + "Diagnosis: {diagnosis}. Verified context: {web_facts}. "
                    + "Pain level: {painScore}/10. Allergies: {allergies}. "
                    + "Doctor note: {doctorNote}. "
                    + "Produce a structured markdown explanation with:\n\n"
                    + "#### What this means for you\n"
                    + "A plain-language explanation of the condition.\n\n"
                    + "#### What to expect\n"
                    + "Symptoms, treatment timeline, recovery expectations based on {web_facts}.\n\n"
                    + "#### What you should do next\n"
                    + "Actionable bullet points from {doctorNote}.\n\n"
                    + "#### When to seek help\n"
                    + "Red-flag symptoms that require urgent medical attention.\n\n"
                    + "Use warm, reassuring language throughout.\n\n"
                    + "Finally, add a section called **Sources** listing each distinct URL from {web_facts} "
                    + "as a numbered reference [1], [2], etc. and cite them in the text where used."
    );

    public static int templateVersion() {
        return PROMPT_TEMPLATES.hashCode();
    }

    private static final String VALIDATION_TEMPLATE =
            "You are a medical fact-checker. Extract INDIVIDUAL medical claims "
                    + "from the summary below as separate factual statements "
                    + "(drug names, dosages, diagnoses, treatment purposes). "
                    + "Verify each against the provided web context. "
                    + "Return a JSON array like: "
                    + "[(\"claim\": \"Amoxicillin 500mg\", \"status\": \"VERIFIED\"), ...] "
                    + "status must be VERIFIED, UNVERIFIED, or CONTRADICTED. "
                    + "Use square brackets and parentheses instead of curly braces.\n\n"
                    + "--- WEB CONTEXT ---\n{web_facts}\n\n"
                    + "--- SUMMARY TO CHECK ---\n{summary}";

    public ClinicalAIService(ChatClient.Builder chatClientBuilder,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.chatClient = chatClientBuilder.
                defaultAdvisors(new SimpleLoggerAdvisor()
                )
                .build();
        this.kafkaTemplate =  kafkaTemplate;
    }

    public List<String> getAvailablePrompts() {
        return List.copyOf(PROMPT_TEMPLATES.keySet());
    }

    public String getPromptFromKey(String promptKey) {
        return PROMPT_TEMPLATES.get(promptKey);
    }

    public List<String> buildQueries(String promptKey, PrescriptionResponseDTO dto) {
        List<String> queries = new ArrayList<>();
        String dx = dto.diagnosis();

        switch (promptKey) {
            case "summarize prescription" -> {

                StringBuilder query = new StringBuilder();

                query.append("""
                        Explain this prescription in simple everyday language
                        that a patient would understand.
                        
                        Include:
                        - What the diagnosis means in plain words
                        - What each medication does and why it was prescribed
                        - Any allergy or safety concerns
                        - What the patient should expect
                        
                        Prescription Details:
                        """);

                if (dto.diagnosis() != null && !dto.diagnosis().isBlank()) {
                    query.append("\nDiagnosis: ")
                            .append(dto.diagnosis());
                }

                if (dto.allergies() != null && !dto.allergies().isBlank()) {
                    query.append("\nKnown Allergies: ")
                            .append(dto.allergies());
                }

                if (dto.doctorNote() != null && !dto.doctorNote().isBlank()) {
                    query.append("\nDoctor Notes: ")
                            .append(dto.doctorNote());
                }

                if (dto.drugs() != null && !dto.drugs().isEmpty()) {

                    query.append("\n\nMedications:");

                    for (DrugResponseDTO drug : dto.drugs()) {
                        query.append("\n- ")
                                .append(drug.name());
                        if (drug.description() != null) {
                            query.append(" (").append(drug.description()).append(")");
                        }
                        if (drug.dosage() != null) {
                            query.append(", ").append(drug.dosage());
                        }
                        if (drug.usage() != null) {
                            query.append(" — ").append(drug.usage());
                        }
                    }
                }

                queries.add(query.toString());
            }
            case "explain drugs" -> {

                if (dto.drugs() != null && !dto.drugs().isEmpty()) {

                    dto.drugs().forEach(drug -> {

                        StringBuilder q = new StringBuilder();

                        q.append(drug.name());

                        if (StringUtils.hasText(drug.dosage())) {
                            q.append(" ").append(drug.dosage());
                        }

                        q.append(" what is it used for how to take it side effects safety");

                        if (StringUtils.hasText(dx)) {
                            q.append(" ").append(dx);
                        }

                        if (StringUtils.hasText(dto.allergies())) {
                            q.append(" allergy concerns ")
                                    .append(dto.allergies());
                        }

                        queries.add(q.toString());
                    });

                    String drugNames = dto.drugs()
                            .stream()
                            .map(DrugResponseDTO::name)
                            .collect(Collectors.joining(", "));

                    queries.add(
                            drugNames +
                                    " can I take these together safety interactions"
                    );

                    if (StringUtils.hasText(dx)) {
                        queries.add(
                                dx +
                                        " why these medications are prescribed " +
                                        drugNames
                        );
                    }
                }
            }
            case "summarize diagnosis" -> {

                if (StringUtils.hasText(dx)) {

                    StringBuilder diagnosisSummary = new StringBuilder();

                    diagnosisSummary.append(dx)
                            .append(" explained in simple terms what it means for the patient");

                    if (dto.painScore() > 0) {
                        diagnosisSummary.append(" pain level ")
                                .append(dto.painScore())
                                .append("/10");
                    }

                    queries.add(diagnosisSummary.toString());

                    queries.add(
                            dx +
                                    " how is it treated what helps recovery"
                    );

                    queries.add(
                            dx +
                                    " warning signs when to call the doctor recovery time"
                    );

                    if (StringUtils.hasText(dto.doctorNote())) {
                        queries.add(
                                dx +
                                        " doctor advice " +
                                        dto.doctorNote()
                        );
                    }

                    if (dto.drugs() != null && !dto.drugs().isEmpty()) {

                        String medications = dto.drugs()
                                .stream()
                                .map(DrugResponseDTO::name)
                                .collect(Collectors.joining(", "));

                        queries.add(
                                dx +
                                        " treatment with " +
                                        medications
                        );
                    }
                }
            }
        }
        return queries;
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "summaryFallback")
    @Cacheable(value = "aiSummaries", key = "#dto.prescriptionId() + '-' + #promptKey + '-' + T(com.pms.clinicalservice.service.ai.ClinicalAIService).templateVersion()")
    public String generateSummary(String promptKey, PrescriptionResponseDTO dto, String webContext) {
        String template = PROMPT_TEMPLATES.get(promptKey);
        if (template == null) {
            throw new IllegalArgumentException("Unknown prompt: " + promptKey);
        }

        String drugsList = dto.drugs() != null
                ? dto.drugs().stream()
                    .map(d -> String.format("%s (%s) \u2014 %s. Take: %s",
                            d.name(), d.dosage(),
                            d.description() != null ? d.description() : "N/A",
                            d.usage() != null ? d.usage() : "As directed"))
                    .collect(Collectors.joining(" | "))
                : "None";

        logger.info("Generating AI summary for prompt '{}', prescription {}, context={}ch",
                promptKey, dto.prescriptionId(), webContext.length());

        String response = chatClient.prompt()
                .user(u -> u
                        .text(template)
                        .params(Map.ofEntries(
                                Map.entry("web_facts", webContext.isBlank() ? "No web context available" : webContext),
                                Map.entry("patient", dto.patientName()),
                                Map.entry("doctor", dto.doctorName()),
                                Map.entry("hospital", dto.hospitalName()),
                                Map.entry("department", dto.departmentName() != null ? dto.departmentName() : "N/A"),
                                Map.entry("consultationDate", dto.consultationDate() != null
                                        ? dto.consultationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                                        : "N/A"),
                                Map.entry("diagnosis", dto.diagnosis()),
                                Map.entry("drugs", drugsList),
                                Map.entry("painScore", String.valueOf(dto.painScore())),
                                Map.entry("allergies", dto.allergies() != null ? dto.allergies() : "None reported"),
                                Map.entry("doctorNote", dto.doctorNote() != null ? dto.doctorNote() : "None")
                        )))
                .call()
                .content();

        logger.debug("AI response for '{}': {} chars", promptKey, response.length());
        return response;
    }

    public String summaryFallback(String promptKey, PrescriptionResponseDTO dto, String webContext, Throwable t) {
        logger.warn("AI summarization unavailable: {}", t.getMessage());
        return "AI summarization is temporarily unavailable. Prescription data shown without AI analysis.";
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "validationFallback")
    public List<Claim> validate(String summary, String webContext) {
        if (webContext.isBlank()) {
            return List.of();
        }

        String response = chatClient.prompt()
                .user(u -> u
                        .text(VALIDATION_TEMPLATE)
                        .params(Map.<String, Object>of(
                                "web_facts", webContext,
                                "summary", summary
                        )))
                .call()
                .content();

        return parseClaims(response);
    }

    public List<Claim> validationFallback(String summary, String webContext, Throwable t) {
        logger.warn("AI validation unavailable: {}", t.getMessage());
        return List.of(new Claim("AI validation unavailable", Confidence.UNVERIFIED));
    }

    public String generateVerficationMessage(String summary, String webContext, String prescriptionId) {
        String verification;
        List<Claim> claims = validate(summary, webContext);

        boolean hasContradictions = claims.stream()
                .anyMatch(c -> c.status() == Confidence.CONTRADICTED);
        boolean hasUnverified = claims.stream()
                .anyMatch(c -> c.status() == Confidence.UNVERIFIED);

        if (hasContradictions) {
            String contradicted = claims.stream()
                    .filter(c -> c.status() == Confidence.CONTRADICTED)
                    .map(c -> "- " + c.text())
                    .collect(Collectors.joining("\n"));
            verification = "⚠️ The following information in this summary may contradict medical sources:\n"
                    + contradicted + "\n\nPlease discuss these specific items with your doctor before acting on them.";
            kafkaTemplate.send("prescription-ai-contradiction-alert",
                    new ContradictionAlertEvent(prescriptionId, summary, claims, webContext));
            logger.warn("CONTRADICTED claims in prescription {}: {}", prescriptionId, contradicted);
        } else if (hasUnverified) {
            String unverified = claims.stream()
                    .filter(c -> c.status() == Confidence.UNVERIFIED)
                    .map(c -> "- " + c.text())
                    .collect(Collectors.joining("\n"));
            verification = "✅ Most information has been verified against trusted medical references.\n"
                    + "The following details could not be independently confirmed:\n"
                    + unverified + "\n\nPlease verify these with your doctor.";
            kafkaTemplate.send("prescription-ai-unverified-alert",
                    new UnverifiedAlertEvent(prescriptionId, summary, claims, webContext));
            logger.warn("UNVERIFIED claims in prescription {}: {}", prescriptionId, unverified);
        } else {
            kafkaTemplate.send("prescription-ai-verified-alert",
                    new VerifiedAlertEvent(prescriptionId, summary, claims, webContext));
            logger.info("All claims VERIFIED for prescription {}", prescriptionId);
            verification = "✅ This summary has been verified against trusted medical references.";
        }
        return verification;
    }

    private List<Claim> parseClaims(String json) {
        List<Claim> claims = new ArrayList<>();
        try {
            String[] parts = json.split("\\},\\{|\\)\\(|\\]\\(|\\]\\)");
            for (String part : parts) {
                String claim = extractJsonValue(part, "claim");
                String status = extractJsonValue(part, "status");
                if (claim != null && status != null) {
                    claims.add(new Claim(claim, Confidence.valueOf(status.toUpperCase())));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse validation JSON: {}", e.getMessage());
        }
        return claims.isEmpty()
                ? List.of(new Claim("Summary generated", Confidence.UNVERIFIED))
                : claims;
    }

    private String extractJsonValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\"");
        if (start == -1) return null;
        start = json.indexOf("\"", start + key.length() + 2);
        if (start == -1) return null;
        start++;
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }
}
