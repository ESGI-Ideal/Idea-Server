package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.User;
import fr.esgi.ideal.dao.tables.pojos.Users;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/*
 * Based on https://github.com/dazraf/vertx-oauth2-server
 */
//@Slf4j
@AllArgsConstructor
public class ApiAuth {
    private final EventBus eventBus;

    public void token(final RoutingContext context) {
        try {
            final String clientID = Optional.ofNullable(context.request().getParam("client_id")).orElseThrow(NullPointerException::new);
            final String clientSecret = Optional.ofNullable(context.request().getParam("client_id")).orElseThrow(NullPointerException::new);
            final String userName = Optional.ofNullable(context.request().getParam("username")).orElseThrow(NullPointerException::new);
            final String userPsw = Optional.ofNullable(context.request().getParam("password")).orElseThrow(NullPointerException::new);
            final String grantType = Optional.ofNullable(context.request().getParam("grant_type")).orElseThrow(NullPointerException::new);
            final String[] scopes = Optional.ofNullable(context.request().getParam("scope")).orElseThrow(NullPointerException::new).split("\\s+");

            //final Logger log = LoggerFactory.getLogger(this.getClass());
            log.info("type="+ grantType);
            log.info("name="+ userName);
            log.info("psw="+ userPsw);
            log.info("id="+ clientID);
            log.info("sct="+ clientSecret);
            log.info("scopes='"+ Arrays.toString(scopes) +"'");

            switch(grantType) {
                case "password":
                    this.eventBus.<Users>send(DatabaseVerticle.DB_USER_GET_BY_MAIL, userName, asyncMsg -> {
                        if(asyncMsg.succeeded()) {
                            if(asyncMsg.result().body() == null)
                                context.response().setStatusCode(404).setStatusMessage("Unknown user").end();
                            else {
                                final User user = DbConverter.toAPI(asyncMsg.result().body());
                                if(!Optional.ofNullable(asyncMsg.result().body().getPassword()).orElse("").equals(userPsw))
                                    context.response().setStatusCode(400).setStatusMessage("Invalid credentials").end();
                                else {
                                    //final String accessToken = tokenFountain.nextAccessToken();
                                    JsonObject response = new JsonObject()
                                            .put("access_token", Base64.getEncoder().encodeToString(String.join(" ", user.getMail(), user.getId().toString(), ""+new Date().getTime()).getBytes(StandardCharsets.UTF_8)))
                                            .put("token_type", "bearer")
                                            .put("expires_in", 3600)
                                            //.put("refresh_token", "")
                                            .put("scope", String.join(" ", scopes));
                                    context.response().putHeader("Cache-Control", "no-store").putHeader("Pragma", "no-cache")
                                            .putHeader("Content-Type", "application/json")
                                            .end(response.encodePrettily());
                                }
                            }
                        } else {
                            log.error("Get error from bus resquest", asyncMsg.cause());
                            RouteUtils.error(context, asyncMsg.cause().getMessage());
                        }
                    });
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

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static String KEY_TOKEN_LIMIT = "token_limit";

    public void prepare_oauth(final RoutingContext routingContext) {
        final String raw = routingContext.request().getHeader("Authorization");
        if((raw != null) && raw.startsWith("Bearer ")) {
            final String tokenDecode = new String(Base64.getDecoder().decode(raw.split(" ")[1]));
            final String[] tokenRaw = tokenDecode.split(" ");
            log.info("token: "+Arrays.toString(tokenRaw));
            //routingContext.setUser(this.accounts.get(tokenRaw[0]));
            routingContext.setUser(User.builder().mail(tokenRaw[0]).id(Long.valueOf(tokenRaw[1])).build());
            //routingContext.user().principal().put("id", Long.valueOf(tokenRaw[1]));
            //routingContext.data().put(KEY_TOKEN_LIMIT, new Date(tokenRaw[2]));
        }
        routingContext.next();
    }

    private /*static*/ boolean verif_token_limit(final RoutingContext routingContext) {
        if(routingContext.user() == null && routingContext.request().headers().contains("Authorization"))
            prepare_oauth(routingContext);
        log.debug("user session = {}", routingContext.user());
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
                //if(res.succeeded() && res.result())
                //    routingContext.next();
                //else
                //    routingContext.fail(404);
            });
    }

    public void check_scope_admin(final RoutingContext routingContext) {
        if(verif_token_limit(routingContext))
            routingContext.user().isAuthorized("admin", res -> {
                //if(res.succeeded() && res.result())
                //    routingContext.next();
                //else
                //    routingContext.fail(404);
            });
    }

}
