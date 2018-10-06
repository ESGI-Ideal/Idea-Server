package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.Image;
import fr.esgi.ideal.dao.tables.pojos.Images;
import fr.esgi.ideal.internal.FSIO;
import fr.esgi.ideal.storage.LocalStorage;
import fr.esgi.ideal.storage.Storage;
import fr.esgi.ideal.storage.TypeObject;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class ApiImage implements SubApi<Images, Image> {
    private final Vertx vertx;
    private final EventBus eventBus;
    private final Storage storage;
    //private ImageManager imgManager;
    private final Path noimg = getNoImg();
    private static Path getNoImg() {
        try {
            return FSIO.getResourceAsExternal("240px-No_image_available.png").toAbsolutePath();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Image mapTo(final Images obj) {
        return DbConverter.toAPI(obj);
    }

    /*@Override
    public Images mapFrom(final Image obj) {
        return DbConverter.toDB(obj);
    }*/

    @Override
    public Future<List<Images>> getAll() {
        final Future<List<Images>> future = Future.future();
        this.eventBus.<List<Images>>send(DatabaseVerticle.DB_IMAGE_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(asyncMsg.result().body());
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Optional<Images>> get(final Long id) {
        final Future<Optional<Images>> future = Future.future();
        this.eventBus.<Images>send(DatabaseVerticle.DB_IMAGE_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Optional.ofNullable(asyncMsg.result().body()));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Void> delete(Long id) {
        final Future<Void> future = Future.future();
        this.eventBus.<Void>send(DatabaseVerticle.DB_IMAGE_DELETE_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete();
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    public void getFile(@NonNull final RoutingContext routingContext) {
        final long id = ((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong();
        final String path = ((LocalStorage)this.storage).generatePath(TypeObject.Image, id);
        this.vertx.fileSystem().exists(path, res -> {
            if(res.succeeded())
                routingContext.response().putHeader("Content-Type", "image/jpg").sendFile(res.result() ? path : this.noimg.toString());
            else
                RouteUtils.error(routingContext, res.cause().toString());
        });
    }
    public void getThumb(@NonNull final RoutingContext routingContext){} //TODO

    public void upload(@NonNull final RoutingContext routingContext){
        final long id = ((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong();
        this.storage.upload(id, TypeObject.Image, routingContext.request().getHeader("content-type"), routingContext.request());
    }

    public void temp() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //digest.update();
        digest.digest();
        DigestUtils.getSha512Digest();
    }

    public void tmp() throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream("c:\\loging.log");

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        byte[] mdbytes = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Hex format : " + sb.toString());

        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<mdbytes.length;i++) {
            hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
        }

        System.out.println("Hex format : " + hexString.toString());
    }

    /*
     * application/x-www-form-urlencoded : data in form : key1=val1&key2=val2...
     * application/form-data, multipart/form-data : same but for binary data
     * multipart/mixed
     * text/plain
     */

    void coreupload(@NonNull final RoutingContext routingContext) {
        final HttpServerRequest req = routingContext.request().pause();
        String filename = UUID.randomUUID() + ".uploaded";
        vertx.fileSystem().open(filename, new OpenOptions(), ares -> {
            final AsyncFile file = ares.result();
            final Pump pump = Pump.pump(req, file);
            req.endHandler(v1 -> file.close(v2 -> {
                System.out.println("Uploaded to " + filename);
                req.response().end();
            }));
            pump.start();
            req.resume();
        });
    }

    void webupload(@NonNull final RoutingContext routingContext) {
        /*
        <form action="/form" method="post" enctype="multipart/form-data">
        <label for="name">Select a file:</label>
        <input type="file" name="file" />
         */
        final HttpServerResponse response = routingContext.response().putHeader("Content-Type", "text/plain").setChunked(true);
        for(final FileUpload f : routingContext.fileUploads()) {
            System.out.println("f");
            response.write("Filename: " + f.fileName());
            response.write("\n");
            response.write("Size: " + f.size());
        }
        response.end();
    }

    void coreuploadform(@NonNull final RoutingContext routingContext) {
        final HttpServerRequest req = routingContext.request().setExpectMultipart(true);
        req.uploadHandler(upload -> {
            upload.exceptionHandler(cause -> req.response().setChunked(true).end("Upload failed"));
            upload.endHandler(v -> req.response().setChunked(true).end("Successfully uploaded to " + upload.filename()));
            // FIXME - Potential security exploit! In a real system you must check this filename
            // to make sure you're not saving to a place where you don't want!
            // Or better still, just use Vert.x-Web which controls the upload area.
            upload.streamToFileSystem(upload.filename());
        });
    }

    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final int BAD_REQUEST_ERROR_CODE = 400;
    public void multipart() {
        Router router = null;
        /*
        <form action="/form" ENCTYPE="multipart/form-data" method="POST" name="wibble">
        <input type="text" name="foo"/><br>
        <input type="text" name="bar"/><br>
        <input type="text" name="quux"/><br>
        <input type="file" name="myfile"/><br>
         */
        router.post("/form").handler(BodyHandler.create().setMergeFormAttributes(true));
        router.post("/form")
                .handler(routingContext -> {
                    Set<FileUpload> fileUploadSet = routingContext.fileUploads();
                    Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
                    while(fileUploadIterator.hasNext()){
                        FileUpload fileUpload = fileUploadIterator.next();
                        Buffer uploadedFile = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName()); // To get the uploaded file do
                        try { // Uploaded File Name
                            String fileName = URLDecoder.decode(fileUpload.fileName(), "UTF-8");
                        } catch(UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        // Use the Event Bus to dispatch the file now
                        // Since Event Bus does not support POJOs by default so we need to create a MessageCodec implementation
                        // and provide methods for encode and decode the bytes
                    }
                });
    }
    private JsonObject getRequestParams(final MultiMap params) {
        return params.entries().stream().reduce(new JsonObject(), (paramMap, entry) -> paramMap.put(entry.getKey(), entry.getValue()), null);
    }

    public void send() {
        Router router = null;
        router.get("/").handler(getContext -> {
            getContext.request().response().sendFile("index.html");
        });
    }
}
