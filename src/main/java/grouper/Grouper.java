/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.premdc.ProcessGrouperParameter;
import grouper.structures.DRGOutput;
import grouper.structures.DRGPayload;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.GrouperMethod;
import grouper.utility.NamedParameterStatement;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
//    private final DRGUtility drgutility = new DRGUtility();
//    private final ValidateFindMDC vfm = new ValidateFindMDC();
//    private final GrouperMethod gm = new GrouperMethod();

    /**
     * Retrieves representation of an instance of
     * drg.claims.drgapplication.Grouper
     *
     * @return an instance of java.lang.String
     */
    //Gget Server Data and Time
    @GET
    @Path("GetServerDateTime")
    @Produces(MediaType.APPLICATION_JSON)
    public String GetServerDateTime() {
        String result = "";
        SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
        try (Connection connection = datasource.getConnection()) {
            String query = "SELECT SYSDATE FROM DUAL";
            NamedParameterStatement SDxVal = new NamedParameterStatement(connection, query);
            SDxVal.execute();
            ResultSet rest = SDxVal.executeQuery();
            if (rest.next()) {
                result = "SERVER DATE AND TIME : " + String.valueOf(sdf.format(rest.getDate("SYSDATE")));
            }
        } catch (SQLException ex) {
            result = ex.toString();
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //FOR FRONT VALIDATION KEYVALUE PAIR METHOD
//    @POST
//    @Path("PROCESSGrouper")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DRGWSResult PROCESSGrouper(final GrouperParameter grouperparameter) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ValidateFindMDC vfm = new ValidateFindMDC();
//        try {
//            DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, grouperparameter);
//            if (validateresult.isSuccess()) {
//                DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
//                result.setMessage(drgResults.getDRGName());
//                result.setSuccess(true);
//                result.setResult(drgResults.getDRG());
//            } else {
//                result.setMessage(validateresult.getMessage());
//            }
//        } catch (IOException | ParseException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //START OF FINAL METHOD FOR GROUPER
    //START OF FINAL METHOD FOR GROUPER
    @POST
    @Path("ProcessGrouperParameter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ProcessGrouperParameter(final List<GrouperParameter> grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<DRGOutput> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        ProcessGrouperParameter processparameter = new ProcessGrouperParameter();
        try {
            for (int g = 0; g < grouperparameter.size(); g++) {
                DRGWSResult grouperResult = processparameter.ProcessGrouperParameter(datasource, grouperparameter.get(g));
                if (grouperResult.isSuccess()) {
                    DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
                    drgresultList.add(drgout);
                } else {
                    errorList.add(grouperResult.getMessage());
                }
            }
            if (grouperparameter.size() > 0) {
                result.setMessage("Data Process : " + grouperparameter.size() + " DRG Claims , Error Ecounter : " + errorList.toString());
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
            } else {
                result.setMessage("NO DATA AVAILABLE TO PROCESS");
            }

        } catch (IOException ex) {
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
        GrouperMethod gm = new GrouperMethod();
        try {
            DRGWSResult getgrouperresult = gm.GetGrouper(datasource, tags);
            result.setMessage(getgrouperresult.getMessage());
            result.setSuccess(getgrouperresult.isSuccess());
            result.setResult(getgrouperresult.getResult());
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //TEST POST REQUEST  
    @POST
    @Path("PhilSeeker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult PhilSeeker(@HeaderParam("token") String token,
            final List<GrouperParameter> grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<DRGOutput> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        ProcessGrouperParameter processparameter = new ProcessGrouperParameter();
        try {
            DRGWSResult GetPayLoad = utility.GetPayload(token);
            if (!GetPayLoad.isSuccess()) {
                result.setMessage(GetPayLoad.getMessage());
            } else {
                for (int g = 0; g < grouperparameter.size(); g++) {
                    DRGWSResult grouperResult = processparameter.ProcessGrouperParameter(datasource, grouperparameter.get(g));
                    if (grouperResult.isSuccess()) {
                        DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
                        drgresultList.add(drgout);
                    } else {
                        errorList.add(grouperResult.getMessage());
                    }
                }
                if (grouperparameter.size() > 0) {
                    result.setMessage("Data Process : " + grouperparameter.size() + " DRG Claims , Error Ecounter : " + errorList.toString());
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
                } else {
                    result.setMessage("NO DATA AVAILABLE TO PROCESS");
                }
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    //TEST POST REQUEST  

    @POST
    @Path("GenerateToken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GenerateToken(final DRGPayload payload) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            result.setMessage("OK");
            result.setResult(utility.GenerateToken(payload.getCode1(), payload.getCode2(), payload.getCode3()));
            result.setSuccess(true);
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS METHOD IS FOR SEEKER 
    //START OF FINAL METHOD FOR GROUPER
//    @POST
//    @Path("Seeker")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DRGWSResult Seeker(final GrouperParameter grouperparameter) throws IOException {
//        DRGWSResult result = utility.DRGWSResult();
//        String[] sex = {"M", "F"};
//        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try {
//
//            DRGOutput drgresult = utility.DRGOutput();
//            GrouperParameter newGrouperParam = utility.GrouperParameter();
//            //======================== TIME FORMAT CONVERTER ==============================
//            newGrouperParam.setExpireTime(grouperparameter.getExpireTime());
//            newGrouperParam.setTimeAdmission(grouperparameter.getTimeAdmission());
//            newGrouperParam.setAdmissionDate(grouperparameter.getAdmissionDate());
//            newGrouperParam.setTimeDischarge(grouperparameter.getTimeDischarge());
//            newGrouperParam.setDischargeDate(grouperparameter.getDischargeDate());
//            newGrouperParam.setExpiredDate(grouperparameter.getExpiredDate());
//            newGrouperParam.setBirthDate(grouperparameter.getBirthDate());
//            newGrouperParam.setClaimseries(grouperparameter.getClaimseries());
//            newGrouperParam.setGender(grouperparameter.getGender());
//            newGrouperParam.setIdseries(grouperparameter.getIdseries());
//            newGrouperParam.setPdx(grouperparameter.getPdx());
//            newGrouperParam.setProc(grouperparameter.getProc());
//            newGrouperParam.setResult_id(grouperparameter.getResult_id());
//            newGrouperParam.setSdx(grouperparameter.getSdx());
//            newGrouperParam.setDischargeType(grouperparameter.getDischargeType());
//            newGrouperParam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
//            //===================VALIDATION AREA ==================================
//            //END PRIMARY CODES VALIDATION
//            if (newGrouperParam.getGender().trim().isEmpty()) {
//                drgresult.setDRG("26509");
//                drgresult.setDRGName("Invalid sex , Patient sex is required");
//            } else if (!Arrays.asList(sex).contains(newGrouperParam.getGender().toUpperCase())) {
//                drgresult.setDRG("26509");
//                drgresult.setDRGName("Patient sex is not valid");
//            }
//            //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
//            if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
//                if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
//                } else {
//                    if (utility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) <= 0
//                            && utility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 0) {
//                        drgresult.setDRG("26539");
//                        drgresult.setDRGName("Invalid age");
//                    }
//                }
//            }
//            if (!newGrouperParam.getAdmissionDate().isEmpty()
//                    && !newGrouperParam.getDischargeDate().isEmpty()
//                    && !newGrouperParam.getTimeDischarge().isEmpty()
//                    && !newGrouperParam.getTimeAdmission().isEmpty()) {
//                if (!utility.IsValidDate(newGrouperParam.getBirthDate())) {
//                    drgresult.setDRG("26539");
//                    drgresult.setDRGName("Invalid Age");
//                } else if (!utility.IsValidDate(newGrouperParam.getAdmissionDate()) || !utility.IsValidDate(newGrouperParam.getDischargeDate())) {
//                    drgresult.setDRG("26549");
//                    drgresult.setDRGName("Invalid LOS");
//                } else if (!utility.IsValidTime(newGrouperParam.getTimeAdmission()) || !utility.IsValidTime(newGrouperParam.getTimeDischarge())) {
//                    drgresult.setDRG("26549");
//                    drgresult.setDRGName("Invalid LOS");
//                } else {
//                    int oras = utility.ComputeTime(
//                            newGrouperParam.getAdmissionDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
//                            newGrouperParam.getDischargeDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeDischarge()));
//                    int araw = utility.ComputeDay(newGrouperParam.getAdmissionDate(),
//                            newGrouperParam.getDischargeDate());
//                    int minuto = utility.MinutesCompute(
//                            newGrouperParam.getAdmissionDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
//                            newGrouperParam.getDischargeDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeDischarge()));
//                    int taon = utility.ComputeYear(newGrouperParam.getAdmissionDate(),
//                            newGrouperParam.getDischargeDate());
//                    if (utility.ComputeLOS(newGrouperParam.getAdmissionDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeAdmission()),
//                            newGrouperParam.getDischargeDate(),
//                            utility.Convert24to12(newGrouperParam.getTimeDischarge())) == 0) {
//                        if (araw <= 0 && oras < 0) {
//                            drgresult.setDRG("26549");
//                            drgresult.setDRGName("Invalid LOS");
//                        }
//                    } else if (taon <= 0 && araw < 0) {
//                        drgresult.setDRG("26549");
//                        drgresult.setDRGName("Invalid LOS");
//                    }
//                }
//            }
//            //END LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
//            //REQUIRED DATE DATA NEEDED FOR GROUPER
//            if (newGrouperParam.getAdmissionDate().isEmpty()) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            } else if (!utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            }
//            if (newGrouperParam.getDischargeDate().isEmpty()) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("DischargeDate is required");
//            } else if (!utility.IsValidDate(newGrouperParam.getDischargeDate())) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            }
//            if (newGrouperParam.getBirthDate().isEmpty()) {
//                drgresult.setDRG("26539");
//                drgresult.setDRGName("Invalid Age");
//            } else if (!utility.IsValidDate(newGrouperParam.getBirthDate())) {
//                drgresult.setDRG("26539");
//                drgresult.setDRGName("Invalid Age");
//            }
//            //END REQUIRED DATE DATA NEEDED FOR GROUPER
//            // GET THE TIME DATA REQUIRED FOR THE GROUPER
//            if (newGrouperParam.getTimeAdmission().isEmpty()) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            } else if (!utility.IsValidTime(newGrouperParam.getTimeAdmission())) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            }
//            if (newGrouperParam.getTimeDischarge().isEmpty()) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            } else if (!utility.IsValidTime(newGrouperParam.getTimeDischarge())) {
//                drgresult.setDRG("26549");
//                drgresult.setDRGName("Invalid LOS");
//            }
//            // END GET THE TIME DATA REQUIRED FOR THE GROUPER
//            //VALIDATION FOR NEW BORN DATA
//            if (newGrouperParam.getDischargeType().isEmpty()) {
//                drgresult.setDRG("26509");
//                drgresult.setDRGName("Disposition is empty");
//            } else if (!Arrays.asList(disposition).contains(newGrouperParam.getDischargeType())) {
//                drgresult.setDRG("26509");
//                drgresult.setDRGName("Disposition is not valid");
//            }
//            if (!newGrouperParam.getBirthDate().isEmpty() && !newGrouperParam.getAdmissionDate().isEmpty()) {
//                if (!utility.IsValidDate(newGrouperParam.getBirthDate()) || !utility.IsValidDate(newGrouperParam.getAdmissionDate())) {
//                } else {
//                    if (utility.ComputeYear(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) == 0
//                            && utility.ComputeDay(newGrouperParam.getBirthDate(), newGrouperParam.getAdmissionDate()) < 28) {
//                        if (newGrouperParam.getAdmissionWeight() != null) {
//                            if (!utility.isValidNumeric(newGrouperParam.getAdmissionWeight())) {
//                                drgresult.setDRG("26509");
//                                drgresult.setDRGName("Admission Weight is not valid");
//                            } else if (Double.parseDouble(newGrouperParam.getAdmissionWeight()) < 0.3) {
//                                drgresult.setDRG("26509");
//                                drgresult.setDRGName("Admission Weight less than 0.3 kg is not valid");
//                            }
//                        } else {
//                            drgresult.setDRG("26509");
//                            drgresult.setDRGName("Admission Weight is empty");
//                        }
//                    }
//                }
//            }
//
//            if (drgresult.getDRG() != null) {
//                result.setMessage("UNGROUPABLE DRG CODES");
//                result.setSuccess(true);
//                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
//            } else {
//                ValidateFindMDC vfm = new ValidateFindMDC();
//                //=================END OF VALIDATION AREA ================================
//                DRGWSResult validateresult = vfm.ValidateFindMDC(datasource, newGrouperParam);
//                if (validateresult.isSuccess()) {
//                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
//                    //=================================================================================
//                    result.setSuccess(true);
//                    result.setResult(utility.objectMapper().writeValueAsString(drgResults));
//                    result.setMessage(validateresult.getMessage());
//                } else {
//                    result.setMessage(validateresult.getMessage());
//                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
//                }
//            }
//        } catch (IOException | ParseException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
}
