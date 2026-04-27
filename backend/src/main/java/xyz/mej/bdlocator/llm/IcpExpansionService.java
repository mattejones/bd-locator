package xyz.mej.bdlocator.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IcpExpansionService {

    private final LlmAdapter llmAdapter;

    /**
     * Expands a natural language ICP description into CQC taxonomy keywords.
     * Returns an empty set if the description is blank or expansion fails —
     * the scoring engine treats an empty set as no gate applied.
     */
    public Set<String> expand(String icpDescription) {
        if (icpDescription == null || icpDescription.isBlank()) {
            return Set.of();
        }

        log.info("Expanding ICP description: \"{}\"", icpDescription);
        Set<String> keywords = llmAdapter.expandIcp(icpDescription);
        log.info("ICP expansion returned {} keywords: {}", keywords.size(), keywords);
        return keywords;
    }
}
