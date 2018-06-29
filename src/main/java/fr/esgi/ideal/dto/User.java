package fr.esgi.ideal.dto;

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

import java.util.Date;

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
     * Password of the user (for auth)
     */
    private String password;

    /**
     * Indicate if {@link #password} is in hashed form or in plain text
     */
    private boolean psw_hash;

    /**
     * user's date inscription
     */
    private Date inscription;

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
