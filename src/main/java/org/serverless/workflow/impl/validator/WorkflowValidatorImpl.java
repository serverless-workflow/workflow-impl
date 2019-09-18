/*
 *
 *   Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.serverless.workflow.impl.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.states.DelayState;
import org.serverless.workflow.api.states.EndState;
import org.serverless.workflow.api.states.OperationState;
import org.serverless.workflow.api.states.ParallelState;
import org.serverless.workflow.api.states.SwitchState;
import org.serverless.workflow.api.validation.ValidationError;
import org.serverless.workflow.api.validation.WorkflowSchemaLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowValidatorImpl implements WorkflowValidator {

    private boolean enabled = true;
    private boolean schemaValidationEnabled = true;
    private boolean strictValidationEnabled = false;
    private List<ValidationError> validationErrors = new ArrayList<>();
    private Schema workflowSchema = WorkflowSchemaLoader.getWorkflowSchema();
    private WorkflowManager workflowManager;

    private static final Logger logger = LoggerFactory.getLogger(WorkflowValidatorImpl.class);

    @Override
    public void reset() {
        validationErrors.clear();
        enabled = true;
        schemaValidationEnabled = true;
        strictValidationEnabled = false;
    }

    @Override
    public WorkflowValidator setWorkflowManager(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
        return this;
    }

    @Override
    public List<ValidationError> validate() {
        validationErrors.clear();
        if (enabled) {
            try {
                if (schemaValidationEnabled && workflowManager.toJson() != null) {
                    try {
                        workflowSchema.validate(new JSONObject(workflowManager.toJson()));
                    } catch (ValidationException e) {
                        // main error
                        addValidationError(e.getMessage(),
                                           ValidationError.SCHEMA_VALIDATION);
                        // suberrors
                        e.getCausingExceptions().stream()
                                .map(ValidationException::getMessage)
                                .forEach(m -> addValidationError(m,
                                                                 ValidationError.SCHEMA_VALIDATION));
                    }
                }

                if (workflowManager.getWorkflow() != null) {
                    Workflow workflow = workflowManager.getWorkflow();
                    if (workflow.getName() == null || workflow.getName().trim().isEmpty()) {
                        addValidationError("Workflow name should not be empty",
                                           ValidationError.WORKFLOW_VALIDATION);
                    }
                    // make sure we have at least one state
                    if (workflow.getStates() == null || workflow.getStates().isEmpty()) {
                        addValidationError("No states found.",
                                           ValidationError.WORKFLOW_VALIDATION);
                    }

                    // make sure we have one start state and check for null next id and next-state
                    final Validation validation = new Validation();
                    if (workflow.getStates() != null) {
                        workflow.getStates().forEach(s -> {
                            if (s.getName() != null && s.getName().trim().isEmpty()) {
                                addValidationError("Name should not be empty.",
                                                   ValidationError.WORKFLOW_VALIDATION);
                            } else {
                                validation.addState(s.getName());
                            }
                            if (s.isStart()) {
                                validation.addStartState();
                            }

                            if (s instanceof OperationState) {
                                OperationState operationState = (OperationState) s;

                                if (operationState.getNextState() == null || operationState.getNextState().trim().isEmpty()) {
                                    addValidationError("Next state should not be empty.",
                                                       ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                            if (s instanceof SwitchState) {
                                SwitchState switchState = (SwitchState) s;

                                if (switchState.getDefault() == null || switchState.getDefault().trim().isEmpty()) {
                                    addValidationError("Default should not be empty.",
                                                       ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                            if (s instanceof ParallelState) {
                                ParallelState parallelState = (ParallelState) s;

                                if (parallelState.getNextState() == null || parallelState.getNextState().trim().isEmpty()) {
                                    addValidationError("Next state should not be empty.",
                                                       ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                            if (s instanceof DelayState) {
                                DelayState delayState = (DelayState) s;

                                if (delayState.getNextState() == null || delayState.getNextState().trim().isEmpty()) {
                                    addValidationError("Next state should not be empty.",
                                                       ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                            if (s instanceof EndState) {
                                validation.addEndState();
                            }
                        });
                    }

                    if (validation.startStates == 0) {
                        addValidationError("No start state found.",
                                           ValidationError.WORKFLOW_VALIDATION);
                    }

                    if (validation.startStates > 1) {
                        addValidationError("Multiple start states found.",
                                           ValidationError.WORKFLOW_VALIDATION);
                    }

                    if (strictValidationEnabled) {
                        if (validation.endStates == 0) {
                            addValidationError("No end state found.",
                                               ValidationError.WORKFLOW_VALIDATION);
                        }

                        if (validation.endStates > 1) {
                            addValidationError("Multiple end states found.",
                                               ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                    // make sure if we have trigger events that they unique name
                    if (workflow.getTriggerDefs() != null) {
                        workflow.getTriggerDefs().forEach(triggerEvent -> {
                            if (triggerEvent.getName() == null || triggerEvent.getName().isEmpty()) {
                                addValidationError("Trigger Event has no name",
                                                   ValidationError.WORKFLOW_VALIDATION);
                            } else {
                                validation.addEvent(triggerEvent.getName());
                            }
                            if (triggerEvent.getEventID() == null || triggerEvent.getEventID().isEmpty()) {
                                addValidationError("Trigger Event has no event id",
                                                   ValidationError.WORKFLOW_VALIDATION);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading schema: " + e.getMessage());
            }
        }

        return validationErrors;
    }

    @Override
    public boolean isValid() {
        return validate().size() < 1;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setSchemaValidationEnabled(boolean schemaValidationEnabled) {
        this.schemaValidationEnabled = schemaValidationEnabled;
    }

    @Override
    public void setStrictValidationEnabled(boolean strictValidationEnabled) {
        this.strictValidationEnabled = strictValidationEnabled;
    }

    private void addValidationError(String message,
                                    String type) {
        ValidationError mainError = new ValidationError();
        mainError.setMessage(message);
        mainError.setType(type);
        validationErrors.add(mainError);
    }

    private class Validation {

        final Set<String> events = new HashSet<>();
        final Set<String> states = new HashSet<>();
        Integer startStates = 0;
        Integer endStates = 0;

        void addEvent(String name) {
            if (events.contains(name)) {
                addValidationError("Trigger Event does not have unique name: " + name,
                                   ValidationError.WORKFLOW_VALIDATION);
            } else {
                events.add(name);
            }
        }

        void addState(String name) {
            if (states.contains(name)) {
                addValidationError("State does not have a unique name: " + name,
                                   ValidationError.WORKFLOW_VALIDATION);
            } else {
                states.add(name);
            }
        }

        void addStartState() {
            startStates++;
        }

        void addEndState() {
            endStates++;
        }
    }
}
