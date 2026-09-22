/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.methods.validation.GenderConfictValidation;
import grouper.methods.validation.GetDA;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.GetPCOM;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class ValidateFindMDC {

    private final Logger logger = LogManager.getLogger(ValidateFindMDC.class);
    private final Utility utility = new Utility();
    public ValidateFindMDC() {
    }
    public DRGWSResult validateFindMDC(
            final DataSource datasource,
            final String schemaName,
            final GrouperParameter grouperParameter) {

        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGOutput drgResult = new DRGOutput();

        try {
            // 1. Shallow clone the parameter context
            GrouperParameter newGrouperParam = cloneGrouperParameter(grouperParameter);
            drgResult.setClaimseries(grouperParameter.getClaimseries());
            List<String> procList = Arrays.stream(grouperParameter.getProc().split(","))
                    .collect(Collectors.toList());
            List<String> combiCode = new ArrayList<>();
            // 2. Optimized Cross-Join Combination Check
            GetPCOM pcomService = new GetPCOM();
            for (int y = 0; y < procList.size(); y++) {
                String dataA = procList.get(y).replace(">1", "").trim();
                for (int w = 0; w < procList.size(); w++) {
                    String dataB = procList.get(w).replace(">1", "").trim();
                    DRGWSResult pcomResult = pcomService.GetPCOM(datasource, schemaName, dataA, dataB);
                    if (pcomResult.isSuccess()) {
                        combiCode.add(pcomResult.getResult());
                    }
                }
            }
            // 3. Process Asterisk / Diagnostic Alignment (DA) Mapping
            List<String> sDxList = Arrays.stream(grouperParameter.getSdx().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            List<Boolean> asteriskMask = new ArrayList<>();
            GetDA daService = new GetDA();
            sDxList.stream().map((sdx) -> daService.GetDA(datasource, schemaName, grouperParameter.getPdx(), sdx)).forEachOrdered((gDAResult) -> {
                asteriskMask.add(gDAResult.isSuccess());
            });
            SDxPDx swapping = new SDxPDx();
            int indexNumber = asteriskMask.indexOf(true);
            if (indexNumber != -1) {
                sDxList.set(indexNumber, grouperParameter.getPdx());
                String newListSDx = sDxList.stream().collect(Collectors.joining(","));
                swapping.setNewsdx(newListSDx);
                List<String> newPrimary = Arrays.asList(grouperParameter.getSdx().split(","));
                swapping.setNewpdx(newPrimary.get(indexNumber));
            } else {
                swapping.setNewpdx(grouperParameter.getPdx());
                if (!grouperParameter.getSdx().isEmpty()) {
                    swapping.setNewsdx(grouperParameter.getSdx());
                }
            }
            // 4. Run Standard Clinical Validations
            DRGWSResult getIcd10Result = new GetICD10PreMDC().GetICD10PreMDC(datasource, schemaName, swapping.getNewpdx());
            DRGWSResult getSexConflictResult = new GenderConfictValidation().GenderConfictValidation(datasource, schemaName, swapping.getNewpdx(), grouperParameter.getGender());
            int calculatedAge = utility.ComputeYear(grouperParameter.getBirthDate(), grouperParameter.getAdmissionDate());
            //PROCESS PROC LIST
            String newProcList = Stream.concat(
                    combiCode.stream(),
                    Arrays.stream(grouperParameter.getProc().split(","))
            )
                    .filter(s -> s != null && !s.trim().isEmpty()) // Optional: removes empty strings
                    .distinct() // Keeps only unique values
                    .collect(Collectors.joining(","));
            GetValidatedPreMDC getPreMDC = new GetValidatedPreMDC();
            if (!getIcd10Result.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Invalid PDx : " + grouperParameter.getPdx());
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (grouperParameter.getBirthDate() == null || grouperParameter.getBirthDate().isEmpty()) {
                drgResult.setDRG("26539");
                drgResult.setDC("2653");
                drgResult.setDRGName("Ungroupable, invalid age due to missing birthdate");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (calculatedAge > 124) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Ungroupable, invalid age more than 124 years old");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (!getSexConflictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName(grouperParameter.getPdx() + " PDx having conflict with sex");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (indexNumber == -1) {
                // If there's no diagnostic swap configuration found
                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(getIcd10Result.getResult(), ICD10PreMDCResult.class);
                if (!"Y".equalsIgnoreCase(icd10Result.getAccPDX())) {
                    drgResult.setDRG("26519");
                    drgResult.setDC("2651");
                    drgResult.setDRGName("Inappropriate PDx " + grouperParameter.getPdx());
                    result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                    result.setSuccess(true);
                } else {
                    newGrouperParam.setPdx(grouperParameter.getPdx());
                    newGrouperParam.setProc(newProcList);
                    newGrouperParam.setSdx(grouperParameter.getSdx());
                    result = getPreMDC.getValidatedPreMDC(datasource, schemaName, newGrouperParam);
                }
            } else {
                // Execute standard pre-MDC mapping sequence with swapped codes  newProcList
                newGrouperParam.setPdx(swapping.getNewpdx());
                newGrouperParam.setProc(newProcList);
                newGrouperParam.setSdx(swapping.getNewsdx());
                result = getPreMDC.getValidatedPreMDC(datasource, schemaName, newGrouperParam);
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.error("Error in Find MDC Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    private GrouperParameter cloneGrouperParameter(GrouperParameter src) {
        GrouperParameter target = new GrouperParameter();
        target.setAdmissionDate(src.getAdmissionDate());
        target.setAdmissionWeight(src.getAdmissionWeight());
        target.setBirthDate(src.getBirthDate());
        target.setDischargeDate(src.getDischargeDate());
        target.setDischargeType(src.getDischargeType());
        target.setExpireTime(src.getExpireTime());
        target.setExpiredDate(src.getExpiredDate());
        target.setTimeAdmission(src.getTimeAdmission());
        target.setTimeDischarge(src.getTimeDischarge());
        target.setTimeOfBirth(src.getTimeOfBirth());
        target.setWarningerror(src.getWarningerror());
        target.setGender(src.getGender());
        target.setClaimseries(src.getClaimseries());
        return target;
    }

    public static class SDxPDx {

        private String newpdx;
        private String newsdx;

        public String getNewpdx() {
            return newpdx;
        }

        public void setNewpdx(String newpdx) {
            this.newpdx = newpdx;
        }

        public String getNewsdx() {
            return newsdx;
        }

        public void setNewsdx(String newsdx) {
            this.newsdx = newsdx;
        }
    }
}
