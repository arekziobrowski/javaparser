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

package com.github.javaparser.generator.core.node;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.generator.NodeGenerator;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.javaparser.StaticJavaParser.parseType;
import static com.github.javaparser.ast.Modifier.Keyword.FINAL;
import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static com.github.javaparser.ast.Modifier.createModifierList;
import static com.github.javaparser.utils.CodeGenerationUtils.f;
import static com.github.javaparser.utils.Utils.camelCaseToScreaming;

public class PropertyGenerator extends NodeGenerator {

    private final Map<String, PropertyMetaModel> declaredProperties = new HashMap<>();
    private final Map<String, PropertyMetaModel> derivedProperties = new HashMap<>();

    public PropertyGenerator(SourceRoot sourceRoot) {
        super(sourceRoot);
    }

    @Override
    protected void generateNode(BaseNodeMetaModel nodeMetaModel, CompilationUnit nodeCu, ClassOrInterfaceDeclaration nodeCoid) {
        for (PropertyMetaModel property : nodeMetaModel.getDeclaredPropertyMetaModels()) {
            this.generateGetter(nodeMetaModel, nodeCoid, property);
            this.generateSetter(nodeMetaModel, nodeCoid, property);
        }
        nodeMetaModel.getDerivedPropertyMetaModels().forEach(p -> this.derivedProperties.put(p.getName(), p));
    }

    private void generateSetter(BaseNodeMetaModel nodeMetaModel, ClassOrInterfaceDeclaration nodeCoid, PropertyMetaModel property) {
        final String name = property.getName();
        // Fill body
        final String observableName = camelCaseToScreaming(name.startsWith("is") ? name.substring(2) : name);
        this.declaredProperties.put(observableName, property);

        if (property == JavaParserMetaModel.nodeMetaModel.commentPropertyMetaModel) {
            // Node.comment has a very specific setter that we shouldn't overwrite.
            return;
        }

        final MethodDeclaration setter = new MethodDeclaration(createModifierList(PUBLIC), parseType(property.getContainingNodeMetaModel().getTypeNameGenerified()), property.getSetterMethodName());
        if (property.getContainingNodeMetaModel().hasWildcard()) {
            setter.setType(parseType("T"));
        }
        setter.addAndGetParameter(property.getTypeNameForSetter(), property.getName())
                .addModifier(FINAL);

        final BlockStmt body = setter.getBody().get();
        body.getStatements().clear();

        if (property.isRequired()) {
            Class<?> type = property.getType();
            if (property.isNonEmpty() && property.isSingular()) {
                body.addStatement(f("assertNonEmpty(%s);", name));
            } else if (type != boolean.class && type != int.class) {
                body.addStatement(f("assertNotNull(%s);", name));
            }
        }

        // Only include the cast on `this` if the type is different to the coid we're currently in
        String castValue = (setter.getTypeAsString().equals(nodeCoid.getNameAsString())) ? "" : "(" + setter.getTypeAsString() + ")";
        body.addStatement(f(
                "if (%s == this.%s) { return %s this; }",
                name,
                name,
                castValue
        ));

        body.addStatement(f("this.notifyPropertyChange(ObservableProperty.%s, this.%s, %s);", observableName, name, name));
        if (property.isNode()) {
            body.addStatement(f("if (this.%s != null) { this.%s.setParentNode(null); }", name, name));
        }
        body.addStatement(f("this.%s = %s;", name, name));
        if (property.isNode()) {
            body.addStatement(f("this.setAsParentNodeOf(%s);", name));
        }
        if (property.getContainingNodeMetaModel().hasWildcard()) {
            body.addStatement(f("return (T) this;"));
        } else {
            body.addStatement(f("return this;"));
        }
        this.addOrReplaceWhenSameSignature(nodeCoid, setter);
        if (property.getContainingNodeMetaModel().hasWildcard()) {
            this.annotateSuppressWarnings(setter);
        }
    }

    private void generateGetter(BaseNodeMetaModel nodeMetaModel, ClassOrInterfaceDeclaration nodeCoid, PropertyMetaModel property) {
        final MethodDeclaration getter = new MethodDeclaration(createModifierList(PUBLIC), parseType(property.getTypeNameForGetter()), property.getGetterMethodName());
        final BlockStmt body = getter.getBody().get();
        body.getStatements().clear();
        if (property.isOptional()) {
            body.addStatement(f("return Optional.ofNullable(this.%s);", property.getName()));
        } else {
            body.addStatement(f("return this.%s;", property.getName()));
        }
        this.addOrReplaceWhenSameSignature(nodeCoid, getter);
    }

    private void generateObservableProperty(EnumDeclaration observablePropertyEnum, PropertyMetaModel property, boolean derived) {
        boolean isAttribute = !Node.class.isAssignableFrom(property.getType());
        String name = property.getName();
        String constantName = camelCaseToScreaming(name.startsWith("is") ? name.substring(2) : name);
        EnumConstantDeclaration enumConstantDeclaration = observablePropertyEnum.addEnumConstant(constantName);
        if (isAttribute) {
            enumConstantDeclaration.addArgument("Type.SINGLE_ATTRIBUTE");
        } else {
            if (property.isNodeList()) {
                enumConstantDeclaration.addArgument("Type.MULTIPLE_REFERENCE");
            } else {
                enumConstantDeclaration.addArgument("Type.SINGLE_REFERENCE");
            }
        }
        if (derived) {
            enumConstantDeclaration.addArgument("true");
        }
    }

    @Override
    protected void after() {

        try {
            CompilationUnit observablePropertyCu = this.sourceRoot.tryToParse("com.github.javaparser.ast.observer", "ObservableProperty.java").getResult().get();
            EnumDeclaration observablePropertyEnum = observablePropertyCu.getEnumByName("ObservableProperty").get();
            observablePropertyEnum.getEntries().clear();
            List<String> observablePropertyNames = new LinkedList<>(this.declaredProperties.keySet());
            observablePropertyNames.sort(String::compareTo);
            for (String propName : observablePropertyNames) {
                this.generateObservableProperty(observablePropertyEnum, this.declaredProperties.get(propName), false);
            }
            List<String> derivedPropertyNames = new LinkedList<>(this.derivedProperties.keySet());
            derivedPropertyNames.sort(String::compareTo);
            for (String propName : derivedPropertyNames) {
                this.generateObservableProperty(observablePropertyEnum, this.derivedProperties.get(propName), true);
            }
            observablePropertyEnum.addEnumConstant("RANGE");
            observablePropertyEnum.addEnumConstant("COMMENTED_NODE");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing the file -- IOException (see stack trace for details)", e);
        }
    }
}
