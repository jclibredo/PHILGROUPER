/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.DRG;
import grouper.methods.validation.GetFDRG;
//import grouper.methods.validation.GetDC;
//import grouper.methods.validation.GetPCCL;
//import grouper.methods.validation.ValidatePCCL;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.FDRG;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC15 {

    public GetMDC15() {
    }
//    private final Logger logger = (Logger) LogManager.getLogger(GetMDC15.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC15(
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
            float AdmWTValues = 0;
            if (!grouperparameter.getAdmissionWeight().isEmpty()) {
                Float totaladmision = Float.parseFloat(grouperparameter.getAdmissionWeight());
                AdmWTValues = totaladmision * 1000;
            }
            int finalage = 0;
            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                finalage = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) * 365;
            } else {
                finalage = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            }
            int Counter15PBX = 0;
            int Counter15BX = 0;
            int Counter15CX = 0;
            int Counter15PCX = 0;
            int Counter15PEX = 0;
            int Counter15PDX = 0;
            int MainCCPDx = 0;
            int MainCCSDx = 0;
            int PDxCounter15CX = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                Counter15PBX += checkAX.AX(datasource, SchemaName, "15PBX", procS).isSuccess() ? 1 : 0;
                Counter15PCX += checkAX.AX(datasource, SchemaName, "15PCX", procS).isSuccess() ? 1 : 0;
                Counter15PEX += checkAX.AX(datasource, SchemaName, "15PEX", procS).isSuccess() ? 1 : 0;
                Counter15PDX += checkAX.AX(datasource, SchemaName, "15PDX", procS).isSuccess() ? 1 : 0;
            }
            for (int y = 0; y < SecondaryList.size(); y++) {
                String sdxCode = SecondaryList.get(y).trim();
                MainCCSDx += checkAX.AX(datasource, SchemaName, "15BX", sdxCode).isSuccess() ? 1 : 0;
                Counter15BX += checkAX.AX(datasource, SchemaName, "15BX", sdxCode).isSuccess() ? 1 : 0;
                Counter15CX += checkAX.AX(datasource, SchemaName, "15CX", sdxCode).isSuccess() ? 1 : 0;
            }
            PDxCounter15CX += checkAX.AX(datasource, SchemaName, "15CX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            MainCCPDx += checkAX.AX(datasource, SchemaName, "15BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge()));
            String disCharge = grouperparameter.getDischargeType();
            if (los < 5 && disCharge.equals("4")) {
                if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    drgResult.setDC(Counter15PCX > 0 ? "1502" : ("4".equals(disCharge) ? "1550" : "1555"));
                }

            } else if (los < 5 && disCharge.equals("8")) {
                if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    drgResult.setDC(Counter15PCX > 0 ? "1502" : ("4".equals(disCharge) ? "1550" : "1555"));
                }
            } else if (los < 5 && disCharge.equals("9")) {
                if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    drgResult.setDC(Counter15PCX > 0 ? "1502" : ("4".equals(grouperparameter.getDischargeType()) ? "1550" : "1555"));
                }
            } else if (Counter15PEX > 0) {
                drgResult.setDRG("15129");
                drgResult.setDC("1512");
            } else if (Counter15PDX > 0) {
                drgResult.setDC("1511");
            } else if (AdmWTValues > 2499.0) {
                drgResult.setDC(Counter15PBX > 0 ? "1509" : (Counter15PCX > 0 ? "1510" : "1554"));
            } else if (finalage > 27 && AdmWTValues < 1.0) {
                drgResult.setDC(Counter15PBX > 0 ? "1509" : (Counter15PCX > 0 ? "1510" : "1554"));
            } else if (AdmWTValues > 1499.0) {
                drgResult.setDC(Counter15PBX > 0 ? "1507" : (Counter15PCX > 0 ? "1508" : "1553"));
            } else if (AdmWTValues > 999.0) {
                drgResult.setDC(Counter15PBX > 0 ? "1505" : (Counter15PCX > 0 ? "1506" : "1552"));
            } else if (Counter15PBX > 0) {
                if (MainCCSDx > 0 && MainCCPDx > 0) {
                    drgResult.setDRG("15033");
                    drgResult.setDC("1503");
                } else if (MainCCSDx > 0 && Counter15BX > 1) {
                    drgResult.setDRG("15033");
                    drgResult.setDC("1503");
                } else {
                    drgResult.setDRG("15032");
                    drgResult.setDC("1503");
                }
            } else {
                drgResult.setDC(Counter15PCX > 0 ? "1504" : "1551");
            }
            // FINDING FINAL DRG
            DRG checkDRG = new DRG();
            GetFDRG getFdrg = new GetFDRG();
            if (drgResult.getDRG() == null) {
                if (MainCCPDx > 0 || Counter15BX > 0) {
                    if (MainCCPDx > 0 && Counter15BX > 0) {
                        drgResult.setDRG(drgResult.getDC() + "3");
                    } else if (Counter15BX > 1) {
                        drgResult.setDRG(drgResult.getDC() + "3");
                    } else {
                        drgResult.setDRG(drgResult.getDC() + "2");
                    }
                } else {
                    if (Counter15CX > 0) {
                        drgResult.setDRG(drgResult.getDC() + "1");
                    } else if (PDxCounter15CX > 0) {
                        drgResult.setDRG(drgResult.getDC() + "1");
                    } else {
                        drgResult.setDRG(drgResult.getDC() + "0");
                    }
                }
            }
            DRGWSResult getFdrgResult = getFdrg.GetFDRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG());
            if (getFdrgResult.isSuccess()) {
                FDRG fdrg = utility.objectMapper().readValue(getFdrgResult.getResult(), FDRG.class);
                String finalDrg = fdrg.getFdrg() == null ? fdrg.getPdrg() : fdrg.getFdrg();
                String finalPccl = fdrg.getFpccl() == null ? fdrg.getPpccl() : fdrg.getFpccl();
                drgResult.setDRG(finalDrg);
                drgResult.setFinalpccl(finalPccl);
                drgResult.setPrepccl(fdrg.getPpccl());
                drgResult.setPredrg(fdrg.getPdrg());
                drgResult.setDC(fdrg.getDc());
                drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, fdrg.getDc(), finalDrg).getMessage());
            } else {
                drgResult.setDRG(drgResult.getDRG());
                drgResult.setFinalpccl("X");
                drgResult.setDRGName(getFdrgResult.getMessage());
            }
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setSuccess(true);
            result.setMessage("MDC 15 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        }
        return result;

    }

}
