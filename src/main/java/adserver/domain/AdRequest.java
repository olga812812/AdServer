package adserver.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import sun.security.krb5.Config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class AdRequest {
        private String requestId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
        private ConfigFile configFile;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private String adserverConfigFile;

        public AdRequest(HttpServletRequest request, HttpServletResponse response, String adserverConfigFile){
            this.request=request;
            this.response=response;
            this.adserverConfigFile = adserverConfigFile;
            configFile = new ConfigFile(adserverConfigFile);
          }



        public void logInfoAboutRequest(){
            log.info("URI is: "+request.getRequestURL()+"?"+request.getQueryString() +" requestId is: " + requestId);
            Collections.list(request.getHeaderNames()).forEach(header->log.info("RequestHeader: " + header+": " + request.getHeader(header)));
            log.info("Requests's client IP: " + request.getRemoteAddr());
        }

        public void createResponse() throws IOException {
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
            ArrayList<String> cookies = configFile.getKeysOrValuesFromConfig("cookie", "value");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, dd-MMM-yyy, hh:mm:ss", Locale.ENGLISH);
            String  expiresDate = LocalDateTime.now().plusMonths(1).format(dateFormatter);
            cookies.forEach(cookie->response.addHeader("Set-Cookie", cookie+requestId+"; expires="+expiresDate+" GMT; path=/; domain="+configFile.getProperty("cookie.domain")));

        }

        private void setResponseBody() throws IOException {
            PrintWriter responseBody = response.getWriter();
            String[] responseFromConfigFile = configFile.getResponseFromConfigFile(request);
            String responseCode=responseFromConfigFile[0];

            switch(responseCode) {
                case "200":
                    if(responseCode.equals("200")) {
                        response.setStatus(200);
                        response.setContentType("text/xml");
                        String pathToVastFiles = configFile.getProperty("pathToVastFiles");
                        String responseVastFile = responseFromConfigFile[1];
                        log.info("Response file is:  " +responseVastFile);

                        Scanner in = new Scanner(new FileInputStream(new File(pathToVastFiles + responseVastFile)));
                        String responseVastFileLine;

                        while(in.hasNextLine()) {
                            responseVastFileLine = in.nextLine();
                            if(responseVastFileLine.contains("%session_id%")) {
                                responseVastFileLine = responseVastFileLine.replace("%session_id%", String.valueOf(requestId));
                            }
                            responseBody.println(responseVastFileLine);
                        }
                        in.close();
                    }
                    break;
                case "204":
                    if(responseCode.equals("204")) {
                        response.setStatus(204);
                    }
                    break;
                case "302":
                    if(responseCode.equals("302")) {
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

        public void logInfoAboutResponse(){
            log.info("Response code: " + response.getStatus());
            response.getHeaderNames().stream().distinct().forEach(header->log.info("ResponseHeader: " + header + " = " +response.getHeaders(header)));
        }

}
