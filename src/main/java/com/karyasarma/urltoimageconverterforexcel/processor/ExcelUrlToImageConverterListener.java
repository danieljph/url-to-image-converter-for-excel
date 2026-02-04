package com.karyasarma.urltoimageconverterforexcel.processor;

/**
 * @author Daniel Joi Partogi Hutapea
 */
public interface ExcelUrlToImageConverterListener
{
    void onStart();

    void onConvertStarting(int total);

    void onConvertProgressing(int counter);

    void onInfo(String info);

    void onError(String message, Throwable throwable);

    void onFinish();
}
