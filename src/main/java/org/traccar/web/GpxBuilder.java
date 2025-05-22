package org.traccar.web;

import java.util.Collection;

import org.traccar.helper.DateUtil;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;


public class GpxBuilder {
    private StringBuilder builder = new StringBuilder();


    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Traccar\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";


    private static final String NAME = "<name>%1$s</name><trkseg>%n";


    private static final String POINT = "<trkpt lat=\"%1$f\" lon=\"%2$f\"><time>%3$s</time><geoidheight>%4$f</geoidheight><course>%5$f</course><speed>%6$f</speed></trkpt>%n";


    private static final String FOOTER = "</trkseg></trk></gpx>";


    public GpxBuilder() {
        this.builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Traccar\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n");
        this.builder.append("<trkseg>\n");
    }

    public GpxBuilder(String name) {
        this.builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Traccar\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n");
        this.builder.append(String.format("<name>%1$s</name><trkseg>%n", new Object[]{name}));
    }

    public void addPosition(Position position) {
        this.builder.append(String.format("<trkpt lat=\"%1$f\" lon=\"%2$f\"><time>%3$s</time><geoidheight>%4$f</geoidheight><course>%5$f</course><speed>%6$f</speed></trkpt>%n", new Object[]{Double.valueOf(position.getLatitude()), Double.valueOf(position.getLongitude()),
                DateUtil.formatDate(position.getFixTime()), Double.valueOf(position.getAltitude()),
                Double.valueOf(position.getCourse()), Double.valueOf(UnitsConverter.mpsFromKnots(position.getSpeed()))}));
    }

    public void addPositions(Collection<Position> positions) {
        for (Position position : positions) {
            addPosition(position);
        }
    }

    public String build() {
        this.builder.append("</trkseg></trk></gpx>");
        return this.builder.toString();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\web\GpxBuilder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */