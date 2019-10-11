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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Logger builder service.
 */
@Singleton
public class LoggerBuilderService {

    /**
     * The map of classes.
     */
    private static final Map<Class, Function<Object, String>> CLASSES = new ConcurrentHashMap<>();

    /**
     * The map of assignable classes.
     */
    private static final Map<Class<?>, Function<Object, String>> ASSIGNABLE_FROM = new HashMap<>();

    /**
     * Static init.
     */
    static {
        CLASSES.put(Class[].class, LoggerBuilderService::array);
        CLASSES.put(int[].class, LoggerBuilderService::array);
        CLASSES.put(double[].class, LoggerBuilderService::array);
        CLASSES.put(float[].class, LoggerBuilderService::array);
        CLASSES.put(boolean[].class, LoggerBuilderService::array);
        CLASSES.put(long[].class, LoggerBuilderService::array);
        CLASSES.put(byte[].class, LoggerBuilderService::array);
        CLASSES.put(Integer[].class, LoggerBuilderService::array);
        CLASSES.put(Double[].class, LoggerBuilderService::array);
        CLASSES.put(String[].class, LoggerBuilderService::array);
        CLASSES.put(Boolean[].class, LoggerBuilderService::array);
        CLASSES.put(Long[].class, LoggerBuilderService::array);
        CLASSES.put(Byte[].class, LoggerBuilderService::array);
        CLASSES.put(Class.class, LoggerBuilderService::basic);
        CLASSES.put(byte.class, LoggerBuilderService::basic);
        CLASSES.put(int.class, LoggerBuilderService::basic);
        CLASSES.put(double.class, LoggerBuilderService::basic);
        CLASSES.put(float.class, LoggerBuilderService::basic);
        CLASSES.put(boolean.class, LoggerBuilderService::basic);
        CLASSES.put(long.class, LoggerBuilderService::basic);
        CLASSES.put(Integer.class, LoggerBuilderService::basic);
        CLASSES.put(Double.class, LoggerBuilderService::basic);
        CLASSES.put(String.class, LoggerBuilderService::basic);
        CLASSES.put(Boolean.class, LoggerBuilderService::basic);
        CLASSES.put(Long.class, LoggerBuilderService::basic);
        CLASSES.put(Byte.class, LoggerBuilderService::basic);
        CLASSES.put(Enum.class, LoggerBuilderService::enumeration);
        CLASSES.put(HashMap.class, LoggerBuilderService::map);
        CLASSES.put(HashSet.class, LoggerBuilderService::collection);
        CLASSES.put(ArrayList.class, LoggerBuilderService::collection);

        ASSIGNABLE_FROM.put(Collection.class, LoggerBuilderService::collection);
        ASSIGNABLE_FROM.put(InputStream.class, LoggerBuilderService::inputStream);
        ASSIGNABLE_FROM.put(Map.class, LoggerBuilderService::map);
        ASSIGNABLE_FROM.put(OutputStream.class, LoggerBuilderService::outputStream);
        ASSIGNABLE_FROM.put(Response.class, LoggerBuilderService::response);
    }

    /**
     * The generated logger builder.
     */
    @Inject
    LoggerBuilder loggerBuilder;

    /**
     * Init method.
     */
    @PostConstruct
    public void init() {
        CLASSES.putAll(loggerBuilder.getClasses());
        ASSIGNABLE_FROM.putAll(loggerBuilder.getAssignableFrom());
    }

    /**
     * Gets the method parameter value.
     *
     * @param parameter the method parameter.
     * @return the value from the parameter.
     */
    public String getParameterValue(Object parameter) {
        if (parameter != null) {
            Class<?> clazz = parameter.getClass();
            Function<Object, String> fn = CLASSES.get(clazz);
            if (fn != null) {
                return fn.apply(parameter);
            }

            for (Map.Entry<Class<?>, Function<Object, String>> entry : ASSIGNABLE_FROM.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    Function<Object, String> fn2 = entry.getValue();
                    CLASSES.put(clazz, fn2);
                    return fn2.apply(parameter);
                }
            }
        }
        return "" + parameter;
    }

    /**
     * Mapping method for the basic.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String basic(Object parameter) {
        return "" + parameter;
    }

    /**
     * Mapping method for the array.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String array(Object parameter) {
        return parameter.getClass().getSimpleName() + "[" + Array.getLength(parameter) + "]";
    }

    /**
     * Mapping method for the enumeration.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String enumeration(Object parameter) {
        return parameter.getClass().getSimpleName() + ":" + parameter.toString();
    }

    /**
     * Mapping method for the input-stream.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String inputStream(Object parameter) {
        return parameter.getClass().getSimpleName();
    }

    /**
     * Mapping method for the output-stream.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String outputStream(Object parameter) {
        return parameter.getClass().getSimpleName();
    }

    /**
     * Mapping method for the response.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String response(Object parameter) {
        Response response = (Response) parameter;
        Response.StatusType status = response.getStatusInfo();
        return "[" + status.getStatusCode() + "-" + status.getReasonPhrase() + "," + response.hasEntity() + "]";
    }

    /**
     * Mapping method for the map.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String map(Object parameter) {
        StringBuilder sb = new StringBuilder();

        String name = parameter.getClass().getSimpleName();
        Map<?, ?> tmp = (Map<?, ?>) parameter;

        if (tmp.isEmpty()) {
            sb.append("empty ").append(name);
        } else {
            sb.append(name).append(' ').append(tmp.size()).append(" of [");

            String keyClassName = null;
            String valueClassName = null;

            if (parameter.getClass().getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getClass().getGenericSuperclass();

                Type keyType = parameterizedType.getActualTypeArguments()[0];
                if (!(keyType instanceof TypeVariable)) {
                    keyClassName = keyType.getClass().getSimpleName();
                }

                Type valueType = parameterizedType.getActualTypeArguments()[1];
                if (!(valueType instanceof TypeVariable)) {
                    keyClassName = valueType.getClass().getSimpleName();
                }
            }

            Map.Entry<?, ?> item = tmp.entrySet().iterator().next();
            // get key class name
            if (keyClassName == null && item.getKey() != null) {
                keyClassName = item.getKey().getClass().getSimpleName();
            }
            // get value class name
            if (item.getValue() != null) {
                valueClassName = item.getValue().getClass().getSimpleName();
            }
            sb.append(keyClassName).append('+').append(valueClassName).append(']');
        }
        return sb.toString();
    }

    /**
     * Mapping method for the collection.
     *
     * @param parameter to map.
     * @return the corresponding string result for the log.
     */
    public static String collection(Object parameter) {
        Collection<?> tmp = (Collection<?>) parameter;
        String name = tmp.getClass().getSimpleName();

        StringBuilder sb = new StringBuilder();

        if (tmp.isEmpty()) {
            sb.append("empty ").append(name);
        } else {
            sb.append(name).append('(').append(tmp.size());
            Class clazz = Object.class;
            if (parameter.getClass().getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getClass().getGenericSuperclass();
                Type type = parameterizedType.getActualTypeArguments()[0];
                clazz = type.getClass();

                // check generic type variable
                if (type instanceof TypeVariable) {

                    // load first item from the collection
                    Object obj = tmp.iterator().next();
                    if (obj != null) {
                        clazz = obj.getClass();
                    }
                }
            } else {
                Object obj = tmp.iterator().next();
                if (obj != null) {
                    clazz = obj.getClass();
                }
            }
            sb.append(clazz.getSimpleName());
            sb.append(')');
        }
        return sb.toString();
    }
}
