package io.halahutskyi.boilerplate.plugin.immutable;

import static io.halahutskyi.boilerplate.plugin.generation.MembersGenerateHandlerFactory.getAllMembersFactory;

public class ImmutableGenerate extends AbstractBaseGenerateAction {

    public ImmutableGenerate() {
        super(getAllMembersFactory());
    }

}
