package adserver.domain;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Random;

@Slf4j
public class AdRequest {
        String requestId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));

        public void logInfoAboutRequest(HttpServletRequest request){
            log.info("URI is: "+request.getRequestURL()+"?"+request.getQueryString() +" requestId is: " + requestId);
            Enumeration<String> requestHeaders =  request.getHeaderNames();
            String header;
            while(requestHeaders.hasMoreElements()) {
                header = requestHeaders.nextElement();
                log.info("RequestHeader: " + header+": " + request.getHeader(header));
            }
            log.info("Requests's client IP: " + request.getRemoteAddr());
        }

        public void createResponse(HttpServletResponse response) throws IOException {
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(200);
            response.setContentType("text/xml");
            PrintWriter responseBody = response.getWriter();
            responseBody.println("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.0\" xsi:noNamespaceSchemaLocation=\"http://specs.adfox.ru/uploads/vast.xsd\">This is simple vast!!!</VAST>");

        }
        public void logInfoAboutResponse(HttpServletResponse response){
            Collection<String> responseHeaders = response.getHeaderNames();
            responseHeaders.forEach(header->log.info("ResponseHeader: " + header + " = " +response.getHeader(header)));
        }

}
