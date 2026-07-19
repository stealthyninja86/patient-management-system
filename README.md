# Orbit — Multi-Hospital Patient Management Platform

![Java](https://img.shields.io/badge/Java-21-%23ED8B00)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.10-%236DB33F)
![Kafka](https://img.shields.io/badge/Kafka-3.9-%23231F20)
![gRPC](https://img.shields.io/badge/gRPC-1.68-%23162872)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%234169E1)

---

## The Problem

Healthcare data is fragmented. A patient's records live across multiple hospitals, clinics, and pharmacies — each with its own system, its own login, its own paper trail. When a doctor needs a patient's full history, there's no unified way to get it. Cross-hospital data sharing is cumbersome, consent management is ad-hoc, and medical records often lack proper audit trails.


---

### A Real Scenario

Over the past two years, **Sameera** has visited three different hospitals—a neighborhood clinic for routine checkups, a pulmonologist for recurring breathing problems, and an emergency department after a severe allergic reaction. Each hospital maintains its own electronic health record system. None of them automatically share information with one another.

Today, Sameera visits **Dr. Arjun** at a new hospital.

As he reviews her symptoms, he wants to know:

* What antibiotics was she prescribed during her last infection?
* Does she have any documented drug allergies?
* Were any medications discontinued because of side effects?
* Has she recently undergone any diagnostic tests?

The answers exist—but in different hospital systems.

Dr. Arjun asks Sameera if she remembers the names of her medications. She only recalls that "one of them started with A." She opens three different patient portals on her phone, but one account is locked, another doesn't show prescriptions from specialist visits, and the emergency department records aren't available.

The previous hospital can share the records only after a formal request, a process that may take hours or even days depending on their workflow and interoperability capabilities. Meanwhile, the consultation is happening now.

Without a complete medication history, Dr. Arjun cannot confidently perform medication reconciliation—the process of verifying a patient's current and previous medications to prevent harmful interactions or duplicate therapies. To prioritize patient safety, he prescribes conservatively and asks Sameera to return once the missing records become available. 

---

### Why This Matters

Healthcare has become increasingly digital, but medical records often remain fragmented across hospitals and healthcare providers. Patients are frequently responsible for piecing together their own medical history, while clinicians spend valuable consultation time requesting records, verifying medications, and repeating tests that have already been performed elsewhere. 

**Patients shouldn't have to remember every prescription they've ever received. Doctors shouldn't have to make clinical decisions with incomplete information.**

## The Solution

A secure platform where patients own their medical data across hospitals, doctors access it with explicit consent, and every record is immutably tracked.

---

## What It Does

### For Patients

Register once. Book appointments at any hospital with OTP confirmation. View your complete medical timeline — prescriptions, diagnoses, upcoming visits — all in one place across all hospitals you've visited. Grant or revoke a doctor's access to your records. Download your prescriptions as PDFs. Get AI-generated summaries of your prescriptions.

### For Doctors

Register under a hospital. Create available time slots. Manage appointments through their full lifecycle — book, start, complete, cancel. Prescribe medications with idempotency protection (no duplicates). Generate AI-powered clinical summaries for both yourself and your patients. View a patient's complete history across all hospitals where you have their consent.

### For Admins

Add and manage hospitals, departments, and doctors. Full visibility across the entire platform.

### Behind the Scenes

- **10 microservices** working together — each with its own database (PostgreSQL, MongoDB), communicating via REST, gRPC, and async Kafka events.
- **Event-driven core** — appointments, prescriptions, consent grants, and user registrations all produce Kafka events consumed by multiple services. No tight coupling, no lost messages.
- **CQRS read model** — the patient timeline is a pre-computed view in MongoDB, populated by Kafka consumers. Reads are fast and don't aggregate from multiple services at request time.
- **Transactional outbox pattern** — every Kafka event is written to a database table within the same transaction as the domain change, then reliably published by a background poller. No events are lost on failure.
- **Consent-gated access** — patients grant cross-hospital access via OTP. The consent is cached in Redis and checked at read time before any data is returned. 24-hour TTL with automatic expiry.
- **Immutable prescriptions** — once created, a prescription is never modified. Full audit trail with version tracking. Rate-limited and idempotency-key protected.
- **Async PDF generation** — prescriptions trigger a Kafka pipeline that generates PDFs in the background using Thymeleaf + Flying Saucer. The doctor gets a response immediately.
- **AI integration** — clinical summaries powered by Spring AI with Ollama (gemma4) and SearXNG for web-grounded context. Available to both doctors and patients.
- **OTP state machines** — appointments and consent use dual OTP flows (REST + gRPC) with SHA-256 hashed codes in Redis, 3-strike brute-force protection, 60s cooldown, and 180s TTL.
- **Resilience patterns** — circuit breakers on all gRPC calls, rate limiting via Bucket4j + Redis, idempotency keys on critical writes.
- **Full observability** — distributed tracing (OpenTelemetry + Zipkin), metrics (Micrometer + Prometheus + Grafana with pre-built dashboards), centralized logging (ELK + Loki), plus structured JSON logging via GELF.
- **Service discovery** — all services register with Eureka for dynamic resolution, including gRPC channels via the `discovery:///` scheme.

---

## Built With

Java 21, Spring Boot 3.5, Spring Cloud, Spring Security (JWT/OAuth2), gRPC, Apache Kafka, PostgreSQL, MongoDB, Redis, Resilience4j, Bucket4j, OpenTelemetry, Zipkin, Prometheus, Grafana, ELK Stack, Loki, Thymeleaf, Flying Saucer, Spring AI, Ollama, SearXNG, Eureka, Docker Compose, JUnit 5, RestAssured, Testcontainers.

---

## Quick Start

```bash
# Prerequisites: Docker Compose v2.20+, Java 21, Maven 3.9+

# 1. Build all service JARs (parallel)
bash build-jars.sh

# 2. Start everything
docker compose up -d

# 3. Verify
open http://localhost:8761          # Service discovery dashboard
curl http://localhost:4004/actuator/health
```

---

## Project Structure

```
├── api-gateway/              # Entry point — routes, auth, rate limiting
├── auth-service/             # Login, registration, JWT
├── patient-service/          # Patient profiles, consent management
├── hospital-service/         # Hospitals, departments, doctors
├── schedule-service/         # Time slots, appointments
├── clinical-service/         # Prescriptions, PDF generation, AI
├── notification-service/     # OTP, email notifications
├── timeline-service/         # Unified patient timeline
├── billing-service/          # Billing (stubbed)
├── eureka-server/            # Service registry
├── integration-tests/        # End-to-end tests
```

---


