/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
public class CleanSDxDCDetermination {

    public CleanSDxDCDetermination() {
    }

    public String CleanSDxDCDetermination(
            final DataSource datasource,
            final String sdxoriglist,
            final String sdxdcfinder,
            final String pdx,
            final String dcs) {
        String result = "";
        try {
            LinkedList<String> SDXoriglist = new LinkedList<>();
            List<String> sdxlist = Arrays.asList(sdxoriglist.split(","));
            LinkedList<Integer> valuelist = new LinkedList<>();
            for (int i = 0; i < sdxlist.size(); i++) {
                SDXoriglist.add(sdxlist.get(i).trim());
            }

            if (sdxdcfinder != null) {
                LinkedList<String> SDxUseDC = new LinkedList<>();
                List<String> sdxfinder = Arrays.asList(sdxdcfinder.split(","));
                for (int i = 0; i < sdxfinder.size(); i++) {
                    SDxUseDC.add(sdxfinder.get(i).trim());
                }
                //===============================
                if (SDxUseDC.size() == 1) {
                    // if the size is one removal process only can be perform
                    SDXoriglist.remove(SDxUseDC.get(0));
//                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = new GetDC().GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = new CheckExclusionList().CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = new GetICD10PreMDC().GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = new GetCCLValue().GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
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

                } else if (SDxUseDC.isEmpty()) {
                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = new GetDC().GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = new CheckExclusionList().CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = new GetICD10PreMDC().GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = new GetCCLValue().GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
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
                    DRGWSResult detdccol = new GetDC().GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = new CheckExclusionList().CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = new GetICD10PreMDC().GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = new GetCCLValue().GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
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
                //GET DCCOL USING DC
                String DCCol = "";
                DRGWSResult detdccol = new GetDC().GetDC(datasource, dcs);
                if (detdccol.isSuccess()) {
                    DCCol = detdccol.getMessage();
                }
                //END OF GETTING DCCOL USING DC
                for (int x = 0; x < SDXoriglist.size(); x++) {
                    DRGWSResult exclusionlistcheck = new CheckExclusionList().CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                    if (!exclusionlistcheck.isSuccess()) {
                        //GET CCROW USING CLEAN SDX
                        DRGWSResult getccrowResult = new GetICD10PreMDC().GetICD10PreMDC(datasource, SDXoriglist.get(x));
                        //GET CCL VALUE USING SDX CCROW   
                        int cclvalueresult = new GetCCLValue().GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
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
            Logger.getLogger(CleanSDxDCDetermination.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
