package adserver.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.Enumeration;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AdRequestTestMethod_logInfoAboutRequest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Logger logger;
    AdRequest adRequest;

    @BeforeEach
    public void setUp(){
       ReflectionTestUtils.setField(adRequest, "log", logger);
    }

     @Test
    public void checkWhenNoRequestHeaders(){
        StringBuffer requestUrl = new StringBuffer("http://test/noHeaders");
        String queryString = "var1=someText&var2=otherText";
        Enumeration<String> requestHeaders = Collections.emptyEnumeration();
        String remoteAddr = "195.125.111.222";

        when(request.getRequestURL()).thenReturn(requestUrl);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getHeaderNames()).thenReturn(requestHeaders);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);

        adRequest.logInfoAboutRequest();
        verify(logger, never()).info(contains("RequestHeader:"));
        verify(logger, times(1)).info("Requests's client IP: " +remoteAddr);
        verify(logger, times(1)).info(contains("URI is: "+requestUrl+"?"+queryString +" requestId is: "));
    }
}
