package edu.temple.bookcase;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AudioService extends Service {

    private Handler audioHandler; //Reference to client Handler, so the service can communicate with client


    boolean paused;

    class AudioBinder extends Binder {
        AudioService getService(){
            return AudioService.this;
        }
    }
    public AudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioBinder(); //Binder is returned to client when it binds to the service
    }

    public void startMusic(final int start, final int dur){
        new Thread(){ //run in worker thread, unless you use Intent Service that creates one for you
            @Override
            public void run(){
                for(int i = start; i <= dur; i ++){
                    if(audioHandler != null){
                        audioHandler.sendEmptyMessage(i); //Send the current spot in the audio
                        while(paused); //Spin-lock for pause
                        Log.d("AUDIO_SPOT", "Time in audio at: " + i);
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    public boolean onUnbind(Intent intent){
        audioHandler = null;
        return super.onUnbind(intent);
    }


}
