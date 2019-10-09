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

import java.util.List;
import java.util.Map;

public interface LogException {

        /**
     * Gets the key of resource.
     *
     * @return the key of resource.
     */
    Enum<?> getKey();

    /**
     * Gets the parameters of the message.
     *
     * @return the parameters of the message.
     */
    List<Object> getParameters();

    /**
     * Gets the named parameters of the message.
     *
     * @return the named parameters of the message.
     */
    Map<String, Object> getNamedParameters();

    boolean isStackTraceLog();

    void setStackTraceLog(boolean stackTraceLog);
}
