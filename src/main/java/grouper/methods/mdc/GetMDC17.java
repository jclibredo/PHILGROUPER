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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC17 {

    public GetMDC17() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC17(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        
        
        
        
        try {

            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int PBXCounter99 = 0;
            int Counter17PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter17PA = 0;
            String PBX17 = "17PBX";
            String PA17 = "17PA";
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();

            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                DRGWSResult PA17Result = gm.Endovasc(datasource, proc.trim(), "17PA", drgResult.getMDC());
                if (PA17Result.isSuccess()) {
                    Counter17PA++;
                }
                if (utility.isValid99PEX(proc.trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(proc.trim())) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(proc.trim())) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
                DRGWSResult PBX17Result = gm.AX(datasource, PBX17, proc);
                if (String.valueOf(PBX17Result.isSuccess()).equals("true")) {
                    Counter17PBX++;
                }
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PBX Checking
                if (utility.isValid99PBX(proc)) {
                    PBXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc, drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = gm.GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                if (utility.isValid99BX(Secon)) {
                    CartSDx++;
                }
                if (utility.isValid99CX(Secon)) {
                    CaCRxSDx++;
                }
            }

            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    switch (drgResult.getPDC()) {
                        case "17A"://Acute Leukemia
                            if (ORProcedureCounter > 0) {
                                if (Counter17PA > 0) {
                                    drgResult.setDC("1701");
                                } else {
                                    drgResult.setDC("1703");
                                }
                            } else {
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1756");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1757");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1758");
                                } else if (Counter17PBX > 0) { //##Dx Procedure
                                    drgResult.setDC("1759");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1760");
                                } else {//Malignancy 
                                    drgResult.setDC("1750");
                                }
                            }
                            break;
                        case "17B"://Lymphoma & Non-acute Leukemia
                            if (ORProcedureCounter > 0) {
                                if (Counter17PA > 0) {
                                    drgResult.setDC("1701");
                                } else {
                                    drgResult.setDC("1703");
                                }
                            } else {
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1761");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1762");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1763");
                                } else if (Counter17PBX > 0) { //##Dx Procedure
                                    drgResult.setDC("1764");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1765");
                                } else {//Malignancy 
                                    drgResult.setDC("1751");
                                }
                            }
                            break;
                        case "17C"://Other Neoplastic Disorders PDC 17C
                            if (ORProcedureCounter > 0) {
                                if (Counter17PA > 0) {
                                    drgResult.setDC("1702");
                                } else {
                                    drgResult.setDC("1704");
                                }
                            } else {
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1766");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1767");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1768");
                                } else if (Counter17PBX > 0) { //##Dx Procedure
                                    drgResult.setDC("1769");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1770");
                                } else {//Malignancy 
                                    drgResult.setDC("1752");
                                }
                            }
                            break;
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1705");
                    } else {
                        drgResult.setDC("1706");
                    }
                }
            } else {
                switch (drgResult.getPDC()) {
                    case "17A"://Acute Leukemia
                        if (ORProcedureCounter > 0) {
                            if (Counter17PA > 0) {
                                drgResult.setDC("1701");
                            } else {
                                drgResult.setDC("1703");
                            }
                        } else {
                            //Radio+Chemotherapy
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1756");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1757");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("1758");
                            } else if (Counter17PBX > 0) { //##Dx Procedure
                                drgResult.setDC("1759");
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("1760");
                            } else {//Malignancy 
                                drgResult.setDC("1750");
                            }
                        }
                        break;
                    case "17B"://Lymphoma & Non-acute Leukemia
                        if (ORProcedureCounter > 0) {
                            if (Counter17PA > 0) {
                                drgResult.setDC("1701");
                            } else {
                                drgResult.setDC("1703");
                            }
                        } else {
                            //Radio+Chemotherapy
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1761");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1762");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("1763");
                            } else if (Counter17PBX > 0) { //##Dx Procedure
                                drgResult.setDC("1764");
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("1765");
                            } else {//Malignancy 
                                drgResult.setDC("1751");
                            }
                        }
                        break;
                    case "17C"://Other Neoplastic Disorders PDC 17C
                        if (ORProcedureCounter > 0) {
                            if (Counter17PA > 0) {
                                drgResult.setDC("1702");
                            } else {
                                drgResult.setDC("1704");
                            }
                        } else {
                            //Radio+Chemotherapy
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1766");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1767");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("1768");
                            } else if (Counter17PBX > 0) { //##Dx Procedure
                                drgResult.setDC("1769");
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("1770");
                            } else {//Malignancy 
                                drgResult.setDC("1752");
                            }
                        }
                        break;
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
            result.setMessage("MDC 17 Done Checking");
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC17.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
