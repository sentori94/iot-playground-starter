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
        String segmentName = httpReq.getMethod() + " " + httpReq.getRequestURI();

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
            throw e;
        } finally {
            AWSXRay.endSegment();
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

