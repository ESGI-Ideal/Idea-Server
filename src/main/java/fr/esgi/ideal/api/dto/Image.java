package fr.esgi.ideal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * Information/metadata about an image
 */
@Data
@Builder
@AllArgsConstructor
public class Image {
    private Long id;

    private byte[] hash;

    private String filename;

    private byte[] hashfile;

    /**
     * Size of the image
     */
    @Min(0)
    private int height, width;
}
