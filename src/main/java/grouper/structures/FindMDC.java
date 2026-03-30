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
public class FindMDC {

    public FindMDC() {
    }

    private String p_pdx_code;
    private String p_sdx_code;
    private String p_patient_sex;
    private String p_admdate;
    private String p_disdate;
    private String p_bdate;
    private String p_proc_code; 
    
}
