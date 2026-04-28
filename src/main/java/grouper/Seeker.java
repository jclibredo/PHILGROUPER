/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import drgseeker.utilities.SeekerMethods;
import drgseeker.utilities.SeekerUser;
import grouper.methods.seeker.SeekerDRG;
import grouper.methods.seeker.SeekerICD10;
import grouper.methods.seeker.SeekerICD9cm;
import grouper.methods.seeker.SeekerRVS;
import grouper.structures.DRGWSResult;
import grouper.utility.Cryptor;
import grouper.utility.Utility;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
 * @author MINOSUN
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
            result = new SeekerMethods().GetAllUser(dataSource);
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
            result = new SeekerMethods().GetUserByID(dataSource, puserid);
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
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new SeekerMethods().VALIDATEOTP(dataSource, user.getEmail(), user.getPassword(), user.getOtp());
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
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            return new SeekerMethods().UserInsert(dataSource, user, session);
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
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            if (!new SeekerMethods().ValidateUserUpdate(dataSource, user).isSuccess()) {
                result.setMessage(new SeekerMethods().ValidateUserUpdate(dataSource, user).getMessage());
            } else {
                result = new SeekerMethods().UserUpdate(dataSource, user);
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
        return new SeekerMethods().UserLogin(dataSource, email.trim(), password, expire.trim(), session);
    }

    @POST
    @Path("ForgetPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ForgetPassword(
            @HeaderParam("mail") String mail) {
        return new SeekerMethods().ForgatPassword(dataSource, mail, utility.GenerateRandomPassword(10));
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
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new SeekerRVS().SeekerRVS(dataSource);
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
            result = new SeekerICD9cm().SeekerICD9cm(dataSource);
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
            result = new SeekerDRG().SeekerDRG(dataSource);
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
            result = new SeekerICD10().SeekerICD10(dataSource);
        }
        return result;
    }

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

                    URI uri = new URI(utility.GetString("SeekerModule").getResult());

                    Desktop.getDesktop().browse(uri);
                } else {
                    result = insertResult;
                }
            }
        } catch (IOException | URISyntaxException ex) {
            result.setMessage("Something went wrong");
        }
        return result;
    }
}
