package fr.esgi.ideal.api.database.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

abstract public class AbstractMessageCodec<T> implements MessageCodec<T, T> {
    abstract JsonObject encodeToWire(final T s);

    /**
     * Called by Vert.x when marshalling a message to the wire.
     *
     * @param buffer the message should be written into this buffer
     * @param s      the message that is being sent
     */
    @Override
    public void encodeToWire(final Buffer buffer, final T s) {
        this.encodeToWire(s).writeToBuffer(buffer);
    }

    abstract T decodeFromWire(final JsonObject json);

    /**
     * Called by Vert.x when a message is decoded from the wire.
     *
     * @param pos    the position in the buffer where the message should be read from.
     * @param buffer the buffer to read the message from
     * @return the read message
     */
    @Override
    public T decodeFromWire(final int pos, final Buffer buffer) {
        final JsonObject json = new JsonObject();
        json.readFromBuffer(pos, buffer);
        return this.decodeFromWire(json);
    }

    /**
     * If a message is sent <i>locally</i> across the event bus, this method is called to transform the message from
     * the sent type S to the received type R
     *
     * @param s the sent message
     * @return the transformed message
     */
    @Override
    public T transform(final T s) {
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
