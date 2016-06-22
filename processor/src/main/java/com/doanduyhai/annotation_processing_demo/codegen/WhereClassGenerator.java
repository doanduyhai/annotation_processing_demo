package com.doanduyhai.annotation_processing_demo.codegen;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.doanduyhai.annotation_processing_demo.dsl.AbstractWhere;
import com.squareup.javapoet.*;

public class WhereClassGenerator {

    private static final ClassName ABSTRACT_WHERE = ClassName.get(AbstractWhere.class);
    private final Filer filer;

    public WhereClassGenerator(Filer filer) {
        this.filer = filer;
    }

    public TypeSpec generateWhereClass(TypeElement entity, List<VariableElement> columns) throws IOException {
        final String entityClassName = entity.getSimpleName().toString();
        final String whereClassName = entityClassName + "_Where";
        final ClassName whereClass = ClassName.get(Constants.GENERATED_CODE_PACKAGE, whereClassName);
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(whereClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ABSTRACT_WHERE)
                .addMethod(MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(StringBuilder.class), "query")
                        .addStatement("super(query)")
                        .build())
                .addJavadoc("This is the impl WHERE class for entity $S", entityClassName);

        // Add xxx_Eq() methods
        columns.forEach(column -> classBuilder.addMethod(generateWhereClause(whereClass, column)));

        return classBuilder.build();
    }

    private MethodSpec generateWhereClause(ClassName whereClass, VariableElement column) {
        final String columnName = column.getSimpleName().toString();
        return MethodSpec.methodBuilder(columnName + "_Eq")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(whereClass)
                .addJavadoc("WHERE ... $L = ?", columnName)
                .addParameter(TypeName.get(column.asType()), "input", Modifier.FINAL)
                .addStatement("whereConditions.add($S + input.toString())", columnName)
                .addStatement("return this")
                .build();
    }
}
