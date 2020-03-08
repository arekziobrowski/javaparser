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

package com.github.javaparser.generator.core;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.generator.core.node.*;
import com.github.javaparser.generator.core.other.BndGenerator;
import com.github.javaparser.generator.core.other.RemoveGeneratorAnnotations;
import com.github.javaparser.generator.core.other.TokenKindGenerator;
import com.github.javaparser.generator.core.visitor.*;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.javaparser.ParserConfiguration.LanguageLevel.RAW;
import static com.github.javaparser.utils.CodeGenerationUtils.f;

/**
 * Generates all generated visitors in the javaparser-core module.
 * Suggested usage is by running the run_core_generators.sh script.
 * You may want to run_metamodel_generator.sh before that.
 */
public class CoreGenerator {

    public static final String GENERATOR_INIT_TIMESTAMP = System.currentTimeMillis() + "L";

    private static final ParserConfiguration parserConfiguration = new ParserConfiguration()
            .setLanguageLevel(RAW)
//                                .setStoreTokens(false)
//                                .setAttributeComments(false)
//                                .setLexicalPreservationEnabled(true)
            ;

    protected final List<SourceRoot> sourceRoots;

    private final SourceRoot jpCoreSourceRoot;
    private final SourceRoot jssLogicSourceRoot;
    private final SourceRoot jssModelSourceRoot;
    private final SourceRoot jssSourceRoot;
    private final SourceRoot generatedJavaCcSourceRoot;


    public CoreGenerator(Path projectRoot) {
        // Setup source roots
        this.jpCoreSourceRoot = getSourceRootForJpModule(projectRoot, "javaparser-core");
        this.jssLogicSourceRoot = getSourceRootForJpModule(projectRoot, "javaparser-symbol-solver-logic");
        this.jssModelSourceRoot = getSourceRootForJpModule(projectRoot, "javaparser-symbol-solver-model");
        this.jssSourceRoot = getSourceRootForJpModule(projectRoot, "javaparser-symbol-solver-core");

        final Path generatedJavaCcRoot = projectRoot.resolve("javaparser-core").resolve("target").resolve("generated-sources").resolve("javacc");
        this.generatedJavaCcSourceRoot = new SourceRoot(generatedJavaCcRoot, parserConfiguration);

        // Setup collection for later iteration
        this.sourceRoots = new ArrayList<>(7);
        this.sourceRoots.add(this.jpCoreSourceRoot);
        this.sourceRoots.add(this.jssLogicSourceRoot);
        this.sourceRoots.add(this.jssModelSourceRoot);
        this.sourceRoots.add(this.jssSourceRoot);
        this.sourceRoots.add(this.generatedJavaCcSourceRoot);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Need 1 parameter: the JavaParser generator source checkout root directory.");
        }
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());
        StaticJavaParser.setConfiguration(parserConfiguration);

        Path projectRoot = Paths.get(args[0]).resolve("..");

        // Do generating.
        final CoreGenerator coreGenerator = new CoreGenerator(projectRoot);
        coreGenerator.deleteAllGeneratorAnnotations(coreGenerator.sourceRoots);
        coreGenerator.runner();
    }

    private static SourceRoot getSourceRootForJpModule(Path projectRoot, String moduleName) {
        final Path jpCoreProjectRoot = projectRoot.resolve(moduleName).resolve("src").resolve("main").resolve("java");
        return getSourceRoot(jpCoreProjectRoot);
    }

    private static SourceRoot getSourceRoot(Path rootPath) {
        return new SourceRoot(rootPath, parserConfiguration); //.setPrinter(LexicalPreservingPrinter::print);
    }

    public void runner() {
        this.run(this.jpCoreSourceRoot, this.generatedJavaCcSourceRoot);
    }

    public void deleteAllGeneratorAnnotations(List<SourceRoot> sourceRoots) {
        final String description = "deleteAllGeneratorAnnotations";
        final Set<CompilationUnit> allTouchedCus = new HashSet<>();

        // Do deletion for all source roots
        for (SourceRoot sourceRoot : sourceRoots) {
            try {
                final List<CompilationUnit> thisTouchedCus = new RemoveGeneratorAnnotations(sourceRoot).generate();
                allTouchedCus.addAll(thisTouchedCus);
                System.out.println(f(
                        "\n" +
                                "\nFinished running %s for sourceRoot %s" +
                                "\nsetup touched %d files within %s %n" +
                                "\n\n"
                        , description, sourceRoot, thisTouchedCus.size(), sourceRoot)
                );
            } catch (Exception e) {
                System.out.println(f("ERROR:: running %s for sourceRoot %s", description, sourceRoot));
                e.printStackTrace();
                throw new RuntimeException(f("ERROR:: running %s for sourceRoot %s", description, sourceRoot), e);
            }
        }

        // Save the files after removing the annotations.
        System.out.println(f("Finished %s for all sourceRoots", description));
        allTouchedCus.forEach(compilationUnit -> compilationUnit.getStorage().orElseThrow(IllegalStateException::new).save());

    }

    private void x(String a, Set<CompilationUnit> y, List<CompilationUnit> z) {
        System.out.println(a + ": " + "adding " + z.size() + " cus -- now totalling " + y.size());
        y.addAll(z);
    }

    private void run(SourceRoot jpCoreSourceRoot, SourceRoot generatedJavaCcSourceRoot) {
        Set<CompilationUnit> touchedCus = new HashSet<>();

        this.x("TypeCastingGenerator", touchedCus, new TypeCastingGenerator(jpCoreSourceRoot).generate());
        this.x("GenericListVisitorAdapterGenerator", touchedCus, new GenericListVisitorAdapterGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("GenericVisitorAdapterGenerator", touchedCus, new GenericVisitorAdapterGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("GenericVisitorWithDefaultsGenerator", touchedCus, new GenericVisitorWithDefaultsGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("EqualsVisitorGenerator", touchedCus, new EqualsVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("ObjectIdentityEqualsVisitorGenerator", touchedCus, new ObjectIdentityEqualsVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("NoCommentEqualsVisitorGenerator", touchedCus, new NoCommentEqualsVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("VoidVisitorAdapterGenerator", touchedCus, new VoidVisitorAdapterGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("VoidVisitorGenerator", touchedCus, new VoidVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("VoidVisitorWithDefaultsGenerator", touchedCus, new VoidVisitorWithDefaultsGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("GenericVisitorGenerator", touchedCus, new GenericVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("HashCodeVisitorGenerator", touchedCus, new HashCodeVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("ObjectIdentityHashCodeVisitorGenerator", touchedCus, new ObjectIdentityHashCodeVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("NoCommentHashCodeVisitorGenerator", touchedCus, new NoCommentHashCodeVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Not triggered.
        this.x("CloneVisitorGenerator", touchedCus, new CloneVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: Doesn't regenerate ????
        this.x("ModifierVisitorGenerator", touchedCus, new ModifierVisitorGenerator(jpCoreSourceRoot).generate()); // TODO: not triggered.

        this.x("PropertyGenerator", touchedCus, new PropertyGenerator(jpCoreSourceRoot).generate());
        this.x("RemoveMethodGenerator", touchedCus, new RemoveMethodGenerator(jpCoreSourceRoot).generate());
        this.x("ReplaceMethodGenerator", touchedCus, new ReplaceMethodGenerator(jpCoreSourceRoot).generate());
        this.x("CloneGenerator", touchedCus, new CloneGenerator(jpCoreSourceRoot).generate());
        this.x("GetMetaModelGenerator", touchedCus, new GetMetaModelGenerator(jpCoreSourceRoot).generate());
        this.x("MainConstructorGenerator", touchedCus, new MainConstructorGenerator(jpCoreSourceRoot).generate());
        this.x("NodeModifierGenerator", touchedCus, new NodeModifierGenerator(jpCoreSourceRoot).generate());
        this.x("AcceptGenerator", touchedCus, new AcceptGenerator(jpCoreSourceRoot).generate());
        this.x("TokenKindGenerator", touchedCus, new TokenKindGenerator(jpCoreSourceRoot, generatedJavaCcSourceRoot).generate());

        // Does its own file-writing
        new BndGenerator(jpCoreSourceRoot).generate();

        System.out.printf("run touched %d files in %s %n", touchedCus.size(), jpCoreSourceRoot);
        touchedCus.forEach(compilationUnit -> compilationUnit.getStorage().orElseThrow(IllegalStateException::new).save());

    }

}
