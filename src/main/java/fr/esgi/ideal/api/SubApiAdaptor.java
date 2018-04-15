package fr.esgi.ideal.api;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class SubApiAdaptor<T> implements SubApi<T> {
    private final Map<Long, T> datas = new HashMap<>();
    private final Function<T, Long> getId;

    protected SubApiAdaptor(@NonNull final Function<T, Long> getId) {
        this.getId = getId;
    }

    protected SubApiAdaptor(@NonNull final Function<T, Long> getId, T... startingDatas) {
        this(getId);
        Arrays.stream(startingDatas).forEach(e -> this.datas.put(this.getId.apply(e), e));
    }

    @Override
    public Collection<T> getAll() {
        return this.datas.values();
    }

    @Override
    public Optional<T> get(@NonNull final Long id) {
        return Optional.ofNullable(this.datas.get(id));
    }

    /*void create();

    void update();

    void delete();

    void createOrUpdate();*/
}
