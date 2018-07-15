package fr.esgi.ideal.api;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.NoSuchElementException;

@UtilityClass
class RouteUtils {
    @Getter(lazy = true, value = AccessLevel.PRIVATE) private static final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    /**
     * {@see org.apache.http.HttpStatus}
     */
    @Deprecated
    public static void send(@NonNull final RoutingContext rtgCtx, final int statusCode, final Object result) {
        send(rtgCtx, HttpResponseStatus.valueOf(statusCode), result);
    }

    @SneakyThrows
    public static void send(@NonNull final RoutingContext rtgCtx, final HttpResponseStatus responseStatus, final Object result) {
        val response = rtgCtx.response().setStatusCode(responseStatus.code()).setStatusMessage(responseStatus.reasonPhrase());
        switch(rtgCtx.getAcceptableContentType()) {
            case "text/xml":
            case "application/xml":
                response.end(getXmlMapper().writeValueAsString(result));
                break;
            /*case "text/plain":
                break;*/
            //case "application/json":
            default:
                response.end(Json.encode/*Prettily*/(result));
        }
    }

    public static void send(@NonNull final RoutingContext rtgCtx, Object result) {
        send(rtgCtx, HttpResponseStatus.OK, result);
    }

    public static void error(@NonNull final RoutingContext rtgCtx, final String reason) {
        send(rtgCtx, HttpResponseStatus.INTERNAL_SERVER_ERROR, new JsonObject().put("error", new JsonObject().put("reason", reason)));
    }

    public static void error(@NonNull final RoutingContext rtgCtx, final HttpStatusException error) {
        send(rtgCtx, HttpResponseStatus.valueOf(error.getStatusCode()), error.getPayload());
    }

    public static void error(@NonNull final RoutingContext rtgCtx, final NoSuchElementException error) {
        send(rtgCtx, HttpResponseStatus.NOT_FOUND, error.getMessage());
    }
}
