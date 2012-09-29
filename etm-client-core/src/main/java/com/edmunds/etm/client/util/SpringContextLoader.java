package com.edmunds.etm.client.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

/**
 * Utility function for loading a spring context.
 */
public final class SpringContextLoader {

    private static final Logger logger = Logger.getLogger(SpringContextLoader.class);

    private SpringContextLoader() {
    }

    /**
     * Loads a spring context from the classpath.
     *
     * @param path       the file to load.
     * @param properties optional properties to set into the spring context.
     * @return the spring context.
     */
    public static ApplicationContext loadClassPathSpringContext(String path, Properties properties) {
        logger.debug("Initializing ETM Spring context");

        final ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext();

        if (properties != null) {
            final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
            configurer.setProperties(properties);
            appCtx.addBeanFactoryPostProcessor(configurer);
        }

        appCtx.setConfigLocation(path);
        appCtx.refresh();

        return appCtx;
    }
}
