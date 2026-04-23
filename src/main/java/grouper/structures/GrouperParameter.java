/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrouperParameter {

    public GrouperParameter() {
    }

    @JsonProperty("expiretime")
    private String ExpireTime;
    
    @JsonProperty("expiredate")
    private String ExpiredDate;
    
    @JsonProperty("timeofbirth")
    private String TimeOfBirth;
    
    @JsonProperty("prepccl")
    private String prepccl;
    
    @JsonProperty("finalpccl")
    private String finalpccl;
    
    @JsonProperty("warningerror")
    private String warningerror;
    
    @JsonProperty("idseries")
    private String idseries;
    
    @JsonProperty("result_id")
    private String result_id;

    @JsonProperty("claimseries")
    private String claimseries;

    @JsonProperty("pdx")
    private String Pdx;

    @JsonProperty("sdx")
    private String Sdx;

    @JsonProperty("proc")
    private String Proc;

    @JsonProperty("birthDate")
    private String BirthDate;

    @JsonProperty("admissionDate")
    private String AdmissionDate;

    @JsonProperty("dischargeDate")
    private String DischargeDate;

    @JsonProperty("gender")
    private String Gender;

    @JsonProperty("timeAdmission")
    private String TimeAdmission;

    @JsonProperty("timeDischarge")
    private String TimeDischarge;

    @JsonProperty("dischargeType")
    private String DischargeType;

    @JsonProperty("admissionWeight")
    private String AdmissionWeight;

}
