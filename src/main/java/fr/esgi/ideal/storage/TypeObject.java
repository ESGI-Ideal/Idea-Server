package fr.esgi.ideal.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeObject {
    Image("image")/*,
    ImgAd("image"),
    ImgArticle("image"),
    ImgPartner("image"),
    ImgUser("image")*/;

    private final String rootFolder;
}
