package com.doanduyhai.annotation_processing_demo.processor;

import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.doanduyhai.annotation_processing_demo.annotation.Column;
import com.doanduyhai.annotation_processing_demo.annotation.Entity;
import com.doanduyhai.annotation_processing_demo.codegen.Constants;
import com.doanduyhai.annotation_processing_demo.codegen.DSLClassGenerator;
import com.doanduyhai.annotation_processing_demo.codegen.SelectClassGenerator;
import com.doanduyhai.annotation_processing_demo.codegen.WhereClassGenerator;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@AutoService(Processor.class)
public class DemoProcessor extends AbstractProcessor {


    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private boolean processed = false;
    private SelectClassGenerator selectClassGenerator;
    private WhereClassGenerator whereClassGenerator;
    private DSLClassGenerator dslClassGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        this.selectClassGenerator = new SelectClassGenerator(filer);
        this.whereClassGenerator= new WhereClassGenerator(filer);
        this.dslClassGenerator= new DSLClassGenerator(filer);
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!this.processed) {
            messager.printMessage(MANDATORY_WARNING, "Start processing entities ");
            try {
                final List<TypeSpec> generatedClasses = new ArrayList<>();

                final List<TypeElement> entities = annotations
                        .stream()
                        .filter(elm -> elm.getKind() == ElementKind.ANNOTATION_TYPE
                                && elm.getQualifiedName().toString().equals(Entity.class.getCanonicalName()))
                        .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
                        .map(MoreElements::asType)
                        .collect(Collectors.toList());
                for(TypeElement typeElement: entities) {
                    messager.printMessage(MANDATORY_WARNING, "Processing entity " + typeElement.getSimpleName());
                    final List<VariableElement> columns = ElementFilter
                            .fieldsIn(elementUtils.getAllMembers(typeElement))
                            .stream()
                            .filter(elm -> elm.getAnnotation(Column.class) != null)
                            .collect(Collectors.toList());

                    generatedClasses.add(whereClassGenerator.generateWhereClass(typeElement, columns));
                    generatedClasses.add(selectClassGenerator.generateSelectClass(typeElement, columns));
                    generatedClasses.add(dslClassGenerator.generateDSLClass(typeElement));
                }

                for (TypeSpec generatedClass : generatedClasses) {
                    JavaFile.builder(Constants.GENERATED_CODE_PACKAGE, generatedClass).build().writeTo(filer);
                }

            } catch (Throwable throwable) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                messager.printMessage(Diagnostic.Kind.ERROR, "Error : " + sw.toString());
            }
            this.processed = true;
        }
        return true;
    }
}
