/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import drgseeker.utilities.SeekerMethods;
import drgseeker.utilities.SeekerUser;
import grouper.structures.DRGWSResult;
import grouper.utility.Cryptor;
import grouper.utility.GrouperMethod;
import grouper.utility.NamedParameterStatement;
import grouper.utility.Utility;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.mail.Session;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author DRG_SHADOWBILLING
 */
@Path("Seeker")
public class Seeker {
    
    public Seeker() {
    }
    
    @Resource(lookup = "jdbc/grouperuser")
    private DataSource dataSource;
    //-------------------------------------
    @Resource(lookup = "mail/acrgbmail")
    private Session session;
    
    private final Utility utility = new Utility();
    
    @GET
    @Path("GetServerDateTime")
    @Produces(MediaType.APPLICATION_JSON)
    public String GetServerDateTime() {
        String result = "";
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT SYSDATE FROM DUAL";
            NamedParameterStatement SDxVal = new NamedParameterStatement(connection, query);
            SDxVal.execute();
            ResultSet rest = SDxVal.executeQuery();
            if (rest.next()) {
                result = "SERVER DATE AND TIME : " + String.valueOf(utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a").format(rest.getDate("SYSDATE")));
            }
        } catch (SQLException ex) {
            result = ex.toString();
            Logger.getLogger(Seeker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Retrieves representation of an instance of Seeker.Seeker
     *
     * @param token
     * @return
     */
    @GET
    @Path("GetAllUser")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetAllUser(
            @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            DRGWSResult getResult = new SeekerMethods().GetAllUser(dataSource);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
    
    @GET
    @Path("GetUserByID/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetUserByID(
            @PathParam("puserid") String puserid,
            @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result.setMessage(new SeekerMethods().GetUserByID(dataSource, puserid).getMessage());
            result.setResult(new SeekerMethods().GetUserByID(dataSource, puserid).getResult());
            result.setSuccess(new SeekerMethods().GetUserByID(dataSource, puserid).isSuccess());
        }
        return result;
    }
    
    @POST
    @Path("ValidateCode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ValidateCode(
            @HeaderParam("token") String token,
            final SeekerUser user) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
//            DRGWSResult resultOtp = new SeekerMethods().VALIDATEOTP(dataSource, user.getEmail(), user.getPassword(), user.getOtp());
            result.setMessage(new SeekerMethods().VALIDATEOTP(dataSource, user.getEmail(), user.getPassword(), user.getOtp()).getMessage());
            result.setSuccess(new SeekerMethods().VALIDATEOTP(dataSource, user.getEmail(), user.getPassword(), user.getOtp()).isSuccess());
            result.setResult(new SeekerMethods().VALIDATEOTP(dataSource, user.getEmail(), user.getPassword(), user.getOtp()).getResult());
        }
        return result;
    }
    
    @POST
    @Path("InsertUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult InsertUser(
            @HeaderParam("token") String token,
            final SeekerUser user) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
//            DRGWSResult insertresult = new SeekerMethods().UserInsert(dataSource, user, session);
            result.setMessage(new SeekerMethods().UserInsert(dataSource, user, session).getMessage());
            result.setSuccess(new SeekerMethods().UserInsert(dataSource, user, session).isSuccess());
            result.setResult(new SeekerMethods().UserInsert(dataSource, user, session).getResult());
        }
        return result;
    }
    
    @PUT
    @Path("UpdateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateUser(
            @HeaderParam("token") String token,
            final SeekerUser user) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
//            DRGWSResult validate = new SeekerMethods().ValidateUserUpdate(dataSource, user);
            if (!new SeekerMethods().ValidateUserUpdate(dataSource, user).isSuccess()) {
                result.setMessage(new SeekerMethods().ValidateUserUpdate(dataSource, user).getMessage());
            } else {
//                DRGWSResult updateresult = new SeekerMethods().UserUpdate(dataSource, user);
                result.setMessage(new SeekerMethods().UserUpdate(dataSource, user).getMessage());
                result.setSuccess(new SeekerMethods().UserUpdate(dataSource, user).isSuccess());
                result.setResult(new SeekerMethods().UserUpdate(dataSource, user).getResult());
            }
        }
        return result;
    }
    
    @POST
    @Path("UserLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UserLogin(
            @HeaderParam("email") String email,
            @HeaderParam("password") String password,
            @HeaderParam("expire") String expire) {
//        DRGWSResult insertresult = new SeekerMethods().UserLogin(dataSource, email.trim(), password, expire.trim(), session);
        return new SeekerMethods().UserLogin(dataSource, email.trim(), password, expire.trim(), session);
    }
    
    @POST
    @Path("ForgetPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ForgetPassword(
            @HeaderParam("mail") String mail) {
//        DRGWSResult result = utility.DRGWSResult();
//        DRGWSResult Updatepass = new SeekerMethods().TestEmailSender(dataSource, mail, utility.GenerateRandomPassword(10), "FORGET", "OTP");
//        result.setMessage(Updatepass.getMessage());
//        result.setResult(Updatepass.getResult());
//        result.setSuccess(Updatepass.isSuccess());
//        return new SeekerMethods().TestEmailSender(dataSource, mail, utility.GenerateRandomPassword(10), "FORGET", "OTP");
        return new SeekerMethods().ForgatPassword(dataSource, mail, utility.GenerateRandomPassword(10), session);
    }
    
    @GET
    @Path("GetCaptchaCode")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetCaptchaCode() {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("OK");
        result.setSuccess(true);
        result.setResult(utility.Create2FACode());
        return result;
    }
    
    @GET
    @Path("GetRVS")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetRVS(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new GrouperMethod().SeekerRVS(dataSource);
        }
        return result;
    }
    
    @GET
    @Path("GetICD9CM")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetICD9CM(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new GrouperMethod().SeekerICD9cm(dataSource);
        }
        return result;
    }
    
    @GET
    @Path("GetDRG")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetDRG(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new GrouperMethod().SeekerDRG(dataSource);
        }
        return result;
    }
    
    @GET
    @Path("GetICD10")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetICD10(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new GrouperMethod().SeekerICD10(dataSource);
        }
        return result;
    }

//    @POST
//    @Path("TestInsertUser")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DRGWSResult TestInsertUser(
//            final SeekerUser user) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage(new SeekerMethods().TestUserInsert(dataSource, user).getMessage());
//        result.setSuccess(new SeekerMethods().TestUserInsert(dataSource, user).isSuccess());
//        result.setResult(new SeekerMethods().TestUserInsert(dataSource, user).getResult());
//        return result;
//    }
    //EncryptPasscode
    @GET
    @Path("EncryptPasscode/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ENCRYPT(@PathParam("password") String password) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("Passcode Enrypted");
        result.setResult(new Cryptor().encrypt(password, password, "SEEKER"));
        result.setSuccess(true);
        return result;
    }
    
    @GET
    @Path("GenerateToken/{username}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GenerateToken(
            @PathParam("username") String username,
            @PathParam("password") String password) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("OK");
        result.setSuccess(true);
        result.setResult(utility.GenerateToken(username, password, "480000"));
        return result;
    }

    //FOR HCF FRONT GUI
    @GET
    @Path("GetHcfToken")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetHcfToken() {
        return new SeekerMethods().GETTOKEN(dataSource);
    }
    
    @GET
    @Path("GETHCFSEEKERMODULE")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GETHCFSEEKERMODULE(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                DRGWSResult insertResult = new SeekerMethods().InsertToken(dataSource, token);
                if (insertResult.isSuccess()) {
                    URI uri = new URI(utility.GetString("SeekerModule"));
                    Desktop.getDesktop().browse(uri);
                } else {
                    
                    result.setMessage(insertResult.getMessage());
                }
//                Desktop desktop = Desktop.getDesktop();
//                desktop.browse(java.net.URI.create(utility.GetString("SeekerModule")));
//                Desktop desktop = Desktop.getDesktop();

            }
        } catch (IOException | URISyntaxException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Seeker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //https://www.172.21.53.144:8501/
//    @GET
//    @Path("TESTGETHCFSEEKERMODULE")
//    @Produces(MediaType.APPLICATION_JSON)
//    public DRGWSResult TESTGETHCFSEEKERMODULE() {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try {
//            Desktop desktop = Desktop.getDesktop();
//            URI uri = new URI(utility.GetString("SeekerModule"));
//            desktop.browse(uri);
//            URL myURL = new URL(utility.GetString("SeekerModule"));
//            URLConnection myURLConnection = myURL.openConnection();
//            myURLConnection.connect();
//        } catch (IOException | URISyntaxException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Seeker.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
}
