package projects.uscore


import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Vital Sign Observation Pediatric BMI for Age.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-pediatric-bmi-for-age.html
 *
 * hints:
 * - Observation are specified by LOINC codes.
 * - Units are specified by UCUM codes.
 *
 * Note: The mapping requires labor methods, labor values and units defined in CXX that correspond to the specification of the
 * profile! For more information, see project README.md
 *
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 */
observation {
  if ("US_CORE_PEDIATRIC_BMI_FOR_AGE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-bmi")
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "59576-9"
    }
  }

  final def laborFindingLaborValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "PEDIATRIC_BMI_FOR_AGE" }

  valueQuantity {
    value = laborFindingLaborValue[NUMERIC_VALUE]
    unit = laborFindingLaborValue[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
        .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
    system = "http://unitsofmeasure.org"
    code = "%"
  }
}


