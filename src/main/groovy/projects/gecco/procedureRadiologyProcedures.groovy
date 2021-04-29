package projects.gecco


import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.MedProcedure
import de.kairos.fhir.centraxx.metamodel.OpsCatalog
import de.kairos.fhir.centraxx.metamodel.OpsEntry
import org.hl7.fhir.r4.model.Annotation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX MedProcedure
 * @author Lukas Reinert
 * @since CXX.v.3.18.1
 */
procedure {
  context.source[patient().patientContainer().medProcedures() as String].each { final opsCode ->
    if ((opsCode[MedProcedure.OPS_ENTRY][OpsEntry.CODE] as String).matches("^3-*")) {

      id = "RadiologyProcedures/" + opsCode[MedProcedure.ID] as String

      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/radiology-procedures"
      }

      extension {
        url = "https://simplifier.net/packages/de.medizininformatikinitiative.kerndatensatz.prozedur/1.0.6/files/351664"
        valueDateTime = opsCode[MedProcedure.CREATIONDATE]
      }

      status = "unknown"

      category {
        coding {
          system = "http://snomed.info/sct"
          code = "103693007"
        }
      }

      code {
        coding {
          system = "http://fhir.de/CodeSystem/dimdi/ops"
          version = opsCode[MedProcedure.OPS_ENTRY][OpsEntry.CATALOGUE][OpsCatalog.CATALOGUE_VERSION]
          code = opsCode[MedProcedure.OPS_ENTRY][OpsEntry.CODE] as String
        }
      }

      subject {
        reference = "Patient/" + context.source[patient().patientContainer().id()]
      }

      if (opsCode[MedProcedure.EPISODE][Episode.ID]) {
        encounter {
          reference = "Encounter/" + opsCode[MedProcedure.EPISODE][Episode.ID] as String
        }
      }

      performedDateTime {
        performedDateTime = opsCode[MedProcedure.OPS_ENTRY][OpsEntry.CREATIONDATE] as String
      }

      if (opsCode[MedProcedure.COMMENTS]) {
        final Annotation annotation = new Annotation()
        annotation.setText(opsCode[MedProcedure.COMMENTS] as String)
        note.add(annotation)
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
