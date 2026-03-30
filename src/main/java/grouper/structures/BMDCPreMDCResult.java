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
public class BMDCPreMDCResult {

    public BMDCPreMDCResult() {
    }

    private String ICD10;
    private String MDC_F;
    private String MDC_M;
    private String PDC_F;
    private String PDC_M;

}
