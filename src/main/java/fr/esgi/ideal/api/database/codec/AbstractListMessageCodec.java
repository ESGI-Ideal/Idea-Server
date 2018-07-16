package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IAds;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract public class AbstractListMessageCodec<T> implements MessageCodec<List<T>, List<T>> {
    protected final AbstractMessageCodec<T> codec;

    @Override
    public void encodeToWire(final Buffer buffer, final List<T> objs) {
        final JsonArray array = new JsonArray();
        objs.stream().map(codec::encodeToWire).collect(Collectors.toList()).forEach(array::add);
        array.writeToBuffer(buffer);
    }

    /**
     * Called by Vert.x when a message is decoded from the wire.
     *
     * @param pos    the position in the buffer where the message should be read from.
     * @param buffer the buffer to read the message from
     * @return the read message
     */
    @Override
    public List<T> decodeFromWire(final int pos, final Buffer buffer) {
        final JsonArray array = new JsonArray();
        array.readFromBuffer(pos, buffer);
        return IntStream.range(0, array.size()).mapToObj(array::getJsonObject).map(codec::decodeFromWire).collect(Collectors.toList());
    }

    /**
     * If a message is sent <i>locally</i> across the event bus, this method is called to transform the message from
     * the sent type S to the received type R
     *
     * @param s the sent message
     * @return the transformed message
     */
    @Override
    public List<T> transform(final List<T> s) {
        return s;
    }

    /**
     * The codec name. Each codec must have a unique name. This is used to identify a codec when sending a message and
     * for unregistering codecs.
     *
     * @return the name
     */
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * Used to identify system codecs. Should always return -1 for a user codec.
     *
     * @return -1 for a user codec.
     */
    @Override
    public byte systemCodecID() {
        return -1;
    }
}
