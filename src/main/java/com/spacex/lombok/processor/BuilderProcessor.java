package com.spacex.lombok.processor;

import com.spacex.lombok.annotation.Builder;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.spacex.lombok.processor.ProcessUtil.BUILDER_STATIC_METHOD_NAME;

public class BuilderProcessor extends BaseProcessor {

    private Name className;

    private Name builderClassName;

    private List<JCTree.JCVariableDecl> fieldJCVariables;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Builder.class);
        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "@Builder process [" + jcClassDecl.name.toString() + "] start!");
                    before(jcClassDecl);
                    jcClassDecl.defs = jcClassDecl.defs.append(createStaticBuilderMethod());

                    jcClassDecl.defs = jcClassDecl.defs.append(createJCClass());

                    after();
                    messager.printMessage(Diagnostic.Kind.NOTE, "@Builder process [" + jcClassDecl.name.toString() + "] end!");
                }
            });
        });


        return true;
    }

    private JCTree createJCClass() {
        ListBuffer<JCTree> jcTrees = new ListBuffer<>();
        jcTrees.appendList(createVariables());
        jcTrees.appendList(createSetJCMethods());
        jcTrees.append(createBuildJCMethod());
        return treeMaker.ClassDef(
                treeMaker.Modifiers(Flags.PUBLIC + Flags.STATIC + Flags.FINAL),
                builderClassName,
                List.nil(),
                null,
                List.nil(),
                jcTrees.toList()
        );
    }

    private JCTree createBuildJCMethod() {
        return null;
    }

    private List<JCTree> createSetJCMethods() {
        return null;
    }

    private List<JCTree> createVariables() {
        return null;
    }


    private JCTree createStaticBuilderMethod() {
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        jcStatements.append(treeMaker.Return(
                treeMaker.NewClass(
                        null,
                        List.nil(),
                        treeMaker.Ident(builderClassName),
                        List.nil(),
                        null)
        ));

        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatements.toList());


        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC + Flags.STATIC),
                names.fromString(BUILDER_STATIC_METHOD_NAME),
                treeMaker.Ident(builderClassName),
                List.nil(),
                List.nil(),
                List.nil(),
                jcBlock,
                null
        );
    }

    private void before(JCTree.JCClassDecl jcClassDecl) {
        this.className = names.fromString(jcClassDecl.name.toString());
        this.builderClassName = names.fromString(this.className + "Builder");
        this.fieldJCVariables = getVariables();
    }

    private List<JCTree.JCVariableDecl> getVariables() {
        return null;
    }

    private void after() {
        this.className = null;
        this.builderClassName = null;
        this.fieldJCVariables = null;
    }
}
