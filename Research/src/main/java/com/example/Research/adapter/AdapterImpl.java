package com.example.Research.adapter;

import com.example.Research.ValidateFHIR;
import com.example.Research.model.CSV;
import com.example.Research.model.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class AdapterImpl extends ValidateFHIR {

    ObjectMapper objectMapper = new ObjectMapper(); // Create ObjectMapper instance

    /** Creation of Hashmap of denominations and SNOMED code *
     *
     * Both CSV and JSON files had different denominations of the same Diagnosis confirmation
     * so I created a 'codes' Hashmap with the corresponding SNOMED code,
     * and then mapped the code against 'codesCT' Hashmap which had both the code the correct
     * SNOMED denomination.
     *
     * */

    public List<String> resolveSNOMEDCode(String str){
        HashMap<String, Integer> codes = new HashMap<>();
        codes.put("gitelman syndrome", 707756004 );
        codes.put("IgA glomerulonephritis", 236407003 );
        codes.put("Gitelman's syndrome", 707756004 );
        codes.put("distal renal tubular acidosis", 236461000 );
        codes.put("familial nephrotic syndrome", 48796009 ); //Congenital nephrotic syndrome


        HashMap<Integer, String> codesCT = new HashMap<>();
        codesCT.put( 707756004, "Gitelman syndrome" );
        codesCT.put(236407003 , "IgA glomerulonephritis"  );
        codesCT.put(236461000, "Distal renal tubular acidosis" );
        codesCT.put( 48796009, "Congenital nephrotic syndrome"  );

        Integer getCode = codes.get(str);
        List<String> code = new ArrayList<>();
        if(codesCT.containsKey(getCode)){
            code.add(String.valueOf(getCode));
            code.add(codesCT.get(getCode));
        }else{
            code.add("Not found");
            code.add("Not found");
        }
        return code;
    }

    /** Creation of Hashmap of denominations and LOINC code
     *
     * Both CSV and JSON files had different denominations of the same test_types
     * so I created a 'codes' Hashmap with the corresponding LOINC code,
     * and then mapped the code against 'codesCT' Hashmap which had both the code the correct
     * LOINC denomination.
     *
     * **/

    public String resolveLOINCCode(String str){
        HashMap<String, String> codes = new HashMap<>();
        codes.put("exome", "LP248469-1" );
        codes.put("whole exome", "LP248469-1" );
        codes.put("gene panel", "LP94241-4" );
        codes.put("panel", "LP94241-4" );
        codes.put("wgs", "LP248470-9" );


        HashMap<String, String>  codesCT = new HashMap<>();
        codesCT.put( "LP248469-1", "Whole exome sequence analysis " );
        codesCT.put("LP94241-4" , "Genetic analysis discrete result panel"  );
        codesCT.put("LP248470-9" , "Whole genome sequence analysis"  );

        String getCode = codes.get(str);
        String code = "";
        if(codesCT.containsKey(getCode)){
            code = codesCT.get(getCode);
        }else{
            code = "Not found";
        }
        return code;

    }

    public String CSVconverter(MultipartFile multipart) throws IOException, ParseException {

        /** CSV to POJO Converter **/

        File file = new File(System.getProperty("java.io.tmpdir")+"/"+multipart.getOriginalFilename());
        multipart.transferTo(file);
        String delimiter = ",";
        List<CSV> CSVreceived = new ArrayList<>();

        Scanner read = null;

        read = new Scanner(file);

        String line = read.nextLine();

        while(read.hasNextLine()) {

            line = read.nextLine();

            String[] userData = line.split(delimiter, -1);

            CSVreceived.add(new CSV(userData[0], userData[1],userData[2], userData[3], userData[4], userData[5], userData[6], userData[7]));
        }

        /** POJO to FHIR Format Converter **/

        List<DiagnosticReport> diagnosticReports = new ArrayList<>();

        for ( CSV data: CSVreceived
        ) {


            org.hl7.fhir.r4.model.DiagnosticReport diagnosticReport = new org.hl7.fhir.r4.model.DiagnosticReport(); //Create FHIR diagnostic Report

            diagnosticReport.setImplicitRules("https://www.hl7.org/fhir/diagnosticreport.html"); //Add FHIR resource

            diagnosticReport.setLanguage("english"); //LANGUAGE

            CodeableConcept codeableConcept = new CodeableConcept();

            List<String> CTcode = resolveSNOMEDCode(data.getFinal_diag());

            codeableConcept.addCoding(new org.hl7.fhir.r4.model.Coding().setDisplay("Differential Diagnosis").setSystem("http://snomed.info/sct")); //DIFFERENTIAL DIAGNOSIS - NEEDED

            diagnosticReport.setCode(codeableConcept);

            CodeableConcept codeableConceptCategory = new CodeableConcept();
            List<CodeableConcept> codeableConceptsCategory = new ArrayList<>();
            codeableConceptsCategory.add(codeableConceptCategory);
            codeableConceptCategory.addCoding(new org.hl7.fhir.r4.model.Coding().setDisplay(CTcode.get(1)).setCode(CTcode.get(0)).setSystem("http://snomed.info/sct"));

            diagnosticReport.setCategory(codeableConceptsCategory);

            if (data.getFinal_diag().isEmpty()) {
                diagnosticReport.setConclusion("Not found"); //CONFIRMED DIAGNOSIS - NEEDED
                diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.UNKNOWN);
            } else {

                diagnosticReport.setConclusion(CTcode.get(1));//CONFIRMED DIAGNOSIS - NEEDED
                diagnosticReport.setStatus(org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus.FINAL); //Established this as final result
            }

            String[] formattedDate = data.getTest_date().split("/");

            for (int i = 0; i < formattedDate.length; i++) {
                if(formattedDate[i].length() < 2){
                    formattedDate[i] = "0" + formattedDate[i];
            }
            }

            String str = String.join("-", formattedDate);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");

            Date date = formatter.parse(str);

            diagnosticReport.setIssued(date); //TEST_DATE

            IdType idType = new IdType();
            idType.setValue("1");

            Reference referenceSubject = new Reference();
            referenceSubject.setId(data.getPat_id());
            diagnosticReport.setSubject(referenceSubject); //PATIENID

            String testCodeLOINC = resolveLOINCCode(data.getTest_type());

            Reference reference = new Reference();
            reference.setReference(testCodeLOINC);
            List<Reference> arrayList = new ArrayList<>();
            arrayList.add(reference);
            diagnosticReport.setSpecimen(arrayList); //TEST_TYPE - NEEDED
            diagnosticReports.add(diagnosticReport);



        }

        /** Validation of Diagnostic Resource **/

        return validate(diagnosticReports);

    }

    public String JSONconverter(String jsonObject) throws IOException {

        /** JSON to POJO Converter **/

        List<JSON> jsoNreceiveds = objectMapper.readValue(jsonObject, new TypeReference<List<JSON>>(){});

        /** POJO to FHIR Format Converter **/

        List<DiagnosticReport> diagnosticReports = new ArrayList<>();
        for ( JSON data: jsoNreceiveds) {

            org.hl7.fhir.r4.model.DiagnosticReport diagnosticReport = new org.hl7.fhir.r4.model.DiagnosticReport(); //Create FHIR diagnostic Report

            diagnosticReport.setImplicitRules("https://www.hl7.org/fhir/diagnosticreport.html"); //Add FHIR resource

            diagnosticReport.setLanguage("english"); //LANGUAGE

            List<String> CTcode = resolveSNOMEDCode(data.getConfirmedDiagnosis());

            CodeableConcept codeableConcept = new CodeableConcept();

            codeableConcept.addCoding(new org.hl7.fhir.r4.model.Coding().setDisplay("Differential Diagnosis").setSystem("http://snomed.info/sct")); //DIFFERENTIAL DIAGNOSIS - NEEDED

            diagnosticReport.setCode(codeableConcept);


            CodeableConcept codeableConceptCategory = new CodeableConcept();
            List<CodeableConcept> codeableConceptsCategory = new ArrayList<>();
            codeableConceptsCategory.add(codeableConceptCategory);
            codeableConceptCategory.addCoding(new org.hl7.fhir.r4.model.Coding().setDisplay(CTcode.get(1)).setCode(CTcode.get(0)).setSystem("http://snomed.info/sct"));

            diagnosticReport.setCategory(codeableConceptsCategory);

            if (data.getConfirmedDiagnosis().isEmpty()) {
                diagnosticReport.setConclusion("Not found"); //CONFIRMED DIAGNOSIS - NEEDED
                diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.UNKNOWN); //Established this as unknown result
            } else {

                diagnosticReport.setConclusion(CTcode.get(1));//CONFIRMED DIAGNOSIS - NEEDED
                diagnosticReport.setStatus(org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus.FINAL); //Established this as final result
            }

            diagnosticReport.setIssued(data.getTest_date()); //TEST_DATE

            IdType idType = new IdType();
            idType.setValue("1");

            Reference referenceSubject = new Reference();
            referenceSubject.setId(data.getPatientId());
            diagnosticReport.setSubject(referenceSubject); //PATIENID

            String testCodeLOINC = resolveLOINCCode(data.getTest_type());

            Reference reference = new Reference();
            reference.setReference(testCodeLOINC);
            List<Reference> arrayList = new ArrayList<>();
            arrayList.add(reference);
            diagnosticReport.setSpecimen(arrayList); //TEST_TYPE - NEEDED

            diagnosticReports.add(diagnosticReport);

        }

        /** Validation of Diagnostic Resource **/

        return validate(diagnosticReports);

    }

}
