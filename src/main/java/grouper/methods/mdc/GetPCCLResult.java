/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.CleanSDxDCDeterminationPLSQL;
import grouper.methods.validation.DRG;
import grouper.methods.validation.GetPCCL;
import grouper.methods.validation.ValidatePCCL;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetPCCLResult {

    public GetPCCLResult() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetPCCLResult(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRG checkDRG = new DRG();
            if (drgResult.getDRG() == null) {
                drgResult.setPrepccl("X");
                drgResult.setFinalpccl("X");
                drgResult.setDRGName("Grouper Error");
                drgResult.setDRG(drgResult.getDC() + "X");
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                    drgResult.setPrepccl("9");
                    drgResult.setFinalpccl("9");
                } else {
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GetPCCL().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        drgResult.setPrepccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                        drgResult.setFinalpccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                        drgResult.setDRG(finaldrgresult.getDRG());
                        if (checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRGName(checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                        } else {
                            DRGWSResult drgvalues = new ValidatePCCL().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                String drgcode = drgResult.getDC() + drgvalues.getResult();
                                drgResult.setDRG(drgcode);
                                DRGWSResult drgnames = checkDRG.DRG(datasource, drgResult.getDC(), drgcode);
                                if (drgnames.isSuccess()) {
                                    drgResult.setDRGName(drgnames.getMessage());
                                }
                                drgResult.setFinalpccl(drgcode.substring(drgcode.length() - 1));
                            } else {
                                drgResult.setDRGName("DRG code grouper provide not exist in the library");
                            }
                        }
                    }
                }
            } else {
                if (checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException | NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetPCCLResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
