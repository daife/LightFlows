package com.dypho.lightflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_features2d.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_features2d.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_highgui.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import org.opencv.core.CvType;

public class CameraMovementEstimator {
    private ORB orb;
    private Mat previousDescriptors;
    private KeyPointVector previousKeypoints;
    private DescriptorMatcher descriptorMatcher;
    private Mat previousFrame; // 用于保存上一帧图像

    public CameraMovementEstimator() {
        orb = ORB.create();
        orb.setMaxFeatures(20);
        orb.setScaleFactor(1.2f);
        orb.setNLevels(4);
        orb.setEdgeThreshold(5);
        orb.setFirstLevel(0);
        orb.setWTA_K(2);
        orb.setScoreType(ORB.FAST_SCORE);
        previousDescriptors = new Mat();
        previousKeypoints = new KeyPointVector();
        descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    }

    public int processFrame(Mat currentFrame) {
        KeyPointVector currentKeypoints = new KeyPointVector();
        Mat currentDescriptors = new Mat();
        if (currentFrame == null || currentFrame.empty()) {
            System.err.println("Invalid frame.");
            return 0;
        }
        orb.detectAndCompute(currentFrame, new Mat(), currentKeypoints, currentDescriptors);
        if (currentDescriptors.empty()) {
            System.out.println("meirwentiba");
            return 0;
        }
        if (previousDescriptors.empty()) {
            previousKeypoints.clear();
            previousKeypoints.put(currentKeypoints.get());
            previousDescriptors.release();
            previousDescriptors = currentDescriptors.clone();
            previousFrame = currentFrame.clone();
            System.out.println("First frame processed. No movement to calculate.");
            return 0;
        }

        DMatchVector matches = new DMatchVector();
        descriptorMatcher.match(previousDescriptors, currentDescriptors, matches);

        // 修改此处代码以选择距离最小的前5个匹配点
        List<DMatch> allMatches = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            allMatches.add(matches.get(i));
        }
        // 使用自定义比较器按照DMatch的distance属性进行排序
        Collections.sort(allMatches, new Comparator<DMatch>() {
            @Override
            public int compare(DMatch m1, DMatch m2) {
                return Double.compare(m1.distance(), m2.distance());
            }
        });

        // 取排序后的前5个匹配点
        List<DMatch> goodMatches = allMatches.subList(0, Math.min(5, allMatches.size()));
        DMatchVector goodMatchesVector = new DMatchVector(goodMatches.toArray(new DMatch[0]));
        int result = (int) -computeAverageMovementX(
                                        previousKeypoints, currentKeypoints, goodMatches);

      /*  Mat outImg = new Mat(100, 150 * 2, CvType.CV_8UC3);
        drawMatches(
                previousFrame,
                previousKeypoints,
                currentFrame,
                currentKeypoints,
                goodMatchesVector,
                outImg,
                new Scalar(255, 0, 0, 0),
                new Scalar(0, 255, 0, 0),
                new BytePointer(),
                2);
        imwrite(
                "/storage/emulated/0/Download/woodbox/apk/杂项/app/outImg"
                        + String.valueOf(
                                result)
                        + ".jpg",
                outImg);
        outImg.release();*///测试

        previousKeypoints.clear();
        previousKeypoints.put(currentKeypoints.get());
        previousDescriptors.release();
        previousDescriptors = currentDescriptors.clone();
        previousFrame.release();
        previousFrame = currentFrame.clone();
        return result;
    }

    private double computeAverageMovementX(
            KeyPointVector previousKeypoints,
            KeyPointVector currentKeypoints,
            List<DMatch> goodMatches) {
        if (goodMatches.isEmpty() || previousKeypoints.empty() || currentKeypoints.empty()) {
            return 0.0;
        }

        List<Double> movementsX = new ArrayList<>();
        for (DMatch match : goodMatches) {
            KeyPoint prevKeyPoint = previousKeypoints.get(match.queryIdx());
            KeyPoint currKeyPoint = currentKeypoints.get(match.trainIdx());
            Point2f prevPoint = prevKeyPoint.pt();
            Point2f currPoint = currKeyPoint.pt();
            movementsX.add(Double.valueOf(currPoint.x() - prevPoint.x()));
        }
        // 使用中位数而不是平均值来减少异常值的影响
        if (movementsX.isEmpty()) return 0.0;
        Collections.sort(movementsX);
        double medianX;
        if (movementsX.size() % 2 == 0) {
            medianX =
                    (movementsX.get(movementsX.size() / 2 - 1)
                                    + movementsX.get(movementsX.size() / 2))
                            / 2.0;
        } else {
            medianX = movementsX.get(movementsX.size() / 2);
        }

        return medianX;
    }

    public static void main(String[] args) {
        // 这里只是一个示例主函数，实际使用时需要根据实际情况创建CameraMovementEstimator实例并传入图像帧
        CameraMovementEstimator estimator = new CameraMovementEstimator();
        // Mat currentFrame = ... // 获取当前帧图像
        // estimator.processFrame(currentFrame);
    }
}