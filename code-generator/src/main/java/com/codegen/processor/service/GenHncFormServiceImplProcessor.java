package com.codegen.processor.service;


import com.codegen.DefaultNameContext;
import com.codegen.processor.BaseCodeGenProcessor;
import com.codegen.spi.CodeGenProcessor;
import com.codegen.util.StringUtils;
import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.hnc.socialization.entity.hnc.HncProduct;
import com.hnc.socialization.hennengcai.HncFormAliasDao;
import com.hnc.socialization.hennengcai.HncSinglePage;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gim 获取名称时可以先获取上下文再取，不用一个个的取，这样更方便
 */
@AutoService(value = CodeGenProcessor.class)
public class GenHncFormServiceImplProcessor extends BaseCodeGenProcessor {

  public static final String IMPL_SUFFIX = "ServiceImpl";
  public static final String HNCFROMDAO = "hncFormAliasDao";
  public static final String SWITCHID = "switchboard_identity_id";

  @Override
  protected void generateClass(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    DefaultNameContext nameContext = getNameContext(typeElement);
    String className = typeElement.getSimpleName() + IMPL_SUFFIX;
    TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
        .addSuperinterface(
            ClassName.get(nameContext.getServicePackageName(), nameContext.getServiceClassName()))
        .addAnnotation(Transactional.class)
        .addAnnotation(Service.class)
        .addAnnotation(Slf4j.class)
        .addAnnotation(RequiredArgsConstructor.class)
        .addModifiers(Modifier.PUBLIC);
    String classFieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
        typeElement.getSimpleName().toString());
    FieldSpec repositoryField = FieldSpec
        .builder(HncFormAliasDao.class, HNCFROMDAO)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build();

    typeSpecBuilder.addField(repositoryField);
    Optional<MethodSpec> createMethod = createMethod(typeElement, nameContext, HNCFROMDAO,
        classFieldName);
    createMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> updateMethod = updateMethod(typeElement, nameContext, HNCFROMDAO);
    updateMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findByIdMethod = findByIdMethod(typeElement, nameContext);
    findByIdMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findByPageMethod = findByPageMethod(typeElement, nameContext,
        HNCFROMDAO);
    findByPageMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findAllMethod = findAllMethod(typeElement, nameContext,
        HNCFROMDAO);
    findAllMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    genJavaSourceFile(generatePackage(typeElement),
        typeElement.getAnnotation(GenHncFormServiceImpl.class).sourcePath(), typeSpecBuilder);
  }

  @Override
  public Class<? extends Annotation> getAnnotation() {
    return GenHncFormServiceImpl.class;
  }

  @Override
  public String generatePackage(TypeElement typeElement) {
    return typeElement.getAnnotation(GenHncFormServiceImpl.class).pkgName();
  }

  private Optional<MethodSpec> createMethod(TypeElement typeElement, DefaultNameContext nameContext,
      String repositoryFieldName, String classFieldName) {
    return Optional.of(MethodSpec.methodBuilder("create" + typeElement.getSimpleName())
        .addParameter(ClassName.get(typeElement), "creator")
        .addParameter(String.class, SWITCHID)
        .addModifiers(Modifier.PUBLIC)

        .addException(Exception.class)
        .addCode(
            CodeBlock.of(
                "return $L.add(creator, " + SWITCHID + ", HncFormAliasDao.QUERY_AUTHORITY_All);\n"
                , repositoryFieldName)
        )
        .addJavadoc("createImpl")
        .addAnnotation(Override.class)
        .returns(String.class).build());
  }

  private Optional<MethodSpec> updateMethod(TypeElement typeElement, DefaultNameContext nameContext,
      String repositoryFieldName) {
    return Optional.of(MethodSpec.methodBuilder("update" + typeElement.getSimpleName())
        .addParameter(ClassName.get(typeElement), "updater")
        .addParameter(String.class, "id")
        .addParameter(String.class, SWITCHID)
        .addModifiers(Modifier.PUBLIC)
        .addException(Exception.class)
        .addCode(
            CodeBlock.of("hncFormAliasDao.update(updater," + SWITCHID +
                ", updater.getId(), HncFormAliasDao.QUERY_AUTHORITY_All);\n"
            )
        )
        .addJavadoc("update")
        .addAnnotation(Override.class)
        .build());
  }

  private Optional<MethodSpec> findByIdMethod(TypeElement typeElement,
      DefaultNameContext nameContext) {

    return Optional.of(MethodSpec.methodBuilder("findById")
        .addParameter(String.class, "id")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(String.class, SWITCHID)
        .addException(Exception.class)
        .addJavadoc("findById")
        .addAnnotation(Override.class)
        .addCode(CodeBlock.of(
            "return hncFormAliasDao.showSigle($T.class, id," + SWITCHID + " , false);\n",
            typeElement))
        .returns(ClassName.get(typeElement))
        .build());

  }


  private Optional<MethodSpec> findByPageMethod(TypeElement typeElement,
      DefaultNameContext nameContext, String repositoryFieldName) {

    Builder methodBuilder = MethodSpec.methodBuilder("findByPage")
        .addParameter(ParameterizedTypeName.get(HashMap.class, String.class, Object.class), "map")
        .addParameter(Integer.class, "currentPage")
        .addParameter(Integer.class, "pageSize")
        .addException(Exception.class)
        .addParameter(String.class, SWITCHID)
        .addParameter(ClassName.get(typeElement), "from")
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("findByPage")
        .addAnnotation(Override.class)
        .returns(ParameterizedTypeName.get(ClassName.get(HncSinglePage.class),
            ClassName.get(typeElement)));
    methodBuilder.addCode("if(from!=null){\n");
    List<? extends Element> fieldTypes = typeElement.getEnclosedElements();
    for (Element field : fieldTypes) {
      if (field.getKind() != ElementKind.FIELD) {
        continue;
      }
      String fieldName = field.getSimpleName().toString();
      String getterMethod =
          "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "()";
      methodBuilder.addCode("if (from." + getterMethod + " != null) {\n");
      if (field.asType().toString().equals(String.class.getCanonicalName())) {
        methodBuilder.addCode("  if (!from." + getterMethod + ".isEmpty()) {\n");
        methodBuilder.addCode("    map.put($S, from." + getterMethod + ");\n",
            field.getSimpleName());
        methodBuilder.addCode("  }\n");
      } else {
        methodBuilder.addCode("  map.put($S, from." + getterMethod + ");\n", field.getSimpleName());
      }

      methodBuilder.addCode("}\n");
    }
    methodBuilder.addCode("}\n");
    methodBuilder.addCode(
        CodeBlock.of(" return hncFormAliasDao.pageSigleObject"
                + "($T.class, switchboard_identity_id,map,false,HncFormAliasDao.QUERY_AUTHORITY_All, currentPage, pageSize);\n",
            typeElement)
    );
    MethodSpec build = methodBuilder.build();
    return Optional.of(build);


  }

  private Optional<MethodSpec> findAllMethod(TypeElement typeElement,
      DefaultNameContext nameContext, String repositoryFieldName) {
    boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(),
        nameContext.getVoPackageName());

    Builder methodBuilder = MethodSpec.methodBuilder("findAll")
        .addParameter(ParameterizedTypeName.get(HashMap.class, String.class, Object.class), "map")
        .addParameter(String.class, SWITCHID)
        .addParameter(ClassName.get(typeElement), "from")
        .addModifiers(Modifier.PUBLIC)
        .addException(Exception.class)
        .addJavadoc("findAll")
        .addAnnotation(Override.class)
        .returns(ParameterizedTypeName.get(ClassName.get(List.class),
            ClassName.get(typeElement)));
    methodBuilder.addCode("if(from!=null){\n");
    List<? extends Element> fieldTypes = typeElement.getEnclosedElements();
    for (Element field : fieldTypes) {
      if (field.getKind() != ElementKind.FIELD) {
        continue;
      }
      String fieldName = field.getSimpleName().toString();
      String getterMethod =
          "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "()";
      methodBuilder.addCode("if (from." + getterMethod + " != null) {\n");
      if (field.asType().toString().equals(String.class.getCanonicalName())) {
        methodBuilder.addCode("  if (!from." + getterMethod + ".isEmpty()) {\n");
        methodBuilder.addCode("    map.put($S, from." + getterMethod + ");\n",
            field.getSimpleName());
        methodBuilder.addCode("  }\n");
      } else {
        methodBuilder.addCode("  map.put($S, from." + getterMethod + ");\n", field.getSimpleName());
      }
      methodBuilder.addCode("}\n");
    }
    methodBuilder.addCode("}\n");
    methodBuilder.addCode(
        CodeBlock.of(" return hncFormAliasDao.listSigle"
                + "($T.class, switchboard_identity_id,map,false,HncFormAliasDao.QUERY_AUTHORITY_All);\n",
            typeElement)
    );
    MethodSpec build = methodBuilder.build();
    return Optional.of(build);
  }

}
