package io.halahutskyi.boilerplate.plugin.immutable;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateGetterHandler;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

final class ImmutableGenerateGetterHandler extends GenerateGetterHandler {

    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project, Editor editor) {
        final ClassMember[] allMembers = getAllOriginalMembers(aClass);
        if (allMembers == null) {
            HintManager.getInstance().showErrorHint(editor, getNothingFoundMessage());
            return null;
        }
        if (allMembers.length == 0) {
            HintManager.getInstance().showErrorHint(editor, getNothingAcceptedMessage());
            return null;
        }
        return allMembers;
    }

}
