package com.epam.gym.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Transaction-level logging (task note #17.1): every incoming request is assigned a transactionId
 * — reused from an incoming {@code X-Transaction-Id} header if the caller already has one (so it
 * can be threaded through from an upstream caller), otherwise freshly generated — which is put in
 * the SLF4J MDC for the lifetime of the request (so every log line emitted while handling it,
 * across filter/aspect/controller/service, carries the same id) and echoed back on the response so
 * the caller and any downstream services can correlate on it too.
 */
@Component
@Order(1)
public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(TRANSACTION_ID_HEADER);
        String transactionId = (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
        MDC.put(MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
