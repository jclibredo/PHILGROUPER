/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.structures;

import lombok.Data;

/**
 *
 * @author LAPTOP
 */
@Data
public class UserLogs {
    private String username;
    private String dataaction;
    private String module;
    private String action;
    private String details;
}
