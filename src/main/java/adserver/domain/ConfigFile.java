package adserver.domain;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

@Slf4j
class ConfigFile {
    private String defaultRespCode,defaultFile,defaultLocation;
    private String[] defaultResponse;
    private String responseUrlFromConfigFile;
    private String adserverConfigFile;

    ConfigFile(String adserverConfigFile){
        this.adserverConfigFile = adserverConfigFile;
    }

    String[] getResponseFromConfigFile(HttpServletRequest request) {
        String responseVastFileName;
        String responseCode;
        String responseLocation;
        String responseNumberFromConfigFile;


        if(!isDefaultDataInConfigFile()) throw new IllegalArgumentException("You should add DefaultRespFile, DefaultRespCode and DefaultLocation to config file");
        responseUrlFromConfigFile = getResponseUrlFromConfigFile(request);
        if(responseUrlFromConfigFile==null) return defaultResponse;

        responseNumberFromConfigFile = responseUrlFromConfigFile.substring(3);
        responseCode = getProperty("code" + responseNumberFromConfigFile);
        if(responseCode == null) responseCode = "200";
        log.info("There is resp code for this URL in config file: " + responseCode + " respNumber is " + responseNumberFromConfigFile);

        switch(responseCode) {
            case "200":
                if(responseCode.equals("200")) {
                    String[] allVastFilesInResponse =  loadPropertiesFromConfigFile().getProperty("resp" + responseNumberFromConfigFile, defaultFile).split(",");
                    responseVastFileName = allVastFilesInResponse[(int)(Math.random()*allVastFilesInResponse.length)];
                    if(responseVastFileName.equals("")) return defaultResponse;
                    responseLocation = "0";
                    return new String[]{responseCode, responseVastFileName, responseLocation};
                }
                break;
            case "204":
                if(responseCode.equals("204")) {
                    responseVastFileName = "0";
                    responseLocation = "0";
                    return new String[]{responseCode, responseVastFileName, responseLocation};
                }
                break;
            case "302":
                if(responseCode.equals("302")) {
                    responseLocation = getProperty("location" + responseNumberFromConfigFile);
                    responseVastFileName = "0";
                    if (responseLocation == null)  responseLocation = defaultLocation;
                    return new String[]{responseCode, responseVastFileName, responseLocation};
                }

        }
        responseVastFileName = "0";
        responseLocation = "0";
        return new String[]{responseCode, responseVastFileName, responseLocation};

    }

    private boolean isDefaultDataInConfigFile() {
        initDefaultValues();
        if(defaultFile == null || defaultRespCode == null || defaultLocation == null) return false;
        else return true;
    }

    private void initDefaultValues() {
        defaultFile=getProperty("DefaultRespFile");
        defaultRespCode = getProperty("DefaultRespCode");
        defaultLocation = getProperty("DefaultLocation");
        defaultResponse = new String[]{defaultRespCode, defaultFile, "0"};
    }

    private String getResponseUrlFromConfigFile(HttpServletRequest request) {
        ArrayList<String> allUrlsFromConfigFile = getKeysOrValuesFromConfig("url", "key");
        if (allUrlsFromConfigFile.size()== 0) return null;

        String requestUriAndQueryString;
        if (request.getQueryString()!=null) requestUriAndQueryString=request.getRequestURI()+request.getQueryString();
        else requestUriAndQueryString=request.getRequestURI();

        allUrlsFromConfigFile.forEach(url->{
            String[] urlValues = getProperty(url).split(",");
            for(String urlValue: urlValues) {
                if (requestUriAndQueryString.contains(urlValue)) responseUrlFromConfigFile=url;
            }
        });

        return responseUrlFromConfigFile;
    }

     ArrayList<String> getKeysOrValuesFromConfig(String key, String resultType) {
        Set<String> allProperties = loadPropertiesFromConfigFile().stringPropertyNames();
        ArrayList<String> dataFromConfigFile = new ArrayList<>();

        allProperties.stream().filter(property->property.length()>=key.length())
                .filter(property->property.substring(0, key.length()).equals(key))
                .forEach(property->{
                    if(resultType.equals("key")) dataFromConfigFile.add(property);
                    if (resultType.equals("value")) dataFromConfigFile.add(getProperty(property));});
        return dataFromConfigFile;
    }

    synchronized private Properties loadPropertiesFromConfigFile()
    {
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(new File(adserverConfigFile));
             InputStreamReader reader = new InputStreamReader(stream, "Windows-1251")) {
            properties.load(reader);
            }
        catch (Exception exception)
        {
           exception.printStackTrace();
        }
        return properties;
    }


    String getProperty(String propertyName)
    {
        return loadPropertiesFromConfigFile().getProperty(propertyName);
    }


}
