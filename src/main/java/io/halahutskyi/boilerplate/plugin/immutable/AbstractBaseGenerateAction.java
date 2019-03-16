package io.halahutskyi.boilerplate.plugin.immutable;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import io.halahutskyi.boilerplate.plugin.generation.MembersGenerateHandlerFactory;

abstract class AbstractBaseGenerateAction extends BaseGenerateAction {

    AbstractBaseGenerateAction(MembersGenerateHandlerFactory membersGenerateHandlerFactory) {
        super(new ImmutableGenerateActionHandler(
                membersGenerateHandlerFactory.getGetterHandler(),
                membersGenerateHandlerFactory.getConstructorHandler(),
                membersGenerateHandlerFactory.getEqualsAndHashCodeHandler(),
                membersGenerateHandlerFactory.getToStringHandler()
        ));
    }
}
