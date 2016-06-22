package com.doanduyhai.annotation_processing_demo.codegen;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.truth0.Truth;

import com.doanduyhai.annotation_processing_demo.annotation.Column;
import com.doanduyhai.annotation_processing_demo.entity.TestEntity;
import com.doanduyhai.annotation_processing_demo.processor_utils.AbstractTestProcessor;
import com.google.common.collect.Sets;
import com.google.testing.compile.JavaSourceSubjectFactory;
import com.squareup.javapoet.TypeSpec;

@RunWith(MockitoJUnitRunner.class)
public class WhereClassGeneratorTest extends AbstractTestProcessor {

    @Test
    public void should_generate_where_class() throws Exception {
        //Given
        setExec((typeUtils, filer, messager) -> {
            final WhereClassGenerator generator = new WhereClassGenerator(filer);
            final TypeElement typeElement = elementUtils.getTypeElement(TestEntity.class.getCanonicalName());
            final List<VariableElement> columns = ElementFilter.fieldsIn(elementUtils.getAllMembers(typeElement))
                    .stream()
                    .filter(elm -> elm.getAnnotation(Column.class) != null)
                    .collect(toList());

            final TypeSpec generateWhereClass = generator.generateWhereClass(typeElement, columns);
            assertThat(generateWhereClass.toString().trim().replaceAll("\n", ""))
                    .isEqualTo(readCodeLineFromFile("expected_code/should_generate_where_class.txt"));
        });

        //When
        Truth.ASSERT.about(JavaSourceSubjectFactory.javaSource())
                .that(loadClass(TestEntity.class))
                .processedWith(this)
                .compilesWithoutError();

    }

}