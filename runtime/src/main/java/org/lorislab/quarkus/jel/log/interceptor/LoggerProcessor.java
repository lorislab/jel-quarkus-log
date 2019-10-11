package org.lorislab.quarkus.jel.log.interceptor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.Collections;
import java.util.Set;

/**
 * The logger processor.
 */
public class LoggerProcessor extends AbstractProcessor {

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        try {
            String dir = processingEnv.getOptions().get("disableTestDirectory");
            if (dir == null || dir.isEmpty()) {
                dir = "generated-test-sources";
            }
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(LoggerBuilder.IMPL);
            if (!builderFile.getName().contains(dir)) {
                String tmp = loadTemplate("/" + LoggerBuilder.IMPL);
                // write output source file
                try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                    out.write(tmp);
                }
            }
        } catch (Exception ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error creating the source file " + LoggerBuilder.IMPL);
            throw new IllegalStateException("Error creating source file.", ex);
        }

    }

    /**
     * Loads the template of the class from the class-path.
     *
     * @param template the template name.
     * @return the corresponding template.
     * @throws IOException if the method fails.
     */
    private static String loadTemplate(String template) throws IOException {
        try (InputStream inputStream = LoggerProcessor.class.getResourceAsStream(template);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            StringBuilder resultStringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
            return resultStringBuilder.toString();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(LoggerParam.class.getName());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

}
