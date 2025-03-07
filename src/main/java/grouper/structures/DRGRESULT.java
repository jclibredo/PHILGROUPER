/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import lombok.Data;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@Data
public class DRGRESULT {
    public DRGRESULT() {
    }
    private String result_id;
    private String seriesnum;
    private String lhio;
    private String tags;
    private String pdx;
    private String sdx;
    private String proc;

}
