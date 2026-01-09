package main.java.com.hiku.weatherService.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;

/**
 * Health check endpoints for Kubernetes probes.
 * 
 * Endpoints:
 * - /api/weather/health        - Overall health status with detailed checks
 * - /api/weather/health/live   - Liveness probe (is the service running?)
 */
@Path("/health")
@ApplicationScoped
public class HealthController {

    private MemoryMXBean checkMemory() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * Overall health check - returns detailed status of all components.
     * Used for monitoring dashboards and debugging.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        try {
            JsonArrayBuilder checksArray = Json.createArrayBuilder();
            
            // Memory check
            MemoryMXBean memoryBean = checkMemory();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (heapUsed * 100.0) / heapMax;
            
            boolean memoryHealthy = heapUsagePercent < 90;
            checksArray.add(Json.createObjectBuilder()
                    .add("name", "memory")
                    .add("status", memoryHealthy ? "UP" : "DOWN")
                    .add("heapUsed", heapUsed)
                    .add("heapMax", heapMax)
                    .add("heapUsagePercent", String.format("%.2f", heapUsagePercent))
                    .build());
            
            // Uptime check
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            checksArray.add(Json.createObjectBuilder()
                    .add("name", "uptime")
                    .add("status", "UP")
                    .add("uptimeMillis", runtimeBean.getUptime())
                    .build());
            
            boolean allHealthy = memoryHealthy;
            
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("service", "weather-service")
                    .add("status", allHealthy ? "UP" : "DOWN")
                    .add("timestamp", Instant.now().toString())
                    .add("checks", checksArray.build());
            
            return Response.status(allHealthy ? 200 : 503)
                    .entity(response.build())
                    .build();
        } catch (Exception e) {
            System.err.println("[HealthController] health check error: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(503)
                    .entity(Json.createObjectBuilder()
                            .add("service", "weather-service")
                            .add("status", "DOWN")
                            .add("timestamp", Instant.now().toString())
                            .add("error", e.getMessage())
                            .build())
                    .build();
        }
    }

    /**
     * Liveness probe endpoint.
     * Returns UP if the service is running (JVM is alive).
     * Kubernetes uses this to decide if the container should be restarted.
     */
    @GET
    @Path("/live")
    @Produces(MediaType.APPLICATION_JSON)
    public Response liveness() {
        return Response.ok(Json.createObjectBuilder()
                .add("service", "weather-service")
                .add("status", "UP")
                .add("timestamp", Instant.now().toString())
                .build())
                .build();
    }
}
