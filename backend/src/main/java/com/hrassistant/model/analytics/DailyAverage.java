package com.hrassistant.model.analytics;

import java.time.LocalDate;

public record DailyAverage(LocalDate date, double averageMs) {}
