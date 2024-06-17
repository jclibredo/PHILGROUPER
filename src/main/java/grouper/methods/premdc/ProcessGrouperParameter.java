/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.Grouper;
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
 * @author MinoSun
 */
@RequestScoped
public class ProcessGrouperParameter {

    public ProcessGrouperParameter() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ProcessGrouperParameter(final DataSource datasource, final GrouperParameter grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
//            File path = new File("D:\\DRG Result Log Files\\LogFileForgrouperResult.txt");
            //  for (int g = 0; g < grouperparameter.size(); g++) {
            DRGOutput drgresult = utility.DRGOutput();
            GrouperParameter newGrouperParam = utility.GrouperParameter();
            //======================== TIME FORMAT CONVERTER ==============================
            newGrouperParam.setExpireTime(grouperparameter.getExpireTime());
            newGrouperParam.setTimeAdmission(grouperparameter.getTimeAdmission());
            newGrouperParam.setAdmissionDate(grouperparameter.getAdmissionDate());
            newGrouperParam.setTimeDischarge(grouperparameter.getTimeDischarge());
            newGrouperParam.setDischargeDate(grouperparameter.getDischargeDate());
            newGrouperParam.setExpiredDate(grouperparameter.getExpiredDate());
            newGrouperParam.setBirthDate(grouperparameter.getBirthDate());
            newGrouperParam.setClaimseries(grouperparameter.getClaimseries());
            newGrouperParam.setGender(grouperparameter.getGender());
            newGrouperParam.setIdseries(grouperparameter.getIdseries());
            newGrouperParam.setPdx(grouperparameter.getPdx());

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
                newGrouperParam.setProc(String.join(",", newprocList));
            } else {
                newGrouperParam.setProc(grouperparameter.getProc());
            }

            newGrouperParam.setResult_id(grouperparameter.getResult_id());
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
//                                                //errors.add(" SDx:" + SDxCode + " conflict with age");
                                                newsdxList.remove(sdxList.get(u));
                                            }
                                            //CHECKING FOR GENDER CONFLICT
                                            DRGWSResult getSexConfictResult = gm.GenderConfictValidation(datasource, sdxList.get(u), grouperparameter.getGender());
                                            if (!getSexConfictResult.isSuccess()) {
                                                //errors.add(" SDx:" + SDxCode + " confict with sex");
                                                newsdxList.remove(sdxList.get(u));
                                            }
                                        } else {
                                            //  errors.add(" SDx:" + SDxCode + " Invalid SDx");
                                            newsdxList.remove(sdxList.get(u));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                newGrouperParam.setSdx(String.join(",", newsdxList));
            } else {
                newGrouperParam.setSdx(grouperparameter.getSdx());
            }
            //END CLEANING SDX
            newGrouperParam.setDischargeType(grouperparameter.getDischargeType());
            newGrouperParam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            //===================VALIDATION AREA ==================================

            //END PRIMARY CODES VALIDATION
            //  AGE VALIDATION AND GENDER
            if (newGrouperParam.getGender().trim().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Invalid sex , Patient sex is required");
            } else if (!Arrays.asList(sex).contains(newGrouperParam.getGender().toUpperCase())) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Patient sex is not valid");
            }
////----------------------------------------------------------------
//                //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
            if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
                } else {
                    if (utility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) <= 0
                            && utility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 0) {
                        drgresult.setDRG("26539");
                        drgresult.setDRGName("Invalid age");
                    }
                }
            }
            if (!newGrouperParam.getAdmissionDate().isEmpty()
                    && !newGrouperParam.getDischargeDate().isEmpty()
                    && !newGrouperParam.getTimeDischarge().isEmpty()
                    && !newGrouperParam.getTimeAdmission().isEmpty()) {
                if (!utility.IsValidDate(newGrouperParam.getBirthDate())) {
                    drgresult.setDRG("26539");
                    drgresult.setDRGName("Invalid Age");
                } else if (!utility.IsValidDate(newGrouperParam.getAdmissionDate()) || !utility.IsValidDate(newGrouperParam.getDischargeDate())) {
                    drgresult.setDRG("26549");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidTime(newGrouperParam.getTimeAdmission()) || !utility.IsValidTime(newGrouperParam.getTimeDischarge())) {
                    drgresult.setDRG("26549");
                    drgresult.setDRGName("Invalid LOS");
                } else {
                    int oras = utility.ComputeTime(
                            newGrouperParam.getAdmissionDate(),
                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            utility.Convert24to12(newGrouperParam.getTimeDischarge()));
                    int araw = utility.ComputeDay(newGrouperParam.getAdmissionDate(),
                            newGrouperParam.getDischargeDate());
                    int minuto = utility.MinutesCompute(
                            newGrouperParam.getAdmissionDate(),
                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            utility.Convert24to12(newGrouperParam.getTimeDischarge()));
                    int taon = utility.ComputeYear(newGrouperParam.getAdmissionDate(),
                            newGrouperParam.getDischargeDate());
                    if (utility.ComputeLOS(newGrouperParam.getAdmissionDate(),
                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            utility.Convert24to12(newGrouperParam.getTimeDischarge())) == 0) {
                        if (araw <= 0 && oras < 0) {
                            drgresult.setDRG("26549");
                            drgresult.setDRGName("Invalid LOS");
                        }
                    } else if (taon <= 0 && araw < 0) {
                        drgresult.setDRG("26549");
                        drgresult.setDRGName("Invalid LOS");
                    }
                }
            }
//                //END LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
//                //REQUIRED DATE DATA NEEDED FOR GROUPER
            DRGWSResult geticd10Result = gm.GetICD10PreMDC(datasource, grouperparameter.getPdx());
            if (!geticd10Result.isSuccess()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Unacceptable PDx");
            }
            if (newGrouperParam.getAdmissionDate().isEmpty()) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            } else if (!utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
            if (newGrouperParam.getDischargeDate().isEmpty()) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("DischargeDate is required");
            } else if (!utility.IsValidDate(newGrouperParam.getDischargeDate())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
            if (newGrouperParam.getBirthDate().isEmpty()) {
                drgresult.setDRG("26539");
                drgresult.setDRGName("Invalid Age");
            } else if (!utility.IsValidDate(newGrouperParam.getBirthDate())) {
                drgresult.setDRG("26539");
                drgresult.setDRGName("Invalid Age");
            }
//                //END REQUIRED DATE DATA NEEDED FOR GROUPER
//                // GET THE TIME DATA REQUIRED FOR THE GROUPER
            if (newGrouperParam.getTimeAdmission().isEmpty()) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            } else if (!utility.IsValidTime(newGrouperParam.getTimeAdmission())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
            if (newGrouperParam.getTimeDischarge().isEmpty()) { //GET THE DATE OF ADMISSION
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            } else if (!utility.IsValidTime(newGrouperParam.getTimeDischarge())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
//                // END GET THE TIME DATA REQUIRED FOR THE GROUPER
//                //VALIDATION FOR NEW BORN DATA
            if (newGrouperParam.getDischargeType().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Disposition is empty");
            } else if (!Arrays.asList(disposition).contains(newGrouperParam.getDischargeType())) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Disposition is not valid");
            }
            if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
                } else {
                    if (utility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) == 0
                            && utility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 28) {
                        if (newGrouperParam.getAdmissionWeight() != null) {
                            if (!utility.isValidNumeric(newGrouperParam.getAdmissionWeight())) {
                                drgresult.setDRG("26509");
                                drgresult.setDRGName("Admission Weight is not valid");
                            } else if (Double.parseDouble(newGrouperParam.getAdmissionWeight()) < 0.3) {
                                drgresult.setDRG("26509");
                                drgresult.setDRGName("Admission Weight less than 0.3 kg is not valid");
                            }
                        } else {
                            drgresult.setDRG("26509");
                            drgresult.setDRGName("Admission Weight is empty");
                        }
                    }
                }
            }
//
            if (drgresult.getDRG() != null) {
//                    DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
//                            drgresult.getMDC(),
//                            drgresult.getPDC(),
//                            drgresult.getDC(),
//                            newGrouperParam.getResult_id(),
//                            newGrouperParam.getClaimseries(),
//                            drgresult.getDRG());
//                    singleresult.setMessage(updatedrgresult.getMessage());
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgresult));

                //------------------------------ FILE WRITER PART--------------------------------
//                FileReader fr = new FileReader(path);
//                ArrayList<String> oldContent;
//                try (BufferedReader br = new BufferedReader(fr)) {
//                    String line;
//                    oldContent = new ArrayList<>();
//                    while ((line = br.readLine()) != null) {
//                        oldContent.add(line);
//                    }
//                }
//
//                try (PrintWriter pw = new PrintWriter(path)) {
//                    for (int a = 0; a < oldContent.size(); a++) {
//                        pw.write(oldContent.get(a) + "\n");
//                    }
//                    pw.write(drgresult.getDRG() + "\n");
//                    pw.flush();
//                }

                //------------------------------ FILE WRITER PART--------------------------------
            } else {
                ValidateFindMDC vfm = new ValidateFindMDC();
                // =================END OF VALIDATION AREA ================================
                DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, newGrouperParam);
                if (validateresult.isSuccess()) {
                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
                    //=================================================================================
//                        DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
//                                drgResults.getMDC(),
//                                drgResults.getPDC(),
//                                drgResults.getDC(),
//                                newGrouperParam.getResult_id(),
//                                newGrouperParam.getClaimseries(),
//                                drgResults.getDRG());
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(drgResults));
                    result.setMessage("OK");
//                        //String dataResult = "DRG:" + drgResults.getDRG() + "|MDC:" + drgResults.getMDC();
//                        //DRG Grouper Auditrail
//                        //System.out.println(dataResult);
//                        DRGWSResult grouperauditrail = gm.InsertGrouperAuditTrail(datasource,
//                                newGrouperParam.getClaimseries(), newGrouperParam.getIdseries(),
//                                updatedrgresult.getMessage(),
//                                "SUCCESS");
                    //DRG Grouper Auditrail
                    //singleresult.setMessage(updatedrgresult.getMessage() + " LOGS:" + grouperauditrail.getMessage());
                    //resultdata.add(singleresult);
                    //------------------------------ FILE WRITER PART--------------------------------
//                    FileReader fr = new FileReader(path);
//                    ArrayList<String> oldContent;
//                    try (BufferedReader br = new BufferedReader(fr)) {
//                        String line;
//                        oldContent = new ArrayList<>();
//                        while ((line = br.readLine()) != null) {
//                            oldContent.add(line);
//                        }
//                    }
//                    try (PrintWriter pw = new PrintWriter(path)) {
//                        for (int a = 0; a < oldContent.size(); a++) {
//                            pw.write(oldContent.get(a) + "\n");
//                        }
//                        pw.write(drgResults.getDRG() + "\n");
//                        pw.flush();
//                    }
                    //------------------------------ FILE WRITER PART--------------------------------
                } else {

//                    FileReader fr = new FileReader(path);
//                    ArrayList<String> oldContent;
//                    try (BufferedReader br = new BufferedReader(fr)) {
//                        String line;
//                        oldContent = new ArrayList<>();
//                        while ((line = br.readLine()) != null) {
//                            oldContent.add(line);
//                        }
//                    }
//                    try (PrintWriter pw = new PrintWriter(path)) {
//                        for (int a = 0; a < oldContent.size(); a++) {
//                            pw.write(oldContent.get(a) + "\n");
//                        }
//                        pw.write(validateresult.getMessage() + "\n");
//                        pw.flush();
//                    }

                    //DRG Grouper Auditrail
//                        DRGWSResult grouperauditrail = gm.InsertGrouperAuditTrail(datasource,
//                                newGrouperParam.getClaimseries(), newGrouperParam.getIdseries(),
//                                validateresult.getMessage(),
//                                "FAILED");
                    //DRG Grouper Auditrail
                    // singleresult.setMessage(validateresult.getMessage()); //+ " LOGS:" + grouperauditrail.getMessage());
                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
                    result.setMessage("OK");

                }
            }
        } catch (ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
