/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.Endovasc;
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
public class GetMDC05 {

    public GetMDC05() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC05.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC05(
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
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX checkAX = new AX();
            //5BX USES PRIMARY CODES
            int Counter5BX = 0;
            if (checkAX.AX(datasource, SchemaName, "5BX", grouperparameter.getPdx()).isSuccess()) {
                Counter5BX++;
            }
            // AX 5CX
            int Counter5CX = 0;
            // AX 5CX
            int Counter5DXSDx = 0;
            int Counter5DXPDx = 0;
            for (int x = 0; x < SecondaryList.size(); x++) {
                if (checkAX.AX(datasource, SchemaName, "5CX", SecondaryList.get(x).trim()).isSuccess()) {
                    Counter5CX++;
                }
                if (checkAX.AX(datasource, SchemaName, "5DX", SecondaryList.get(x).trim()).isSuccess()) {
                    Counter5DXSDx++;
                }
            }

            if (checkAX.AX(datasource, SchemaName, "5DX", grouperparameter.getPdx()).isSuccess()) {
                Counter5DXPDx++;
            }
            //AX 5PEX
            int Counter5PEX = 0;
            //AX 5PCX
            int Counter5PCX = 0;
            //AX 5PFX
            int Counter5PFX = 0;
            //AX 5PDX
            int Counter5PDX = 0;
            //AX 5PGX
            int Counter5PGX = 0;
            //AX 5PGX
            int Counter5PHX = 0;
            //AX 5PJX
            int Counter5PJX = 0;
            int CardiacCount = 0;
            //AX 5PBX
            int PPCount = 0;
            int Counter5PBX = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        ProcedureList.get(x).trim(),
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
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                //AX 5PEX
                if (checkAX.AX(datasource, SchemaName, "5PEX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PEX++;
                }
                //AX 5PCX
                if (checkAX.AX(datasource, SchemaName, "5PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PCX++;
                }
                //AX 5PFX
                if (checkAX.AX(datasource, SchemaName, "5PFX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PFX++;
                }
                //AX 5PDX
                if (checkAX.AX(datasource, SchemaName, "5PDX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PDX++;
                }
                //AX 5PGX
                if (checkAX.AX(datasource, SchemaName, "5PGX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PGX++;
                }
                //AX 5PHX
                if (checkAX.AX(datasource, SchemaName, "5PHX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PHX++;
                }
                if (checkAX.AX(datasource, SchemaName, "5PJX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PJX++;
                }
                //Cardiac Cath PDC 5PT
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "5PT", mdcWithoutZeros).isSuccess()) {
                    CardiacCount++;
                }
                //AX 5PBX
                if (checkAX.AX(datasource, SchemaName, "5PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter5PBX++;
                }
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "5PK", mdcWithoutZeros).isSuccess()) {
                    PPCount++;
                }
            }
            int AMICount = 0;
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "5A").isSuccess()) {
                AMICount++;
            }

            // THIS AREA WILL START THE CONDITIONAL STATEMENT FOR THIS MDC
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (AMICount > 0) {
                        MDC5Proc getAMICount = this.pdxAMI(
                                Counter5PEX,
                                Counter5PCX,
                                PPCount,
                                Counter5PFX,
                                Counter5PDX,
                                Counter5PGX,
                                Counter5PHX,
                                Counter5CX,
                                SecondaryList,
                                datasource,
                                SchemaName,
                                grouperparameter.getDischargeType());
                        drgResult.setDC(getAMICount.getDc());
                        if (!getAMICount.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getAMICount.getSdxfinder());
                        }
                    } else if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        MDC5Proc getMDCProcedure = this.mdcProcedure(
                                drgResult.getPDC(),
                                SecondaryList,
                                datasource,
                                SchemaName,
                                Counter5PCX,
                                CardiacCount,
                                Counter5PBX,
                                Counter5PDX,
                                Counter5PJX,
                                Counter5BX,
                                Counter5DXPDx,
                                Counter5DXSDx
                        );
                        drgResult.setDC(getMDCProcedure.getDc());
                        if (!getMDCProcedure.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getMDCProcedure.getSdxfinder());
                        }
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(ORProcedureCounterList);
                        drgResult.setDC(dc);
                    } else {
                        String dc = this.principalDaignosis(drgResult.getPDC(), grouperparameter.getDischargeType());
                        drgResult.setDC(dc);
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0534");
                    } else {
                        drgResult.setDC("0535");
                    }
                }

            } else if (AMICount > 0) {
                MDC5Proc getAMICount = this.pdxAMI(
                        Counter5PEX,
                        Counter5PCX,
                        PPCount,
                        Counter5PFX,
                        Counter5PDX,
                        Counter5PGX,
                        Counter5PHX,
                        Counter5CX,
                        SecondaryList,
                        datasource,
                        SchemaName,
                        grouperparameter.getDischargeType());
                drgResult.setDC(getAMICount.getDc());
                if (!getAMICount.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getAMICount.getSdxfinder());
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                MDC5Proc getMDCProcedure = this.mdcProcedure(
                        drgResult.getPDC(),
                        SecondaryList,
                        datasource,
                        SchemaName,
                        Counter5PCX,
                        CardiacCount,
                        Counter5PBX,
                        Counter5PDX,
                        Counter5PJX,
                        Counter5BX,
                        Counter5DXPDx,
                        Counter5DXSDx
                );
                drgResult.setDC(getMDCProcedure.getDc());
                if (!getMDCProcedure.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getMDCProcedure.getSdxfinder());
                }
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(ORProcedureCounterList);
                drgResult.setDC(dc);
            } else {
                String dc = this.principalDaignosis(drgResult.getPDC(), grouperparameter.getDischargeType());
                drgResult.setDC(dc);

            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 05 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC5 Method");
            logger.error("Error in MDC5 Method : {}", ex.getMessage(), ex);
        }

        return result;
    }

    public MDC5Proc pdxAMI(
            final Integer Counter5PEX,
            final Integer Counter5PCX,
            final Integer PPCount,
            final Integer Counter5PFX,
            final Integer Counter5PDX,
            final Integer Counter5PGX,
            final Integer Counter5PHX,
            final Integer Counter5CX,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName,
            final String discharge) {
        AX checkAX = new AX();
        MDC5Proc result = new MDC5Proc();
        result.setPdc("");
        result.setDc("");
        result.setSdxfinder("");
        ArrayList<String> sdxfinder = new ArrayList<>();
        if (Counter5PEX > 0) {
            if (Counter5PCX > 0) {
                result.setDc("0525");
            } else {
                result.setDc("0526");
            }
        } else if (PPCount > 0) {
            result.setDc("0510");
        } else if (Counter5PFX > 0) {
            if (Counter5PDX > 0) {
                result.setDc("0527");
            } else {
                result.setDc("0528");
            }
        } else if (Counter5PGX > 0) {
            if (Counter5PDX > 0) {
                result.setDc("0529");
            } else {
                result.setDc("0530");
            }
        } else if (Counter5PHX > 0) {
            if (Counter5CX > 0) {
                for (int x = 0; x < SecondaryList.size(); x++) {
                    DRGWSResult sdxfinderResult = checkAX.AX(datasource, SchemaName, "5CX", SecondaryList.get(x));
                    if (sdxfinderResult.isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x));
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    result.setSdxfinder(String.join(",", sdxfinder));
                }
                result.setDc("0550");
            } else {
                result.setDc("0551");
            }
        } else if (discharge.equals("4")) {
            result.setDc("0569");
        } else {
            if (Counter5CX > 0) {
                for (int x = 0; x < SecondaryList.size(); x++) {
                    DRGWSResult sdxfinderResult = checkAX.AX(datasource, SchemaName, "5CX", SecondaryList.get(x));
                    if (sdxfinderResult.isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x));
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    result.setSdxfinder(String.join(",", sdxfinder));
                }
                result.setDc("0552");
            } else {
                result.setDc("0553");
            }
        }

        return result;
    }

    public MDC5Proc mdcProcedure(
            final String pdc,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName,
            final Integer Counter5PCX,
            final Integer CardiacCount,
            final Integer Counter5PBX,
            final Integer Counter5PDX,
            final Integer Counter5PJX,
            final Integer Counter5BX,
            final Integer Counter5DXPDx,
            final Integer Counter5DXSDx) {
        MDC5Proc result = new MDC5Proc();
        result.setPdc("");
        result.setDc("");
        result.setSdxfinder("");
        //CHECKING FOR TRAUMA CODES
        AX checkAX = new AX();
        ArrayList<String> sdxfinder = new ArrayList<>();
        switch (pdc) {
            case "5PE"://Thoracoabdominal Procedures Combination
                result.setDc("0507");
                break;
            case "5PC"://Coronary Bypass
                if (Counter5PCX > 0) {
                    result.setDc("0503");
                } else {
                    if (CardiacCount > 0) {//Cardiac Cath
                        result.setDc("0504");
                    } else {
                        result.setDc("0505");
                    }
                }
                break;
            case "5PV"://Multiple Valve Procedures
                if (Counter5PBX > 0) { //Cardiac Cath
                    result.setDc("0532");
                } else {
                    result.setDc("0533");
                }
                break;
            case "5PX"://Complex Cardiothoracic Procedures
                result.setDc("0537");
                break;
            case "5PA"://Valve Replacement and Open Valvuloplasty
                if (Counter5PBX > 0) {
                    result.setDc("0501");
                } else {
                    result.setDc("0502");
                }
                break;
            case "5PD"://Other Cardiothoracic Procedures
                result.setDc("0506");
                break;
            case "5PF"://Major Cardiovascular Procedures
                result.setDc("0508");
                break;
            case "5PU"://Simple Cardiothoracic Procedures
                result.setDc("0513");
                break;
            case "5PH"://Cardiac Electrophysiologic Procedures
                result.setDc("0514");
                break;
            case "5PG"://Percutaneous Cardiovascular Procedures
                if (Counter5PDX > 0) {
                    result.setDc("0523");
                } else {
                    result.setDc("0524");
                }
                break;
            case "5PS"://Other Vascular Procedures
                if (Counter5PJX > 0) {
                    result.setDc("0531");
                } else {
                    result.setDc("0515");
                }
                break;
            case "5PW"://Multiple Wound Debridement
                result.setDc("0536");
                break;
            case "5PK"://Permanent Pacemaker
                if (Counter5BX > 0) {
                    result.setDc("0510");
                } else {
                    result.setDc("0511");
                }
                break;
            case "5PJ"://Major Amputation
                result.setDc("0509");
                break;
            case "5PM"://Automatic Cardioverter Procedures
                result.setDc("0512");
                break;
            case "5PP"://Pacemaker Device Replacement
                result.setDc("0518");
                break;
            case "5PN"://Pacemaker Revision
                result.setDc("0517");
                break;
            case "5PR"://Other Circulatory System OR Procedures
                result.setDc("0520");
                break;
            case "5PT"://Cardiac Cath
                if (Counter5DXPDx > 0 || Counter5DXSDx > 0) {
                    for (int x = 0; x < SecondaryList.size(); x++) {
                        DRGWSResult sdxfinderResult = checkAX.AX(datasource, SchemaName, "5CX", SecondaryList.get(x));
                        if (sdxfinderResult.isSuccess()) {
                            sdxfinder.add(SecondaryList.get(x));
                        }
                    }
                    if (!sdxfinder.isEmpty()) {
                        result.setSdxfinder(String.join(",", sdxfinder));
                    }
                    result.setDc("0521");
                } else {
                    result.setDc("0522");
                }
                break;
            case "5PL"://Minor Amputation
                result.setDc("0516");
                break;
            default://Vein Ligation and Stripping
                result.setDc("0519");
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
            final String discharge) {
        String dc = "";
        switch (pdc) {
            case "5B"://Infective Endocarditis
                dc = "0554";
                break;
            case "5C"://Heart Failure and Shock
                dc = "0555";
                break;
            case "5D"://Venous Thrombosis
                dc = "0556";
                break;
            case "5E"://Skin Ulcer for Circulatory Disorders
                dc = "0557";
                break;
            case "5F"://Peripheral Vascular
                if (discharge.equals("4")) {
                    dc = "0570";
                } else {
                    dc = "0558";
                }
                break;
            case "5G"://Coronary Atherosclerosis
                dc = "0559";
                break;
            case "5H"://Hypertension
                dc = "0560";
                break;
            case "5J"://Congenital Heart Disease
                dc = "0561";
                break;
            case "5K"://Valvular Disorders
                dc = "0562";
                break;
            case "5L"://Major Arrhythmia and Cardiac Arrest
                dc = "0563";
                break;
            case "5M"://Non-major Arrhythmia and Conduction Disorders
                dc = "0564";
                break;
            case "5N"://Unstable Angina
                dc = "0565";
                break;
            case "5P"://Syncope and Collapse
                dc = "0566";
                break;
            case "5Q"://Chest Pain
                dc = "0567";
                break;
            case "5R"://Other Circulatory System Diagnoses PDC 5R
                dc = "0568";
                break;
        }
        return dc;

    }

    public class MDC5Proc {

        private String pdc;
        private String dc;
        private String sdxfinder;

        public String getPdc() {
            return pdc;
        }

        public void setPdc(String pdc) {
            this.pdc = pdc;
        }

        public String getDc() {
            return dc;
        }

        public void setDc(String dc) {
            this.dc = dc;
        }

        public String getSdxfinder() {
            return sdxfinder;
        }

        public void setSdxfinder(String sdxfinder) {
            this.sdxfinder = sdxfinder;
        }

    }

}
