package org.lorislab.quarkus.jel.log.interceptor;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

/**
 * The rest client log interceptor
 *
 * @author Andrej Petras
 */
@LoggerService(log = false)
public class RestClientLogInterceptor implements ClientRequestFilter, ClientResponseFilter {

    /**
     * The logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(RestClientLogInterceptor.class);

    /**
     * The context interceptor property.
     */
    private static final String CONTEXT = "context";

    /**
     * The message start.
     */
    private MessageFormat messageStart;

    /**
     * The message succeed.
     */
    private MessageFormat messageSucceed;

    public RestClientLogInterceptor() {
        Config config = ConfigProvider.getConfig();
        messageStart = new MessageFormat(config.getOptionalValue("org.lorislab.jel.logger.rs.client.start", String.class).orElse("{0} {1} [{2}] started."));
        messageSucceed = new MessageFormat(config.getOptionalValue("org.lorislab.jel.logger.rs.client.succeed", String.class).orElse("{0} {1} finished in [{2}s] with [{3}-{4},{5}]."));
    }

    /**
     * The rest client logger interceptor disable flag.
     */
    @Inject
    @ConfigProperty(name = "org.lorislab.jel.logger.rs.client.disable", defaultValue = "false")
    private boolean disable;

    /**
     * {@inheritDoc }
     */
    @Override
    public void filter(ClientRequestContext requestContext) {
        if (disable) {
            return;
        }
        InterceptorContext context = new InterceptorContext(requestContext.getMethod(), requestContext.getUri().toString());
        requestContext.setProperty(CONTEXT, context);
        log.info("{}", LoggerConfiguration.msg(messageStart, new Object[]{requestContext.getMethod(), requestContext.getUri(), requestContext.hasEntity()}));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        if (disable) {
            return;
        }
        InterceptorContext context = (InterceptorContext) requestContext.getProperty(CONTEXT);
        if (context != null) {
            Response.StatusType status = responseContext.getStatusInfo();
            context.closeContext(status.getReasonPhrase());
            log.info("{}", LoggerConfiguration.msg(messageSucceed, new Object[]{context.method, requestContext.getUri(), context.time, status.getStatusCode(), context.result, responseContext.hasEntity()}));
        }
    }
}
