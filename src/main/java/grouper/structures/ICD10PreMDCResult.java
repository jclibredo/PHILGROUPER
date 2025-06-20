/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
public class ICD10PreMDCResult {

    public ICD10PreMDCResult() {
    }
    private String Code;
    private String MDC;
    private String PDC;
    private String CC;
    private String MainCC;
    private String CCRow;
    private String HIV_AX;
    private String Sex;
    private String AccPDX;
    private String AgeDUse;
    private String AgeMin;
    private String AgeMax;
    private String AgeDMin;
    private String Trauma;

}
