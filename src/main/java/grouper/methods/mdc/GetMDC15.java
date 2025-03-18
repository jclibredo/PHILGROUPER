/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.Arrays;
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
public class GetMDC15 {

    public GetMDC15() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC15(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            GrouperMethod gm = new GrouperMethod();
            float AdmWTValues = 0;
            if (!grouperparameter.getAdmissionWeight().isEmpty()) {
                Float totaladmision = Float.parseFloat(grouperparameter.getAdmissionWeight());
                AdmWTValues = totaladmision * 1000;
            }
            int finalage = 0;
            int days = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            int year = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());

            if (year > 0) {
                finalage = year * 365;
            } else {
                finalage = days;
            }
            int Counter15PBX = 0;
            int Counter15BX = 0;
            int Counter15CX = 0;
            int Counter15PCX = 0;
            int Counter15PEX = 0;
            int Counter15PDX = 0;
            int MainCCPDx = 0;
            int MainCCSDx = 0;
            int AXMainCC = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procs = ProcedureList.get(x);
                //AX 99PDX Checking
                DRGWSResult Result15PBX = gm.AX(datasource, "15PBX", procs);
                if (Result15PBX.isSuccess()) {
                    Counter15PBX++;
                }
                //AX 99PDX Checking
                DRGWSResult Result15PCX = gm.AX(datasource, "15PCX", procs);
                if (Result15PCX.isSuccess()) {
                    Counter15PCX++;
                }
                //AX 15PEX
                DRGWSResult Result15PEX = gm.AX(datasource, "15PEX", procs);
                if (Result15PEX.isSuccess()) {
                    Counter15PEX++;
                }

                //AX 15PDX
                DRGWSResult Result15PDX = gm.AX(datasource, "15PDX", procs);
                if (Result15PDX.isSuccess()) {
                    Counter15PDX++;
                }

            }

            for (int y = 0; y < SecondaryList.size(); y++) {
                String SeconD = SecondaryList.get(y);
                //AX SDx Main CC
                DRGWSResult SDxMainCC = gm.AX(datasource, "15BX", SeconD.trim());
                if (SDxMainCC.isSuccess()) {
                    MainCCSDx++;
                }
                // THIS AREA IS FOR SDx15BX

                DRGWSResult Result15SDx = gm.AX(datasource, "15BX", SeconD.trim());
                if (Result15SDx.isSuccess()) {
                    Counter15BX++;
                }

                DRGWSResult Result15CX = gm.AX(datasource, "15CX", SeconD.trim());
                if (Result15CX.isSuccess()) {
                    Counter15CX++;
                }
            }
            int PDxCounter15CX = 0;
            DRGWSResult Result15PCX = gm.AX(datasource, "15CX", grouperparameter.getPdx());
            if (Result15PCX.isSuccess()) {
                PDxCounter15CX++;
            }

            DRGWSResult Result15BXSDx = gm.AX(datasource, "15BX", grouperparameter.getPdx());
            if (Result15BXSDx.isSuccess()) {
                MainCCPDx++;
            }

            if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge())) < 5
                    && grouperparameter.getDischargeType().equals("4")) {
                if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1502");
                    } else {
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("1550");
                        } else {
                            drgResult.setDC("1555");
                        }
                    }
                }

            } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge())) < 5
                    && grouperparameter.getDischargeType().equals("8")) {
                if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1502");
                    } else {
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("1550");
                        } else {
                            drgResult.setDC("1555");
                        }
                    }
                }
            } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge())) < 5
                    && grouperparameter.getDischargeType().equals("9")) {
                if (Counter15PDX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PBX > 0) {
                    drgResult.setDC("1501");
                } else if (Counter15PEX > 0) {
                    drgResult.setDC("1501");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1502");
                    } else {
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("1550");
                        } else {
                            drgResult.setDC("1555");
                        }
                    }
                }
            } else if (Counter15PEX > 0) {
                drgResult.setDRG("15129");
                drgResult.setDC("1512");
            } else if (Counter15PDX > 0) {
                drgResult.setDC("1511");
            } else if (AdmWTValues > 2499.0) {
                if (Counter15PBX > 0) {
                    drgResult.setDC("1509");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1510");
                    } else {
                        drgResult.setDC("1554");
                    }
                }
            } else if (finalage > 27 && AdmWTValues < 1.0) {
                if (Counter15PBX > 0) {
                    drgResult.setDC("1509");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1510");
                    } else {
                        drgResult.setDC("1554");
                    }
                }
            } else if (AdmWTValues > 1499.0) {
                if (Counter15PBX > 0) { // YOUR HERE FOR CHECKING AREA
                    drgResult.setDC("1507");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1508");
                    } else {
                        drgResult.setDC("1553");
                    }
                }
            } else if (AdmWTValues > 999.0) {
                if (Counter15PBX > 0) {
                    drgResult.setDC("1505");
                } else {
                    if (Counter15PCX > 0) {
                        drgResult.setDC("1506");
                    } else {
                        drgResult.setDC("1552");
                    }
                }
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
                if (Counter15PCX > 0) {
                    drgResult.setDC("1504");
                } else {
                    drgResult.setDC("1551");
                }
            }
            // FINDING FINAL DRG
            if (drgResult.getDRG() == null) {
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                    result.setSuccess(true);
                } else {
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
            }

            // FINAL RESULT IS HERE
            DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
            if (drgname.isSuccess()) {
                drgResult.setDRGName(drgname.getMessage());
            } else {
                drgResult.setDRGName("Grouper Error");
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setSuccess(true);
            result.setMessage("MDC 15 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC16.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
