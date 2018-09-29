package fr.esgi.ideal.api;

import fr.esgi.ideal.api.dto.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Based on https://github.com/dazraf/vertx-oauth2-server
 */
@AllArgsConstructor
public class ApiAuth {
    private final EventBus eventBus;
    private final User tmp = new User(0L, "mail@mail.com", OffsetDateTime.now(), null, false);

    @Getter
    private final AuthProvider provider = new AuthProvider() {
        @Override
        public void authenticate(JsonObject authInfo, Handler<AsyncResult<io.vertx.ext.auth.User>> resultHandler) {
            /*final User usr = accounts.get(authInfo.getString("username"));
            if(usr == null)
                resultHandler.handle(Future.failedFuture("User not exist"));
            else if(authInfo.getString("password", "").equals(usr.getPassword()))
                resultHandler.handle(Future.failedFuture("incorrect password"));
            else*/
                resultHandler.handle(Future.succeededFuture(tmp));
                //TODO
        }
    };

    public void token(final RoutingContext context) {
        try {
            final String clientID = Optional.ofNullable(context.request().getParam("client_id")).orElseThrow(NullPointerException::new);
            final String clientSecret = Optional.ofNullable(context.request().getParam("client_id")).orElseThrow(NullPointerException::new);
            final String userName = Optional.ofNullable(context.request().getParam("username")).orElseThrow(NullPointerException::new);
            final String userPsw = Optional.ofNullable(context.request().getParam("password")).orElseThrow(NullPointerException::new);
            final String grantType = Optional.ofNullable(context.request().getParam("grant_type")).orElseThrow(NullPointerException::new);
            final String[] scopes = Optional.ofNullable(context.request().getParam("scope")).orElseThrow(NullPointerException::new).split("\\s+");

            final Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.info("type="+ grantType);
            logger.info("name="+ userName);
            logger.info("psw="+ userPsw);
            logger.info("id="+ clientID);
            logger.info("sct="+ clientSecret);
            logger.info("scopes='"+ Arrays.toString(scopes) +"'");

            switch(grantType) {
                case "password":
                    /*if(!this.accounts.containsKey(userName)) {
                        context.response().setStatusCode(400).setStatusMessage("Unknown user").end();
                        return;
                    }
                    final User user = this.accounts.get(userName);
                    if(!Optional.ofNullable(user.getPassword()).orElse("").equals(userPsw)) {
                        context.response().setStatusCode(400).setStatusMessage("Invalid credentials").end();
                        return;
                    }*/
                    //final String accessToken = tokenFountain.nextAccessToken();
                    JsonObject response = new JsonObject()
                            .put("access_token", Base64.getEncoder().encodeToString((userName+" "+new Date().getTime()).getBytes(StandardCharsets.UTF_8)))
                            .put("token_type", "bearer")
                            .put("expires_in", 3600)
                            //.put("refresh_token", "")
                            .put("scope", Arrays.stream(scopes).collect(Collectors.joining(" ")));
                    context.response().putHeader("Cache-Control", "no-store").putHeader("Pragma", "no-cache")
                            .putHeader("Content-Type", "application/json")
                            .end(response.encodePrettily());
                    break;
                /*case "refresh_token": //TODO ?
                    break;*/
                default:
                    //log.error
                    context.response().setStatusCode(400).setStatusMessage("unsupported_grant_type").end("grant_type " + grantType + " must be password");
                    break;
            }
        } catch(final NullPointerException n) {
            context.response().setStatusCode(400).setStatusMessage("missing argument").end();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String KEY_TOKEN_LIMIT = "token_limit";

    public void prepare_oauth(final RoutingContext routingContext) {
        final String raw = routingContext.request().getHeader("Authorization");
        if((raw != null) && raw.startsWith("Bearer ")) {
            final String tokenDecode = new String(Base64.getDecoder().decode(raw.split(" ")[1]));
            final String[] tokenRaw = tokenDecode.split(" ");
            logger.info("token: "+Arrays.toString(tokenRaw));
            //routingContext.setUser(this.accounts.get(tokenRaw[0]));
            routingContext.setUser(tmp);
            //TODO routingContext.data().put(KEY_TOKEN_LIMIT, new Date(tokenRaw[1]));
        }
        routingContext.next();
    }

    private static boolean verif_token_limit(final RoutingContext routingContext) {
        if(routingContext.user() == null)
            routingContext.fail(401);
        else {
            //final Date limit = (Date) routingContext.data().get(KEY_TOKEN_LIMIT);
            //if((limit != null) && (Instant.now().isAfter(limit.toInstant()))) {
                return true;
            //} else
            //    routingContext.response().setStatusCode(400).setStatusMessage("Token expired").end();
        }
        return false;
    }

    public void check_scope_user(final RoutingContext routingContext) {
        if(verif_token_limit(routingContext))
            routingContext.user().isAuthorized("user", res -> {
                if(res.succeeded() && res.result())
                    routingContext.next();
                else
                    routingContext.fail(404);
            });
    }

    public void check_scope_admin(final RoutingContext routingContext) {
        if(verif_token_limit(routingContext))
            routingContext.user().isAuthorized("admin", res -> {
                if(res.succeeded() && res.result())
                    routingContext.next();
                else
                    routingContext.fail(404);
            });
    }

    /*final AuthHandler handler;

    final BasicAuthHandler basic;
    final RedirectAuthHandler redirect;
    final ChainAuthHandler chain;
    final DigestAuthHandler digest;
    final JWTAuthHandler jwt;
    final OAuth2AuthHandler oauth2 = new OAuth2AuthHandler() {
        @Override
        public OAuth2AuthHandler extraParams(JsonObject extraParams) {
            return null;
        }

        @Override
        public OAuth2AuthHandler setupCallback(Route route) {
            return null;
        }

        @Override
        public AuthHandler addAuthority(String authority) {
            return null;
        }

        @Override
        public AuthHandler addAuthorities(Set<String> authorities) {
            return null;
        }

        @Override
        public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {

        }

        @Override
        public void authorize(User user, Handler<AsyncResult<Void>> handler) {

        }

        @Override
        public void handle(RoutingContext event) {

        }
    };*/
}
