package fr.esgi.ideal.api;

import fr.esgi.ideal.dto.Article;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class ApiArticle extends SubApiAdaptor<Article> {
    public ApiArticle() {
        super(Article::getId, initArticles());
    }

    private static Article[] initArticles() {
        AtomicLong counter = new AtomicLong(1);
        Set<Article> articles = new HashSet<>(23);
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout Plus").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("L'ESGI pour les nuls").build());
        IntStream.rangeClosed(1, 20).forEach(i -> articles.add(Article.builder().id(counter.getAndIncrement()).name("Carte de crédit "+(i*200)+"pts").build()));
        return articles.toArray(new Article[articles.size()]);
    }
}
