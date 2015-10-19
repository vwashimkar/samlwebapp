package vish.saml;

import org.apache.commons.logging.Log;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.*;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import vish.config.AppSettings;
import vish.util.Utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Created by vwashimker on 10/14/2015.
 */
public class SamlConsumer {

    private AppSettings appSettings =  null;
    private static SecureRandomIdentifierGenerator generator;

    private final String signature = "MIICNTCCAZ6gAwIBAgIES343gjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJVUzELMAkGA1UE\n" +
            "CAwCQ0ExFjAUBgNVBAcMDU1vdW50YWluIFZpZXcxDTALBgNVBAoMBFdTTzIxEjAQBgNVBAMMCWxv\n" +
            "Y2FsaG9zdDAeFw0xMDAyMTkwNzAyMjZaFw0zNTAyMTMwNzAyMjZaMFUxCzAJBgNVBAYTAlVTMQsw\n" +
            "CQYDVQQIDAJDQTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzENMAsGA1UECgwEV1NPMjESMBAGA1UE\n" +
            "AwwJbG9jYWxob3N0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUp/oV1vWc8/TkQSiAvTou\n" +
            "sMzOM4asB2iltr2QKozni5aVFu818MpOLZIr8LMnTzWllJvvaA5RAAdpbECb+48FjbBe0hseUdN5\n" +
            "HpwvnH/DW8ZccGvk53I6Orq7hLCv1ZHtuOCokghz/ATrhyPq+QktMfXnRS4HrKGJTzxaCcU7OQID\n" +
            "AQABoxIwEDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQEFBQADgYEAW5wPR7cr1LAdq+IrR44i\n" +
            "QlRG5ITCZXY9hI0PygLP2rHANh+PYfTmxbuOnykNGyhM6FjFLbW2uZHQTY1jMrPprjOrmyK5sjJR\n" +
            "O4d1DeGHT/YnIjs9JogRKv4XHECwLtIVdAbIdWHEtVZJyMSktcyysFcvuhPQK8Qc/E/Wq8uHSCo=";

    private Log LOG;

    public SamlConsumer() {
        this.appSettings = AppSettings.getAppSettings();
        this.LOG = Utils.getLog(SamlConsumer.class);
    }


    public String getRequestString() throws IOException, MarshallingException {
        // the issuerUrl is the url of the service provider who generates the message
        String issuerUrl = appSettings.getIssuerUrl();
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
        issuer.setValue(issuerUrl);

        //creating the authentication Request now
        DateTime issueInstant = new DateTime();
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authnRequest = authnRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
                        "AuthnRequest", "samlp");
        authnRequest.setForceAuthn(false);
        authnRequest.setIsPassive(false);
        authnRequest.setIssueInstant(issueInstant);
        authnRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST")
        ;
        authnRequest.setAssertionConsumerServiceURL(issuerUrl);
        authnRequest.setIssuer(issuer);
        authnRequest.setID(appSettings.getRandomId());
        authnRequest.setVersion(SAMLVersion.VERSION_20);


        //encoding the message
        return encodeAthenticationRequest(authnRequest);

    }

    private String encodeAthenticationRequest(AuthnRequest authnRequest) throws MarshallingException, IOException {
        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authnRequest);
        Element authDOM = marshaller.marshall(authnRequest);
        StringWriter rspWrt = new StringWriter();
        XMLHelper.writeNode(authDOM, rspWrt);
        String requestMessage = rspWrt.toString();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new
                DeflaterOutputStream(byteArrayOutputStream, deflater);
        deflaterOutputStream.write(requestMessage.getBytes());
        deflaterOutputStream.close();
        /* Encoding the compressed message */
        String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
        LOG.debug("encodedRequestMessage " + encodedRequestMessage);
        String encodedAuthnRequest = URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();
        LOG.debug("encodedAuthnRequest " + encodedAuthnRequest);
        return encodedAuthnRequest;
    }

    public String decodeResponseMessage(String authnReqStr) throws ParserConfigurationException, IOException, SAXException, UnmarshallingException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        //decoding it
        byte[] decoded = Base64.decode(authnReqStr);
        Document document = docBuilder.parse(new ByteArrayInputStream(decoded));
        Element element = document.getDocumentElement();
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        Response response = (Response) unmarshaller.unmarshall(element);
        String subject = response.getAssertions().get(0).getSubject() .getNameID().getValue();
        String certificate = "";
        //lets check if it has certificate
        if(response.getSignature() != null){
             certificate = response.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();
        }

        return subject + "||||" + certificate;
    }
}
