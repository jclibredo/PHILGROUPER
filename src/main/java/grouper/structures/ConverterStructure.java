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
public class ConverterStructure {

    public ConverterStructure() {
    }

    private String claimseries;
    private String rvs;
    private String icd9cm;
    private String category;

}
