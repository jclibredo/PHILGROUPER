/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.utility;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import okhttp3.OkHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author MinoSun
 */
@ApplicationScoped
@Singleton
public class Utility {

    public Utility() {
    }

    private Pattern pattern;
    private Matcher matcher;

    private static final String email_pattern
            = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public DRGWSResult DRGWSResult() {
        return new DRGWSResult();
    }

    public GrouperParameter GrouperParameter() {
        return new GrouperParameter();
    }

    
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public DRGOutput DRGOutput() {
        return new DRGOutput();
    }

    public OkHttpClient OkHttpClient() {
        return new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
    }

    public SimpleDateFormat SimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    public String GetString(String name) {
        String result = "";
        try {
            Context context = new InitialContext();
            Context environment = (Context) context.lookup("java:comp/env");
            result = (String) environment.lookup(name);
        } catch (NamingException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        }
        return result;
    }

    public String RandomAlphaNumeric(int length) {
        String char_lower = "abcdefghijklmnopqrstuvwxyz";
        String char_upper = char_lower.toUpperCase();
        String number = "0123456789";
        String data = char_lower + char_upper + number;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomAlpha(int length) {
        String char_lower = "abcdefghijklmnopqrstuvwxyz";
        String char_upper = char_lower.toUpperCase();
        String data = char_lower + char_upper;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomNumeric(int length) {
        //reset daily random number series 
        String number = "0123456789";
        String data = number;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public boolean IsValidNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidPhoneNumber(String phone_number) {
        boolean isValid = phone_number.matches("\\d{11}");

        return isValid;
    }

    public boolean isValidEmail(String email) {
        boolean isValid = email.matches(email_pattern);
        return isValid;
    }

    public boolean IsValidDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean isValidNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {

            return false;
        }
        return true;
    }

    public boolean IsValidTime(String string) {
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm");
        sdt.setLenient(false);
        try {
            sdt.parse(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean IsValidPIN(String string) {
        boolean result = false;
        int[] weights = {6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 1};
        int sum = 0;
        try {
            String PIN = string.replaceAll("[-,]", "");
            Double.parseDouble(PIN);
            if (PIN.length() == 12) {
                for (int i = 0; i < 12; i++) {
                    int intpin = Integer.parseInt(PIN.substring(i, i + 1));
                    sum = sum + (intpin * (weights[i]));
                }
                if ((sum % 11) == 0) {
                    result = true;
                }
            }
        } catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    public boolean IsValidDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean ValidDate(String stringdate, String pattern) {
        SimpleDateFormat sdf = this.SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(stringdate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean ValidDateRange(String startdate, String enddate, String pattern) {
        boolean result = true;
        try {
            Date datestart = this.SimpleDateFormat(pattern).parse(startdate);
            Date dateend = this.SimpleDateFormat(pattern).parse(enddate);
            if (datestart.compareTo(dateend) > 0) {
                result = false;
            }
        } catch (ParseException e) {
            result = false;
        }
        return result;
    }

    public Response Response() {
        Response result = new Response() {
            @Override
            public Object getEntity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getStatus() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Response.StatusType getStatusInfo() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public <T> T readEntity(Class<T> entityType) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public <T> T readEntity(GenericType<T> entityType) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public boolean hasEntity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public boolean bufferEntity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void close() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public MediaType getMediaType() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Locale getLanguage() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public int getLength() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Set<String> getAllowedMethods() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Map<String, NewCookie> getCookies() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public EntityTag getEntityTag() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Date getDate() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Date getLastModified() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public URI getLocation() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Set<Link> getLinks() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public boolean hasLink(String relation) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Link getLink(String relation) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public Link.Builder getLinkBuilder(String relation) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public MultivaluedMap<String, String> getStringHeaders() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public String getHeaderString(String name) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        return result;
    }

}
