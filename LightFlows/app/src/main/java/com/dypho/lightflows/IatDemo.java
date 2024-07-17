package com.dypho.lightflows;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONTokener;

public class IatDemo {
    private static String TAG = IatDemo.class.getSimpleName();
    static private SpeechRecognizer mIat;
    // 语音听写UI
    //private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private static HashMap<String, String> mIatResults = new LinkedHashMap<>();

    // 引擎类型
    private static String mEngineType = SpeechConstant.TYPE_CLOUD;

    //private String[] languageEntries;
   // private String[] languageValues;
    private static String language = "zh_cn";
    //private int selectedNum = 0;

    private static String resultType = "json";

    private static StringBuffer buffer = new StringBuffer();
            static int ret = 0; // 函数调用返回值
    
    
    public static void init(Context context){
        mIat = SpeechRecognizer.createRecognizer(context,mInitListener);
        
    }
    
    public static void start() {
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
           // this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

            // 开始听写
            // 如何判断一次听写结束：OnResult isLast=true 或者 onError
                buffer.setLength(0);
                //mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
                // 设置参数
                setParam();
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        //showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                    } else {
                       // showTip(getString(R.string.text_begin));
                    }
                
                
        
            
    }
    
    public static void stop(){
         mIat.stopListening();
               // showTip("停止听写");
    }
    static private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                //showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

   

    /**
     * 听写监听器。
     */
    static private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //showTip("开始说话");
            FastInputIME.turnoff();
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.d(TAG, "onError " + error.getPlainDescription(true));
            //showTip(error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //showTip("结束说话");
            //FastInputIME.turnon();
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
          //  Log.d(TAG, results.getResultString());
            if (isLast) {
                Log.d(TAG, "onResult 结束");
            }
            if (resultType.equals("json")) {
                printResult(results);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                if(FastInputIME.autoclear) {
        	FastInputIME.clear();
        }
                FastInputIME.input(buffer.toString());
                if(FastInputIME.autoenter) {
        	FastInputIME.enter();
        }
                FastInputIME.turnon();
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
           // showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
//            	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//            		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//            		Log.d(TAG, "session id =" + sid);
//            	}
        }
    };
    
    private static void printResult(RecognizerResult results) {
        String text = parseIatResult(results.getResultString());
        String sn = null;
        // 读9取json结果中的sn字段
         if(FastInputIME.autoclear) {
        	FastInputIME.clear();
        }
                FastInputIME.input(text);
                if(FastInputIME.autoenter) {
        	FastInputIME.enter();
        }
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
       
                FastInputIME.turnon(); 
    }
    
    public static void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = "mandarin";
            // 设置语言
            Log.e(TAG, "language = " + language);
            mIat.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
        "/sdcard/Android/data/com.dypho.lightflows/files/temp/iat.wav");
    }
    
    private static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
//				如果需要多候选结果，解析数组其他字段
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
}
