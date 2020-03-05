package adserver.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequestScope
public class AdRequest {
        private ConfigFile configFile;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private String requestId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
        private Logger log = LoggerFactory.getLogger(AdRequest.class);


        public AdRequest(HttpServletRequest request, HttpServletResponse response,  @Value("${adserver.config.file}") String adserverConfigFile){
            this.request=request;
            this.response=response;
            configFile = new ConfigFile(adserverConfigFile);
        }



        public void logInfoAboutRequest(){
            addRequestIdToLogFile();
            log.info("URI is: "+request.getRequestURL()+"?"+request.getQueryString() +" requestId is: " + requestId);
            Collections.list(request.getHeaderNames()).forEach(header->log.info("RequestHeader: " + header+": " + request.getHeader(header)));
            log.info("Request client's IP: " + request.getRemoteAddr());
        }

        private void addRequestIdToLogFile() {
            MDC.put("requestId", requestId);
         }

        public void createResponse()  {
            setHeadersToResponse();
            setResponseBody();
        }

        private void setHeadersToResponse(){
            if (request.getHeader("Cookie")==null) setCookiesToResponse();
            if(request.getHeader("Origin") != null) {
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
        }

        private void setCookiesToResponse(){
            ArrayList<String> cookies = configFile.getKeysOrValuesFromConfig("Cookie", "value");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, dd-MMM-yyy, hh:mm:ss", Locale.ENGLISH);
            String  expiresDate = LocalDateTime.now().plusMonths(1).format(dateFormatter);
            cookies.forEach(cookie->response.addHeader("Set-Cookie", cookie+requestId+"; expires="+expiresDate+" GMT; path=/; domain="+configFile.getProperty("cookie.domain")));

        }

        private void setResponseBody() {
            try(PrintWriter responseBody = response.getWriter();) {

                String[] responseFromConfigFile = configFile.getResponseFromConfigFile(request);
                String responseCode = responseFromConfigFile[0];

                switch (responseCode) {
                    case "200":
                        if (responseCode.equals("200")) {
                            response.setStatus(200);
                            response.setContentType("text/xml");
                            String pathToVastFiles = configFile.getProperty("pathToVastFiles");
                            String responseVastFile = responseFromConfigFile[1];
                            log.info("Response file is:  " + responseVastFile);

                            Scanner in = new Scanner(new FileInputStream(new File(pathToVastFiles + responseVastFile)));
                            String responseVastFileLine;

                            while (in.hasNextLine()) {
                                responseVastFileLine = in.nextLine();
                                if (responseVastFileLine.contains("%session_id%")) {
                                    responseVastFileLine = responseVastFileLine.replace("%session_id%", String.valueOf(requestId));
                                }
                                responseBody.println(responseVastFileLine);
                            }
                            in.close();
                        }
                        break;
                    case "204":
                        if (responseCode.equals("204")) {
                            response.setStatus(204);
                        }
                        break;
                    case "302":
                        if (responseCode.equals("302")) {
                            response.setStatus(302);
                            response.setHeader("Location", responseFromConfigFile[2]);
                            break;
                        }
                    default:
                        response.setStatus(200);
                        response.setContentType("text/html");
                        responseBody.println("");
                }
            }
            catch (Exception exception) {exception.printStackTrace();}
        }

        public void logInfoAboutResponse(){
            log.info("Response code: " + response.getStatus());
            response.getHeaderNames().stream().distinct().forEach(header->log.info("ResponseHeader: " + header + " = " +response.getHeaders(header)));
        }

}
