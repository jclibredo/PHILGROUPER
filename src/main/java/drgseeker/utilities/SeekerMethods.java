/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drgseeker.utilities;

import grouper.structures.DRGWSResult;
import grouper.utility.Cryptor;
import grouper.utility.Utility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
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
import javax.mail.PasswordAuthentication;

/**
 *
 * @author DRG_SHADOWBILLING
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
            if (this.GetUserByUsername(dataSource, seekerUser.getEmail()).isSuccess()) {
                result.setMessage("Email is already exist");
            } else {
                String encryptpword = new Cryptor().encrypt(seekerUser.getPassword(), seekerUser.getPassword(), "SEEKER");
                CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.drgseeker.insertuser(:message,:code,:pemail,:ppassword,:prole,:udatecreated,:ucreatedby,:ustatus,:uname)");
                statement.registerOutParameter("Message", OracleTypes.VARCHAR);
                statement.registerOutParameter("Code", OracleTypes.INTEGER);
                statement.setString("pemail", seekerUser.getEmail().trim());
                statement.setString("ppassword", encryptpword);
                statement.setString("prole", seekerUser.getRole().trim());
                statement.setTimestamp("p_datecreated", (Timestamp) new Timestamp(utility.StringToDate(seekerUser.getDatecreated()).getTime()));//tranch.getDatecreated());
                statement.setString("ucreatedby", seekerUser.getCreatedby());
                statement.setString("ustatus", "A".trim());
                statement.setString("uname", seekerUser.getName());
                statement.execute();
                if (statement.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage(statement.getString("Message"));
                    this.EmailSender(dataSource, seekerUser.getEmail().trim(), seekerUser.getPassword(), mailsession);
                } else {
                    result.setMessage(statement.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult TestUserInsert(
            final DataSource dataSource,
            final SeekerUser seekerUser) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (this.GetUserByUsername(dataSource, seekerUser.getEmail()).isSuccess()) {
                result.setMessage("Email is already exist");
            } else {
                String encryptpword = new Cryptor().encrypt(seekerUser.getPassword(), seekerUser.getPassword(), "SEEKER");
                CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.drgseeker.insertuser(:message,:code,:pemail,:ppassword,:prole,:udatecreated,:ucreatedby,:ustatus,:uname)");
                statement.registerOutParameter("Message", OracleTypes.VARCHAR);
                statement.registerOutParameter("Code", OracleTypes.INTEGER);
                statement.setString("pemail", seekerUser.getEmail().trim());
                statement.setString("ppassword", encryptpword);
                statement.setString("prole", seekerUser.getRole().trim());
                statement.setTimestamp("p_datecreated", (Timestamp) new Timestamp(utility.StringToDate(seekerUser.getDatecreated()).getTime()));//tranch.getDatecreated());
                statement.setString("ucreatedby", seekerUser.getCreatedby());
                statement.setString("ustatus", "A".trim());
                statement.setString("uname", seekerUser.getName());
                statement.execute();
                if (statement.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage(statement.getString("Message"));
                    this.TestEmailSender(dataSource, seekerUser.getEmail().trim(), seekerUser.getPassword().trim());
                } else {
                    result.setMessage(statement.getString("Message"));
                }
            }
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.drgseeker.GETUSERBYID(:puserid); end;");
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

    public DRGWSResult GetUserByUsername(final DataSource dataSource, final String pusername) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.drgseeker.GETUSERBYUSERNAME(:pusername); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pusername", pusername.trim());
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
                if (resultset.getString("CREATEDBY") != null) {
                    if (this.GetUserByID(dataSource, resultset.getString("CREATEDBY").trim()).isSuccess()) {
                        SeekerUser getuser = utility.objectMapper().readValue(this.GetUserByID(dataSource, resultset.getString("CREATEDBY")).getResult(), SeekerUser.class);
                        user.setCreatedby(getuser.getName());
                    } else {
                        user.setCreatedby("NO DATA FOUND");
                    }
                } else {
                    user.setCreatedby("N/A");
                }
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.drgseeker.GETALLUSER(); end;");
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
                if (resultset.getString("CREATEDBY") != null) {
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

    public DRGWSResult UserLogin(
            final DataSource dataSource,
            final String uemail,
            final String upassword,
            final String expire) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRGWSResult getUserDetails = this.GetUserByUsername(dataSource, uemail.trim());
            if (getUserDetails.isSuccess()) {
                SeekerUser userA = utility.objectMapper().readValue(getUserDetails.getResult(), SeekerUser.class);
                String decryptString = new Cryptor().decrypt(userA.getPassword(), upassword, "SEEKER");
                if (decryptString.trim().equals(upassword)) {
                    if (userA.getStatus().trim().equals("A")) {
                        SeekerUser user = new SeekerUser();
                        user.setUserid(userA.getUserid());
                        user.setCreatedby(userA.getCreatedby());
                        user.setDatecreated(userA.getDatecreated());
                        user.setDateupdated(userA.getDateupdated());
                        user.setEmail(userA.getEmail());
                        user.setName(userA.getName());
                        user.setPassword(userA.getPassword());
                        user.setRole(userA.getRole());
                        user.setStatus(userA.getStatus());
                        user.setToken(utility.GenerateToken(uemail, upassword, expire));
                        user.setUpdatedby(userA.getUpdatedby());
                        result.setMessage("OK");
                        result.setSuccess(true);
                        result.setResult(utility.objectMapper().writeValueAsString(user));
                    } else {
                        result.setMessage("LOGIN CREDENTIAL IS CURRENTLY DISABLED BY THE SYSTEM ADMIN");
                    }
                } else {
                    result.setMessage("INVALID USERNAME OR PASSWORD");
                }
            } else {
                result.setMessage("INVALID USERNAME OR PASSWORD");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult ValidateUserUpdate(final DataSource dataSource, final SeekerUser seekerUser) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!seekerUser.getUserid().isEmpty()) {
                DRGWSResult getUserbyid = this.GetUserByID(dataSource, seekerUser.getUserid());
                if (getUserbyid.isSuccess()) {
                    SeekerUser user = utility.objectMapper().readValue(getUserbyid.getResult(), SeekerUser.class);
                    if (user.getEmail().trim().equals(seekerUser.getEmail().trim())) {
                        result.setSuccess(true);
                    } else {
                        DRGWSResult getCount = this.COUNTEMAIL(dataSource, seekerUser.getEmail());
                        if (getCount.isSuccess()) {
                            if (getCount.getMessage().equals("0")) {
                                result.setSuccess(true);
                            } else {
                                if (Integer.parseInt(getCount.getMessage()) > 0) {
                                    result.setMessage("Email has duplicate");
                                } else {
                                    result.setSuccess(true);
                                }
                            }
                        } else {
                            result.setSuccess(true);
                        }
                    }
                } else {
                    result.setMessage("User info not found");
                }
            } else {
                result.setMessage("Account can't be empty");
            }
        } catch (IOException ex) {
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
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.drgseeker.edituser(:message,:code,:pemail,:ppassword,:prole,:puserid,:ustatus,:uname,:udateupdated,:uupdatedby)");
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

    public DRGWSResult COUNTEMAIL(final DataSource dataSource, final String pusername) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.drgseeker.COUNTEMAIL(:pusername); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pusername", pusername.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setMessage(statement.getString("TOTALEMAIL"));
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
            DRGWSResult getAccountID = this.GetUserByUsername(dataSource, uemail);
            if (getAccountID.isSuccess()) {
                SeekerUser user = utility.objectMapper().readValue(getAccountID.getResult(), SeekerUser.class);
                DRGWSResult updatepassword = this.UPDATEPASSWORD(dataSource, user.getUserid(), uemail, new Cryptor().encrypt(randpass, randpass, "SEEKER"));
                if (updatepassword.isSuccess()) {
                    result.setSuccess(true);
                    result.setMessage("Account password successfully resetted and sent it to your email please check the new passcode ");
                    Message message = new MimeMessage(mailSession);
                    message.setFrom(new InternetAddress("noreply@philhealth.gov.ph", false));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(uemail.trim(), false));
                    message.setSubject("GROUPER SEEKER");
                    message.setSentDate(new Date());
                    message.setText("PHL-DRGSEEKER NEW PASSWORD : " + randpass);
                    Transport.send(message);
                } else {
                    result.setMessage(updatepassword.getResult());
                }
            } else {
                result.setMessage("Email not found");
            }
        } catch (IOException | MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;//  PHL-DRGSEEKER
    }

    public DRGWSResult UPDATEPASSWORD(
            final DataSource dataSource,
            final String puserid,
            final String pemail,
            final String ppassword) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.drgseeker.UPDATEPASSWORD(:message,:code,:puserid,:pemail,:ppasswordd)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("puserid", puserid.trim());
            statement.setString("pemail", pemail.trim());
            statement.setString("ppassword", new Cryptor().encrypt(ppassword, ppassword, "SEEKER"));
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

    public DRGWSResult TestEmailSender(
            final DataSource dataSource,
            final String emailreciever,
            final String randpass) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try {
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");
            // Get the Session object.// and pass username and password
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.pass"));
                    return new PasswordAuthentication("roland.aboga@gmail.com", "bvyf bnrj nire gbvb");
                }
            });
            // Used to debug SMTP issues
            session.setDebug(true);
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no_reply@phic.gov.ph", "no_reply@phic.gov.ph"));
            message.setReplyTo(InternetAddress.parse("no_reply@phic.gov.ph", false));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailreciever.trim()));
            // Set Subject: header field
            message.setSubject("DRG Claims");
            //DRGWSResult validateUsername = new UserServicesGET().GETUSERBYPARAM(dataSource, "0", emailreciever, "OTHERS");
            DRGWSResult validateUsername = this.GetUserByUsername(dataSource, emailreciever);
            if (!validateUsername.isSuccess()) {
                result.setMessage(emailreciever + "User email not found");
            } else {
                SeekerUser user = utility.objectMapper().readValue(validateUsername.getResult(), SeekerUser.class);
                if (randpass.length() > 0) {
                    // Now set the actual message
                    //message.setContent(utility.EmailSenderContent(email.getEmailto().trim(), randpass), "text/html");
                    message.setText("Username : " + emailreciever + " Passcode " + randpass);
                    DRGWSResult updatepassword = this.UPDATEPASSWORD(dataSource, user.getUserid(), emailreciever, randpass);
                    if (updatepassword.isSuccess()) {
                        result.setSuccess(true);
                        result.setMessage("Account credentials successfully sent to " + emailreciever.trim());
                        Transport.send(message);
                    } else {
                        result.setMessage(updatepassword.getMessage());
                    }
                } else {
                    // Now set the actual message
                    String newPass = utility.GenerateRandomPassword(10);
                    //message.setContent(utility.EmailSenderContent(email.getEmailto(), newPass), "text/html");
                    message.setText("Username : " + emailreciever + " Passcode " + newPass);
                    DRGWSResult updatepassword = this.UPDATEPASSWORD(dataSource, user.getUserid(), emailreciever, newPass);
                    if (updatepassword.isSuccess()) {
                        result.setSuccess(true);
                        result.setMessage("Account credentials successfully updated and sent to " + emailreciever.trim());
                        Transport.send(message);
                    } else {
                        result.setMessage(updatepassword.getMessage());
                    }
                }
            }
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
