package com.doanduyhai.annotation_processing_demo.codegen;

import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.*;

public class DSLClassGenerator {

    private final Filer filer;

    public DSLClassGenerator(Filer filer) {
        this.filer = filer;
    }

    public TypeSpec generateDSLClass(TypeElement entity) throws IOException {
        final String entityClassName = entity.getSimpleName().toString();
        final String dslClassName = entityClassName + "_DSL";
        final TypeName selectClass = ClassName.get(Constants.GENERATED_CODE_PACKAGE, entityClassName + "_Select");
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(dslClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("DSL class for entity $S", entityClassName)
                .addMethod(MethodSpec
                        .methodBuilder("select")
                        .addJavadoc("Start a SELECT dsl")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .returns(selectClass)
                        .addStatement("return new $T()", selectClass)
                        .build());

        return classBuilder.build();}
}
