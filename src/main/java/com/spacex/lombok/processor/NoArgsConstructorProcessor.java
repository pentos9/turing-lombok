package com.spacex.lombok.processor;

import com.spacex.lombok.annotation.NoArgsConstructor;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.spacex.lombok.processor.ProcessUtil.CONSTRUCTOR_NAME;

@SupportedAnnotationTypes("com.spacex.lombok.annotation.NoArgsConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class NoArgsConstructorProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(NoArgsConstructor.class);

        set.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    System.out.println("NoArgsConstructorProcessor#visitClassDef invoke!");
                    messager.printMessage(Diagnostic.Kind.NOTE, "@NoArgsConstructor process [" + jcClassDecl.name.toString() + "] start!");
                    if (!ProcessUtil.hasNoArgsConstructor(jcClassDecl)) {
                        JCTree.JCMethodDecl noArgsConstructor = createNoArgsConstructor();
                        jcClassDecl.defs = jcClassDecl.defs.append(noArgsConstructor);
                    }
                    messager.printMessage(Diagnostic.Kind.NOTE, "@NoArgsConstructor process [" + jcClassDecl.name.toString() + "] end!");
                }
            });
        });

        return true;
    }

    private JCTree.JCMethodDecl createNoArgsConstructor() {
        JCTree.JCBlock jcBlock = treeMaker.Block(0, List.nil());


        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(CONSTRUCTOR_NAME),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),//generic type list
                List.nil(),//args list
                List.nil(),//exception list
                jcBlock,
                null
        );
    }
}
