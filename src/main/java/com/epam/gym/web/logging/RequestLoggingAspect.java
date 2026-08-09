package com.epam.gym.web.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Specific REST call logging (task note #17.2): for every controller method invocation, logs which
 * endpoint was called, the request (with any parameter/field whose name contains "password"
 * masked out — see {@link #describeValue}), and the outcome. Runs inside the request handled by
 * {@link TransactionIdFilter}, so every line already carries the shared transactionId via MDC.
 */
@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("com.epam.gym.web.RestCall");
    private static final Pattern PASSWORD_FIELD_PATTERN =
            Pattern.compile("(?i)\"(\\w*password\\w*)\"\\s*:\\s*\"[^\"]*\"");
    private static final String MASK = "***";

    private final ObjectMapper objectMapper;

    public RequestLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("within(com.epam.gym.web.controller..*)")
    public Object logRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = describeEndpoint();
        String handler = joinPoint.getSignature().toShortString();
        log.info("REST call started: endpoint={} handler={} request={}", endpoint, handler, describeArgs(joinPoint));
        try {
            Object result = joinPoint.proceed();
            log.info("REST call completed: endpoint={} handler={} response=200 OK", endpoint, handler);
            return result;
        } catch (Exception e) {
            log.warn("REST call failed: endpoint={} handler={} error={}: {}",
                    endpoint, handler, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private String describeEndpoint() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getMethod() + " " + request.getRequestURI();
    }

    private String describeArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return "{}";
        }
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < args.length; i++) {
            String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(name).append('=').append(describeValue(name, args[i]));
        }
        return sb.append('}').toString();
    }

    /** Masks by parameter name (raw password params) and, for POJOs, by any "*password*" JSON field. */
    private String describeValue(String paramName, Object value) {
        if (value == null) {
            return "null";
        }
        if (paramName.toLowerCase().contains("password")) {
            return MASK;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean
                || value.getClass().isEnum()) {
            return String.valueOf(value);
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            return PASSWORD_FIELD_PATTERN.matcher(json).replaceAll("\"$1\":\"" + MASK + "\"");
        } catch (Exception e) {
            return value.getClass().getSimpleName();
        }
    }
}
