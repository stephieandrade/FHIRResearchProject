package com.example.Research;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidateFHIR {

    FhirContext ctx = FhirContext.forR4();
    FhirValidator validator = ctx.newValidator();
    List<JSONObject> jsonObjects = new ArrayList<>();

    public String validate(List<DiagnosticReport> diagnosticReports) {

        for (DiagnosticReport diagnosticReport: diagnosticReports
             ) {
            ValidationResult result = validator.validateWithResult(diagnosticReport);

            /** SERIALIZATION METHOD **/
            IParser parser = ctx.newJsonParser();

            parser.setPrettyPrint(true); // Indent the output

            String serialized = parser.encodeResourceToString(diagnosticReport); // Serialize it

            JSONObject json = new JSONObject(serialized);

            /** RETURN DIAGNOSTIC AS FHIR FORMAT IF VALIDATION WAS SUCCESFULL **/
            if (result.isSuccessful()) {

                jsonObjects.add(json);

            } else {

                /** RETURN RESULT OF VALIDATION IF IT WAS UNSUCCESFULL **/
                return result.toString(); // Return result response to check which attributes aren't in the correct format
            }
        }

        return jsonObjects.toString();

    }





}
