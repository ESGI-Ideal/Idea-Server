package fr.esgi.ideal;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@UtilityClass
public final class Utils {
    /**
     * convert InputStream to String
     *
     * take from https://www.mkyong.com/java/how-to-convert-inputstream-to-string-in-java/
     *  and http://www.baeldung.com/convert-input-stream-to-string
     *
     * @param is input
     * @return string of input
     */
    public static String getStringFromInputStream(@NonNull final InputStream is) throws IOException {
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }
}
