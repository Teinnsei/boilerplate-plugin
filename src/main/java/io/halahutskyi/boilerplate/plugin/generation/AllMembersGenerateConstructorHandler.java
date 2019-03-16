package io.halahutskyi.boilerplate.plugin.generation;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateConstructorHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public final class AllMembersGenerateConstructorHandler extends GenerateConstructorHandler {

    @Nullable
    @Override
    protected ClassMember[] chooseMembers(ClassMember[] members,
                                          boolean allowEmptySelection,
                                          boolean copyJavadocCheckbox,
                                          Project project,
                                          @Nullable Editor editor) {
        return members;
    }
}
