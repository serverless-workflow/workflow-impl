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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.actions.Retry;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.TriggerEvent;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.impl.utils.WorkflowUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.serverless.workflow.impl.util.IsEqualJSON.equalToJSONInFile;

public class WorkflowManagerTest extends BaseWorkflowTest {

    @ParameterizedTest
    @ValueSource(strings = {"controller/eventstatewithtrigger.json", "controller/eventstatewithtrigger.yml"})
    public void testManagerFromMarkup(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
        assertTrue(workflowValidator.isValid());

        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EventState);

        EventState eventState = (EventState) workflow.getStates().get(0);
        assertEquals("test-state",
                     eventState.getName());
        assertEquals(EventState.Type.EVENT,
                     eventState.getType());

        assertNotNull(eventState.getEvents());
        assertEquals(1,
                     eventState.getEvents().size());

        Event event = eventState.getEvents().get(0);
        assertEquals("testNextState",
                     event.getNextState());
        assertNotNull(event.getActions());

        assertEquals(1,
                     event.getActions().size());

        assertNotNull(workflow.getTriggerDefs());
        assertEquals(1,
                     workflow.getTriggerDefs().size());

        assertTrue(WorkflowUtils.haveTriggers(workflowManager));

        assertTrue(WorkflowUtils.haveStates(workflowManager));

        assertEquals(1,
                     WorkflowUtils.getUniqueStates(workflowManager).size());
        assertEquals(1,
                     WorkflowUtils.getUniqueTriggerEvents(workflowManager).size());

        TriggerEvent triggerEvent = WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger");
        assertNotNull(triggerEvent);

        List<EventState> eventStatesForTrigger = WorkflowUtils.getEventStatesForTriggerEvent(triggerEvent,
                                                                                             workflowManager);
        assertNotNull(eventStatesForTrigger);
        assertEquals(1,
                     eventStatesForTrigger.size());
        EventState eventStateForTrigger = eventStatesForTrigger.get(0);
        assertEquals("test-state",
                     eventStateForTrigger.getName());
    }

    @Test
    public void testManagerFromWorkflow() {
        Workflow workflow = new Workflow().withName("test-wf")
                .withTriggerDefs(
                        Arrays.asList(
                                new TriggerEvent().withName("test-trigger").withType("testeventtype")
                                        .withCorrelationToken("testcorrelationtoken").withSource("testsource")
                        )
                )
                .withStates(new ArrayList<State>() {{
                    add(new EventState().withStart(true).withName("test-state").withType(EventState.Type.EVENT)
                                .withEvents(Arrays.asList(
                                        new Event().withEventExpression("trigger.equals('test-trigger')").withTimeout("testTimeout")
                                                .withActionMode(Event.ActionMode.SEQUENTIAL)
                                                .withNextState("testNextState")
                                                .withActions(Arrays.asList(
                                                        new Action().withFunction(new Function().withName("testFunction"))
                                                                .withTimeout(5)
                                                                .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                                                   .withRetryInterval(2)
                                                                                   .withNextState("testNextRetryState"))
                                                ))
                                )));
                }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflow.getStates());
        assertThat(workflow.getStates().size(),
                   is(1));
        assertTrue(workflow.getStates().get(0) instanceof EventState);

        EventState eventState = (EventState) workflow.getStates().get(0);
        assertEquals("test-state",
                     eventState.getName());
        assertEquals(EventState.Type.EVENT,
                     eventState.getType());

        assertNotNull(eventState.getEvents());
        assertEquals(1,
                     eventState.getEvents().size());

        Event event = eventState.getEvents().get(0);
        assertEquals("testNextState",
                     event.getNextState());
        assertNotNull(event.getActions());

        assertEquals(1,
                     event.getActions().size());

        assertNotNull(workflow.getTriggerDefs());
        assertEquals(1,
                     workflow.getTriggerDefs().size());

        assertTrue(WorkflowUtils.haveTriggers(workflowManager));

        assertTrue(WorkflowUtils.haveStates(workflowManager));

        assertEquals(1,
                     WorkflowUtils.getUniqueStates(workflowManager).size());
        assertEquals(1,
                     WorkflowUtils.getUniqueTriggerEvents(workflowManager).size());

        TriggerEvent triggerEvent = WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger");
        assertNotNull(triggerEvent);

        List<EventState> eventStatesForTrigger = WorkflowUtils.getEventStatesForTriggerEvent(triggerEvent,
                                                                                             workflowManager);
        assertNotNull(eventStatesForTrigger);
        assertEquals(1,
                     eventStatesForTrigger.size());
        EventState eventStateForTrigger = eventStatesForTrigger.get(0);
        assertEquals("test-state",
                     eventStateForTrigger.getName());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("controller/eventstatewithtrigger.json")));

        assertEquals(workflowManager.toYaml(), getFileContents(getResourcePath("controller/eventstatewithtrigger.yml")));

    }
}
