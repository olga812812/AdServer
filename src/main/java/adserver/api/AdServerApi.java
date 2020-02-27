package adserver.api;

import adserver.domain.AdRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class AdServerApi {
    @Autowired
    AdRequest adRequest;

    @GetMapping(path="/**", produces = MediaType.APPLICATION_XML_VALUE)
    public void getAllRequests() throws IOException {
        adRequest.logInfoAboutRequest();
        adRequest.createResponse();
        adRequest.logInfoAboutResponse();
   }


}
