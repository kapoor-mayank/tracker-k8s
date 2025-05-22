package org.traccar.config;


public final class Keys {
    public static final ConfigSuffix PROTOCOL_TIMEOUT = new ConfigSuffix(".timeout", Integer.class);


    public static final ConfigSuffix PROTOCOL_PREFIX = new ConfigSuffix(".prefix", Boolean.class);


    public static final ConfigKey SERVER_TIMEOUT = new ConfigKey("server.timeout", Integer.class);


    public static final ConfigKey SERVER_STATISTICS = new ConfigKey("server.statistics", Boolean.class);


    public static final ConfigKey EVENT_ENABLE = new ConfigKey("event.enable", Boolean.class);


    public static final ConfigKey EVENT_OVERSPEED_NOT_REPEAT = new ConfigKey("event.overspeed.notRepeat", Boolean.class);


    public static final ConfigKey EVENT_OVERSPEED_MINIMAL_DURATION = new ConfigKey("event.overspeed.minimalDuration", Long.class);


    public static final ConfigKey EVENT_OVERSPEED_PREFER_LOWEST = new ConfigKey("event.overspeed.preferLowest", Boolean.class);


    public static final ConfigKey EVENT_IGNORE_DUPLICATE_ALERTS = new ConfigKey("event.ignoreDuplicateAlerts", Boolean.class);


    public static final ConfigKey EXTRA_HANDLERS = new ConfigKey("extra.handlers", String.class);


    public static final ConfigKey FORWARD_ENABLE = new ConfigKey("forward.enable", Boolean.class);


    public static final ConfigKey FORWARD_URL = new ConfigKey("forward.url", String.class);


    public static final ConfigKey FORWARD_HEADER = new ConfigKey("forward.header", String.class);


    public static final ConfigKey FORWARD_JSON = new ConfigKey("forward.json", Boolean.class);


    public static final ConfigKey FILTER_ENABLE = new ConfigKey("filter.enable", Boolean.class);


    public static final ConfigKey FILTER_INVALID = new ConfigKey("filter.invalid", Boolean.class);


    public static final ConfigKey FILTER_ZERO = new ConfigKey("filter.zero", Boolean.class);


    public static final ConfigKey FILTER_DUPLICATE = new ConfigKey("filter.duplicate", Boolean.class);


    public static final ConfigKey FILTER_FUTURE = new ConfigKey("filter.future", Long.class);


    public static final ConfigKey FILTER_ACCURACY = new ConfigKey("filter.accuracy", Integer.class);


    public static final ConfigKey FILTER_APPROXIMATE = new ConfigKey("filter.approximate", Boolean.class);


    public static final ConfigKey FILTER_STATIC = new ConfigKey("filter.static", Boolean.class);


    public static final ConfigKey FILTER_DISTANCE = new ConfigKey("filter.distance", Integer.class);


    public static final ConfigKey FILTER_MAX_SPEED = new ConfigKey("filter.maxSpeed", Integer.class);


    public static final ConfigKey FILTER_MIN_PERIOD = new ConfigKey("filter.minPeriod", Integer.class);


    public static final ConfigKey FILTER_SKIP_LIMIT = new ConfigKey("filter.skipLimit", Long.class);


    public static final ConfigKey FILTER_SKIP_ATTRIBUTES_ENABLE = new ConfigKey("filter.skipAttributes.enable", Boolean.class);


    public static final ConfigKey TIME_OVERRIDE = new ConfigKey("time.override", String.class);


    public static final ConfigKey TIME_PROTOCOLS = new ConfigKey("time.protocols", String.class);


    public static final ConfigKey COORDINATES_FILTER = new ConfigKey("coordinates.filter", Boolean.class);


    public static final ConfigKey COORDINATES_MIN_ERROR = new ConfigKey("coordinates.minError", Integer.class);


    public static final ConfigKey COORDINATES_MAX_ERROR = new ConfigKey("filter.maxError", Integer.class);


    public static final ConfigKey PROCESSING_REMOTE_ADDRESS_ENABLE = new ConfigKey("processing.remoteAddress.enable", Boolean.class);


    public static final ConfigKey PROCESSING_ENGINE_HOURS_ENABLE = new ConfigKey("processing.engineHours.enable", Boolean.class);


    public static final ConfigKey PROCESSING_COPY_ATTRIBUTES_ENABLE = new ConfigKey("processing.copyAttributes.enable", Boolean.class);


    public static final ConfigKey PROCESSING_COMPUTED_ATTRIBUTES_ENABLE = new ConfigKey("processing.computedAttributes.enable", Boolean.class);


    public static final ConfigKey PROCESSING_COMPUTED_ATTRIBUTES_DEVICE_ATTRIBUTES = new ConfigKey("processing.computedAttributes.deviceAttributes", Boolean.class);


    public static final ConfigKey GEOCODER_ENABLE = new ConfigKey("geocoder.enable", Boolean.class);


    public static final ConfigKey GEOCODER_TYPE = new ConfigKey("geocoder.type", String.class);


    public static final ConfigKey GEOCODER_URL = new ConfigKey("geocoder.url", String.class);


    public static final ConfigKey GEOCODER_ID = new ConfigKey("geocoder.id", String.class);


    public static final ConfigKey GEOCODER_KEY = new ConfigKey("geocoder.key", String.class);


    public static final ConfigKey GEOCODER_LANGUAGE = new ConfigKey("geocoder.language", String.class);


    public static final ConfigKey GEOCODER_FORMAT = new ConfigKey("geocoder.format", String.class);


    public static final ConfigKey GEOCODER_CACHE_SIZE = new ConfigKey("geocoder.cacheSize", Integer.class);


    public static final ConfigKey GEOCODER_IGNORE_POSITIONS = new ConfigKey("geocoder.ignorePositions", Boolean.class);


    public static final ConfigKey GEOCODER_PROCESS_INVALID_POSITIONS = new ConfigKey("geocoder.processInvalidPositions", Boolean.class);


    public static final ConfigKey GEOCODER_REUSE_DISTANCE = new ConfigKey("geocoder.reuseDistance", Integer.class);


    public static final ConfigKey GEOLOCATION_ENABLE = new ConfigKey("geolocation.enable", Boolean.class);


    public static final ConfigKey GEOLOCATION_TYPE = new ConfigKey("geolocation.type", String.class);


    public static final ConfigKey GEOLOCATION_URL = new ConfigKey("geolocation.url", String.class);


    public static final ConfigKey GEOLOCATION_KEY = new ConfigKey("geolocation.key", String.class);


    public static final ConfigKey GEOLOCATION_PROCESS_INVALID_POSITIONS = new ConfigKey("geolocation.processInvalidPositions", Boolean.class);


    public static final ConfigKey LOCATION_LATITUDE_HEMISPHERE = new ConfigKey("location.latitudeHemisphere", Boolean.class);


    public static final ConfigKey LOCATION_LONGITUDE_HEMISPHERE = new ConfigKey("location.longitudeHemisphere", Boolean.class);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\config\Keys.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */