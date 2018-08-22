package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Partners;

public class PartnersListMessageCodec extends AbstractListMessageCodec<Partners> {
    public PartnersListMessageCodec() {
        super(new PartnersMessageCodec());
    }
}
