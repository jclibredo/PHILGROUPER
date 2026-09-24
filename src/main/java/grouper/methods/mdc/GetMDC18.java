/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCCodeOptimize;
import grouper.utility.Utility;
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
public class GetMDC18 {

    public GetMDC18() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC18.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC18(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            int mdcprocedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CounterSDxBX18 = 0;
            int CounterPDxBX18 = 0;
//            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x);
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", procS.trim()).isSuccess()) {//Dx Procedure
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", procS.trim()).isSuccess()) {//Dx Procedure
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
//                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }

                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        procS.trim(),
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CounterSDxBX18 += checkAX.AX(datasource, SchemaName, "18BX", sdxCode).isSuccess() ? 1 : 0;
            }
            CounterPDxBX18 += checkAX.AX(datasource, SchemaName, "18BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
//                    if (ORProcedureCounter > 0) {
                    if (mdcprocedureCounter > 0) {
                        String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                        drgResult.setDC(dc);
                    } else {
                        MDCCodeOptimize getMPrincipal = this.principalDaignosis(
                                drgResult.getPDC(),
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate(),
                                grouperparameter.getDischargeType(),
                                CounterSDxBX18, CounterPDxBX18,
                                SecondaryList,
                                datasource,
                                SchemaName);
                        drgResult.setDC(getMPrincipal.getDC());
                        if (!getMPrincipal.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getMPrincipal.getSdxfinder());
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1808");
                    } else {
                        drgResult.setDC("1809");
                    }
                }
//            } else if (ORProcedureCounter > 0) {
            } else if (mdcprocedureCounter > 0) {
                String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                drgResult.setDC(dc);
            } else {
                MDCCodeOptimize getMPrincipal = this.principalDaignosis(
                        drgResult.getPDC(),
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate(),
                        grouperparameter.getDischargeType(),
                        CounterSDxBX18, CounterPDxBX18,
                        SecondaryList,
                        datasource,
                        SchemaName);
                drgResult.setDC(getMPrincipal.getDC());
                if (!getMPrincipal.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getMPrincipal.getSdxfinder());
                }
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 18 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC18 Method");
            logger.error("Error in MDC18 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String orProcedure(final Integer maxCounter) {
        String dc = "";
        switch (maxCounter) {
            case 6://OR Proc Level 6
                dc = "1806";
                break;
            case 5://OR Proc Level 5
                dc = "1805";
                break;
            case 4://OR Proc Level 4
                dc = "1804";
                break;
            case 3://OR Proc Level 3
                dc = "1803";
                break;
            case 2://OR Proc Level 2
                dc = "1802";
                break;
            case 1://OR Proc Level 1
                dc = "1801";
                break;
        }
        return dc;
    }

    private MDCCodeOptimize principalDaignosis(
            final String pdc,
            final String bdate,
            final String admDate,
            final String dischargeType,
            final Integer CounterSDxBX18,
            final Integer CounterPDxBX18,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        long age = utility.ComputeYear(bdate, admDate);
        switch (pdc.toUpperCase()) {
            case "18A": {//Septicemia
                result.setDC(age > 14 ? ("4".equals(dischargeType) ? "1872" : "1850") : "1851");
                break;
            }
            case "18B": {//Postop & Posttraumatic Infections
                result.setDC(age > 54 ? "1852" : "1853");
                break;
            }
            case "18C": {//Malaria
                result.setDC(age > 14 ? "1854" : "1855");
                break;
            }
            case "18D": {//CounterPDxBX18
                if (CounterSDxBX18 > 0 || CounterPDxBX18 > 0) {
                    result.setDC(age > 14 ? "1870" : "1871");
                    if (CounterPDxBX18 == 0 && CounterSDxBX18 > 0) {
                        for (String sdx : SecondaryList) {
                            if (checkAX.AX(datasource, SchemaName, "18BX", sdx.trim()).isSuccess()) {
                                result.setSdxfinder(sdx.trim());
                                break;
                            }
                        }
                    }
                } else {
                    result.setDC(age > 14 ? "1856" : "1857");
                }
                break;
            }
            case "18E": {//Fever of Unknown Origin
                result.setDC(age > 14 ? "1858" : "1859");
                break;
            }
            case "18F": {//Viral Illness Except Dengue
                result.setDC(age > 14 ? "1860" : "1861");
                break;
            }
            case "18G": {//Fungal Diseases
                result.setDC(age > 14 ? "1862" : "1863");
                break;
            }
            case "18H": {//Other Infectious & Parasitic Diseases
                result.setDC(age > 14 ? "1864" : "1865");
                break;
            }
            case "18J": {//Melioidosis
                result.setDC(age > 14 ? "1866" : "1867");
                break;
            }
            case "18K": {//Leptospirosis
                result.setDC(age > 14 ? "1868" : "1869");
                break;
            }
        }
        return result;
    }

}
