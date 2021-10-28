package com.tyron.psi.completions.lang.java.patterns;

import com.tyron.psi.patterns.ElementPattern;
import com.tyron.psi.patterns.InitialPatternCondition;
import com.tyron.psi.patterns.PatternCondition;
import com.tyron.psi.patterns.PlatformPatterns;
import com.tyron.psi.patterns.PsiElementPattern;
import com.tyron.psi.patterns.PsiNamePatternCondition;
import com.tyron.psi.patterns.StandardPatterns;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.com.intellij.psi.JavaResolveResult;
import org.jetbrains.kotlin.com.intellij.psi.PsiAnnotation;
import org.jetbrains.kotlin.com.intellij.psi.PsiAnnotationParameterList;
import org.jetbrains.kotlin.com.intellij.psi.PsiClass;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiExpression;
import org.jetbrains.kotlin.com.intellij.psi.PsiExpressionList;
import org.jetbrains.kotlin.com.intellij.psi.PsiIdentifier;
import org.jetbrains.kotlin.com.intellij.psi.PsiJavaCodeReferenceElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiMember;
import org.jetbrains.kotlin.com.intellij.psi.PsiMethod;
import org.jetbrains.kotlin.com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.kotlin.com.intellij.psi.PsiNewExpression;
import org.jetbrains.kotlin.com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.kotlin.com.intellij.psi.PsiVariable;
import org.jetbrains.kotlin.com.intellij.util.ProcessingContext;
import org.jetbrains.kotlin.com.intellij.util.containers.ContainerUtil;

/**
 * @author peter
 */
public class PsiJavaElementPattern<T extends PsiElement,Self extends PsiJavaElementPattern<T,Self>> extends PsiElementPattern<T,Self> {
    @NonNls private static final String VALUE = "value";

    public PsiJavaElementPattern(final Class<T> aClass) {
        super(aClass);
    }

    public PsiJavaElementPattern(@NotNull final InitialPatternCondition<T> condition) {
        super(condition);
    }

    public Self annotationParam(@NonNls final String annotationQualifiedName, @NonNls final String parameterName) {
        return withParent(
                PsiJavaPatterns.psiNameValuePair().withName(parameterName).withParent(
                        PlatformPatterns.psiElement(PsiAnnotationParameterList.class).withParent(
                                PsiJavaPatterns.psiAnnotation().qName(annotationQualifiedName))));
    }

    public Self annotationParam(@NonNls final String annotationQualifiedName) {
        return annotationParam(annotationQualifiedName, VALUE);
    }

    public Self annotationParam(final ElementPattern<String> annotationQualifiedName, @NonNls final String parameterName) {
        return withParent(
                PsiJavaPatterns.psiNameValuePair().withName(parameterName).withParent(
                        PlatformPatterns.psiElement(PsiAnnotationParameterList.class).withParent(
                                PsiJavaPatterns.psiAnnotation().qName(annotationQualifiedName))));
    }
    public Self annotationParam(@NotNull ElementPattern<? extends PsiAnnotation> annotation) {
        return withParent(
                PsiJavaPatterns.psiNameValuePair().withParent(
                        PlatformPatterns.psiElement(PsiAnnotationParameterList.class).withParent(
                                annotation)));
    }

    public Self annotationParam(String parameterName, @NotNull ElementPattern<? extends PsiAnnotation> annotation) {
        return withParent(
                PsiJavaPatterns.psiNameValuePair().withName(parameterName).withParent(
                        PlatformPatterns.psiElement(PsiAnnotationParameterList.class).withParent(annotation)));
    }

    public Self insideAnnotationParam(final ElementPattern<String> annotationQualifiedName, @NonNls final String parameterName) {
        return withAncestor(3,   // can be array initializer
                PsiJavaPatterns.psiNameValuePair().withName(parameterName).withParent(
                        PlatformPatterns.psiElement(PsiAnnotationParameterList.class).withParent(
                                PsiJavaPatterns.psiAnnotation().qName(annotationQualifiedName))));
    }

    public Self insideAnnotationParam(final ElementPattern<String> annotationQualifiedName) {
        return insideAnnotationParam(annotationQualifiedName, VALUE);
    }

    public Self insideAnnotationParam(@NotNull String annotationQualifiedName) {
        return insideAnnotationParam(StandardPatterns.string().equalTo(annotationQualifiedName));
    }

    public Self nameIdentifierOf(final Class<? extends PsiMember> aClass) {
        return nameIdentifierOf(StandardPatterns.instanceOf(aClass));
    }

    public Self nameIdentifierOf(final ElementPattern<? extends PsiMember> pattern) {
        return with(new PatternCondition<T>("nameIdentifierOf") {
            @Override
            public boolean accepts(@NotNull final T t, final ProcessingContext context) {
                if (!(t instanceof PsiIdentifier)) return false;

                final PsiElement parent = t.getParent();
                if (parent instanceof PsiClass && t != ((PsiClass) parent).getNameIdentifier()) return false;
                if (parent instanceof PsiMethod && t != ((PsiMethod) parent).getNameIdentifier()) return false;
                if (parent instanceof PsiVariable && t != ((PsiVariable) parent).getNameIdentifier()) return false;

                return pattern.accepts(parent, context);
            }
        });
    }

    public Self methodCallParameter(final int index, final ElementPattern<? extends PsiMethod> methodPattern) {
        final PsiNamePatternCondition nameCondition = ContainerUtil.findInstance(methodPattern.getCondition().getConditions(), PsiNamePatternCondition.class);

        return with(new PatternCondition<T>("methodCallParameter") {
            @Override
            public boolean accepts(@NotNull final T literal, final ProcessingContext context) {
                final PsiElement parent = literal.getParent();
                if (parent instanceof PsiExpressionList) {
                    return hasIndex(literal, index) && checkCall(context, (PsiExpressionList)parent, methodPattern, nameCondition);
                }
                return false;
            }

            private boolean hasIndex(@NotNull T literal, int index) {
                int currentIndex = 0;
                PsiElement each = literal;
                while (each != null) {
                    each = each.getPrevSibling();
                    if (each instanceof PsiExpression) {
                        currentIndex++;
                        if (currentIndex > index) return false;
                    }
                }

                if (currentIndex != index) return false;
                return true;
            }
        });
    }

    public Self methodCallParameter(@NotNull final ElementPattern<? extends PsiMethod> methodPattern) {
        final PsiNamePatternCondition nameCondition = ContainerUtil.findInstance(methodPattern.getCondition().getConditions(), PsiNamePatternCondition.class);

        return with(new PatternCondition<T>("methodCallParameter") {
            @Override
            public boolean accepts(@NotNull final T literal, final ProcessingContext context) {
                final PsiElement parent = literal.getParent();
                return parent instanceof PsiExpressionList && checkCall(context, (PsiExpressionList)parent, methodPattern, nameCondition);
            }
        });
    }

    private static boolean checkCall(ProcessingContext context,
                                     PsiExpressionList psiExpressionList,
                                     ElementPattern<? extends PsiMethod> methodPattern,
                                     @Nullable PsiNamePatternCondition nameCondition) {
        final PsiElement element = psiExpressionList.getParent();
        if (element instanceof PsiMethodCallExpression) {
            PsiReferenceExpression methodExpression = ((PsiMethodCallExpression)element).getMethodExpression();
            if (nameCondition != null && !nameCondition.getNamePattern().accepts(methodExpression.getReferenceName())) {
                return false;
            }

            for (JavaResolveResult result : methodExpression.multiResolve(false)) {
                if (methodPattern.accepts(result.getElement(), context)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Self constructorParameter(final int index, final String... fqns) {
        return with(new PatternCondition<T>("constructorParameter") {
            @Override
            public boolean accepts(@NotNull final T literal, final ProcessingContext context) {
                final PsiElement parent = literal.getParent();
                if (parent instanceof PsiExpressionList) {
                    final PsiExpressionList psiExpressionList = (PsiExpressionList)parent;
                    final PsiExpression[] psiExpressions = psiExpressionList.getExpressions();
                    if (!(psiExpressions.length > index && psiExpressions[index] == literal)) return false;

                    final PsiElement element = psiExpressionList.getParent();
                    if (element instanceof PsiNewExpression) {
                        PsiJavaCodeReferenceElement reference = ((PsiNewExpression)element).getClassOrAnonymousClassReference();
                        if (reference != null) {
                            String qualifiedName = reference.getQualifiedName();
                            for (String fqn : fqns) {
                                if( fqn.equals(qualifiedName)) return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    public static class Capture<T extends PsiElement> extends PsiJavaElementPattern<T, Capture<T>> {
        public Capture(final Class<T> aClass) {
            super(aClass);
        }

        public Capture(@NotNull final InitialPatternCondition<T> condition) {
            super(condition);
        }
    }



}