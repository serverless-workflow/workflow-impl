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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        workflowManager.setMarkup("{}");
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(6,
                     validationErrorList.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/emptyworkflow.json", "basic/emptyworkflow.yml"})
    public void testNoData(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(4,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "Workflow does not define a start state",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No end state found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidtrigger.json", "validation/invalidtrigger.yml"})
    public void testInvalidTriggerEvent(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();
        assertEquals(6,
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

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidtriggerproperties.json", "validation/invalidtriggerproperties.yml"})
    public void testInvalidTriggerEventNotUniqueProperties(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(5,
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
    public void testValidationDisabled() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup("{}");
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setEnabled(false);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(0,
                     validationErrorList.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/emptyworkflow.json", "basic/emptyworkflow.yml"})
    public void testSchemaValidationDisabled(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setSchemaValidationEnabled(false);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(4,
                     validationErrorList.size());
        expectError(validationErrorList,
                    "No states found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No start state found.",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "Workflow does not define a start state",
                    ValidationError.WORKFLOW_VALIDATION);
        expectError(validationErrorList,
                    "No end state found.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/emptyworkflow.json", "basic/emptyworkflow.yml"})
    public void testStrictValidationEnabled(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);
        workflowValidator.setSchemaValidationEnabled(false);
        workflowValidator.setStrictValidationEnabled(true);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(4,
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
        expectError(validationErrorList,
                    "Workflow does not define a start state",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/emptynextstate.json", "validation/emptynextstate.yml"})
    public void testEmptyNextState(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(1,
                     validationErrorList.size());

        expectError(validationErrorList,
                    "Next state should not be empty.",
                    ValidationError.WORKFLOW_VALIDATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/emptyname.json", "validation/emptyname.yml"})
    public void testEmptyName() {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath("validation/emptyname.json")));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(2,
                     validationErrorList.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidstate.json", "validation/invalidstate.yml"})
    public void testInvalidStateDefinition(String model) {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setMarkup(getFileContents(getResourcePath(model)));
                     });
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidstatetype.json", "validation/invalidstatetype.yml"})
    public void testInvalidStateType(String model) {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setMarkup(getFileContents(getResourcePath(model)));
                     });
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidactionmode.json", "validation/invalidactionmode.yml"})
    public void testInvalidActionMode(String model) {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setMarkup(getFileContents(getResourcePath(model)));
                     });
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidoperator.json", "validation/invalidoperator.yml"})
    public void testInvalidOperator(String model) {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setMarkup(getFileContents(getResourcePath(model)));
                     });
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/invalidstatus.json", "validation/invalidstatus.yml"})
    public void testInvalidStatus(String model) {

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         WorkflowManager workflowManager = getWorkflowManager();
                         assertNotNull(workflowManager);
                         workflowManager.setMarkup(getFileContents(getResourcePath(model)));
                     });
    }

    @ParameterizedTest
    @ValueSource(strings = {"validation/duplicatedstateid.json", "validation/duplicatedstateid.yml"})
    public void testUniqueStateName(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertNotNull(workflowValidator);

        List<ValidationError> validationErrorList = workflowValidator.validate();

        assertEquals(3,
                     validationErrorList.size());

        expectError(validationErrorList,
                    "State does not have a unique name: duplicated",
                    ValidationError.WORKFLOW_VALIDATION);
    }
}
