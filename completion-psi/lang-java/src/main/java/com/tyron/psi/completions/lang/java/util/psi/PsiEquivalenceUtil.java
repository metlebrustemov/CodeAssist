package com.tyron.psi.completions.lang.java.util.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.com.intellij.lang.ASTNode;
import org.jetbrains.kotlin.com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.kotlin.com.intellij.openapi.util.Comparing;
import org.jetbrains.kotlin.com.intellij.openapi.util.Couple;
import org.jetbrains.kotlin.com.intellij.psi.*;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.kotlin.com.intellij.util.PairConsumer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author ven
 */
public final class PsiEquivalenceUtil {
    private static final Logger LOG = Logger.getInstance(PsiEquivalenceUtil.class);

    public static boolean areElementsEquivalent(@NotNull PsiElement element1,
                                                @NotNull PsiElement element2,
                                                @Nullable Comparator<? super PsiElement> resolvedElementsComparator,
                                                boolean areCommentsSignificant) {
        return areElementsEquivalent(element1, element2, new ReferenceComparator(resolvedElementsComparator), null, null, areCommentsSignificant);
    }

    public static boolean areElementsEquivalent(@NotNull PsiElement element1,
                                                @NotNull PsiElement element2,
                                                @Nullable Comparator<? super PsiElement> resolvedElementsComparator,
                                                @Nullable Comparator<? super PsiElement> leafElementsComparator) {
        return areElementsEquivalent(element1, element2, new ReferenceComparator(resolvedElementsComparator), leafElementsComparator, null, false);
    }

    private static class ReferenceComparator implements Comparator<PsiReference> {
        private @Nullable
        final Comparator<? super PsiElement> myResolvedElementsComparator;

        ReferenceComparator(@Nullable Comparator<? super PsiElement> resolvedElementsComparator) {
            myResolvedElementsComparator = resolvedElementsComparator;
        }

        @Override
        public int compare(PsiReference ref1, PsiReference ref2) {
            PsiElement resolved1 = ref1.resolve();
            PsiElement resolved2 = ref2.resolve();
            return Comparing.equal(resolved1, resolved2) ||
                    myResolvedElementsComparator != null && myResolvedElementsComparator.compare(resolved1, resolved2) == 0 ? 0 : 1;
        }
    }

    public static boolean areElementsEquivalent(@NotNull PsiElement element1,
                                                @NotNull PsiElement element2,
                                                @NotNull Comparator<? super PsiReference> referenceComparator,
                                                @Nullable Comparator<? super PsiElement> leafElementsComparator,
                                                @Nullable Predicate<? super PsiElement> isElementSignificantCondition,
                                                boolean areCommentsSignificant) {
        if (element1 == element2) return true;
        ASTNode node1 = element1.getNode();
        ASTNode node2 = element2.getNode();
        if (node1 == null || node2 == null) return false;
        if (node1.getElementType() != node2.getElementType()) return false;

        PsiElement[] children1 = getFilteredChildren(element1, isElementSignificantCondition, areCommentsSignificant);
        PsiElement[] children2 = getFilteredChildren(element2, isElementSignificantCondition, areCommentsSignificant);
        if (children1.length != children2.length) return false;

        for (int i = 0; i < children1.length; i++) {
            PsiElement child1 = children1[i];
            PsiElement child2 = children2[i];
            if (!areElementsEquivalent(child1, child2, referenceComparator,
                    leafElementsComparator, isElementSignificantCondition, areCommentsSignificant))
                return false;
        }

        if (children1.length == 0) {
            if (leafElementsComparator != null) {
                if (leafElementsComparator.compare(element1, element2) != 0) return false;
            } else {
                if (!element1.textMatches(element2)) return false;
            }
        }

        PsiReference ref1 = element1.getReference();
        if (ref1 != null) {
            PsiReference ref2 = element2.getReference();
            if (ref2 == null) return false;
            if (referenceComparator.compare(ref1, ref2) != 0) return false;
        }
        return true;
    }

    public static boolean areElementsEquivalent(@NotNull PsiElement element1, @NotNull PsiElement element2) {
        return areElementsEquivalent(element1, element2, null, false);
    }

    public static PsiElement[] getFilteredChildren(@NotNull final PsiElement element,
                                                   @Nullable Predicate<? super PsiElement> isElementSignificantCondition,
                                                   boolean areCommentsSignificant) {
        ASTNode[] children1 = element.getNode().getChildren(null);
        ArrayList<PsiElement> array = new ArrayList<>();
        for (ASTNode node : children1) {
            final PsiElement child = node.getPsi();
            if (!(child instanceof PsiWhiteSpace) && (areCommentsSignificant || !(child instanceof PsiComment)) &&
                    (isElementSignificantCondition == null || isElementSignificantCondition.test(child))) {
                array.add(child);
            }
        }
        return PsiUtilCore.toPsiElementArray(array);
    }

    public static void findChildRangeDuplicates(PsiElement first, PsiElement last,
                                                final List<? super Couple<PsiElement>> result,
                                                PsiElement scope) {
        findChildRangeDuplicates(first, last, scope, (start, end) -> result.add(Couple.of(start, end)));
    }

    public static void findChildRangeDuplicates(PsiElement first, PsiElement last, PsiElement scope,
                                                PairConsumer<? super PsiElement, ? super PsiElement> consumer) {
        LOG.assertTrue(first.getParent() == last.getParent());
        LOG.assertTrue(!(first instanceof PsiWhiteSpace) && !(last instanceof PsiWhiteSpace));
        addRangeDuplicates(scope, first, last, consumer);
    }

    private static void addRangeDuplicates(final PsiElement scope,
                                           final PsiElement first,
                                           final PsiElement last,
                                           final PairConsumer<? super PsiElement, ? super PsiElement> result) {
        final PsiElement[] children = getFilteredChildren(scope, null, true);
        NextChild:
        for (int i = 0; i < children.length; ) {
            PsiElement child = children[i];
            if (child != first) {
                int j = i;
                PsiElement next = first;
                while (areElementsEquivalent(children[j], next)) {
                    j++;
                    if (next == last) {
                        result.consume(child, children[j - 1]);
                        i = j + 1;
                        continue NextChild;
                    }
                    next = PsiTreeUtil.skipWhitespacesForward(next);
                }

                if (i == j) {
                    addRangeDuplicates(child, first, last, result);
                }
            }

            i++;
        }
    }
}
