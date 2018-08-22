package fr.esgi.ideal.api.dto;

import fr.esgi.ideal.dao.tables.interfaces.IAds;
import fr.esgi.ideal.dao.tables.interfaces.IArticles;
import fr.esgi.ideal.dao.tables.interfaces.IImages;
import fr.esgi.ideal.dao.tables.interfaces.IPartners;
import fr.esgi.ideal.dao.tables.interfaces.IUsers;
import fr.esgi.ideal.dao.tables.pojos.Ads;
import fr.esgi.ideal.dao.tables.pojos.Articles;
import fr.esgi.ideal.dao.tables.pojos.Images;
import fr.esgi.ideal.dao.tables.pojos.Partners;
import fr.esgi.ideal.dao.tables.pojos.Users;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Converter of beans between DB DAO and API DTO
 */
@UtilityClass
public class DbConverter {
    public static User toAPI(final IUsers db) {
        if(db != null) {
            final User user = User.builder()
                    .id(db.getId())
                    .inscription(null)
                    .isAdmin(db.getAdmin())
                    .mail(db.getMail())
                    .img(db.getImage()) //TODO
                    .build();
            /*if(db.getCreated() != null)
                user.setInscription(TODO);*/
            return user;
        } else
            return null;
    }

    public static User jsonUser(final JsonObject db) {
        if(db != null) {
            final User user = User.builder()
                    .id(db.getLong("id"))
                    .inscription(null)
                    .isAdmin(db.getBoolean("isAdmin", false))
                    .mail(db.getString("email"))
                    .img(db.getLong("img")) //TODO
                    .build();
            /*if(db.getCreated() != null)
                user.setInscription(TODO);*/
            return user;
        } else
            return null;
    }

    public static Users toDB(final User api) {
        if(api != null) {
            final Users user = new Users(api.getId(), api.getImg(), api.getMail(), convert(api.getInscription()), api.isAdmin());
            return user;
        } else
            return null;
    }

    public static Article toAPI(final IArticles db) {
        if(db != null) {
            final Article article = Article.builder()
                    .id(db.getId())
                    .name(db.getName())
                    .created(null)
                    .updated(null)
                    .description(db.getDescription())
                    .price(Optional.ofNullable(db.getPrice()).map(Double::floatValue).orElse(null))
                    .customerRating(Math.toIntExact(db.getRate()))
                    .img(db.getImage()) //TODO
                    .build();
            /*if(db.getCreated() != null)
                article.setCreated(TODO);
            if(db.getCreated() != null)
                article.setCreated(TODO);*/
            return article;
        } else
            return null;
    }

    public static Article jsonArticle(final JsonObject db) {
        if(db != null) {
            final Article article = Article.builder()
                    .id(db.getLong("id"))
                    .name(db.getString("name"))
                    .created(null)
                    .updated(null)
                    .description(db.getString("description"))
                    .price(db.getFloat("price"))
                    //.customerRating(db.getInteger("customerRating"))
                    .img(db.getLong("id")) //TODO
                    .build();
            /*if(db.getCreated() != null)
                article.setCreated(TODO);
            if(db.getCreated() != null)
                article.setCreated(TODO);*/
            return article;
        } else
            return null;
    }

    public static Articles toDB(final Article api) {
        if(api != null) {
            final Articles article = new Articles(api.getId(), api.getName(), api.getImg(), api.getDescription(), Optional.ofNullable(api.getPrice()).map(Float::doubleValue).orElse(null),
                    convert(api.getCreated()), convert(api.getUpdated()), Optional.ofNullable(api.getCustomerRating()).map(Integer::longValue).orElse(null));
            return article;
        } else
            return null;
    }

    public static Partner toAPI(final IPartners db) {
        if(db != null) {
            return Partner.builder()
                    .id(db.getId())
                    .name(db.getName())
                    .description(db.getDescription())
                    .img(db.getImage()) //TODO
                    .build();
        } else
            return null;
    }

    public static Partner jsonPartner(final JsonObject db) {
        if(db != null) {
            return Partner.builder()
                    .id(db.getLong("id"))
                    .name(db.getString("name"))
                    .description(db.getString("description"))
                    .img(db.getLong("img")) //TODO
                    .build();
        } else
            return null;
    }

    public static Partners toDB(final Partner api) {
        if(api != null) {
            final Partners partener = new Partners(api.getId(),api.getName(), api.getDescription(), api.getImg());
            return partener;
        } else
            return null;
    }

    public static Ad toAPI(final IAds db) {
        if(db != null) {
            return Ad.builder()
                    .id(db.getId())
                    .description(db.getTitle())
                    .img(db.getImage()) //TODO
                    .build();
        } else
            return null;
    }

    public static Ad jsonAds(final JsonObject db) {
        if(db != null) {
            return Ad.builder()
                    .id(db.getLong("id"))
                    .description(db.getString("description"))
                    .img(db.getLong("img")) //TODO
                    .build();
        } else
            return null;
    }

    public static Ads toDB(final Ad api) {
        if(api != null) {
            final Ads ad = new Ads(api.getId(), api.getDescription(), api.getImg());
            return ad;
        } else
            return null;
    }

    public static Image toAPI(final IImages db) {
        if(db != null) {
            return Image.builder()
                    .id(db.getId())
                    .height(db.getHeight())
                    .width(db.getWidth())
                    .filename(db.getFilename())
                    .hash(db.getHash())
                    .hashfile(db.getHashfile())
                    .build();
        } else
            return null;
    }

    public static Images toDB(final Image api) {
        if(api != null) {
            final Images ad = new Images(api.getId(), api.getHash(), api.getFilename(), api.getHashfile(), api.getWidth(), api.getHeight());
            return ad;
        } else
            return null;
    }

    private static LocalDateTime convert(final Date date) {
        if(date != null) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            //return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else
            return null;
    }
}
