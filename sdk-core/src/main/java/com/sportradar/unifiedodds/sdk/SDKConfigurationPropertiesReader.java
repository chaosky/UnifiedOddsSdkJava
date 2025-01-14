/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 03/01/2018.
 * // TODO @eti: Javadoc
 */
@SuppressWarnings({ "AbbreviationAsWordInName", "AvoidNoArgumentSuperConstructorCall", "ConstantName" })
public class SDKConfigurationPropertiesReader extends SDKConfigurationReader {

    private static final Logger logger = LoggerFactory.getLogger(SDKConfigurationPropertiesReader.class);
    private static final String SDK_PROPERTIES_FILENAME = "UFSdkConfiguration.properties";

    private final String filename;

    SDKConfigurationPropertiesReader() {
        super();
        filename = SDK_PROPERTIES_FILENAME;
    }

    SDKConfigurationPropertiesReader(String filename) {
        this.filename = filename;
    }

    @Override
    Map<String, String> readConfiguration() {
        Properties prop = new Properties();

        InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
        try {
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            logger.warn("SDK properties file loading failed, exc:", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // already closed,...
                }
            }
        }

        Map<String, String> result = Maps.newHashMapWithExpectedSize(prop.size());
        prop.stringPropertyNames().forEach(p -> result.put(p, prop.getProperty(p)));

        return result;
    }
}
