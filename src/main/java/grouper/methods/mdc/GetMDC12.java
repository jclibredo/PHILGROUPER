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
import java.util.Collections;
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
public class GetMDC12 {

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC12(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            // CHECKING ICD9 TO MDC START
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            //Malignant Counter for Primay Code (PDx)
            int MalignantCount = 0;
            if (new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "12A").isSuccess()) {
                MalignantCount++;
            }
            // CHECKING ICD9 TO MDC START
            //int ICD9CMFindDC = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX12Proc = 0;
            int PBX99Proc = 0;

            for (int y = 0; y < ProcedureList.size(); y++) {
                if (utility.isValid99PDX(ProcedureList.get(y).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(y).trim())) {
                    PCXCounter99++;
                }
                if (utility.isValid99PEX(ProcedureList.get(y).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(y).trim())) {
                    CaCRxProc++;
                }
                if (new GrouperMethod().AX(datasource, "12PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    PBX12Proc++;
                }
                if (utility.isValid99PBX(ProcedureList.get(y).trim())) {
                    PBX99Proc++;
                }
                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }

            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
                    CaCRxSDx++;
                }
            }

            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1209");
                    } else {
                        drgResult.setDC("1210");
                    }

                } else if (mdcprocedureCounter > 0) { //MDC Procedure
                    int min = hierarvalue.get(0);
                    //Loop through the array  
                    for (int i = 0; i < hierarvalue.size(); i++) {
                        //Compare elements of array with min  
                        if (hierarvalue.get(i) < min) {
                            min = hierarvalue.get(i);
                        }
                    }

                    drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                    switch (pdclist.get(hierarvalue.indexOf(min))) {
                        case "12PA":   //1Major Male Pelvic Procedures
                            drgResult.setDC("1201");
                            break;
                        case "12PB"://Transurethral Prostatectomy
                            drgResult.setDC("1202");
                            break;
                        case "12PD"://Penis Procedures
                            drgResult.setDC("1204");
                            break;
                        case "12PF"://Other Male Reproductive System OR Procedures
                            if (MalignantCount > 0) {
                                drgResult.setDC("1206");
                            } else {
                                drgResult.setDC("1207");
                            }
                            break;
                        case "12PC":  //Testis Procedures
                            drgResult.setDC("1203");
                            break;
                        case "12PG": //1Cystourethroscopy
                            drgResult.setDC("1208");
                            break;
                        case "12PE"://Circumcision 12PE
                            drgResult.setDC("1205");
                            break;
                    }

                } else if (ORProcedureCounter > 0) {
                    switch (Collections.max(ORProcedureCounterList)) {
                        case 1:
                            drgResult.setDC("2601");
                            break;
                        case 2:
                            drgResult.setDC("2602");
                            break;
                        case 3:
                            drgResult.setDC("2603");
                            break;
                        case 4:
                            drgResult.setDC("2604");
                            break;
                        case 5:
                            drgResult.setDC("2605");
                            break;
                        case 6:
                            drgResult.setDC("2606");
                            break;
                    }

                } else {
                    switch (drgResult.getPDC()) {
                        case "12A":
                            //Radio+Chemotherapy
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1255");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("1256");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("1257");
                            } else if (PBX12Proc > 0) { //##Dx Procedure
                                drgResult.setDC("1258");
                                //Radiotherapy
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("1259");
                            } else {
                                drgResult.setDC("1250");
                            }
                            break;
                        case "12B": //#Benign prostatic hypertrophy
                            drgResult.setDC("1251");
                            break;
                        case "12C": //#Inflammation of male reproductive system
                            drgResult.setDC("1252");
                            break;
                        case "12D": //#Inflammation of male reproductive system
                            drgResult.setDC("1253");
                            break;
                        case "12E"://##Other male reproductive system diagnoses 12E
                            drgResult.setDC("1254");
                            break;
                    }

                }

            } else if (mdcprocedureCounter > 0) { //MDC Procedure
                int min = hierarvalue.get(0);
                //Loop through the array  
                for (int i = 0; i < hierarvalue.size(); i++) {
                    //Compare elements of array with min  
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                switch (pdclist.get(hierarvalue.indexOf(min))) {
                    case "12PA":   //1Major Male Pelvic Procedures
                        drgResult.setDC("1201");
                        break;
                    case "12PB"://Transurethral Prostatectomy
                        drgResult.setDC("1202");
                        break;
                    case "12PD"://Penis Procedures
                        drgResult.setDC("1204");
                        break;
                    case "12PF"://Other Male Reproductive System OR Procedures
                        if (MalignantCount > 0) {
                            drgResult.setDC("1206");
                        } else {
                            drgResult.setDC("1207");
                        }
                        break;
                    case "12PC":  //Testis Procedures
                        drgResult.setDC("1203");
                        break;
                    case "12PG": //1Cystourethroscopy
                        drgResult.setDC("1208");
                        break;
                    case "12PE"://Circumcision 12PE
                        drgResult.setDC("1205");
                        break;
                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 1:
                        drgResult.setDC("2601");
                        break;
                    case 2:
                        drgResult.setDC("2602");
                        break;
                    case 3:
                        drgResult.setDC("2603");
                        break;
                    case 4:
                        drgResult.setDC("2604");
                        break;
                    case 5:
                        drgResult.setDC("2605");
                        break;
                    case 6:
                        drgResult.setDC("2606");
                        break;
                }

            } else {
                switch (drgResult.getPDC()) {
                    case "12A":
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1255");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1256");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("1257");
                        } else if (PBX12Proc > 0) { //##Dx Procedure
                            drgResult.setDC("1258");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("1259");
                        } else {
                            drgResult.setDC("1250");
                        }
                        break;
                    case "12B": //#Benign prostatic hypertrophy
                        drgResult.setDC("1251");
                        break;
                    case "12C": //#Inflammation of male reproductive system
                        drgResult.setDC("1252");
                        break;
                    case "12D": //#Inflammation of male reproductive system
                        drgResult.setDC("1253");
                        break;
                    case "12E"://##Other male reproductive system diagnoses 12E
                        drgResult.setDC("1254");
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
            result.setMessage("MDC 12 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC12.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
