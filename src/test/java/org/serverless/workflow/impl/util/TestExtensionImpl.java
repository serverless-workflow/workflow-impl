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

package org.serverless.workflow.impl.util;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.serverless.workflow.api.interfaces.Extension;

public class TestExtensionImpl implements Extension {

    public TestExtensionImpl() {

    }

    public TestExtensionImpl(String extensionId,
                             String testparam1,
                             String testparam2,
                             Map<String, String> testparam3) {
        this.extensionId = extensionId;
        this.testparam1 = testparam1;
        this.testparam2 = testparam2;
        this.testparam3 = testparam3;
    }

    @JsonProperty("extensionid")
    private String extensionId;

    @JsonProperty("testparam1")
    private String testparam1;

    @JsonProperty("testparam2")
    private String testparam2;

    @JsonProperty("testparam3")
    private Map<String, String> testparam3;

    @Override
    public String getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    public String getTestparam1() {
        return testparam1;
    }

    public void setTestparam1(String testparam1) {
        this.testparam1 = testparam1;
    }

    public String getTestparam2() {
        return testparam2;
    }

    public void setTestparam2(String testparam2) {
        this.testparam2 = testparam2;
    }

    public Map<String, String> getTestparam3() {
        return testparam3;
    }

    public void setTestparam3(Map<String, String> testparam3) {
        this.testparam3 = testparam3;
    }
}
