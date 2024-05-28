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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC05 {

    public GetMDC05() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC05(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
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
            }

            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                String proc = ProcedureList.get(y);
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc.trim(), drgResult.getMDC());
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
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc.trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }

            }

            //5BX USES PRIMARY CODES
            String BX5 = "5BX";
            int Counter5BX = 0;
            DRGWSResult Result5BX = gm.AX(datasource, BX5, grouperparameter.getPdx());
            if (Result5BX.isSuccess()) {
                Counter5BX++;
            }
            // AX 5CX
            int Counter5CX = 0;
            String CX5 = "5CX";
            // AX 5CX
            int Counter5DXSDx = 0;
            int Counter5DXPDx = 0;
            String SDX5 = "5DX";
            for (int x = 0; x < SecondaryList.size(); x++) {
                String SeconRes = SecondaryList.get(x);

                DRGWSResult CX5Result = gm.AX(datasource, CX5, SeconRes.trim());
                if (CX5Result.isSuccess()) {
                    Counter5CX++;
                }

                DRGWSResult SDx5Result = gm.AX(datasource, SDX5, SeconRes.trim());
                if (SDx5Result.isSuccess()) {
                    Counter5DXSDx++;
                }
            }

            DRGWSResult PDx5Result = gm.AX(datasource, SDX5, grouperparameter.getPdx());
            if (PDx5Result.isSuccess()) {
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
            //Cardiac Cath PDC 5PT
            String pdc5PT = "5PT";
            int CardiacCount = 0;
            //AX 5PBX
            //Permanent Pacemaker 5PK
            String pdc5PK = "5PK";
            int PPCount = 0;
            int Counter5PBX = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                //AX 5PEX
                DRGWSResult Result5PEX = gm.AX(datasource, "5PEX", ProcedureList.get(x).trim());
                if (Result5PEX.isSuccess()) {
                    Counter5PEX++;
                }
                //AX 5PCX
                DRGWSResult Result5PCX = gm.AX(datasource, "5PCX", ProcedureList.get(x).trim());
                if (Result5PCX.isSuccess()) {
                    Counter5PCX++;
                }
                //AX 5PFX
                DRGWSResult Result5PFX = gm.AX(datasource, "5PFX", ProcedureList.get(x).trim());
                if (Result5PFX.isSuccess()) {
                    Counter5PFX++;
                }
                //AX 5PDX
                DRGWSResult Result5PDX = gm.AX(datasource, "5PDX", ProcedureList.get(x).trim());
                if (Result5PDX.isSuccess()) {
                    Counter5PDX++;
                }

                //AX 5PGX
                DRGWSResult Result5PGX = gm.AX(datasource, "5PGX", ProcedureList.get(x).trim());
                if (Result5PGX.isSuccess()) {
                    Counter5PGX++;
                }

                //AX 5PHX
                DRGWSResult Result5PHX = gm.AX(datasource, "5PHX", ProcedureList.get(x).trim());
                if (Result5PHX.isSuccess()) {
                    Counter5PHX++;
                }

                DRGWSResult Result5PJX = gm.AX(datasource, "5PJX", ProcedureList.get(x).trim());
                if (Result5PJX.isSuccess()) {
                    Counter5PJX++;
                }

                //Cardiac Cath PDC 5PT
                DRGWSResult getCardiacResult = gm.Endovasc(datasource, ProcedureList.get(x).trim(), pdc5PT, drgResult.getMDC());
                if (getCardiacResult.isSuccess()) {
                    CardiacCount++;
                }
                //AX 5PBX
                DRGWSResult Result5PBX = gm.AX(datasource, "5PBX", ProcedureList.get(x).trim());
                if (Result5PBX.isSuccess()) {
                    Counter5PBX++;
                }

                DRGWSResult getPPResult = gm.Endovasc(datasource, ProcedureList.get(x).trim(), pdc5PK, drgResult.getMDC());
                if (getPPResult.isSuccess()) {
                    PPCount++;
                }
            }
            //AMI PDC 5A
            String pdc5A = "5A";
            int AMICount = 0;
            DRGWSResult getAMIResult = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), pdc5A);
            if (getAMIResult.isSuccess()) {
                AMICount++;
            }

            // THIS AREA WILL START THE CONDITIONAL STATEMENT FOR THIS MDC
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {

                    if (AMICount > 0) {
                        if (Counter5PEX > 0) {
                            if (Counter5PCX > 0) {
                                drgResult.setDC("0525");
                            } else {
                                drgResult.setDC("0526");
                            }
                        } else if (drgResult.getPDC().equals("5PK")) { //DOUBLE CHECK THIS AREA
                            drgResult.setDC("0510");

                        } else if (Counter5PFX > 0) {
                            if (Counter5PDX > 0) {
                                drgResult.setDC("0527");
                            } else {
                                drgResult.setDC("0528");
                            }
                        } else if (Counter5PGX > 0) {

                            if (Counter5PDX > 0) {
                                drgResult.setDC("0529");
                            } else {
                                drgResult.setDC("0530");
                            }
                        } else if (Counter5PHX > 0) {
                            if (Counter5CX > 0) {
                                for (int x = 0; x < SecondaryList.size(); x++) {
                                    DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                                    if (sdxfinderResult.isSuccess()) {
                                        sdxfinder.add(SecondaryList.get(x));
                                    }
                                }
                                if (!sdxfinder.isEmpty()) {
                                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                }
                                drgResult.setDC("0550");
                            } else {
                                drgResult.setDC("0551");
                            }
                        } else if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0569");
                        } else {
                            if (Counter5CX > 0) {

                                for (int x = 0; x < SecondaryList.size(); x++) {
                                    DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                                    if (sdxfinderResult.isSuccess()) {
                                        sdxfinder.add(SecondaryList.get(x));
                                    }
                                }
                                if (!sdxfinder.isEmpty()) {
                                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                }
                                drgResult.setDC("0552");
                            } else {
                                drgResult.setDC("0553");
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
                            case "5PE"://Thoracoabdominal Procedures Combination
                                drgResult.setDC("0507");
                                break;
                            case "5PC"://Coronary Bypass
                                if (Counter5PCX > 0) {
                                    drgResult.setDC("0503");
                                } else {
                                    if (CardiacCount > 0) {//Cardiac Cath
                                        drgResult.setDC("0504");
                                    } else {
                                        drgResult.setDC("0505");
                                    }
                                }
                                break;
                            case "5PV"://Multiple Valve Procedures
                                if (Counter5PBX > 0) { //Cardiac Cath
                                    drgResult.setDC("0532");
                                } else {
                                    drgResult.setDC("0533");
                                }
                                break;
                            case "5PX"://Complex Cardiothoracic Procedures
                                drgResult.setDC("0537");
                                break;
                            case "5PA"://Valve Replacement and Open Valvuloplasty
                                if (Counter5PBX > 0) {
                                    drgResult.setDC("0501");
                                } else {
                                    drgResult.setDC("0502");
                                }
                                break;
                            case "5PD"://Other Cardiothoracic Procedures
                                drgResult.setDC("0506");
                                break;
                            case "5PF"://Major Cardiovascular Procedures
                                drgResult.setDC("0508");
                                break;
                            case "5PU"://Simple Cardiothoracic Procedures
                                drgResult.setDC("0513");
                                break;
                            case "5PH"://Cardiac Electrophysiologic Procedures
                                drgResult.setDC("0514");
                                break;
                            case "5PG"://Percutaneous Cardiovascular Procedures
                                if (Counter5PDX > 0) {
                                    drgResult.setDC("0523");
                                } else {
                                    drgResult.setDC("0524");
                                }
                                break;
                            case "5PS"://Other Vascular Procedures
                                if (Counter5PJX > 0) {
                                    drgResult.setDC("0531");
                                } else {
                                    drgResult.setDC("0515");
                                }
                                break;
                            case "5PW"://Multiple Wound Debridement
                                drgResult.setDC("0536");
                                break;
                            case "5PK"://Permanent Pacemaker
                                if (Counter5BX > 0) {
                                    drgResult.setDC("0510");
                                } else {
                                    drgResult.setDC("0511");
                                }
                                break;
                            case "5PJ"://Major Amputation
                                drgResult.setDC("0509");
                                break;
                            case "5PM"://Automatic Cardioverter Procedures
                                drgResult.setDC("0512");
                                break;
                            case "5PP"://Pacemaker Device Replacement
                                drgResult.setDC("0518");
                                break;
                            case "5PN"://Pacemaker Revision
                                drgResult.setDC("0517");
                                break;
                            case "5PR"://Other Circulatory System OR Procedures
                                drgResult.setDC("0520");
                                break;
                            case "5PT"://Cardiac Cath
                                if (Counter5DXPDx > 0 || Counter5DXSDx > 0) {
                                    for (int x = 0; x < SecondaryList.size(); x++) {
                                        DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                                        if (sdxfinderResult.isSuccess()) {
                                            sdxfinder.add(SecondaryList.get(x));
                                        }
                                    }
                                    if (!sdxfinder.isEmpty()) {
                                        drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                    }
                                    drgResult.setDC("0521");
                                } else {
                                    drgResult.setDC("0522");
                                }
                                break;
                            case "5PL"://Minor Amputation
                                drgResult.setDC("0516");
                                break;
                            default://Vein Ligation and Stripping
                                drgResult.setDC("0519");
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
                            case "5B"://Infective Endocarditis
                                drgResult.setDC("0554");
                                break;
                            case "5C"://Heart Failure and Shock
                                drgResult.setDC("0555");
                                break;
                            case "5D"://Venous Thrombosis
                                drgResult.setDC("0556");
                                break;
                            case "5E"://Skin Ulcer for Circulatory Disorders
                                drgResult.setDC("0557");
                                break;
                            case "5F"://Peripheral Vascular
                                if (grouperparameter.getDischargeType().equals("4")) {
                                    drgResult.setDC("0570");
                                } else {
                                    drgResult.setDC("0558");
                                }
                                break;
                            case "5G"://Coronary Atherosclerosis
                                drgResult.setDC("0559");
                                break;
                            case "5H"://Hypertension
                                drgResult.setDC("0560");
                                break;
                            case "5J"://Congenital Heart Disease
                                drgResult.setDC("0561");
                                break;
                            case "5K"://Valvular Disorders
                                drgResult.setDC("0562");
                                break;
                            case "5L"://Major Arrhythmia and Cardiac Arrest
                                drgResult.setDC("0563");
                                break;
                            case "5M"://Non-major Arrhythmia and Conduction Disorders
                                drgResult.setDC("0564");
                                break;
                            case "5N"://Unstable Angina
                                drgResult.setDC("0565");
                                break;
                            case "5P"://Syncope and Collapse
                                drgResult.setDC("0566");
                                break;
                            case "5Q"://Chest Pain
                                drgResult.setDC("0567");
                                break;
                            case "5R"://Other Circulatory System Diagnoses PDC 5R
                                drgResult.setDC("0568");
                                break;
                        }
                    }

                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0534");
                    } else {
                        drgResult.setDC("0535");
                    }
                }

            } else if (AMICount > 0) {
                if (Counter5PEX > 0) {
                    if (Counter5PCX > 0) {
                        drgResult.setDC("0525");
                    } else {
                        drgResult.setDC("0526");
                    }
                } else if (PPCount > 0) {
                    drgResult.setDC("0510");
                } else if (Counter5PFX > 0) {
                    if (Counter5PDX > 0) {
                        drgResult.setDC("0527");
                    } else {
                        drgResult.setDC("0528");
                    }
                } else if (Counter5PGX > 0) {
                    if (Counter5PDX > 0) {
                        drgResult.setDC("0529");
                    } else {
                        drgResult.setDC("0530");
                    }
                } else if (Counter5PHX > 0) {
                    if (Counter5CX > 0) {
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        drgResult.setDC("0550");
                    } else {
                        drgResult.setDC("0551");
                    }
                } else if (grouperparameter.getDischargeType().equals("4")) {
                    drgResult.setDC("0569");
                } else {
                    if (Counter5CX > 0) {
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        drgResult.setDC("0552");
                    } else {
                        drgResult.setDC("0553");
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
                    case "5PE"://Thoracoabdominal Procedures Combination
                        drgResult.setDC("0507");
                        break;
                    case "5PC"://Coronary Bypass
                        if (Counter5PCX > 0) {
                            drgResult.setDC("0503");
                        } else {
                            if (CardiacCount > 0) {//Cardiac Cath
                                drgResult.setDC("0504");
                            } else {
                                drgResult.setDC("0505");
                            }
                        }
                        break;
                    case "5PV"://Multiple Valve Procedures
                        if (Counter5PBX > 0) { //Cardiac Cath
                            drgResult.setDC("0532");
                        } else {
                            drgResult.setDC("0533");
                        }
                        break;
                    case "5PX"://Complex Cardiothoracic Procedures
                        drgResult.setDC("0537");
                        break;
                    case "5PA"://Valve Replacement and Open Valvuloplasty
                        if (Counter5PBX > 0) {
                            drgResult.setDC("0501");
                        } else {
                            drgResult.setDC("0502");
                        }
                        break;
                    case "5PD"://Other Cardiothoracic Procedures
                        drgResult.setDC("0506");
                        break;
                    case "5PF"://Major Cardiovascular Procedures
                        drgResult.setDC("0508");
                        break;
                    case "5PU"://Simple Cardiothoracic Procedures
                        drgResult.setDC("0513");
                        break;
                    case "5PH"://Cardiac Electrophysiologic Procedures
                        drgResult.setDC("0514");
                        break;
                    case "5PG"://Percutaneous Cardiovascular Procedures
                        if (Counter5PDX > 0) {
                            drgResult.setDC("0523");
                        } else {
                            drgResult.setDC("0524");
                        }
                        break;
                    case "5PS"://Other Vascular Procedures
                        if (Counter5PJX > 0) {
                            drgResult.setDC("0531");
                        } else {
                            drgResult.setDC("0515");
                        }
                        break;
                    case "5PW"://Multiple Wound Debridement
                        drgResult.setDC("0536");
                        break;
                    case "5PK"://Permanent Pacemaker
                        if (Counter5BX > 0) {
                            drgResult.setDC("0510");
                        } else {
                            drgResult.setDC("0511");
                        }
                        break;
                    case "5PJ"://Major Amputation
                        drgResult.setDC("0509");
                        break;
                    case "5PM"://Automatic Cardioverter Procedures
                        drgResult.setDC("0512");
                        break;
                    case "5PP"://Pacemaker Device Replacement
                        drgResult.setDC("0518");
                        break;
                    case "5PN"://Pacemaker Revision
                        drgResult.setDC("0517");
                        break;
                    case "5PR"://Other Circulatory System OR Procedures
                        drgResult.setDC("0520");
                        break;
                    case "5PT"://Cardiac Cath
                        if (Counter5DXPDx > 0 || Counter5DXSDx > 0) {
                            for (int x = 0; x < SecondaryList.size(); x++) {
                                DRGWSResult sdxfinderResult = gm.AX(datasource, CX5, SecondaryList.get(x));
                                if (sdxfinderResult.isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                            drgResult.setDC("0521");
                        } else {
                            drgResult.setDC("0522");
                        }
                        break;
                    case "5PL"://Minor Amputation
                        drgResult.setDC("0516");
                        break;
                    case "5PQ"://Vein Ligation and Stripping PDC 5PQ
                        drgResult.setDC("0519");
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
                    case "5B"://Infective Endocarditis
                        drgResult.setDC("0554");
                        break;
                    case "5C"://Heart Failure and Shock
                        drgResult.setDC("0555");
                        break;
                    case "5D"://Venous Thrombosis
                        drgResult.setDC("0556");
                        break;
                    case "5E"://Skin Ulcer for Circulatory Disorders
                        drgResult.setDC("0557");
                        break;
                    case "5F"://Peripheral Vascular
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0570");
                        } else {
                            drgResult.setDC("0558");
                        }
                        break;
                    case "5G"://Coronary Atherosclerosis
                        drgResult.setDC("0559");
                        break;
                    case "5H"://Hypertension
                        drgResult.setDC("0560");
                        break;
                    case "5J"://Congenital Heart Disease
                        drgResult.setDC("0561");
                        break;
                    case "5K"://Valvular Disorders
                        drgResult.setDC("0562");
                        break;
                    case "5L"://Major Arrhythmia and Cardiac Arrest
                        drgResult.setDC("0563");
                        break;
                    case "5M"://Non-major Arrhythmia and Conduction Disorders
                        drgResult.setDC("0564");
                        break;
                    case "5N"://Unstable Angina
                        drgResult.setDC("0565");
                        break;
                    case "5P"://Syncope and Collapse
                        drgResult.setDC("0566");
                        break;
                    case "5Q"://Chest Pain
                        drgResult.setDC("0567");
                        break;
                    case "5R"://Other Circulatory System Diagnoses PDC 5R
                        drgResult.setDC("0568");
                        break;
                }
            }
            //PROCESS PCCL VALUE
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
            result.setMessage("MDC 05 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC05.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
