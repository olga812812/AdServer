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

import javax.security.auth.login.Configuration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServer_testMethod_createResponse {
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
        ReflectionTestUtils.setField(adRequest, "log", logger);
    }

    @Test
    @DisplayName("")
    public void checkResponse200() throws IOException {
        when(request.getHeader("Cookie")).thenReturn("CookieValue");
        when(request.getHeader("Origin")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml",""});
        when(configFile.getProperty("pathToVastFiles")).thenReturn("pathToVastFiles/");
        doReturn(new Scanner("text in vast file")).when(adRequest).getScanner("pathToVastFiles/test.xml");

        adRequest.createResponse();

        verify(printWriter, times(1)).println("text in vast file");

    }

    @Test
    @DisplayName("")
    public void checkRequestIdInResponse() throws IOException {
        when(request.getHeader("Cookie")).thenReturn("CookieValue");
        when(request.getHeader("Origin")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        when(configFile.getResponseFromConfigFile(request)).thenReturn(new String[] {"200","test.xml",""});
        when(configFile.getProperty("pathToVastFiles")).thenReturn("pathToVastFiles/");
        doReturn(new Scanner("text %session_id% in vast file")).when(adRequest).getScanner("pathToVastFiles/test.xml");

        adRequest.createResponse();

        verify(printWriter, times(1)).println("text "+ReflectionTestUtils.getField(adRequest, "requestId")+" in vast file");

    }
}
