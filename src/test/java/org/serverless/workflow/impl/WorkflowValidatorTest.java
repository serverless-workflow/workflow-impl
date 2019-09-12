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

package org.serverless.workflow.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.validation.ValidationError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkflowValidatorTest extends BaseWorkflowTest {

    @Test
    public void testEmptyJson() {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson("{}");
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(4,
                     validationErrorList.size());
    }

    @Test
    public void testNoData() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("basic/emptyworkflow.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(2,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testInvalidTriggerEvent() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/invalidtrigger.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();
        assertEquals(4,
                     validationErrorList.size());

        expectError(validationErrorList,
                    "#/trigger-defs/0: required key [name] not found",
                    ValidationError.SCHEMA_VALIDATION);
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "Trigger Event has no name",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testInvalidTriggerEventNotUniqueProperties() {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/invalidtriggerproperties.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(3,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "Trigger Event does not have unique name: testtriggerevent",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testMultipleStartStates() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/multiplestartstates.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(1,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "Multiple start states found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testMultipleEndStatesInStrictMode() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/multipleendstates.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setStrictValidationEnabled(true);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(2,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "Multiple end states found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testValidationDisabled() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson("{}");
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setEnabled(false);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(0,
                     validationErrorList.size());
    }

    @Test
    public void testSchemaValidationDisabled() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("basic/emptyworkflow.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setSchemaValidationEnabled(false);

        List<ValidationError> validationErrorList = workflowValidator.validate();
        assertEquals(2,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testStrictValidationEnabled() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("basic/emptyworkflow.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setSchemaValidationEnabled(false);
        workflowValidator.setStrictValidationEnabled(true);

        List<ValidationError> validationErrorList = workflowValidator.validate();
        assertEquals(3,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No end state found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testEmptyNextState() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/emptynextstate.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(1,
                     validationErrorList.size());

        expectError(validationErrorList,
                    "Next state should not be empty.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @Test
    public void testEmptyName() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/emptyname.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(2,
                     validationErrorList.size());
    }

    @Test
    public void testInvalidStateDefinition() {


        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setJson(getFileContents(getResourcePath("validation/invalidstate.json")));
                     });
    }

    @Test
    public void testInvalidStateType() {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setJson(getFileContents(getResourcePath("validation/invalidstatetype.json")));
                     });
    }

    @Test
    public void testInvalidActionMode() {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setJson(getFileContents(getResourcePath("validation/invalidactionmode.json")));
                     });
    }

    @Test
    public void testInvalidOperator() {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setJson(getFileContents(getResourcePath("validation/invalidoperator.json")));
                     });
    }

    @Test
    public void testInvalidStatus() {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setJson(getFileContents(getResourcePath("validation/invalidstatus.json")));
                     });
    }

    @Test
    public void testUniqueStateName() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setJson(getFileContents(getResourcePath("validation/duplicatedstateid.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(1,
                     validationErrorList.size());

        expectError(validationErrorList,
                    "State does not have a unique name: duplicated",
                    ValidationError.WORKFLOW_VALIDATION);
    }
}
