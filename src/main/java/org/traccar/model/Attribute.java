package org.traccar.model;


public class Attribute
        extends BaseModel {
    private String description;
    private String attribute;
    private String expression;
    private String type;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }


    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Attribute.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */