/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
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
public class GetMDC24 {

    public GetMDC24() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC24(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            int A = 0;
            int B = 0;
            int C = 0;
            int D = 0;
            int E = 0;
            int F = 0;
            int G = 0;
            int H = 0;
            int J = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter24PBX = 0;
            String PBX24 = "24PBX";
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
                if (utility.isValid24PBX(proc)) {
                    Counter24PBX++;
                    ORProcedureCounter++;
                }

                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc, drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = gm.GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc);

                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                    switch (ORProcedureResult.getMessage()) {
                        case "A":
                            A++;
                            break;
                        case "B":
                            B++;
                            break;
                        case "C":
                            C++;
                            break;
                        case "D":
                            D++;
                            break;
                        case "E":
                            E++;
                            break;
                        case "F":
                            F++;
                            break;
                        case "G":
                            G++;
                            break;
                        case "H":
                            H++;
                            break;

                        case "J":
                            J++;
                            break;
                    }
                }

            }
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (ORProcedureCounter > 0) {
                        if (A > 0 && D > 0) {//Intracranial w Others Proc site A+D/E/G/H
                            drgResult.setDC("2401");
                        } else if (A > 0 && H > 0) {//Intracranial w Others Proc site A+D/E/G/H
                            drgResult.setDC("2401");
                        } else if (A > 0 && G > 0) {//Intracranial w Others Proc site A+D/E/G/H
                            drgResult.setDC("2401");
                        } else if (A > 0 && E > 0) {//Intracranial w Others Proc site A+D/E/G/H
                            drgResult.setDC("2401");
                        } else if (E > 0 && D > 0) {//Spinal w Others
                            drgResult.setDC("2405");
                        } else if (E > 0 && H > 0) {//Spinal w Others
                            drgResult.setDC("2405");
                        } else if (E > 0 && G > 0) {//Spinal w Others
                            drgResult.setDC("2405");
                        } else if (Counter24PBX > 0) {//Multiple Wound Debridement
                            drgResult.setDC("2417");
                        } else if (D > 0 && G > 0) { //Abdominal w Lower Ext.
                            drgResult.setDC("2410");
                        } else if (E > 0) {//Spinal
                            drgResult.setDC("2412");
                        } else if (A > 0) {//Intracranial
                            drgResult.setDC("2411");
                        } else if (H > 0 && D > 0) {//Wound Debridement w Abdominal or Lower Ext.
                            drgResult.setDC("2408");
                        } else if (H > 0 && G > 0) {//Wound Debridement w Abdominal or Lower Ext.
                            drgResult.setDC("2408");
                        } else if (B > 0 || C > 0 || D > 0 || F > 0 || G > 0 || H > 0 || J > 0) {//Wound Debridement w Abdominal or Lower Ext.
                            drgResult.setDC("2413");
                        } else {//Other OR Procedure
                            drgResult.setDC("2414");
                        }
                    } else {
                        drgResult.setDC("2450");
                    }
                } else {
                    if (PCXCounter99 > 0) {//Cont. Mech Vent 96+ hr
                        drgResult.setDC("2415");
                    } else {
                        drgResult.setDC("2416");
                    }
                }
            } else {
                if (ORProcedureCounter > 0) {
                    if (A > 0 && D > 0) {//Intracranial w Others Proc site A+D/E/G/H
                        drgResult.setDC("2401");
                    } else if (A > 0 && H > 0) {//Intracranial w Others Proc site A+D/E/G/H
                        drgResult.setDC("2401");
                    } else if (A > 0 && G > 0) {//Intracranial w Others Proc site A+D/E/G/H
                        drgResult.setDC("2401");
                    } else if (A > 0 && E > 0) {//Intracranial w Others Proc site A+D/E/G/H
                        drgResult.setDC("2401");
                    } else if (E > 0 && D > 0) {//Spinal w Others
                        drgResult.setDC("2405");
                    } else if (E > 0 && H > 0) {//Spinal w Others
                        drgResult.setDC("2405");
                    } else if (E > 0 && G > 0) {//Spinal w Others
                        drgResult.setDC("2405");
                    } else if (Counter24PBX > 0) {//Multiple Wound Debridement
                        drgResult.setDC("2417");
                    } else if (D > 0 && G > 0) { //Abdominal w Lower Ext.
                        drgResult.setDC("2410");
                    } else if (E > 0) {//Spinal
                        drgResult.setDC("2412");
                    } else if (A > 0) {//Intracranial
                        drgResult.setDC("2411");
                    } else if (H > 0 && D > 0) {//Wound Debridement w Abdominal or Lower Ext.
                        drgResult.setDC("2408");
                    } else if (H > 0 && G > 0) {//Wound Debridement w Abdominal or Lower Ext.
                        drgResult.setDC("2408");
                    } else if (B > 0 || C > 0 || D > 0 || F > 0 || G > 0 || H > 0 || J > 0) {//Wound Debridement w Abdominal or Lower Ext.
                        drgResult.setDC("2413");
                    } else {//Other OR Procedure
                        drgResult.setDC("2414");
                    }
                } else {
                    drgResult.setDC("2450");
                }
            }

            if (drgResult.getDRG() == null) {
                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList = gm.CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = gm.CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = gm.GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        String drgValue = finaldrgresult.getDRG();
                        DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgValue);
                        //-----------------------------------------------------------------------
                        if (drgname.isSuccess()) {
                            drgResult.setDRG(drgValue);
                            drgResult.setDRGName(drgname.getMessage());
                        } else {
                            DRGWSResult drgvalues = gm.ValidatePCCL(datasource, drgResult.getDC(), drgValue);
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(drgValue);
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG("00000");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                result.setSuccess(true);
                //----------------------------------------------------------------------
            } else {
                result.setSuccess(true);
                DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 24 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC24.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
