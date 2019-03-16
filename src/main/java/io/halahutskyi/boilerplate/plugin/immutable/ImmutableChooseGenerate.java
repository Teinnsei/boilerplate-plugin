package io.halahutskyi.boilerplate.plugin.immutable;

import static io.halahutskyi.boilerplate.plugin.generation.MembersGenerateHandlerFactory.getChooseMembersFactory;

public class ImmutableChooseGenerate extends AbstractBaseGenerateAction {

    public ImmutableChooseGenerate() {
        super(getChooseMembersFactory());
    }

}
