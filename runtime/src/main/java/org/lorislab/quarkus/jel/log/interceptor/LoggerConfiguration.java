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

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.text.MessageFormat;

/**
 * The logger configuration.
 *
 * @author Andrej Petras
 */
@ApplicationScoped
@LoggerService(log = false)
public class LoggerConfiguration {

    @ConfigProperty(name = "org.lorislab.jel.logger.nouser", defaultValue = "anonymous")
    String noUser;

    @ConfigProperty(name = "org.lorislab.jel.logger.result.void", defaultValue = "void")
    String resultVoid;

    @ConfigProperty(name = "org.lorislab.jel.logger.start", defaultValue = "{0}({1}) started.")
    String patternStart;

    @ConfigProperty(name = "org.lorislab.jel.logger.succeed", defaultValue = "{0}({1}):{2} [{3}s] succeed.")
    String patternSucceed;

    @ConfigProperty(name = "org.lorislab.jel.logger.failed", defaultValue = "{0}({1}):{2} [{3}s] failed.")
    String patternFailed;

    @ConfigProperty(name = "org.lorislab.jel.logger.futureStart", defaultValue = "{0}({1}) future started.")
    String patternFutureStart;

    private MessageFormat messageStart;

    private MessageFormat messageSucceed;

    private MessageFormat messageFutureStart;

    private MessageFormat messageFailed;

    @PostConstruct
    public void init() {
        messageStart = new MessageFormat(patternStart);
        messageSucceed = new MessageFormat(patternSucceed);
        messageFailed = new MessageFormat(patternFailed);
        messageFutureStart = new MessageFormat(patternFutureStart);
    }

    public Object msgFailed(InterceptorContext context) {
        return msg(messageFailed, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public Object msgSucceed(InterceptorContext context) {
        return msg(messageSucceed, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public Object msgFutureStart(InterceptorContext context) {
        return msg(messageFutureStart, new Object[]{context.getMethod(), context.getParameters(), context.getResult(), context.getTime()});
    }

    public Object msgStart(InterceptorContext context) {
        return msg(messageStart, new Object[]{context.getMethod(), context.getParameters()});
    }

    private static Object msg(MessageFormat mf, Object[] parameters) {
        return new Object() {
            @Override
            public String toString() {
                return mf.format(parameters, new StringBuffer(), null).toString();
            }
        };
    }

}

