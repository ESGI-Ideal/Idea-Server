package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IAds;
import fr.pixel.dao.tables.pojos.Ads;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdsListMessageCodec extends AbstractListMessageCodec<Ads> {
    public AdsListMessageCodec() {
        super(new AdsMessageCodec());
    }
}
