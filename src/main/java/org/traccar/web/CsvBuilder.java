package org.traccar.web;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.helper.DateUtil;


public class CsvBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvBuilder.class);

    private static final String LINE_ENDING = "\r\n";

    private static final String SEPARATOR = ";";
    private StringBuilder builder = new StringBuilder();

    private void addLineEnding() {
        this.builder.append("\r\n");
    }

    private void addSeparator() {
        this.builder.append(";");
    }

    private SortedSet<Method> getSortedMethods(Object object) {
        Method[] methodArray = object.getClass().getMethods();
        SortedSet<Method> methods = new TreeSet<>(new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                if (m1.getName().equals("getAttributes") && !m1.getName().equals(m2.getName())) {
                    return 1;
                }
                if (m2.getName().equals("getAttributes") && !m1.getName().equals(m2.getName())) {
                    return -1;
                }
                return m1.getName().compareTo(m2.getName());
            }
        });
        methods.addAll(Arrays.asList(methodArray));
        return methods;
    }


    public void addLine(Object object) {
        SortedSet<Method> methods = getSortedMethods(object);

        for (Method method : methods) {
            if (method.getName().startsWith("get") && (method.getParameterTypes()).length == 0) {
                try {
                    if (method.getReturnType().equals(boolean.class)) {
                        this.builder.append(method.invoke(object, new Object[0]));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        this.builder.append(method.invoke(object, new Object[0]));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(long.class)) {
                        this.builder.append(method.invoke(object, new Object[0]));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(double.class)) {
                        this.builder.append(method.invoke(object, new Object[0]));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(String.class)) {
                        this.builder.append((String) method.invoke(object, new Object[0]));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(Date.class)) {
                        Date value = (Date) method.invoke(object, new Object[0]);
                        this.builder.append(DateUtil.formatDate(value));
                        addSeparator();
                        continue;
                    }
                    if (method.getReturnType().equals(Map.class)) {
                        Map value = (Map) method.invoke(object, new Object[0]);
                        if (value != null) {
                            try {
                                String map = Context.getObjectMapper().writeValueAsString(value);
                                map = map.replaceAll("[\\{\\}\"]", "");
                                map = map.replaceAll(",", " ");
                                this.builder.append(map);
                                addSeparator();
                            } catch (JsonProcessingException e) {
                                LOGGER.warn("Map JSON formatting error", (Throwable) e);
                            }
                        }
                    }
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException error) {
                    LOGGER.warn("Reflection invocation error", error);
                }
            }
        }
        addLineEnding();
    }


    public void addHeaderLine(Object object) {
        SortedSet<Method> methods = getSortedMethods(object);

        for (Method method : methods) {
            if (method.getName().startsWith("get") && (method.getParameterTypes()).length == 0) {
                String name = Introspector.decapitalize(method.getName().substring(3));
                if (!name.equals("class")) {
                    this.builder.append(name);
                    addSeparator();
                }
            }
        }
        addLineEnding();
    }

    public void addArray(Collection<?> array) {
        for (Object object : array) {
            switch (object.getClass().getSimpleName().toLowerCase()) {
                case "string":
                    this.builder.append(object.toString());
                    addLineEnding();
                    continue;
                case "long":
                    this.builder.append(((Long) object).longValue());
                    addLineEnding();
                    continue;
                case "double":
                    this.builder.append(((Double) object).doubleValue());
                    addLineEnding();
                    continue;
                case "boolean":
                    this.builder.append(((Boolean) object).booleanValue());
                    addLineEnding();
                    continue;
            }
            addLine(object);
        }
    }


    public String build() {
        return this.builder.toString();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\web\CsvBuilder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */