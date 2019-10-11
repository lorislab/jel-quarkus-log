package org.lorislab.quarkus.jel.log.interceptor;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.text.MessageFormat;

/**
 * The rest log interceptor.
 */
@Provider
@LoggerService(log = false)
public class RestLogInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * The annotation interceptor property.
     */
    private static final String ANO = "ano";

    /**
     * The context interceptor property.
     */
    private static final String CONTEXT = "context";

    /**
     * The message start.
     */
    private static MessageFormat messageStart;

    /**
     * The message succeed.
     */
    private static MessageFormat messageSucceed;

    static {
        Config config = ConfigProvider.getConfig();
        messageStart = new MessageFormat(config.getOptionalValue("org.lorislab.jel.logger.rs.start", String.class).orElse("{0} {1} [{2}] started."));
        messageSucceed = new MessageFormat(config.getOptionalValue("org.lorislab.jel.logger.rs.succeed", String.class).orElse("{0} {1} [{2}s] finished [{3}-{4},{5}]."));
    }

    /**
     * The resource info.
     */
    @Context
    private ResourceInfo resourceInfo;

    /**
     * The rest logger interceptor disable flag.
     */
    @Inject
    @ConfigProperty(name = "org.lorislab.jel.logger.rs.disable", defaultValue = "false")
    private boolean disable;

    /**
     * {@inheritDoc }
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (disable) {
            return;
        }
        LoggerService ano = LoggerServiceInterceptor.getLoggerServiceAno(resourceInfo.getResourceClass(), resourceInfo.getResourceClass().getName(), resourceInfo.getResourceMethod());
        requestContext.setProperty(ANO, ano);

        if (ano.log()) {
            InterceptorContext context = new InterceptorContext(requestContext.getMethod(), requestContext.getUriInfo().getRequestUri().toString());
            requestContext.setProperty(CONTEXT, context);

            // create the logger
            Logger logger = LoggerFactory.getLogger(resourceInfo.getResourceClass());
            logger.info("{}", LoggerConfiguration.msg(messageStart, new Object[]{context.method, requestContext.getUriInfo().getRequestUri(), requestContext.hasEntity()}));
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (disable) {
            return;
        }
        LoggerService ano = (LoggerService) requestContext.getProperty(ANO);
        if (ano != null && ano.log()) {
            InterceptorContext context = (InterceptorContext) requestContext.getProperty(CONTEXT);
            Response.StatusType status = responseContext.getStatusInfo();
            context.closeContext(status.getReasonPhrase());
            Logger logger = LoggerFactory.getLogger(resourceInfo.getResourceClass());
            logger.info("{}", LoggerConfiguration.msg(messageSucceed,
                    new Object[]{
                            context.method,
                            context.parameters,
                            context.time,
                            status.getStatusCode(),
                            status.getReasonPhrase(),
                            responseContext.hasEntity()
                    }));
        }
    }

}
