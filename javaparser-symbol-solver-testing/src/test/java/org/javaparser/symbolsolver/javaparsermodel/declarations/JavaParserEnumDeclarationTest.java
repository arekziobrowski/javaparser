/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2019 The JavaParser Team.
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

package org.javaparser.symbolsolver.javaparsermodel.declarations;

import org.javaparser.JavaParser;
import org.javaparser.ParseStart;
import org.javaparser.ParserConfiguration;
import org.javaparser.StringProvider;
import org.javaparser.ast.AccessSpecifier;
import org.javaparser.ast.CompilationUnit;
import org.javaparser.ast.body.FieldDeclaration;
import org.javaparser.resolution.MethodUsage;
import org.javaparser.resolution.UnsolvedSymbolException;
import org.javaparser.resolution.declarations.*;
import org.javaparser.resolution.types.ResolvedPrimitiveType;
import org.javaparser.resolution.types.ResolvedReferenceType;
import org.javaparser.symbolsolver.AbstractSymbolResolutionTest;
import org.javaparser.symbolsolver.JavaSymbolSolver;
import org.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.javaparser.symbolsolver.reflectionmodel.ReflectionFactory;
import org.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import org.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import org.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.javaparser.symbolsolver.utils.LeanParserConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaParserEnumDeclarationTest extends AbstractSymbolResolutionTest {

    private TypeSolver typeSolver;

    @BeforeEach
    void setup() {
        Path srcNewCode = adaptPath("src/test/test_sourcecode/javaparser_new_src/javaparser-core");
        CombinedTypeSolver combinedtypeSolver = new CombinedTypeSolver();
        combinedtypeSolver.add(new ReflectionTypeSolver());
        combinedtypeSolver.add(new JavaParserTypeSolver(srcNewCode, new LeanParserConfiguration()));
        combinedtypeSolver.add(new JavaParserTypeSolver(adaptPath("src/test/test_sourcecode/javaparser_new_src/javaparser-generated-sources"), new LeanParserConfiguration()));
        typeSolver = combinedtypeSolver;
    }

    ///
    /// Test misc
    ///

    @Test
    void testIsClass() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(false, modifier.isClass());
    }

    @Test
    void testIsInterface() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(false, modifier.isInterface());
    }

    @Test
    void testIsEnum() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(true, modifier.isEnum());
    }

    @Test
    void testIsTypeVariable() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(false, modifier.isTypeParameter());
    }

    @Test
    void testIsType() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(true, modifier.isType());
    }

    @Test
    void testAsType() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(modifier, modifier.asType());
    }

    @Test
    void testAsClass() {
        assertThrows(UnsupportedOperationException.class, () -> {
            JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        modifier.asClass();
    });
}

    @Test
    void testAsInterface() {
        assertThrows(UnsupportedOperationException.class, () -> {
            JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        modifier.asInterface();
    });
}

    @Test
    void testAsEnum() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals(modifier, modifier.asEnum());
    }

    @Test
    void testGetPackageName() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals("org.javaparser.ast", modifier.getPackageName());
    }

    @Test
    void testGetClassName() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals("Modifier", modifier.getClassName());
    }

    @Test
    void testGetQualifiedName() {
        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
        assertEquals("org.javaparser.ast.Modifier", modifier.getQualifiedName());
    }

    ///
    /// Test ancestors
    ///

    @Test
    void getGetAncestors() {
        Path src = adaptPath("src/test/resources/enums");
        CombinedTypeSolver combinedtypeSolver = new CombinedTypeSolver();
        combinedtypeSolver.add(new ReflectionTypeSolver());
        combinedtypeSolver.add(new JavaParserTypeSolver(src, new LeanParserConfiguration()));

        JavaParserEnumDeclaration enum1 = (JavaParserEnumDeclaration) combinedtypeSolver.solveType("EnumWithAncestor");
        List<ResolvedReferenceType> ancestors = enum1.getAncestors();
        assertEquals(2, ancestors.size());
        assertEquals("java.lang.Enum", ancestors.get(0).getQualifiedName());
        assertEquals("java.lang.Cloneable", ancestors.get(1).getQualifiedName());
    }

//    @Test
//    public void testGetSuperclassWithoutTypeParameters() {
//        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
//        assertEquals("org.javaparser.ast.Node", compilationUnit.getSuperClass().getQualifiedName());
//    }

    @Test
    void testGetSuperclassWithTypeParameters() {
        JavaParserClassDeclaration compilationUnit = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals("org.javaparser.ast.body.BodyDeclaration", compilationUnit.getSuperClass().getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", compilationUnit.getSuperClass().typeParametersMap().getValueBySignature("org.javaparser.ast.body.BodyDeclaration.T").get().asReferenceType().getQualifiedName());
    }

    @Test
    void testGetAllSuperclassesWithoutTypeParameters() {
        JavaParserClassDeclaration cu = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.CompilationUnit");
        assertEquals(ImmutableSet.of("org.javaparser.ast.Node", "java.lang.Object"), cu.getAllSuperClasses().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
    }

    @Test
    void testGetAllSuperclassesWithTypeParameters() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals(3, constructorDeclaration.getAllSuperClasses().size());

        ResolvedReferenceType ancestor;

        ancestor = constructorDeclaration.getAllSuperClasses().get(0);
        assertEquals("org.javaparser.ast.body.BodyDeclaration", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.body.BodyDeclaration.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllSuperClasses().get(1);
        assertEquals("org.javaparser.ast.Node", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAllSuperClasses().get(2);
        assertEquals("java.lang.Object", ancestor.getQualifiedName());
    }

//    @Test
//    public void testGetInterfacesWithoutParameters() {
//        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
//        assertEquals(ImmutableSet.of(), compilationUnit.getInterfaces().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
//
//        JavaParserClassDeclaration coid = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ClassOrInterfaceDeclaration");
//        assertEquals(ImmutableSet.of("org.javaparser.ast.DocumentableNode"), coid.getInterfaces().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
//    }

    @Test
    void testGetInterfacesWithParameters() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals(7, constructorDeclaration.getInterfaces().size());

        ResolvedReferenceType interfaze;

        interfaze = constructorDeclaration.getInterfaces().get(0);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithJavaDoc", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithJavaDoc.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(1);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithDeclaration", interfaze.getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(2);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithName", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithName.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(3);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithModifiers", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithModifiers.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(4);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithParameters", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithParameters.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(5);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithThrowable.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getInterfaces().get(6);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithBlockStmt", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithBlockStmt.T").get().asReferenceType().getQualifiedName());
    }

//    @Test
//    public void testGetAllInterfacesWithoutParameters() {
//        JavaParserEnumDeclaration modifier = (JavaParserEnumDeclaration) typeSolver.solveType("org.javaparser.ast.Modifier");
//        assertEquals(ImmutableSet.of("java.lang.Cloneable"), compilationUnit.getAllInterfaces().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
//
//        JavaParserClassDeclaration coid = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ClassOrInterfaceDeclaration");
//        assertEquals(ImmutableSet.of("java.lang.Cloneable", "org.javaparser.ast.NamedNode", "org.javaparser.ast.body.AnnotableNode", "org.javaparser.ast.DocumentableNode"), coid.getAllInterfaces().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
//    }

    @Test
    void testGetAllInterfacesWithParameters() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals(9, constructorDeclaration.getAllInterfaces().size());

        ResolvedReferenceType interfaze;

        interfaze = constructorDeclaration.getAllInterfaces().get(0);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithJavaDoc", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithJavaDoc.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(1);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithDeclaration", interfaze.getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(2);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithName", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithName.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(3);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithModifiers", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithModifiers.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(4);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithParameters", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithParameters.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(5);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithThrowable.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(6);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithBlockStmt", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithBlockStmt.T").get().asReferenceType().getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(7);
        assertEquals("java.lang.Cloneable", interfaze.getQualifiedName());

        interfaze = constructorDeclaration.getAllInterfaces().get(8);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithAnnotations", interfaze.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", interfaze.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithAnnotations.T").get().asReferenceType().getQualifiedName());
    }

    @Test
    void testGetAncestorsWithTypeParameters() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals(8, constructorDeclaration.getAncestors().size());

        ResolvedReferenceType ancestor;

        ancestor = constructorDeclaration.getAncestors().get(0);
        assertEquals("org.javaparser.ast.body.BodyDeclaration", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.body.BodyDeclaration.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(1);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithJavaDoc", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithJavaDoc.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(2);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithDeclaration", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(3);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithName", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithName.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(4);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithModifiers", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithModifiers.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(5);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithParameters", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithParameters.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(6);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithThrowable.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAncestors().get(7);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithBlockStmt", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithBlockStmt.T").get().asReferenceType().getQualifiedName());
    }

    @Test
    void testGetAllAncestorsWithoutTypeParameters() {
        JavaParserClassDeclaration cu = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.CompilationUnit");
        assertEquals(ImmutableSet.of("java.lang.Cloneable", "org.javaparser.ast.Node", "java.lang.Object"), cu.getAllAncestors().stream().map(i -> i.getQualifiedName()).collect(Collectors.toSet()));
    }

    @Test
    void testGetAllAncestorsWithTypeParameters() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        assertEquals(12, constructorDeclaration.getAllAncestors().size());

        ResolvedReferenceType ancestor;

        ancestor = constructorDeclaration.getAllAncestors().get(0);
        assertEquals("org.javaparser.ast.body.BodyDeclaration", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.body.BodyDeclaration.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(1);
        assertEquals("org.javaparser.ast.Node", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(2);
        assertEquals("java.lang.Cloneable", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(3);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithAnnotations", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithAnnotations.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(4);
        assertEquals("java.lang.Object", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(5);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithJavaDoc", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithJavaDoc.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(6);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithDeclaration", ancestor.getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(7);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithName", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithName.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(8);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithModifiers", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithModifiers.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(9);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithParameters", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithParameters.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(10);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithThrowable.T").get().asReferenceType().getQualifiedName());

        ancestor = constructorDeclaration.getAllAncestors().get(11);
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithBlockStmt", ancestor.getQualifiedName());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration", ancestor.typeParametersMap().getValueBySignature("org.javaparser.ast.nodeTypes.NodeWithBlockStmt.T").get().asReferenceType().getQualifiedName());
    }

    ///
    /// Test fields
    ///

    @Test
    void testGetFieldForExistingField() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        ResolvedFieldDeclaration fieldDeclaration;

        // declared field
        fieldDeclaration = constructorDeclaration.getField("modifiers");
        assertEquals("modifiers", fieldDeclaration.getName());
        assertEquals("java.util.EnumSet", fieldDeclaration.getType().asReferenceType().getQualifiedName());
        assertEquals(AccessSpecifier.PRIVATE, fieldDeclaration.accessSpecifier());
        assertFalse(fieldDeclaration.isStatic());

        // inherited field
        fieldDeclaration = constructorDeclaration.getField("annotations");
        assertEquals("annotations", fieldDeclaration.getName());
        assertEquals("java.util.List", fieldDeclaration.getType().asReferenceType().getQualifiedName());
        assertEquals(AccessSpecifier.PRIVATE, fieldDeclaration.accessSpecifier());
    }

    @Test
    void testGetFieldForUnexistingField() {
        assertThrows(UnsolvedSymbolException.class, () -> {
            JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
        constructorDeclaration.getField("unexisting");
    });

}

    @Test
    void testGetAllFields() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        List<ResolvedFieldDeclaration> allFields = constructorDeclaration.getAllFields();
        assertEquals(16, allFields.size());

        ResolvedFieldDeclaration fieldDeclaration;

        fieldDeclaration = allFields.get(0);
        assertEquals("modifiers", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(1);
        assertEquals("typeParameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(2);
        assertEquals("name", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(3);
        assertEquals("parameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(4);
        assertEquals("throws_", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(5);
        assertEquals("body", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(6);
        assertEquals("annotations", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(7);
        assertEquals("NODE_BY_BEGIN_POSITION", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(8);
        assertEquals("range", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(9);
        assertEquals("parentNode", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(10);
        assertEquals("childrenNodes", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(11);
        assertEquals("orphanComments", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(12);
        assertEquals("userData", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(13);
        assertEquals("comment", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(14);
        assertEquals("ABSOLUTE_BEGIN_LINE", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(15);
        assertEquals("ABSOLUTE_END_LINE", fieldDeclaration.getName());
    }

    @Test
    void testGetAllStaticFields() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        List<ResolvedFieldDeclaration> allFields = constructorDeclaration.getAllStaticFields();
        assertEquals(3, allFields.size());

        ResolvedFieldDeclaration fieldDeclaration;

        fieldDeclaration = allFields.get(0);
        assertEquals("NODE_BY_BEGIN_POSITION", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(1);
        assertEquals("ABSOLUTE_BEGIN_LINE", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(2);
        assertEquals("ABSOLUTE_END_LINE", fieldDeclaration.getName());
    }

    @Test
    void testGetAllNonStaticFields() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        List<ResolvedFieldDeclaration> allFields = constructorDeclaration.getAllNonStaticFields();
        assertEquals(13, allFields.size());

        ResolvedFieldDeclaration fieldDeclaration;

        fieldDeclaration = allFields.get(0);
        assertEquals("modifiers", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(1);
        assertEquals("typeParameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(2);
        assertEquals("name", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(3);
        assertEquals("parameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(4);
        assertEquals("throws_", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(5);
        assertEquals("body", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(6);
        assertEquals("annotations", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(7);
        assertEquals("range", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(8);
        assertEquals("parentNode", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(9);
        assertEquals("childrenNodes", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(10);
        assertEquals("orphanComments", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(11);
        assertEquals("userData", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(12);
        assertEquals("comment", fieldDeclaration.getName());
    }

    @Test
    void testGetDeclaredFields() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        List<ResolvedFieldDeclaration> allFields = constructorDeclaration.getDeclaredFields();
        assertEquals(6, allFields.size());

        ResolvedFieldDeclaration fieldDeclaration;

        fieldDeclaration = allFields.get(0);
        assertEquals("modifiers", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(1);
        assertEquals("typeParameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(2);
        assertEquals("name", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(3);
        assertEquals("parameters", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(4);
        assertEquals("throws_", fieldDeclaration.getName());

        fieldDeclaration = allFields.get(5);
        assertEquals("body", fieldDeclaration.getName());
    }

    ///
    /// Test methods
    ///

    @Test
    void testGetDeclaredMethods() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        Set<ResolvedMethodDeclaration> allMethods = constructorDeclaration.getDeclaredMethods();
        assertEquals(20, allMethods.size());

        List<ResolvedMethodDeclaration> sortedMethods = allMethods.stream()
                .sorted(Comparator.comparing(ResolvedMethodLikeDeclaration::getQualifiedSignature))
                .collect(Collectors.toList());

        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.accept(org.javaparser.ast.visitor.GenericVisitor<R, A>, A)", sortedMethods.get(0).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.accept(org.javaparser.ast.visitor.VoidVisitor<A>, A)", sortedMethods.get(1).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getBody()", sortedMethods.get(2).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString()", sortedMethods.get(3).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString(boolean, boolean)", sortedMethods.get(4).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString(boolean, boolean, boolean)", sortedMethods.get(5).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getJavaDoc()", sortedMethods.get(6).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getModifiers()", sortedMethods.get(7).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getName()", sortedMethods.get(8).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getNameExpr()", sortedMethods.get(9).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getParameters()", sortedMethods.get(10).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getThrows()", sortedMethods.get(11).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.getTypeParameters()", sortedMethods.get(12).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setBody(org.javaparser.ast.stmt.BlockStmt)", sortedMethods.get(13).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setModifiers(java.util.EnumSet<org.javaparser.ast.Modifier>)", sortedMethods.get(14).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setName(java.lang.String)", sortedMethods.get(15).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setNameExpr(org.javaparser.ast.expr.NameExpr)", sortedMethods.get(16).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setParameters(java.util.List<org.javaparser.ast.body.Parameter>)", sortedMethods.get(17).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setThrows(java.util.List<org.javaparser.ast.type.ReferenceType>)", sortedMethods.get(18).getQualifiedSignature());
        assertEquals("org.javaparser.ast.body.ConstructorDeclaration.setTypeParameters(java.util.List<org.javaparser.ast.type.TypeParameter>)", sortedMethods.get(19).getQualifiedSignature());
    }

    @Test
    void testGetAllMethods() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        Set<MethodUsage> allMethods = constructorDeclaration.getAllMethods();

        List<MethodUsage> sortedMethods = allMethods.stream()
                .sorted(Comparator.comparing(MethodUsage::getQualifiedSignature))
                .collect(Collectors.toList());

        List<String> signatures = sortedMethods.stream().map(m -> m.getQualifiedSignature()).collect(Collectors.toList());

        assertEquals(ImmutableList.of("org.javaparser.ast.Node.addOrphanComment(org.javaparser.ast.comments.Comment)",
                "org.javaparser.ast.Node.clone()",
                "org.javaparser.ast.Node.contains(org.javaparser.ast.Node)",
                "org.javaparser.ast.Node.equals(java.lang.Object)",
                "org.javaparser.ast.Node.getAllContainedComments()",
                "org.javaparser.ast.Node.getBegin()",
                "org.javaparser.ast.Node.getChildrenNodes()",
                "org.javaparser.ast.Node.getComment()",
                "org.javaparser.ast.Node.getEnd()",
                "org.javaparser.ast.Node.getNodesByType(java.lang.Class<N>)",
                "org.javaparser.ast.Node.getOrphanComments()",
                "org.javaparser.ast.Node.getParentNode()",
                "org.javaparser.ast.Node.getParentNodeOfType(java.lang.Class<T>)",
                "org.javaparser.ast.Node.getRange()",
                "org.javaparser.ast.Node.getUserData(org.javaparser.ast.UserDataKey<M>)",
                "org.javaparser.ast.Node.hasComment()",
                "org.javaparser.ast.Node.hashCode()",
                "org.javaparser.ast.Node.isPositionedAfter(org.javaparser.Position)",
                "org.javaparser.ast.Node.isPositionedBefore(org.javaparser.Position)",
                "org.javaparser.ast.Node.setAsParentNodeOf(org.javaparser.ast.Node)",
                "org.javaparser.ast.Node.setAsParentNodeOf(java.util.List<? extends org.javaparser.ast.Node>)",
                "org.javaparser.ast.Node.setBegin(org.javaparser.Position)",
                "org.javaparser.ast.Node.setBlockComment(java.lang.String)",
                "org.javaparser.ast.Node.setComment(org.javaparser.ast.comments.Comment)",
                "org.javaparser.ast.Node.setEnd(org.javaparser.Position)",
                "org.javaparser.ast.Node.setLineComment(java.lang.String)",
                "org.javaparser.ast.Node.setParentNode(org.javaparser.ast.Node)",
                "org.javaparser.ast.Node.setRange(org.javaparser.Range)",
                "org.javaparser.ast.Node.setUserData(org.javaparser.ast.UserDataKey<M>, M)",
                "org.javaparser.ast.Node.toString()",
                "org.javaparser.ast.Node.toStringWithoutComments()",
                "org.javaparser.ast.Node.tryAddImportToParentCompilationUnit(java.lang.Class<?>)",
                "org.javaparser.ast.body.BodyDeclaration.getAnnotations()",
                "org.javaparser.ast.body.BodyDeclaration.setAnnotations(java.util.List<org.javaparser.ast.expr.AnnotationExpr>)",
                "org.javaparser.ast.body.ConstructorDeclaration.accept(org.javaparser.ast.visitor.GenericVisitor<R, A>, A)",
                "org.javaparser.ast.body.ConstructorDeclaration.accept(org.javaparser.ast.visitor.VoidVisitor<A>, A)",
                "org.javaparser.ast.body.ConstructorDeclaration.getBody()",
                "org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString()",
                "org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString(boolean, boolean)",
                "org.javaparser.ast.body.ConstructorDeclaration.getDeclarationAsString(boolean, boolean, boolean)",
                "org.javaparser.ast.body.ConstructorDeclaration.getJavaDoc()",
                "org.javaparser.ast.body.ConstructorDeclaration.getModifiers()",
                "org.javaparser.ast.body.ConstructorDeclaration.getName()",
                "org.javaparser.ast.body.ConstructorDeclaration.getNameExpr()",
                "org.javaparser.ast.body.ConstructorDeclaration.getParameters()",
                "org.javaparser.ast.body.ConstructorDeclaration.getThrows()",
                "org.javaparser.ast.body.ConstructorDeclaration.getTypeParameters()",
                "org.javaparser.ast.body.ConstructorDeclaration.setBody(org.javaparser.ast.stmt.BlockStmt)",
                "org.javaparser.ast.body.ConstructorDeclaration.setModifiers(java.util.EnumSet<org.javaparser.ast.Modifier>)",
                "org.javaparser.ast.body.ConstructorDeclaration.setName(java.lang.String)",
                "org.javaparser.ast.body.ConstructorDeclaration.setNameExpr(org.javaparser.ast.expr.NameExpr)",
                "org.javaparser.ast.body.ConstructorDeclaration.setParameters(java.util.List<org.javaparser.ast.body.Parameter>)",
                "org.javaparser.ast.body.ConstructorDeclaration.setThrows(java.util.List<org.javaparser.ast.type.ReferenceType>)",
                "org.javaparser.ast.body.ConstructorDeclaration.setTypeParameters(java.util.List<org.javaparser.ast.type.TypeParameter>)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addAnnotation(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addMarkerAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addMarkerAnnotation(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addSingleMemberAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.addSingleMemberAnnotation(java.lang.String, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.getAnnotationByClass(java.lang.Class<? extends java.lang.annotation.Annotation>)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.getAnnotationByName(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.isAnnotationPresent(java.lang.Class<? extends java.lang.annotation.Annotation>)",
                "org.javaparser.ast.nodeTypes.NodeWithAnnotations.isAnnotationPresent(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithBlockStmt.createBody()",
                "org.javaparser.ast.nodeTypes.NodeWithJavaDoc.setJavaDocComment(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.addModifier(org.javaparser.ast.Modifier...)",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isAbstract()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isFinal()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isNative()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isPrivate()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isProtected()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isPublic()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isStatic()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isStrictfp()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isSynchronized()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isTransient()",
                "org.javaparser.ast.nodeTypes.NodeWithModifiers.isVolatile()",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addAndGetParameter(org.javaparser.ast.body.Parameter)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addAndGetParameter(org.javaparser.ast.type.Type, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addAndGetParameter(java.lang.Class<?>, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addAndGetParameter(java.lang.String, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addParameter(org.javaparser.ast.body.Parameter)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addParameter(org.javaparser.ast.type.Type, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addParameter(java.lang.Class<?>, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.addParameter(java.lang.String, java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.getParamByName(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.getParamByType(java.lang.Class<?>)",
                "org.javaparser.ast.nodeTypes.NodeWithParameters.getParamByType(java.lang.String)",
                "org.javaparser.ast.nodeTypes.NodeWithThrowable.addThrows(org.javaparser.ast.type.ReferenceType)",
                "org.javaparser.ast.nodeTypes.NodeWithThrowable.addThrows(java.lang.Class<? extends java.lang.Throwable>)",
                "org.javaparser.ast.nodeTypes.NodeWithThrowable.isThrows(java.lang.Class<? extends java.lang.Throwable>)",
                "org.javaparser.ast.nodeTypes.NodeWithThrowable.isThrows(java.lang.String)",
                "java.lang.Object.finalize()",
                "java.lang.Object.getClass()",
                "java.lang.Object.notify()",
                "java.lang.Object.notifyAll()",
                "java.lang.Object.registerNatives()",
                "java.lang.Object.wait()",
                "java.lang.Object.wait(long)",
                "java.lang.Object.wait(long, int)"), signatures);
    }

    ///
    /// Test constructors
    ///

    @Test
    void testGetConstructors() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        List<ResolvedConstructorDeclaration> constructors = constructorDeclaration.getConstructors();
        assertEquals(4, constructors.size());

        assertEquals("ConstructorDeclaration()", constructors.get(0).getSignature());
        assertEquals("ConstructorDeclaration(java.util.EnumSet<org.javaparser.ast.Modifier>, java.lang.String)", constructors.get(1).getSignature());
        assertEquals("ConstructorDeclaration(java.util.EnumSet<org.javaparser.ast.Modifier>, java.util.List<org.javaparser.ast.expr.AnnotationExpr>, java.util.List<org.javaparser.ast.type.TypeParameter>, java.lang.String, java.util.List<org.javaparser.ast.body.Parameter>, java.util.List<org.javaparser.ast.type.ReferenceType>, org.javaparser.ast.stmt.BlockStmt)", constructors.get(2).getSignature());
        assertEquals("ConstructorDeclaration(org.javaparser.Range, java.util.EnumSet<org.javaparser.ast.Modifier>, java.util.List<org.javaparser.ast.expr.AnnotationExpr>, java.util.List<org.javaparser.ast.type.TypeParameter>, java.lang.String, java.util.List<org.javaparser.ast.body.Parameter>, java.util.List<org.javaparser.ast.type.ReferenceType>, org.javaparser.ast.stmt.BlockStmt)", constructors.get(3).getSignature());
    }

    ///
    /// Resolution
    ///

    //SymbolReference<MethodDeclaration> solveMethod(String name, List<Type> parameterTypes);
    @Test
    void testSolveMethodExisting() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        SymbolReference<ResolvedMethodDeclaration> res;

        res = constructorDeclaration.solveMethod("isStatic", ImmutableList.of());
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithModifiers.isStatic()", res.getCorrespondingDeclaration().getQualifiedSignature());

        res = constructorDeclaration.solveMethod("isThrows", ImmutableList.of(ReflectionFactory.typeUsageFor(RuntimeException.class.getClass(), typeSolver)));
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable.isThrows(java.lang.Class<? extends java.lang.Throwable>)", res.getCorrespondingDeclaration().getQualifiedSignature());

        res = constructorDeclaration.solveMethod("isThrows", ImmutableList.of(ReflectionFactory.typeUsageFor(String.class, typeSolver)));
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable.isThrows(java.lang.String)", res.getCorrespondingDeclaration().getQualifiedSignature());

        // This is solved because it is raw
        res = constructorDeclaration.solveMethod("isThrows", ImmutableList.of(ReflectionFactory.typeUsageFor(Class.class, typeSolver)));
        assertEquals("org.javaparser.ast.nodeTypes.NodeWithThrowable.isThrows(java.lang.Class<? extends java.lang.Throwable>)", res.getCorrespondingDeclaration().getQualifiedSignature());
    }

    @Test
    void testSolveMethodNotExisting() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");

        SymbolReference<ResolvedMethodDeclaration> res;

        res = constructorDeclaration.solveMethod("unexistingMethod", ImmutableList.of());
        assertEquals(false, res.isSolved());

        res = constructorDeclaration.solveMethod("isStatic", ImmutableList.of(ResolvedPrimitiveType.BOOLEAN));
        assertEquals(false, res.isSolved());
    }

//    @Test
//    public void testSolveMethodNotExistingBecauseOfTypeParameters() {
//        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
//
//        SymbolReference<ResolvedMethodDeclaration> res = null;
//
//        ResolvedReferenceType stringType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(String.class, typeSolver);
//        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
//        ResolvedReferenceType classOfStringType = (ResolvedReferenceType) rawClassType.replaceTypeVariables("T", stringType);
//        res = constructorDeclaration.solveMethod("isThrows", ImmutableList.of(classOfStringType));
//        assertEquals(false, res.isSolved());
//    }

//    @Test
//    public void testSolveSymbolUnexisting() {
//        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
//
//        SymbolReference<? extends ResolvedValueDeclaration> res = constructorDeclaration.solveSymbol("unexisting", typeSolver);
//        assertEquals(false, res.isSolved());
//    }
//
//    @Test
//    public void testSolveSymbolToDeclaredField() {
//        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
//
//        SymbolReference<? extends ResolvedValueDeclaration> res = constructorDeclaration.solveSymbol("name", typeSolver);
//        assertEquals(true, res.isSolved());
//        assertEquals(true, res.getCorrespondingDeclaration().isField());
//    }
//
//    @Test
//    public void testSolveSymbolToInheritedPublicField() {
//        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
//
//        SymbolReference<? extends ResolvedValueDeclaration> res = constructorDeclaration.solveSymbol("NODE_BY_BEGIN_POSITION", typeSolver);
//        assertEquals(true, res.isSolved());
//        assertEquals(true, res.getCorrespondingDeclaration().isField());
//    }
//
//    @Test
//    public void testSolveSymbolToInheritedPrivateField() {
//        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("org.javaparser.ast.body.ConstructorDeclaration");
//
//        SymbolReference<? extends ResolvedValueDeclaration> res = constructorDeclaration.solveSymbol("parentNode", typeSolver);
//        assertEquals(false, res.isSolved());
//    }

    ///
    /// Assignability
    ///

    // boolean isAssignableBy(Type type);

    // boolean canBeAssignedTo(TypeDeclaration other)

    // boolean isAssignableBy(TypeDeclaration other);

    ///
    /// Annotations
    ///

    // hasDirectlyAnnotation

    // hasAnnotation

    ///
    ///
    ///

    // List<TypeParameterDeclaration> getTypeParameters();

    // AccessLevel accessLevel();

    ///
    /// Containment
    ///

    // Set<TypeDeclaration> internalTypes()

    // Optional<TypeDeclaration> containerType()

    ///
    /// Annotations
    ///

    // Issue 1749
    @Test
    void testHasDirectlyAnnotationNegative() {
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(
                new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit cu = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT,
                new StringProvider("public class Employee {"
                        + "    public enum Weekend { SUNDAY, SATURDAY }"
                        + "    private Weekend weekend;"
                        + "}"
        )).getResult().get();
        FieldDeclaration field = cu.getClassByName("Employee").get().getMembers().get(1).asFieldDeclaration();
        ResolvedReferenceTypeDeclaration dec = field.getElementType().resolve().asReferenceType().getTypeDeclaration();
        assertEquals(false, dec.hasDirectlyAnnotation("javax.persistence.Embeddable"));
    }

    // Issue 1749
    @Test
    void testHasDirectlyAnnotationPositive() {
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(
                new JavaSymbolSolver(new ReflectionTypeSolver()));
        CompilationUnit cu = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT,
                new StringProvider("@interface MyAnno {} public class Employee {"
                        + "    public @MyAnno enum Weekend { SUNDAY, SATURDAY }"
                        + "    private Weekend weekend;"
                        + "}"
                )).getResult().get();
        FieldDeclaration field = cu.getClassByName("Employee").get().getMembers().get(1).asFieldDeclaration();
        ResolvedReferenceTypeDeclaration dec = field.getElementType().resolve().asReferenceType().getTypeDeclaration();
        assertEquals(false, dec.hasDirectlyAnnotation("javax.persistence.Embeddable"));
        assertEquals(true, dec.hasDirectlyAnnotation("MyAnno"));
    }
}
