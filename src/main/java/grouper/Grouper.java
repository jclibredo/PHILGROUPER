/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.premdc.ValidateFindMDC;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.DRGUtility;
import grouper.utility.GrouperMethod;
import grouper.utility.TestParamObject;
import grouper.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author MinoSun
 */
@Path("Grouper")
@RequestScoped
public class Grouper {

    public Grouper() {
    }

    @Resource(lookup = "jdbc/drgsbuser")
    private DataSource datasource;
    private final Utility utility = new Utility();
    private final DRGUtility drgutility = new DRGUtility();
    private final ValidateFindMDC vfm = new ValidateFindMDC();
    private final GrouperMethod gm = new GrouperMethod();

    /**
     * Retrieves representation of an instance of
     * drg.claims.drgapplication.Grouper
     *
     * @param grouperparameter
     * @return an instance of java.lang.String
     */
    //FOR FRONT VALIDATION KEYVALUE PAIR METHOD
    @POST
    @Path("PROCESSGrouper")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult PROCESSGrouper(final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, grouperparameter);
            if (validateresult.isSuccess()) {
                DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
                result.setMessage(drgResults.getDRGName());
                result.setSuccess(true);
                result.setResult(drgResults.getDRG());
            } else {
                result.setMessage(validateresult.getMessage());
                result.setSuccess(false);
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //START OF FINAL METHOD FOR GROUPER
    @POST
    @Path("ProcessGrouperParameter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ProcessGrouperParameter(final List<GrouperParameter> grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        ArrayList<DRGWSResult> resultdata = new ArrayList<>();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            File path = new File("D:\\DRG Result Log Files\\LogFileForgrouperResult.txt");
            for (int g = 0; g < grouperparameter.size(); g++) {
                DRGOutput drgresult = utility.DRGOutput();
                DRGWSResult singleresult = utility.DRGWSResult();
                GrouperParameter newGrouperParam = utility.GrouperParameter();
                //======================== TIME FORMAT CONVERTER ==============================
                newGrouperParam.setExpireTime(grouperparameter.get(g).getExpireTime());
                newGrouperParam.setTimeAdmission(grouperparameter.get(g).getTimeAdmission());
                newGrouperParam.setAdmissionDate(grouperparameter.get(g).getAdmissionDate());
                newGrouperParam.setTimeDischarge(grouperparameter.get(g).getTimeDischarge());
                newGrouperParam.setDischargeDate(grouperparameter.get(g).getDischargeDate());
                newGrouperParam.setExpiredDate(grouperparameter.get(g).getExpiredDate());
                newGrouperParam.setBirthDate(grouperparameter.get(g).getBirthDate());
                newGrouperParam.setClaimseries(grouperparameter.get(g).getClaimseries());
                newGrouperParam.setGender(grouperparameter.get(g).getGender());
                newGrouperParam.setIdseries(grouperparameter.get(g).getIdseries());
                newGrouperParam.setPdx(grouperparameter.get(g).getPdx());
                newGrouperParam.setProc(grouperparameter.get(g).getProc());
                newGrouperParam.setResult_id(grouperparameter.get(g).getResult_id());
                newGrouperParam.setSdx(grouperparameter.get(g).getSdx());
                newGrouperParam.setDischargeType(grouperparameter.get(g).getDischargeType());
                newGrouperParam.setAdmissionWeight(grouperparameter.get(g).getAdmissionWeight());
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
//----------------------------------------------------------------
                //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
                if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
                    if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
                    } else {
                        if (drgutility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) <= 0
                                && drgutility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 0) {
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
                        int oras = drgutility.ComputeTime(
                                newGrouperParam.getAdmissionDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                                newGrouperParam.getDischargeDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeDischarge()));
                        int araw = drgutility.ComputeDay(newGrouperParam.getAdmissionDate(),
                                newGrouperParam.getDischargeDate());
                        int minuto = drgutility.MinutesCompute(
                                newGrouperParam.getAdmissionDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                                newGrouperParam.getDischargeDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeDischarge()));
                        int taon = drgutility.ComputeYear(newGrouperParam.getAdmissionDate(),
                                newGrouperParam.getDischargeDate());
                        if (drgutility.ComputeLOS(newGrouperParam.getAdmissionDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                                newGrouperParam.getDischargeDate(),
                                drgutility.Convert24to12(newGrouperParam.getTimeDischarge())) == 0) {

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
                //END LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
                //REQUIRED DATE DATA NEEDED FOR GROUPER
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
                //END REQUIRED DATE DATA NEEDED FOR GROUPER
                // GET THE TIME DATA REQUIRED FOR THE GROUPER
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
                // END GET THE TIME DATA REQUIRED FOR THE GROUPER
                //VALIDATION FOR NEW BORN DATA
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
                        if (drgutility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) == 0
                                && drgutility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 28) {
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

                if (drgresult.getDRG() != null) {
                    DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
                            drgresult.getMDC(),
                            drgresult.getPDC(),
                            drgresult.getDC(),
                            newGrouperParam.getResult_id(),
                            newGrouperParam.getClaimseries(),
                            drgresult.getDRG());
                    // singleresult.setMessage(updatedrgresult.getMessage());
                    singleresult.setSuccess(true);
                    //singleresult.setResult(utility.objectMapper().writeValueAsString(drgresult));
                    System.out.println(drgresult.getDRG());

                    //------------------------------ FILE WRITER PART--------------------------------
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
                        pw.write("MSG: UNGR ||DRG :" + drgresult.getDRG() + "\n");
                        pw.flush();
                    }

                    //------------------------------ FILE WRITER PART--------------------------------
                } else {
                    //=================END OF VALIDATION AREA ================================
                    DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, newGrouperParam);
                    if (validateresult.isSuccess()) {
                        DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
                        //=================================================================================
                        DRGWSResult updatedrgresult = gm.UpdateDRGResult(datasource,
                                drgResults.getMDC(),
                                drgResults.getPDC(),
                                drgResults.getDC(),
                                newGrouperParam.getResult_id(),
                                newGrouperParam.getClaimseries(),
                                drgResults.getDRG());
                        singleresult.setSuccess(true);

                        // singleresult.setResult(drgResults.getDRG());
                        String dataResult = "DRG:" + drgResults.getDRG() + "|MDC:" + drgResults.getMDC();
                        //DRG Grouper Auditrail

                        System.out.println(dataResult);
                        DRGWSResult grouperauditrail = gm.InsertGrouperAuditTrail(datasource,
                                newGrouperParam.getClaimseries(), newGrouperParam.getIdseries(),
                                updatedrgresult.getMessage(),
                                "SUCCESS");
                        //DRG Grouper Auditrail
                        //singleresult.setMessage(updatedrgresult.getMessage() + " LOGS:" + grouperauditrail.getMessage());
                        resultdata.add(singleresult);

                        //------------------------------ FILE WRITER PART--------------------------------
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
                            pw.write("MDC" + drgResults.getMDC() + " || DRG:" + drgResults.getDRG() + "\n");
                            pw.flush();
                        }
                        //------------------------------ FILE WRITER PART--------------------------------

                    } else {
                        //DRG Grouper Auditrail
                        DRGWSResult grouperauditrail = gm.InsertGrouperAuditTrail(datasource,
                                newGrouperParam.getClaimseries(), newGrouperParam.getIdseries(),
                                validateresult.getMessage(),
                                "FAILED");
                        //DRG Grouper Auditrail
                        //singleresult.setMessage(validateresult.getMessage() + " LOGS:" + grouperauditrail.getMessage());
                        ///singleresult.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
                        singleresult.setSuccess(false);
                        resultdata.add(singleresult);

                    }
                }
            }

            if (grouperparameter.size() > 0) {
                result.setMessage("Grouper Process : " + grouperparameter.size() + " DRG Claims");
                //result.setResult(resultdata.toString());
            } else {
                result.setMessage("NO DATA AVAILABLE TO PROCESS");
            }
            result.setSuccess(true);

        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //TRIGGER GROUPER METHOD TO GROUP
    @GET
    @Path("GetGrouper")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetGrouper() {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String tags = "FG";
        try {
            DRGWSResult getgrouperresult = gm.GetGrouper(datasource, tags);
            if (getgrouperresult.isSuccess()) {
                result.setMessage(getgrouperresult.getMessage());
                result.setSuccess(getgrouperresult.isSuccess());
                result.setResult(getgrouperresult.getResult());
            } else {
                result.setMessage(getgrouperresult.getMessage());
                result.setSuccess(getgrouperresult.isSuccess());
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //TEST POST REQUEST  
    @POST
    @Path("HTTPPostResquest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult HTTPPostResquest(final TestParamObject testparamobject) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            TestParamObject param = new TestParamObject();
            param.setClaimnumber(testparamobject.getClaimnumber());
            param.setDataafter(testparamobject.getDataafter());
            param.setDatein(testparamobject.getDatein());
            param.setDetailes(testparamobject.getDetailes());
            param.setSeries(testparamobject.getSeries());
            param.setStatus(testparamobject.getStatus());
            String p_series = param.getSeries();
            String p_claimnumber = param.getClaimnumber();
            String p_details = param.getDetailes();
            String p_status = param.getStatus();
            DRGWSResult testinsert = gm.InsertGrouperAuditTrail(datasource, p_series, p_claimnumber, p_details, p_status);
            if (testinsert.isSuccess()) {
                result.setMessage("HTTP Request successfully process");
                result.setSuccess(true);
            }
            result.setResult(utility.objectMapper().writeValueAsString(param));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS METHOD IS FOR SEEKER 
    //START OF FINAL METHOD FOR GROUPER
    @POST
    @Path("Seeker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult Seeker(final GrouperParameter grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        String[] sex = {"M", "F"};
        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);

        try {

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
            newGrouperParam.setProc(grouperparameter.getProc());
            newGrouperParam.setResult_id(grouperparameter.getResult_id());
            newGrouperParam.setSdx(grouperparameter.getSdx());
            newGrouperParam.setDischargeType(grouperparameter.getDischargeType());
            newGrouperParam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            //===================VALIDATION AREA ==================================
            //END PRIMARY CODES VALIDATION
            if (newGrouperParam.getGender().trim().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Invalid sex , Patient sex is required");
            } else if (!Arrays.asList(sex).contains(newGrouperParam.getGender().toUpperCase())) {
                drgresult.setDRG("26509");
                drgresult.setDRGName("Patient sex is not valid");
            }
            //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
            if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
                } else {
                    if (drgutility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) <= 0
                            && drgutility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 0) {
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
                    int oras = drgutility.ComputeTime(
                            newGrouperParam.getAdmissionDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeDischarge()));
                    int araw = drgutility.ComputeDay(newGrouperParam.getAdmissionDate(),
                            newGrouperParam.getDischargeDate());
                    int minuto = drgutility.MinutesCompute(
                            newGrouperParam.getAdmissionDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeDischarge()));
                    int taon = drgutility.ComputeYear(newGrouperParam.getAdmissionDate(),
                            newGrouperParam.getDischargeDate());
                    if (drgutility.ComputeLOS(newGrouperParam.getAdmissionDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeAdmission()),
                            newGrouperParam.getDischargeDate(),
                            drgutility.Convert24to12(newGrouperParam.getTimeDischarge())) == 0) {
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
            //END LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
            //REQUIRED DATE DATA NEEDED FOR GROUPER
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
            //END REQUIRED DATE DATA NEEDED FOR GROUPER
            // GET THE TIME DATA REQUIRED FOR THE GROUPER
            if (newGrouperParam.getTimeAdmission().isEmpty()) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            } else if (!utility.IsValidTime(newGrouperParam.getTimeAdmission())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
            if (newGrouperParam.getTimeDischarge().isEmpty()) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            } else if (!utility.IsValidTime(newGrouperParam.getTimeDischarge())) {
                drgresult.setDRG("26549");
                drgresult.setDRGName("Invalid LOS");
            }
            // END GET THE TIME DATA REQUIRED FOR THE GROUPER
            //VALIDATION FOR NEW BORN DATA
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
                    if (drgutility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) == 0
                            && drgutility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 28) {
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

            if (drgresult.getDRG() != null) {
                result.setMessage("UNGROUPABLE DRG CODES");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
            } else {
                //=================END OF VALIDATION AREA ================================
                DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, newGrouperParam);
                if (validateresult.isSuccess()) {
                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
                    //=================================================================================
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(drgResults));
                    result.setMessage(validateresult.getMessage());
                } else {
                    result.setMessage(validateresult.getMessage());
                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
                    result.setSuccess(false);
                }
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
