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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GetMDC17 {

    public GetMDC17() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC17(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
//            int PBXCounter99 = 0;
            int Counter17PBX = 0;
            int ORProcedureCounter = 0;
//            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter17PA = 0;
//            String PBX17 = "17PBX";
//            String PA17 = "17PA";
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(x).trim(), "17PA", drgResult.getMDC()).isSuccess()) {
                    Counter17PA++;
                }
                if (utility.isValid99PEX(ProcedureList.get(x).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(x).trim())) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(ProcedureList.get(x).trim())) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
                if (new GrouperMethod().AX(datasource, "17PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter17PBX++;
                }
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PBX Checking
//                if (utility.isValid99PBX(proc)) {
//                    PBXCounter99++;
//                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                if (new GrouperMethod().ORProcedure(datasource, ProcedureList.get(x).trim()).isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(new GrouperMethod().ORProcedure(datasource, ProcedureList.get(x).trim()).getResult()));
                }
                if (new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC()).isSuccess()) {
//                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC()).getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
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
            result.setMessage("MDC 17 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC17.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
