package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IImages;
import fr.pixel.dao.tables.pojos.Images;
import io.vertx.core.json.JsonObject;

public class ImagesListMessageCodec extends AbstractListMessageCodec<Images> {
    public ImagesListMessageCodec() {
        super(new ImagesMessageCodec());
    }
}
