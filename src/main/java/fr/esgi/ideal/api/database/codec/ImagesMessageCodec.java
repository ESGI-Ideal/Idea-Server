package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Images;
import io.vertx.core.json.JsonObject;

public class ImagesMessageCodec extends AbstractMessageCodec<Images> {
    @Override
    JsonObject encodeToWire(final Images iImages) {
        return JsonObject.mapFrom(iImages);
        /*return new JsonObject()
                .put("id", iImages.getId())
                .put("hash", iImages.getHash())
                .put("filename", iImages.getFilename())
                .put("hashfile", iImages.getHashfile())
                .put("height", iImages.getHeight())
                .put("width", iImages.getWidth());*/
    }

    @Override
    Images decodeFromWire(final JsonObject json) {
        return new Images(json.getLong("id"), json.getBinary("hash"), json.getString("filename"), json.getBinary("hashfile"),
                          json.getInteger("width"), json.getInteger("height"));
    }
}
