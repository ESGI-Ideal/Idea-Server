package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IPartners;
import fr.pixel.dao.tables.pojos.Partners;
import io.vertx.core.json.JsonObject;

public class PartnersListMessageCodec extends AbstractListMessageCodec<Partners> {
    public PartnersListMessageCodec() {
        super(new PartnersMessageCodec());
    }
}
