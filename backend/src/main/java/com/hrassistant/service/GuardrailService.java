package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ClassificationResponse;
import com.hrassistant.model.ConfidenceLevel;
import com.hrassistant.model.GuardrailResult;
import com.hrassistant.model.HrCategory;
import com.hrassistant.model.OutputGuardrailResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class GuardrailService {

  private static final long CLASSIFICATION_TIMEOUT_SECONDS = 5;

  private static final String[] OFF_TOPIC_KEYWORDS = {
    "météo", "weather",
    "capitale", "géographie",
    "recette", "cuisine",
    "sport", "football",
    "actualité", "news",
    "film", "cinéma",
    "blague", "joke"
  };

  private static final Map<String, Pattern> PII_PATTERNS =
      Map.of(
          "FRENCH_PHONE",
          Pattern.compile("(?:(?:\\+33|0033)\\s?|0)[1-9](?:[\\s.-]?\\d{2}){4}"),
          "EMAIL",
          Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
          "FRENCH_SSN",
          Pattern.compile("[12]\\s?\\d{2}\\s?\\d{2}\\s?\\d{2}\\s?\\d{3}\\s?\\d{3}\\s?\\d{2}"),
          "IBAN",
          Pattern.compile("FR\\d{2}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{3}"),
          "SALARY",
          Pattern.compile(
              "\\d{1,3}(?:[\\s.,]\\d{3})*(?:[.,]\\d{2})?\\s?(?:euros?|EUR|€)",
              Pattern.CASE_INSENSITIVE));

  private static final String[] HARMFUL_KEYWORDS = {
    "discrimination", "discriminatoire",
    "harcèlement sexuel", "harcèlement moral",
    "licenciement abusif", "conseil juridique",
    "avis médical", "diagnostic"
  };

  private final ChatModel chatModel;
  private final Resource classificationPrompt;
  private final BeanOutputConverter<ClassificationResponse> outputConverter;

  public GuardrailService(
      ChatModel chatModel,
      @Value("classpath:prompts/classification-prompt.txt") Resource classificationPrompt) {
    this.chatModel = chatModel;
    this.classificationPrompt = classificationPrompt;
    this.outputConverter = new BeanOutputConverter<>(ClassificationResponse.class);
  }

  /**
   * Classifies a question as HR-related or off-topic using LLM-based classification. Falls back to
   * keyword detection on timeout or error.
   *
   * @param question the user's question
   * @return classification result with category and confidence
   */
  public GuardrailResult classifyQuestion(String question) {
    try {
      GuardrailResult result =
          CompletableFuture.supplyAsync(() -> classifyWithLlm(question))
              .orTimeout(CLASSIFICATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
              .join();

      log.info(
          "Question classified: hrRelated={}, category={}, confidence={}",
          result.hrRelated(),
          result.category(),
          result.confidence());
      return result;

    } catch (Exception e) {
      log.warn("LLM classification failed, falling back to keyword detection: {}", e.getMessage());
      return classifyWithKeywords(question);
    }
  }

  /**
   * Validates that a question is appropriate for the HR assistant.
   *
   * @param question the user's question
   * @throws HrAssistantException if the question is invalid or off-topic
   */
  public void validateQuestion(String question) {
    if (!StringUtils.hasText(question)) {
      log.warn("Empty question received");
      throw new HrAssistantException(
          HrAssistantException.ErrorCode.INVALID_INPUT, "La question ne peut pas être vide");
    }

    GuardrailResult result = classifyQuestion(question);

    if (!result.hrRelated()) {
      log.info("Off-topic question detected: {}", question);
      throw new HrAssistantException(
          HrAssistantException.ErrorCode.INVALID_INPUT,
          "Cette question ne concerne pas les ressources humaines. Veuillez contacter directement"
              + " le service RH pour des questions non liées aux politiques RH.");
    }

    log.debug("Question validated: {}", question);
  }

  /**
   * Validates LLM output for PII and harmful content.
   *
   * @param output the LLM response text
   * @return validation result indicating safety status
   */
  public OutputGuardrailResult validateOutput(String output) {
    if (output == null || output.isBlank()) {
      log.debug("Output guardrail passed");
      return new OutputGuardrailResult(true, List.of(), null);
    }

    List<String> issues = new ArrayList<>();

    // Check PII patterns
    PII_PATTERNS.forEach(
        (type, pattern) -> {
          if (pattern.matcher(output).find()) {
            log.warn("PII detected in output: {}", type);
            issues.add("PII_DETECTED: " + type);
          }
        });

    // Check harmful content
    String lowerOutput = output.toLowerCase();
    for (String keyword : HARMFUL_KEYWORDS) {
      if (lowerOutput.contains(keyword)) {
        log.warn("Output guardrail blocked response: issues=[HARMFUL_CONTENT: {}]", keyword);
        issues.add("HARMFUL_CONTENT: " + keyword);
      }
    }

    if (issues.isEmpty()) {
      log.debug("Output guardrail passed");
      return new OutputGuardrailResult(true, List.of(), null);
    }

    log.warn("Output guardrail blocked response: issues={}", issues);
    return new OutputGuardrailResult(false, issues, null);
  }

  /** Classifies a question using the LLM with structured output. */
  private GuardrailResult classifyWithLlm(String question) {
    PromptTemplate promptTemplate = PromptTemplate.builder().resource(classificationPrompt).build();

    String promptText =
        promptTemplate.render(Map.of("question", question, "format", outputConverter.getFormat()));

    ChatResponse response =
        chatModel.call(
            new Prompt(
                promptText,
                OllamaChatOptions.builder()
                    .temperature(0.0)
                    .format(outputConverter.getJsonSchema())
                    .build()));

    String content = response.getResult().getOutput().getText();
    ClassificationResponse classification = outputConverter.convert(content);

    return mapToGuardrailResult(classification);
  }

  /** Maps the LLM classification response to a GuardrailResult. */
  private GuardrailResult mapToGuardrailResult(ClassificationResponse classification) {
    HrCategory category = null;
    if (classification.hrRelated() && classification.category() != null) {
      try {
        category = HrCategory.valueOf(classification.category().toUpperCase().trim());
      } catch (IllegalArgumentException e) {
        log.warn(
            "Unknown HR category from LLM: '{}', defaulting to GENERAL_RH",
            classification.category());
        category = HrCategory.GENERAL_RH;
      }
    }

    ConfidenceLevel confidence = ConfidenceLevel.MEDIUM;
    if (classification.confidence() != null) {
      try {
        confidence = ConfidenceLevel.valueOf(classification.confidence().toUpperCase().trim());
      } catch (IllegalArgumentException e) {
        log.warn(
            "Unknown confidence level from LLM: '{}', defaulting to MEDIUM",
            classification.confidence());
      }
    }

    return new GuardrailResult(classification.hrRelated(), category, confidence);
  }

  /** Keyword-based fallback classification. */
  private GuardrailResult classifyWithKeywords(String question) {
    boolean offTopic = isOffTopicByKeywords(question);
    GuardrailResult result =
        new GuardrailResult(
            !offTopic, offTopic ? null : HrCategory.GENERAL_RH, ConfidenceLevel.LOW);

    log.info(
        "Question classified (keyword fallback): hrRelated={}, category={}, confidence={}",
        result.hrRelated(),
        result.category(),
        result.confidence());
    return result;
  }

  /** Keyword-based off-topic detection (legacy fallback). */
  private boolean isOffTopicByKeywords(String question) {
    String lowerQuestion = question.toLowerCase();
    for (String keyword : OFF_TOPIC_KEYWORDS) {
      if (lowerQuestion.contains(keyword)) {
        return true;
      }
    }
    return false;
  }
}
