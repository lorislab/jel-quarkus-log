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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The logger builder interface.
 */
public interface LoggerBuilder {

    /**
     * The implementation class.
     */
    String IMPL = LoggerBuilder.class.getName() + "Impl";

    /**
     * Gets the map of the mapping function for the class.
     *
     * @return the map of the mapping function for the class.
     */
    default Map<Class, Function<Object, String>> getClasses() {
        return new HashMap<>();
    }

    /**
     * Gets the map of the mapping function for the assignable class.
     *
     * @return the map of the mapping function for the assignable class.
     */
    default Map<Class<?>, Function<Object, String>> getAssignableFrom() {
        return new HashMap<>();
    }

}
