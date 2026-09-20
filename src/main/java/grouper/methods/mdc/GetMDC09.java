/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.methods.validation.PDxMalignancy;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC09 {

    public GetMDC09() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC09.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC09(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
        String mdcWithoutZeros = String.valueOf(mdcAsInt);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter9PCX = 0;
            int Counter9PBX = 0;

            //Seach Malignant using ICD10 PDx and SDx
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int SDxMalignantCount = 0;
            int PDxMalignantCount = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, SecondaryList.get(a).trim(), "9E").isSuccess()) {
                    SDxMalignantCount++;
                }
                if (checkAX.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "9E").isSuccess()) {
                PDxMalignantCount++;
            }
            //PDX Skin Ulcer or Cellulitis  AX 9BX
            int Counter9BX = 0;
            if (checkAX.AX(datasource, SchemaName, "9BX", grouperparameter.getPdx()).isSuccess()) {
                Counter9BX++;
            }
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            int Counter9PDX = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                if (checkAX.AX(datasource, SchemaName, "9PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter9PDX++;
                }
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        ProcedureList.get(y).trim(),
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    PBX99Proc++;
                }
                if (checkAX.AX(datasource, SchemaName, "9PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter9PCX++;
                }
                if (checkAX.AX(datasource, SchemaName, "9PBX(", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter9PBX++;
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
                        MDC5Proc getMdcProc = this.mdcProcedure(
                                drgResult.getPDC(),
                                Counter9BX,
                                Counter9PBX,
                                Counter9PDX,
                                SDxMalignantCount,
                                PDxMalignantCount,
                                SecondaryList,
                                datasource,
                                SchemaName
                        );
                        drgResult.setDC(getMdcProc.getDC());
                        if (!getMdcProc.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getMdcProc.getSdxfinder());
                        }
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(ORProcedureCounterList);
                        drgResult.setDC(dc);
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter9PCX,
                                PBX99Proc,
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate());
                        drgResult.setDC(dc);
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
                MDC5Proc getMdcProc = this.mdcProcedure(
                        drgResult.getPDC(),
                        Counter9BX,
                        Counter9PBX,
                        Counter9PDX,
                        SDxMalignantCount,
                        PDxMalignantCount,
                        SecondaryList,
                        datasource,
                        SchemaName
                );
                drgResult.setDC(getMdcProc.getDC());
                if (!getMdcProc.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getMdcProc.getSdxfinder());
                }
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(ORProcedureCounterList);
                drgResult.setDC(dc);
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter9PCX,
                        PBX99Proc,
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate());
                drgResult.setDC(dc);
            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 09 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC9 Method");
            logger.error("Error in MDC9 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    public MDC5Proc mdcProcedure(
            final String pdc,
            final Integer Counter9BX,
            final Integer Counter9PBX,
            final Integer Counter9PDX,
            final Integer SDxMalignantCount,
            final Integer PDxMalignantCount,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName) {
        MDC5Proc result = new MDC5Proc();
        result.setPDC("");
        result.setDC("");
        result.setSdxfinder("");
        ArrayList<String> sdxfinder = new ArrayList<>();
        PDxMalignancy malignant = new PDxMalignancy();
        switch (pdc.toUpperCase()) {
            case "9PJ"://Pedicle Graft Plastic Procedures
                result.setDC("0911");
                break;
            case "9PD"://Skin Graft and Debridement
                if (Counter9BX > 0) {
                    if (Counter9PBX > 0) {
                        result.setDC("0910");
                    } else {
                        result.setDC("0905");
                    }
                } else {
                    if (Counter9PBX > 0) {
                        result.setDC("0915");
                    } else {
                        result.setDC("0906");
                    }
                }
                break;
            case "9PA"://Total Mastectomy
                if (Counter9PDX > 0) {
                    result.setDC("0914");
                } else {
                    if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                        for (int a = 0; a < SecondaryList.size(); a++) {
                            String MalignantCodes = SecondaryList.get(a);
                            DRGWSResult MaligSDxResult = malignant.PDxMalignancy(datasource, SchemaName, MalignantCodes, "9E");
                            if (MaligSDxResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(a));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            result.setSdxfinder(String.join(",", sdxfinder));
                        }
                        result.setDC("0901");
                    } else {
                        result.setDC("0903");
                    }
                }
                break;
            case "9PF"://Plastic
                result.setDC("0908");
                break;
            case "9PC"://Subtotal Mastextomy, Biopsy and Local Excision of Breast
            case "9PB":
                if (SDxMalignantCount > 0 || PDxMalignantCount > 0) {
                    for (int a = 0; a < SecondaryList.size(); a++) {
                        String MalignantCodes = SecondaryList.get(a);
                        DRGWSResult MaligSDxResult = malignant.PDxMalignancy(datasource, SchemaName, MalignantCodes, "9E");
                        if (MaligSDxResult.isSuccess()) {
                            sdxfinder.add(SecondaryList.get(a));
                        }
                    }
                    if (!sdxfinder.isEmpty()) {
                        result.setSdxfinder(String.join(",", sdxfinder));
                    }
                    result.setDC("0902");
                } else {
                    if (pdc.equals("9PB")) {
                        result.setDC("0903");
                    } else {
                        result.setDC("0904");
                    }
                }
                break;
            case "9PG"://Other Skin, Subcutaneous Tissue and Breast Procedures
            case "9PH":
                result.setDC("0909");
                break;
            case "9PE"://Perianal and Pilonidal PDC 9PE
                result.setDC("0907");
                break;

        }
        return result;
    }

    public String orProcedure(ArrayList<Integer> ORProcedureCounterList) {
        String dc = "";
        switch (Collections.max(ORProcedureCounterList)) {
            case 1:
                dc = "2601";
                break;
            case 2:
                dc = "2602";
                break;
            case 3:
                dc = "2603";
                break;
            case 4:
                dc = "2604";
                break;
            case 5:
                dc = "2605";
                break;
            case 6:
                dc = "2606";
                break;
        }
        return dc;
    }

    public String principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter9PCX,
            final Integer PBX99Proc,
            final String bdate,
            final String admDate) {
        String dc = "";
        switch (pdc) {
            case "9A"://Skin Ulcer
                dc = "0950";
//                drgResult.setDC("0950");
                break;
            case "9B"://Severe Skin Disorders
                dc = "0951";
                break;
            case "9C"://Moderate Skin Disorders
                dc = "0952";
                break;
            case "9D"://Minor Skin Disorders
                dc = "0953";
                break;
            case "9E"://Malignant Breast Disorders
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0960";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0961";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0962";
                } else if (Counter9PCX > 0) { //Dx Procedure
                    dc = "0963";
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0964";
                } else {//Malignancy 
                    dc = "0954";
                }
                break;
            case "9F"://Non-Malignant Breast Disorders
                dc = "0955";
                break;
            case "9G"://Cellulites
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    dc = "0956";
                } else {
                    dc = "0957";
                }
                break;
            case "9H"://Trauma
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    dc = "0958";
                } else {
                    dc = "0959";
                }
                break;
        }
        return dc;

    }

    public class MDC5Proc {

        private String PDC;
        private String DC;
        private String sdxfinder;

        public String getPDC() {
            return PDC;
        }

        public void setPDC(String PDC) {
            this.PDC = PDC;
        }

        public String getDC() {
            return DC;
        }

        public void setDC(String DC) {
            this.DC = DC;
        }

        public String getSdxfinder() {
            return sdxfinder;
        }

        public void setSdxfinder(String sdxfinder) {
            this.sdxfinder = sdxfinder;
        }

    }

}
