package com.pms.clinicalservice.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearXNGSearchClient {

    private static final Logger log = LoggerFactory.getLogger(SearXNGSearchClient.class);

    private final RestClient restClient;

    public SearXNGSearchClient(@Value("${searxng.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String fetchContext(List<String> queries) {
        if (queries == null || queries.isEmpty()) return "";

        return queries.stream()
                .map(this::search)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("\n---\n"));
    }

    private String search(String query) {
        try {
            SearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "json")
                            .queryParam("language", "en-US")
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                return "";
            }

            return response.results().stream()
                    .map(r -> r.title() + ": " + r.content())
                    .collect(Collectors.joining("\n"));

        } catch (Exception e) {
            log.warn("SearXNG search failed for query '{}': {}", query, e.getMessage());
            return "";
        }
    }

    record SearchResult(String title, String url, String content, double score) {}
    record SearchResponse(List<SearchResult> results) {}
}
