package com.hiku.weatherService.controller;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.annotation.security.DeclareRoles;
import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"user", "admin"})
@ApplicationPath("/api/weather")
public class WeatherService extends Application {
    
}
