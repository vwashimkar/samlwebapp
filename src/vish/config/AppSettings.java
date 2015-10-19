package vish.config;

import org.opensaml.DefaultBootstrap;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;

/**
 * Created by vwashimker on 10/14/2015.
 */
public class AppSettings {

    private String issuerUrl;

    private String consumerURL;

    private String Idp_url;

    private static AppSettings appSettings;
    private static SecureRandomIdentifierGenerator generator;

    /**
     Any use of this class assures that OpenSAML is bootstrapped.
     Also initializes an ID generator.
     */
    static
    {
        try
        {
            DefaultBootstrap.bootstrap();
            generator = new SecureRandomIdentifierGenerator();
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
    }

    private AppSettings() {}

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public String getConsumerURL() {
        return consumerURL;
    }

    public void setConsumerURL(String consumerURL) {
        this.consumerURL = consumerURL;
    }

    public String getIdp_url() {
        return Idp_url;
    }

    public void setIdp_url(String idp_url) {
        Idp_url = idp_url;
    }

    public String getRandomId() {
       if(generator != null){
           return generator.generateIdentifier();

       } else {
          throw new RuntimeException("cannot generate Random Id");
       }


    }

    public static AppSettings getAppSettings(){
        if(appSettings == null){
           appSettings = new AppSettings();
            appSettings.setIssuerUrl("http://localhost:8080/samlwebapp/");
            appSettings.setConsumerURL("http://localhost:8080/samlwebapp/") ;
            appSettings.setIdp_url("https://localhost:9443/samlsso");
        }
        return appSettings;
    }
}
