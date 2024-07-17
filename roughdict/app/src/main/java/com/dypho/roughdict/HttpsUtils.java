package com.dypho.roughdict;

import com.dypho.roughdict.Utils;
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
    //private static String apiUrl = "https://aidemo.youdao.com/ocrapi1"; // 替换为您的目标 API URL
       // private static String langParam = "lang=auto";
    private static HttpURLConnection conn;
    private static BufferedReader reader;
    private static String jsonResponse;
    //private static String wordsString;
    //private static StringBuilder wordsBuilder;
    
    //状态控制符
    public static boolean ifsend=false;
    public static boolean ifdetect=false;
    
    public static String sendOnce(String word){
        word=word.toLowerCase();
        connect(word);
        String result=send();
        disconnect();
        return result;
        
        
    }
    public static String send(/*String base64Param*/) {
        String result="null";
        	//String params = langParam + "&" + "imgBase=base64," + base64Param;
            try {
            	//conn.getOutputStream().write(params.getBytes());
            //conn.getOutputStream().flush();
            //System.out.println("don1");
            
             reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             jsonResponse = reader.lines().collect(Collectors.joining());
            //System.out.println("原始"+jsonResponse);
            result=Utils.jsonProcess(jsonResponse);
            //System.out.println(result);
            
            } catch(Exception err) {
      	
      }
        return result;
            
    }
    
    public static void connect(String word){
        try {
        	 // 创建 URL 对象
            URL url = new URL("https://dict.youdao.com/jsonapi?q="+word+"&dicts=%7B%22count%22%3A99%2C%22dicts%22%3A%5B%5B%22ec%22%2C%22ce%22%2C%22rel_word%22%2C%22phrs%22%2C%22fanyi%22%2C%22blng_sents_part%22%%22%5D%5D%7D");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
           // conn.setRequestProperty("Connection","keep-Alive");
            //conn.setDoOutput(true);
            //conn.setDoInput(true);
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
   }


