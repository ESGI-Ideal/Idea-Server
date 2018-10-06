package fr.esgi.ideal.storage;

import afu.org.apache.commons.lang3.tuple.Pair;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

public interface Storage {
    void init(final Vertx vertx, final JsonObject configStorage, final Future<Void> future);
    void close(final Future<Void> future);

    Future<Pair<Long, ReadStream<Buffer>>> get(final TypeObject type, final Long id);
    Future<Void> delete(final TypeObject type, final Long id);
    //void put();
    Future<Void> upload(final Long id, final TypeObject type, final String contentType, final ReadStream<Buffer> asyncInput);
}
