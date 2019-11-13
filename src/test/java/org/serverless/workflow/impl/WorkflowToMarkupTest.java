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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.actions.Retry;
import org.serverless.workflow.api.branches.Branch;
import org.serverless.workflow.api.choices.AndChoice;
import org.serverless.workflow.api.choices.DefaultChoice;
import org.serverless.workflow.api.events.Event;
import org.serverless.workflow.api.events.TriggerEvent;
import org.serverless.workflow.api.filters.Filter;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.Choice;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.DelayState;
import org.serverless.workflow.api.states.EventState;
import org.serverless.workflow.api.states.OperationState;
import org.serverless.workflow.api.states.ParallelState;
import org.serverless.workflow.api.states.SubflowState;
import org.serverless.workflow.api.states.SwitchState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.serverless.workflow.impl.util.IsEqualJSON.equalToJSONInFile;

public class WorkflowToMarkupTest extends BaseWorkflowTest {

    @Test
    public void testEmptyWorkflow() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("");

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/emptyworkflow.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/emptyworkflow.yml")));
    }

    @Test
    public void testSimpleWorkflowWithMetadata() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("")
                .withMetadata(
                        Stream.of(new Object[][]{
                                {"key1", "value1"},
                                {"key2", "value2"},
                        }).collect(Collectors.toMap(data -> (String) data[0],
                                                    data -> (String) data[1]))
                );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/workflowwithmetadata.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/workflowwithmetadata.yml")));
    }

    @Test
    public void testTrigger() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("").withTriggerDefs(
                Arrays.asList(
                        new TriggerEvent().withName("test-trigger").withType("testeventtype")
                                .withCorrelationToken("testcorrelationtoken").withSource("testsource")
                )
        );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singletriggerevent.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singletriggerevent.yml")));
    }

    @Test
    public void testEventState() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(new EventState().withName("test-state").withEnd(true)
                        .withEvents(Arrays.asList(
                                new Event().withEventExpression("testEventExpression").withTimeout("testTimeout")
                                        .withActionMode(Event.ActionMode.SEQUENTIAL)
                                        .withNextState("testNextState")
                                        .withActions(Arrays.asList(
                                                new Action().withFunction(new Function().withName("testFunction").withType("someType"))
                                                        .withTimeout("PT5S")
                                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                                           .withRetryInterval("PT5S")
                                                                           .withNextState("testNextRetryState"))
                                        ))
                        ))
            );
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singleeventstate.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singleeventstate.yml")));
    }

    @Test
    public void testDelayState() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(new DelayState().withName("test-state").withEnd(false).withNextState("testNextState").withTimeDelay("PT5S"));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singledelaystate.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singledelaystate.yml")));
    }

    @Test
    public void testSubflowState() {

        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(new SubflowState().withName("test-wf").withEnd(true).withNextState("testNextState").withWorkflowId("abc")
                        .withWorkflowVersion("123").withWaitForCompletion(true));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singlesubflowstate.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singlesubflowstate.yml")));
    }

    @Test
    public void testOperationState() {
        Map<String, String> params = new HashMap<String, String>() {{
            put("one",
                "1");
            put("two",
                "2");
        }};
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(new OperationState().withName("test-state").withEnd(true).withActionMode(OperationState.ActionMode.SEQUENTIAL).withNextState("testnextstate")
                        .withFilter(new Filter()
                                            .withInputPath("$.owner.address.zipcode")
                                            .withResultPath("$.country.code")
                                            .withOutputPath("$.owner.address.countryCode"))
                        .withActions(Arrays.asList(
                                new Action().withFunction(new Function().withName("testFunction")
                                                                  .withType("someType")
                                                                  .withParameters(params))
                                        .withTimeout("PT5S")
                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                           .withRetryInterval("PT5S")
                                                           .withNextState("testNextRetryState"))
                        )));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singleoperationstate.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singleoperationstate.yml")));
    }

    @Test
    public void testParallellState() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(new ParallelState().withName("test-state").withEnd(true).withNextState("testnextstate")
                        .withBranches(Arrays.asList(
                                new Branch().withName("firsttestbranch").withStartsAt("operationstate").withStates(
                                        new ArrayList<State>() {{
                                            add(new OperationState().withName("operationstate").withEnd(true).withActionMode(OperationState.ActionMode.SEQUENTIAL).withNextState("testnextstate")
                                                        .withActions(Arrays.asList(
                                                                new Action().withFunction(new Function().withName("testFunction").withType("someType"))
                                                                        .withTimeout("PT5S")
                                                                        .withRetry(new Retry().withMatch("testMatch").withMaxRetry(10)
                                                                                           .withRetryInterval("PT5S")
                                                                                           .withNextState("testNextRetryState"))
                                                        )));
                                        }}
                                ),
                                new Branch().withName("secondtestbranch").withStartsAt("delaystate").withStates(
                                        new ArrayList<State>() {{
                                            add(new DelayState().withName("delaystate").withEnd(false).withNextState("testNextState").withTimeDelay("PT5S"));
                                        }}
                                ).withWaitForCompletion(true)
                        )));
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singleparallelstate.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singleparallelstate.yml")));
    }

    @Test
    public void testSwitchState() {
        Workflow workflow = new Workflow().withName("test-wf").withId("abc").withStartsAt("test-state").withStates(new ArrayList<State>() {{
            add(
                    new SwitchState().withName("test-state").withDefault("defaultteststate").withEnd(false).withChoices(
                            new ArrayList<Choice>() {{
                                add(
                                        new AndChoice().withNextState("testnextstate").withAnd(
                                                Arrays.asList(
                                                        new DefaultChoice()
                                                                .withOperator(DefaultChoice.Operator.EQUALS)
                                                                .withPath("testpath")
                                                                .withValue("testvalue")
                                                )
                                        )
                                );
                            }}
                    )
            );
        }});

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.setWorkflow(workflow);

        assertNotNull(workflowManager.toJson());

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("basic/singleswitchstateandchoice.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("basic/singleswitchstateandchoice.yml")));
    }
}