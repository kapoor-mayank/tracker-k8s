package org.traccar.database;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.traccar.model.BaseModel;
import org.traccar.model.User;
  

public class UsersManager
        extends SimpleObjectManager<User> {
    private Map<String, User> usersTokens;

    public UsersManager(DataManager dataManager) {
        super(dataManager, User.class);
        if (this.usersTokens == null) {
            this.usersTokens = new ConcurrentHashMap<>();
        }
    }

    private void putToken(User user) {
        if (this.usersTokens == null) {
            this.usersTokens = new ConcurrentHashMap<>();
        }
        if (user.getToken() != null) {
            this.usersTokens.put(user.getToken(), user);
        }
    }


    protected void addNewItem(User user) {
        super.addNewItem(user);
        putToken(user);
    }


    protected void updateCachedItem(User user) {
        User cachedUser = getById(user.getId());
        super.updateCachedItem(user);
        putToken(user);
        if (cachedUser.getToken() != null && !cachedUser.getToken().equals(user.getToken())) {
            this.usersTokens.remove(cachedUser.getToken());
        }
    }


    protected void removeCachedItem(long userId) {
        User cachedUser = getById(userId);
        if (cachedUser != null) {
            String userToken = cachedUser.getToken();
            super.removeCachedItem(userId);
            if (userToken != null) {
                this.usersTokens.remove(userToken);
            }
        }
    }


    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getUserItems(userId));
        result.add(Long.valueOf(userId));
        return result;
    }

    public User getUserByToken(String token) {
        return this.usersTokens.get(token);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\UsersManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */