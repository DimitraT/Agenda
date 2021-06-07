package com.example.agenda;

import android.content.Context;

import java.util.Locale;

public class TextToSpeech {
    private android.speech.tts.TextToSpeech tts;
    private android.speech.tts.TextToSpeech.OnInitListener initListener=
            new android.speech.tts.TextToSpeech.OnInitListener() {

                //kiria glossa ta agglika
                @Override
                public void onInit(int i) {
                    tts.setLanguage(Locale.getDefault());
                }
            };

    //constructor gia energopoihsh ths efarmoghs
    public TextToSpeech(Context context) {
        tts = new android.speech.tts.TextToSpeech(context,initListener);
    }

    // sunartisi gia omilia
    public void speak(String message){
        tts.speak(message, android.speech.tts.TextToSpeech.QUEUE_ADD,null,null);
    }
}
