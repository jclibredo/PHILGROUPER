/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.CleanSDxDCDeterminationPLSQL;
import grouper.methods.validation.DRG;
import grouper.methods.validation.GetPCCL;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.methods.validation.PDxMalignancy;
import grouper.methods.validation.ValidatePCCL;
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
public class GetMDC12 {

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC12(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);

        try {
            // 1. Pre-instantiate services (Crucial for performance)
            AX axService = new AX();
            ORProcedure orService = new ORProcedure();
            MDCProcedureMethod mdcService = new MDCProcedureMethod();
            GetPDC pdcService = new GetPDC();
            DRG drgService = new DRG();
            String mdc = drgResult.getMDC();

            // 2. Prepare Data & Counters
            String[] procArray = grouperparameter.getProc() != null ? grouperparameter.getProc().split(",") : new String[0];
            String[] sdxArray = grouperparameter.getSdx() != null ? grouperparameter.getSdx().split(",") : new String[0];

            int PDXCounter99 = 0, PCXCounter99 = 0, PBX12Proc = 0, PBX99Proc = 0;
            int CartProc = 0, CaCRxProc = 0, ORProcedureCounter = 0, mdcprocedureCounter = 0;
            List<Integer> ORProcedureCounterList = new ArrayList<>();
            List<Integer> hierarvalue = new ArrayList<>();
            List<String> pdclist = new ArrayList<>();

            // 3. Process Procedures
            for (String rawCode : procArray) {
                String code = rawCode.trim();
                if (code.isEmpty()) {
                    continue;
                }

                if (utility.isValid99PDX(code)) {
                    PDXCounter99++;
                }
                if (utility.isValid99PCX(code)) {
                    PCXCounter99++;
                }
                if (utility.isValid99PEX(code)) {
                    CartProc++;
                }
                if (utility.isValid99PFX(code)) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(code)) {
                    PBX99Proc++;
                }

                if (axService.AX(datasource, "12PBX", code).isSuccess()) {
                    PBX12Proc++;
                }

                // MDC Procedure Logic
                DRGWSResult joinRes = mdcService.MDCProcedure(datasource, code, mdc);
                if (joinRes.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mProc = utility.objectMapper().readValue(joinRes.getResult(), MDCProcedure.class);
                    DRGWSResult pRes = pdcService.GetPDC(datasource, mProc.getA_PDC(), mdc);
                    if (pRes.isSuccess()) {
                        PDC hiar = utility.objectMapper().readValue(pRes.getResult(), PDC.class);
                        hierarvalue.add(hiar.getHIERAR());
                        pdclist.add(hiar.getPDC());
                    }
                }

                // OR Procedure Logic
                DRGWSResult orRes = orService.ORProcedure(datasource, code);
                if (orRes.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(orRes.getResult()));
                }
            }

            // 4. Process Secondaries & Malignancy
            int CartSDx = 0, CaCRxSDx = 0;
            for (String rawSdx : sdxArray) {
                String sdx = rawSdx.trim();
                if (utility.isValid99BX(sdx)) {
                    CartSDx++;
                }
                if (utility.isValid99CX(sdx)) {
                    CaCRxSDx++;
                }
            }
            boolean isMalignant = new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "12A").isSuccess();

            // 5. Main DC Determination Logic
            long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));

            if (PDXCounter99 > 0 && los >= 21) {
                drgResult.setDC(PCXCounter99 > 0 ? "1209" : "1210");
            } else if (mdcprocedureCounter > 0) {
                applyMDC12ProcedureLogic(drgResult, hierarvalue, pdclist, isMalignant);
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC("260" + Collections.max(ORProcedureCounterList));
            } else {
                applyMDC12DefaultLogic(drgResult, CartSDx, CaCRxSDx, CartProc, CaCRxProc, PBX12Proc, PBX99Proc);
            }

            // 6. DRG Finalization Logic (The "PCCL" section)
            finalizeDRG(drgResult, grouperparameter, datasource, utility, drgService);

            result.setSuccess(true);
            result.setMessage("MDC 12 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC12.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private void applyMDC12ProcedureLogic(DRGOutput drgResult, List<Integer> hierarvalue, List<String> pdclist, boolean isMalignant) {
        int min = Collections.min(hierarvalue);
        String selectedPdc = pdclist.get(hierarvalue.indexOf(min));
        drgResult.setPDC(selectedPdc);

        switch (selectedPdc) {
            case "12PA":
                drgResult.setDC("1201");
                break;
            case "12PB":
                drgResult.setDC("1202");
                break;
            case "12PC":
                drgResult.setDC("1203");
                break;
            case "12PD":
                drgResult.setDC("1204");
                break;
            case "12PE":
                drgResult.setDC("1205");
                break;
            case "12PG":
                drgResult.setDC("1208");
                break;
            case "12PF":
                drgResult.setDC(isMalignant ? "1206" : "1207");
                break;
        }
    }

    private void applyMDC12DefaultLogic(DRGOutput drgResult, int cartS, int cacS, int cartP, int cacP, int pbx12, int pbx99) {
        switch (drgResult.getPDC()) {
            case "12A":
                if (cartS > 0 && cacS > 0 && cartP > 0 && cacP > 0) {
                    drgResult.setDC("1255");
                } else if (cacS > 0 && cacP > 0) {
                    drgResult.setDC("1256");
                } else if (cartS > 0 && cartP > 0) {
                    drgResult.setDC("1257");
                } else if (pbx12 > 0) {
                    drgResult.setDC("1258");
                } else if (pbx99 > 0) {
                    drgResult.setDC("1259");
                } else {
                    drgResult.setDC("1250");
                }
                break;
            case "12B":
                drgResult.setDC("1251");
                break;
            case "12C":
                drgResult.setDC("1252");
                break;
            case "12D":
                drgResult.setDC("1253");
                break;
            case "12E":
                drgResult.setDC("1254");
                break;
        }
    }

    private void finalizeDRG(DRGOutput drgResult, GrouperParameter gp, DataSource ds, Utility utility, DRG drgService) throws IOException {
        if (drgResult.getDRG() == null) {
            if (utility.isValidDCList(drgResult.getDC())) {
                drgResult.setDRG(drgResult.getDC() + "9");
            } else {
                String sdxClean = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(ds, gp.getSdx(), drgResult.getSDXFINDER(), gp.getPdx(), drgResult.getDC());
                DRGWSResult pcclVal = new GetPCCL().GetPCCL(ds, drgResult, gp, sdxClean);
                if (pcclVal.isSuccess()) {
                    DRGOutput output = utility.objectMapper().readValue(pcclVal.getResult(), DRGOutput.class);
                    DRGWSResult drgCheck = drgService.DRG(ds, drgResult.getDC(), output.getDRG());
                    if (drgCheck.isSuccess()) {
                        drgResult.setDRG(output.getDRG());
                        drgResult.setDRGName(drgCheck.getMessage());
                    } else {
                        DRGWSResult vPccl = new ValidatePCCL().ValidatePCCL(ds, drgResult.getDC(), output.getDRG());
                        drgResult.setDRG(drgResult.getDC() + (vPccl.isSuccess() ? vPccl.getResult() : "X"));
                        drgResult.setDRGName(vPccl.isSuccess() ? drgService.DRG(ds, drgResult.getDC(), drgResult.getDRG()).getMessage() : "Grouper Error");
                    }
                }
            }
        } else {
            DRGWSResult res = drgService.DRG(ds, drgResult.getDC(), drgResult.getDRG());
            drgResult.setDRGName(res.isSuccess() ? res.getMessage() : "Grouper Error");
        }
    }

}
