package org.traccar.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.model.BaseModel;


public class BaseObjectManager<T extends BaseModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseObjectManager.class);

    private final DataManager dataManager;

    private Map<Long, T> items;
    private Class<T> baseClass;

    protected BaseObjectManager(DataManager dataManager, Class<T> baseClass) {
        this.dataManager = dataManager;
        this.baseClass = baseClass;
        refreshItems();
    }

    protected final DataManager getDataManager() {
        return this.dataManager;
    }

    protected final Class<T> getBaseClass() {
        return this.baseClass;
    }

    public T getById(long itemId) {
        return this.items.get(Long.valueOf(itemId));
    }

    public void refreshItems() {
        if (this.dataManager != null) {
            try {
                Collection<T> databaseItems = this.dataManager.getObjects(this.baseClass);
                if (this.items == null) {
                    this.items = new ConcurrentHashMap<>(databaseItems.size());
                }
                Set<Long> databaseItemIds = new HashSet<>();
                for (BaseModel baseModel : databaseItems) {
                    databaseItemIds.add(Long.valueOf(baseModel.getId()));
                    if (this.items.containsKey(Long.valueOf(baseModel.getId()))) {
                        updateCachedItem((T) baseModel);
                        continue;
                    }
                    addNewItem((T) baseModel);
                }

                for (Long cachedItemId : this.items.keySet()) {
                    if (!databaseItemIds.contains(cachedItemId)) {
                        removeCachedItem(cachedItemId.longValue());
                    }
                }
            } catch (SQLException error) {
                LOGGER.warn("Error refreshing items", error);
            }
        }
    }

    protected void addNewItem(T item) {
        this.items.put(Long.valueOf(item.getId()), item);
    }

    public void addItem(T item) throws SQLException {
        this.dataManager.addObject((BaseModel) item);
        addNewItem(item);
    }

    protected void updateCachedItem(T item) {
        this.items.put(Long.valueOf(item.getId()), item);
    }

    public void updateItem(T item) throws SQLException {
        this.dataManager.updateObject((BaseModel) item);
        updateCachedItem(item);
    }

    protected void removeCachedItem(long itemId) {
        this.items.remove(Long.valueOf(itemId));
    }

    public void removeItem(long itemId) throws SQLException {
        BaseModel item = (BaseModel) getById(itemId);
        if (item != null) {
            this.dataManager.removeObject(this.baseClass, itemId);
            removeCachedItem(itemId);
        }
    }

    public final Collection<T> getItems(Set<Long> itemIds) {
        Collection<T> result = new LinkedList<>();
        for (Iterator<Long> iterator = itemIds.iterator(); iterator.hasNext(); ) {
            long itemId = ((Long) iterator.next()).longValue();
            result.add(getById(itemId));
        }

        return result;
    }

    public Set<Long> getAllItems() {
        return this.items.keySet();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\BaseObjectManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */