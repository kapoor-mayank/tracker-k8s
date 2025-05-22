package org.traccar.reports.model;


public class SummaryReport
        extends BaseReport {
    private long engineHours;

    public long getEngineHours() {
        return this.engineHours;
    }

    public void setEngineHours(long engineHours) {
        this.engineHours = engineHours;
    }

    public void addEngineHours(long engineHours) {
        this.engineHours += engineHours;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\reports\model\SummaryReport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */