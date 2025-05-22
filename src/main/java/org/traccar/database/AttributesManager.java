package org.traccar.database;

import org.traccar.model.Attribute;
import org.traccar.model.BaseModel;


public class AttributesManager
        extends ExtendedObjectManager<Attribute> {
    public AttributesManager(DataManager dataManager) {
        super(dataManager, Attribute.class);
    }


    public void updateCachedItem(Attribute attribute) {
        Attribute cachedAttribute = getById(attribute.getId());
        cachedAttribute.setDescription(attribute.getDescription());
        cachedAttribute.setAttribute(attribute.getAttribute());
        cachedAttribute.setExpression(attribute.getExpression());
        cachedAttribute.setType(attribute.getType());
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\AttributesManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */