package com.karyasarma.urltoimageconverterforexcel.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class FileTypeFilter extends FileFilter
{
    private final String extension;
    private final String description;

    public FileTypeFilter(String extension, String description)
    {
        this.extension = extension;
        this.description = description;
    }

    @Override
    public boolean accept(File file)
    {
        if(file.isDirectory())
        {
            return true;
        }

        return file.getName().toLowerCase().endsWith(extension.toLowerCase());
    }

    @Override
    public String getDescription()
    {
        return description;
    }
}
