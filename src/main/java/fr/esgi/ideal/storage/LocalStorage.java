package fr.esgi.ideal.storage;

import afu.org.apache.commons.lang3.tuple.Pair;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import lombok.NonNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

public class LocalStorage implements Storage {
    private Path assets;
    private Vertx vertx;

    @Override
    public void init(@NonNull final Vertx vertx, @NonNull final JsonObject configStorage, @NonNull final Future<Void> future) {
        this.vertx = vertx;
        this.assets = Paths.get(configStorage.getString("assets_dir", "./ideal-assets"));
        //
        this.vertx.fileSystem().exists(this.assets.toAbsolutePath().toString(), res -> {
            if(res.succeeded()) {
                if(!res.result())
                    this.vertx.fileSystem().mkdirs(this.assets.toAbsolutePath().toString(), res2 -> {
                       if(res2.succeeded()) {
                           if(!Files.isDirectory(this.assets))
                               future.fail(new InvalidPathException(this.assets.toString(), "The assets path is not an directory"));
                           else if(!Files.isReadable(this.assets) || !Files.isWritable(this.assets))
                               future.fail(new InvalidPathException(this.assets.toString(), "Haven't right acces to assets directory"));
                           else
                               future.complete();
                       } else
                           future.fail(res2.cause());
                    });
                else
                    future.complete();
            } else
                future.fail(res.cause());
        });
    }

    @Override
    public void close(final Future<Void> future) {
        future.complete();
    }

    public String generatePath(@NonNull final TypeObject type, @NonNull final Long id) {
        return this.assets.resolve(Paths.get(".", type.getRootFolder(), id.toString())).toAbsolutePath().toString();
    }

    @Override
    public Future<Pair<Long, ReadStream<Buffer>>> get(@NonNull final TypeObject type, @NonNull final Long id) {
        final Future<Pair<Long, ReadStream<Buffer>>> future = Future.future();
        final String path = generatePath(type, id);
        final AtomicLong size = new AtomicLong(0L);
        this.vertx.fileSystem().readFile(path, res -> {
            if(res.succeeded())
                this.vertx.fileSystem().props(path, ares -> {
                    final FileProps props = ares.result();
                    size.set(props.size());
                    //TODO req.headers().set("content-length", "" + size);
                    this.vertx.fileSystem().readFile(path, fres -> {
                        //final Pump pump = Pump.pump(fres.result(), )
                    });
                });
            else
                future.fail(res.cause());
        });
        return future;

        /*fs.props(filename, ares -> {
            ;
            System.out.println("props is " + props);
            fs.open(filename, new OpenOptions(), ares2 -> {
                AsyncFile file = ares2.result();
                Pump pump = Pump.pump(file, req);
                file.endHandler(v -> {
                    req.end();
                });
                pump.start();
            });
        });*/
    }

    @Override
    public Future<Void> delete(@NonNull final TypeObject type, @NonNull final Long id) {
        final Future<Void> future = Future.future();
        vertx.fileSystem().delete(generatePath(type, id), future.completer());
        return future;
    }

    @Override
    public Future<Void> upload(@NonNull final Long id, @NonNull final TypeObject type, @NonNull final String contentType, @NonNull final ReadStream<Buffer> asyncInput) {
        final Future<Void> future = Future.future();
        //this.vertx.fileSystem().writeFile(generatePath(type, id), asyncInput, future.completer());
        asyncInput.pause();
        this.vertx.fileSystem().open(generatePath(type, id), new OpenOptions(), res -> {
            final Pump pump = Pump.pump(asyncInput, res.result());
            asyncInput.endHandler(end -> res.result().close(future.completer()));
            pump.start();
            asyncInput.resume();
        });
        return future;
    }
}
