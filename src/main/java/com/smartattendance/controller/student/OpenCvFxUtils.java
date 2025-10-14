package com.smartattendance.controller.student;

import javafx.scene.image.Image;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacpp.BytePointer;

import java.io.ByteArrayInputStream;

public final class OpenCvFxUtils {
    private OpenCvFxUtils(){}

    public static Image mat2Image(Mat mat) {
        BytePointer buf = new BytePointer();
        opencv_imgcodecs.imencode(".png", mat, buf);
        byte[] bytes = new byte[(int) buf.limit()];
        buf.get(bytes);
        return new Image(new ByteArrayInputStream(bytes));
    }
}
