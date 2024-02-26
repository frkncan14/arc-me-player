package com.mediatek.wwtv.mediaplayer.util;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.media.AudioAttributes;
import android.os.Bundle;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

import java.util.List;

public class TextToSpeechUtil {
    private static final String TAG = "TextToSpeechUtil";

    public static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/.TalkBackService";

    private static TextToSpeech mTts = null;
    private Context myContext = null;
    private Bundle mTtsBundle = null;

   /**
    * Queue mode where all entries in the playback queue (media to be played
    * and text to be synthesized) are dropped and replaced by the new entry.
    * Queues are flushed with respect to a given calling app. Entries in the queue
    * from other callees are not discarded.
    */
    public static final int QUEUE_FLUSH = 0;

   /**
    * Queue mode where the new entry is added at the end of the playback queue.
    */
    public static final int QUEUE_ADD = 1;

   /**
    * The constructor for the TextToSpeechUtil class, using the default TTS engine.
    * @param context The context this instance is running in.
    */
    public TextToSpeechUtil(Context context) {
        if(context!=null){
            myContext = context.getApplicationContext();
        }else{
            Log.d(TAG, "context is null!!!");
        }
    }

    private TextToSpeech getTextToSpeech() {
        if(mTts == null) {
            mTts = new TextToSpeech(myContext, mInitListener);
            Log.d(TAG, "new TextToSpeech created!!!");
            mTts.setAudioAttributes(new AudioAttributes.Builder().setContentType(1).setUsage(11).build());
            mTtsBundle = new Bundle();
            mTtsBundle.putInt("streamType", 10);
        }

        return mTts;
    }

   /**
    * The initialization listener used when we are initalizing the settings
    * screen for the first time (as opposed to when a user changes his choice
    * of engine).
    */
    private final TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
        onInitEngine(status);
        }
    };

   /**
    * Called when the TTS engine is initialized.
    */
    private void onInitEngine(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS engine for settings screen initialized.");
            //Log.d(TAG, "Updating engine: Successfully bound to the engine: " +
            //    getTextToSpeech().getCurrentEngine());
        } else {
            Log.d(TAG, "TTS engine for settings screen failed to initialize successfully.");
        }
    }

   /**
    * Check current is TTS Enable or disable status.
    * @return true when current TTS is enable from Launcher Settings.
    * @return false when disable TTS.
    */
    public boolean isTTSEnabled() {
    	return TextToSpeechUtil.isTTSEnabled(myContext);
    }

    public static boolean isTTSEnabled(Context context) {
		  AccessibilityManager am =
		  	(AccessibilityManager) context.getSystemService(
		  		Context.ACCESSIBILITY_SERVICE);
	      List<AccessibilityServiceInfo> enableServices =
		  	am.getEnabledAccessibilityServiceList(
		  		AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

	      for (AccessibilityServiceInfo service : enableServices) {
	          if (service.getId().contains(TALKBACK_SERVICE)) {
	              return true;
	          }
	      }

        return false;
    }

   /**
    * Speaks the string using the specified queuing strategy and speech parameters.
    * @param text The string of text to be spoken. No longer than
    *{@link #getMaxSpeechInputLength()} characters.
    */
    public void speak(final String text) {
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override public void run() {
                if(isTTSEnabled()){
                    if(getTextToSpeech() == null){
                        Log.d(TAG, "mTts is NULL in speak!!!");
                    }
                    Log.d(TAG, "mTts.getLanguage()="+mTts.getLanguage());
                    Log.d(TAG, "mTts.isLanguageAvailable(mTts.getLanguage())="+mTts.isLanguageAvailable(mTts.getLanguage()));

                    for (int i = 0; i < 5; i++) {
                        if(mTts.isLanguageAvailable(mTts.getLanguage()) >= TextToSpeech.LANG_AVAILABLE){
                            Log.d(TAG, "mTts.speak "+ text);
                            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, mTtsBundle, "MediaPlayer");
                            break;
                        }else{
                            try {
                                Thread.sleep(50);
                                Log.d(TAG, "mTts.isLanguageAvailable false");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    Log.d(TAG, "isTTSEnabled is false!!!");
                }
            }
        });
    }

   /**
    * Speaks the string using the specified queuing strategy and speech parameters.
    * @param text The string of text to be spoken. No longer than
    * {@link #getMaxSpeechInputLength()} characters.
    * @param queueMode The queuing strategy to use, {@link #QUEUE_ADD} or {@link #QUEUE_FLUSH}.
    */
    public int speak(final String text, int queueMode) {
        if(isTTSEnabled()){
            if(getTextToSpeech() == null){
                Log.d(TAG, "mTts is NULL in speak!!!");
                return -1;
            }
            Log.d(TAG, "mTts.getLanguage()="+mTts.getLanguage());
            Log.d(TAG, "mTts.isLanguageAvailable(mTts.getLanguage())="+mTts.isLanguageAvailable(mTts.getLanguage()));
            if(mTts.isLanguageAvailable(mTts.getLanguage()) >= TextToSpeech.LANG_AVAILABLE){
                Log.d(TAG, "mTts.speak "+ text);
                return mTts.speak(text, queueMode, mTtsBundle, "MediaPlayer");
            }else{
                Log.d(TAG, "mTts.isLanguageAvailable false");
                return -1;
            }
        }else{
            Log.d(TAG, "isTTSEnabled is false!!!");
        }
        return -1;
    }

   /**
    * Releases the resources used by the TextToSpeech engine.
    * It is good practice for instance to call this method in the onDestroy() method of an Activity
    * so the TextToSpeech engine can be cleanly stopped.
    */
    public void shutdown() {
        if(getTextToSpeech() == null){
            Log.d(TAG, "mTts is NULL in shutdown!!!");
            return;
        }
        try {
            mTts.shutdown();
            mTts = null;
            Log.d(TAG, "TextToSpeech shutdown now!!!");
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down TTS engine" + e);
        }
    }
}
