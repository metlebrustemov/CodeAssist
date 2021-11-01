package com.tyron.psi.completions.lang.java.search;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.com.intellij.psi.search.SearchScope;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A general interface to perform PsiElement's search scope optimization. The interface should be used only for optimization purposes.
 * It's used in:
 * <ol>
 * <li>
 * {@link PsiSearchHelper#getUseScope(PsiElement)},
 * {@link com.intellij.psi.impl.search.PsiSearchHelperImpl#USE_SCOPE_OPTIMIZER_EP_NAME}
 * to perform optimization of PsiElement's use scope.
 * </li>
 * <li>
 * {@link SearchRequestCollector#searchWord(String, SearchScope, short, boolean, PsiElement)},
 * {@link PsiSearchHelper#getCodeUsageScope(PsiElement)},
 * {@link PsiSearchHelper#CODE_USAGE_SCOPE_OPTIMIZER_EP_NAME}
 * to exclude a scope without references in code from a usages search when the search with {@link UsageSearchContext#IN_CODE} or {@link UsageSearchContext#ANY}
 * context was requested.
 * </li>
 * </ol>
 */
public interface ScopeOptimizer {

    /**
     * @deprecated use {@link ScopeOptimizer#getRestrictedUseScope(PsiElement)} instead.
     */
    @Deprecated
    @Nullable("is null when given optimizer can't provide a scope to exclude")
    default GlobalSearchScope getScopeToExclude(@NotNull PsiElement element) {
        return null;
    }

    @Nullable("is null when given optimizer can't provide a scope to restrict")
    default SearchScope getRestrictedUseScope(@NotNull PsiElement element) {
        GlobalSearchScope scopeToExclude = getScopeToExclude(element);

        return scopeToExclude == null ? null : GlobalSearchScope.notScope(scopeToExclude);
    }

    @Nullable
    static SearchScope calculateOverallRestrictedUseScope(ScopeOptimizer[] optimizers, @NotNull PsiElement element) {
        return Stream
                .of(optimizers)
                .peek(optimizer -> ProgressManager.checkCanceled())
                .map(optimizer -> optimizer.getRestrictedUseScope(element))
                .filter(Objects::nonNull)
                .reduce((s1, s2) -> s1.intersectWith(s2))
                .orElse(null);
    }
}
