package com.sentori.iot.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;

public class XRayJakartaFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = httpReq.getRequestURI();
        // Exclure explicitement Actuator pour éviter les segments inutiles
        if (path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String segmentName = httpReq.getMethod() + " " + path;

        Segment segment = null;
        try {
            segment = AWSXRay.beginSegment(segmentName);
            if (segment != null) {
                segment.putHttp("request", buildRequestInfo(httpReq));
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            if (segment != null) {
                segment.addException(e);
            }
            // Ne pas propager l'erreur X-Ray, juste log
            if (!(e instanceof IOException) && !(e instanceof ServletException)) {
                System.err.println("WARN: X-Ray tracing error for " + segmentName + ": " + e.getMessage());
            } else {
                throw e;
            }
        } finally {
            try {
                AWSXRay.endSegment();
            } catch (Exception e) {
                // Ignorer les erreurs de fin de segment
            }
        }
    }

    private java.util.Map<String, Object> buildRequestInfo(HttpServletRequest request) {
        java.util.Map<String, Object> requestInfo = new java.util.HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("url", request.getRequestURL().toString());
        requestInfo.put("user_agent", request.getHeader("User-Agent"));
        requestInfo.put("client_ip", request.getRemoteAddr());
        return requestInfo;
    }
}
