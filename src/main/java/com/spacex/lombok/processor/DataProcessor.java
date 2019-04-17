package com.spacex.lombok.processor;

import com.spacex.lombok.annotation.Data;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.spacex.lombok.processor.ProcessUtil.THIS;

@SupportedAnnotationTypes("com.spacex.lombok.annotation.Data")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DataProcessor extends BaseProcessor {

    private JCTree.JCClassDecl jcClassDecl;
    private List<JCTree.JCVariableDecl> fieldJCVariables;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Data.class);

        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "@Data process class [" + jcClassDecl.name.toString() + "],start");
                    before(jcClassDecl);

                    List<JCTree> dataJCTreeList = createDataMethods();
                    jcClassDecl.defs = jcClassDecl.defs.appendList(dataJCTreeList);

                    after();
                    messager.printMessage(Diagnostic.Kind.NOTE, "@Data process class [" + jcClassDecl.name.toString() + "],end");

                }
            });
        });


        return true;
    }


    private List<JCTree> createDataMethods() {
        ListBuffer<JCTree> dataMethods = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariableDecl : fieldJCVariables) {
            if (!jcVariableDecl.mods.getFlags().contains(Modifier.FINAL)
                    && ProcessUtil.hasSetMethod(jcVariableDecl, jcClassDecl)) {
                dataMethods.append(createSetJCMethod(jcVariableDecl));
            }

            if (!ProcessUtil.hasGetMethod(jcVariableDecl, jcClassDecl)) {
                dataMethods.append(createGetJCMethod(jcVariableDecl));
            }
        }
        return dataMethods.toList();
    }

    private JCTree createGetJCMethod(JCTree.JCVariableDecl jcVariableDecl) {

        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
        jcStatements.append(
                treeMaker.Return(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString(THIS)),
                                jcVariableDecl.name
                        )
                )
        );

        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatements.toList());

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(ProcessUtil.fromPropertyNameToSetMethodName(jcVariableDecl.name.toString())),
                jcVariableDecl.vartype,//return result type
                List.nil(),
                List.nil(),//method args list
                List.nil(),//exception list
                jcBlock,// method body
                null

        );
    }

    private JCTree createSetJCMethod(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        jcStatements.append(treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString(THIS)),
                                jcVariableDecl.name
                        ),
                        treeMaker.Ident(jcVariableDecl.name)
                )
        ));

        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatements.toList());


        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(ProcessUtil.fromPropertyNameToSetMethodName(jcVariableDecl.name.toString())),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.of(ProcessUtil.cloneJCVariableDeclAsParam(treeMaker, jcVariableDecl)),
                List.nil(),
                jcBlock,
                null

        );
    }

    private void before(JCTree.JCClassDecl jcClassDecl) {
        this.jcClassDecl = jcClassDecl;
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClassDecl);
    }

    private void after() {
        this.jcClassDecl = null;
        this.fieldJCVariables = null;
    }
}
