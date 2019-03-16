package io.halahutskyi.boilerplate.plugin.immutable;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.generate.tostring.GenerateToStringClassFilter;
import org.jetbrains.java.generate.GenerateToStringActionHandlerImpl;
import org.jetbrains.java.generate.GenerateToStringWorker;
import org.jetbrains.java.generate.GenerationUtil;
import org.jetbrains.java.generate.config.ConflictResolutionPolicy;
import org.jetbrains.java.generate.config.ReplacePolicy;
import org.jetbrains.java.generate.template.TemplateResource;
import org.jetbrains.java.generate.template.toString.ToStringTemplatesManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

class ImmutableGenerateToStringActionHandler extends GenerateToStringActionHandlerImpl {

    @Nullable
    private static PsiClass getSubjectClass(Editor editor, final PsiFile file) {
        if (file == null) return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);

        if (context == null) return null;

        PsiClass clazz = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (clazz == null) {
            return null;
        }

        //exclude interfaces, non-java classes etc
        for (GenerateToStringClassFilter filter : GenerateToStringClassFilter.EP_NAME.getExtensions()) {
            if (!filter.canGenerateToString(clazz)) return null;
        }
        return clazz;
    }

    private static final Method EXITS_METHOD_DIALOG_METHOD;

    static  {
        try {
            EXITS_METHOD_DIALOG_METHOD = GenerateToStringWorker.class.getDeclaredMethod("exitsMethodDialog", TemplateResource.class);
            EXITS_METHOD_DIALOG_METHOD.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PsiClass clazz = getSubjectClass(editor, file);
        assert clazz != null;

        doExecuteAction(project, clazz, editor);
    }

    @Override
    public void executeActionQuickFix(final Project project, final PsiClass clazz) {
        doExecuteAction(project, clazz, null);
    }

    private static void doExecuteAction(@NotNull final Project project, @NotNull final PsiClass clazz, final Editor editor) {
        try {
            if (!FileModificationService.getInstance().preparePsiElementsForWrite(clazz)) {
                return;
            }

            final PsiElementClassMember[] dialogMembers = buildMembersToShow(clazz);

            final MemberChooserHeaderPanel header = new MemberChooserHeaderPanel(clazz);

            Collection<PsiMember> selectedMembers = GenerationUtil.convertClassMembersToPsiMembers(Arrays.asList(dialogMembers));

            final TemplateResource template = header.getSelectedTemplate();
            ToStringTemplatesManager.getInstance().setDefaultTemplate(template);

            if (template.isValidTemplate()) {
                final GenerateToStringWorker worker = new GenerateToStringWorker(clazz, editor, true);
                // decide what to do if the method already exists
                try {
                    WriteAction.run(() -> worker.execute(selectedMembers, template, ReplacePolicy.getInstance()));
                } catch (Exception e) {
                    GenerationUtil.handleException(project, e);
                }
            } else {
                HintManager.getInstance().showErrorHint(editor, "toString() template '" + template.getFileName() + "' is invalid");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
