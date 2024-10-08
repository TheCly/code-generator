package com.codegen.processor.mapper;

import com.codegen.DefaultNameContext;
import com.codegen.processor.BaseCodeGenProcessor;
import com.codegen.spi.CodeGenProcessor;
import com.codegen.util.StringUtils;
import com.google.auto.service.AutoService;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author: Gim
 * @Date: 2019/11/25 14:14
 * @Description:
 */
@AutoService(value = CodeGenProcessor.class)
public class GenMapperProcessor extends BaseCodeGenProcessor {

    public static final String SUFFIX = "Mapper";

    @Override
    protected void generateClass(TypeElement typeElement, RoundEnvironment roundEnvironment) {
        String className = typeElement.getSimpleName() + SUFFIX;
        String packageName = typeElement.getAnnotation(GenMapper.class).pkgName();
        AnnotationSpec mapperAnnotation = AnnotationSpec.builder(Mapper.class)
                .build();
        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(className)
                .addAnnotation(mapperAnnotation)
                .addModifiers(Modifier.PUBLIC);
        FieldSpec instance = FieldSpec
                .builder(ClassName.get(packageName, className), "INSTANCE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.getMapper($T.class)",
                        Mappers.class, ClassName.get(packageName, className))
                .build();
        typeSpecBuilder.addField(instance);
        DefaultNameContext nameContext = getNameContext(typeElement);
        Optional<MethodSpec> dtoToEntityMethod = dtoToEntityMethod(typeElement, nameContext);
        dtoToEntityMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        Optional<MethodSpec> request2UpdaterMethod = request2UpdaterMethod(nameContext);
        request2UpdaterMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        Optional<MethodSpec> request2DtoMethod = request2DtoMethod(nameContext);
        request2DtoMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        Optional<MethodSpec> request2QueryMethod = request2QueryMethod(nameContext);
        request2QueryMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        Optional<MethodSpec> vo2ResponseMethod = vo2ResponseMethod(nameContext);
        vo2ResponseMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        Optional<MethodSpec> vo2CustomResponseMethod = vo2CustomResponseMethod(nameContext);
        vo2CustomResponseMethod.ifPresent(m -> typeSpecBuilder.addMethod(m));
        genJavaSourceFile(generatePackage(typeElement),
                typeElement.getAnnotation(GenMapper.class).sourcePath(), typeSpecBuilder);
    }


    @Override
    public Class<? extends Annotation> getAnnotation() {
        return GenMapper.class;
    }

    @Override
    public String generatePackage(TypeElement typeElement) {
        return typeElement.getAnnotation(GenMapper.class).pkgName();
    }

    private Optional<MethodSpec> dtoToEntityMethod(TypeElement typeElement, DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("dtoToEntity")
                    .returns(ClassName.get(typeElement))
                    .addParameter(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()),
                            "dto")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<MethodSpec> request2UpdaterMethod(DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(), nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("request2Updater")
                    .returns(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()))
                    .addParameter(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()),
                            "request")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<MethodSpec> request2DtoMethod(DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(), nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("request2Dto")
                    .returns(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()))
                    .addParameter(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()),
                            "request")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<MethodSpec> request2QueryMethod(DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(), nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("request2Query")
                    .returns(
                            ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()))
                    .addParameter(ClassName.get(nameContext.getVoPackageName(),
                            nameContext.getVoClassName()), "request")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<MethodSpec> vo2ResponseMethod(DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(), nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("vo2Response")
                    .returns(ClassName.get(nameContext.getVoPackageName(),
                            nameContext.getVoClassName()))
                    .addParameter(ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()),
                            "vo")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<MethodSpec> vo2CustomResponseMethod(DefaultNameContext nameContext) {
        boolean containsNull = StringUtils.containsNull(nameContext.getVoPackageName(), nameContext.getVoPackageName());
        if (!containsNull) {
            return Optional.of(MethodSpec
                    .methodBuilder("vo2CustomResponse")
                    .returns(ClassName.get(nameContext.getVoPackageName(),
                            nameContext.getVoClassName()))
                    .addParameter(ClassName.get(nameContext.getVoPackageName(), nameContext.getVoClassName()),
                            "vo")
                    .addCode(
                            CodeBlock.of("$T response = vo2Response(vo);\n",
                                    ClassName.get(nameContext.getVoPackageName(),
                                            nameContext.getVoClassName()))
                    )
                    .addCode(
                            CodeBlock.of("return response;")
                    )
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .build());
        }
        return Optional.empty();
    }
}