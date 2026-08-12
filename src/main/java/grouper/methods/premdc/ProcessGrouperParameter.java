/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.methods.validation.AgeConfictValidation;
import grouper.methods.validation.GenderConfictValidation;
import grouper.methods.validation.GenderConfictValidationProc;
import grouper.methods.validation.GetICD10;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.InsertGrouperAuditTrail;
import grouper.methods.validation.UpdateDRGResult;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
//import java.util.logging.Level;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class ProcessGrouperParameter {

    public ProcessGrouperParameter() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(ProcessGrouperParameter.class);
    private final Utility utility = new Utility();

    public DRGWSResult ProcessGrouperParameter(
            final DataSource datasource,
            final String SchemaName,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String path = utility.GetString("FilePathReports").getResult();
        try {
            DRGOutput drgresult = utility.DRGOutput();
            GrouperParameter grouper = utility.GrouperParameter();
            //TIME FORMAT CONVERTER
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
            String rawDischargeType = grouperparameter.getDischargeType().toUpperCase();

            switch (rawDischargeType) {
                case "E": {
                    grouper.setDischargeType("8");
                    break;
                }
                case "O": {
                    grouper.setDischargeType("5");
                    break;
                }
                case "I":
                case "R": {
                    grouper.setDischargeType("1");
                    break;
                }
                case "A": {
                    grouper.setDischargeType("3");
                    break;
                }
                case "T": {
                    grouper.setDischargeType("4");
                    break;
                }
                case "H": {
                    grouper.setDischargeType("2");
                    break;
                }
                default: {
                    grouper.setDischargeType(grouperparameter.getDischargeType());
                    break;
                }
            }

            grouper.setPrepccl("");
            grouper.setFinalpccl("");
            grouper.setWarningerror("");
            drgresult.setPrepccl("");
            drgresult.setFinalpccl("");
            drgresult.setWarningerror("");

            //CLEANING PROC DATA
            GenderConfictValidationProc validateProc = new GenderConfictValidationProc();
            if (!grouperparameter.getProc().trim().isEmpty()) {
                LinkedList<String> newprocList = new LinkedList<>();
                List<String> procList = Arrays.asList(grouperparameter.getProc().split(","));
                for (int m = 0; m < procList.size(); m++) {
                    newprocList.add(procList.get(m));
                }
                for (int pro = 0; pro < procList.size(); pro++) {
                    DRGWSResult validateFirst = validateProc.GenderConfictValidationProc(datasource, SchemaName, procList.get(pro).trim(),
                            grouper.getGender());
                    if (!validateFirst.isSuccess()) {
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
                            String year = String.valueOf(utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()));
                            if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) > 0) {
                                daysfinal = utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) * 365;
                            } else {
                                daysfinal = utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate());
                            }
                            if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
                                    && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0) {
                                if (!grouper.getBirthDate().isEmpty() && !grouper.getAdmissionDate().isEmpty()) {
                                    if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
                                            && utility.ComputeDay(grouper.getBirthDate(),
                                                    grouper.getAdmissionDate()) >= 0 && !sdxList.get(u).isEmpty()) {
                                        DRGWSResult SDxResult = new GetICD10().GetICD10(datasource, SchemaName, sdxList.get(u).toUpperCase().trim());
                                        if (SDxResult.isSuccess()) {
                                            //CHECKING FOR AGE CONFLICT
                                            DRGWSResult getAgeConfictResult = new AgeConfictValidation().AgeConfictValidation(datasource, SchemaName, sdxList.get(u).toUpperCase().trim(),
                                                    String.valueOf(daysfinal), year);
                                            if (!getAgeConfictResult.isSuccess()) {
                                                newsdxList.remove(sdxList.get(u));
                                            }
                                            //CHECKING FOR GENDER CONFLICT
                                            DRGWSResult getSexConfictResult = new GenderConfictValidation().GenderConfictValidation(datasource, SchemaName, sdxList.get(u),
                                                    grouper.getGender());
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
            grouper.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            //VALIDATION AREA
            DRGWSResult geticd10Result = new GetICD10PreMDC().GetICD10PreMDC(datasource, SchemaName, grouper.getPdx());
            if (grouper.getPdx().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid PDx");
            } else if (!geticd10Result.isSuccess()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Unacceptable PDx");
            } else if (grouper.getBirthDate().equals("") || grouper.getBirthDate().isEmpty()) {
                drgresult.setDRG("26539");
                drgresult.setDC("2653");
                drgresult.setDRGName("Ungroupable, invalid age due to missing birthdate");
            } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) > 124) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Ungroupable, invalid age more than 124 years old");
            } else {
                if (grouper.getGender().trim().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid sex , Patient sex is required");
                } else if (!Arrays.asList(sex).contains(grouper.getGender().toUpperCase())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Patient sex is not valid");
                } else if (grouper.getAdmissionDate().isEmpty() || !utility.IsValidDate(grouper.getDischargeDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                } else if (!utility.IsValidDate(grouper.getAdmissionDate()) || !utility.IsValidDate(grouper.getDischargeDate())) {
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
                } else if (!utility.IsValidDate(grouper.getBirthDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid Age");
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
                } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) < 1
                        && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) < 0) {
                    drgresult.setDRG("26539");
                    drgresult.setDC("2653");
                    drgresult.setDRGName("Ungroupable, DateofBirth Must be less than or equal to AdmissionDate");
                } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getDischargeDate()) < 1
                        && utility.ComputeDay(grouper.getBirthDate(), grouper.getDischargeDate()) < 0) {
                    drgresult.setDRG("26539");
                    drgresult.setDC("2653");
                    drgresult.setDRGName("Ungroupable, DateofBirth Must be less than or equal to DischargeDate");
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
                //COMMENT THIS IN TESTING MODE STARTING LINE
                DRGWSResult updatedrgresult = new UpdateDRGResult().UpdateDRGResult(datasource,
                        SchemaName,
                        "ERR",
                        "ERR",
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
                //COMMENT THIS IN TESTING MODE ENDING LINE
                //FILE WRITE IN TESTING MODE
//                this.FileWriter(path, grouperparameter.getClaimseries(), drgresult.getDRG(), "N/A", drgresult.getDRGName(), "N/A", "N/A", "N/A");
            } else {
                DRGWSResult validateresult = new ValidateFindMDC().validateFindMDC(datasource, SchemaName, grouper);
                if (validateresult.isSuccess()) {
                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
//                    COMMENT THIS IN TESTING MODE STARTING LINE
                    DRGWSResult updatedrgresult = new UpdateDRGResult().UpdateDRGResult(datasource,
                            SchemaName,
                            drgResults.getMDC(),
                            drgResults.getPDC(),
                            drgResults.getDC(),
                            grouper.getResult_id(),
                            grouper.getClaimseries(),
                            drgResults.getDRG(), "");
//
                    drgResults.setResultid(grouper.getResult_id());
                    drgResults.setClaimseries(grouperparameter.getClaimseries());
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(drgResults));
                    result.setMessage(validateresult.getMessage());
//                    //Grouper Auditrail
                    DRGAuditTrail(datasource, SchemaName, grouper.getClaimseries(),
                            grouper.getIdseries(),
                            updatedrgresult.getMessage(), "SUCCESS");
                    //COMMENT THIS IN TESTING MODE ENDING LINE
                    //FILE WRITE IN TESTING MODE
//                    this.FileWriter(path, grouperparameter.getClaimseries(),
//                            drgResults.getDRG(), drgResults.getPDC(),
//                            drgResults.getDRGName(), drgResults.getPrepccl(), drgResults.getFinalpccl(), drgResults.getWarningerror());
                } else {
                    //COMMENT THIS IN TESTING MODE STARTING LINE
                    DRGAuditTrail(datasource, SchemaName, grouper.getClaimseries(), grouper.getIdseries(), validateresult.getMessage(), "FAILED");
                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
                    result.setMessage(validateresult.getMessage());
                    //COMMENT THIS IN TESTING MODE ENDING LINE
                    //FILE WRITE IN TESTING MODE
//                    this.FileWriter(path, grouperparameter.getClaimseries(), "N/A", "N/A", validateresult.getMessage(), "N/A", "N/A", "N/A");
                }
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing Process Grouper Parameter Method");
            logger.error("Error in Process Grouper Parameter Method : {}", ex.getMessage(), ex);
            //FILE WRITE IN TESTING MODE
            this.FileWriter(path, grouperparameter.getClaimseries(), "N/A", "N/A", ex.toString(), "N/A", "N/A", "N/A");
        }
        return result;
    }

    public void FileWriter(
            final String path,
            final String series,
            final String drgcode,
            final String pdc,
            final String drgname,
            final String prepccl,
            final String finalpccl,
            final String warningerror) {
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
                pw.write(
                        "SERIES: " + series
                        + ", DRGCODE:" + drgcode
                        + ", PDC:" + pdc
                        + ", NAME:" + drgname
                        + ", PREPCCL:" + prepccl
                        + ", FINALPCCL:" + finalpccl
                        + ", ERROR:" + warningerror + "\n");
                pw.flush();

            }
        } catch (IOException ex) {
            logger.info("Executing File writer Method");
            logger.error("Error in File writer Method : {}", ex.getMessage(), ex);
        }
    }

    public String DRGAuditTrail(final DataSource datasource, final String SchemaName, String claimsSeries, String idSeries, String deTails, String status) {
        DRGWSResult grouperauditrail = new InsertGrouperAuditTrail().InsertGrouperAuditTrail(datasource, SchemaName, claimsSeries, idSeries, deTails, status);
        return grouperauditrail.getMessage();
    }

}
