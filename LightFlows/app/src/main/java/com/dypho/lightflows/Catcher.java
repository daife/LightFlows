package com.dypho.lightflows;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.opencv.opencv_core.Size;
import static org.bytedeco.opencv.global.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*; // 导入 imwrite 方法
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

import java.io.IOException;

public class Catcher {
    private static Camera camera; // 相机对象
    private static int imageFormat = ImageFormat.NV21; // 图像格式
    private static int imgWidth = 75;
    private static int imgHeight = 50;
    public static int templateWidth = 40;
    public static int matchdegree = 70;
    public static int fps = 15;
    public static int dy = 10;//y偏移容忍
    // private static Mat dstmat;//多线程需要使用为成员变量
    // private static Mat mat;//同上
    private static Mat perspective; // = mgetMatrix(54,13,176,6,176,138,54,131);
    private static Rect roi;
    private static Mat templ;
    private static Point nowPoint = new Point(imgWidth - templateWidth, 0);
    public static Mat backGround;
    private static Camera.Parameters p;
    private static byte[] mPreviewData = new byte[176 * 144 * 3 / 2];
    private static SurfaceTexture surfaceTexture;
    public static boolean isTorch = true;
    public static boolean isbackGroundempty = true;
    public static boolean shouldprocess = false;
    public static int cameraIndex;
    public static int threshold;
    public static String filePath;
    public static int ocrapi;

    public static void init() {
        roi = new Rect(imgWidth - templateWidth, dy, templateWidth, 50-dy);
        nowPoint = new Point(imgWidth - templateWidth, 0);
        // dstmat = new Mat(144, 176, CV_8UC3);//多线程所需
        // mat = new Mat();//多线程所需
        perspective = mgetMatrix(54, 13, 176, 6, 176, 138, 54, 131);
        // initCameara();
        // setCallback();
    }

    public static void startCatch() {

        FastInputIME.couldCatch = false;
        //
        // initCameara();
        setCallback();
        //
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        camera.startPreview();
        isTorch = true;
        FastInputIME.isCapturing = true;
    }

    public static void stopCatch() {
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
        shouldprocess = false;
        isbackGroundempty = true;
        isTorch = false;
        nowPoint = new Point(imgWidth - templateWidth, 0);
        FastInputIME.couldCatch = true;
    }

    // 测试方法
    public static void setPar() {
        camera.setParameters(p);
    }

    public static void setdistorch() {
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    }

    public static void onlystoppreview() {
        camera.stopPreview();
    }

    public static void onlystartpreview() {
        camera.startPreview();
    }

    public static void initCameara() {
        camera = Camera.open(cameraIndex);
        p = camera.getParameters();
        List<Integer> supportedFps = p.getSupportedPreviewFrameRates();
        int maxFps = Collections.max(supportedFps);

        // 设置预览帧率
        if (fps < maxFps) {
            p.setPreviewFrameRate(fps);
        } else {
            p.setPreviewFrameRate(maxFps);
        }
        p.setPreviewFormat(ImageFormat.NV21);
        p.setPreviewSize(176, 144);
    }

    private static void backGroundProcess() {
        try {

            if (FastInputIME.ifleft) {
                flip(backGround, backGround, -1);
            }
            imwrite(filePath, backGround);
            new Thread() {
                @Override
                public void run() {

                    try {
                        switch (ocrapi) {
                            case 0:
                                OcrDemo.main();
                                break;
                            case 1:
                                UniversalCharacterRecognition.main();
                                break;
                            case 2:
                                HttpsUtils.sendOnce();
                                break;
                            default:
                                break;
                        }
                        // OcrDemo.main();

                    } catch (Exception err) {

                    }
                }
            }.start();

        } catch (Exception err) {

        }
    }

    private static void setCallback() {
        try {
            // 主要是surfaceTexture获取预览数据，但不显示
            surfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置 camera.addCallbackBuffer(mPreviewData) 后才会回调，旨在每处理完一帧数据回调一次
        camera.setPreviewCallbackWithBuffer(mPreviewCallback);
        camera.addCallbackBuffer(mPreviewData);
        // camera.startPreview();
    }

    public static void closeCamera() {
        camera.stopPreview();
        camera.setPreviewCallbackWithBuffer(null);
        camera.release();
        camera = null;
    }

    private static Camera.PreviewCallback mPreviewCallback =
            new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    // 在此处处理当前帧数据，并设置下一帧回调
                    if (FastInputIME.isCapturing) {
                        // 该if内的所有内容应当在一个普通线程中执行
                        new Thread() {
                            @Override
                            public void run() {
                                Mat dstmat = new Mat(144, 176, CV_8UC3); // 多线程所需
                                Mat mat = new Mat(); // 多线程所需

                                cvtColor(
                                        new Mat(216, 176, CV_8UC1, new BytePointer(data)),
                                        mat,
                                        CV_YUV2BGR_NV21);

                                warpPerspective(
                                        mat, dstmat, perspective, new Size(imgWidth, imgHeight));
                                // 以下为优化图像质量

                                cvtColor(dstmat, dstmat, COLOR_BGR2GRAY); // 转换为灰度图

                                GaussianBlur(dstmat, dstmat, new Size(3, 3), 0); // 使用高斯滤波器

                                //threshold(dstmat, dstmat, 0, 255, THRESH_OTSU);

                                if (backGround == null || isbackGroundempty) {
                                    // backGround = new Mat(100, 4000, CV_8UC1, new Scalar(255));
                                    // dstmat.copyTo(backGround.apply(new Rect(0, 0, 150, 100)));
                                    backGround = dstmat.clone();
                                    templ = dstmat.apply(roi).clone();
                                    isbackGroundempty = false;
                                } else {
                                    // 将此处提交到单线程处理器执行
                                    // 注意要换成最终变量被单线程执行器捕获
                                    Runnable finalTask =
                                            () -> {
                                                Point point = matchTemplateAndReturn(dstmat, templ);
                                                if (point.x() < imgWidth - templateWidth
                                                        && point.x() > threshold) {

                                                    nowPoint.x(nowPoint.x() - point.x());//配准后，后图左边缘在backGround的位置
                                    Mat crossRrea = new Mat();
                                    addWeighted(
                                        backGround.apply(
                                                                    new Rect(
                                                                            nowPoint.x()
                                                                                    + point.x(),
                                                                            0,
                                                                            templateWidth,//backGroud中的模板左边缘至右边缘
                                                                            50)),
                                        0.5,
                                         dstmat.apply(
                                                                    new Rect(
                                                                            point.x(),
                                                                            0,
                                                                            templateWidth,//模板左边缘至右边缘
                                                                            50)),
                                        0.5,
                                        0.0,//这是干嘛的?
                                        crossRrea
                                        );
                                    hconcat(
                                                            backGround.apply(
                                                                    new Rect(
                                                                            0,
                                                                            0,
                                                                            nowPoint.x()
                                                                                    + point.x(),//模板左边缘，这不是脱裤子放屁...现在是除了模板backGround左边部分
                                                                            50)),
                                                            crossRrea,
                                                            backGround);

                                                    hconcat(
                                                            backGround.apply(
                                                                    new Rect(
                                                                            0,
                                                                            0,
                                                                            nowPoint.x()
                                                                                    + point.x()//模板左边缘，这不是脱裤子放屁...
                                                                                    + templateWidth,//加上这个就是整个backGround原图，好像又有点脱裤子放屁的感觉...
                                                                            50)),
                                                            dstmat.apply(
                                                                    new Rect(
                                                                            point.x()
                                                                                    + templateWidth,
                                                                            0,
                                                                            imgWidth
                                                                                    - point.x()
                                                                                    - templateWidth,//注意rect构造不是左上右下点，而是左上点坐标,宽,高
                                                                            50)),
                                                            backGround);
                                                    templ = dstmat.apply(roi).clone();
                                                    nowPoint.x(nowPoint.x() + imgWidth - templateWidth);
                                                }
                                            };
                                    singleThreadExecutor.submit(finalTask);
                                }
                            }
                        }.start();

                        // 以上为图像优化

                    }

                    if (shouldprocess) {
                        // 集合了stop方法

                        // 待修改
                        // 应当将此处对图像最后的处理提交到单线程，注意保留“停止下一次回调的操作”不要到单线程，否则还会继续捕捉图像
                        // 记得关闭执行器
                        Runnable finalTask =
                                () -> {
                                    nowPoint = new Point(imgWidth - templateWidth, 0);
                                    backGroundProcess();
                                };
                        singleThreadExecutor.submit(finalTask);
                        // singleThreadExecutor.shutdown();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(p);
                        camera.stopPreview();
                        shouldprocess = false;
                        isbackGroundempty = true;
                        isTorch = false;
                        FastInputIME.isCapturing = false;
                        FastInputIME.couldCatch = true;
                    }
                    camera.addCallbackBuffer(mPreviewData);

                    // MainActivity.imageCount++; // 将图片的编号加一&
                }
            };

    private static Mat mgetMatrix(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {

        FloatPointer srcCorners =
                new FloatPointer(
                        x1, y1,
                        x2, y2,
                        x3, y3,
                        x4, y4);

        FloatPointer dstCorners =
                new FloatPointer(
                        imgWidth - 50,
                        0,
                        imgWidth,
                        0,
                        imgWidth,
                        imgHeight,
                        imgWidth - 50,
                        imgHeight);

        // create matrices with width 2 to hold the x,y values, and 4 rows, to hold the 4 different
        // corners.
        Mat src = new Mat(new Size(2, 4), CV_32F, srcCorners);
        Mat dst = new Mat(new Size(2, 4), CV_32F, dstCorners);

        Mat perspective = getPerspectiveTransform(src, dst);
        src.release();
        dst.release();
        srcCorners.deallocate();
        dstCorners.deallocate();

        return perspective;
    }

    // 定义一个方法，参数为源图像 src，模板图像 templ，匹配方法 method
    private static Point matchTemplateAndReturn(Mat src, Mat tem) {
        // 创建一个结果矩阵，大小为 src 和 templ 的差值加一

        Mat result = new Mat(imgWidth - templateWidth + 1, 0 + 1, CV_32FC1);

        // 使用 matchTemplate 函数进行模板匹配，结果存储在 result 矩阵中
        matchTemplate(src, tem, result, TM_CCORR_NORMED);

        // 创建两个 double 数组，用于存储匹配度的最大值和最小值
        double[] maxVal = new double[1];

        // 创建两个 Point 对象，用于存储匹配度最大值和最小值的位置
        Point maxLoc = new Point();
        // 使用 minMaxLoc 函数找到 result 矩阵中的最大值和最小值及其位置
        minMaxLoc(result, new double[0], maxVal, new Point(), maxLoc, null);
        if (maxVal[0] < (double) matchdegree / 100) {
            maxLoc.x(0);
        }
        return maxLoc;
    }

    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
}
