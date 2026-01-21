package com.hrassistant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
        if (lower.endsWith(".pdf")) {
            return PDF;
        } else if (lower.endsWith(".txt")) {
            return TXT;
        }
        return null;
    }
}
