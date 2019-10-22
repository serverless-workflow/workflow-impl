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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.branches.Branch;
import org.serverless.workflow.api.choices.AndChoice;
import org.serverless.workflow.api.choices.NotChoice;
import org.serverless.workflow.api.choices.OrChoice;
import org.serverless.workflow.api.choices.SingleChoice;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.states.DefaultState;
import org.serverless.workflow.api.states.DelayState;
import org.serverless.workflow.api.states.EndState;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.api.states.OperationState;
import org.serverless.workflow.api.states.ParallelState;
import org.serverless.workflow.api.states.SwitchState;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonToWorkflowTest extends BaseWorkflowTest {

    @ParameterizedTest
    @ValueSource(strings = {"basic/emptyworkflow.json", "basic/emptyworkflow.yml"})
    public void testEmptyWorkflow(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();

        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/workflowwithmetadata.json", "basic/workflowwithmetadata.yml"})
    public void testSimpleWorkflowWithMetadata(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();

        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(0));

        assertNotNull(workflow.getMetadata());
        assertEquals(2,
                     workflow.getMetadata().size());
        assertEquals("value1",
                     workflow.getMetadata().get("key1"));
        assertEquals("value2",
                     workflow.getMetadata().get("key2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singletriggerevent.json", "basic/singletriggerevent.yml"})
    public void testTrigger(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();

        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(1));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(0));

        assertEquals("test-trigger",
                     workflow.getTriggerDefs().get(0).getName());
        assertEquals("testsource",
                     workflow.getTriggerDefs().get(0).getSource());
        assertEquals("testeventtype",
                     workflow.getTriggerDefs().get(0).getType());
        assertEquals("testcorrelationtoken",
                     workflow.getTriggerDefs().get(0).getCorrelationToken());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleendstate.json", "basic/singleendstate.yml"})
    public void testEndState(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EndState);

        EndState endState = (EndState) workflow.getStates().get(0);
        assertEquals(EndState.Status.SUCCESS,
                     endState.getStatus());
        assertFalse(endState.isStart());
        assertEquals(EndState.Type.END,
                     endState.getType());
        assertEquals("test-state",
                     endState.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleeventstate.json", "basic/singleeventstate.yml"})
    public void testEventState(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EventState);

        EventState eventState = (EventState) workflow.getStates().get(0);
        assertEquals("test-state",
                     eventState.getName());
        assertTrue(eventState.isStart());
        assertEquals(EventState.Type.EVENT,
                     eventState.getType());

        assertNotNull(eventState.getEvents());
        assertEquals(1,
                     eventState.getEvents().size());

        Event event = eventState.getEvents().get(0);
        assertEquals("testNextState",
                     event.getNextState());
        assertEquals("testEventExpression",
                     event.getEventExpression());
        assertEquals(Event.ActionMode.SEQUENTIAL,
                     event.getActionMode());

        assertNotNull(event.getActions());
        assertEquals(1,
                     event.getActions().size());
        assertEquals("testFunction",
                     event.getActions().get(0).getFunction().getName());
        assertNotNull(event.getActions().get(0).getRetry());

        assertEquals("testMatch",
                     event.getActions().get(0).getRetry().getMatch());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singledelaystate.json", "basic/singledelaystate.yml"})
    public void testDelayState(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof DelayState);

        DelayState delayState = (DelayState) workflow.getStates().get(0);
        assertEquals("testNextState",
                     delayState.getNextState());
        assertEquals("test-state",
                     delayState.getName());
        assertEquals(EventState.Type.DELAY,
                     delayState.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleoperationstate.json", "basic/singleoperationstate.yml"})
    public void testOperationState(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof OperationState);

        OperationState operationState = (OperationState) workflow.getStates().get(0);
        assertEquals("testnextstate",
                     operationState.getNextState());
        assertEquals("test-state",
                     operationState.getName());
        assertEquals(EventState.Type.OPERATION,
                     operationState.getType());

        assertNotNull(operationState.getActions());
        assertEquals(1,
                     operationState.getActions().size());

        Action action = operationState.getActions().get(0);
        assertEquals("testFunction",
                     action.getFunction().getName());
        assertNotNull(action.getRetry());
        assertEquals("testMatch",
                     action.getRetry().getMatch());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleparallelstate.json", "basic/singleparallelstate.yml"})
    public void testParallellState(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof ParallelState);

        ParallelState parallelState = (ParallelState) workflow.getStates().get(0);

        assertEquals("testnextstate",
                     parallelState.getNextState());
        assertEquals("test-state",
                     parallelState.getName());
        assertEquals(EventState.Type.PARALLEL,
                     parallelState.getType());

        assertNotNull(parallelState.getBranches());
        assertEquals(2,
                     parallelState.getBranches().size());

        Branch branch1 = parallelState.getBranches().get(0);
        assertEquals("firsttestbranch",
                     branch1.getName());
        assertNotNull(branch1.getStates());
        assertEquals(1,
                     branch1.getStates().size());
        assertTrue(branch1.getStates().get(0) instanceof OperationState);
        assertEquals("operationstate",
                     ((OperationState) branch1.getStates().get(0)).getName());
        assertEquals(1,
                     ((OperationState) branch1.getStates().get(0)).getActions().size());
        assertEquals("testFunction",
                     ((OperationState) branch1.getStates().get(0)).getActions().get(0).getFunction().getName());
        assertFalse(branch1.isWaitForCompletion());

        Branch branch2 = parallelState.getBranches().get(1);
        assertEquals("secondtestbranch",
                     branch2.getName());
        assertNotNull(branch2.getStates());
        assertEquals(1,
                     branch2.getStates().size());
        assertTrue(branch2.getStates().get(0) instanceof DelayState);
        assertEquals("delaystate",
                     ((DelayState) branch2.getStates().get(0)).getName());
        assertEquals("testNextState",
                     ((DelayState) branch2.getStates().get(0)).getNextState());
        assertEquals(5,
                     ((DelayState) branch2.getStates().get(0)).getTimeDelay());
        assertTrue(branch2.isWaitForCompletion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleswitchstateandchoice.json", "basic/singleswitchstateandchoice.yml"})
    public void testSwitchStateAndChoice(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        assertThat(workflow.getTriggerDefs().size(),
                   is(0));
        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof SwitchState);

        SwitchState switchState = (SwitchState) workflow.getStates().get(0);
        assertEquals("test-state",
                     switchState.getName());
        assertEquals(DefaultState.Type.SWITCH,
                     switchState.getType());
        assertEquals("defaultteststate",
                     switchState.getDefault());

        assertNotNull(switchState.getChoices());
        assertThat(switchState.getChoices().size(),
                   is(1));
        assertTrue(switchState.getChoices().get(0) instanceof AndChoice);

        workflowManager.setMarkup(getFileContents(getResourcePath("basic/singleswitchstatenotchoice.json")));
        workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        switchState = (SwitchState) workflow.getStates().get(0);
        assertTrue(switchState.getChoices().get(0) instanceof NotChoice);

        workflowManager.setMarkup(getFileContents(getResourcePath("basic/singleswitchstateorchoice.json")));
        workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        switchState = (SwitchState) workflow.getStates().get(0);
        assertTrue(switchState.getChoices().get(0) instanceof OrChoice);
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleswitchstatenotchoice.json", "basic/singleswitchstatenotchoice.yml"})
    public void testSwitchStateNotChoice(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        SwitchState switchState = (SwitchState) workflow.getStates().get(0);
        assertTrue(switchState.getChoices().get(0) instanceof NotChoice);
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleswitchstateorchoice.json", "basic/singleswitchstateorchoice.yml"})
    public void testSwitchStateOrChoice(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        SwitchState switchState = (SwitchState) workflow.getStates().get(0);
        assertTrue(switchState.getChoices().get(0) instanceof OrChoice);
    }

    @ParameterizedTest
    @ValueSource(strings = {"basic/singleswitchstatesinglechoice.json", "basic/singleswitchstatesinglechoice.yml"})
    public void testSwitchStateSingleChoice(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);
        SwitchState switchState = (SwitchState) workflow.getStates().get(0);
        assertTrue(switchState.getChoices().get(0) instanceof SingleChoice);
    }
}
