/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC19 {

    public GetMDC19() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC19(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            int Counter19PBX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                if (new GrouperMethod().AX(datasource, "19PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    ORProcedureCounterList.add(Integer.valueOf(new GrouperMethod().AX(datasource, "19PBX", ProcedureList.get(x).trim()).getResult()));
                    Counter19PBX++;
                }
            }
            switch (drgResult.getPDC()) {
                case "19A"://Acute Psychotic Disorders
                    if (Counter19PBX > 0) {
                        drgResult.setDC("1901");
                    } else {
                        drgResult.setDC("1950");
                    }
                    break;
                case "19B"://Chronic Psychotic Disorders
                    if (Counter19PBX > 0) {

                        drgResult.setDC("1902");
                    } else {
                        drgResult.setDC("1951");
                    }
                    break;
                case "19C"://Major Affective Disorders
                    if (Counter19PBX > 0) {
                        drgResult.setDC("1903");
                    } else {
                        drgResult.setDC("1952");
                    }
                    break;
                case "19D"://Other Affect and Somatoform Disorders
                    drgResult.setDC("1953");
                    break;
                case "19E"://Acute Reaction and Psychosocial Dysfunction
                    drgResult.setDC("1954");
                    break;
                case "19F"://Anxiety Disorders
                    drgResult.setDC("1955");
                    break;
                case "19G"://Eating and Obsessive Compulsive Disorders
                    drgResult.setDC("1956");
                    break;
                case "19H"://Personality and Impulse Control Disorders
                    drgResult.setDC("1957");
                    break;
                case "19J"://Childhood Mental Disorders
                    drgResult.setDC("1958");
                    break;
                case "19K"://Organic Disturbance and Mental Retardation
                    drgResult.setDC("1959");
                    break;
                case "19L"://Other Mental Disorders
                    drgResult.setDC("1960");
                    break;
                case "19M"://Sexual Dysfunction PDC 19M
                    drgResult.setDC("1961");
                    break;
            }

            if (drgResult.getDRG() == null) {

                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        //-----------------------------------------------------------------------
                        if (new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRG(finaldrgresult.getDRG());
                            drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                        } else {
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(finaldrgresult.getDRG());
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG(drgResult.getDC() + "X");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                //----------------------------------------------------------------------
            } else {
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 19 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC19.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
