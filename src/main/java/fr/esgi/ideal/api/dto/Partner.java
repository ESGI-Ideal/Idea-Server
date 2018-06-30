package fr.esgi.ideal.api.dto;

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
     * Infos on image
     */
    private Image img;

    /**
     * Description about the partner
     */
    private String description;
}
