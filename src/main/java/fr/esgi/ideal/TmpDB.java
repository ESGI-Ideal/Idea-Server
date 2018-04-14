package fr.esgi.ideal;

import fr.esgi.ideal.dto.Article;
import fr.esgi.ideal.dto.Partner;
import fr.esgi.ideal.dto.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Temporary database for example
 */
@Data
@Setter(AccessLevel.PRIVATE)
public class TmpDB {
    private static TmpDB instance = null;

    public synchronized static TmpDB getInstance() {
        if(instance == null)
            instance = new TmpDB();
        return instance;
    }

    private TmpDB() {
        LongStream.rangeClosed(1, 20).forEach(this.ads::add);
        //this.ads.addAll()
        {
            AtomicLong counter = new AtomicLong(1);
            this.addArticle(Article.builder().id(counter.getAndIncrement()).name("LaveTout").build(), true, false);
            this.addArticle(Article.builder().id(counter.getAndIncrement()).name("LaveTout Plus").build(), true, false);
            this.addArticle(Article.builder().id(counter.getAndIncrement()).name("L'ESGI pour les nuls").build(), true, false);
            IntStream.rangeClosed(1, 20).forEach(i -> this.addArticle(Article.builder().id(counter.getAndIncrement()).name("Carte de crédit "+(i*200)+"pts").build(), true, false));
        }
        //this.partners.addAll()
        this.addUser(User.builder().id(1L).mail("user@mail.com").password("password").build(), true, false); //logged
        this.addUser(User.builder().id(2L).mail("other@mail.com").build(), true, false); //other
    }

    private final Map<Long, Article> articles = new HashMap<>();

    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Partner> partners = new HashMap<>();

    private final Set<Long> ads = new HashSet<>();

    public Optional<User> getUser(@NonNull final Long id) {
        return Optional.ofNullable(this.users.get(id));
    }

    public void addUser(@NonNull final User user, final boolean addIfNotExist, final boolean replaceIfExist) {
        this.users.compute(user.getId(), (key, oldVal) -> {
            if ((oldVal == null) && addIfNotExist || (oldVal != null) && replaceIfExist) return user;
            else return oldVal;
        });
    }

    public Optional<Article> getArticle(@NonNull final Long id) {
        return Optional.ofNullable(this.articles.get(id));
    }

    public void addArticle(@NonNull final Article article, final boolean addIfNotExist, final boolean replaceIfExist) {
        this.articles.compute(article.getId(), (key, oldVal) -> {
            if ((oldVal == null) && addIfNotExist || (oldVal != null) && replaceIfExist) return article;
            else return oldVal;
        });
    }

    public Optional<Partner> getPartner(@NonNull final Long id) {
        return Optional.ofNullable(this.partners.get(id));
    }

    public void addPartner(@NonNull final Partner partner, final boolean addIfNotExist, final boolean replaceIfExist) {
        this.partners.compute(partner.getId(), (key, oldVal) -> {
            if ((oldVal == null) && addIfNotExist || (oldVal != null) && replaceIfExist) return partner;
            else return oldVal;
        });
    }

    public boolean getAd(@NonNull final Long id) {
        return this.ads.contains(id);
    }
}
