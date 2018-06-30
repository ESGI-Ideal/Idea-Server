package fr.esgi.ideal.api.dto;

import fr.pixel.dao.tables.interfaces.IAds;
import fr.pixel.dao.tables.interfaces.IArticles;
import fr.pixel.dao.tables.interfaces.IPartners;
import fr.pixel.dao.tables.interfaces.IUsers;
import fr.pixel.dao.tables.pojos.Articles;
import fr.pixel.dao.tables.pojos.Users;
import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Optional;

/**
 * Converter of beans between DB DAO and API DTO
 */
@UtilityClass
public class DbConverter {
    public static User fromApi(final IUsers db) {
        if(db != null) {
            final User user = User.builder()
                    .id(db.getId())
                    .inscription(null)
                    .isAdmin(db.getAdmin())
                    .mail(db.getMail())
                    .img(null) //TODO
                    .build();
            /*if(db.getCreated() != null)
                user.setInscription(TODO);*/
            return user;
        } else
            return null;
    }

    public static Article fromApi(final IArticles db) {
        if(db != null) {
            final Article article = Article.builder()
                    .id(db.getId())
                    .name(db.getName())
                    .created(null)
                    .updated(null)
                    .description(db.getDescription())
                    .price(null) //TODO
                    .img(null) //TODO
                    .build();
            /*if(db.getCreated() != null)
                article.setCreated(TODO);
            if(db.getCreated() != null)
                article.setCreated(TODO);*/
            return article;
        } else
            return null;
    }

    public static Partner fromApi(final IPartners db) {
        if(db != null) {
            return Partner.builder()
                    .id(db.getId())
                    .name(db.getName())
                    .description(db.getDescription())
                    .img(null) //TODO
                    .build();
        } else
            return null;
    }

    public static Ad fromApi(final IAds db) {
        if(db != null) {
            return Ad.builder()
                    .id(db.getId())
                    .description(db.getTitle())
                    .img(null) //TODO
                    .build();
        } else
            return null;
    }
}
