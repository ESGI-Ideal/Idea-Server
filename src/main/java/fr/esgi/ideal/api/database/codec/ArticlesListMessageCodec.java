package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Articles;

public class ArticlesListMessageCodec extends AbstractListMessageCodec<Articles> {
    public ArticlesListMessageCodec() {
        super(new ArticlesMessageCodec());
    }
}
