/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class NClaimsData {
    public NClaimsData(){
    }
    private String dateofBirth;
    private String admissionDate;
    private String dischargeDate;
    private String gender;
    private String dischargeType;
    private String expiredDate;
    private String expiredTime;
    private String timeAdmission;
    private String timeDischarge;
}
