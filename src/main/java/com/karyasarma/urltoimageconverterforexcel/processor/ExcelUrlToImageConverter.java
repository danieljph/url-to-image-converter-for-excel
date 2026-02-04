package com.karyasarma.urltoimageconverterforexcel.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public class ExcelUrlToImageConverter
{
    private final Logger log = LogManager.getLogger(ExcelUrlToImageConverter.class);

    private final ExcelUrlToImageConverterConfigs configs;
    private final ExcelUrlToImageConverterListener listener;

    public ExcelUrlToImageConverter(ExcelUrlToImageConverterConfigs configs, ExcelUrlToImageConverterListener listener)
    {
        this.configs = configs;
        this.listener = listener;
    }

    public void process()
    {
        listener.onStart();

        try
        (
            var fis = new FileInputStream(configs.getFileInput());
            var workbook = new XSSFWorkbook(fis)
        )
        {
            processWorkbook(configs, workbook);
        }
        catch(Exception ex)
        {
            logError("Failed on method 'process'.", ex);
        }
        finally
        {
            listener.onFinish();
        }
    }

    private void processWorkbook(ExcelUrlToImageConverterConfigs configs, XSSFWorkbook workbook) throws Exception
    {
        try
        {
            var sheetIndex = configs.getSheetIndex();
            var rowIndexStart = configs.getRowIndexStart() - 1;
            var rowIndexEnd = configs.getRowIndexEnd() - 1;

            var sheet = workbook.getSheetAt(sheetIndex);

            rowIndexEnd = Math.min(rowIndexEnd, sheet.getLastRowNum());
            listener.onConvertStarting(rowIndexEnd - rowIndexStart + 1);
            int progressCounter = 0;

            for(var rowIndex = rowIndexStart; rowIndex <= rowIndexEnd; rowIndex++)
            {
                logInfo("Processing Row(%s).".formatted(rowIndex + 1));

                var row = sheet.getRow(rowIndex);

                for(var cell : row)
                {
                    logInfo("Processing Cell(%s, %s).".formatted(rowIndex + 1, cell.getColumnIndex() + 1));

                    var dataFormatter = new DataFormatter();
                    var cellValue = dataFormatter.formatCellValue(cell);

                    if(cellValue != null && cellValue.startsWith("IMAGE("))
                    {
                        var imageRefOrUrl = cellValue
                            .replace("IMAGE(", "")
                            .replace(")", "");

                        String imageUrl;

                        if(imageRefOrUrl.contains("http"))
                        {
                            imageUrl = imageRefOrUrl.replace("\"", "");
                        }
                        else
                        {
                            var cellReferenceImageRef = new CellReference(imageRefOrUrl);

                            imageUrl = sheet
                                .getRow(cellReferenceImageRef.getRow())
                                .getCell(cellReferenceImageRef.getCol())
                                .getStringCellValue();
                        }

                        insertImage(sheet, cell, imageUrl);
                    }

                    logInfo("Processing Cell(%s, %s) done.".formatted(rowIndex + 1, cell.getColumnIndex() + 1));
                }

                logInfo("Processing Row(%s) done.".formatted(rowIndex + 1));
                progressCounter++;
                listener.onConvertProgressing(progressCounter);
            }

            saveWorkbook(workbook);
        }
        catch(Exception ex)
        {
            logError("Failed on method 'processWorkbook'.", ex);
            saveWorkbook(workbook);
        }
    }

    private void insertImage(XSSFSheet sheet, Cell cellImage, String imageUrl) throws Exception
    {
        byte[] imageBytes;

        logInfo("Downloading image: %s".formatted(imageUrl));

        try(var is = new URI(imageUrl).toURL().openStream())
        {
            imageBytes = IOUtils.toByteArray(is);
        }

        logInfo("Downloading image done.");

        int pictureIdx = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

        var drawing = sheet.createDrawingPatriarch();
        var helper = sheet.getWorkbook().getCreationHelper();
        var anchor = helper.createClientAnchor();

        anchor.setRow1(cellImage.getRowIndex());
        anchor.setCol1(cellImage.getColumnIndex());
        anchor.setRow2(cellImage.getRowIndex() + 1);
        anchor.setCol2(cellImage.getColumnIndex() + 1);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        drawing.createPicture(anchor, pictureIdx);

        var widthInChars = configs.getColumnWidth();
        sheet.setColumnWidth(cellImage.getColumnIndex(), widthInChars * 256); // The width argument is in units of 1/256th of a character width.
        cellImage.getRow().setHeightInPoints(configs.getRowHeight());

        // Optionally clear the cell
        cellImage.setBlank();
    }

    private void saveWorkbook(XSSFWorkbook workbook) throws Exception
    {
        logInfo("Saving file to: %s".formatted(configs.getFileOutput().getAbsolutePath()));

        try(FileOutputStream fos = new FileOutputStream(configs.getFileOutput()))
        {
            workbook.write(fos);
        }

        logInfo("Saving file done.");
    }

    private void logInfo(String message)
    {
        log.info(message);
        listener.onInfo(message);
    }

    private void logError(String message, Throwable throwable)
    {
        log.error(message, throwable);
        listener.onError(message, throwable);
    }
}
