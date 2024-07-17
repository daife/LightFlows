package com.dypho.lightflows;

import android.util.Base64;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
//import java.util.Base64;
import java.net.URLEncoder;
import org.bytedeco.opencv.opencv_core.Mat;
import java.io.IOException;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*; // 导入 imwrite 方法


public class Converter {
   
    public static String imageToBase64(String imagePath) {
          try{  InputStream inputStream = new FileInputStream(imagePath); // You can get an inputStream using any I/O API
byte[] bytes;
byte[] buffer = new byte[8192];
int bytesRead;
ByteArrayOutputStream output = new ByteArrayOutputStream();


    while ((bytesRead = inputStream.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
    }


bytes = output.toByteArray();
            return URLEncoder.encode(Base64.encodeToString(bytes, Base64.DEFAULT), "UTF-8");
            }
catch (IOException e) {
    e.printStackTrace();
            return null;
}
    }

}

