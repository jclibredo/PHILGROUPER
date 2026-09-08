/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class CleanSDxDCDetermination {

    public CleanSDxDCDetermination() {
    }
//    private final Utility utility = new Utility();
    private final Logger logger = (Logger) LogManager.getLogger(CleanSDxDCDetermination.class);

    public String CleanSDxDCDetermination(
            final DataSource datasource,
            final String SchemaName,
            final String sdxoriglist,
            //            final String sdxdcfinder,
            final String pdx,
            final String dcs) {
        String result = "";
        GetICD10PreMDC getI10 = new GetICD10PreMDC();
        GetCCLValue getCCLVal = new GetCCLValue();
        CheckExclusionList getExclu = new CheckExclusionList();
        GetDC getDC = new GetDC();
        try {
            LinkedList<Integer> valuelist = new LinkedList<>();
            LinkedList<String> SDXoriglist = new LinkedList<>();
            if (sdxoriglist != null
                    && !sdxoriglist.trim().isEmpty()) {
                for (String s : sdxoriglist.split(",")) {
                    SDXoriglist.add(s.trim());
                }
            }
            if (SDXoriglist.size() > 0) {
                LinkedList<String> SDxUseDC = new LinkedList<>();
//                List<String> sdxfinder = Arrays.asList(SDXoriglist.split(","));
                for (int i = 0; i < SDXoriglist.size(); i++) {
                    SDxUseDC.add(SDXoriglist.get(i).trim());
                }
//                LinkedList<String> SDxUseDC = new LinkedList<>();
//                for (String s : sdxdcfinder.split(",")) {
//                    SDxUseDC.add(s.trim());
//                }
                //===============================
                if (SDxUseDC.size() == 1) {
                    // if the size is one removal process only can be perform
                    SDXoriglist.remove(SDxUseDC.get(0));
//                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = getDC.GetDC(datasource, SchemaName, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = getExclu.CheckExclusionList(
                                datasource,
                                SchemaName,
                                SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = getI10.GetICD10PreMDC(datasource, SchemaName, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = getCCLVal.GetCCLValue(datasource, SchemaName, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);
                        }
                    }
                    //Arrange value from highest to lowest
//                    System.out.println(valuelist);
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    //----------------------------------------------
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }

                } else if (SDxUseDC.isEmpty()) {
                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = getDC.GetDC(datasource, SchemaName, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = getExclu.CheckExclusionList(datasource, SchemaName, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = getI10.GetICD10PreMDC(datasource, SchemaName, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = getCCLVal.GetCCLValue(datasource, SchemaName, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);
                        }
                    }
                    //Arrange value from highest to lowest
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }
                    //----------------------------------------------
                } else {
                    // remove all elements inside SDxUseDC from SDXoriglist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.remove(SDxUseDC.get(i));
                    }
                    //Arrange all SDx Inside use to determinie dc
                    Collections.sort(SDxUseDC, Collections.reverseOrder());
                    //Remove Higher value
                    SDxUseDC.remove(SDxUseDC.get(0));
                    //Combine all value from tow linkedlist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.add(SDxUseDC.get(i));
                    }

                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = getDC.GetDC(datasource, SchemaName, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = getExclu.CheckExclusionList(datasource, SchemaName, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = getI10.GetICD10PreMDC(datasource, SchemaName, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = getCCLVal.GetCCLValue(datasource, SchemaName, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);

                        }
                    }
                    //Arrange value from highest to lowest
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    //----------------------------------------------
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }
                }
            } else {
//                System.out.println("YOU ARE HERE B " + utility.objectMapper().writeValueAsString(SDXoriglist));
                //GET DCCOL USING DC
                String DCCol = "";
                DRGWSResult detdccol = getDC.GetDC(datasource, SchemaName, dcs);
                if (detdccol.isSuccess()) {
                    DCCol = detdccol.getMessage();
                }
                //END OF GETTING DCCOL USING DC
                for (int x = 0; x < SDXoriglist.size(); x++) {
                    DRGWSResult exclusionlistcheck = getExclu.CheckExclusionList(
                            datasource,
                            SchemaName,
                            SDXoriglist.get(x),
                            pdx);
                    if (!exclusionlistcheck.isSuccess()) {
                        //GET CCROW USING CLEAN SDX
                        DRGWSResult getccrowResult = getI10.GetICD10PreMDC(datasource, SchemaName, SDXoriglist.get(x));
                        //GET CCL VALUE USING SDX CCROW   
                        int cclvalueresult = getCCLVal.GetCCLValue(datasource, SchemaName, DCCol, getccrowResult.getMessage());
                        valuelist.add(cclvalueresult);
                    }
                }
                //Arrange value from highest to lowest
                Collections.sort(valuelist, Collections.reverseOrder());
                //Assigning Variable area
                double B, G, H, I, PCCL, sum = 0;
                ArrayList<Double> valueresult = new ArrayList<>();
                //GETTING THE LAST DIGIT PCCL Value
                for (int a = 0; a < valuelist.size(); a++) {
                    B = -0.4 * a;
                    valueresult.add(valuelist.get(a) * Math.exp(B));
                }
                for (int i = 0; i < valueresult.size(); i++) {
                    sum += valueresult.get(i);
                }
                G = 1 + sum;
                H = Math.log(G);
                I = Math.log(3 / 0.4) / 4;
                PCCL = H / I;
                //----------------------------------------------
                if (Math.round(PCCL) > 4) {
                    result = String.valueOf(4);
                } else {
                    result = String.valueOf(Math.round(PCCL));
                }
            }

        } catch (NumberFormatException ex) {
            result = "Something went wrong";
            logger.info("Executing clean SDX DC determination Method");
            logger.error("Error in clean SDX DC determination Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
