package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Ads;

public class AdsListMessageCodec extends AbstractListMessageCodec<Ads> {
    public AdsListMessageCodec() {
        super(new AdsMessageCodec());
    }
}
