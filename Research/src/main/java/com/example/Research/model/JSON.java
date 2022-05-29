package com.example.Research.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSON {
    private String patientId;
    private List<String> differentialDiagnosis;
    private String confirmedDiagnosis;
    private String test_type;
    private Date test_date;

}
