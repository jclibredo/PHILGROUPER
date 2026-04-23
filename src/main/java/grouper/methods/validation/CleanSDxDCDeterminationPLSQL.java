/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class CleanSDxDCDeterminationPLSQL {

    public CleanSDxDCDeterminationPLSQL() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(CleanSDxDCDeterminationPLSQL.class);

    public String CleanSDxDCDeterminationPLSQL(final DataSource datasource, final String sdxoriglist, final String sdxdcfinder, final String pdx, final String dcs) {
        String result = "";
        try {
            LinkedList<String> SDXoriglist = new LinkedList<>();
            List<String> sdxlist = Arrays.asList(sdxoriglist.split(","));
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
                    Collections.sort(SDXoriglist, Collections.reverseOrder());
                    SDXoriglist.remove(SDxUseDC.get(0));
                    result = String.join(",", SDXoriglist);
                } else if (SDxUseDC.isEmpty()) {
                    //GET DCCOL USING DC
                    Collections.sort(SDXoriglist, Collections.reverseOrder());
                    result = String.join(",", SDXoriglist);
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
                    result = String.join(",", SDXoriglist);
                }
            } else {
                Collections.sort(SDXoriglist, Collections.reverseOrder());
                result = String.join(",", SDXoriglist);
            }

        } catch (NumberFormatException ex) {
            result = "Something went wrong";
            logger.info("Executing clean SDX DC Determination PLSQL Method");
            logger.error("Error in SDX DC Determination PLSQL Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
