package fr.esgi.ideal.api.dto;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class User extends AbstractUser {
    /**
     * ID of the user in the database
     */
    private Long id;

    /**
     * Mail Adresse of the user.
     * Also used for login
     */
    private String mail;

    /**
     * user's date inscription
     */
    private OffsetDateTime inscription;

    /**
     * infos for the image
     */
    //private Image img;
    private Long img;

    @Default private boolean isAdmin = false;

    @Override
    protected void doIsPermitted(final String permission, final Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(this.isAdmin || "admin".equalsIgnoreCase(permission)));
    }

    /**
     * Get the underlying principal for the User. What this actually returns depends on the implementation.
     * For a simple user/password based auth, it's likely to contain a JSON object with the following structure:
     * <pre>
     *   {
     *     "username", "tim"
     *   }
     * </pre>
     *
     * @return JSON representation of the Principal
     */
    @Override
    public JsonObject principal() {
        return new JsonObject().putNull("todo");
    }

    /**
     * Set the auth provider for the User. This is typically used to reattach a detached User with an AuthProvider, e.g.
     * after it has been deserialized.
     *
     * @param authProvider the AuthProvider - this must be the same type of AuthProvider that originally created the User
     */
    @Override
    public void setAuthProvider(final AuthProvider authProvider) {
    }
}
