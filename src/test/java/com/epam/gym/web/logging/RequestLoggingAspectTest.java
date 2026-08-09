package com.epam.gym.web.logging;

import com.epam.gym.web.dto.ChangeLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestLoggingAspectTest {

    private final RequestLoggingAspect aspect = new RequestLoggingAspect(new ObjectMapper());

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void logRestCall_returnsProceedResult_andMasksRawPasswordParam() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/john.doe");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AuthController.login(..)");
        when(signature.getParameterNames()).thenReturn(new String[]{"username", "password"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"john.doe", "Secret123!"});
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logRestCall(joinPoint);

        assertEquals("OK", result);
    }

    @Test
    void logRestCall_rethrowsOnFailure_whenNoRequestContextAndNoParamNames() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("TraineeController.getProfile(..)");
        when(signature.getParameterNames()).thenReturn(null);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"john.doe"});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> aspect.logRestCall(joinPoint));
    }

    @Test
    void logRestCall_masksNestedPasswordFieldsInPojoArgs() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AuthController.changePassword(..)");
        when(signature.getParameterNames()).thenReturn(new String[]{"request"});

        ChangeLoginRequest dto = new ChangeLoginRequest();
        dto.setUsername("john.doe");
        dto.setOldPassword("old-secret");
        dto.setNewPassword("new-secret");
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto});
        when(joinPoint.proceed()).thenReturn(null);

        Object result = aspect.logRestCall(joinPoint);

        assertEquals(null, result);
    }

    @Test
    void logRestCall_handlesEmptyArgsAndNullValues() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("TrainingTypeController.getTrainingTypes(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(java.util.List.of());

        Object result = aspect.logRestCall(joinPoint);

        assertEquals(java.util.List.of(), result);
    }
}
