/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2020 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;

/**
 * Makes it easier to generate visitor classes.
 * It will create missing visit methods on the fly,
 * and will ask you to fill in the bodies of the visit methods.
 */
public abstract class VisitorGenerator extends AbstractGenerator {

    private final String pkg;
    private final String visitorClassName;
    private final String returnType;
    private final String argumentType;
    private final boolean createMissingVisitMethods;

    protected VisitorGenerator(SourceRoot sourceRoot, String pkg, String visitorClassName, String returnType, String argumentType, boolean createMissingVisitMethods) {
        super(sourceRoot);
        this.pkg = pkg;
        this.visitorClassName = visitorClassName;
        this.returnType = returnType;
        this.argumentType = argumentType;
        this.createMissingVisitMethods = createMissingVisitMethods;
    }

    @Override
    public final List<CompilationUnit> generate() {
        Log.info("Running %s", () -> this.getClass().getSimpleName());

        try {
            final CompilationUnit compilationUnit = this.sourceRoot.tryToParse(this.pkg, this.visitorClassName + ".java").getResult().get();

            Optional<ClassOrInterfaceDeclaration> visitorClassOptional = compilationUnit.getClassByName(this.visitorClassName);
            if (!visitorClassOptional.isPresent()) {
                visitorClassOptional = compilationUnit.getInterfaceByName(this.visitorClassName);
            }
            final ClassOrInterfaceDeclaration visitorClass = visitorClassOptional.get();

            JavaParserMetaModel.getNodeMetaModels().stream()
                    .filter((baseNodeMetaModel) -> !baseNodeMetaModel.isAbstract())
                    .forEach(node -> this.generateVisitMethodForNode(node, visitorClass, compilationUnit));
            this.after();

            return Collections.singletonList(compilationUnit);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing the file -- IOException (see stack trace for details)", e);
        }
    }

    private void generateVisitMethodForNode(BaseNodeMetaModel node, ClassOrInterfaceDeclaration visitorClass, CompilationUnit compilationUnit) {
        final Optional<MethodDeclaration> existingVisitMethod = visitorClass.getMethods().stream()
                .filter(m -> m.getNameAsString().equals("visit"))
                .filter(m -> m.getParameter(0).getType().toString().equals(node.getTypeName()))
                .findFirst();

        if (existingVisitMethod.isPresent()) {
            this.generateVisitMethodBody(node, existingVisitMethod.get(), compilationUnit);
            this.annotateGenerated(existingVisitMethod.get());
        } else if (this.createMissingVisitMethods) {
            MethodDeclaration newVisitMethod = visitorClass.addMethod("visit")
                    .addParameter(node.getTypeNameGenerified(), "n")
                    .addParameter(this.argumentType, "arg")
                    .setType(this.returnType);
            if (!visitorClass.isInterface()) {
                newVisitMethod
                        .addAnnotation(new MarkerAnnotationExpr(new Name("Override")))
                        .addModifier(PUBLIC);
            }
            this.generateVisitMethodBody(node, newVisitMethod, compilationUnit);
            this.annotateGenerated(newVisitMethod);
        }
    }

    protected abstract void generateVisitMethodBody(BaseNodeMetaModel node, MethodDeclaration visitMethod, CompilationUnit compilationUnit);
}
