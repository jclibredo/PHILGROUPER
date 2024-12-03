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
public class WarningError {

    public WarningError() {
    }
    private String series;
    private String errorcode;
    private String data;
    private String description;
    private String lhio;
    private String resultid;

}
