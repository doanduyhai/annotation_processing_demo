package com.doanduyhai.annotation_processing_demo.codegen;

import java.io.IOException;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.doanduyhai.annotation_processing_demo.dsl.AbstractSelect;
import com.squareup.javapoet.*;

public class SelectClassGenerator {

    private final Filer filer;

    public SelectClassGenerator(Filer filer) {
        this.filer = filer;
    }

    public TypeSpec generateSelectClass(TypeElement entity, List<VariableElement> columns) throws IOException {
        final String entityClassName = entity.getSimpleName().toString();
        final String selectClassName = entityClassName + "_Select";
        final String whereClassName = entityClassName + "_Where";
        final TypeName superClass =  ParameterizedTypeName.get(ClassName.get(AbstractSelect.class),
                ClassName.get(Constants.GENERATED_CODE_PACKAGE, whereClassName));
        final TypeName selectClass = ClassName.get(Constants.GENERATED_CODE_PACKAGE, selectClassName);

        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(selectClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(superClass)
                .addJavadoc("This is the impl SELECT class for entity $S", entityClassName);

        // Add xxx() methods
        columns.forEach(column -> classBuilder.addMethod(generateSelectClause(selectClass, column)));

        // Generate the where() method
        classBuilder.addMethod(generateWhereMethod(entityClassName));

        return classBuilder.build();
    }

    private MethodSpec generateSelectClause(TypeName selectClass, VariableElement column) {
        final String columnName = column.getSimpleName().toString();
        return MethodSpec.methodBuilder(columnName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(selectClass)
                .addJavadoc("SELECT ... $L, ... ", columnName)
                .addStatement("selectClauses.add($S)", columnName)
                .addStatement("return this")
                .build();
    }

    private MethodSpec generateWhereMethod(String entityClassName) {
        TypeName whereClass = ClassName.get(Constants.GENERATED_CODE_PACKAGE, entityClassName + "_Where");
        return MethodSpec.methodBuilder("where")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(whereClass)
                .addStatement("query.append(selectClauses.toString())")
                .addStatement("return new $T(query)", whereClass)
                .build();
    }
}
