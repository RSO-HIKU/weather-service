package com.hiku.weatherService.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Simple JAX-RS resource that fetches the external ARSO XML and returns a small
 * JSON payload with temperature, wind and a short weather descriptor.
 */

@ApplicationScoped
@Path("/")  
public class WeatherResource {

    private static final String SOURCE_XML = "https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observationAms_KREDA-ICA_latest.xml";

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrent() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(SOURCE_XML);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (InputStream is = conn.getInputStream()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);

                XPath x = XPathFactory.newInstance().newXPath();
                String temp = x.evaluate("/data/metData/t", doc);
                String windKmh = x.evaluate("/data/metData/ff_val_kmh", doc);
                if (windKmh == null || windKmh.isEmpty()) {
                    windKmh = x.evaluate("/data/metData/ff_val", doc);
                }
                String windDir = x.evaluate("/data/metData/dd_shortText", doc);
                String weatherIcon = x.evaluate("/data/metData/nn_icon", doc);
                String weatherDesc = x.evaluate("/data/metData/nn_shortText", doc);
                // snow description and unit (e.g. height of snow cover)
                String snowValue = x.evaluate("/data/metData/snow", doc);
                String snowDesc = x.evaluate("/data/metData/snow_var_desc", doc);
                String snowUnit = x.evaluate("/data/metData/snow_var_unit", doc);

                // Build a small JSON response. Values are strings as the source may
                // contain empty elements; consumers should handle parsing.
                // Only include snow fields if the <snow> element has a value (not empty)
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{\"temp\":").append((temp == null || temp.isEmpty()) ? "null" : temp)
                           .append(",\"wind_kmh\":").append((windKmh == null || windKmh.isEmpty()) ? "null" : windKmh)
                           .append(",\"wind_dir\":\"").append(escapeJson(windDir)).append("\"")
                           .append(",\"icon\":\"").append(escapeJson(weatherIcon)).append("\"")
                           .append(",\"desc\":\"").append(escapeJson(weatherDesc)).append("\"");
                
                // Include snow fields only if <snow> element has a non-empty value
                if (snowValue != null && !snowValue.isEmpty()) {
                    jsonBuilder.append(",\"snow_var_desc\":\"").append(escapeJson(snowDesc)).append("\"")
                               .append(",\"snow_var_unit\":\"").append(escapeJson(snowUnit)).append("\"");
                }
                jsonBuilder.append("}");
                String json = jsonBuilder.toString();

                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String err = String.format("{\"error\":\"%s\"}", escapeJson(e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).type(MediaType.APPLICATION_JSON).build();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
