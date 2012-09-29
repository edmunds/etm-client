/*
 * Copyright 2011 Edmunds.com, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edmunds.etm.client.impl;

/**
 * ETM client settings bean.
 *
 * @author Ryan Holmes
 */
public class ClientSettings {

    private boolean enabled;

    /**
     * Indicates whether the ETM client is enabled.
     *
     * @return true to enable ETM client, false to disable
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the ETM client is enabled.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
