package org.lorislab.quarkus.jel.log.deployment;

import org.jboss.jandex.DotName;
import org.objectweb.asm.*;

import java.util.Map;
import java.util.function.BiFunction;

public class LogBuilderEnhancer implements BiFunction<String, ClassVisitor, ClassVisitor> {

    private Map<DotName, LoggerParamInfo> mapClasses;

    private Map<DotName, LoggerParamInfo> mapAssignableFrom;

    public LogBuilderEnhancer(Map<DotName, LoggerParamInfo> mapClasses, Map<DotName, LoggerParamInfo> mapAssignableFrom) {
        this.mapClasses = mapClasses;
        this.mapAssignableFrom = mapAssignableFrom;
    }

    @Override
    public ClassVisitor apply(String className, ClassVisitor outputClassVisitor) {
        return new AbstractEntityBuilderEnhancerClassVisitor(outputClassVisitor, mapClasses, mapAssignableFrom);
    }

    static class AbstractEntityBuilderEnhancerClassVisitor extends ClassVisitor {

        private Map<DotName, LoggerParamInfo> mapClasses;

        private Map<DotName, LoggerParamInfo> mapAssignableFrom;

        public AbstractEntityBuilderEnhancerClassVisitor(ClassVisitor outputClassVisitor, Map<DotName, LoggerParamInfo> mapClasses, Map<DotName, LoggerParamInfo> mapAssignableFrom) {
            super(Opcodes.ASM7, outputClassVisitor);
            this.mapClasses = mapClasses;
            this.mapAssignableFrom = mapAssignableFrom;
        }

        @Override
        public void visitEnd() {
            if (mapClasses != null && !mapClasses.isEmpty()) {
                addClasses("getClasses", mapClasses);
            }
            if (mapAssignableFrom != null && !mapAssignableFrom.isEmpty()) {
                addClasses("getAssignableFrom", mapAssignableFrom);
            }
            super.visitEnd();
        }

        private void addClasses(String methodName, Map<DotName, LoggerParamInfo> data) {
            MethodVisitor visitor = super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE,
                    methodName,
                    "()Ljava/util/Map;",
                    "()Ljava/util/Map<Ljava/lang/Class;Ljava/util/function/Function<Ljava/lang/Object;Ljava/lang/String;>;>;",
                    null);

            visitor.visitCode();
            visitor.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
            visitor.visitInsn(Opcodes.DUP);
            visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            visitor.visitVarInsn(Opcodes.ASTORE, 1);

            for (Map.Entry<DotName, LoggerParamInfo> entry : data.entrySet()) {
                LoggerParamInfo info = entry.getValue();
                addMapping(visitor, getInternalName(info.name), getInternalName(info.methodInfo.declaringClass().name()), info.methodInfo.name());
            }
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitInsn(Opcodes.ARETURN);
            visitor.visitMaxs(0, 1);
            visitor.visitEnd();
        }

        private void addMapping(MethodVisitor visitor, String className, String methodClass, String methodName) {
            Handle handle = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                    "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);

            Type param1 = Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;");
            Handle param2 = new Handle(Opcodes.H_INVOKESTATIC, methodClass,  methodName, "(Ljava/lang/Object;)Ljava/lang/String;", false);
            Type param3 = Type.getType("(Ljava/lang/Object;)Ljava/lang/String;");

            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitLdcInsn(Type.getType("L" + className + ";"));
            visitor.visitInvokeDynamicInsn("apply", "()Ljava/util/function/Function;", handle, param1, param2, param3);
            visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            visitor.visitInsn(Opcodes.POP);
        }

        private static String getInternalName(final DotName clazz) {
            return clazz.toString().replace('.', '/');
        }
    }
}
