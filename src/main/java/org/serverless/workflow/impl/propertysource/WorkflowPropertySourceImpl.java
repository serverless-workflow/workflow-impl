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
package org.serverless.workflow.impl.propertysource;

import java.io.InputStream;
import java.util.Properties;

import org.serverless.workflow.api.WorkflowPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowPropertySourceImpl implements WorkflowPropertySource {

    private Properties propertySource = new Properties();
    private final String propertySourceName = "application.properties";

    private static Logger logger = LoggerFactory.getLogger(WorkflowPropertySourceImpl.class);

    public WorkflowPropertySourceImpl() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(propertySourceName);

            if (is != null) {
                propertySource.load(is);
            } else {
                logger.warn("Unable to find application.properties. No property source available.");
            }
        } catch (Exception e) {
            logger.error("Error loading application.properties: " + e.getMessage());
        }
    }

    @Override
    public Properties getPropertySource() {
        return propertySource;
    }

    @Override
    public void setPropertySource(Properties propertySource) {
        this.propertySource = propertySource;
    }
}
