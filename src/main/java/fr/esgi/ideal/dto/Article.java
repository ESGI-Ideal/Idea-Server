package fr.esgi.ideal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Article implements TmpMap {
    /**
     * ID of the article in the database
     */
    private Integer id;

    /**
     * Name of the article
     */
    private String name;
}
