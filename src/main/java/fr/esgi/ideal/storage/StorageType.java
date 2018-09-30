package fr.esgi.ideal.storage;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public enum StorageType {
    local(LocalStorage::new);

    @NonNull private final Supplier<Storage> newInstance;
}
