package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.pojos.Partners;

public class PartnersListMessageCodec extends AbstractListMessageCodec<Partners> {
    public PartnersListMessageCodec() {
        super(new PartnersMessageCodec());
    }
}
