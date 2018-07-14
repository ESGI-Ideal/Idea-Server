package fr.esgi.ideal.internal;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for vertx's incapability to read from jar ...
 */
@Slf4j
@UtilityClass
public class FSIO {
    public static Path getResourceAsExternal(@NonNull final String resourcePath) throws IOException {
        try(final InputStream in = FSIO.class.getClassLoader().getResourceAsStream(resourcePath)) {
            final Path file = newTmpFile();
            log.debug("copy resource {} in {}", resourcePath, file);
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        }
    }

    public static Path newTmpFile() throws IOException {
        final Path file = Files.createTempFile("ideal-", null);
        log.info("New temp file : {}", file);
        file.toFile().deleteOnExit();
        return file;
    }
}
