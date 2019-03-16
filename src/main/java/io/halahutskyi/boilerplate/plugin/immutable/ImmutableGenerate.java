package io.halahutskyi.boilerplate.plugin.immutable;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class ImmutableGenerate extends BaseGenerateAction {

    public ImmutableGenerate() {
        super(new ImmutableGenerateActionHandler());
    }

}
