package com.hrassistant.model.analytics;

import lombok.Builder;

@Builder
public record DocumentReference(String documentId, String documentName, long referenceCount) {}
