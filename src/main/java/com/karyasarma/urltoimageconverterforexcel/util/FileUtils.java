package com.karyasarma.urltoimageconverterforexcel.util;

import java.io.File;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class FileUtils
{
    private FileUtils()
    {
    }

    public static String getFileNameWithoutExtension(File file)
    {
        var name = file.getName();

        int lastDot = name.lastIndexOf('.');

        if(lastDot == -1)
        {
            return name; // No extension found.
        }

        return name.substring(0, lastDot);
    }
}
