package com.example.zerolog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    public static final String MASK_VAL = "**************";
    @Value("${sensitives:}")
    private String[] sensitives;

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
                    request.getMethod(), request.getRequestURI(), maskParams(request.getQueryString()), maskJson(requestBody), maskJson(getHeaders(requestWrapper)));
            log.info("Response [status={}, body={}]", response.getStatus(), maskJson(responseBody));

            responseWrapper.copyBodyToResponse();

        } finally {
            MDC.remove("userId");
        }
    }

    private Map<String, String> getHeaders(ContentCachingRequestWrapper requestWrapper) {
        Map<String, String> headers = new HashMap<>(5);
        Enumeration<String> names = requestWrapper.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, requestWrapper.getHeader(name));
        }
        return headers;
    }

    private String maskParams(String params) {
        AtomicReference<String> replaceParams = new AtomicReference<>(params);
        Arrays.stream(sensitives).forEach(s -> {
            if (StringUtils.equals(s, StringUtils.substringBefore(params, "="))) {
                replaceParams.set(StringUtils.replace(params, StringUtils.substringAfter(params, "="), MASK_VAL));
            }
        });
        return replaceParams.get();
    }

    private String maskJson(Map<String, String> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return maskJson(mapper.writeValueAsString(data));
    }
    private String maskJson(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonString);
        Arrays.stream(sensitives).forEach(s -> {
            node.fields().forEachRemaining( e -> {
                // Preferring EXACT match.
                if (e.getKey().equals(s.toString())) {
                    e.setValue(new TextNode(MASK_VAL));
                }
            });
        });
        return node.toString();
    }
    
}
