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

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrej Petras
 */
public final class Configuration {

    public static final String PATTERN_NO_USER;
    public static final String PATTERN_RESULT_VOID;

    private static final MessageFormat MESSAGE_SERVICE_EXCEPTION;

    private static final MessageFormat MESSAGE_START;
    private static final MessageFormat MESSAGE_SUCCEED;
    private static final MessageFormat MESSAGE_FUTURE_START;
    private static final MessageFormat MESSAGE_FAILED;
    private static final MessageFormat MESSAGE_EXCEPTION;

    private static final MessageFormat REST_MESSAGE_START;
    private static final MessageFormat REST_MESSAGE_SUCCEED;
    private static final MessageFormat REST_MESSAGE_CLIENT_START;
    private static final MessageFormat REST_MESSAGE_CLIENT_SUCCEED;
    private static final MessageFormat REST_MESSAGE_EXCEPTION;

    static {
        Config config = ConfigProvider.getConfig();
        PATTERN_NO_USER = stringProperty(config, "org.lorislab.jel.logger.nouser", "anonymous");
        PATTERN_RESULT_VOID = stringProperty(config, "org.lorislab.jel.logger.result.void", "void");
        MESSAGE_SERVICE_EXCEPTION = messageFormat(config, "org.lorislab.jel.logger.service.exception", "Exception:\nclass:{0}\nmsg:{1}\ninfo:{2}");
        MESSAGE_START = messageFormat(config, "org.lorislab.jel.logger.start", "{0}({1}) started.");
        MESSAGE_SUCCEED = messageFormat(config, "org.lorislab.jel.logger.succeed", "{0}({1}):{2} [{3}s] succeed.");
        MESSAGE_FUTURE_START = messageFormat(config, "org.lorislab.jel.logger.futureStart", "{0}({1}) future started.");

        MESSAGE_FAILED = messageFormat(config, "org.lorislab.jel.logger.failed", "{0}({1}):{2} [{3}s] failed.");
        MESSAGE_EXCEPTION = messageFormat(config, "org.lorislab.jel.logger.exception", "Exception in {0}:{1} error");

        REST_MESSAGE_START = messageFormat(config, "org.lorislab.jel.logger.rs.start", "{0} {1} entity {2} started.");
        REST_MESSAGE_SUCCEED = messageFormat(config, "org.lorislab.jel.logger.rs.succeed", "{0} {1} [{2}s] finished [{3}-{4},{5}].");
        REST_MESSAGE_CLIENT_START = messageFormat(config, "org.lorislab.jel.logger.rs.client.start", "[outgoing] {0} {1} entity {2} started.");
        REST_MESSAGE_CLIENT_SUCCEED = messageFormat(config, "org.lorislab.jel.logger.rs.client.start", "[incoming] {0} {1} finished in [{2}s] with [{3}-{4},{5}].");
        REST_MESSAGE_EXCEPTION = messageFormat(config, "org.lorislab.jel.logger.rs.exception", "{2} {3} threw exception [{4}].");
    }

    private Configuration() {
    }

    private static String stringProperty(Config config, String name, String defaultValue) {
        return config.getOptionalValue(name, String.class).orElse(defaultValue);
    }

    private static MessageFormat messageFormat(Config config, String name, String defaultValue) {
        return new MessageFormat(config.getOptionalValue(name, String.class).orElse(defaultValue));
    }

    public static Object msgException(InterceptorContext context) {
        return msg(MESSAGE_EXCEPTION, new Object[]{context.getClassName(), context.getMethod()});
    }

    public static Object msgServiceException(Exception sec) {
        return msg(MESSAGE_SERVICE_EXCEPTION, new Object[]{sec.getClass().getName(), sec.getMessage(), sec.toString()});
    }

    public static Object msgFailed(InterceptorContext context) {
        return msg(MESSAGE_FAILED, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public static Object msgSucceed(InterceptorContext context) {
        return msg(MESSAGE_SUCCEED, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public static Object msgFutureStart(InterceptorContext context) {
        return msg(MESSAGE_FUTURE_START, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public static Object msgStart(InterceptorContext context) {
        return msg(MESSAGE_START, new Object[]{context.getMethod(), context.getParameters()});
    }

    private static Object msg(MessageFormat mf, Object[] parameters) {
        return new Object() {
            @Override
            public String toString() {
                return mf.format(parameters, new StringBuffer(), null).toString();
            }
        };
    }

    public static Object restMsgException(Object... parameters) {
        return msg(REST_MESSAGE_EXCEPTION, parameters);
    }

    public static Object restMsgStart(Object... parameters) {
        return msg(REST_MESSAGE_START, parameters);
    }

    public static Object restMsgSucceed(Object... parameters) {
        return msg(REST_MESSAGE_SUCCEED, parameters);
    }

    public static Object restMsgClientSucceed(Object... parameters) {
        return msg(REST_MESSAGE_CLIENT_SUCCEED, parameters);
    }

    public static Object restMsgClientStart(Object... parameters) {
        return Configuration.msg(REST_MESSAGE_CLIENT_START, parameters);
    }

    /**
     * Gets the message for the resource key specified by locale and arguments.
     *
     * @param key       the key of resource.
     * @param arguments the arguments for the message.
     * @return the message of the resource key.
     */
    private static String getMessage(final Enum<?> key, final List<Object> arguments) {
        if (arguments != null) {
            MessageFormat msgFormat = new MessageFormat(key.name());
            return msgFormat.format(arguments.toArray(new Object[0]), new StringBuffer(), null).toString();
        }
        return key.name();
    }

}

