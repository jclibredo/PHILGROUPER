/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;
import lombok.Data;


/**
 *
 * @author MINOSUN
 */
@Data
public class DRGWSResult {
    private boolean success;
    private String message;
    private String result;
}
