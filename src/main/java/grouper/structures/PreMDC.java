/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import javax.enterprise.context.RequestScoped;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class PreMDC {
    public PreMDC(){
    }
  
    private String v_drg;
    private String v_dx_mdc;
    private String v_result;
    private String v_dc ;

    public String getV_drg() {
        return v_drg;
    }

    public void setV_drg(String v_drg) {
        this.v_drg = v_drg;
    }

    public String getV_dx_mdc() {
        return v_dx_mdc;
    }

    public void setV_dx_mdc(String v_dx_mdc) {
        this.v_dx_mdc = v_dx_mdc;
    }

    public String getV_result() {
        return v_result;
    }

    public void setV_result(String v_result) {
        this.v_result = v_result;
    }

    public String getV_dc() {
        return v_dc;
    }

    public void setV_dc(String v_dc) {
        this.v_dc = v_dc;
    }
    
    
}
