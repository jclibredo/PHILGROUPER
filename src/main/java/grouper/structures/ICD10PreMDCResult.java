/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author MINOSUN
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ICD10PreMDCResult {

    public ICD10PreMDCResult() {
    }
    
//    @JsonProperty("code")
    private String Code;
    
//    @JsonProperty("mdc")
    private String MDC;
    
//    @JsonProperty("pdc")
    private String PDC;
    
//    @JsonProperty("cc")
    private String CC;
    
//    @JsonProperty("maincc")
    private String MainCC;
    
//    @JsonProperty("ccrow")
    private String CCRow;
    
//    @JsonProperty("hiv_ax")
    private String HIV_AX;
    
//    @JsonProperty("sex")
    private String Sex;
    
//    @JsonProperty("accpdx")
    private String AccPDX;
    
//    @JsonProperty("ageduse")
    private String AgeDUse;
    
//    @JsonProperty("agemin")
    private String AgeMin;
    
//    @JsonProperty("agemax")
    private String AgeMax;
    
//    @JsonProperty("agedmin")
    private String AgeDMin;
    
//    @JsonProperty("trauma")
    private String Trauma;

}
