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

import com.edmunds.etm.common.thrift.ClientConfigDto;
import com.edmunds.etm.common.thrift.MavenModuleDto;
import com.edmunds.etm.common.xml.XmlMarshaller;
import com.edmunds.etm.common.xml.XmlValidationException;
import com.edmunds.etm.common.xml.XmlValidator;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class of etm client. <p/> The etm client is instanced as a servlet context listener. It will connect to the
 * ZooKeeper server regardless of when the servlets are loaded. <p/> The disconnect will happen  when the context is
 * shutdown or if the process is terminates unexpectedly (due to zookeeper heartbeats). <p/> Copyright (C) 2010
 * Edmunds.com <p/> <p/> Date: Apr 21, 2010
 *
 * @author Aliaksandr Savin
 * @author David Trott
 * @author Ryan Holmes
 */
public class ClientServletContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(ClientServletContextListener.class);

    private final static String CLIENT_CONTEXT_PATH = "/etm-client-context.xml";

    /**
     * 'etm-config.xml' file name.
     */
    public static final String ETM_CONFIG_PATH = "/etm-config.xml";

    private ClientManager clientManager;

    /**
     * Notification that the web application initialization process is starting. All ServletContextListeners are
     * notified of context initialization before any filter or servlet in the web application is initialized.
     *
     * @param servletContextEvent the servlet context event.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        logger.debug("Starting ETM listener.");
        ServletContext ctx = servletContextEvent.getServletContext();

        final String ipAddress = getIpAddress();
        final String contextPath = ctx.getContextPath();

        ClientConfigDto clientConfig = readClientConfiguration();
        MavenModuleDto mavenModule = clientConfig.getMavenModule();

        if(logger.isDebugEnabled()) {
            logger.debug(String.format("ETM application: %s", clientConfig.getMavenModule()));
        }

        // validate IP address and maven module
        if(StringUtils.isBlank(ipAddress)) {
            logger.error("Unable to register with ETM: IP address is blank");
            return;
        } else if(mavenModule == null) {
            logger.error("Unable to register with ETM: Maven artifact information is missing");
            return;
        }

        // create Spring application context
        logger.debug("Initializing ETM Spring context");
        final ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext();
        appCtx.addBeanFactoryPostProcessor(getConfigurer(mavenModule));
        appCtx.setConfigLocation(CLIENT_CONTEXT_PATH);
        appCtx.refresh();

        // register with the ETM controller
        clientManager = (ClientManager) appCtx.getBean("clientManager");
        clientManager.register(ctx.getServerInfo(), ipAddress, contextPath, clientConfig);
    }

    /**
     * Notification that the servlet context is about to be shut down. All servlets and filters have been destroy()ed
     * before any ServletContextListeners are notified of context destruction.
     *
     * @param servletContextEvent the servlet context event.
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if(clientManager != null) {
            clientManager.shutdown();
        }
    }

    private String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private BeanFactoryPostProcessor getConfigurer(final MavenModuleDto mavenModule) {
        final Properties properties = new Properties();
        properties.setProperty("maven.groupId", mavenModule.getGroupId());
        properties.setProperty("maven.artifactId", mavenModule.getArtifactId());
        properties.setProperty("maven.version", mavenModule.getVersion());

        final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(properties);
        return configurer;
    }

    private ClientConfigDto readClientConfiguration() {
        byte[] xmlData;
        try {
            xmlData = IOUtils.toByteArray(getClass().getResourceAsStream(ETM_CONFIG_PATH));
        } catch(IOException e) {
            String message = String.format("Could not read ETM configuration file at %s", ETM_CONFIG_PATH);
            logger.error(message);
            throw new RuntimeException(message, e);
        }

        return parseXmlConfiguration(xmlData);
    }

    private ClientConfigDto parseXmlConfiguration(byte[] xmlData) {
        XmlValidator xmlValidator = new XmlValidator();

        // Validate XML
        try {
            xmlValidator.validate(xmlData, XmlValidator.CLIENT_CONFIG_XSD);
        } catch(XmlValidationException e) {
            throw new RuntimeException("ETM XML configuration file is invalid", e);
        }

        return XmlMarshaller.unmarshal(xmlData, ClientConfigDto.class);
    }
}
