package com.example.Research.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CSV {

        private String stud_num;
        private String pat_id;
        private String test_type;

        private String test_date;

        private String dif_diag_1;
        private String dif_diag_2;
        private String dif_diag_3;

        private String final_diag;


        public CSV(String stud_num, String pat_id, String test_type,String test_date, String dif_diag_1, String dif_diag_2, String dif_diag_3, String final_diag) {
            this.stud_num = stud_num;
            this.pat_id = pat_id;
            this.test_type = test_type;
            this.dif_diag_1 = dif_diag_1;
            this.dif_diag_2 = dif_diag_2;
            this.dif_diag_3 = dif_diag_3;
            this.final_diag = final_diag;
            this.test_date = test_date;
        }

    }
