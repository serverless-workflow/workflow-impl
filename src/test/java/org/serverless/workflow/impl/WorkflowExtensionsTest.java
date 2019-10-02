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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.impl.util.SecondTestExtensionImpl;
import org.serverless.workflow.impl.util.TestExtensionImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.serverless.workflow.impl.util.IsEqualJSON.equalToJSONInFile;

public class WorkflowExtensionsTest extends BaseWorkflowTest {

    @ParameterizedTest
    @ValueSource(strings = {"extensions/singleextension.json", "extensions/singleextension.yml"})
    public void testSingleExtensionFromMarkup(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.registerExtension("testextension",
                                          TestExtensionImpl.class);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertNotNull(workflow.getExtensions());
        assertEquals(1,
                     workflow.getExtensions().size());

        assertTrue(workflow.getExtensions().get(0) instanceof TestExtensionImpl);

        TestExtensionImpl testExtension = (TestExtensionImpl) workflow.getExtensions().get(0);

        assertEquals("testextension",
                     testExtension.getExtensionId());
        assertEquals("testvalue1",
                     testExtension.getTestparam1());
        assertEquals("testvalue2",
                     testExtension.getTestparam2());
        assertNotNull(testExtension.getTestparam3());
        assertEquals(2,
                     testExtension.getTestparam3().size());
        assertEquals("value1",
                     testExtension.getTestparam3().get("key1"));
        assertEquals("value2",
                     testExtension.getTestparam3().get("key2"));
    }

    @Test
    public void testSingleExtensionFromWorkflow() {
        Map<String, String> testParam3Map = new HashMap<>();
        testParam3Map.put("key1",
                          "value1");
        testParam3Map.put("key2",
                          "value2");

        Workflow workflow = new Workflow().withName("test-wf")
                .withExtensions(
                        Arrays.asList(
                                new TestExtensionImpl("testextension",
                                                      "testvalue1",
                                                      "testvalue2",
                                                      testParam3Map)
                        )
                );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.registerExtension("testextension",
                                          TestExtensionImpl.class);
        workflowManager.setWorkflow(workflow);

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("extensions/singleextension.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("extensions/singleextension.yml")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"extensions/twoextensions.json", "extensions/twoextensions.yml"})
    public void tesTwoExtensionsFromMarkup(String model) {
        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.registerExtension("testextension",
                                          TestExtensionImpl.class);
        workflowManager.registerExtension("secondtestextension",
                                          SecondTestExtensionImpl.class);
        workflowManager.setMarkup(getFileContents(getResourcePath(model)));

        Workflow workflow = workflowManager.getWorkflow();
        assertNotNull(workflow);

        assertNotNull(workflow.getExtensions());
        assertEquals(2,
                     workflow.getExtensions().size());

        assertTrue(workflow.getExtensions().get(0) instanceof TestExtensionImpl);

        TestExtensionImpl testExtension = (TestExtensionImpl) workflow.getExtensions().get(0);

        assertEquals("testextension",
                     testExtension.getExtensionId());
        assertEquals("testvalue1",
                     testExtension.getTestparam1());
        assertEquals("testvalue2",
                     testExtension.getTestparam2());
        assertNotNull(testExtension.getTestparam3());
        assertEquals(2,
                     testExtension.getTestparam3().size());
        assertEquals("value1",
                     testExtension.getTestparam3().get("key1"));
        assertEquals("value2",
                     testExtension.getTestparam3().get("key2"));

        assertTrue(workflow.getExtensions().get(1) instanceof SecondTestExtensionImpl);

        SecondTestExtensionImpl secondTestExtension = (SecondTestExtensionImpl) workflow.getExtensions().get(1);

        assertEquals("secondtestextension",
                     secondTestExtension.getExtensionId());
        assertEquals("secondtestvalue1",
                     secondTestExtension.getTestparam1());
        assertEquals("secondtestvalue2",
                     secondTestExtension.getTestparam2());
        assertNotNull(secondTestExtension.getTestparam3());
        assertEquals(2,
                     secondTestExtension.getTestparam3().size());
        assertEquals("secondvalue1",
                     secondTestExtension.getTestparam3().get("key1"));
        assertEquals("secondvalue2",
                     secondTestExtension.getTestparam3().get("key2"));
        assertEquals("secondtestvalue4",
                     secondTestExtension.getTestparam4());
    }

    @Test
    public void testTwoeExtensionsFromWorkflow() {
        Map<String, String> firstTestParam3Map = new HashMap<>();
        firstTestParam3Map.put("key1",
                               "value1");
        firstTestParam3Map.put("key2",
                               "value2");

        Map<String, String> secondTestParam3Map = new HashMap<>();
        secondTestParam3Map.put("key1",
                                "secondvalue1");
        secondTestParam3Map.put("key2",
                                "secondvalue2");

        Workflow workflow = new Workflow().withName("test-wf")
                .withExtensions(
                        Arrays.asList(
                                new TestExtensionImpl("testextension",
                                                      "testvalue1",
                                                      "testvalue2",
                                                      firstTestParam3Map),
                                new SecondTestExtensionImpl("secondtestextension",
                                                            "secondtestvalue1",
                                                            "secondtestvalue2",
                                                            secondTestParam3Map,
                                                            "secondtestvalue4")
                        )
                );

        WorkflowManager workflowManager = getWorkflowManager();
        assertNotNull(workflowManager);
        workflowManager.registerExtension("testextension",
                                          TestExtensionImpl.class);
        workflowManager.registerExtension("secondtestextension",
                                          SecondTestExtensionImpl.class);
        workflowManager.setWorkflow(workflow);

        assertThat(workflowManager.toJson(),
                   equalToJSONInFile(getResourcePathFor("extensions/twoextensions.json")));

        assertEquals(workflowManager.toYaml(),
                     getFileContents(getResourcePath("extensions/twoextensions.yml")));
    }
}
