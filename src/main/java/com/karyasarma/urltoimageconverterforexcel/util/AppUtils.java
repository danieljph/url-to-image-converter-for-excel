package com.karyasarma.urltoimageconverterforexcel.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class AppUtils
{
    private AppUtils()
    {
    }

    public static String toString(Throwable ex)
    {
        var sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
