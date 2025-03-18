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
 * @author MINOSUN
 */
@Path("Grouper")
@RequestScoped
public class Grouper {

    public Grouper() {
    }

    @Resource(lookup = "jdbc/grouperuser")
    private DataSource datasource;
    private final Utility utility = new Utility();

    //TRIGGER GROUPER METHOD TO GROUP
    @GET
    @Path("GetGrouper")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetGrouper() {
        DRGWSResult result = utility.DRGWSResult();
        DRGWSResult getgrouperresult = new GrouperMethod().GetGrouper(datasource, "FG".trim());
        result.setMessage(getgrouperresult.getMessage());
        result.setSuccess(getgrouperresult.isSuccess());
        result.setResult(getgrouperresult.getResult());
        return result;
    }

    //SET DEADLINE  FOR ITMD   
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
        try {
            //------------------------------------------------------------------
            
            //------------------------------------------------------------------
            for (int g = 0; g < grouperparameter.size(); g++) {
                DRGWSResult grouperResult = new ProcessGrouperParameter().ProcessGrouperParameter(datasource, grouperparameter.get(g));
                if (grouperResult.isSuccess()) {
                    DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
                    drgresultList.add(drgout);
                } else {
                    errorList.add(grouperResult.getMessage());
                }
            }
            //------------------------------------------------------------------
            if (grouperparameter.size() > 0) {
                result.setMessage("Data Process : " + grouperparameter.size() + " DRG Claims , Error Ecounter : " + errorList.toString());
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
            } else {
                result.setMessage("NO DATA AVAILABLE TO PROCESS");
            }
            //------------------------------------------------------------------
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS METHOD IS FOR DRG CLAIMS SUBMISSION FULL IMPLEMENTATION
    @POST
    @Path("DRGClaimsData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult DRGClaimsData(final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        DRGWSResult grouperResult = new ProcessGrouperParameter().ProcessGrouperParameter(datasource, grouperparameter);
        result.setMessage(grouperResult.getMessage());
        result.setResult(grouperResult.getResult());
        result.setSuccess(grouperResult.isSuccess());
        return result;
    }

    @POST
    @Path("PhilSeeker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult PhilSeeker(@HeaderParam("token") String token, final List<GrouperParameter> grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<DRGOutput> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        ProcessGrouperParameter processparameter = new ProcessGrouperParameter();
        try {
            DRGWSResult GetPayLoad = utility.GetPayload(datasource, token);
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

    @POST
    @Path("GenerateToken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GenerateToken(final DRGPayload payload) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("OK");
        result.setResult(utility.GenerateToken(payload.getCode1(), payload.getCode2(), payload.getCode3()));
        result.setSuccess(true);
        return result;
    }
}
