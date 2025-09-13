/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.rules.core.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.rules.core.services.RestCallService;
import com.firefly.rules.core.utils.JsonLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of RestCallService using Spring WebClient for reactive HTTP operations.
 * Provides robust error handling, timeout management, and JSON parsing.
 */
@Slf4j
@Service
public class RestCallServiceImpl implements RestCallService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    private static final long DEFAULT_TIMEOUT_MS = 5000L;
    private static final String USER_AGENT = "Firefly-Rule-Engine/1.0";
    
    public RestCallServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Map<String, Object>> get(String url, Map<String, String> headers, Long timeoutMs) {
        return request("GET", url, null, headers, timeoutMs);
    }
    
    @Override
    public Mono<Map<String, Object>> post(String url, Object body, Map<String, String> headers, Long timeoutMs) {
        return request("POST", url, body, headers, timeoutMs);
    }
    
    @Override
    public Mono<Map<String, Object>> put(String url, Object body, Map<String, String> headers, Long timeoutMs) {
        return request("PUT", url, body, headers, timeoutMs);
    }
    
    @Override
    public Mono<Map<String, Object>> delete(String url, Map<String, String> headers, Long timeoutMs) {
        return request("DELETE", url, null, headers, timeoutMs);
    }
    
    @Override
    public Mono<Map<String, Object>> patch(String url, Object body, Map<String, String> headers, Long timeoutMs) {
        return request("PATCH", url, body, headers, timeoutMs);
    }
    
    @Override
    public Mono<Map<String, Object>> request(String method, String url, Object body, 
                                           Map<String, String> headers, Long timeoutMs) {
        
        long timeout = timeoutMs != null ? timeoutMs : DEFAULT_TIMEOUT_MS;
        String operationId = java.util.UUID.randomUUID().toString();
        
        JsonLogger.info(log, operationId, String.format("Making %s request to: %s", method, url));
        
        return webClient
                .method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::add);
                    }
                })
                .bodyValue(body != null ? body : "")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .map(this::parseJsonResponse)
                .doOnSuccess(response -> JsonLogger.info(log, operationId, 
                    String.format("REST call successful: %s %s", method, url)))
                .doOnError(error -> JsonLogger.error(log, operationId, 
                    String.format("REST call failed: %s %s", method, url), error))
                .onErrorResume(this::handleRestError);
    }
    
    /**
     * Parse JSON response string into a Map
     */
    private Map<String, Object> parseJsonResponse(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return new HashMap<>();
            }
            
            // Try to parse as JSON object first
            return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            
        } catch (Exception e) {
            JsonLogger.warn(log, "Failed to parse JSON response, returning as string: " + e.getMessage());
            // If parsing fails, return the raw response as a "data" field
            Map<String, Object> result = new HashMap<>();
            result.put("data", jsonResponse);
            result.put("_raw", true);
            return result;
        }
    }
    
    /**
     * Handle REST call errors and convert them to meaningful responses
     */
    private Mono<Map<String, Object>> handleRestError(Throwable error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", true);
        
        if (error instanceof WebClientResponseException webClientError) {
            errorResponse.put("status", webClientError.getStatusCode().value());
            errorResponse.put("message", webClientError.getMessage());
            errorResponse.put("body", webClientError.getResponseBodyAsString());
        } else {
            errorResponse.put("message", error.getMessage());
            errorResponse.put("type", error.getClass().getSimpleName());
        }
        
        return Mono.just(errorResponse);
    }
}
