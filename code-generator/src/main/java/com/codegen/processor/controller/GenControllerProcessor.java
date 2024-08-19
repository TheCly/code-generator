package com.codegen.processor.controller;


import com.codegen.DefaultNameContext;
import com.codegen.processor.BaseCodeGenProcessor;
import com.codegen.processor.vo.IgnoreVo;
import com.codegen.spi.CodeGenProcessor;
import com.codegen.util.StringUtils;
import com.google.auto.service.AutoService;

import com.google.gson.JsonObject;
import com.hnc.socialization.controller.BaseController;
import com.hnc.socialization.entity.User;
import com.hnc.socialization.result.Result;
import com.hnc.socialization.result.ResultUtil;
import com.hnc.socialization.utils.RequestUtil;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author G 获取名称时可以先获取上下文再取，不用一个个的取，这样更方便
 */
@AutoService(value = CodeGenProcessor.class)
public class GenControllerProcessor extends BaseCodeGenProcessor {

  public static final String CONTROLLER_SUFFIX = "Controller";

  //生成class
  @Override
  protected void generateClass(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    DefaultNameContext nameContext = getNameContext(typeElement);
    TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(nameContext.getControllerClassName())
        .addAnnotation(RestController.class)
        .addAnnotation(Slf4j.class)
        .addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S",
            StringUtils.camel(typeElement.getSimpleName().toString()) + "/v1").build())
        .addAnnotation(RequiredArgsConstructor.class)
        .superclass(BaseController.class)
        .addModifiers(Modifier.PUBLIC);
    String serviceFieldName = StringUtils.camel(typeElement.getSimpleName().toString()) + "Service";
    if (StringUtils.containsNull(nameContext.getServicePackageName())) {
      return;
    }
    FieldSpec serviceField = FieldSpec
        .builder(
            ClassName.get(nameContext.getServicePackageName(), nameContext.getServiceClassName()),
            serviceFieldName)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build();
    typeSpecBuilder.addField(serviceField);
    Optional<MethodSpec> createMethod = createMethod(serviceFieldName, typeElement, nameContext);
    createMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> updateMethod = updateMethod(serviceFieldName, typeElement, nameContext);
    updateMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findById = findById(serviceFieldName, nameContext);
    findById.ifPresent(m -> typeSpecBuilder.addMethod(m));
    Optional<MethodSpec> findByPage = findByPage(serviceFieldName, nameContext);
    findByPage.ifPresent(m -> typeSpecBuilder.addMethod(m));
    genJavaSourceFile(generatePackage(typeElement),
        typeElement.getAnnotation(GenController.class).sourcePath(), typeSpecBuilder);
  }

  @Override
  public Class<? extends Annotation> getAnnotation() {
    return GenController.class;
  }

  @Override
  public String generatePackage(TypeElement typeElement) {
    return typeElement.getAnnotation(GenController.class).pkgName();
  }

  /**
   * 创建方法
   *
   * @param serviceFieldName
   * @param typeElement
   * @param nameContext
   * @return
   */
  private Optional<MethodSpec> createMethod(String serviceFieldName, TypeElement typeElement,
      DefaultNameContext nameContext) {
    Set<VariableElement> fields = findFields(typeElement,
        ve -> Objects.nonNull(ve.getAnnotation(ApiModelProperty.class))
    );
    Builder builder = MethodSpec.methodBuilder("create" + typeElement.getSimpleName())
        .addParameter(ParameterSpec.builder(ClassName.get(typeElement), "creator")
            .addAnnotation(RequestBody.class).build())
        .addException(Exception.class)
        .addParameter(ClassName.get(HttpServletRequest.class), "request")
        .addAnnotation(
            AnnotationSpec.builder(PostMapping.class).addMember("value", "$S", "add").build())
        .addModifiers(Modifier.PUBLIC)
        .addCode(
            CodeBlock.of(" $T user = getUser(request);\n", ClassName.get(User.class))
        )
        .addCode(
            CodeBlock.of("if(user==null){\n")
        )
        .addCode(
            CodeBlock.of("return $T.businessLogicError(\"用户未登录\");\n"
                + "}\n", ClassName.get(ResultUtil.class))
        )
        .addCode(
            CodeBlock.of(
                "return $T.success($L.create$L(creator,user.getSwitchboard_identity_id()));\n",
                ClassName.get(ResultUtil.class), serviceFieldName,
                typeElement.getSimpleName().toString())
        )
        .addJavadoc("createRequest")
        .returns(Result.class);
    builder.addAnnotation(AnnotationSpec.builder(ApiOperation.class).addMember("value", "$S",
        "add接口").addMember("notes", "$S", "add接口").build());
    // 创建 ApiImplicitParams 注解构建器
    AnnotationSpec.Builder apiImplicitParamsBuilder = AnnotationSpec.builder(ApiImplicitParams.class);

    // 遍历字段，生成每个 ApiImplicitParam 注解
    for (VariableElement field : fields) {
      ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
      if (apiModelProperty != null) {
        // 构建 ApiImplicitParam 注解
        AnnotationSpec apiImplicitParam = AnnotationSpec.builder(ApiImplicitParam.class)
            .addMember("name", "$S", field.getSimpleName().toString())
            .addMember("value", "$S", apiModelProperty.value())
            .addMember("dataType", "$S", field.asType().toString()) // 假设数据类型为 String，可以根据需要调整
            .addMember("required", "$L", apiModelProperty.required()) // 使用 ApiModelProperty 的 required 属性
            .build();

        // 将 ApiImplicitParam 注解添加到 ApiImplicitParams 中
        apiImplicitParamsBuilder.addMember("value", "$L", apiImplicitParam);
      }
    }
    builder.addAnnotation(apiImplicitParamsBuilder.build());
    return Optional.of(builder.build());
  }

  /**
   * 更新方法
   *
   * @param serviceFieldName
   * @param typeElement
   * @param nameContext
   * @return
   */
  private Optional<MethodSpec> updateMethod(String serviceFieldName, TypeElement typeElement,
      DefaultNameContext nameContext) {

    return Optional.empty();
  }

  /**
   * 启用
   *
   * @param serviceFieldName
   * @param typeElement
   * @return
   */
  private Optional<MethodSpec> validMethod(String serviceFieldName, TypeElement typeElement) {

    return Optional.empty();
  }

  /**
   * 修复不返回方法的问题
   *
   * @param serviceFieldName
   * @param typeElement
   * @return
   */
  private Optional<MethodSpec> inValidMethod(String serviceFieldName, TypeElement typeElement) {

    return Optional.empty();
  }

  private Optional<MethodSpec> findById(String serviceFieldName, DefaultNameContext nameContext) {

    return Optional.empty();
  }

  /**
   * 分页
   *
   * @param serviceFieldName
   * @param nameContext
   * @return
   */
  private Optional<MethodSpec> findByPage(String serviceFieldName, DefaultNameContext nameContext) {

    return Optional.empty();
  }
}
