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

import org.apache.log4j.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class contains helper methods to access the Weblogic MBean configuration values.
 * <p/>
 * Copyright (C) 2007 Edmunds.com
 * <p/>
 * Date: Apr 8, 2008:10:42:35 AM
 *
 * @author Eric Gramond
 */
public final class WeblogicListenPortDetector implements ListenPortDetector {

    /**
     * Name to use for looking up the local MBeanServer via an InitialContext.
     */
    private static final String LOCAL_MBEAN_SERVER_NAME = "java:comp/env/jmx/runtime";

    /**
     * ObjectName of the RuntimeServerMBean for the current server.
     */
    private static final String RUNTIME_MBEAN_NAME =
            "com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean";

    /**
     * Name of the ServerConfiguration attribute of the RuntimeServerMBean.
     */
    private static final String SERVER_CONFIGURATION = "ServerConfiguration";

    /**
     * Name of the ListenPort attribute of the ServerConfiguration MBean.
     */
    private static final String LISTEN_PORT = "ListenPort";

    /**
     * Log4J Logger for this class.
     */
    private static final Logger log = Logger.getLogger(WeblogicListenPortDetector.class);

    @Override
    public int getListenPort() {
        return getWeblogicPort();
    }

    /**
     * Gets the MBeanServer of the local instance directly via an initial context lookup.
     *
     * @return the local MBeanServer
     * @throws NamingException if the server cannot be found
     */
    public static MBeanServer getMBeanServer() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (MBeanServer) ctx.lookup(LOCAL_MBEAN_SERVER_NAME);
    }

    /**
     * Gets the listen port of the local weblogic instance.
     *
     * @return the listen port of the local weblogic instance, or -1 if an error occured.
     */
    public static int getWeblogicPort() {
        try {
            MBeanServer server = getMBeanServer();
            ObjectName config =
                    (ObjectName) server.getAttribute(new ObjectName(RUNTIME_MBEAN_NAME), SERVER_CONFIGURATION);
            return (Integer) server.getAttribute(config, LISTEN_PORT);
        } catch (NamingException exc) {
            log.warn("NamingException obtaining weblogic port: " + exc.getMessage(), exc);
        } catch (JMException exc) {
            log.warn("MBeanException obtaining weblogic port: " + exc.getMessage(), exc);
        }
        return -1;
    }
}
