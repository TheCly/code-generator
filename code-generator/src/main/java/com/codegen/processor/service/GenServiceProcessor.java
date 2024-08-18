package com.codegen.processor.service;


import com.codegen.DefaultNameContext;
import com.codegen.processor.BaseCodeGenProcessor;
import com.codegen.spi.CodeGenProcessor;
import com.codegen.util.StringUtils;
import com.google.auto.service.AutoService;

import com.hnc.socialization.entity.hnc.HncProduct;
import com.hnc.socialization.hennengcai.HncSinglePage;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * @author gim
 */
@AutoService(value = CodeGenProcessor.class)
public class GenServiceProcessor extends BaseCodeGenProcessor {

  public static final String SERVICE_SUFFIX = "Service";

  public static final String SERVICE_PREFIX = "";
  public static final String HNCFROMDAO = "hncFormAliasDao";

  public static final String SWITCHID = "switchboard_identity_id";

  @Override
  protected void generateClass(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String className = SERVICE_PREFIX + typeElement.getSimpleName() + SERVICE_SUFFIX;
    TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(className)
        .addModifiers(Modifier.PUBLIC);

    DefaultNameContext nameContext = getNameContext(typeElement);
    Optional<MethodSpec> createMethod = createMethod(typeElement, nameContext);
    createMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> updateMethod = updateMethod(typeElement);
    updateMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findByIdMethod = findByIdMethod(nameContext);
    findByIdMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findByPageMethod = findByPageMethod(nameContext);
    findByPageMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findAllMethod = findAllMethod(nameContext);
    findAllMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    genJavaSourceFile(generatePackage(typeElement),
        typeElement.getAnnotation(GenService.class).sourcePath(), typeSpecBuilder);
  }

  @Override
  public Class<? extends Annotation> getAnnotation() {
    return GenService.class;
  }

  @Override
  public String generatePackage(TypeElement typeElement) {
    return typeElement.getAnnotation(GenService.class).pkgName();
  }

  private Optional<MethodSpec> createMethod(TypeElement typeElement,
      DefaultNameContext nameContext) {
    return Optional.of(MethodSpec.methodBuilder("create" + typeElement.getSimpleName())
        .addParameter(ClassName.get(typeElement), "creator")
        .addParameter(String.class, SWITCHID)
        .addModifiers(Modifier.PUBLIC)
        .addModifiers(Modifier.ABSTRACT)
        .addException(Exception.class)
        .addJavadoc("createImpl")
        .returns(String.class)
        .build());
  }


  private Optional<MethodSpec> updateMethod(TypeElement typeElement) {
    return Optional.of(MethodSpec.methodBuilder("update" + typeElement.getSimpleName())
        .addParameter(ClassName.get(typeElement), "updater")
        .addParameter(String.class, "id")
        .addParameter(String.class, SWITCHID)
        .addException(Exception.class)
        .addModifiers(Modifier.PUBLIC)
        .addModifiers(Modifier.ABSTRACT)
        .addJavadoc("update")
        .build());
  }

  private Optional<MethodSpec> findByIdMethod(DefaultNameContext nameContext) {
    return Optional.of(MethodSpec.methodBuilder("findById")
        .addParameter(String.class, "id")
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addJavadoc("findById")
        .addParameter(String.class, SWITCHID)
        .addException(Exception.class)
        .returns(
            ClassName.get(nameContext.getOriginalPackageName(), nameContext.getOriginalClassName()))
        .build());
  }

  private Optional<MethodSpec> findByPageMethod(DefaultNameContext nameContext) {
    Builder methodBuilder = MethodSpec.methodBuilder("findByPage")
        .addParameter(ParameterizedTypeName.get(HashMap.class, String.class, Object.class), "map")
        .addParameter(Integer.class, "currentPage")
        .addParameter(Integer.class, "pageSize")
        .addParameter(String.class, SWITCHID)
        .addException(Exception.class)
        .addParameter(
            ClassName.get(nameContext.getOriginalPackageName(), nameContext.getOriginalClassName()),
            "from")
        .addModifiers(Modifier.PUBLIC)
        .addModifiers(Modifier.ABSTRACT)
        .addJavadoc("findByPage")
        .returns(ParameterizedTypeName.get(ClassName.get(HncSinglePage.class),
            ClassName.get(nameContext.getOriginalPackageName(),
                nameContext.getOriginalClassName())));
    MethodSpec build = methodBuilder.build();
    return Optional.of(build);

  }

  private Optional<MethodSpec> findAllMethod(DefaultNameContext nameContext) {
    Builder methodBuilder = MethodSpec.methodBuilder("findAll")
        .addParameter(ParameterizedTypeName.get(HashMap.class, String.class, Object.class), "map")
        .addParameter(String.class, SWITCHID)
        .addParameter(
            ClassName.get(nameContext.getOriginalPackageName(), nameContext.getOriginalClassName()),
            "from")
        .addModifiers(Modifier.PUBLIC)
        .addException(Exception.class)
        .addModifiers(Modifier.ABSTRACT)
        .addJavadoc("findAll")
        .returns(ParameterizedTypeName.get(ClassName.get(List.class),
            ClassName.get(nameContext.getOriginalPackageName(),
                nameContext.getOriginalClassName())));
    MethodSpec build = methodBuilder.build();
    return Optional.of(build);
  }
}
