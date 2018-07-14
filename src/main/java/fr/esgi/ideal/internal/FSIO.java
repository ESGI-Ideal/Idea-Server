package fr.esgi.ideal.internal;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Utility class for vertx's incapability to read from jar ...
 */
@Slf4j
@UtilityClass
public class FSIO {
    /**
     *
     * @param resourcePath the intern file to copy
     * @return {@link Path} of file copied
     * @throws IOException
     */
    public static Path getResourceAsExternal(@NonNull final String resourcePath) throws IOException {
        try(final InputStream in = FSIO.class.getClassLoader().getResourceAsStream(resourcePath)) {
            final Path file = newTmpFile();
            log.debug("copy resource {} in {}", resourcePath, file);
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        }
    }

    /**
     *
     * @param resourcesPath the intern files (should not start with "/" !)
     * @return the {@link Path} of folder with files
     * @throws IOException
     */
    public static Path getResourcesAsExternal(@NonNull final String... resourcesPath) throws IOException {
        final Path tmpRoot = newTmpFolder();
        for(@NonNull final String res : resourcesPath) {
            try(final InputStream in = FSIO.class.getClassLoader().getResourceAsStream(res)) {
                final Path file = tmpRoot.resolve(res);
                log.debug("copy resource {} in {}", res, file);
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return tmpRoot;
    }

    /**
     *
     * @param resourcesPath files to concat
     * @return files concatenated
     * @throws IOException
     */
    public static Path getResourcesAsExternalMerged(@NonNull final String... resourcesPath) throws IOException {
        try(final InputStream in = new SequenceInputStream(Collections.enumeration(Arrays.stream(resourcesPath).sequential().map(FSIO.class.getClassLoader()::getResourceAsStream).collect(Collectors.toList())))) {
            final Path file = newTmpFile();
            log.debug("copy in {} resources {}", file, Arrays.toString(resourcesPath));
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            return file;
        }
    }

    public static Path getResourcesYamlsMergedAsExternal(@NonNull final String... resourcesPath) throws IOException {
        try {
            //final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            final ObjectMapper mapper = io.swagger.v3.parser.ObjectMapperFactory.createYaml();
            final Path file = newTmpFile();
            log.debug("copy in {} resources {}", file, Arrays.toString(resourcesPath));
            mapper.writeTree(new YAMLFactory().createGenerator(file.toFile(), JsonEncoding.UTF8), Arrays.stream(resourcesPath).map(res -> {
                try (final InputStream in = FSIO.class.getClassLoader().getResourceAsStream(res)) {
                    return mapper.readTree(in);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }).reduce((prev, next) -> {merge((ObjectNode)prev, (ObjectNode)next); return prev;}).orElseThrow(NoSuchElementException::new));
            //new JsonObject((Map)Json.mapper.convertValue(new User(), Map.class));
            return file;
        } finally{}
    }
    //from https://github.com/addthis/codec/blob/master/src/main/java/com/addthis/codec/jackson/Jackson.java
    private static void merge(final ObjectNode primary, final ObjectNode backup) {
        backup.fieldNames().forEachRemaining(fieldName -> {
            final JsonNode primaryValue = primary.get(fieldName);
            if (primaryValue == null) {
                primary.set(fieldName, backup.get(fieldName).deepCopy());
            } else if(primaryValue.isObject()) {
                final JsonNode backupValue = backup.get(fieldName);
                if(backupValue.isObject())
                    merge((ObjectNode) primaryValue, backupValue.deepCopy());
            }
        });
    }

    /**
     * Create a new temporary file
     * @return
     * @throws IOException
     */
    public static Path newTmpFile() throws IOException {
        final Path file = Files.createTempFile("ideal-", null);
        log.info("New temp file : {}", file);
        //file.toFile().deleteOnExit();
        return file;
    }

    /**
     * Create a new temporary file
     * @return
     * @throws IOException
     */
    public static Path newTmpFolder() throws IOException {
        final Path folder = Files.createTempDirectory("ideal-");
        log.info("New temp folder : {}", folder);
        folder.toFile().deleteOnExit();
        return folder;
    }
}
