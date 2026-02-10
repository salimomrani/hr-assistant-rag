package com.hrassistant.model.analytics;

import java.time.LocalDate;

public record DailyCount(LocalDate date, long count) {}
