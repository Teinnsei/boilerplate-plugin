package io.halahutskyi.boilerplate.plugin.immutable;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.GenerateConstructorHandler;
import com.intellij.codeInsight.generation.GenerateEqualsHandler;
import com.intellij.codeInsight.generation.GenerateGetterHandler;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.GenerateToStringActionHandlerImpl;

import java.util.*;
import java.util.stream.Collectors;

public class ImmutableGenerateActionHandler implements CodeInsightActionHandler {

    private static final Set<String> STD_COLLECTION_TYPES;

    static {
        Set<String> stdCollectionTypes = new HashSet<>(0);
        stdCollectionTypes.add("Collection");
        stdCollectionTypes.add("List");
        stdCollectionTypes.add("Set");
        stdCollectionTypes.add("SortedSet");
        stdCollectionTypes.add("NavigableSet");
        stdCollectionTypes.add("Map");
        stdCollectionTypes.add("SortedMap");
        stdCollectionTypes.add("NavigableMap");
        STD_COLLECTION_TYPES = Collections.unmodifiableSet(stdCollectionTypes);
    }

    private final GenerateGetterHandler generateGetterHandler = new ImmutableGenerateGetterHandler();

    private final GenerateConstructorHandler generateConstructorHandler = new ImmutableGenerateConstructorHandler();

    private final GenerateEqualsHandler generateEqualsHandler = new ImmutableGenerateEqualsHandler();

    private final GenerateToStringActionHandlerImpl generateToStringActionHandler = new ImmutableGenerateToStringActionHandler();

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
           return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) file;
        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);

        int offset = editor.getCaretModel().getOffset();
        PsiElement context = file.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
        if (psiClass == null) {
            throw new RuntimeException("Cannot find clazz");
        }

        delegateBaseMethods(project, editor, file);
        addMandatoryImports(psiJavaFile, psiElementFactory);
        modifyClass(psiClass, psiElementFactory);
        generateConstructor(psiClass, psiElementFactory);
        sortStatements(psiClass);
    }

    private void delegateBaseMethods(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        generateConstructorHandler.invoke(project, editor, file);
        generateGetterHandler.invoke(project, editor, file);
        generateEqualsHandler.invoke(project, editor, file);
        generateToStringActionHandler.invoke(project, editor, file);
    }

    private void modifyClass(PsiClass psiClass, PsiElementFactory psiElementFactory) {
        PsiModifierList clazzModifierList = psiClass.getModifierList();
        if (clazzModifierList == null) {
            throw new RuntimeException("Modifiers not found");
        }
        clazzModifierList.setModifierProperty(PsiModifier.FINAL, true);
        addAnnotation(psiElementFactory, psiClass, "@JsonIgnoreProperties(ignoreUnknown = true)");

        PsiField[] allFields = psiClass.getAllFields();
        for (PsiField field : allFields) {
            if (!field.hasModifier(JvmModifier.STATIC)) {
                PsiModifierList modifierList = field.getModifierList();
                if (modifierList == null) {
                    throw new RuntimeException("Modifiers not found");
                }
                modifierList.setModifierProperty(PsiModifier.PRIVATE, true);
                modifierList.setModifierProperty(PsiModifier.FINAL, true);
            }
        }
    }

    private void generateConstructor(PsiClass psiClass, PsiElementFactory psiElementFactory) {
        PsiMethod[] constructors = psiClass.getConstructors();
        PsiMethod constructor = constructors[0];
        addAnnotation(psiElementFactory, constructor, "@JsonCreator");
        PsiParameterList parameterList = constructor.getParameterList();
        for (PsiParameter parameter : parameterList.getParameters()) {
            addAnnotation(psiElementFactory, parameter, String.format("@JsonProperty(\"%s\")", parameter.getName()));
        }
        JvmParameter[] parameters = constructor.getParameters();
        PsiCodeBlock body = constructor.getBody();
        PsiStatement[] statements = body.getStatements();
        for (int i = 0; i < parameters.length; i++) {
            JvmParameter parameter = parameters[i];
            JvmType type = parameter.getType();
            if (!(type instanceof PsiClassReferenceType)) continue;
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) type;
            String className = psiClassReferenceType.getClassName();
            if (!STD_COLLECTION_TYPES.contains(className)) continue;
            String name = parameter.getName();
            PsiStatement statementFromText = psiElementFactory.createStatementFromText(
                    String.format("this.%s = $s == null ? null : Collections.unmodifiable%s(%s);",
                            name, className, name), null);
            PsiStatement statement = statements[i];
            statement.replace(statementFromText);
        }
    }

    private void addMandatoryImports(PsiJavaFile psiJavaFile, PsiElementFactory psiElementFactory) {
        PsiImportList importList = psiJavaFile.getImportList();
        PsiImportStatement importStatementOnDemand = psiElementFactory.createImportStatementOnDemand("com.fasterxml.jackson.annotation");
        importList.add(importStatementOnDemand);
        importStatementOnDemand = psiElementFactory.createImportStatementOnDemand("java.util");
        importList.add(importStatementOnDemand);
    }

    private void sortStatements(PsiClass psiClass) {
        PsiElement[] children = psiClass.getChildren();
        List<PsiElement> methods = Arrays.stream(children)
                .filter(c -> c instanceof PsiMethodImpl)
                .collect(Collectors.toList());

        PsiElement toStringMethod = methods.get(0);
        PsiElement equalsMethod = methods.get(1);
        PsiElement hashCodeMethod = methods.get(2);
        PsiElement constructor = methods.get(methods.size() - 1);

        psiClass.add(constructor.copy());
        psiClass.add(hashCodeMethod.copy());
        psiClass.add(equalsMethod.copy());
        psiClass.add(toStringMethod.copy());

        toStringMethod.delete();
        equalsMethod.delete();
        hashCodeMethod.delete();
        constructor.delete();
    }

    private void addAnnotation(PsiElementFactory psiElementFactory, PsiModifierListOwner psiModifierListOwner, String text) {
        PsiModifierList clazzModifierList = psiModifierListOwner.getModifierList();
        PsiAnnotation annotationFromText = psiElementFactory.createAnnotationFromText(text, null);
        clazzModifierList.addBefore(annotationFromText, clazzModifierList.getFirstChild());
    }

}
