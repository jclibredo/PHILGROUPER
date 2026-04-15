/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.library.ServiceDashboard;
import grouper.methods.library.ServiceUserActivity;
import grouper.methods.library.ServicesI10VX;
import grouper.methods.library.ServicesAX;
import grouper.methods.library.ServicesCCEX;
import grouper.methods.library.ServicesDRG;
import grouper.methods.library.ServicesI10;
import grouper.methods.validation.GetValidICD10Accpdx;
import grouper.structures.AX;
import grouper.structures.CCEX;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.ICD10;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author LAPTOP
 */
@Path("Library")
@RequestScoped
public class LibraryManagement {

    public LibraryManagement() {
    }

    @Resource(lookup = "jdbc/grouperuser")
    private DataSource dataSource;

    private final Utility utility = new Utility();

    @POST
    @Path("ManageAX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateAX(
            @HeaderParam("token") String token,
            final List<AX> ax,
            @HeaderParam("action") String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            ServiceUserActivity serviceLogs = new ServiceUserActivity();
            ServicesAX axAction = new ServicesAX();
            String upperAction = (action == null) ? "" : action.toUpperCase();
            int succ = 0;
            int err = 0;
            String actions = "";
            String details = "";
            ArrayList<String> errorList = new ArrayList<>();
            switch (upperAction) {
                case "CREATE": {
                    DRGWSResult removeAx = axAction.DeleteAx(dataSource);
                    if (removeAx.isSuccess()) {
                        for (int x = 0; x < ax.size(); x++) {
                            DRGWSResult create = axAction.CreateAx(dataSource, ax.get(x).getAx(), ax.get(x).getCodes());
                            if (create.isSuccess()) {
                                succ++;
                            } else {
                                errorList.add(create.getMessage());
                                err++;
                            }
                        }
                        result.setSuccess(true);
                        result.setResult(errorList.toString());
                        result.setMessage("Total rows inserted: success[" + succ + "] error[" + err + "]");
                        actions = "CREATE";
                        details = "Total rows[" + ax.size() + "] inserted[" + succ + "]";
                    } else {
                        result = removeAx;
                        actions = "DELETE";
                        details = "Failed to remove all AX data";
                    }
                    break;
                }
                case "READ": {
                    result = axAction.GetAx(dataSource);
                    actions = "READ";
                    details = "Get all AX data";
                    break;
                }
                default: {
                    result.setMessage("Action not authorize");
                    actions = "FAIL";
                    details = "Action not authorize";
                    break;
                }
            }
            //ACTIVITY LOGS
            serviceLogs.CreateUserLogs(dataSource, authCheck.getMessage(), "AX", actions, details);
        }
        return result;
    }

    @POST
    @Path("ManageI10VX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateI10VX(@HeaderParam("token") String token, final List<ICD10> icd10vx, @HeaderParam("action") String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            ServiceUserActivity serviceLogs = new ServiceUserActivity();
            ServicesI10VX serviceIcd10vx = new ServicesI10VX();
            String upperAction = (action == null) ? "" : action.toUpperCase();
            int succ = 0;
            int err = 0;
            String actions = "";
            String details = "";
            ArrayList<String> errorList = new ArrayList<>();
            switch (upperAction) {
                case "CREATE": {
                    DRGWSResult removeIcd10VX = serviceIcd10vx.DeleteIcd10(dataSource);
                    if (removeIcd10VX.isSuccess()) {
                        for (int x = 0; x < icd10vx.size(); x++) {
                            DRGWSResult create = serviceIcd10vx.CreateIcd10(dataSource,
                                    icd10vx.get(x).getValidcode(),
                                    icd10vx.get(x).getDescription(),
                                    icd10vx.get(x).getCode());
                            if (create.isSuccess()) {
                                succ++;
                            } else {
                                errorList.add(create.getMessage());
                                err++;
                            }
                        }
                        result.setSuccess(true);
                        result.setResult(errorList.toString());
                        result.setMessage("Total rows inserted: success[" + succ + "] error[" + err + "]");
                        actions = "CREATE";
                        details = "Total rows[" + icd10vx.size() + "] inserted[" + succ + "]";
                    } else {
                        result = removeIcd10VX;
                        actions = "DELETE";
                        details = "Failed to remove all I10VX data";
                    }
                    break;
                }
                case "READ": {
                    result = serviceIcd10vx.GetIcd10(dataSource);
                    actions = "READ";
                    details = "Get all I10VX data";
                    break;
                }
                default: {
                    result.setMessage("Action not authorize");
                    actions = "FAIL";
                    details = "Action not authorize";
                    break;
                }
            }
            //ACTIVITY LOGS
            serviceLogs.CreateUserLogs(dataSource, authCheck.getMessage(), "I10VX", actions, details);
        }
        return result;
    }

    @POST
    @Path("ManageI10")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateI10(@HeaderParam("token") String token, final List<ICD10PreMDCResult> icd10, @HeaderParam("action") String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            ServiceUserActivity serviceLogs = new ServiceUserActivity();
            ServicesI10 serviceIcd10 = new ServicesI10();
            String upperAction = (action == null) ? "" : action.toUpperCase();
            int succ = 0;
            int err = 0;
            String actions = "";
            String details = "";
            ArrayList<String> errorList = new ArrayList<>();
            switch (upperAction) {
                case "CREATE": {
                    DRGWSResult removeIcd10 = serviceIcd10.DeleteIcd10PreMdc(dataSource);
                    if (removeIcd10.isSuccess()) {
                        for (int x = 0; x < icd10.size(); x++) {
                            DRGWSResult create = serviceIcd10.CreateIcd10PreMdc(dataSource,
                                    icd10.get(x).getCode(),
                                    icd10.get(x).getMDC(),
                                    icd10.get(x).getPDC(),
                                    icd10.get(x).getCC(),
                                    icd10.get(x).getMainCC(),
                                    icd10.get(x).getCCRow(),
                                    icd10.get(x).getHIV_AX(),
                                    icd10.get(x).getTrauma(),
                                    icd10.get(x).getSex(),
                                    icd10.get(x).getAccPDX(),
                                    icd10.get(x).getAgeDUse(),
                                    icd10.get(x).getAgeMin(),
                                    icd10.get(x).getAgeMax(),
                                    icd10.get(x).getAgeDMin());
                            if (create.isSuccess()) {
                                succ++;
                            } else {
                                errorList.add(create.getMessage());
                                err++;
                            }
                        }
                        result.setSuccess(true);
                        result.setResult(errorList.toString());
                        result.setMessage("Total rows inserted: success[" + succ + "] error[" + err + "]");
                        actions = "CREATE";
                        details = "Total rows[" + icd10.size() + "] inserted[" + succ + "]";
                    } else {
                        result = removeIcd10;
                        actions = "DELETE";
                        details = "Failed to remove all I10 data";
                    }
                    break;
                }
                case "READ": {
                    result = serviceIcd10.GetIcd10PreMDC(dataSource);
                    actions = "READ";
                    details = "Get all I10 data";
                    break;
                }
                case "CODE": {
                    result = serviceIcd10.GetIcd10PreMDC(dataSource);
                    actions = "READ";
                    details = "Get all I10 data";
                    break;
                }
                default: {
                    String input = upperAction;
                    if (input.contains(":")) {
                        String[] parts = input.split(":");
                        String prefix = parts[0];
                        String code = parts[1];
                        if (prefix.toUpperCase().trim().equals("CODE")) {
                            return new GetValidICD10Accpdx().GetValidICD10Accpdx(dataSource, code.trim());
                        } else {
                            result.setMessage("Action not authorize");
                            actions = "FAIL";
                            details = "Action not authorize";
                        }
                    } else {
                        result.setMessage("Seperator not found");
                        actions = "FAIL";
                        details = "Seperator not found";
                    }
                    break;
                }
            }
            //ACTIVITY LOGS
            serviceLogs.CreateUserLogs(dataSource, authCheck.getMessage(), "I10", actions, details);
        }
        return result;
    }

    @POST
    @Path("ManageCCEX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateCCEX(
            @HeaderParam("token") String token,
            final List<CCEX> ccex, @HeaderParam("action") String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            ServiceUserActivity serviceLogs = new ServiceUserActivity();
            ServicesCCEX serviceCCEX = new ServicesCCEX();
            String upperAction = (action == null) ? "" : action.toUpperCase();
            int succ = 0;
            int err = 0;
            String actions = "";
            String details = "";
            ArrayList<String> errorList = new ArrayList<>();
            switch (upperAction) {
                case "CREATE": {
                    DRGWSResult removeCCEX = serviceCCEX.DeleteCCEX(dataSource);
                    if (removeCCEX.isSuccess()) {
                        for (int x = 0; x < ccex.size(); x++) {
                            DRGWSResult create = serviceCCEX.CreateCCEX(dataSource, ccex.get(x).getSdx(), ccex.get(x).getPdx());
                            if (create.isSuccess()) {
                                succ++;
                            } else {
                                errorList.add(create.getMessage());
                                err++;
                            }
                        }
                        result.setSuccess(true);
                        result.setResult(errorList.toString());
                        result.setMessage("Total rows inserted: success[" + succ + "] error[" + err + "]");
                        actions = "CREATE";
                        details = "Total rows[" + ccex.size() + "] inserted[" + succ + "]";
                    } else {
                        result = removeCCEX;
                        actions = "DELETE";
                        details = "Failed to remove all CCEX data";
                    }
                    break;
                }
                case "READ": {
                    result = serviceCCEX.GetCCEX(dataSource);
                    actions = "READ";
                    details = "Get all CCEX data";
                    break;
                }
                default: {
                    result.setMessage("Action not authorize");
                    actions = "FAIL";
                    details = "Action not authorize";
                    break;
                }
            }
            //ACTIVITY LOGS
            serviceLogs.CreateUserLogs(dataSource, authCheck.getMessage(), "CCEX", actions, details);
        }
        return result;
    }

    @POST
    @Path("ManageDRG")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateDRG(@HeaderParam("token") String token, final List<DRGOutput> drgOutput, @HeaderParam("action") String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            ServiceUserActivity serviceLogs = new ServiceUserActivity();
            ServicesDRG serviceDRG = new ServicesDRG();
            String upperAction = (action == null) ? "" : action.toUpperCase();
            int succ = 0;
            int err = 0;
            String actions = "";
            String details = "";
            ArrayList<String> errorList = new ArrayList<>();
            switch (upperAction) {
                case "CREATE": {
                    DRGWSResult removeDRG = serviceDRG.DeleteDrg(dataSource);
                    if (removeDRG.isSuccess()) {
                        for (int x = 0; x < drgOutput.size(); x++) {
                            DRGWSResult create = serviceDRG.CreateDrg(dataSource,
                                    drgOutput.get(x).getRW(),
                                    drgOutput.get(x).getWTLOS(),
                                    drgOutput.get(x).getOT(),
                                    drgOutput.get(x).getMDF(),
                                    drgOutput.get(x).getDRGName(),
                                    drgOutput.get(x).getDRG(),
                                    drgOutput.get(x).getMDC(),
                                    drgOutput.get(x).getDC());
                            if (create.isSuccess()) {
                                succ++;
                            } else {
                                errorList.add(create.getMessage());
                                err++;
                            }
                        }
                        result.setSuccess(true);
                        result.setResult(errorList.toString());
                        result.setMessage("Total rows inserted: success[" + succ + "] error[" + err + "]");
                        actions = "CREATE";
                        details = "Total rows[" + drgOutput.size() + "] inserted[" + succ + "]";
                    } else {
                        result = removeDRG;
                        actions = "DELETE";
                        details = "Failed to remove all DRG data";
                    }
                    break;
                }
                case "READ": {
                    result = serviceDRG.GetDrg(dataSource);
                    actions = "READ";
                    details = "Get all DRG data";
                    break;
                }
                default: {
                    result.setMessage("Action not authorize");
                    actions = "FAIL";
                    details = "Action not authorize";
                    break;
                }
            }
            //ACTIVITY LOGS
            serviceLogs.CreateUserLogs(dataSource, authCheck.getMessage(), "DRG", actions, details);
        }
        return result;
    }

    @GET
    @Path("GetJsonFormat") //I10VX,I10,AX,CCEX
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GenerateJson(
            @HeaderParam("token") String token,
            @HeaderParam("type") String type) {
        DRGWSResult result = utility.DRGWSResult();
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        }
        try {
            switch (type.toUpperCase().trim()) {
                case "I10VX": {
                    result.setResult(utility.objectMapper().writeValueAsString(new ICD10()));
                    break;
                }
                case "I10": {
                    result.setResult(utility.objectMapper().writeValueAsString(new ICD10PreMDCResult()));
                    break;
                }
                case "AX": {
                    result.setResult(utility.objectMapper().writeValueAsString(new AX()));
                    break;
                }
                case "CCEX": {
                    result.setResult(utility.objectMapper().writeValueAsString(new CCEX()));
                    break;
                }
                case "DRG": {
                    result.setResult(utility.objectMapper().writeValueAsString(new DRGOutput()));
                    break;
                }
                default: {
                    result.setMessage("REQUEST TYPE NOT VALID");
                    break;
                }
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
        }
        return result;
    }

    @GET
    @Path("DASHBOARD")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetDashboard(
            @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        } else {
            result = new ServiceDashboard().GetDashboard(dataSource);
        }
        return result;
    }

    @GET
    @Path("GetUserActivity")
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult GetUserLogs(
            @HeaderParam("token") String token) {
        DRGWSResult result = utility.DRGWSResult();
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result = authCheck;
        } else {
            result = new ServiceUserActivity().GetUserLogs(dataSource);
        }
        return result;
    }

}
