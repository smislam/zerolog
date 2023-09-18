package com.example.zerolog;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            MDC.put("userId", "JOHNDOE");

            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            filterChain.doFilter(requestWrapper, responseWrapper);

            String requestBody = new String(requestWrapper.getContentAsByteArray(), 0, requestWrapper.getContentAsByteArray().length, request.getCharacterEncoding());
            String responseBody = new String(responseWrapper.getContentAsByteArray(), 0, responseWrapper.getContentAsByteArray().length, response.getCharacterEncoding());

            //Add logic for data filter

            log.info("Request [{} {}. query={{}}, payload={{}}, headers={}]", 
                request.getMethod(), request.getRequestURI(), request.getQueryString(), requestBody, getHeaders(requestWrapper));
            log.info("Response [status={}, body={}]", response.getStatus(), responseBody);

            responseWrapper.copyBodyToResponse();

        } finally {
            MDC.remove("userId");
        }
    }

     private Map<String, String> getHeaders(ContentCachingRequestWrapper requestWrapper) {
        Map<String, String> headers = new HashMap<>(5);
        Enumeration<String> names = requestWrapper.getHeaderNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, requestWrapper.getHeader(name));
        }
        return headers;
    }
    
}
