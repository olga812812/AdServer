package adserver.api;

import adserver.domain.AdRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class AdServerApi {


    @GetMapping(path="/**", produces = MediaType.APPLICATION_XML_VALUE)
    public void getAllRequests(AdRequest adRequest) throws IOException {
        adRequest.logInfoAboutRequest();
        adRequest.createResponse();
        adRequest.logInfoAboutResponse();
   }


}
