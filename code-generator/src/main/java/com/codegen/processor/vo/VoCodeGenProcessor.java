package com.codegen.processor.vo;

import com.codegen.processor.BaseCodeGenProcessor;
import com.codegen.spi.CodeGenProcessor;
import com.google.auto.service.AutoService;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import io.swagger.annotations.ApiModel;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Data;

/**
 * @author gim vo 代码生成器
 */
@AutoService(value = CodeGenProcessor.class)
public class VoCodeGenProcessor extends BaseCodeGenProcessor {

  public static final String SUFFIX = "VO";

  @Override
  public Class<? extends Annotation> getAnnotation() {
    return GenVo.class;
  }

  @Override
  public String generatePackage(TypeElement typeElement) {
    return typeElement.getAnnotation(GenVo.class).pkgName();
  }

  @Override
  protected void generateClass(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    Set<VariableElement> fields = findFields(typeElement,
        ve -> Objects.isNull(ve.getAnnotation(IgnoreVo.class)));
    String className = typeElement.getSimpleName() + SUFFIX;
    Builder builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ApiModel.class)//v3用Schema，功能更齐全
        .addAnnotation(Data.class);
    addSetterAndGetterMethod(builder, fields);
    builder.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PROTECTED)
        .build());
    genJavaSourceFile(generatePackage(typeElement),
        typeElement.getAnnotation(GenVo.class).sourcePath(),
        builder);
  }
}