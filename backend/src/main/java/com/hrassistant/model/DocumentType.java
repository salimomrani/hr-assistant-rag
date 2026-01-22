package com.hrassistant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DocumentType {
    PDF("application/pdf", ".pdf"),
    TXT("text/plain", ".txt");

    private final String mimeType;
    private final String extension;

    public static DocumentType fromExtension(String filename) {
        if (filename == null) {
            return null;
        }
        String lower = filename.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> lower.endsWith(type.getExtension()))
                .findFirst()
                .orElse(null);
    }
}
