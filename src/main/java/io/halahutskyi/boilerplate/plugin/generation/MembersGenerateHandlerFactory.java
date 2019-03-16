package io.halahutskyi.boilerplate.plugin.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.GenerateConstructorHandler;
import com.intellij.codeInsight.generation.GenerateEqualsHandler;
import com.intellij.codeInsight.generation.GenerateGetterHandler;
import com.intellij.codeInsight.generation.GenerateSetterHandler;
import org.jetbrains.java.generate.GenerateToStringActionHandlerImpl;

public abstract class MembersGenerateHandlerFactory {

    public abstract CodeInsightActionHandler getGetterHandler();

    public abstract CodeInsightActionHandler getSetterHandler();

    public abstract CodeInsightActionHandler getConstructorHandler();

    public abstract CodeInsightActionHandler getEqualsAndHashCodeHandler();

    public abstract CodeInsightActionHandler getToStringHandler();

    private MembersGenerateHandlerFactory() {

    }

    private static class Holder {
        private static final MembersGenerateHandlerFactory allMembersFactory = new AllMembersGenerateHandlerFactory();

        private static final MembersGenerateHandlerFactory chooseMembersFactory = new ChooseMembersGenerateHandlerFactory();
    }

    public static MembersGenerateHandlerFactory getAllMembersFactory() {
        return Holder.allMembersFactory;
    }

    public static MembersGenerateHandlerFactory getChooseMembersFactory() {
        return Holder.chooseMembersFactory;
    }

    private static class ChooseMembersGenerateHandlerFactory extends MembersGenerateHandlerFactory {

        @Override
        public CodeInsightActionHandler getGetterHandler() {
            return new GenerateGetterHandler();
        }

        @Override
        public CodeInsightActionHandler getSetterHandler() {
            return new GenerateSetterHandler();
        }

        @Override
        public CodeInsightActionHandler getConstructorHandler() {
            return new GenerateConstructorHandler();
        }

        @Override
        public CodeInsightActionHandler getEqualsAndHashCodeHandler() {
            return new GenerateEqualsHandler();
        }

        @Override
        public CodeInsightActionHandler getToStringHandler() {
            return new GenerateToStringActionHandlerImpl();
        }

    }

    private static class AllMembersGenerateHandlerFactory extends MembersGenerateHandlerFactory {

        @Override
        public CodeInsightActionHandler getGetterHandler() {
            return new AllMembersGenerateGetterHandler();
        }

        @Override
        public CodeInsightActionHandler getSetterHandler() {
            return new AllMembersGenerateSetterHandler();
        }

        @Override
        public CodeInsightActionHandler getConstructorHandler() {
            return new AllMembersGenerateConstructorHandler();
        }

        @Override
        public CodeInsightActionHandler getEqualsAndHashCodeHandler() {
            return new AllMembersGenerateEqualsHandler();
        }

        @Override
        public CodeInsightActionHandler getToStringHandler() {
            return new AllMembersGenerateToStringActionHandler();
        }
    }


}