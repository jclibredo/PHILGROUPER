/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.cache.GrouperCache;
import grouper.methods.cache.I10Cache;
import grouper.methods.premdc.ProcessGrouperParameter;
import grouper.methods.validation.GetGrouper;
import grouper.structures.DRGOutput;
import grouper.structures.DRGPayload;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.NamedParameterStatement;
import grouper.utility.Utility;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private final Logger logger = (Logger) LogManager.getLogger(Grouper.class);
    private final Utility utility = new Utility();

    //TRIGGER GROUPER METHOD TO GROUP
    @GET
    @Path("GetGrouper")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetGrouper() {
        return new GetGrouper().GetGrouper(datasource, "FG".trim());
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
            result = "Something went wrong";
            logger.info("Executing GetServerDateTime");
            logger.error("Error in GetServerDateTime: {}", ex.getMessage(), ex);
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
        ArrayList<String> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        try {
            for (int g = 0; g < grouperparameter.size(); g++) {
                DRGWSResult grouperResult = new ProcessGrouperParameter().ProcessGrouperParameter(datasource, grouperparameter.get(g));
                if (grouperResult.isSuccess()) {
//                    DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
//                    drgresultList.add(drgout);
                    drgresultList.add(grouperResult.getResult());
                } else {
                    errorList.add(grouperResult.getMessage());
                }
            }
            if (grouperparameter.size() > 0) {
                result.setMessage("Data Process : " + grouperparameter.size() + " DRG Claims , Error Ecounter : " + (errorList.isEmpty() ? "0" : errorList.toString()));
                result.setSuccess(true);
//                result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
                result.setResult(drgresultList.toString());
            } else {
                result.setMessage("NO DATA AVAILABLE TO PROCESS");
            }
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing ProcessGrouperParameter");
            logger.error("Error in ProcessGrouperParameter: {}", ex.getMessage(), ex);
        }
        return result;
    }

    @POST
    @Path("DRGClaimsData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult DRGClaimsData(final GrouperParameter grouperparameter) {
        return new ProcessGrouperParameter().ProcessGrouperParameter(datasource, grouperparameter);
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
            result.setMessage("Something went wrong");
            logger.info("Executing PhilSeeker");
            logger.error("Error in PhilSeeker: {}", ex.getMessage(), ex);
        }
        return result;
    }

    @POST
    @Path("GenerateToken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GenerateToken(final DRGPayload payload) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult(utility.GenerateToken(payload.getCode1(),
                payload.getCode2(), utility.GetString("OtpExpiration").getResult()));
        result.setSuccess(true);
        return result;
    }

    @GET
    @Path("GetVersion")
    @Produces(MediaType.TEXT_PLAIN)
    public String GetSoftwareVersion() {
        String result;
        Properties properties = new Properties();
        try (InputStream input = Grouper.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                result = "Sorry unable to find version.properties";
            } else {
                properties.load(input);
                result = properties.getProperty("project.version");
            }
        } catch (IOException ex) {
            result = ex.toString();
            logger.info("Executing GetVersion");
            logger.error("Error in GetVersion: {}", ex.getMessage(), ex);
        }
        return result;
    }

    @GET
    @Path("download-log")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadLogFile() {
        // This MUST match the fileName in the XML above
        File logFile = new File("logs/drg-system/app-log.log");
        if (!logFile.exists()) {
            return Response.status(404).entity("Log file not found at " + logFile.getAbsolutePath()).build();
        }
        return Response.ok(logFile)
                .header("Content-Disposition", "attachment; filename=drg-api-system.log")
                .build();
    }

//    @GET
//    @Path("GetJsonFile")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String GetJsonFile() throws IOException {
//        // Get the data from memory (zero disk IO)
//        List<GrouperParameter> myData = GrouperCache.getData();
//        System.out.println(utility.objectMapper().writeValueAsString(myData));
//        return null;
//    }
    @GET
    @Path("GetJsonFile")
    @Produces(MediaType.APPLICATION_JSON) // Better for JSON data
    public String GetJsonFile() throws IOException {

        
        //TEST GROUPER PARAMETER
//        List<GrouperParameter> myData = GrouperCache.getData();
//        if (myData.isEmpty()) {
//            return "[]"; // Return empty array string if no data
//        }
//        String jsonOutput = utility.objectMapper().writeValueAsString(myData);
//        // This prints to the SERVER console (IDE)
//        System.out.println("Outputting data: " + myData.size());


        //TEST I10
        List<ICD10PreMDCResult> myData = I10Cache.getData();
        if (myData.isEmpty()) {
            return "[]"; // Return empty array string if no data
        }
        String jsonOutput = utility.objectMapper().writeValueAsString(myData);
        // This prints to the SERVER console (IDE)
        System.out.println("Outputting data: " + myData.size());
        return jsonOutput;
    }

    @GET
    @Path("GetByClaim")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByClaim(@QueryParam("id") String claimId) throws IOException {
        // 1. Get the data from memory
        List<GrouperParameter> myData = GrouperCache.getData();

        // 2. Use Stream to find the specific claim
        Optional<GrouperParameter> result = myData.stream()
                .filter(p -> p.getClaimseries() != null && p.getClaimseries().equals(claimId))
                .findFirst();

        // 3. Return the result if found, otherwise return 404
        if (result.isPresent()) {
            String json = utility.objectMapper().writeValueAsString(result.get());
            return Response.ok(json).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Claim series not found\"}")
                    .build();
        }
    }
}
