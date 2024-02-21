/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class PDC implements Serializable {

    public PDC() {
    }

    private String PDC;
    private String CTYPE;
    private int HIERAR;
    private String CNAME;
    private String MDC;

  

}
