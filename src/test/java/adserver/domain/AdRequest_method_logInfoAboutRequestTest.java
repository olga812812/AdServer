package adserver.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AdRequest_method_logInfoAboutRequestTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Logger logger;
    @InjectMocks
    AdRequest adRequest;

    @BeforeEach
    public  void setUp(){
        ReflectionTestUtils.setField(adRequest, "log", logger);
    }

    @Test
    @DisplayName("Check logging about Request Headers: no request headers")
    public void noRequestHeaders(){
        mockMethods_WithDefaultValues("getRequestURL","getQueryString","getRemoteAddr");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        adRequest.logInfoAboutRequest();

        verify(logger, never()).info(contains("RequestHeader:"));
    }


     @Test
     @DisplayName("Check logging about Request URI")
     public void checkRequestUri(){
         String requestId = (String)ReflectionTestUtils.getField(adRequest, "requestId");
         mockMethods_WithDefaultValues("getRemoteAddr", "getHeaderNames");
         when(request.getRequestURL()).thenReturn(new StringBuffer("http://some/url"));
         when(request.getQueryString()).thenReturn("param1=someText&param2=otherText");

         adRequest.logInfoAboutRequest();

        verify(logger, times(1)).info("URI is: http://some/url?param1=someText&param2=otherText requestId is: "+requestId);
     }

    @Test
    @DisplayName("Check logging about Request client's IP")
    public void checkClientIp(){
        mockMethods_WithDefaultValues("getRequestURL","getQueryString","getHeaderNames");
        when(request.getRemoteAddr()).thenReturn("125.35.2.1");

        adRequest.logInfoAboutRequest();

        verify(logger, times(1)).info("Request client's IP: 125.35.2.1");
    }

    @ParameterizedTest
    @DisplayName("Check logging about Request Headers: several request headers")
    @ValueSource(strings = {"oneHeader",
            "firstHeader,secondHeader,thirdHeader"})
    public void threeRequestHeaders(String requestHeadersString){
        mockMethods_WithDefaultValues("getRequestURL","getQueryString","getRemoteAddr");
        when(request.getHeaderNames()).thenReturn(createRequestHeadersEnumeration(requestHeadersString));
        for (String header: requestHeadersString.split(",")) {
            when(request.getHeader(header)).thenReturn(header+"Value");
        }

        adRequest.logInfoAboutRequest();

        for (String header: requestHeadersString.split(",")) {
            verify(logger, times(1)).info("RequestHeader: "+header+": "+header+"Value");
        }
    }


    private void mockMethods_WithDefaultValues(String... methodNames){
        for(String methodName: methodNames){
            switch(methodName){
                case "getRequestURL":   when(request.getRequestURL()).thenReturn(new StringBuffer(""));
                                        break;
                case "getQueryString":  when(request.getQueryString()).thenReturn("");
                                        break;
                case "getRemoteAddr":   when(request.getRemoteAddr()).thenReturn("");
                                        break;
                case "getHeaderNames":  when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
                                        break;
                }
        }
    }

    private Enumeration<String> createRequestHeadersEnumeration(String requestHeadersString){
        String[] requestHeadersArray = requestHeadersString.split(",");
        ArrayList<String> allHeaders = new ArrayList<>();
        for(String header: requestHeadersArray){
            allHeaders.add(header);
        }
        return Collections.enumeration(allHeaders);
    }


}
