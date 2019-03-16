package io.halahutskyi.boilerplate.plugin.hibernate;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

class HibernateGenerateActionHandler implements CodeInsightActionHandler {

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return;
        }

        PsiJavaFile psiJavaFile = (PsiJavaFile) file;
        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (psiClass == null) {
            throw new RuntimeException("Cannot find clazz");
        }

    }
}
