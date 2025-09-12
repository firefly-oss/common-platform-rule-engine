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

package com.firefly.rules.core.services;

import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import com.firefly.rules.interfaces.dtos.crud.RuleDefinitionDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for managing YAML DSL rule definitions.
 * Provides CRUD operations with validation and business logic.
 */
public interface RuleDefinitionService {

    /**
     * Filter rule definitions based on criteria with pagination.
     * 
     * @param filterRequest The filter criteria and pagination parameters
     * @return Mono containing paginated rule definitions
     */
    Mono<PaginationResponse<RuleDefinitionDTO>> filterRuleDefinitions(
            FilterRequest<RuleDefinitionDTO> filterRequest);

    /**
     * Create a new rule definition.
     * Validates the YAML DSL content before storage.
     * 
     * @param ruleDefinitionDTO The rule definition to create
     * @return Mono containing the created rule definition
     */
    Mono<RuleDefinitionDTO> createRuleDefinition(RuleDefinitionDTO ruleDefinitionDTO);

    /**
     * Update an existing rule definition.
     * Validates the YAML DSL content before storage.
     * 
     * @param ruleDefinitionId The ID of the rule definition to update
     * @param ruleDefinitionDTO The updated rule definition data
     * @return Mono containing the updated rule definition
     */
    Mono<RuleDefinitionDTO> updateRuleDefinition(UUID ruleDefinitionId, RuleDefinitionDTO ruleDefinitionDTO);

    /**
     * Delete a rule definition by ID.
     * 
     * @param ruleDefinitionId The ID of the rule definition to delete
     * @return Mono<Void> indicating completion
     */
    Mono<Void> deleteRuleDefinition(UUID ruleDefinitionId);

    /**
     * Get a rule definition by ID.
     * 
     * @param ruleDefinitionId The ID of the rule definition
     * @return Mono containing the rule definition if found
     */
    Mono<RuleDefinitionDTO> getRuleDefinitionById(UUID ruleDefinitionId);

    /**
     * Get a rule definition by its unique code.
     * 
     * @param code The unique code identifier
     * @return Mono containing the rule definition if found
     */
    Mono<RuleDefinitionDTO> getRuleDefinitionByCode(String code);

    /**
     * Validate a YAML DSL rule definition without storing it.
     *
     * @param yamlContent The YAML DSL content to validate
     * @return Mono containing validation results
     */
    Mono<com.firefly.rules.interfaces.dtos.validation.ValidationResult> validateRuleDefinition(String yamlContent);



    /**
     * Evaluate a stored rule definition by code with input data.
     *
     * @param code The code of the rule definition to evaluate
     * @param inputData The input data for evaluation
     * @return Mono containing the evaluation result
     */
    Mono<com.firefly.rules.core.dsl.evaluation.RulesEvaluationResult> evaluateRuleByCode(String code, java.util.Map<String, Object> inputData);
}
