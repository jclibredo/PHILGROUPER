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
import java.text.ParseException;
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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC09 {

    public GetMDC09() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC09(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            ArrayList<String> sdxfinder = new ArrayList<>();

            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter9PCX = 0;
            int Counter9PBX = 0;
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
                if (utility.isValid99PEX(proc)) {
                    CartProc++;
                }
                if (utility.isValid99PFX(proc)) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(proc)) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
                if (utility.isValid9PCX(proc)) { //Blood Transfusion AX 99PBX
                    Counter9PCX++;
                }
                if (utility.isValid9PBX(proc)) { //Blood Transfusion AX 99PBX
                    Counter9PBX++;
                }
            }

            //Seach Malignant using ICD10 PDx and SDx
            String MPDCCodes = "9E";
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int SDxMalignantCount = 0;
            int PDxMalignantCount = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String MalignantCodes = SecondaryList.get(a);
                DRGWSResult MaligSDxResult = gm.PDxMalignancy(datasource, MalignantCodes, MPDCCodes);
                if (String.valueOf(MaligSDxResult.isSuccess()).equals("true")) {
                    SDxMalignantCount++;
                }
                if (utility.isValid99BX(MalignantCodes)) {
                    CartSDx++;
                }
                if (utility.isValid99CX(MalignantCodes)) {
                    CaCRxSDx++;
                }
            }

            DRGWSResult MaligPDxResult = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), MPDCCodes);
            if (String.valueOf(MaligPDxResult.isSuccess()).equals("true")) {
                PDxMalignantCount++;
            }
            //PDX Skin Ulcer or Cellulitis  AX 9BX
            String BX9 = "9BX";
            int Counter9BX = 0;
            DRGWSResult Result9BX = gm.AX(datasource, BX9, grouperparameter.getPdx());
            if (String.valueOf(Result9BX.isSuccess()).equals("true")) {
                Counter9BX++;
            }
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            String PDX9 = "9PDX";
            int Counter9PDX = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //  String models = String.join(",", secondaryList);
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procs = ProcedureList.get(y);
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, procs);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult Result9PDX = gm.AX(datasource, PDX9, procs);
                if (String.valueOf(Result9PDX.isSuccess()).equals("true")) {
                    Counter9PDX++;
                }
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, procs, drgResult.getMDC());
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
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE

            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (mdcprocedureCounter > 0) {
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
                            case "9PJ"://Pedicle Graft Plastic Procedures
                                drgResult.setDC("0911");
                                break;
                            case "9PD"://Skin Graft and Debridement
                                if (Counter9BX > 0) {
                                    if (Counter9PBX > 0) {
                                        drgResult.setDC("0910");
                                    } else {
                                        drgResult.setDC("0905");
                                    }
                                } else {
                                    if (Counter9PBX > 0) {
                                        drgResult.setDC("0915");
                                    } else {
                                        drgResult.setDC("0906");
                                    }
                                }
                                break;
                            case "9PA"://Total Mastectomy
                                if (Counter9PDX > 0) {
                                    drgResult.setDC("0914");
                                } else {
                                    if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                                        for (int a = 0; a < SecondaryList.size(); a++) {
                                            String MalignantCodes = SecondaryList.get(a);
                                            DRGWSResult MaligSDxResult = gm.PDxMalignancy(datasource, MalignantCodes, MPDCCodes);
                                            if (String.valueOf(MaligSDxResult.isSuccess()).equals("true")) {
                                                sdxfinder.add(SecondaryList.get(a));
                                            }
                                        }
                                        if (!sdxfinder.isEmpty()) {
                                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                        }
                                        drgResult.setDC("0901");
                                    } else {
                                        drgResult.setDC("0903");
                                    }
                                }
                                break;
                            case "9PF"://Plastic
                                drgResult.setDC("0908");
                                break;
                            case "9PC"://Subtotal Mastextomy, Biopsy and Local Excision of Breast
                            case "9PB":
                                if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                                    for (int a = 0; a < SecondaryList.size(); a++) {
                                        String MalignantCodes = SecondaryList.get(a);
                                        DRGWSResult MaligSDxResult = gm.PDxMalignancy(datasource, MalignantCodes, MPDCCodes);
                                        if (String.valueOf(MaligSDxResult.isSuccess()).equals("true")) {
                                            sdxfinder.add(SecondaryList.get(a));
                                        }
                                    }
                                    if (!sdxfinder.isEmpty()) {
                                        drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                    }
                                    drgResult.setDC("0902");
                                } else {
                                    if (drgResult.getPDC().equals("9PB")) {
                                        drgResult.setDC("0903");
                                    } else {
                                        drgResult.setDC("0904");
                                    }
                                }
                                break;
                            case "9PG"://Other Skin, Subcutaneous Tissue and Breast Procedures
                            case "9PH":
                                drgResult.setDC("0909");
                                break;
                            case "9PE"://Perianal and Pilonidal PDC 9PE
                                drgResult.setDC("0907");
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
                            case "9A"://Skin Ulcer
                                drgResult.setDC("0950");
                                break;
                            case "9B"://Severe Skin Disorders
                                drgResult.setDC("0951");
                                break;
                            case "9C"://Moderate Skin Disorders
                                drgResult.setDC("0952");
                                break;
                            case "9D"://Minor Skin Disorders
                                drgResult.setDC("0953");
                                break;
                            case "9E"://Malignant Breast Disorders
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0960");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0961");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("0962");
                                } else if (Counter9PCX > 0) { //Dx Procedure
                                    drgResult.setDC("0963");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("0964");
                                } else {//Malignancy 
                                    drgResult.setDC("0954");
                                }
                                break;
                            case "9F"://Non-Malignant Breast Disorders
                                drgResult.setDC("0955");
                                break;
                            case "9G"://Cellulites
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                        && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                                    drgResult.setDC("0956");
                                } else {
                                    drgResult.setDC("0957");
                                }
                                break;
                            case "9H"://Trauma
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                        && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                                    drgResult.setDC("0958");
                                } else {
                                    drgResult.setDC("0959");
                                }
                                break;
                        }
                    }
                } else {

                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0912");
                    } else {
                        drgResult.setDC("0913");
                    }
                }
            } else if (mdcprocedureCounter > 0) {
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
                    case "9PJ"://Pedicle Graft Plastic Procedures
                        drgResult.setDC("0911");
                        break;
                    case "9PD"://Skin Graft and Debridement
                        if (Counter9BX > 0) {
                            if (Counter9PBX > 0) {
                                drgResult.setDC("0910");
                            } else {
                                drgResult.setDC("0905");
                            }
                        } else {
                            if (Counter9PBX > 0) {
                                drgResult.setDC("0915");
                            } else {
                                drgResult.setDC("0906");
                            }
                        }
                        break;
                    case "9PA"://Total Mastectomy
                        if (Counter9PDX > 0) {
                            drgResult.setDC("0914");
                        } else {
                            if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                                for (int a = 0; a < SecondaryList.size(); a++) {
                                    String MalignantCodes = SecondaryList.get(a);
                                    DRGWSResult MaligSDxResult = gm.PDxMalignancy(datasource, MalignantCodes, MPDCCodes);
                                    if (String.valueOf(MaligSDxResult.isSuccess()).equals("true")) {
                                        sdxfinder.add(SecondaryList.get(a));
                                    }
                                }
                                if (!sdxfinder.isEmpty()) {
                                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                }
                                drgResult.setDC("0901");
                            } else {
                                drgResult.setDC("0903");
                            }
                        }
                        break;

                    case "9PF"://Plastic
                        drgResult.setDC("0908");
                        break;
                    case "9PC"://Subtotal Mastextomy, Biopsy and Local Excision of Breast
                    case "9PB":
                        if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                            for (int a = 0; a < SecondaryList.size(); a++) {
                                String MalignantCodes = SecondaryList.get(a);
                                DRGWSResult MaligSDxResult = gm.PDxMalignancy(datasource, MalignantCodes, MPDCCodes);
                                if (String.valueOf(MaligSDxResult.isSuccess()).equals("true")) {
                                    sdxfinder.add(SecondaryList.get(a));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                            drgResult.setDC("0902");
                        } else {
                            if (drgResult.getPDC().equals("9PB")) {
                                drgResult.setDC("0903");
                            } else {
                                drgResult.setDC("0904");
                            }
                        }
                        break;
                    case "9PG"://Other Skin, Subcutaneous Tissue and Breast Procedures
                    case "9PH":
                        drgResult.setDC("0909");
                        break;
                    case "9PE"://Perianal and Pilonidal PDC 9PE
                        drgResult.setDC("0907");
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
                    case "9A"://Skin Ulcer
                        drgResult.setDC("0950");
                        break;
                    case "9B"://Severe Skin Disorders
                        drgResult.setDC("0951");
                        break;
                    case "9C"://Moderate Skin Disorders
                        drgResult.setDC("0952");
                        break;
                    case "9D"://Minor Skin Disorders
                        drgResult.setDC("0953");
                        break;
                    case "9E"://Malignant Breast Disorders
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0960");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0961");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0962");
                        } else if (Counter9PCX > 0) { //Dx Procedure
                            drgResult.setDC("0963");
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0964");
                        } else {//Malignancy 
                            drgResult.setDC("0954");
                        }
                        break;
                    case "9F"://Non-Malignant Breast Disorders
                        drgResult.setDC("0955");
                        break;
                    case "9G"://Cellulites
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                            drgResult.setDC("0956");
                        } else {
                            drgResult.setDC("0957");
                        }
                        break;
                    case "9H"://Trauma
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                            drgResult.setDC("0958");
                        } else {
                            drgResult.setDC("0959");
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
                    drgResult.setDRGName("No DRG Name Found");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 09 Done Checking");
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC09.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
