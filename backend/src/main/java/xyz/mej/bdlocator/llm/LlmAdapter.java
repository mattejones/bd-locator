package xyz.mej.bdlocator.llm;

import java.util.Set;

/**
 * Adapter interface for LLM providers.
 * Implementations are swappable via the app.llm.provider config value.
 */
public interface LlmAdapter {

    /**
     * Expands a natural language ICP description into a set of
     * lowercase CQC taxonomy keywords suitable for gate filtering.
     *
     * Returns an empty set if expansion fails — callers must treat
     * an empty set as "no gate applied" rather than "no results".
     */
    Set<String> expandIcp(String icpDescription);
}
