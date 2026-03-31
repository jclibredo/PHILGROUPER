/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import lombok.Data;

/**
 *
 * @author LAPTOP
 */
@Data
public class BMDC {
    public BMDC() {
    }
    private String icd10;
    private String mdc_f;
    private String pdc_f;
    private String mdc_m;
    private String pdc_m;
}
