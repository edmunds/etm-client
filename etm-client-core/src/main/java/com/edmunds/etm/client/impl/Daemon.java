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

import com.edmunds.etm.client.util.SpringContextLoader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Stand alone Daemon process which can be run on a node to request a vip be created.
 */
@Component
public class Daemon {

    private static final String DAEMON_CONTEXT_PATH = "/etm-daemon-context.xml";
    private static final Logger logger = Logger.getLogger(Daemon.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Error provide the name of the service as the first argument");
            return;
        }
        final String serviceName = args[0];

        final Properties properties = new Properties();
        properties.put("serviceName", serviceName);

        final ApplicationContext appCtx =
                SpringContextLoader.loadClassPathSpringContext(DAEMON_CONTEXT_PATH, properties);
        final DaemonConfig daemonConfig = (DaemonConfig) appCtx.getBean("daemonConfig");
        daemonConfig.setServiceName(serviceName);
        final Daemon daemon = (Daemon) appCtx.getBean("daemon");

        if (daemon != null) {
            daemon.run();
        }
    }

    private final ClientManager clientManager;
    private final DaemonConfig daemonConfig;

    @Autowired
    public Daemon(ClientManager clientManager, DaemonConfig daemonConfig) {
        this.clientManager = clientManager;
        this.daemonConfig = daemonConfig;
    }

    public void run() {
        logger.debug("Starting ETM daemon.");

        clientManager.register(daemonConfig.buildClientConfig());

        waitForShutdown();
    }

    private void waitForShutdown() {
        try {
            synchronized (Daemon.class) {
                Daemon.class.wait();
            }
        } catch (InterruptedException e) {
            logger.info("ETM Daemon shutting down");
        } finally {
            clientManager.shutdown();
        }
    }
}
