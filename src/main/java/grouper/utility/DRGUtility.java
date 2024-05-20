/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.utility;

import grouper.structures.DRGWSResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
//@ApplicationScoped
//@Singleton
@RequestScoped
public class DRGUtility {

    //   private final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    private final SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    private final Utility utility = new Utility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGUtility() {
    }

   

}
