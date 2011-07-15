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

import org.springframework.stereotype.Component;

/**
 * Creates a listen port detector based on the server info string.
 */
@Component
public class ListenPortDetectorFactory {

    public ListenPortDetector getListenPortDetector(String serverInfo) {
        final String className = getClassName(serverInfo);
        return createInstance(className);
    }

    private String getClassName(String serverInfo) {
        final String upperInfo = serverInfo.toUpperCase();
        if (upperInfo.contains("WEBLOGIC")) {
            return "com.edmunds.etm.client.impl.WeblogicListenPortDetector";
        } else if (upperInfo.contains("TOMCAT")) {
            return "com.edmunds.etm.client.impl.TomcatListenPortDetector";
        } else {
            return "com.edmunds.etm.client.impl.DefaultListenPortDetector";
        }
    }

    private ListenPortDetector createInstance(String className) {
        try {
            final Class cls = Class.forName(className);
            return (ListenPortDetector) cls.newInstance();
        } catch (ClassNotFoundException e) {
            // Won't happen.
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            // Won't happen.
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // Won't happen.
            throw new RuntimeException(e);
        }
    }
}
