package fr.esgi.ideal.api;

import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;

public class ApiAd implements SubApi<Long> {
    private final Set<Long> ads = new HashSet<>();

    public ApiAd() {
        LongStream.rangeClosed(1, 20).forEach(this.ads::add);
    }

    @Override
    public Collection<Long> getAll() {
        return this.ads;
    }

    @Override
    public Optional<Long> get(@NonNull final Long id) {
        return Optional.of(id);
    }
}
