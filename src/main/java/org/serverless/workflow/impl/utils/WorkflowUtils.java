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

package org.serverless.workflow.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.TriggerEvent;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.EventState;

public class WorkflowUtils {

    public static boolean haveTriggers(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getTriggerDefs() != null && !workflowManager.getWorkflow().getTriggerDefs().isEmpty();
    }

    public static Map<String, TriggerEvent> getUniqueTriggerEvents(WorkflowManager workflowManager) {
        if (workflowManager.getWorkflow().getTriggerDefs() != null) {
            return workflowManager.getWorkflow().getTriggerDefs().stream()
                    .collect(Collectors.toMap(TriggerEvent::getName,
                                              tiggerEvent -> tiggerEvent));
        }

        return null;
    }

    public static boolean haveStates(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates() != null && !workflowManager.getWorkflow().getStates().isEmpty();
    }

    public static Map<String, State> getUniqueStates(WorkflowManager workflowManager) {
        if (workflowManager.getWorkflow().getTriggerDefs() != null) {
            return workflowManager.getWorkflow().getStates().stream()
                    .collect(Collectors.toMap(State::getName,
                                              state -> state));
        }

        return null;
    }

    public static List<EventState> getEventStatesForTriggerEvent(TriggerEvent triggerEvent,
                                                                 WorkflowManager workflowManager) {
        List<EventState> triggerStates = new ArrayList<>();

        for (State state : workflowManager.getWorkflow().getStates()) {
            if (state instanceof EventState) {
                EventState eventState = (EventState) state;
                List<Event> triggeredEvents = eventState.getEvents().stream()
                        .filter(event -> workflowManager.getExpressionEvaluator()
                                .evaluate(event.getEventExpression(),
                                          triggerEvent)).collect(Collectors.toList());
                if (triggeredEvents != null && !triggeredEvents.isEmpty()) {
                    triggerStates.add(eventState);
                }
            }
        }

        return triggerStates;
    }

    public static List<TriggerEvent> getTriggerEventsForEventState(EventState eventState,
                                                                   WorkflowManager workflowManager) {
        List<TriggerEvent> eventStateTriggers = new ArrayList<>();

        for (TriggerEvent triggerEvent : workflowManager.getWorkflow().getTriggerDefs()) {
            List<Event> triggeredEvents = eventState.getEvents().stream()
                    .filter(event -> workflowManager.getExpressionEvaluator()
                            .evaluate(event.getEventExpression(),
                                      triggerEvent)).collect(Collectors.toList());

            if (triggeredEvents != null && !triggeredEvents.isEmpty()) {
                eventStateTriggers.add(triggerEvent);
            }
        }

        return eventStateTriggers;
    }

    public static List<TriggerEvent> getAllTriggerEventsAssociatedWithEventStates(WorkflowManager workflowManager) {
        Map<String, TriggerEvent> associatedTriggersMap = new HashMap();
        for (State state : workflowManager.getWorkflow().getStates()) {
            if (state instanceof EventState) {
                EventState eventState = (EventState) state;
                for (TriggerEvent triggerEvent : workflowManager.getWorkflow().getTriggerDefs()) {
                    List<Event> triggeredEvents = eventState.getEvents().stream()
                            .filter(event -> workflowManager.getExpressionEvaluator()
                                    .evaluate(event.getEventExpression(),
                                              triggerEvent)).collect(Collectors.toList());
                    if (triggeredEvents != null && !triggeredEvents.isEmpty()) {
                        associatedTriggersMap.put(triggerEvent.getName(),
                                                  triggerEvent);
                    }
                }
            }
        }

        return new ArrayList<>(associatedTriggersMap.values());
    }

    public static List<Action> getAllActionsForEventState(EventState eventState) {
        List<Action> actions = new ArrayList<>();
        eventState.getEvents().forEach(event -> actions.addAll(event.getActions()));
        return actions;
    }

    public static List<Action> getAllActionsForEventStates(List<EventState> eventStates) {
        List<Action> actions = new ArrayList<>();
        eventStates.forEach(eventState -> actions.addAll(getAllActionsForEventState(eventState)));
        return actions;
    }

    public static List<Function> getAllFunctionsForActions(List<Action> actions) {
        List<Function> functions = new ArrayList<>();
        actions.forEach(action -> functions.add(action.getFunction()));
        return functions;
    }

    public static List<Function> getAllFunctionsForEventStates(List<EventState> eventStates) {
        List<Action> actions = getAllActionsForEventStates(eventStates);
        return getAllFunctionsForActions(actions);
    }

    public static State getStartState(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates().stream().filter(s -> s.getName().equals(workflowManager.getWorkflow().getStartsAt()))
                .findFirst().orElse(null);
    }

    public static State getStateByNAme(String stateName,
                                       WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates().stream().filter(state -> state.getName().equals(stateName))
                .findFirst().orElse(null);
    }

    public static boolean haveEndState(WorkflowManager workflowManager) {
        return workflowManager.getWorkflow().getStates().stream()
                .anyMatch(state -> state.isEnd());
    }
}
