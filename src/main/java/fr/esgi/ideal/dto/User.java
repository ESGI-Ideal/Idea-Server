package fr.esgi.ideal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class User implements TmpMap {
    /**
     * ID of the user in the database
     */
    private Integer id;

    /**
     * Mail Adresse of the user.
     * Also used for login
     */
    private String mail;

    /**
     * Password of the user (for auth)
     */
    private String password;

    /**
     * Indicate if {@link #password} is in hashed form or in plain text
     */
    private boolean psw_hash;
}
