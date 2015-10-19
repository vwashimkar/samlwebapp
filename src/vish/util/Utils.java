package vish.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by vwashimker on 10/14/2015.
 */
public class Utils {

    private Utils() {

    }

    public static boolean isValue(String str) {
        return str != null && "".equals(str.trim());
    }

    public static Log getLog(Class claz) {
        return LogFactory.getLog(claz);
    }


}
