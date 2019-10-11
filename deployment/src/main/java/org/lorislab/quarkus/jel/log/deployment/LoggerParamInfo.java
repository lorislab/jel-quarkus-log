package org.lorislab.quarkus.jel.log.deployment;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

/**
 * The logger parameter info.
 */
class LoggerParamInfo {

    /**
     * The dot name of the parameter.
     */
    DotName name;

    /**
     * The method information.
     */
    MethodInfo methodInfo;

    /**
     * The priority
     */
    int priority;
}
