package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Images;

public class ImagesListMessageCodec extends AbstractListMessageCodec<Images> {
    public ImagesListMessageCodec() {
        super(new ImagesMessageCodec());
    }
}
