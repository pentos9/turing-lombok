package com.spacex.lombok.processor;

import com.spacex.lombok.annotation.AllArgsConstructor;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.spacex.lombok.processor.ProcessUtil.CONSTRUCTOR_NAME;

@SupportedAnnotationTypes("com.spacex.lombok.annotation.AllArgsConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AllArgsConstructorProcessor extends BaseProcessor {

    private List<JCTree.JCVariableDecl> fieldJCVariables;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(AllArgsConstructor.class);

        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "process class [" + jcClassDecl.name.toString() + "],start");

                    before(jcClassDecl);

                    if (!ProcessUtil.hasAllArgsConstructor(fieldJCVariables, jcClassDecl)) {
                        JCTree.JCMethodDecl jcMethodDecl = createAllArgsConstructor();
                        jcClassDecl.defs = jcClassDecl.defs.append(jcMethodDecl);
                    }

                    after();

                    messager.printMessage(Diagnostic.Kind.NOTE, "process class [" + jcClassDecl.name.toString() + "],end");

                }
            });
        });

        return true;
    }


    private JCTree.JCMethodDecl createAllArgsConstructor() {
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        for (JCTree.JCVariableDecl jcVariableDecl : fieldJCVariables) {
            jcStatements.append(treeMaker.Exec(
                    treeMaker.Assign(
                            treeMaker.Select(
                                    treeMaker.Ident(names.fromString(ProcessUtil.THIS)),
                                    names.fromString(jcVariableDecl.name.toString())
                            ),
                            treeMaker.Ident(names.fromString(jcVariableDecl.name.toString()))
                    )
                    )
            );
        }

        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatements.toList());


        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(CONSTRUCTOR_NAME),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                ProcessUtil.cloneJCVariableAsParams(treeMaker, fieldJCVariables),
                List.nil(),
                jcBlock,
                null
        );
    }

    private void before(JCTree.JCClassDecl jcClassDecl) {
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClassDecl);
    }

    private void after() {
        this.fieldJCVariables = null;
    }
}
