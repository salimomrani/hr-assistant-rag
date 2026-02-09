package com.hrassistant.service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for parsing and building source sections in RAG responses. Sources are appended to
 * responses in format: "\n\n\n\n**Sources:**\n- source1\n- source2"
 */
public final class SourceParsingUtil {

  static final String SOURCES_MARKER = "**Sources:**";

  private SourceParsingUtil() {}

  /**
   * Extracts source names from a response containing a sources section.
   *
   * @param response the full response text
   * @return list of source names, empty if no sources found
   */
  public static List<String> extractSources(String response) {
    if (response == null || !response.contains(SOURCES_MARKER)) {
      return List.of();
    }

    String sourcesSection =
        response.substring(response.indexOf(SOURCES_MARKER) + SOURCES_MARKER.length());
    return sourcesSection
        .lines()
        .map(String::trim)
        .filter(line -> line.startsWith("- "))
        .map(line -> line.substring(2).trim())
        .collect(Collectors.toList());
  }

  /**
   * Removes the sources section from a response.
   *
   * @param response the full response text
   * @return response without the sources section
   */
  public static String removeSourcesSection(String response) {
    if (response == null || !response.contains(SOURCES_MARKER)) {
      return response;
    }
    return response.substring(0, response.indexOf(SOURCES_MARKER)).trim();
  }

  /**
   * Builds the sources text to append at the end of a response.
   *
   * @param sources list of source document names
   * @return formatted sources text, empty string if no sources
   */
  public static String buildSourcesText(List<String> sources) {
    if (sources == null || sources.isEmpty()) {
      return "";
    }
    return "\n\n\n\n"
        + SOURCES_MARKER
        + "\n"
        + sources.stream().map(source -> "- " + source).collect(Collectors.joining("\n"));
  }
}
