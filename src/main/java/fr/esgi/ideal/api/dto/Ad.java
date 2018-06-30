package fr.esgi.ideal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Ad {
    /**
     * ID of the article in the database
     */
    private Long id;

    /**
     * Description of article
     */
    private String description;

    /**
     * Information about the image
     */
    private Image img;
}
