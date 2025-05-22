package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseDataHandler;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.AttributesManager;
import org.traccar.database.IdentityManager;
import org.traccar.model.Attribute;
import org.traccar.model.Device;
import org.traccar.model.Position;


@Sharable
public class ComputedAttributesHandler
        extends BaseDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputedAttributesHandler.class);

    private final IdentityManager identityManager;

    private final AttributesManager attributesManager;

    private final JexlEngine engine;

    private final boolean includeDeviceAttributes;

    public ComputedAttributesHandler(Config config, IdentityManager identityManager, AttributesManager attributesManager) {
        this.identityManager = identityManager;
        this.attributesManager = attributesManager;
        this.engine = new JexlEngine();
        this.engine.setStrict(true);
        this.engine.setFunctions(Collections.singletonMap("math", Math.class));
        this.includeDeviceAttributes = config.getBoolean(Keys.PROCESSING_COMPUTED_ATTRIBUTES_DEVICE_ATTRIBUTES);
    }

    private MapContext prepareContext(Position position) {
        MapContext result = new MapContext();
        if (this.includeDeviceAttributes) {
            Device device = this.identityManager.getById(position.getDeviceId());
            if (device != null) {
                for (Object key : device.getAttributes().keySet()) {
                    result.set((String) key, device.getAttributes().get(key));
                }
            }
        }
        Set<Method> methods = new HashSet<>(Arrays.asList(position.getClass().getMethods()));
        methods.removeAll(Arrays.asList((Object[]) Object.class.getMethods()));
        for (Method method : methods) {
            if (method.getName().startsWith("get") && (method.getParameterTypes()).length == 0) {
                String name = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);

                try {
                    if (!method.getReturnType().equals(Map.class)) {
                        result.set(name, method.invoke(position, new Object[0]));
                        continue;
                    }
                    for (Object key : ((Map) method.invoke(position, new Object[0])).keySet()) {
                        result.set((String) key, ((Map) method.invoke(position, new Object[0])).get(key));
                    }
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException error) {
                    LOGGER.warn("Attribute reflection error", error);
                }
            }
        }
        return result;
    }


    @Deprecated
    public Object computeAttribute(Attribute attribute, Position position) throws JexlException {
        return this.engine.createExpression(attribute.getExpression()).evaluate((JexlContext) prepareContext(position));
    }


    protected Position handlePosition(Position position) {
        Collection<Attribute> attributes = this.attributesManager.getItems(this.attributesManager
                .getAllDeviceItems(position.getDeviceId()));
        for (Attribute attribute : attributes) {
            if (attribute.getAttribute() != null) {
                Object result = null;
                try {
                    result = computeAttribute(attribute, position);
                } catch (JexlException error) {
                    LOGGER.warn("Attribute computation error", (Throwable) error);
                }
                if (result != null) {
                    try {
                        Number numberValue;
                        Boolean booleanValue;
                        switch (attribute.getType()) {
                            case "number":
                                numberValue = (Number) result;
                                position.getAttributes().put(attribute.getAttribute(), numberValue);
                                continue;
                            case "boolean":
                                booleanValue = (Boolean) result;
                                position.getAttributes().put(attribute.getAttribute(), booleanValue);
                                continue;
                        }
                        position.getAttributes().put(attribute.getAttribute(), result.toString());
                    } catch (ClassCastException error) {
                        LOGGER.warn("Attribute cast error", error);
                    }
                }
            }
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\ComputedAttributesHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */