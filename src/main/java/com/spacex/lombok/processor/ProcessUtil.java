package com.spacex.lombok.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Modifier;
import java.util.Set;

public class ProcessUtil {
    static final String THIS = "this";
    private static final String SET = "set";
    private static final String GET = "get";

    static final String BUILDER_STATIC_METHOD_NAME = "builder";

    static final String BUILDER_METHOD_NAME = "build";

    static final String CONSTRUCTOR_NAME = "<init>";

    public static JCTree.JCVariableDecl cloneJCVariableDeclAsParam(TreeMaker treeMaker, JCTree.JCVariableDecl prototypeJCVariable) {
        return treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),
                prototypeJCVariable.name,
                prototypeJCVariable.vartype,
                null);
    }

    /**
     * 克隆一个字段的语法树节点集合，
     * 作为方法的参数列表
     *
     * @param treeMaker
     * @param prototypeJCVariables
     * @return
     */
    public static List<JCTree.JCVariableDecl> cloneJCVariableAsParams(TreeMaker treeMaker, List<JCTree.JCVariableDecl> prototypeJCVariables) {
        ListBuffer<JCTree.JCVariableDecl> jcVariableDecls = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariableDecl : jcVariableDecls) {
            jcVariableDecls.append(cloneJCVariableDeclAsParam(treeMaker, jcVariableDecl));
        }
        return jcVariableDecls.toList();
    }

    private static boolean isValidField(JCTree jcTree) {
        if (jcTree.getKind().equals(JCTree.Kind.VARIABLE)) {
            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
            Set<Modifier> flagSets = jcVariableDecl.mods.getFlags();
            return (!flagSets.contains(Modifier.STATIC)
                    && !(flagSets.contains(Modifier.FINAL)));
        }

        return false;
    }

    static List<JCTree.JCVariableDecl> getJCVariable(JCTree.JCClassDecl jcClassDecl) {
        ListBuffer<JCTree.JCVariableDecl> jcVariableDecls = new ListBuffer<>();

        for (JCTree jcTree : jcClassDecl.defs) {
            if (isValidField(jcTree)) {
                jcVariableDecls.append((JCTree.JCVariableDecl) jcTree);
            }
        }

        return jcVariableDecls.toList();
    }


    private boolean isSetMethod(JCTree jcTree) {
        return false;
    }

    private static List<JCTree.JCVariableDecl> getSetJCMethods(JCTree.JCClassDecl jcClassDecl) {
        return null;
    }

}
