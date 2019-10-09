package org.lorislab.quarkus.jel.log.interceptor;

import io.smallrye.config.PropertiesConfigSource;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LogConfigSource extends PropertiesConfigSource {

    private static final String PROP_NAME = "jel-quarkus.properties";

    public LogConfigSource() {
        super(load(), PROP_NAME, 300);
    }

    private static final Map<String, String> load() {

        Map<String, String> result = new HashMap<String, String>();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = LogConfigSource.class.getClassLoader();
        }

        try {
            Enumeration<URL> urls = cl.getResources(PROP_NAME);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStreamReader isr = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                    try (BufferedReader br = new BufferedReader(isr)) {
                        final Properties properties = new Properties();
                        properties.load(br);
                        result.putAll((Map<String, String>) (Map) properties);
                    }
                }
            }
        } catch (Exception ex) {
            throw new IOError(ex);
        }

        return result;
    }
}
