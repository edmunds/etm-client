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

import org.apache.catalina.mbeans.MBeanUtils;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import java.util.Iterator;
import java.util.Set;

/**
 * Looks up tomcat MBeans in order to determine the listen port.
 */
public final class TomcatListenPortDetector implements ListenPortDetector {

    @Override
    public int getListenPort() {

        try {
            final MBeanServer beanServer = MBeanUtils.createServer();

            final ObjectName objectNameQuery = new ObjectName("Catalina:type=Connector,*");
            final QueryExp value = Query.match(Query.attr("protocol"), Query.value("HTTP/1.1"));
            final Set<ObjectInstance> objectInstances = beanServer.queryMBeans(objectNameQuery, value);

            final Iterator<ObjectInstance> it = objectInstances.iterator();

            if (!it.hasNext()) {
                throw new RuntimeException("Unable to detect Tomcat listen port.");
            }

            final ObjectInstance objectInstance = it.next();
            final ObjectName objectName = objectInstance.getObjectName();
            final Object attribute = beanServer.getAttribute(objectName, "port");
            if (!(attribute instanceof Integer)) {
                throw new RuntimeException("Unable to extract Tomcat listen port.");
            }

            return (Integer) attribute;
        } catch (JMException e) {
            throw new RuntimeException("Failed to lookup Tomcat listen port", e);
        }
    }
}
