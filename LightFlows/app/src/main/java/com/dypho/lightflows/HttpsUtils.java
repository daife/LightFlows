package com.dypho.lightflows;
import com.dypho.lightflows.utils.FileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpsUtils {
    private static String apiUrl = "https://aidemo.youdao.com/ocrapi1"; // 替换为您的目标 API URL
        private static String langParam = "lang=auto";
    private static HttpURLConnection conn;
    private static BufferedReader reader;
    private static String jsonResponse;
    private static String wordsString;
    private static StringBuilder wordsBuilder;
    
    //状态控制符
    public static boolean ifsend=false;
    public static boolean ifdetect=false;
    
    public static void init(){
        connect();
        
    }
    public static void sendOnce(){
        connect();
        if(FastInputIME.autoclear) {
        	FastInputIME.clear();
        }
        try {
        	FastInputIME.input(HttpsUtils.send(Converter.imageToBase64(Catcher.filePath)));
        } catch(Exception err) {
        	
        }
        disconnect();
        FastInputIME.turnon();
       // FastInputIME.words="null";
        if(FastInputIME.autoenter) {
        	FastInputIME.enter();
        }
    }
    public static String send(String base64Param) {
        	String params = langParam + "&" + "imgBase=base64," + base64Param;
            try {
            	conn.getOutputStream().write(params.getBytes());
            conn.getOutputStream().flush();
            //System.out.println("don1");
            
             reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             jsonResponse = reader.lines().collect(Collectors.joining());

          /*  StringBuilder jsonResponseBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            System.out.println(getStringByStream(conn.getInputStream()));
            String line;
System.out.println("don2");
            while ((line = reader.readLine()) != null) {
            FastInputIME.turnon();
                jsonResponseBuilder.append(line);
                System.out.println("aaaa"+getStringByStream(conn.getInputStream()));
            }*/
            

            // 解析 JSON家互互联网 信息办公室走草家互互联网 信息办公室走草家互互联网 信息办公室走草
           JSONObject responseObj = new JSONObject(jsonResponse);
            JSONArray lines = responseObj.getJSONArray("lines");

             wordsBuilder = new StringBuilder();

            for (int i = 0; i < lines.length(); i++) {
                JSONObject line1 = lines.getJSONObject(i);
                String word = line1.getString("words");
                wordsBuilder.append(word).append(" ");
         }
          

            // 输出结果
            wordsString = wordsBuilder.toString().trim();
        //System.out.println("Words: " + wordsString);
              } catch(Exception err) {
           	err.printStackTrace();
           } 
        if(wordsString==null) {
        	return "null";
        } else {
        	return wordsString;
        }
            
    }
    
    public static void connect(){
        try {
        	 // 创建 URL 对象
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //conn.setConnectTimeout(3000);
            //conn.setReadTimeout(3000);
           // conn.setRequestProperty("Connection","keep-Alive");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
             //System.out.println("don0");

        } catch(Exception err) {
        err.printStackTrace();	
        }
    }
    public static void disconnect(){
         // 关闭连接
          try {
          	reader.close();
          } catch(Exception err) {
          	
          }  
            conn.disconnect(); 
    }
    private static String getStringByStream(InputStream inputStream){
        Reader reader;
        try {
            reader=new InputStreamReader(inputStream,"UTF-8");
            char[] rawBuffer=new char[512];
            StringBuffer buffer=new StringBuffer();
            int length;
            while ((length=reader.read(rawBuffer))!=-1){
                buffer.append(rawBuffer,0,length);
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void startdetect(){
        ifdetect=true;
        while(ifdetect){
            if(ifsend) {
            	sendOnce();
                ifdetect=false;
            }
            
        }
        
    }

}

