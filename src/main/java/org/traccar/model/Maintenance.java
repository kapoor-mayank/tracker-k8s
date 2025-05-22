package org.traccar.model;


public class Maintenance
        extends ExtendedModel {
    private String name;
    private String type;
    private double start;
    private double period;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public double getStart() {
        return this.start;
    }

    public void setStart(double start) {
        this.start = start;
    }


    public double getPeriod() {
        return this.period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Maintenance.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */