package com.hrassistant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Predefined HR domain categories for question classification. */
@Getter
@RequiredArgsConstructor
public enum HrCategory {
  CONGES_ABSENCES("Congés / Absences"),
  REMUNERATION_PAIE("Rémunération / Paie"),
  FORMATION_DEVELOPPEMENT("Formation / Développement"),
  AVANTAGES_SOCIAUX("Avantages sociaux"),
  CONTRAT_CONDITIONS("Contrat / Conditions de travail"),
  RECRUTEMENT_INTEGRATION("Recrutement / Intégration"),
  REGLEMENT_DISCIPLINE("Règlement intérieur / Discipline"),
  GENERAL_RH("Général RH");

  private final String displayLabel;
}
