package adserver.api;

import adserver.domain.AdRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
public class AdServerApi {

    @GetMapping(path="/**", produces = MediaType.APPLICATION_XML_VALUE)
    public void getAllRequests(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdRequest adRequest = new AdRequest();
        adRequest.logInfoAboutRequest(request);
        adRequest.createResponse(response);
        adRequest.logInfoAboutResponse(response);
   }


}
