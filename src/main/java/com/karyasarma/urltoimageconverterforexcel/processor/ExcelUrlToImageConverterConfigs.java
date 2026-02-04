package com.karyasarma.urltoimageconverterforexcel.processor;

import java.io.File;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class ExcelUrlToImageConverterConfigs
{
    private File fileInput;
    private File fileOutput;

    private int sheetIndex;
    private int rowIndexStart;
    private int rowIndexEnd;

    private int rowHeight;
    private int columnWidth;

    public ExcelUrlToImageConverterConfigs()
    {
    }

    public File getFileInput()
    {
        return fileInput;
    }

    public void setFileInput(File fileInput)
    {
        this.fileInput = fileInput;
    }

    public File getFileOutput()
    {
        return fileOutput;
    }

    public void setFileOutput(File fileOutput)
    {
        this.fileOutput = fileOutput;
    }

    public int getSheetIndex()
    {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex)
    {
        this.sheetIndex = sheetIndex;
    }

    public int getRowIndexStart()
    {
        return rowIndexStart;
    }

    public void setRowIndexStart(int rowIndexStart)
    {
        this.rowIndexStart = rowIndexStart;
    }

    public int getRowIndexEnd()
    {
        return rowIndexEnd;
    }

    public void setRowIndexEnd(int rowIndexEnd)
    {
        this.rowIndexEnd = rowIndexEnd;
    }

    public int getRowHeight()
    {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight)
    {
        this.rowHeight = rowHeight;
    }

    public int getColumnWidth()
    {
        return columnWidth;
    }

    public void setColumnWidth(int columnWidth)
    {
        this.columnWidth = columnWidth;
    }
}
