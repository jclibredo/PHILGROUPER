/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import java.io.Serializable;

/**
 *
 * @author MINOSUN
 */
public class MDC implements Serializable {
    public MDC(){
    
    }
    
    private String MDC;
    private String DESCRIPTION;
    private String LABEL;

    public String getMDC() {
        return MDC;
    }

    public void setMDC(String MDC) {
        this.MDC = MDC;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }

    public String getLABEL() {
        return LABEL;
    }

    public void setLABEL(String LABEL) {
        this.LABEL = LABEL;
    }
    
    
    
    
    
    
}
