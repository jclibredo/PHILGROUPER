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
public class GetMDC11 {

    public GetMDC11() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC11(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            // 1. Pre-instantiate service objects (Do this ONCE outside the loop)
            AX axService = new AX();
            ORProcedure orService = new ORProcedure();
            MDCProcedureMethod mdcService = new MDCProcedureMethod();
            GetPDC pdcService = new GetPDC();
            String currentMdc = drgResult.getMDC();

            // 2. Initial Counter setup
            int PDXCounter99 = 0, PCXCounter99 = 0, Counter11PBX = 0, Counter11PCX = 0;
            int ORProcedureCounter = 0, mdcprocedureCounter = 0;
            int CartProc = 0, CaCRxProc = 0, PBX99Proc = 0;

            List<Integer> ORProcedureCounterList = new ArrayList<>();
            List<Integer> hierarvalue = new ArrayList<>();
            List<String> pdclist = new ArrayList<>();

            // 3. Process Procedures efficiently
            String procData = grouperparameter.getProc();
            if (procData != null && !procData.isEmpty()) {
                for (String rawCode : procData.split(",")) {
                    String code = rawCode.trim();
                    if (code.isEmpty()) {
                        continue;
                    }
                    // Group utility checks to avoid repeated calls
                    if (utility.isValid99PEX(code)) {
                        CartProc++;
                    }
                    if (utility.isValid99PFX(code)) {
                        CaCRxProc++;
                    }
                    if (utility.isValid99PDX(code)) {
                        PDXCounter99++;
                    }
                    if (utility.isValid99PCX(code)) {
                        PCXCounter99++;
                    }
                    if (utility.isValid11PBX(code)) {
                        Counter11PBX++;
                    }

                    // Use the pre-instantiated axService
                    if (axService.AX(datasource, "11PCX", code).isSuccess()) {
                        Counter11PCX++;
                    }

                    // OR Procedure check
                    DRGWSResult orResult = orService.ORProcedure(datasource, code);
                    if (orResult.isSuccess()) {
                        ORProcedureCounter++;
                        ORProcedureCounterList.add(Integer.valueOf(orResult.getResult()));
                    }

                    // MDC Procedure check
                    DRGWSResult mdcResult = mdcService.MDCProcedure(datasource, code, currentMdc);
                    if (mdcResult.isSuccess()) {
                        mdcprocedureCounter++;
                        MDCProcedure mdcProc = utility.objectMapper().readValue(mdcResult.getResult(), MDCProcedure.class);

                        DRGWSResult pdcRes = pdcService.GetPDC(datasource, mdcProc.getA_PDC(), currentMdc);
                        if (pdcRes.isSuccess()) {
                            PDC hiarResult = utility.objectMapper().readValue(pdcRes.getResult(), PDC.class);
                            hierarvalue.add(hiarResult.getHIERAR());
                            pdclist.add(hiarResult.getPDC());
                        }
                    }
                }
            }

            // 4. Process Secondary Diagnosis (SDX)
            int CartSDx = 0, CaCRxSDx = 0;
            String sdxData = grouperparameter.getSdx();
            if (sdxData != null && !sdxData.isEmpty()) {
                for (String rawSdx : sdxData.split(",")) {
                    String sdx = rawSdx.trim();
                    if (sdx.isEmpty()) {
                        continue;
                    }

                    if (utility.isValid99BX(sdx)) {
                        CartSDx++;
                    }
                    if (utility.isValid99CX(sdx)) {
                        CaCRxSDx++;
                    }
                }
            }

            // 5. Final check using pre-instantiated Malignancy service
            int Counter11C = new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "11C").isSuccess() ? 1 : 0;
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            // 1. Calculate LOS and Tracheostomy status once
            boolean isTracheostomy = PDXCounter99 > 0;
            long los = utility.ComputeLOS(
                    grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())
            );
            // 2. Main Logic Flow (Flat structure)
            if (isTracheostomy && los >= 21) {
                drgResult.setDC(PCXCounter99 > 0 ? "1113" : "1114");
            } else {
                // Shared Logic: Call private methods to handle specific counters
                if (mdcprocedureCounter > 0) {
                    processMDCProcedure(drgResult, hierarvalue, pdclist, Counter11C, Counter11PBX);
                } else if (ORProcedureCounter > 0) {
                    processORProcedure(drgResult, ORProcedureCounterList);
                } else {
                    processDefaultPDC(drgResult, grouperparameter, utility, CartSDx, CaCRxSDx, CartProc, CaCRxProc, Counter11PCX, PBX99Proc, Counter11PBX);
                }
            }

            //SECOND STAGE OF PROCESS
//            DRG drgService = new DRG();
//            String dc = drgResult.getDC();
//            String currentDrg = drgResult.getDRG();
//            if (currentDrg == null) {
//                if (utility.isValidDCList(dc)) {
//                    drgResult.setDRG(dc + "9");
//                } else {
//                    // 2. Extract complex logic into a single call
//                    String sdxfinalList = new CleanSDxDCDeterminationPLSQL()
//                            .CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), dc);
//                    DRGWSResult pcclResult = new GetPCCL().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
//                    if (pcclResult.isSuccess()) {
//                        DRGOutput finalOutput = utility.objectMapper().readValue(pcclResult.getResult(), DRGOutput.class);
//                        String finalDrg = finalOutput.getDRG();
//                        // 3. Cache the DRG lookup result to avoid calling it twice in the if/else
//                        DRGWSResult drgLookup = drgService.DRG(datasource, dc, finalDrg);
//                        if (drgLookup.isSuccess()) {
//                            drgResult.setDRG(finalDrg);
//                            drgResult.setDRGName(drgLookup.getMessage());
//                        } else {
//                            // 4. Fallback logic
//                            DRGWSResult validatedPccl = new ValidatePCCL().ValidatePCCL(datasource, dc, finalDrg);
//                            if (validatedPccl.isSuccess()) {
//                                String fullDrg = dc + validatedPccl.getResult();
//                                drgResult.setDRG(fullDrg);
//                                // Get name for the newly constructed DRG
//                                drgResult.setDRGName(drgService.DRG(datasource, dc, fullDrg).getMessage());
//                            } else {
//                                drgResult.setDRG(finalDrg);
//                                drgResult.setDRGName("Grouper Error");
//                            }
//                        }
//                    } else {
//                        drgResult.setDRG(dc + "X");
//                        drgResult.setDRGName("Grouper Error");
//                    }
//                }
//            } else {
//                // 5. Handle the case where DRG is already present
//                DRGWSResult drgLookup = drgService.DRG(datasource, dc, currentDrg);
//                drgResult.setDRGName(drgLookup.isSuccess() ? drgLookup.getMessage() : "Grouper Error");
//            }
//            result.setSuccess(true);
//            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//            result.setMessage("MDC 11 Done Checking");
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 11 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC11.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private void processMDCProcedure(DRGOutput drgResult, List<Integer> hierarvalue, List<String> pdclist, int c11C, int c11PBX) {
        // Java 8 Streams to find the minimum value
        int min = hierarvalue.stream().min(Integer::compare).orElse(0);
        String selectedPdc = pdclist.get(hierarvalue.indexOf(min));
        drgResult.setPDC(selectedPdc);

        switch (selectedPdc) {
            case "11PA":
                drgResult.setDC("1101");
                break;
            case "11PL":
                drgResult.setDC("1115");
                break;
            case "11PB":
                drgResult.setDC("1102");
                break;
            case "11PD":
                drgResult.setDC("1105");
                break;
            case "11PH":
                drgResult.setDC("1109");
                break;
            case "11PF":
                drgResult.setDC("1107");
                break;
            case "11PG":
                drgResult.setDC("1108");
                break;
            case "11PE":
                drgResult.setDC("1106");
                break;
            case "11PC":
                drgResult.setDC(c11C > 0 ? "1103" : "1104");
                break;
            case "11PJ":
            case "11PK":
                if (c11PBX > 0) {
                    drgResult.setDC("1116");
                } else {
                    drgResult.setDC(selectedPdc.equals("11PJ") ? "1110" : "1111");
                }
                break;
        }
    }

    private void processORProcedure(DRGOutput drgResult, List<Integer> orList) {
        int maxVal = Collections.max(orList);
        if (maxVal >= 1 && maxVal <= 6) {
            drgResult.setDC("260" + maxVal); // Concatenation replaces the 6-case switch
        }
    }

    private void processDefaultPDC(final DRGOutput drgResult,
            GrouperParameter gp, Utility utility, int cartSDx, int caCRxSDx, int cartProc, int caCRxProc, int c11PCX, int pbx99, int c11PBX) {
        String pdc = drgResult.getPDC();
        int age = utility.ComputeYear(gp.getBirthDate(), gp.getAdmissionDate());
        boolean isNewbornValid = utility.ComputeDay(gp.getBirthDate(), gp.getAdmissionDate()) > 0;

        switch (pdc) {
            case "11C":
                if (cartSDx > 0 && caCRxSDx > 0 && cartProc > 0 && caCRxProc > 0) {
                    drgResult.setDC("1161");
                } else if (caCRxSDx > 0 && caCRxProc > 0) {
                    drgResult.setDC("1162");
                } else if (cartSDx > 0 && cartProc > 0) {
                    drgResult.setDC("1163");
                } else if (c11PCX > 0) {
                    drgResult.setDC("1164");
                } else if (pbx99 > 0) {
                    drgResult.setDC("1165");
                } else {
                    drgResult.setDC("1153");
                }
                break;
            case "11A":
                drgResult.setDC((age >= 17 && isNewbornValid) ? "1150" : "1151");
                break;
            case "11J":
                if (age >= 17 && isNewbornValid) {
                    drgResult.setDC(gp.getDischargeType().equals("4") ? "1167" : "1159");
                } else {
                    drgResult.setDC("1160");
                }
                break;
            case "11E":
                drgResult.setDC(c11PBX > 0 ? "1112" : "1155");
                break;
            case "11B":
                drgResult.setDC("1152");
                break;
            case "11D":
                drgResult.setDC("1154");
                break;
            case "11F":
                drgResult.setDC("1156");
                break;
            case "11G":
                drgResult.setDC("1157");
                break;
            case "11H":
                drgResult.setDC("1158");
                break;
            case "11K":
                drgResult.setDC("1166");
                break;
        }
    }

}
