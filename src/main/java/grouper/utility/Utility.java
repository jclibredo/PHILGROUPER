/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.utility;

import grouper.structures.DRGOutput;
import grouper.structures.DRGPayload;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import okhttp3.OkHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author MINOSUN
 */
@ApplicationScoped
@Singleton
public class Utility {

    public Utility() {
    }

    private static SecretKeySpec secretkey;
    private byte[] key;
    String regex = "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[@#$%^&+=])"
            + "(?=\\S+$).{8,20}$";
    private static final String CIPHERKEY = "A263B7980A15ADE7";

    String[] pkgcode = {"", "", "", ""};
    String[] OPA = {"5051", "5059"};
    String[] OPB = {"3350", "3351", "3352", "336", "3751"};
    String[] OPD = {"4100", "4101", "4102", "4103", "4104", "4105", "4106", "4107", "4108", "4109"};

    //AX 99BX Radiotherapy
    String[] BX99 = {"Z510"};

    //AX 99CX Chemotherapy
    String[] CX99 = {"Z511"};

    //AX 99PBX Blood transfusion
    String[] PBX99 = {"9903", "9905", "9904"};

    //AX 99PCX Cont mech ventilation 96 consecutive hours or more
    String[] PCX99 = {"9672"};

    //AX 2PDX Cataract Frag/Asp
    //AX 99PDX Tracheostomy
    String[] PDX99 = {"311", "3121", "3129"};

    //AX 99PEX Radiotherapeutic procedure
    String[] PEX99 = {"9223", "9224", "9225", "9227", "9228", "9229", "9230", "9231", "9232", "9241"};

    //AX 99PFX Parenteral cancer chemotherapy
    String[] PFX99 = {"9925", "1770"};
    //AX 2BX Parenteral cancer chemotherapy

    //Procedures for upper airway obstruction
    String[] CX6 = {"C181", "K352", "K383"};
    String[] PEX8 = {"8156"};
    String[] PBX9 = {"86221>1"};
    String[] PBX10 = {"3995"};
    String[] PBX11 = {"9851"};
    String[] PBX16 = {"0096"};
    String[] PBX28 = {"9671"};
    String[] DX28 = {"K920", "K922"};
    String[] PBX24 = {"8628>1"};
    String[] dclist = {"0019", "0029", "0049", "0107", "0110", "0203", "0210", "0213", "0214", "0215", "0311", "0318", "0319", "0450", "0503", "0507", "0510", "0525", "0528", "0535",
        "0557", "0615", "0620", "0622", "0625", "0626", "0627", "0629", "0634", "0712", "0713", "0804", "0826", "0827", "0832", "0833", "0834", "0835", "0907", "0912",
        "0913", "1005", "1009", "1011", "1012", "1113", "1114", "1115", "1152", "1161", "1209", "1210", "1253", "1255", "1312", "1313", "1314", "1318", "1319", "1361",
        "1363", "1512", "1604", "1605", "1705", "1706", "1758", "1766", "1808", "1809", "1863", "1901", "1903", "1950", "1951", "1956", "1957", "1961", "2054", "2105",
        "2106", "2203", "2204", "2303", "2305", "2308", "2311", "2312", "2355", "2414", "2416", "2501", "2502", "2506", "2508", "2509", "2650", "2651", "2652", "2653",
        "2654", "2801", "2802", "2803", "2804", "2805", "2806", "2807", "2808", "2809", "2810", "2811", "2812", "2813", "2814", "2815", "2816", "2817", "2818", "2819",
        "2820", "2821", "2822", "2823", "2824", "2825", "2826", "2827", "2828", "2829", "2830", "2831", "2832", "2833", "2834", "2835", "2836", "2837", "2850", "2851",
        "2852", "2853", "2854", "2855", "2856", "2857", "2858", "2859", "2860", "2861", "2862", "2863", "2864", "2865", "2866", "2867", "2868", "2869"};

    public boolean MaxAge(String DOB, String AD) {
        boolean result = false;
        try {
            SimpleDateFormat sdf = this.SimpleDateFormat("MM-dd-yyyy");
            java.util.Date DateOfBirth = sdf.parse(DOB);
            java.util.Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = Math.abs(AdmissioDate.getTime() - DateOfBirth.getTime());
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = AgeY > 124;
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public Date GetCurrentDate() {
        return new java.util.Date();
    }

    public DRGPayload DRGPayload() {
        return new DRGPayload();
    }

    public String Create2FACode() {
        int randnums = 0;
        for (int i = 0; i < 1; i++) {
            randnums += (int) ((Math.random() * 88881) + 22220);
        }
        return String.valueOf(randnums);
    }

    //GENERATE TOKEN METHODS
    public String GenerateToken(String username, String password, String expiration) {
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
        byte[] userkeybytes = DatatypeConverter.parseBase64Binary(CIPHERKEY);
        Key signingkey = new SecretKeySpec(userkeybytes, algorithm.getJcaName());
        JwtBuilder builder = Jwts.builder()
                .claim("Code1", EncryptString(username))
                .claim("Code2", EncryptString(password))
                .setExpiration(new Date(System.currentTimeMillis() + 30 * Integer.parseInt(expiration)))//ADD EXPIRE TIME 8HOURS
                .signWith(algorithm, signingkey);
        return builder.compact();

    }

    public String EncryptString(String string) {
        String result = null;
        try {
            SetKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretkey);
            result = Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes("UTF-8"))).replaceAll("=", "");
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private void SetKey() {
        MessageDigest sha = null;
        try {
            String userkey = CIPHERKEY;
            key = userkey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretkey = new SecretKeySpec(key, "AES");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean ValidateToken(final String token) {
        boolean result = false;
        try {
            Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(CIPHERKEY))
                    .parseClaimsJws(token).getBody();
            result = true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException ex) {
        }
        return result;
    }

    public String DecryptString(String string) {
        String result = null;
        try {
            SetKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretkey);
            result = new String(cipher.doFinal(Base64.getDecoder().decode(string)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            result = ex.toString();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String CleanCode(String data) {
        return data.trim().replaceAll("\\.", "").toUpperCase();
    }

    public DRGWSResult GetPayload(
            final DataSource dataSource,
            final String token) {
        DRGWSResult result = this.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (token.equals("")) {
                result.setMessage("Token is required");
            } else {
                if (this.ValidateToken(token)) {
                    Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(CIPHERKEY)).parseClaimsJws(token).getBody();
                    if (!this.isJWTExpired(claims)) {
                        DRGPayload payload = this.DRGPayload();
                        payload.setCode1(this.DecryptString((String) claims.get("Code1")));
                        payload.setCode2(this.DecryptString((String) claims.get("Code2")));
                        payload.setExp(claims.getExpiration());
//                        if (new SeekerMethods().ValidatePayloadValue(dataSource, this.DecryptString((String) claims.get("Code1")), this.DecryptString((String) claims.get("Code2")), "10000").isSuccess()) {
                        result.setSuccess(true);
                        result.setResult(this.objectMapper().writeValueAsString(payload));
//                        } else {
//                            result.setMessage("Unrecognized User");
//                        }
                    } else {
                        result.setMessage("Token is expired");
                    }
                } else {
                    result.setMessage("Could not verify JWT token integrity!");
                }
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException | IOException ex) {
            result.setMessage(ex.getLocalizedMessage());
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);

        }
        return result;
    }

    public boolean isJWTExpired(Claims claims) {
        if (claims.getExpiration() == null) {
            return true;
        } else {
            Date expiresAt = claims.getExpiration();
            return expiresAt.before(new Date());
        }
    }

    public int ComputeTime(String DateIn, String TimeIn, String DateOut, String TimeOut) {
        int result = 0;
        try {
            String IN = DateIn + TimeIn;
            String OUT = DateOut + TimeOut;
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            Date AdmissionTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long Time_difference = DischargeTime.getTime() - AdmissionTime.getTime();
            long Hours_difference = (Time_difference / (1000 * 60 * 60)) % 24;
            result = (int) Hours_difference;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int MinutesCompute(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
            String IN = datein + timein;
            String OUT = dateout + timeout;
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            Date AdmissionDateTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDateTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDateTime.getTime() - AdmissionDateTime.getTime();
            long Minutes = (difference_In_Time / (1000 * 60)) % 60;

            result = (int) Minutes;
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeYear(String DOB, String AD) {
        int result = 0;
        try {
            SimpleDateFormat sdf = this.SimpleDateFormat("MM-dd-yyyy");
            Date DateOfBirth = sdf.parse(DOB);
            Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = (int) AgeY;
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    public String Convert12to24(String times) {
        String result = "";
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        try {
            Date dates = parseFormat.parse(times);
            result = displayFormat.format(dates);

        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String Convert24to12(String times) {
        String result = "";
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma");
        try {
            Date time24 = displayFormat.parse(times);
            result = parseFormat.format(time24);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeDay(String DOB, String AD) {
        SimpleDateFormat sdf = this.SimpleDateFormat("MM-dd-yyyy");
        int result = 0;
        try {
            Date DateOfBirth = sdf.parse(DOB);
            Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) difference_In_Days;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeLOS(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            String IN = datein + timein;
            String OUT = dateout + timeout;
            Date AdmissioDate = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDate = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDate.getTime() - AdmissioDate.getTime();
            long CalLOS = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) CalLOS;

        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String CodeConverter(DataSource datasouce, String rvs) {
        GrouperMethod gm = new GrouperMethod();
        String result = "";
        List<String> ProcList = Arrays.asList(rvs.split(","));
        for (int m = 0; m < ProcList.size(); m++) {
            String rvs_code = ProcList.get(m);
            DRGWSResult finalResult = gm.GetICD9cms(datasouce, rvs_code);
            if (String.valueOf(finalResult.isSuccess()).equals(true)) {
                result = finalResult.getResult();
            }
        }
        return result;
    }

    public String SDxSecondary(DataSource datasouce, String icd10) {
        GrouperMethod gm = new GrouperMethod();
        String result = "";
        List<String> ProcList = Arrays.asList(icd10.split(","));
        for (int m = 0; m < ProcList.size(); m++) {
            String rvs_code = ProcList.get(m);
            DRGWSResult finalResult = gm.GetICD9cms(datasouce, rvs_code);
            if (String.valueOf(finalResult.isSuccess()).equals(true)) {
                result = finalResult.getResult();
            }
        }
        return result;
    }

    public boolean isValidDCList(String dcs) {
        boolean result = false;
        result = Arrays.asList(dclist).contains(dcs);
        return result;
    }

    public boolean isValid24PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX24).contains(icd10);
        return result;
    }

    public boolean isValid28DX(String icd10) {
        boolean result = false;
        result = Arrays.asList(DX28).contains(icd10);
        return result;
    }

    public boolean isValid28PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX28).contains(icd10);
        return result;
    }

    public boolean isValid16PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX16).contains(icd10);
        return result;
    }

    public boolean isValid11PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX11).contains(icd10);
        return result;
    }

    public boolean isValid10PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX10).contains(icd10);
        return result;
    }

    public boolean isValid9PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX9).contains(icd10);
        return result;
    }

    public boolean isValid8PEX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PEX8).contains(icd10);
        return result;
    }

    public boolean isValid6CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX6).contains(icd10);
        return result;
    }

    public boolean isValidOPA(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPA).contains(icd10);
        return result;

    }

    public boolean isValidOPB(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPB).contains(icd10);
        return result;
    }

    public boolean isValidOPD(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPD).contains(icd10);
        return result;

    }

    public boolean isValid99BX(String icd10) {
        boolean result = false;
        result = Arrays.asList(BX99).contains(icd10);
        return result;

    }

    public boolean isValid99CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX99).contains(icd10);
        return result;

    }

    public boolean isValid99PBX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PBX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PCX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PCX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PDX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PDX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PEX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PEX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PFX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PFX99).contains(icd9cm);
        return result;
    }

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
            Logger.getLogger(Utility.class
                    .getName()).log(Level.SEVERE, null, ex);
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

//    public boolean isValidEmail(String email) {
//        boolean isValid = email.matches(email_pattern);
//        return isValid;
//    }
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

    public Date StringToDate(String stringdate) {
        java.util.Date sf = null;
        try {
            sf = this.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a").parse(stringdate);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sf;
    }

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String GenerateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        // At least one uppercase letter
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        // At least one lowercase letter
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        // At least one digit
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        // At least one special character
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));
        // Remaining characters randomly selected from all characters
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }
        return password.toString();
    }

    public boolean isParsableDate(String dateString, String dateFormat) {
        boolean result = false;
        DateFormat sdf = this.SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateString);
            result = true;
        } catch (ParseException e) {
        }
        return result;
    }

}
