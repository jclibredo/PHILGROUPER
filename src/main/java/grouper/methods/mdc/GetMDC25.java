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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC25 {

    public GetMDC25() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC25(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            ORProcedure orProc = new ORProcedure();
            GetPDC getPdc = new GetPDC();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter25BXSDx = 0;
            int Counter25CXSDx = 0;
            int Counter25DXSDx = 0;
            int Counter25BXPDx = 0;
            int Counter25DXPDx = 0;
            int Counter25CXPDx = 0;
//            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
//                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = mdcProc.MDCProcedure(datasource, SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                Counter25BXSDx += checkAX.AX(datasource, SchemaName, "25BX", sdxCode).isSuccess() ? 1 : 0;
                Counter25CXSDx += checkAX.AX(datasource, SchemaName, "25CX", sdxCode).isSuccess() ? 1 : 0;
                Counter25DXSDx += checkAX.AX(datasource, SchemaName, "25DX", sdxCode).isSuccess() ? 1 : 0;
            }
            Counter25BXPDx += checkAX.AX(datasource, SchemaName, "25BX", grouperparameter.getPdx().trim()).isSuccess() ? 1 : 0;
            Counter25CXPDx += checkAX.AX(datasource, SchemaName, "25CX", grouperparameter.getPdx().trim()).isSuccess() ? 1 : 0;
            Counter25DXPDx += checkAX.AX(datasource, SchemaName, "25DX", grouperparameter.getPdx().trim()).isSuccess() ? 1 : 0;

            if (PDXCounter99 > 0) {//Trache-ostomy
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        drgResult.setDC(this.MDCProcedures(Collections.max(ORProcedureCounterList)));
                    } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
                        if (Counter25BXPDx == 0 && Counter25BXSDx > 0) {
                            SecondaryList.stream()
                                    .map(String::trim)
                                    .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25BX", sdxCode).isSuccess())
                                    .findFirst()
                                    .ifPresent(drgResult::setSDXFINDER);
                        }
                        drgResult.setDC("2550");
                    } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                        if (Counter25CXSDx > 0 || Counter25CXPDx == 0) {
                            SecondaryList.stream()
                                    .map(String::trim)
                                    .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25CX", sdxCode).isSuccess())
                                    .findFirst()
                                    .ifPresent(drgResult::setSDXFINDER);
                        }
                        drgResult.setDC("2551");
                    } else {
                        if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
                            drgResult.setDC(grouperparameter.getDischargeType().equals("4") ? "2554" : "2552");
                            if (Counter25DXSDx > 0 || Counter25DXPDx == 0) {
                                SecondaryList.stream()
                                        .map(String::trim)
                                        .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25DX", sdxCode).isSuccess())
                                        .findFirst()
                                        .ifPresent(drgResult::setSDXFINDER);
                            }
                        } else {//Other HIV-related Condition
                            drgResult.setDC("2553");
                        }
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "2508" : "2509");
                }
            } else if (mdcprocedureCounter > 0) {
                drgResult.setDC(this.MDCProcedures(Collections.max(ORProcedureCounterList)));
            } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
                if (Counter25BXSDx > 0 || Counter25BXPDx == 0) {
                    SecondaryList.stream()
                            .map(String::trim)
                            .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25BX", sdxCode).isSuccess())
                            .findFirst()
                            .ifPresent(drgResult::setSDXFINDER);
                }
                drgResult.setDC("2550");
            } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                if (Counter25CXSDx > 0 || Counter25CXPDx == 0) {
                    SecondaryList.stream()
                            .map(String::trim)
                            .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25CX", sdxCode).isSuccess())
                            .findFirst()
                            .ifPresent(drgResult::setSDXFINDER);
                }
                drgResult.setDC("2551");
            } else {
                if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
                    drgResult.setDC(grouperparameter.getDischargeType().equals("4") ? "2554" : "2552");
                    if (Counter25DXSDx > 0 || Counter25DXPDx == 0) {
                        SecondaryList.stream()
                                .map(String::trim)
                                .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "25DX", sdxCode).isSuccess())
                                .findFirst()
                                .ifPresent(drgResult::setSDXFINDER);
                    }
                } else {//Other HIV-related Condition
                    drgResult.setDC("2553");
                }
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 25 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (NumberFormatException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC25.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private String MDCProcedures(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
            case 6://OR Proc Level 6
                dc = "2506";
                break;
            case 5://OR Proc Level 5
                dc = "2505";
                break;
            case 4://OR Proc Level 4
                dc = "2504";
                break;
            case 3://OR Proc Level 3
                dc = "2503";
                break;
            case 2://OR Proc Level 2
                dc = "2502";
                break;
            case 1://OR Proc Level 1
                dc = "2501";
                break;
        }
        return dc;
    }

}
