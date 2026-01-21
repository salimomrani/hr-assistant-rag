package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardrailService {

    /**
     * Validates that a question is appropriate for the HR assistant.
     *
     * @param question The user's question
     * @throws HrAssistantException if the question is invalid or off-topic
     */
    public void validateQuestion(String question) {
        // Step 1: Check that the question is not empty
        if (!StringUtils.hasText(question)) {
            log.warn("Empty question received");
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    "La question ne peut pas être vide"
            );
        }

        // Step 2: Detect if the question is off-topic
        if (isOffTopic(question)) {
            log.info("Off-topic question detected: {}", question);
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    "Cette question ne concerne pas les ressources humaines. " +
                    "Veuillez contacter directement le service RH pour des questions non liées aux politiques RH."
            );
        }

        log.debug("Question validated: {}", question);
    }

    /**
     * Detects if a question is off-topic (not related to HR).
     *
     * For MVP, uses simple keyword-based detection.
     * TODO: Improve with classification model later.
     */
    private boolean isOffTopic(String question) {
        String lowerQuestion = question.toLowerCase();

        // Keywords that indicate an off-topic question
        String[] offTopicKeywords = {
            "météo", "weather",
            "capitale", "géographie",
            "recette", "cuisine",
            "sport", "football",
            "actualité", "news",
            "film", "cinéma",
            "blague", "joke"
        };

        for (String keyword : offTopicKeywords) {
            if (lowerQuestion.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
