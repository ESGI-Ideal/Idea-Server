package fr.esgi.ideal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class User {
    /**
     * ID of the user in the database
     */
    private Long id;

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

    /**
     * user's date inscription
     */
    private Date inscription;
}
