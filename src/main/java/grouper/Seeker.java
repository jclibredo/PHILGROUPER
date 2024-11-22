/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import drgseeker.utilities.SeekerMethods;
import drgseeker.utilities.SeekerUser;
import grouper.structures.DRGWSResult;
import grouper.utility.GrouperMethod;
import grouper.utility.NamedParameterStatement;
import grouper.utility.Utility;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author MinoSun
 */
@Path("Seeker")
public class Seeker {

    public Seeker() {
    }

    @Resource(lookup = "jdbc/drgsbuser")
    private DataSource dataSource;
    //-------------------------------------
    @Resource(lookup = "mail/acrgbmail")
    private Session session;

    private final Utility utility = new Utility();

    private final ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();

    @GET
    @Path(value = "GetServerDateTime")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetServerDateTime(@Suspended final AsyncResponse asyncResponse) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetServerDateTime());
            }
        });
    }

    public String doGetServerDateTime() {
        String result = "";
        SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT SYSDATE FROM DUAL";
            NamedParameterStatement SDxVal = new NamedParameterStatement(connection, query);
            SDxVal.execute();
            ResultSet rest = SDxVal.executeQuery();
            if (rest.next()) {
                result = "SERVER DATE AND TIME : " + String.valueOf(sdf.format(rest.getDate("SYSDATE")));
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
     * @param asyncResponse
     * @param token
     */
    @GET
    @Path(value = "GetAllUser")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetAllUser(
            @Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetAllUser(token));
            }
        });
    }

    public DRGWSResult doGetAllUser(
            @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
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
    public void GetUserByID(
            @Suspended final AsyncResponse asyncResponse,
            @PathParam(value = "puserid") final String puserid,
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetUserByID(puserid, token));
            }
        });
    }

    private DRGWSResult doGetUserByID(@PathParam("puserid") String puserid, @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            DRGWSResult getResult = new SeekerMethods().GetUserByID(dataSource, puserid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @POST
    @Path(value = "InsertUser")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    public void InsertUser(
            @Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "token") final String token,
            final SeekerUser user) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doInsertUser(token, user));
            }
        });
    }

    public DRGWSResult doInsertUser(
            @HeaderParam("token") String token,
            final SeekerUser user) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            DRGWSResult insertresult = new SeekerMethods().UserInsert(dataSource, user, session);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    @PUT
    @Path(value = "UpdateUser")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    public void UpdateUser(
            @Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "token") final String token,
            final SeekerUser user) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doUpdateUser(token, user));
            }
        });
    }

    public DRGWSResult doUpdateUser(
            @HeaderParam("token") String token,
            final SeekerUser user) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            DRGWSResult updateresult = new SeekerMethods().UserUpdate(dataSource, user);
            result.setMessage(updateresult.getMessage());
            result.setSuccess(updateresult.isSuccess());
            result.setResult(updateresult.getResult());
        }
        return result;
    }

    @POST
    @Path(value = "UserLogin")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    public void UserLogin(
            @Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "email") final String email,
            @HeaderParam(value = "password") final String password,
            @HeaderParam(value = "expire") final String expire) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doUserLogin(email, password, expire));
            }
        });
    }

    public DRGWSResult doUserLogin(
            @HeaderParam("email") String email,
            @HeaderParam("password") String password,
            @HeaderParam("expire") String expire) {
        DRGWSResult insertresult = new SeekerMethods().UserLogin(dataSource, email.trim(), password, expire.trim());
        return insertresult;
    }

    @POST
    @Path(value = "ForgetPassword")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(value = MediaType.APPLICATION_JSON)
    public void ForgetPassword(
            @Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "mail") final String mail) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doForgetPassword(mail));
            }
        });
    }

    public DRGWSResult doForgetPassword(
            @HeaderParam("mail") String mail) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        new SeekerMethods().ForgatPassword(dataSource, mail, utility.GenerateRandomPassword(10), session);
        return result;
    }

    @GET
    @Path(value = "GetCaptchaCode")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetCaptchaCode(@Suspended final AsyncResponse asyncResponse) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetCaptchaCode());
            }
        });
    }

    private DRGWSResult doGetCaptchaCode() {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("OK");
        result.setSuccess(true);
        result.setResult(utility.Create2FACode());
        return result;
    }

    @GET
    @Path(value = "GetRVS")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetRVS(@Suspended final AsyncResponse asyncResponse,
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetRVS(token));
            }
        });
    }

    private DRGWSResult doGetRVS(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            result = new GrouperMethod().SeekerRVS(dataSource);
        }
        return result;
    }

    @GET
    @Path(value = "GetICD9CM")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetICD9CM(
            @Suspended final AsyncResponse asyncResponse, 
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetICD9CM(token));
            }
        });
    }

    private DRGWSResult doGetICD9CM(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            result = new GrouperMethod().SeekerICD9cm(dataSource);
        }
        return result;
    }

    @GET
    @Path(value = "GetDRG")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetDRG(
            @Suspended final AsyncResponse asyncResponse, 
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetDRG(token));
            }
        });
    }

    private DRGWSResult doGetDRG(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            result = new GrouperMethod().SeekerDRG(dataSource);
        }
        return result;
    }

    @GET
    @Path(value = "GetICD10")
    @Produces(value = MediaType.APPLICATION_JSON)
    public void GetICD10(
            @Suspended final AsyncResponse asyncResponse, 
            @HeaderParam(value = "token") final String token) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncResponse.resume(doGetICD10(token));
            }
        });
    }

    private DRGWSResult doGetICD10(@HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            result = new GrouperMethod().SeekerICD10(dataSource);
        }
        return result;
    }

}
