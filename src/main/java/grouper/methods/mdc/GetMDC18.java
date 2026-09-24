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
            //CHECKING FOR TRAUMA CODES
            int mdcprocedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                }
            }
            int CounterSDxBX18 = 0;
            int CounterPDxBX18 = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                CounterSDxBX18 += checkAX.AX(datasource, SchemaName, "18BX", SecondaryList.get(a).trim()).isSuccess() ? 1 : 0;
            }
            CounterPDxBX18 += checkAX.AX(datasource, SchemaName, "18BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
//                    if (ORProcedureCounter > 0) {
                    if (mdcprocedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
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
                    drgResult.setDC(PCXCounter99 > 0 ? "1808" : "1809");
                }

//            } else if (ORProcedureCounter > 0) {
            } else if (mdcprocedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
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
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
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
            case 6: {//OR Proc Level 6
                dc = "1806";
                break;
            }
            case 5: {//OR Proc Level 5
                dc = "1805";
                break;
            }
            case 4: {//OR Proc Level 4
                dc = "1804";
                break;
            }
            case 3: {//OR Proc Level 3
                dc = "1803";
                break;
            }
            case 2: {//OR Proc Level 2
                dc = "1802";
                break;
            }
            case 1: {//OR Proc Level 1
                dc = "1801";
                break;
            }
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
        ArrayList<String> sdxfinder = new ArrayList<>();
        switch (pdc.toUpperCase()) {
            case "18A"://Septicemia
                if (age > 14) {
                    if (dischargeType.equals("4")) {
                        result.setDC("1872");
                    } else {
                        result.setDC("1850");
                    }
                } else {
                    result.setDC("1851");
                }
                break;
            case "18B"://Postop & Posttraumatic Infections
                if (age > 54) {
                    result.setDC("1852");
                } else {
                    result.setDC("1853");
                }
                break;
            case "18C"://Malaria
                if (age > 14) {
                    result.setDC("1854");
                } else {
                    result.setDC("1855");
                }
                break;
            case "18D"://CounterPDxBX18
                if (CounterSDxBX18 > 0 || CounterPDxBX18 > 0) {
                    result.setDC(age > 14 ? "1870" : "1871");

                    for (int x = 0; x < SecondaryList.size(); x++) {
                        if (checkAX.AX(datasource, SchemaName, "18BX", SecondaryList.get(x).trim()).isSuccess()) {
                            sdxfinder.add(SecondaryList.get(x));
                        }
                    }
                    if (!sdxfinder.isEmpty()) {
                        result.setSdxfinder(String.join(",", sdxfinder));
                    }

                } else {
                    if (utility.ComputeYear(bdate, admDate) > 14) {
                        result.setDC("1856");
                    } else {
                        result.setDC("1857");
                    }
                }
                break;

            case "18E"://Fever of Unknown Origin
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1858");
                } else {
                    result.setDC("1859");
                }
                break;
            case "18F"://Viral Illness Except Dengue
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1860");
                } else {
                    result.setDC("1861");
                }
                break;
            case "18G"://Fungal Diseases
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1862");
                } else {
                    result.setDC("1863");
                }
                break;
            case "18H"://Other Infectious & Parasitic Diseases
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1864");
                } else {
                    result.setDC("1865");
                }
                break;
            case "18J"://Melioidosis
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1866");
                } else {
                    result.setDC("1867");
                }
                break;
            case "18K"://Leptospirosis
                if (utility.ComputeYear(bdate, admDate) > 14) {
                    result.setDC("1868");
                } else {
                    result.setDC("1869");
                }
                break;
        }
        return result;

    }

}
