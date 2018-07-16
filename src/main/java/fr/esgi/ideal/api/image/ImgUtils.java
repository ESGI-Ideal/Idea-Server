package fr.esgi.ideal.api.image;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@UtilityClass
public class ImgUtils {
    /**
     *
     * @param png src
     * @param jpg dest
     * @throws IOException
     */
    public static void toJpg(@NonNull final File png, @NonNull final File jpg) throws IOException {
        final BufferedImage image = ImageIO.read(png);
        //image.getWidth();
        //image.getHeight();
        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        result.createGraphics().drawImage(image, 0, 0, Color.white, null);
        ImageIO.write(result, "jpg", jpg);
    }

    public static void thumbnail(@NonNull final File file) throws IOException {
        Thumbnails.of(file).outputFormat("jpg")
                           .allowOverwrite(false)
                           .useExifOrientation(true)
                           .size(150, 150)
                           .keepAspectRatio(true)
                           .toFiles(Rename.SUFFIX_DOT_THUMBNAIL);
    }

    public static void resize(@NonNull final File file) throws IOException {
        ImageIO.write(Scalr.resize(ImageIO.read(file), 150), "jpg", (File) null);
    }

    //http://www.baeldung.com/sha-256-hashing-java
    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
