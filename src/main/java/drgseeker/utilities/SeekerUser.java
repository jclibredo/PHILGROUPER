/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drgseeker.utilities;

import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class SeekerUser {

    public SeekerUser() {
    }
    private String userid;
    private String name;
    private String email;
    private String role;
    private String status;
    private String password;
    private String datecreated;
    private String createdby;
    private String dateupdated;
    private String updatedby;
    private String token;

}
