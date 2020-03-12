package adserver.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServer_method_createResponseTest {
    @Mock
    HttpServletResponse response;
    @Mock
    HttpServletRequest request;
    @Mock
    ConfigFile configFile;
    @Mock
    private Logger logger;
    @Mock
    private PrintWriter printWriter;

    @Spy
    @InjectMocks
    AdRequest adRequest;

    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(adRequest, "configFile", configFile);
    }


    @Test
    @DisplayName("Check response code 200")
    public void checkResponseCode200() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin", "getWriter", "getProperty", "getScanner");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml","0"});

        adRequest.createResponse();

        verify(response).setStatus(200);
    }

    @Test
    @DisplayName("Check response body when there is no requestId in vast file")
    public void checkNoRequestIdInResponseBody() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin", "getWriter");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml","0"});
        when(configFile.getProperty("pathToVastFiles")).thenReturn("pathToVastFiles/");
        doReturn(new Scanner("text in vast file")).when(adRequest).getScanner("pathToVastFiles/test.xml");

        adRequest.createResponse();

        verify(printWriter, times(1)).println("text in vast file");
    }

    @Test
    @DisplayName("Check response body when there is requestId in vast file")
    public void checkRequestIdInResponseBody() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin", "getWriter");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml",""});
        when(configFile.getProperty("pathToVastFiles")).thenReturn("pathToVastFiles/");
        doReturn(new Scanner("text %session_id% in vast file")).when(adRequest).getScanner("pathToVastFiles/test.xml");

        adRequest.createResponse();

        verify(printWriter, times(1)).println("text "+ReflectionTestUtils.getField(adRequest, "requestId")+" in vast file");
    }

    @Test
    @DisplayName("Check response header Content-Type")
    public void checkContentTypeHeader() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin", "getWriter", "getProperty", "getScanner");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml","0"});

        adRequest.createResponse();

        verify(response).setContentType("text/xml");
    }

    @Test
    @DisplayName("Check logging info about response file")
    public void checkLoggingAboutResponseFile() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin", "getWriter", "getProperty", "getScanner");
        ReflectionTestUtils.setField(adRequest, "log", logger);
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","vast.xml","0"});

        adRequest.createResponse();

        verify(logger, times(1)).info("Response file is: vast.xml");
    }

    @Test
    @DisplayName("Check response code 204")
    public void checkResponseCode204() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"204","0","0"});

        adRequest.createResponse();

        verify(response).setStatus(204);
    }

    @Test
    @DisplayName("Check response code 302")
    public void checkResponseCode302() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie", "getHeaderOrigin");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"302","0","locationUrl"});

        adRequest.createResponse();

        verify(response).setStatus(302);
        verify(response).setHeader("Location", "locationUrl");
    }

    @Test
    @DisplayName("Check cookie headers in response")
    public void checkCookiesInResponse() throws IOException {
        mockMethods_WithDefaultValues("getHeaderOrigin");
        when(request.getHeader("Cookie")).thenReturn(null);
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"204","0","0"});
        when(configFile.getKeysOrValuesFromConfig("Cookie", "value")).thenReturn(Arrays.asList(new String[] {"cookie1","cookie2"}));
        when(configFile.getProperty("cookie.domain")).thenReturn("test");
        String requestId = (String)ReflectionTestUtils.getField(adRequest, "requestId");

        adRequest.createResponse();

        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains("cookie1"+requestId));
        verify(response, times(1)).addHeader(eq("Set-Cookie"), contains("cookie2"+requestId));
    }

    @Test
    @DisplayName("Check Origin headers in response")
    public void checkOriginInResponse() throws IOException {
        mockMethods_WithDefaultValues("getHeaderCookie");
        when(request.getHeader("Origin")).thenReturn("originValue");
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"204","0","0"});

        adRequest.createResponse();

        verify(response, times(1)).setHeader("Access-Control-Allow-Origin", "originValue");
        verify(response, times(1)).setHeader("Access-Control-Allow-Credentials", "true");
    }



    private void mockMethods_WithDefaultValues(String... methodNames) throws IOException{
        for(String methodName: methodNames) {
            switch(methodName){
                case "getHeaderCookie":   when(request.getHeader("Cookie")).thenReturn("CookieValue");
                    break;
                case "getHeaderOrigin":   when(request.getHeader("Origin")).thenReturn(null);
                    break;
                case "getWriter":   when(response.getWriter()).thenReturn(printWriter);
                    break;
                case "getProperty":   when(configFile.getProperty(anyString())).thenReturn("");
                    break;
                case "getScanner":  doReturn(new Scanner("some text in vast file")).when(adRequest).getScanner(anyString());
                    break;
            }
        }
    }
}
