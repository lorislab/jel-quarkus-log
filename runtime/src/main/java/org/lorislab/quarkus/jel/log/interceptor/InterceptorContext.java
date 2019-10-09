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

/**
 * The interceptor context.
 *
 * @author Andrej Petras
 */
public class InterceptorContext {

    private static final String TIME_FORMAT = "%.3f";

    /**
     * The service method.
     */
    private final String method;

    /**
     * The list of method parameters.
     */
    private final String parameters;

    /**
     * The result value.
     */
    private String result;

    /**
     * The execution time.
     */
    private String time;

    /**
     * The start time.
     */
    private final long startTime = System.currentTimeMillis();

    /**
     * The class name.
     */
    private final String className;

    public InterceptorContext(String method, String parameters, String className) {
        this.method = method;
        this.parameters = parameters;
        this.className = className;
    }

    public void closeContext(String result) {
        time = String.format(TIME_FORMAT, (System.currentTimeMillis() - startTime) / 1000f);
        this.result = result;
    }

    public void closeContext() {
        closeContext("");
    }

    public String getClassName() {
        return className;
    }


    public String getMethod() {
        return method;
    }

    public String getParameters() {
        return parameters;
    }

    public String getResult() {
        return result;
    }

    public String getTime() {
        return time;
    }
}
