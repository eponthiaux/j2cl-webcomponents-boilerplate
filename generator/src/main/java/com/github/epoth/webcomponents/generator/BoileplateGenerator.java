package com.github.epoth.webcomponents.generator;

import com.github.epoth.boilerplate.annotations.WebComponent;
import com.github.epoth.webcomponents.generator.observed.WebComponentObservedBinderGenerator;
import com.google.common.annotations.GwtIncompatible;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.github.epoth.webcomponents.generator.ClassNameUtils.lowerCaseClassName;
import static com.github.epoth.webcomponents.generator.ClassNameUtils.packagePath;


/**
 * Copyright 2022 Eric Ponthiaux -/- ponthiaux.eric@gmail.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@GwtIncompatible
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BoileplateGenerator extends AbstractProcessor {

    private static final String DEFINE_COMPONENT_PATTERN = "elemental2.dom.DomGlobal.customElements.define($S,$L.class)";
    private static final String HTML_TEMPLATE_ELEMENT_CREATION = "elemental2.dom.HTMLTemplateElement $L_template = (elemental2.dom.HTMLTemplateElement) elemental2.dom.DomGlobal.document.createElement(\"template\")";
    private static final String HTML_TEMPLATE_ELEMENT_SET_INNER = "$L_template.innerHTML=$S";
    private static final String HTML_TEMPLATE_BIND_TO_HEAD = "elemental2.dom.DomGlobal.document.head.append($L_template)";
    private static final String HTML_TEMPLATE_SET_TO_CLASS = "$L.__Template = $L_template";

    private HTMLTemplateParser templateParser;

    private WebComponentInitializerGenerator componentInitializerGenerator;

    private WebComponentObservedBinderGenerator webComponentObservedBinderGenerator;

    private ArrayList<ComponentDefinition> components = new ArrayList<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        return Collections.singleton("com.github.epoth.boilerplate.annotations.WebComponent");

    }

    @Override
    public boolean process(

            Set<? extends TypeElement> annotations,

            RoundEnvironment roundEnv

    ) {

        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(WebComponent.class);

        for (Element element : classes) {

            processElement(element);

        }

        if (classes.size() == 0) {

            return false;

        }

        /* */

        templateParser = new HTMLTemplateParser();

        componentInitializerGenerator = new WebComponentInitializerGenerator();

        webComponentObservedBinderGenerator = new WebComponentObservedBinderGenerator();


        /* */

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "* generating declarations for " + classes.size() + " components ");

        /* */

        StringBuilder nativeBootstrapBuilder = new StringBuilder();

        nativeBootstrapBuilder.append(" setTimeout( function() { ");
        nativeBootstrapBuilder.append(" var ep = Boot.$create__();");
        nativeBootstrapBuilder.append(" ep.m_onLoad__();");
        nativeBootstrapBuilder.append(" }, 0); ");

        /* */

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("Boot").addModifiers(Modifier.PUBLIC);

        /* */

        CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder();

        /* */

        CodeBlock.Builder defineComponentCodeBlockBuilder = CodeBlock.builder();

        /* */

        for (ComponentDefinition component : components) {

            /* */

            defineComponentCodeBlockBuilder.addStatement(DEFINE_COMPONENT_PATTERN, component.getTagName(), component.getClassName());

            try {

                componentInitializerGenerator.generate(

                        processingEnv,

                        component,

                        staticCodeBlockBuilder

                );

            } catch (IOException ioException) {

                throw new RuntimeException(ioException);

            }

            try {

                webComponentObservedBinderGenerator.generate(

                        processingEnv,

                        component,

                        staticCodeBlockBuilder

                );

            } catch (IOException ioException) {

                throw new RuntimeException(ioException);

            }

            retrieveTemplate(staticCodeBlockBuilder, component);

            /* */

        }

        classBuilder.addStaticBlock(staticCodeBlockBuilder.build());

        /* */

        MethodSpec.Builder onLoadMethodBuilder = MethodSpec.methodBuilder("onLoad");

        onLoadMethodBuilder.addModifiers(Modifier.PUBLIC);

        onLoadMethodBuilder.addCode(defineComponentCodeBlockBuilder.build());

        /* */

        classBuilder.addMethod(onLoadMethodBuilder.build());

        JavaFile javaFile = JavaFile.builder("com.github.epoth.boilerplate", classBuilder.build()).build();

        try {

            javaFile.writeTo(processingEnv.getFiler());

            FileObject jsBoostrap = processingEnv.getFiler().createResource(

                    StandardLocation.SOURCE_OUTPUT,

                    "", "com/github/epoth/boilerplate/Boot.native.js"

            );

            Writer writer = jsBoostrap.openWriter();

            writer.write(nativeBootstrapBuilder.toString());

            writer.flush();

            writer.close();

        } catch (IOException ioException) {

            throw new UncheckedIOException(ioException);

        }

        /* */

        return true;

    }

    private void retrieveTemplate(CodeBlock.Builder codeBuilder, ComponentDefinition component) {

        if (component.getTemplateUrl() != null && !component.getTemplateUrl().trim().equals("")) {

            StringBuilder templatePathBuilder = new StringBuilder();

            String simpleClassName = lowerCaseClassName(component.getClassName());
            String packagePath = packagePath(component.getClassName());

            templatePathBuilder.append(packagePath).append("/").append(component.getTemplateUrl());

            /* */

            String templateContents = null;

            try {

                templateContents = getStringContentsOfPath(processingEnv.getFiler(), templatePathBuilder.toString()).toString();

                /* */

                templateContents = parseTemplate(

                        component.getClassElement(),

                        codeBuilder,

                        component.getClassName(),

                        templateContents

                );

                /* */

                codeBuilder.addStatement(HTML_TEMPLATE_ELEMENT_CREATION, simpleClassName);
                codeBuilder.addStatement(HTML_TEMPLATE_ELEMENT_SET_INNER, simpleClassName, templateContents);
                codeBuilder.addStatement(HTML_TEMPLATE_BIND_TO_HEAD, simpleClassName);
                codeBuilder.addStatement(HTML_TEMPLATE_SET_TO_CLASS, component.getClassName(), simpleClassName);

            } catch (IOException ioException) {

                throw new RuntimeException(ioException);

            }

        }

    }

    private String parseTemplate(

            Element classElement,

            CodeBlock.Builder codeBuilder,

            String className,

            String templateContents

    ) throws IOException {

        HTMLTemplateParser.TemplateParserResult result = templateParser.parse(processingEnv, templateContents);

        WebComponentBinderGenerator generator = new WebComponentBinderGenerator();

        generator.generate(

                processingEnv,

                classElement,

                className,

                result.bindingList,

                codeBuilder

        );

        return result.parserOutput;

    }

    private void processElement(Element element) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "+ processing - " + processingEnv.getElementUtils().getPackageOf(element));

        WebComponent component = element.getAnnotation(WebComponent.class);

        if (component.tagName() != null) {

            ComponentDefinition comp = new ComponentDefinition();

            comp.setClassElement(element);
            comp.setMode(component.mode());
            comp.setTagName(component.tagName());
            comp.setTemplateUrl(component.template());
            comp.setClassName(element.toString());

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "+ extracting - " + comp.toString());

            components.add(comp);

        }

    }

    private CharSequence getStringContentsOfPath(Filer filer, String path) throws IOException {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "+ path - " + path);

        try {
            FileObject resource = filer.getResource(StandardLocation.SOURCE_PATH, "", path);
            if (resource != null && new File(resource.getName()).exists()) {
                return resource.getCharContent(false);
            }
        } catch (IOException e) {
            //ignore, look in the next entry
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/" + path)) {
            if (inputStream != null) {
                final char[] buffer = new char[1024];
                final StringBuilder out = new StringBuilder();
                try (Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    while (true) {
                        int rsz = in.read(buffer, 0, buffer.length);
                        if (rsz < 0) break;
                        out.append(buffer, 0, rsz);
                    }
                }
                return out.toString();
            }
            throw new IllegalStateException("Failed to find resource " + path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource " + path, e);
        }
    }


}
