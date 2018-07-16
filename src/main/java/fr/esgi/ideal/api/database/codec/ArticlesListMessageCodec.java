package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.pojos.Articles;

public class ArticlesListMessageCodec extends AbstractListMessageCodec<Articles> {
    public ArticlesListMessageCodec() {
        super(new ArticlesMessageCodec());
    }
}
