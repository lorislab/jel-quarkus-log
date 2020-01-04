package org.lorislab.quarkus.jel.log.interceptor;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class LoggerDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        context.register(RestLogInterceptor.class);
        context.register(RestClientLogInterceptor.class);
    }
}
