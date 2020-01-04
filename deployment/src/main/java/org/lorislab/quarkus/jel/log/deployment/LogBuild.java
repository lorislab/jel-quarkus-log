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

package org.lorislab.quarkus.jel.log.deployment;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import org.jboss.jandex.*;
import org.lorislab.quarkus.jel.log.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Singleton;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogBuild {

    private static final Logger log = LoggerFactory.getLogger(LogBuild.class);

    BuildConfig buildConfig;

    private static final DotName LOG_PARAM = DotName.createSimple(LoggerParam.class.getName());

    private static final String LOG_BUILDER_SERVICE = LoggerBuilderService.class.getName();

    private static final List<DotName> ANNOTATION_DOT_NAMES =
            Stream.of(ApplicationScoped.class, Singleton.class, RequestScoped.class)
            .map(Class::getName).map(DotName::createSimple).collect(Collectors.toList());

    static final String FEATURE_NAME = "jel-log";

    @BuildStep
    FeatureBuildItem createFeatureItem() {
        return new FeatureBuildItem(FEATURE_NAME);
    }

    @BuildStep
    void initialized(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitialized) {
        runtimeInitialized.produce(new RuntimeInitializedClassBuildItem(RestClientLogInterceptor.class.getCanonicalName()));
    }

    @BuildStep
    void loggerParameter(CombinedIndexBuildItem indexBuildItem, BuildProducer<BytecodeTransformerBuildItem> transformers) {

        Collection<AnnotationInstance> annotations = indexBuildItem.getIndex().getAnnotations(LOG_PARAM);
        if (annotations != null && !annotations.isEmpty()) {

            Map<DotName, LoggerParamInfo> mapClasses = new HashMap<>();
            Map<DotName, LoggerParamInfo> mapAssignableFrom = new HashMap<>();

            for (AnnotationInstance annotation : annotations) {
                MethodInfo methodInfo = annotation.target().asMethod();
                short mod = methodInfo.flags();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                    int priority = annotation.valueWithDefault(indexBuildItem.getIndex(), "priority").asInt();
                    addToMap(mapClasses, annotation, "classes", methodInfo, priority);
                    addToMap(mapAssignableFrom, annotation, "assignableFrom", methodInfo, priority);
                } else {
                    log.warn("The method {}.{} is not public static and will be ignored.", methodInfo.declaringClass(), methodInfo.name());
                }
            }

            if (!mapClasses.isEmpty() || !mapAssignableFrom.isEmpty()) {
                log.debug("MapClasses: {}", mapClasses);
                log.debug("MapAssignableFrom: {}", mapAssignableFrom);
                transformers.produce(new BytecodeTransformerBuildItem(LoggerBuilder.IMPL, new LogBuilderEnhancer(mapClasses, mapAssignableFrom)));
            }
        }
    }

    private void addToMap(Map<DotName, LoggerParamInfo> map, AnnotationInstance annotation, String valueName, MethodInfo methodInfo, int priority) {
        List<DotName> classes = getClassArrayValue(annotation, valueName);
        for (DotName name : classes) {
            LoggerParamInfo info = map.get(name);
            if (info == null) {
                info = new LoggerParamInfo();
                info.name = name;
                info.priority = priority;
                info.methodInfo = methodInfo;
                map.put(name, info);
            } else {
                if (info.priority < priority) {
                    info.priority = priority;
                    info.methodInfo = methodInfo;
                }
            }
        }
    }


    private List<DotName> getClassArrayValue(AnnotationInstance annotation, String name) {
        AnnotationValue value = annotation.value(name);
        if (value == null) {
            return Collections.emptyList();
        }
        Type[] classes = value.asClassArray();
        if (classes.length > 0) {
            List<DotName> result = new ArrayList<>(classes.length);
            for (Type type : classes) {
                result.add(type.name());
            }
            return result;
        }
        return Collections.emptyList();
    }

    @BuildStep
    public AnnotationsTransformerBuildItem interceptorBinding() {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {

            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            public void transform(TransformationContext context) {
                ClassInfo target = context.getTarget().asClass();
                Map<DotName, List<AnnotationInstance>> tmp = target.annotations();
                Optional<DotName> dot = ANNOTATION_DOT_NAMES.stream().filter(tmp::containsKey).findFirst();
                if (dot.isPresent()) {
                    String name = target.name().toString();
                    Optional<String> add = buildConfig.includes.stream().filter(name::startsWith).findFirst();
                    if (add.isPresent() && !LOG_BUILDER_SERVICE.equals(name)) {
                        context.transform().add(LoggerService.class).done();
                    }
                }
            }
        });
    }

}
