/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ProcessGrouperParameter {

    public ProcessGrouperParameter() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ProcessGrouperParameter(
            final DataSource datasource,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            DRGOutput drgresult = utility.DRGOutput();
            GrouperParameter grouper = utility.GrouperParameter();
            //======================== TIME FORMAT CONVERTER ==============================
            grouper.setResult_id(grouperparameter.getResult_id());
            grouper.setExpireTime(grouperparameter.getExpireTime());
            grouper.setTimeAdmission(grouperparameter.getTimeAdmission());
            grouper.setAdmissionDate(grouperparameter.getAdmissionDate());
            grouper.setTimeDischarge(grouperparameter.getTimeDischarge());
            grouper.setDischargeDate(grouperparameter.getDischargeDate());
            grouper.setExpiredDate(grouperparameter.getExpiredDate());
            grouper.setBirthDate(grouperparameter.getBirthDate());
            grouper.setClaimseries(grouperparameter.getClaimseries());
            grouper.setGender(grouperparameter.getGender());
            grouper.setIdseries(grouperparameter.getIdseries());
            grouper.setPdx(grouperparameter.getPdx());
            //CLEANING PROC DATA
            if (!grouperparameter.getProc().trim().isEmpty()) {
                LinkedList<String> newprocList = new LinkedList<>();
                List<String> procList = Arrays.asList(grouperparameter.getProc().split(","));
                for (int m = 0; m < procList.size(); m++) {
                    newprocList.add(procList.get(m));
                }
                for (int pro = 0; pro < procList.size(); pro++) {
                    DRGWSResult sexvalidationresult = gm.GenderConfictValidationProc(datasource, procList.get(pro).trim(), grouperparameter.getGender());
                    if (!sexvalidationresult.isSuccess()) {
                        newprocList.remove(procList.get(pro).trim());
                    }
                }
                grouper.setProc(String.join(",", newprocList));
            } else {
                grouper.setProc(grouperparameter.getProc());
            }
            grouper.setResult_id(grouperparameter.getResult_id());
            //CLEANING SDX
            if (!grouperparameter.getSdx().isEmpty()) {
                LinkedList<String> newsdxList = new LinkedList<>();
                List<String> sdxList = Arrays.asList(grouperparameter.getSdx().split(","));
                for (int m = 0; m < sdxList.size(); m++) {
                    newsdxList.add(sdxList.get(m));
                }
                for (int u = 0; u < sdxList.size(); u++) {
                    if (sdxList.get(u).equals(grouperparameter.getPdx())) {
                        newsdxList.remove(sdxList.get(u));
                    } else {
                        if (!grouperparameter.getBirthDate().isEmpty()
                                && !grouperparameter.getAdmissionDate().isEmpty()) {
                            int daysfinal = 0;
                            String year = String.valueOf(utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()));
                            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                                daysfinal = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) * 365;
                            } else {
                                daysfinal = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
                            }
                            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 0
                                    && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 0) {
                                if (!grouperparameter.getBirthDate().isEmpty() && !grouperparameter.getAdmissionDate().isEmpty()) {
                                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 0
                                            && utility.ComputeDay(grouperparameter.getBirthDate(),
                                                    grouperparameter.getAdmissionDate()) >= 0 && !sdxList.get(u).isEmpty()) {
                                        DRGWSResult SDxResult = gm.GetICD10(datasource, sdxList.get(u).toUpperCase().trim());
                                        if (SDxResult.isSuccess()) {
                                            //CHECKING FOR AGE CONFLICT
                                            DRGWSResult getAgeConfictResult = gm.AgeConfictValidation(datasource, sdxList.get(u).toUpperCase().trim(),
                                                    String.valueOf(daysfinal), year);
                                            if (!getAgeConfictResult.isSuccess()) {
                                                newsdxList.remove(sdxList.get(u));
                                            }
                                            //CHECKING FOR GENDER CONFLICT
                                            DRGWSResult getSexConfictResult = gm.GenderConfictValidation(datasource, sdxList.get(u), grouperparameter.getGender());
                                            if (!getSexConfictResult.isSuccess()) {
                                                newsdxList.remove(sdxList.get(u));
                                            }
                                        } else {
                                            newsdxList.remove(sdxList.get(u));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                grouper.setSdx(String.join(",", newsdxList));
            } else {
                grouper.setSdx(grouperparameter.getSdx());
            }
            //END CLEANING SDX
//            System.out.println("BDAY " + grouper.getBirthDate());

            grouper.setDischargeType(grouperparameter.getDischargeType());
            grouper.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            //===================VALIDATION AREA ==================================
            DRGWSResult geticd10Result = gm.GetICD10PreMDC(datasource, grouper.getPdx());
            if (grouper.getPdx().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid PDx");
            } else if (!geticd10Result.isSuccess()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Unacceptable PDx");
            } else {
                if (grouper.getGender().trim().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid sex , Patient sex is required");
                } else if (!Arrays.asList(sex).contains(grouper.getGender().toUpperCase())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Patient sex is not valid");
                } else if (grouper.getAdmissionDate().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidDate(grouper.getAdmissionDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (grouper.getDischargeDate().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidDate(grouper.getDischargeDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (grouper.getBirthDate().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid Age");
//                    System.out.println("INVALID 1");
                } else if (!utility.IsValidDate(grouper.getBirthDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid Age");
//                    System.out.println("INVALID 2");
                } else if (grouper.getTimeAdmission().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidTime(grouper.getTimeAdmission())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (grouper.getTimeDischarge().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidTime(grouper.getTimeDischarge())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (grouper.getDischargeType().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Disposition is empty");
                } else if (!Arrays.asList(disposition).contains(grouper.getDischargeType())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Disposition is not valid");
                } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) == 0
                        && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) < 28) {
                    if (!grouper.getAdmissionWeight().isEmpty()) {
                        if (!utility.isValidNumeric(grouper.getAdmissionWeight())) {
                            drgresult.setDRG("26509");
                            drgresult.setDC("2650");
                            drgresult.setDRGName("Admission Weight is not valid");
                        } else if (Double.parseDouble(grouper.getAdmissionWeight()) < 0.3) {
                            drgresult.setDRG("26509");
                            drgresult.setDC("2650");
                            drgresult.setDRGName("Admission Weight less than 0.3 kg is not valid");
                        }
                    } else {
                        drgresult.setDRG("26509");
                        drgresult.setDC("2650");
                        drgresult.setDRGName("Admission Weight is empty");
                    }
                } else {
                    int oras = utility.ComputeTime(
                            grouper.getAdmissionDate(),
                            utility.Convert24to12(grouper.getTimeAdmission()),
                            grouper.getDischargeDate(),
                            utility.Convert24to12(grouper.getTimeDischarge()));
                    int araw = utility.ComputeDay(grouper.getAdmissionDate(),
                            grouper.getDischargeDate());
                    int taon = utility.ComputeYear(grouper.getAdmissionDate(),
                            grouper.getDischargeDate());
                    if (utility.ComputeLOS(grouper.getAdmissionDate(),
                            utility.Convert24to12(grouper.getTimeAdmission()),
                            grouper.getDischargeDate(),
                            utility.Convert24to12(grouper.getTimeDischarge())) == 0) {
                        if (araw <= 0 && oras < 0) {
                            drgresult.setDRG("26509");
                            drgresult.setDC("2650");
                            drgresult.setDRGName("Invalid LOS");
                        }
                    } else if (taon <= 0 && araw < 0) {
                        drgresult.setDRG("26509");
                        drgresult.setDC("2650");
                        drgresult.setDRGName("Invalid LOS");
                    }
                }
            }
            if (drgresult.getDRG() != null) {
                DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
                        drgresult.getMDC(),
                        drgresult.getPDC(),
                        drgresult.getDC(),
                        grouper.getResult_id(),
                        grouper.getClaimseries(),
                        drgresult.getDRG(),
                        drgresult.getDRGName());
                if (updatedrgresult.isSuccess()) {
                    result.setSuccess(true);
                }
                result.setMessage(updatedrgresult.getMessage());
                drgresult.setClaimseries(grouperparameter.getClaimseries());
                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
            } else {
                DRGWSResult validateresult = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
                if (validateresult.isSuccess()) {
                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
                    DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
                            drgResults.getMDC(),
                            drgResults.getPDC(),
                            drgResults.getDC(),
                            grouper.getResult_id(),
                            grouper.getClaimseries(),
                            drgResults.getDRG(), "");
                    drgResults.setResultid(grouper.getResult_id());
                    drgResults.setClaimseries(grouperparameter.getClaimseries());
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(drgResults));
                    result.setMessage(validateresult.getMessage());
                    DRGAuditTrail(datasource, grouper.getClaimseries(),
                            grouper.getIdseries(),
                            updatedrgresult.getMessage(), "SUCCESS");
                } else {
                    DRGAuditTrail(datasource, grouper.getClaimseries(), grouper.getIdseries(), validateresult.getMessage(), "FAILED");
                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
                    result.setMessage(validateresult.getMessage());
                }
            }
        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ProcessGrouperParameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void FileWriter(File path, String messasge) {
        try {
            FileReader fr = new FileReader(path);
            ArrayList<String> oldContent;
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                oldContent = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    oldContent.add(line);
                }
            }
            try (PrintWriter pw = new PrintWriter(path)) {
                for (int a = 0; a < oldContent.size(); a++) {
                    pw.write(oldContent.get(a) + "\n");
                }
                pw.write(messasge + "\n");
                pw.flush();

            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessGrouperParameter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String DRGAuditTrail(final DataSource datasource, String claimsSeries, String idSeries, String deTails, String status) {
        GrouperMethod gm = new GrouperMethod();
        DRGWSResult grouperauditrail = gm.InsertGrouperAuditTrail(datasource, claimsSeries, idSeries, deTails, status);
        return grouperauditrail.getMessage();
    }

}
