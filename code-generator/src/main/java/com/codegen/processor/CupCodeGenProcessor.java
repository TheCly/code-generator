package com.codegen.processor;

import com.codegen.context.ProcessingEnvironmentHolder;
import com.codegen.registry.CodeGenProcessorRegistry;
import com.codegen.spi.CodeGenProcessor;
import com.google.auto.service.AutoService;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

/**
 * @author cup
 */
@AutoService(Processor.class)
public class CupCodeGenProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    annotations.stream().forEach(an -> {
      Set<? extends Element> typeElements = roundEnv.getElementsAnnotatedWith(an);
      Set<TypeElement> types = ElementFilter.typesIn(typeElements);
      for (TypeElement typeElement : types) {
        CodeGenProcessor codeGenProcessor = CodeGenProcessorRegistry.find(
            an.getQualifiedName().toString());
        try {
          codeGenProcessor.generate(typeElement, roundEnv);
        } catch (Exception e) {
          ProcessingEnvironmentHolder.getEnvironment().getMessager()
              .printMessage(Kind.ERROR, "代码生成异常:" + e.getMessage());
        }
      }

    });
    return false;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingEnvironmentHolder.setEnvironment(processingEnv);
    CodeGenProcessorRegistry.initProcessors();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return CodeGenProcessorRegistry.getSupportedAnnotations();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
