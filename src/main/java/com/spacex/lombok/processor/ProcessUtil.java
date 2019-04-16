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


    private static boolean isSetMethod(JCTree jcTree) {
        if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
            return jcMethodDecl.name.toString().startsWith(SET)
                    && jcMethodDecl.params.size() == 1
                    && !jcMethodDecl.mods.getFlags().contains(Modifier.STATIC);
        }

        return false;
    }

    static List<JCTree.JCMethodDecl> getSetJCMethods(JCTree.JCClassDecl jcClassDecl) {
        ListBuffer<JCTree.JCMethodDecl> setJCMethods = new ListBuffer<>();
        for (JCTree jcTree : jcClassDecl.defs) {
            if (isSetMethod(jcTree)) {
                setJCMethods.append((JCTree.JCMethodDecl) jcTree);
            }
        }

        return setJCMethods.toList();
    }

    static boolean hasNoArgsConstructor(JCTree.JCClassDecl jcClassDecl) {

        for (JCTree jcTree : jcClassDecl.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethodDecl.name.toString())) {
                    if (jcMethodDecl.params.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static boolean hasAllArgsConstructor(List<JCTree.JCVariableDecl> jcVariableDecls, JCTree.JCClassDecl jcClassDecl) {
        for (JCTree jcTree : jcClassDecl.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethodDecl.name.toString())) {
                    if (jcVariableDecls.size() == jcMethodDecl.params.size()) {
                        boolean isEqual = true;

                        for (int i = 0; i < jcVariableDecls.size(); i++) {
                            if (!jcVariableDecls.get(i).vartype.type.equals(jcMethodDecl.params.get(i).vartype.type)) {
                                isEqual = false;
                                break;
                            }
                        }

                        if (isEqual) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    static boolean hasSetMethod(JCTree.JCVariableDecl jcVariableDecl, JCTree.JCClassDecl jcClassDecl) {
        String setMethodName = fromPropertyNameToSetMethodName(jcVariableDecl.name.toString());
        for (JCTree jcTree : jcClassDecl.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (setMethodName.equals(jcMethodDecl.name.toString())
                        && jcMethodDecl.params.size() == 1
                        && jcMethodDecl.params.get(0).vartype.type.equals(jcVariableDecl.vartype.type)) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean hasGetMethod(JCTree.JCVariableDecl jcVariableDecl, JCTree.JCClassDecl jcClassDecl) {
        String getMethodName = fromPropertyNameToGetMethodName(jcVariableDecl.name.toString());
        for (JCTree jcTree : jcClassDecl.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (getMethodName.equals(jcMethodDecl.name.toString())
                        && jcMethodDecl.params.size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    static String fromPropertyNameToSetMethodName(String propertyName) {
        return SET + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    static String fromPropertyNameToGetMethodName(String propertyName) {
        return GET + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

}
