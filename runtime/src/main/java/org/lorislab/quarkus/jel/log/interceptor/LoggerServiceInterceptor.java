/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.quarkus.jel.log.interceptor;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * The logger service interceptor.
 *
 * @author Andrej Petras
 */
@LoggerService
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class LoggerServiceInterceptor {

    /**
     * The logger builder service.
     */
    @Inject
    LoggerBuilderService loggerBuilderService;

    /**
     * The method execution.
     *
     * @param ic the invocation context.
     * @return the method result object.
     * @throws Exception if the method fails.
     */
    @AroundInvoke
    public Object methodExecution(final InvocationContext ic) throws Exception {
        Object result;
        Method method = ic.getMethod();
        String className = getObjectClassName(ic.getTarget());

        LoggerService ano = getLoggerServiceAno(ic.getTarget().getClass(), className, method);
        if (ano.log()) {

            Logger logger = LoggerFactory.getLogger(className);
            String parameters = getValuesString(ic.getParameters(), method.getParameters());

            InterceptorContext context = new InterceptorContext(method.getName(), parameters);
            logger.info("{}", LoggerConfiguration.msgStart(context));

            try {
                result = ic.proceed();

                if (result instanceof CompletionStage) {
                    logger.info("{}", LoggerConfiguration.msgFutureStart(context));

                    CompletionStage<?> cs = (CompletionStage<?>) result;
                    cs.toCompletableFuture().whenComplete((u, eex) -> {
                        if (eex != null) {
                            handleException(context, logger, ano, (Throwable) eex);
                        } else {
                            String contextResult = LoggerConfiguration.RESULT_VOID;
                            if (u != Void.TYPE) {
                                contextResult = getValue(u);
                            }
                            context.closeContext(contextResult);
                            // log the success message
                            logger.info("{}", LoggerConfiguration.msgSucceed(context));
                        }
                    });
                } else {
                    String contextResult = LoggerConfiguration.RESULT_VOID;
                    if (method.getReturnType() != Void.TYPE) {
                        contextResult = getValue(result);
                    }
                    context.closeContext(contextResult);
                    // log the success message
                    logger.info("{}", LoggerConfiguration.msgSucceed(context));
                }
            } catch (InvocationTargetException ie) {
                handleException(context, logger, ano, ie.getCause());
                throw ie;
            } catch (Exception ex) {
                handleException(context, logger, ano, ex);
                throw ex;
            }
        } else {
            result = ic.proceed();
        }
        return result;
    }

    /**
     * Handles the exception.
     *
     * @param context the interceptor context.
     * @param logger  the logger.
     * @param ano     the annotation.
     * @param ex      the exception.
     */
    private void handleException(InterceptorContext context, Logger logger, LoggerService ano, Throwable ex) {
        context.closeContext(getValue(ex));
        logger.error("{}", LoggerConfiguration.msgFailed(context));
        boolean stacktrace = ano.stacktrace();
        if (stacktrace) {
            logger.error("Error ", ex);
        }
    }

    /**
     * Gets the service class name.
     *
     * @param object the target class.
     * @return the corresponding class name.
     */
    private static String getObjectClassName(Object object) {
        if (object instanceof Proxy) {
            Class<?>[] clazz = object.getClass().getInterfaces();
            if (clazz.length > 0) {
                return getClassName(clazz[0]);
            }
        }
        return getClassName(object.getClass());
    }

    /**
     * Gets the service class name.
     *
     * @param clazz the target class.
     * @return the corresponding class name.
     */
    private static String getClassName(Class<?> clazz) {
        if (clazz != null && clazz.getSuperclass() != null) {
            return clazz.getSuperclass().getName();
        }
        if (clazz != null) {
            return clazz.getName();
        }
        return null;
    }

    /**
     * Gets the logger service annotation.
     *
     * @param clazz  the class.
     * @param method the method.
     * @return the logger service annotation.
     */
    public static LoggerService getLoggerServiceAno(Class<?> clazz, String className, Method method) {

        Config config = ConfigProvider.getConfig();
        String mc = className + "." + method.getName() + "/jel-log/";
        String c = className + "/jel-log/";

        Optional<Boolean> log = config.getOptionalValue(mc + "log", Boolean.class);
        Optional<Boolean> trace = config.getOptionalValue(mc + "trace", Boolean.class);
        LoggerService anno = method.getAnnotation(LoggerService.class);
        if (anno != null) {
            return createLoggerService(log.orElse(anno.log()), trace.orElse(anno.stacktrace()));
        }
        Optional<Boolean> clog = config.getOptionalValue(c + "log", Boolean.class);
        Optional<Boolean> ctrace = config.getOptionalValue(c + "trace", Boolean.class);
        LoggerService canno = clazz.getAnnotation(LoggerService.class);
        if (canno != null) {
            return createLoggerService(log.orElse(clog.orElse(canno.log())), trace.orElse(ctrace.orElse(canno.stacktrace())));
        }
        return createLoggerService(log.orElse(clog.orElse(true)), trace.orElse(ctrace.orElse(true)));
    }

    /**
     * Creates the logger service.
     *
     * @param log        the log flag.
     * @param stacktrace the stacktrace flag.
     * @return the corresponding logger service.
     */
    private static LoggerService createLoggerService(boolean log, boolean stacktrace) {
        return new LoggerService() {
            @Override
            public boolean log() {
                return log;
            }

            @Override
            public boolean stacktrace() {
                return stacktrace;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return LoggerService.class;
            }
        };
    }

    /**
     * Gets the list of string corresponding to the list of parameters.
     *
     * @param value      the list of parameters.
     * @param parameters the list of method parameters.
     * @return the list of string corresponding to the list of parameters.
     */
    private String getValuesString(Object[] value, Parameter[] parameters) {
        if (value != null && value.length > 0) {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            sb.append(getValue(value[index], parameters[index]));
            index++;
            for (; index < value.length; index++) {
                sb.append(',');
                sb.append(getValue(value[index], parameters[index]));
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Get the parameter log value.
     *
     * @param value     the parameter value.
     * @param parameter the method parameter.
     * @return the corresponding log value.
     */
    private String getValue(Object value, Parameter parameter) {
        LoggerExclude pa = parameter.getAnnotation(LoggerExclude.class);
        if (pa != null) {
            if (!pa.mask().isEmpty()) {
                return pa.mask();
            }
            return parameter.getName();
        }
        return getValue(value);
    }

    /**
     * Gets the string corresponding to the parameter.
     *
     * @param parameter the method parameter.
     * @return the string corresponding to the parameter.
     */
    private String getValue(Object parameter) {
        return loggerBuilderService.getParameterValue(parameter);
    }
}
