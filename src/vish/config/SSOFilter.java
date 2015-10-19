package vish.config;

import org.apache.commons.logging.Log;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;
import vish.saml.*;
import vish.util.Utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by vwashimker on 10/14/2015.
 */
public class SSOFilter implements Filter {

    private Log LOG = Utils.getLog(SSOFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            SamlConsumer  samlConsumer = new SamlConsumer();
            if(servletRequest.getParameter("SAMLResponse") == null){

                final String requestString = samlConsumer.getRequestString();
                LOG.debug("Request String " + requestString);
                String redirectionUrl = AppSettings.getAppSettings() .getIdp_url() + "?SAMLRequest=" + requestString;
                ((HttpServletResponse)servletResponse).sendRedirect(redirectionUrl);
                LOG.debug("Is SAML Request");
            } else{
                System.out.println("IS SAML Response");
                LOG.debug("IS SAML Response");

                String responseMessage = servletRequest.getParameter("SAMLResponse");
                String result = null;
                try {
                    result = samlConsumer.decodeResponseMessage(responseMessage);
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (UnmarshallingException e) {
                    e.printStackTrace();
                }
                System.out.println("result after processing string = " + result);
                LOG.debug("result after processing " + result);
                filterChain.doFilter(servletRequest, servletResponse);
            }


        } catch (MarshallingException e) {
            e.printStackTrace();

        }
    }


    @Override
    public void destroy() {

    }

}
