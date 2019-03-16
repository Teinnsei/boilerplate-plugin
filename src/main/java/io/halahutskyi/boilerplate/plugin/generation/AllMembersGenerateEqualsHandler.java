package io.halahutskyi.boilerplate.plugin.generation;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateEqualsHandler;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.ui.GenerateEqualsWizard;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class AllMembersGenerateEqualsHandler extends GenerateEqualsHandler {

    private static final PsiElementClassMember[] DUMMY_RESULT = new PsiElementClassMember[1];

    private static final Field fieldMyEqualsFields;
    private static final Field fieldMyHashCodeFields;
    private static final Field fieldMyNonNullFields;

    static {
        try {
            fieldMyEqualsFields = GenerateEqualsHandler.class.getDeclaredField("myEqualsFields");
            fieldMyEqualsFields.setAccessible(true);
            fieldMyHashCodeFields = GenerateEqualsHandler.class.getDeclaredField("myHashCodeFields");
            fieldMyHashCodeFields.setAccessible(true);
            fieldMyNonNullFields = GenerateEqualsHandler.class.getDeclaredField("myNonNullFields");
            fieldMyNonNullFields.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
        return getAllOriginalMembers(aClass);
    }

    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project, Editor editor) {
        try {
            fieldMyEqualsFields.set(this, null);
            fieldMyHashCodeFields.set(this, null);
            fieldMyNonNullFields.set(this, PsiField.EMPTY_ARRAY);

            boolean hasNonStaticFields = hasNonStaticFields(aClass);
            if (!hasNonStaticFields) {
                HintManager.getInstance().showErrorHint(editor, "No fields to include in equals/hashCode have been found");
                return null;
            }

            GenerateEqualsWizard wizard = new GenerateEqualsWizard(project, aClass, true, true);
            fieldMyEqualsFields.set(this, wizard.getEqualsFields());
            fieldMyHashCodeFields.set(this, wizard.getHashCodeFields());
            fieldMyNonNullFields.set(this, wizard.getNonNullFields());
            return DUMMY_RESULT;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean hasNonStaticFields(PsiClass aClass) {
        for (PsiField field : aClass.getFields()) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                return true;
            }
        }
        return false;
    }

}
