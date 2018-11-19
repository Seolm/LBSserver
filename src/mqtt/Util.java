package mqtt;

import java.text.SimpleDateFormat;

import java.util.Date;
 
 
 
public class Util {
 
    public static String getTime() {
 
        return new SimpleDateFormat("[hh:mm:ss:SSS]").format(new Date(System.currentTimeMillis()));        
 
    }
 
}
