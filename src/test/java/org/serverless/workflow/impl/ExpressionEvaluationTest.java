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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.events.TriggerEvent;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.impl.utils.WorkflowUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionEvaluationTest extends BaseWorkflowTest {

    @ParameterizedTest
    @ValueSource(strings = {"expressions/eventstatestriggers-jexl.json", "expressions/eventstatestriggers-jexl.yml"})
    public void testEventStateJexlExpressions(String model) {

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        assertNotNull(workflowManager.getWorkflowValidator());

        assertTrue(workflowManager.getWorkflowValidator().isValid());

        assertTrue(WorkflowUtils.haveTriggers(workflowManager));
        assertTrue(WorkflowUtils.haveStates(workflowManager));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertThat(workflow.getTriggerDefs().size(),
                   is(3));

        assertThat(workflow.getStates().size(),
                   is(7));

        List<EventState> eventStatesForTrigger1 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-1"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger1);
        assertEquals(5,
                     eventStatesForTrigger1.size());
        EventState eventStateForTrigger11 = eventStatesForTrigger1.get(0);
        assertEquals("test-state-1",
                     eventStateForTrigger11.getName());
        EventState eventStateForTrigger12 = eventStatesForTrigger1.get(1);
        assertEquals("test-state-2",
                     eventStateForTrigger12.getName());
        EventState eventStateForTrigger13 = eventStatesForTrigger1.get(2);
        assertEquals("test-state-3",
                     eventStateForTrigger13.getName());
        EventState eventStateForTrigger14 = eventStatesForTrigger1.get(3);
        assertEquals("test-state-4",
                     eventStateForTrigger14.getName());
        EventState eventStateForTrigger15 = eventStatesForTrigger1.get(4);
        assertEquals("test-state-7",
                     eventStateForTrigger15.getName());

        List<EventState> eventStatesForTrigger2 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-2"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger2);
        assertEquals(3,
                     eventStatesForTrigger2.size());
        EventState eventStateForTrigger21 = eventStatesForTrigger2.get(0);
        assertEquals("test-state-2",
                     eventStateForTrigger21.getName());
        EventState eventStateForTrigger22 = eventStatesForTrigger2.get(1);
        assertEquals("test-state-4",
                     eventStateForTrigger22.getName());
        EventState eventStateForTrigger23 = eventStatesForTrigger2.get(2);
        assertEquals("test-state-7",
                     eventStateForTrigger23.getName());

        List<EventState> eventStatesForTrigger3 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-3"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger3);
        assertEquals(2,
                     eventStatesForTrigger3.size());
        EventState eventStateForTrigger31 = eventStatesForTrigger3.get(0);
        assertEquals("test-state-5",
                     eventStateForTrigger31.getName());
        EventState eventStateForTrigger32 = eventStatesForTrigger3.get(1);
        assertEquals("test-state-7",
                     eventStateForTrigger32.getName());

        List<TriggerEvent> triggerEvents1 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-1"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents1);
        assertThat(triggerEvents1.size(),
                   is(1));
        assertEquals("test-trigger-1",
                     triggerEvents1.get(0).getName());

        List<TriggerEvent> triggerEvents2 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-2"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents2);
        assertThat(triggerEvents2.size(),
                   is(2));
        assertEquals("test-trigger-1",
                     triggerEvents2.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents2.get(1).getName());

        List<TriggerEvent> triggerEvents3 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-3"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents3);
        assertThat(triggerEvents3.size(),
                   is(1));
        assertEquals("test-trigger-1",
                     triggerEvents3.get(0).getName());

        List<TriggerEvent> triggerEvents4 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-4"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents4);
        assertThat(triggerEvents4.size(),
                   is(2));
        assertEquals("test-trigger-1",
                     triggerEvents4.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents4.get(1).getName());

        List<TriggerEvent> triggerEvents5 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-5"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents5);
        assertThat(triggerEvents5.size(),
                   is(1));
        assertEquals("test-trigger-3",
                     triggerEvents5.get(0).getName());

        List<TriggerEvent> triggerEvents6 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-6"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents6);
        assertThat(triggerEvents6.size(),
                   is(0));

        List<TriggerEvent> triggerEvents7 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-7"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents7);
        assertThat(triggerEvents7.size(),
                   is(3));
        assertEquals("test-trigger-1",
                     triggerEvents7.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents7.get(1).getName());
        assertEquals("test-trigger-3",
                     triggerEvents7.get(2).getName());

        List<TriggerEvent> triggerEventsAll = WorkflowUtils.getAllTriggerEventsAssociatedWithEventStates(workflowManager);
        assertNotNull(triggerEventsAll);
        assertEquals(3,
                     triggerEventsAll.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"expressions/eventstatestriggers-spel.json", "expressions/eventstatestriggers-spel.yml"})
    public void testEventStateSpelExpressions(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));
        workflowManager.setDefaultExpressionEvaluator("spel");

        assertTrue(workflowManager.getWorkflowValidator().isValid());

        assertTrue(WorkflowUtils.haveTriggers(workflowManager));
        assertTrue(WorkflowUtils.haveStates(workflowManager));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertThat(workflow.getTriggerDefs().size(),
                   is(3));

        assertThat(workflow.getStates().size(),
                   is(7));

        List<EventState> eventStatesForTrigger1 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-1"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger1);
        assertEquals(5,
                     eventStatesForTrigger1.size());
        EventState eventStateForTrigger11 = eventStatesForTrigger1.get(0);
        assertEquals("test-state-1",
                     eventStateForTrigger11.getName());
        EventState eventStateForTrigger12 = eventStatesForTrigger1.get(1);
        assertEquals("test-state-2",
                     eventStateForTrigger12.getName());
        EventState eventStateForTrigger13 = eventStatesForTrigger1.get(2);
        assertEquals("test-state-3",
                     eventStateForTrigger13.getName());
        EventState eventStateForTrigger14 = eventStatesForTrigger1.get(3);
        assertEquals("test-state-4",
                     eventStateForTrigger14.getName());
        EventState eventStateForTrigger15 = eventStatesForTrigger1.get(4);
        assertEquals("test-state-7",
                     eventStateForTrigger15.getName());

        List<EventState> eventStatesForTrigger2 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-2"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger2);
        assertEquals(3,
                     eventStatesForTrigger2.size());
        EventState eventStateForTrigger21 = eventStatesForTrigger2.get(0);
        assertEquals("test-state-2",
                     eventStateForTrigger21.getName());
        EventState eventStateForTrigger22 = eventStatesForTrigger2.get(1);
        assertEquals("test-state-4",
                     eventStateForTrigger22.getName());
        EventState eventStateForTrigger23 = eventStatesForTrigger2.get(2);
        assertEquals("test-state-7",
                     eventStateForTrigger23.getName());

        List<EventState> eventStatesForTrigger3 = WorkflowUtils.getEventStatesForTriggerEvent(WorkflowUtils.getUniqueTriggerEvents(workflowManager).get("test-trigger-3"),
                                                                                              workflowManager);
        assertNotNull(eventStatesForTrigger3);
        assertEquals(2,
                     eventStatesForTrigger3.size());
        EventState eventStateForTrigger31 = eventStatesForTrigger3.get(0);
        assertEquals("test-state-5",
                     eventStateForTrigger31.getName());
        EventState eventStateForTrigger32 = eventStatesForTrigger3.get(1);
        assertEquals("test-state-7",
                     eventStateForTrigger32.getName());

        List<TriggerEvent> triggerEvents1 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-1"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents1);
        assertThat(triggerEvents1.size(),
                   is(1));
        assertEquals("test-trigger-1",
                     triggerEvents1.get(0).getName());

        List<TriggerEvent> triggerEvents2 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-2"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents2);
        assertThat(triggerEvents2.size(),
                   is(2));
        assertEquals("test-trigger-1",
                     triggerEvents2.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents2.get(1).getName());

        List<TriggerEvent> triggerEvents3 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-3"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents3);
        assertThat(triggerEvents3.size(),
                   is(1));
        assertEquals("test-trigger-1",
                     triggerEvents3.get(0).getName());

        List<TriggerEvent> triggerEvents4 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-4"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents4);
        assertThat(triggerEvents4.size(),
                   is(2));
        assertEquals("test-trigger-1",
                     triggerEvents4.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents4.get(1).getName());

        List<TriggerEvent> triggerEvents5 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-5"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents5);
        assertThat(triggerEvents5.size(),
                   is(1));
        assertEquals("test-trigger-3",
                     triggerEvents5.get(0).getName());

        List<TriggerEvent> triggerEvents6 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-6"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents6);
        assertThat(triggerEvents6.size(),
                   is(0));

        List<TriggerEvent> triggerEvents7 = WorkflowUtils.getTriggerEventsForEventState((EventState) WorkflowUtils.getUniqueStates(workflowManager).get("test-state-7"),
                                                                                        workflowManager);
        assertNotNull(triggerEvents7);
        assertThat(triggerEvents7.size(),
                   is(3));
        assertEquals("test-trigger-1",
                     triggerEvents7.get(0).getName());
        assertEquals("test-trigger-2",
                     triggerEvents7.get(1).getName());
        assertEquals("test-trigger-3",
                     triggerEvents7.get(2).getName());

        List<TriggerEvent> triggerEventsAll = WorkflowUtils.getAllTriggerEventsAssociatedWithEventStates(workflowManager);
        assertNotNull(triggerEventsAll);
        assertEquals(3,
                     triggerEventsAll.size());
    }
}