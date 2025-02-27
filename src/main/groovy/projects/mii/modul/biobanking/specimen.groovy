package projects.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.AbstractIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample
/**
 * Represented by a CXX SAMPLE
 * Codings are custumized in CXX. Therefore, the code system is unknown. In this example the usage of snomed-ct is assumed to be used.
 * If other codings are used in the local CXX system, the code systems must be adjusted.
 * TODO: NOTE: The script was written while the corresponding FHIR profile on simplifier.net was still in draft state. Changes in the profile might require adjustments in the script.
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
specimen {
  id = "Sample/" + context.source[sample().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileSpecimenBioprobe"
  }

  if (context.source[abstractSample().episode()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose"
      valueReference {
        reference = "Diagnosis/" + context.source[sample().episode().id()]
      }
    }
  }

  if (context.source[sample().organisationUnit()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/VerwaltendeOrganisation"
      valueReference {
        reference = "OrganisationUnit/" + context.source[sample().organisationUnit().id()]
      }
    }
  }


  context.source[sample().idContainer()].each { final def idObj ->
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
          display = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.NAME] as String
        }
      }
      value = idObj[AbstractIdContainer.PSN]
    }
  }

  // Specimen status is customized in CXX. Exact meaning depends on implementation in CXX. Here, it is assumed that the codes of the codesystem
  // are implemented in CXX.
  status = mapSpecimenStatus(context.source[sample().sampleStatus().code()] as String)

  if (context.source[sample().sampleType()]) {
    type {
      // SPREC is implemented in CXX.
      coding {
        system = "https://doi.org/10.1089/bio.2017.0109/type-of-sample"
        code = context.source[sample().sampleType().sprecCode()]
      }
    }
  }
  subject {
    reference = "Patient/" + context.source[sample().patientContainer().id()]
  }

  receivedTime {
    date = context.source[sample().receiptDate()]?.getAt(PrecisionDate.DATE)
  }

  if (context.source[sample().parent()]) {
    parent {
      reference = "Sample/" + context.source[sample().parent().id()]
    }
  }

  collection {
    collectedDateTime = context.source[sample().samplingDate().date()]
    if (context.source[sample().orgSample()]) {
      bodySite {
        //Organs are specified user-defined in CXX. sct coding only applies, when used for coding in CXX
        coding {
          system = "http://snomed.info/sct"
          code = context.source[sample().orgSample().code()]
          display = context.source[sample().orgSample().nameMultilingualEntries()].find { final def entry ->
            "de" == entry[MultilingualEntry.LANG]
          }[MultilingualEntry.VALUE]
        }
      }
    }
  }

  if (context.source[sample().sampleLocation()]) {
    processing {
      procedure {
        coding = [new Coding("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/CodeSystem/Probenlagerung",
            "LAGERUNG",
            "Lagerung einer Probe")]
      }
    }
    timeDateTime = context.source[sample().repositionDate().date()]
  }

  if (context.source[sample().receptable()]) {
    container {
      type {
        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/long-term-storage"
          code = context.source[sample().receptable().sprecCode()]
        }
      }
      capacity {
        value = context.source[sample().receptable().size()]
        unit = context.source[sample().receptable().volume()]
      }
      specimenQuantity {
        value = context.source[sample().restAmount().amount()]
        unit = context.source[sample().restAmount().unit()]
      }
      additiveReference {
        if (context.source[sample().sprecPrimarySampleContainer()]){
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().sprecPrimarySampleContainer().sprecCode()]
            }
          }
        }
        if (context.source[sample().stockType()]){
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().stockType().sprecCode()]
            }
          }
        }
      }
    }
  }

  note {
    text = context.source[sample().note()] as String
  }
}

static Specimen.SpecimenStatus mapSpecimenStatus(final String specimenStatus){
  switch (specimenStatus){
    case "available" : return Specimen.SpecimenStatus.AVAILABLE
    case "unavailable" : return Specimen.SpecimenStatus.UNAVAILABLE
    case "unsatisfactory": return Specimen.SpecimenStatus.UNSATISFACTORY
    case "entered-in-error": return Specimen.SpecimenStatus.ENTEREDINERROR
    default: return Specimen.SpecimenStatus.NULL
  }
}


