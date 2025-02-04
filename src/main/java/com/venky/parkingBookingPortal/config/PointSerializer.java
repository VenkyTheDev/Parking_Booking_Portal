package com.venky.parkingBookingPortal.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

public class PointSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Point> {
    @Override
    public void serialize(Point point, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", point.getX());
        gen.writeNumberField("y", point.getY());
        gen.writeEndObject();
    }
}
