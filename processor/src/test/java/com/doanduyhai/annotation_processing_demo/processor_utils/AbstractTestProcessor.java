/*
 * Copyright (C) 2012-2016 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doanduyhai.annotation_processing_demo.processor_utils;

import static com.google.common.truth.Truth.assert_;
import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import javax.tools.JavaFileObject;

import com.doanduyhai.annotation_processing_demo.annotation.Entity;
import com.doanduyhai.annotation_processing_demo.codegen.Constants;
import com.google.common.collect.Sets;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public abstract class AbstractTestProcessor extends AbstractProcessor {

    protected Elements elementUtils;
    protected Types typeUtils;
    protected Messager messager;
    protected Filer filer;
    protected AptAssertOK exec;
    protected RoundEnvironment env;


    public void setExec(AptAssertOK exec) {
        this.exec = exec;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(Entity.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            try {
                env = roundEnv;
                exec.assertOk(typeUtils, filer, messager);
            } catch (Exception ex) {
                ex.printStackTrace();
                messager.printMessage(ERROR, ex.getMessage());
            }
        }
        return true;
    }


    protected void launchTest(Class<?> testClass) {
        assert_().about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(loadClass(testClass)))
                .processedWith(this)
                .compilesWithoutError();

    }


    protected void failTestWithMessage(String message, Class<?> testClass) {
        assert_().about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(loadClass(testClass)))
                .processedWith(this)
                .failsToCompile()
                .withErrorContaining(message);
    }

    protected JavaFileObject loadClass(Class<?> clazz) {
        final String resourceName = clazz.getPackage().getName().replace(".", "/") + '/' + clazz.getSimpleName() + ".java";
        return JavaFileObjects.forResource(resourceName);
    }

    protected String readCodeLineFromFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        StringBuilder result = new StringBuilder("");

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line);
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString().trim().replaceAll("\n", "");
    }

    protected String readCodeBlockFromFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] encoded = new byte[0];
        try {
            File file = new File(classLoader.getResource(filename).getFile());
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, StandardCharsets.UTF_8).trim();
    }

    protected String buildSource(TypeSpec typeSpec) {
        StringBuilder builder = new StringBuilder();
        JavaFile file = JavaFile.builder(Constants.GENERATED_CODE_PACKAGE, typeSpec)
                .skipJavaLangImports(false)
                .build();

        try {
            file.writeTo(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString().trim();

    }
}
