package fr.esgi.ideal.dto;

import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
public class Partner {
    /**
     * ID of the partner in the database
     */
    private Long id;

    /**
     * Name of the partner
     */
    private String name;

    /**
     * URL of the "logo"
     */
    private URI img_url;

    /**
     * Image of the "logo"
     */
    private byte[] description;
}
