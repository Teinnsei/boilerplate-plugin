package io.halahutskyi.boilerplate.plugin.hibernate;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class HibernateGenerate extends BaseGenerateAction {

    public HibernateGenerate() {
        super(new HibernateGenerateActionHandler());
    }

}
