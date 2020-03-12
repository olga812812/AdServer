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
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServer_testMethod_logInfoAboutResponse {
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    private Logger logger;

    @InjectMocks
    AdRequest adRequest;

    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(adRequest, "log", logger);
    }


    @Test
    @DisplayName("Check logging info about response code")
    public void checkResponseCode(){
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaderNames()).thenReturn(new ArrayList<String>());

        adRequest.logInfoAboutResponse();

        verify(logger, times(1)).info("Response code: 200");
    }


    @ParameterizedTest
    @DisplayName("Check logging info about response headers")
    @ValueSource(strings={"oneResponseHeader","firstResponseHeader,secondResponseHeader", "firstResponseHeader,secondResponseHeader,thirdResponseHeader"})
    public void checkResponseHeaders(String responseHeadersString){
        List<String> responseHeaders = Arrays.asList(responseHeadersString.split(","));
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaderNames()).thenReturn(responseHeaders);
        for(String header: responseHeaders) {
            when(response.getHeaders(header)).thenReturn(Arrays.asList(header+"Value"));
        }

        adRequest.logInfoAboutResponse();

        for(String header: responseHeaders) {
            verify(logger, times(1)).info("ResponseHeader: " + header + " = " +Arrays.asList(header+"Value"));
        }
    }


}
