/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drgseeker.utilities;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class SeekerMethods {

    private final Utility utility = new Utility();
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    public DRGWSResult UserInsert(final DataSource dataSource, final SeekerUser seekerUser, final Session mailsession) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call MINOSUN.drgseeker.insertuser(:message,:code,:pemail,:ppassword,:prole,:udatecreated,:ucreatedby,:ustatus,:uname)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("pemail", seekerUser.getEmail().trim());
            statement.setString("ppassword", seekerUser.getPassword());
            statement.setString("prole", seekerUser.getRole().trim());
            statement.setTimestamp("p_datecreated", (Timestamp) new Timestamp(utility.StringToDate(seekerUser.getDatecreated()).getTime()));//tranch.getDatecreated());
            statement.setString("ucreatedby", seekerUser.getCreatedby());
            statement.setString("ustatus", "A".trim());
            statement.setString("uname", seekerUser.getName());
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(statement.getString("Message"));

            } else {
                result.setMessage(statement.getString("Message"));
            }
            new SeekerMethods().EmailSender(dataSource, seekerUser.getEmail().trim(), seekerUser.getPassword(), mailsession);

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult GetUserByID(final DataSource dataSource, final String puserid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := MINOSUN.drgseeker.GETUSERBYID(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                SeekerUser user = new SeekerUser();
                user.setName(resultset.getString("NAME"));
                user.setUserid(resultset.getString("USERID"));
                user.setEmail(resultset.getString("EMAIL"));
                user.setPassword(resultset.getString("PASSWORD"));
                user.setRole(resultset.getString("ROLE"));
                user.setStatus(resultset.getString("STATUS"));
                if (resultset.getString("DATECREATED") != null) {
                    user.setDatecreated(datetimeformat.format(resultset.getTimestamp("DATECREATED")));
                } else {
                    user.setDatecreated("");
                }
                if (resultset.getString("DATEUPDATED") != null) {
                    user.setDateupdated(datetimeformat.format(resultset.getTimestamp("DATEUPDATED")));
                } else {
                    user.setDateupdated("");
                }
                user.setUpdatedby(resultset.getString("UPDATEDBY"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(user));
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult GetAllUser(final DataSource dataSource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := MINOSUN.drgseeker.GETALLUSER(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<SeekerUser> userList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                SeekerUser user = new SeekerUser();
                user.setName(resultset.getString("NAME"));
                user.setUserid(resultset.getString("USERID"));
                user.setEmail(resultset.getString("EMAIL"));
                user.setPassword(resultset.getString("PASSWORD"));
                user.setRole(resultset.getString("ROLE"));
                user.setStatus(resultset.getString("STATUS"));
                if (resultset.getString("DATECREATED") != null) {
                    user.setDatecreated(datetimeformat.format(resultset.getTimestamp("DATECREATED")));
                } else {
                    user.setDatecreated("N/A");
                }
                if (resultset.getString("DATEUPDATED") != null) {
                    user.setDateupdated(datetimeformat.format(resultset.getTimestamp("DATEUPDATED")));
                } else {
                    user.setDateupdated("N/A");
                }
                if (resultset.getString("UPDATEDBY") != null) {
                    if (this.GetUserByID(dataSource, resultset.getString("UPDATEDBY").trim()).isSuccess()) {
                        SeekerUser getuser = utility.objectMapper().readValue(this.GetUserByID(dataSource, resultset.getString("UPDATEDBY")).getResult(), SeekerUser.class);
                        user.setUpdatedby(getuser.getName());
                    } else {
                        user.setUpdatedby("NO DATA FOUND");
                    }
                } else {
                    user.setUpdatedby("N/A");
                }
                if (resultset.getString("UPDATEDBY") != null) {
                    if (this.GetUserByID(dataSource, resultset.getString("CREATEDBY").trim()).isSuccess()) {
                        SeekerUser getuser = utility.objectMapper().readValue(this.GetUserByID(dataSource, resultset.getString("CREATEDBY")).getResult(), SeekerUser.class);
                        user.setCreatedby(getuser.getName());
                    } else {
                        user.setCreatedby("NO DATA FOUND");
                    }
                } else {
                    user.setCreatedby("N/A");
                }
                userList.add(user);
            }
            if (userList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(userList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult UserLogin(final DataSource dataSource, final String uemail, final String upassword, final String expire) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := MINOSUN.drgseeker.USERLOGIN(:uemail,:upassword); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uemail", uemail.trim());
            statement.setString("upassword", upassword.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                if (resultset.getString("STATUS").toUpperCase().trim().equals("A")) {
                    SeekerUser user = new SeekerUser();
                    user.setName(resultset.getString("NAME"));
                    user.setUserid(resultset.getString("USERID"));
                    user.setEmail(resultset.getString("EMAIL"));
                    user.setPassword(resultset.getString("PASSWORD"));
                    user.setRole(resultset.getString("ROLE"));
                    user.setStatus(resultset.getString("STATUS"));
                    if (resultset.getString("DATECREATED") != null) {
                        user.setDatecreated(datetimeformat.format(resultset.getTimestamp("DATECREATED")));
                    } else {
                        user.setDatecreated("");
                    }
                    if (resultset.getString("DATEUPDATED") != null) {
                        user.setDateupdated(datetimeformat.format(resultset.getTimestamp("DATEUPDATED")));
                    } else {
                        user.setDateupdated("");
                    }
                    if (resultset.getString("UPDATEDBY") != null) {
                        if (this.GetUserByID(dataSource, resultset.getString("UPDATEDBY").trim()).isSuccess()) {
                            SeekerUser getuser = utility.objectMapper().readValue(this.GetUserByID(dataSource, resultset.getString("UPDATEDBY")).getResult(), SeekerUser.class);
                            user.setUpdatedby(getuser.getName());
                        } else {
                            user.setUpdatedby("NO DATA FOUND");
                        }
                    } else {
                        user.setUpdatedby("NO DATA FOUND");
                    }

                    if (this.GetUserByID(dataSource, resultset.getString("CREATEDBY").trim()).isSuccess()) {
                        SeekerUser getuser = utility.objectMapper().readValue(this.GetUserByID(dataSource, resultset.getString("CREATEDBY")).getResult(), SeekerUser.class);
                        user.setCreatedby(getuser.getName());
                    } else {
                        user.setCreatedby("NO DATA FOUND");
                    }

                    user.setToken(utility.GenerateToken(uemail, resultset.getString("PASSWORD"), expire));
                    //-----------------------------------------------------------
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.objectMapper().writeValueAsString(user));
                } else {
                    result.setMessage("LOGIN CREDENTIAL IS CURRENTLY DISABLED BY THE SYSTEM ADMIN");
                }
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult UserUpdate(final DataSource dataSource, final SeekerUser seekerUser) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call MINOSUN.drgseeker.edituser(:message,:code,:pemail,:ppassword,:prole,:puserid,:ustatus,:uname,:udateupdated,:uupdatedby)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("pemail", seekerUser.getEmail().trim());
            statement.setString("ppassword", seekerUser.getPassword());
            statement.setString("prole", seekerUser.getRole().trim());
            statement.setString("puserid", seekerUser.getUserid().trim());
            statement.setString("ustatus", seekerUser.getStatus().trim());
            statement.setString("uname", seekerUser.getName());
            statement.setTimestamp("udateupdated", (Timestamp) new Timestamp(utility.StringToDate(seekerUser.getDateupdated()).getTime()));//tranch.getDatecreated());
            statement.setString("uupdatedby", seekerUser.getUpdatedby());
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(statement.getString("Message"));
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult EmailSender(
            final DataSource dataSource,
            final String uemail,
            final String randpass,
            final Session mailSession) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("noreply@philhealth.gov.ph", false));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(uemail.trim(), false));
            message.setSubject("GROUPER SEEKER");
            message.setSentDate(new Date());
            message.setText("ACCOUNT CREDENTIAL FOR PHL-DRGSEEKER USERNAME : " + uemail + " PASSWORD : " + randpass);
            Transport.send(message);
            result.setSuccess(true);
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;//  PHL-DRGSEEKER
    }

    public DRGWSResult ForgatPassword(
            final DataSource dataSource,
            final String uemail,
            final String randpass,
            final Session mailSession) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("noreply@philhealth.gov.ph", false));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(uemail.trim(), false));
            message.setSubject("GROUPER SEEKER");
            message.setSentDate(new Date());
            message.setText("PHL-DRGSEEKER NEW PASSWORD : " + randpass);
            Transport.send(message);
            result.setSuccess(true);
            result.setMessage("Account password successfully reseted and sent it to your email please check the new passcode ");
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;//  PHL-DRGSEEKER
    }

}
