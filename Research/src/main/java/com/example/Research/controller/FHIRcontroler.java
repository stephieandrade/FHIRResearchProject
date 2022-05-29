package com.example.Research.controller;


import com.example.Research.adapter.AdapterImpl;
import com.example.Research.model.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/reports")
public class FHIRcontroler {


    AdapterImpl adapter = new AdapterImpl(); // Instance of Adapter

    /** Validation & transformation JSON format --> FHIR JSON format **/
    @PostMapping("/converter/json") // to save in database
    public ResponseEntity<?> converter(@RequestBody String str) {
        String message = "";

        try {
            return ResponseEntity.ok(adapter.JSONconverter(str));
        }  catch (Exception e) {
            message = "There is an error in the transformation of the file you are trying to convert.";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }

    }

    /** Validation & transformation CSV file format --> FHIR JSON format **/
    @PostMapping("/converter/csv")
    public ResponseEntity<?> converter( @RequestParam("file") MultipartFile file) {
        String message = "";
        String TYPE = ".csv";
            if (!TYPE.equals(file.getContentType())) {
                try {
                    return ResponseEntity.status(HttpStatus.OK).body(adapter.CSVconverter(file));
                }  catch (Exception e) {
                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
                }
            }
        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

}
