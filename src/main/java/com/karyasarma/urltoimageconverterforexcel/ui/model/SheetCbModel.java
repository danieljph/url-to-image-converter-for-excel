package com.karyasarma.urltoimageconverterforexcel.ui.model;

import java.io.File;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class SheetCbModel
{
    private File file;
    private String sheetName;
    private int sheetIndex;
    private int rowCount;

    public SheetCbModel()
    {
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public String getSheetName()
    {
        return sheetName;
    }

    public void setSheetName(String sheetName)
    {
        this.sheetName = sheetName;
    }

    public int getSheetIndex()
    {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex)
    {
        this.sheetIndex = sheetIndex;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }

    @Override
    public String toString()
    {
        return getSheetName();
    }
}
