package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IArticles;
import fr.pixel.dao.tables.pojos.Articles;
import io.vertx.core.json.JsonObject;

public class ArticlesListMessageCodec extends AbstractListMessageCodec<Articles> {
    public ArticlesListMessageCodec() {
        super(new ArticlesMessageCodec());
    }
}
