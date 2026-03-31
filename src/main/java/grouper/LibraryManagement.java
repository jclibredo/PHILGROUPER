/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.library.ServicesICD10;
import grouper.methods.library.ServicesAX;
import grouper.structures.AX;
import grouper.structures.DRGWSResult;
import grouper.structures.ICD10;
import grouper.utility.Utility;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

    @PUT
    @Path("UpdateAX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateAX(@HeaderParam("token") String token, final List<AX> ax, final String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        }
        ServicesAX axAction = new ServicesAX();
        String upperAction = (action == null) ? "" : action.toUpperCase();
        switch (upperAction) {
            case "CREATE": {
                DRGWSResult removeAx = axAction.DeleteAx(dataSource);
                if (removeAx.isSuccess()) {
                    long count = ax.stream()
                            .map(item -> axAction.CreateAx(
                            dataSource,
                            item.getAx(),
                            item.getDescr(),
                            item.getType(),
                            item.getCodes()))
                            .filter(res -> res != null && res.isSuccess())
                            .count();
                    result.setSuccess(true);
                    result.setMessage("Total rows inserted: " + count);
                } else {
                    result = removeAx;
                }
                break;
            }
            case "READ": {
                result = axAction.GetAx(dataSource);
                break;
            }
            default: {
                result.setMessage("Action not authorize");
                break;
            }
        }
        return result;
    }

    @POST
    @Path("UpdateICD10")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UpdateICD10(@HeaderParam("token") String token, final List<ICD10> icd10, final String action) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);

        DRGWSResult authCheck = utility.GetPayload(dataSource, token);
        if (!authCheck.isSuccess()) {
            result.setMessage(authCheck.getMessage());
            return result;
        }
        ServicesICD10 serviceIcd10 = new ServicesICD10();
        String upperAction = (action == null) ? "" : action.toUpperCase();
        switch (upperAction) {
            case "CREATE": {
                DRGWSResult removeIcd10 = serviceIcd10.DeleteIcd10(dataSource);
                if (removeIcd10.isSuccess()) {
                    long count = icd10.stream()
                            .map(item -> serviceIcd10.CreateIcd10(
                            dataSource,
                            item.getValidcode(),
                            item.getDescription(),
                            item.getCode()))
                            .filter(res -> res != null && res.isSuccess())
                            .count();
                    result.setSuccess(true);
                    result.setMessage("Total rows inserted: " + count);
                } else {
                    result = removeIcd10;
                }
                break;
            }
            case "READ": {
                result = serviceIcd10.GetIcd10(dataSource);
                break;
            }
            default: {
                result.setMessage("Action not authorize");
                break;
            }
        }
        return result;
    }

}
