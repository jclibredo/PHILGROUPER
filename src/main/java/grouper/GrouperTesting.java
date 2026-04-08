/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.premdc.ValidateFindMDC;
import grouper.methods.validation.AgeConfictValidation;
import grouper.methods.validation.GenderConfictValidation;
import grouper.methods.validation.GenderConfictValidationProc;
import grouper.methods.validation.GetICD10;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.GET_ICD9;
import grouper.methods.validation.InsertGrouperAuditTrail;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.BufferedReader;
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
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author MinoSun
 */
@Path("GrouperTest")
@RequestScoped
public class GrouperTesting {

    public GrouperTesting() {
    }

    @Resource(lookup = "jdbc/grouperuser")
    private DataSource datasource;
    private final Utility utility = new Utility();

    @POST
    @Path("JasonData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ProcessGrouperParameter(
            final List<GrouperParameter> grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        ArrayList<DRGOutput> drgresultList = new ArrayList<>();
        ArrayList<String> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        try {

//            if (utility.GetString("FilePathReports").isSuccess()) {
            for (int g = 0; g < grouperparameter.size(); g++) {
//                    DRGWSResult grouperResult = this.ProcessData(utility.GetString("FilePathReports").getResult(), datasource, grouperparameter.get(g));
                DRGWSResult grouperResult = this.ProcessData(datasource, grouperparameter.get(g));
                if (grouperResult.isSuccess()) {
//                    DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
                    drgresultList.add(grouperResult.getResult());
                } else {
                    errorList.add(grouperResult.getMessage());
                }
            }
            if (grouperparameter.size() > 0) {
                result.setMessage("Data Process : " + drgresultList.size());
                result.setSuccess(true);
                result.setResult(drgresultList.toString());
//                    File file = new File(utility.GetString("FilePathReports").getResult());
//                    result.setResult("Open file: //" + file.getAbsolutePath().replace("\\", "/"));
            } else {
                result.setMessage("NO DATA AVAILABLE TO PROCESS");
            }
//            } else {
//                result.setMessage("File or directory not found");
//            }

        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GrouperTesting.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private DRGWSResult ProcessData(
            //            final String Path,
            final DataSource datasource,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> warningerror = new ArrayList<>();
        GetICD10PreMDC getPreMDC = new GetICD10PreMDC();
        GetICD10 SDxResult = new GetICD10();
        AgeConfictValidation ageValidation = new AgeConfictValidation();
        GenderConfictValidation sexValidation = new GenderConfictValidation();
        try {
            DRGOutput drgresult = utility.DRGOutput();
            GrouperParameter grouper = utility.GrouperParameter();
            grouper.setResult_id(grouperparameter.getResult_id());
            grouper.setExpireTime(grouperparameter.getExpireTime());
            grouper.setTimeAdmission(grouperparameter.getTimeAdmission());
            grouper.setAdmissionDate(grouperparameter.getAdmissionDate().replaceAll("\\/", "-"));
            grouper.setTimeDischarge(grouperparameter.getTimeDischarge());
            grouper.setDischargeDate(grouperparameter.getDischargeDate().replaceAll("\\/", "-"));
            grouper.setExpiredDate(grouperparameter.getExpiredDate() == null ? "" : grouperparameter.getExpiredDate().replaceAll("\\/", "-"));
            grouper.setBirthDate(grouperparameter.getBirthDate().replaceAll("\\/", "-"));
            grouper.setClaimseries(grouperparameter.getClaimseries());
            grouper.setGender(grouperparameter.getGender());
            grouper.setIdseries(grouperparameter.getIdseries());
            grouper.setDischargeType(grouperparameter.getDischargeType());

            drgresult.setPrepccl("");
            drgresult.setFinalpccl("");
            drgresult.setWarningerror("");

            if (getPreMDC.GetICD10PreMDC(datasource, grouperparameter.getPdx()).isSuccess()) {
                grouper.setPdx(grouperparameter.getPdx());
            } else if (getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(grouperparameter.getPdx())).isSuccess()) {
                grouper.setPdx(utility.icd10Cleaner(grouperparameter.getPdx()));
            } else {
                grouper.setPdx(grouperparameter.getPdx());
            }
            grouper.setAdmissionWeight(grouperparameter.getAdmissionWeight());

            //CLEANING PROC DATA
            GET_ICD9 icd9cm = new GET_ICD9();
            GenderConfictValidationProc sexValidateProc = new GenderConfictValidationProc();
            if (!grouperparameter.getProc().trim().isEmpty()) {
                LinkedList<String> newprocList = new LinkedList<>();
                List<String> procList = Arrays.asList(grouperparameter.getProc().split(","));
                for (int m = 0; m < procList.size(); m++) {
                    newprocList.add(procList.get(m));
                }
                for (int pro = 0; pro < procList.size(); pro++) {
                    if (icd9cm.GetICD9cm(datasource, procList.get(pro)).isSuccess()) {
                        DRGWSResult sexvalidationresult = sexValidateProc.GenderConfictValidationProc(datasource, procList.get(pro).trim(), grouperparameter.getGender());
                        if (!sexvalidationresult.isSuccess()) {
                            newprocList.remove(procList.get(pro).trim());
                        }
                    } else {
                        warningerror.add("Proc " + procList.get(pro) + " invalid");
                    }
                }
                grouper.setProc(String.join(",", newprocList));
            } else {
                grouper.setProc(grouperparameter.getProc());
            }

//            CLEANING SDX
            if (!grouperparameter.getSdx().isEmpty()) {
                LinkedList<String> newsdxList = new LinkedList<>();
                List<String> sdxList = Arrays.asList(grouperparameter.getSdx().split(","));
                for (int m = 0; m < sdxList.size(); m++) {
                    if (sdxList.get(m).toUpperCase().trim().equals(grouper.getPdx().toUpperCase().trim())) {
                        warningerror.add("SDx " + sdxList.get(m) + " duplicate with the PDx");
                    } else {
                        if (getPreMDC.GetICD10PreMDC(datasource, sdxList.get(m)).isSuccess()) {
                            newsdxList.add(sdxList.get(m));
                        } else if (getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(sdxList.get(m))).isSuccess()) {
                            newsdxList.add(utility.icd10Cleaner(sdxList.get(m)));
                        } else {
                            warningerror.add("SDx " + sdxList.get(m) + " invalid");
                        }
                    }
                }
                ArrayList<String> finalSdx = new ArrayList<>();
                for (int u = 0; u < newsdxList.size(); u++) {
                    if (!grouper.getBirthDate().isEmpty()
                            && !grouper.getAdmissionDate().isEmpty()) {
                        int daysfinal = 0;
                        String year = String.valueOf(utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()));
                        if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) > 0) {
                            daysfinal = utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) * 365;
                        } else {
                            daysfinal = utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate());
                        }
                        if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
                                && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0) {
                            if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
                                    && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0) {
                                if (!grouper.getBirthDate().isEmpty() && !grouper.getAdmissionDate().isEmpty()) {
                                    if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
                                            && utility.ComputeDay(grouper.getBirthDate(),
                                                    grouper.getAdmissionDate()) >= 0 && !newsdxList.get(u).isEmpty()) {
                                        //CHECKING FOR AGE CONFLICT
                                        DRGWSResult getAgeConfictResult = ageValidation.AgeConfictValidation(datasource, newsdxList.get(u).toUpperCase().trim(),
                                                String.valueOf(daysfinal), year);
                                        //CHECKING FOR GENDER CONFLICT
                                        DRGWSResult getSexConfictResult = sexValidation.GenderConfictValidation(datasource, newsdxList.get(u), grouper.getGender());
                                        if (!ageValidation.AgeConfictValidation(datasource, newsdxList.get(u).toUpperCase().trim(),
                                                String.valueOf(daysfinal), year).isSuccess()) {
                                            warningerror.add("SDx " + newsdxList.get(u) + " age conflict");
                                            newsdxList.remove(newsdxList.get(u));
                                        }
                                        if (!getSexConfictResult.isSuccess()) {
                                            warningerror.add("SDx " + newsdxList.get(u) + " sex conflict");
                                            newsdxList.remove(newsdxList.get(u));
                                        }
                                        if (getAgeConfictResult.isSuccess() && getSexConfictResult.isSuccess()) {
                                            finalSdx.add(newsdxList.get(u));
                                        }

                                    }
                                }
                            }

                        }
                    }
                }
                grouper.setSdx(String.join(",", finalSdx));
            } else {
                grouper.setSdx(grouperparameter.getSdx());
            }
            if (!warningerror.isEmpty()) {
                grouper.setWarningerror(String.join(",", warningerror));
                drgresult.setWarningerror(String.join(",", warningerror));
            }

            if (grouper.getPdx().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid PDx");
            } else if (!getPreMDC.GetICD10PreMDC(datasource, grouper.getPdx()).isSuccess()
                    && !getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(grouper.getPdx())).isSuccess()) {
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
                    drgresult.setDRGName("Invalid LOS no admission date found");
                } else if (!utility.IsValidDate(grouper.getAdmissionDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS admissiondate is not valid");
                } else if (grouper.getDischargeDate().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS no discharge date found");
                } else if (!utility.IsValidDate(grouper.getDischargeDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS discharge date is not valid");
                } else if (grouper.getBirthDate().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid Age Date of Birth is missing");
                } else if (!utility.IsValidDate(grouper.getBirthDate())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid Age date of birth is not valid");
                } else if (grouper.getTimeAdmission().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS time admission not found");
                } else if (!utility.IsValidTime(grouper.getTimeAdmission())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS time admission is not valid");
                } else if (grouper.getTimeDischarge().isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS discharge time not found");
                } else if (!utility.IsValidTime(grouper.getTimeDischarge())) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS discharge time is not valid");
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
                //result.setMessage(updatedrgresult.getMessage());
                drgresult.setClaimseries(grouperparameter.getClaimseries());
                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
                result.setSuccess(true);
                // File writer
//                this.FileWriter(Path, grouperparameter.getClaimseries(), drgresult.getDRG(), "N/A", drgresult.getDRGName(), "N/A", "N/A", "N/A");
            } else {
//                DRGWSResult validateresult = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
                result = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
//                if (validateresult.isSuccess()) {
////                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
////                    result.setResult(utility.objectMapper().writeValueAsString(utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class)));
//                    result.setResult(validateresult.getResult());
//                    //File writer
////                    this.FileWriter(Path, grouperparameter.getClaimseries(), drgResults.getDRG(), drgResults.getPDC(), drgResults.getDRGName(), drgResults.getPrepccl(), drgResults.getFinalpccl(), drgResults.getWarningerror());
//                } else {
////                    DRGAuditTrail(datasource, grouper.getClaimseries(), grouper.getIdseries(), validateresult.getMessage(), "FAILED");
////                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
//                    result.setResult(validateresult.getResult());
//                    result.setMessage(validateresult.getMessage());
//                    //File writer
////                    this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", validateresult.getMessage(), "N/A", "N/A", "N/A");
//                }
            }
        } catch (ParseException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GrouperTesting.class.getName()).log(Level.SEVERE, null, ex);
            // File writer
//            this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", ex.toString(), "N/A", "N/A", "N/A");
        }
        return result;
    }

    public void FileWriter(String path, String series, String drgcode, String pdc, String drgname, String prepccl, String finalpccl, String warningerror) {
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
                pw.write(series + "," + drgcode + "," + pdc + "," + drgname + ", PREPCCL:" + prepccl + ", FINALPCCL:" + finalpccl + ", WARNING:" + warningerror + "\n");
                pw.flush();

            }
        } catch (IOException ex) {
            Logger.getLogger(GrouperTesting.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String DRGAuditTrail(final DataSource datasource, String claimsSeries, String idSeries, String deTails, String status) {
        DRGWSResult grouperauditrail = new InsertGrouperAuditTrail().InsertGrouperAuditTrail(datasource, claimsSeries, idSeries, deTails, status);
        return grouperauditrail.getMessage();
    }

}
