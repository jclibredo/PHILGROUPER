/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.DRG;
import grouper.methods.validation.DRGDetermination;
import grouper.methods.validation.GetFDRG;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.FDRG;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetPCCLResult {
    
    public GetPCCLResult() {
    }
    private final Utility utility = new Utility();
    
    public DRGWSResult GetPCCLJava(
            final DataSource datasource,
            final String schemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRGDetermination drgDetermination = new DRGDetermination();
            DRG checkDRG = new DRG();
            GetFDRG getFdrg = new GetFDRG();
            String sdxdcfinder = drgResult.getSDXFINDER() != null ? drgResult.getSDXFINDER() : "";
            if (drgResult.getDRG() == null) {
//                drgResult.setDRGName("Grouper Error");
//                System.out.println();
//                if (getDc.GetDC(datasource, schemaName, dc).isSuccess()) {
////                if (utility.isValidDCList(drgResult.getDC())) {
//                    String fallbackDrg = dc + "9";
//                    drgResult.setDRG(fallbackDrg);
//                    drgResult.setPrepccl("9");
//                    drgResult.setFinalpccl("9");
//                    DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, fallbackDrg);
//                    drgResult.setDRGName(drgCheckResult.getMessage());
//                } else {
                String getPrePccl = drgDetermination.PCCLDetermination(
                        datasource,
                        schemaName,
                        grouperparameter.getSdx(),
                        sdxdcfinder,
                        grouperparameter.getPdx(),
                        drgResult.getDC());
                
                String preDrg = drgResult.getDC() + getPrePccl;
                drgResult.setPrepccl(getPrePccl);
                drgResult.setPredrg(preDrg);
                DRGWSResult getFdrgResult = getFdrg.GetFDRG(datasource, schemaName, drgResult.getDC(), preDrg);
                if (getFdrgResult.isSuccess()) {
                    FDRG fdrg = utility.objectMapper().readValue(getFdrgResult.getResult(), FDRG.class);
                    String finalDrg = fdrg.getFdrg() == null ? fdrg.getPdrg() : fdrg.getFdrg();
                    String finalPccl = fdrg.getFpccl() == null ? fdrg.getPpccl() : fdrg.getFpccl();
                    drgResult.setDRG(finalDrg);
                    drgResult.setFinalpccl(finalPccl);
                    drgResult.setDC(fdrg.getDc());
                    DRGWSResult getDrg = checkDRG.DRG(datasource, schemaName, fdrg.getDc(), finalDrg);
                    if (getDrg.isSuccess()) {
                        DRGOutput mapDrg = utility.objectMapper().readValue(getDrg.getResult(), DRGOutput.class);
                        drgResult.setWTLOS(mapDrg.getWTLOS() != null ? mapDrg.getWTLOS() : "");
                        drgResult.setRW(mapDrg.getRW() != null ? mapDrg.getRW() : "");
                        drgResult.setMDF(mapDrg.getMDF() != null ? mapDrg.getMDF() : "");
                        drgResult.setOT(mapDrg.getOT() != null ? mapDrg.getOT() : "");
                    }
                    drgResult.setDRGName(getDrg.getMessage());
                } else {
                    drgResult.setDRG(drgResult.getDC() + getPrePccl);
                    drgResult.setFinalpccl("X");
                    drgResult.setDRGName(getFdrgResult.getMessage());
                }
                // Execute query ONCE and store result
//                DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, constructedDrg);
//                if (drgCheckResult.isSuccess()) {
//                    drgResult.setDRGName(drgCheckResult.getMessage());
//                    drgResult.setFinalpccl(getLastChar(constructedDrg));
//                } else {
//                    DRGWSResult drgValues = validatePCCL.ValidatePCCL(datasource, schemaName, dc, constructedDrg);
//                    if (drgValues.isSuccess()) {
//                        String validDrgCode = dc + drgValues.getResult();
//                        drgResult.setDRG(validDrgCode);
//                        DRGWSResult drgNames = checkDRG.DRG(datasource, schemaName, dc, validDrgCode);
//                        if (drgNames.isSuccess()) {
//                            drgResult.setDRGName(drgNames.getMessage());
//                        }
//                        drgResult.setFinalpccl(getLastChar(validDrgCode));
//                    } else {
//                        drgResult.setPrepccl("X");
//                        drgResult.setFinalpccl("X");
//                        drgResult.setDRGName("DRG code grouper provide not exist in the library");
//                    }
//
//                }
//                }
            } else {
                drgResult.setDRGName(checkDRG.DRG(datasource, schemaName, drgResult.getDC(), drgResult.getDRG()).getMessage());
            }

            //MAP PATIENT ADMISSION DETAILS
            int ageDay = utility.ComputeDay(
                    grouperparameter.getBirthDate(),
                    grouperparameter.getAdmissionDate());
            
            int ageYear = utility.ComputeYear(
                    grouperparameter.getBirthDate(),
                    grouperparameter.getAdmissionDate());
            
            int computedLos = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge()));
            
            int computedTime = utility.ComputeTime(
                    grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge()));
            
            int computedYearLos = utility.ComputeYear(
                    grouperparameter.getAdmissionDate(),
                    grouperparameter.getDischargeDate());
            String YearOfAge = (ageYear > 1) ? "YEARS" : "YEAR";
            String DayOfAge = (ageDay > 1) ? "DAYS" : "DAY";
            String YearOfLos = (computedYearLos > 1) ? "YEARS" : "YEAR";
            String DayOfLos = (computedLos > 1) ? "DAYS" : "DAY";
            String HourOfLos = (computedTime > 1) ? "HOURS" : "HOUR";
            drgResult.setAge(DayOfAge + "(" + ageDay + ") " + YearOfAge + "(" + ageYear + ")");
            drgResult.setLos(YearOfLos + "(" + computedYearLos + ") " + DayOfLos + "(" + computedLos + ") " + HourOfLos + "(" + computedTime + ")");
            drgResult.setPdx(grouperparameter.getPdx());
            drgResult.setSdx(grouperparameter.getSdx());
            drgResult.setProc(grouperparameter.getProc());
            drgResult.setSex(grouperparameter.getGender());
            drgResult.setDischargetype(grouperparameter.getDischargeType());
            drgResult.setRvs(grouperparameter.getRvs());
            
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        } catch (Exception ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        }
        
        return result;
    }

    /**
     * Safe helper to extract the last character of a string
     */
    private String getLastChar(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.substring(str.length() - 1);
    }
}
