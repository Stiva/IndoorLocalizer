package com.indoorlocalizer.app.activity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.indoorlocalizer.app.R;

import java.lang.ref.WeakReference;

/**
 * Created by federicostivani on 08/11/13.
 */
public class SplashActivity extends Activity {
    /*
     * MIN_WAIT_INTERVAL and MAX_WAIT interval contains the ms value that enable the click on image to skip the SplashActivity (after MAX_WAIT the SplashActivity terms anyway)
     */
    private static final long MIN_WAIT_INTERVAL=1500L;
    private static final long MAX_WAIT_INTERVAL=3000L;
    private static final int GO_AHEAD_WHAT=1; //status that the handler must evaluate to let goAHead() method run

    //Gestione dello stato dell'applicazione:
    private static final String IS_DONE_KEY="com.fstiva.ugho.key.IS_DONE_KEY";
    private static final String START_TIME_KEY="com.fstiva.ugho.key.START_TIME_KEY";

    private long mStartTime=-1;
    private boolean mIsDone;
    private UiHandler mHandler;

    private static class UiHandler extends Handler {
        /*
         * To avoid memory leak, that the garbage collector can't evaluate, i use a weak reference between the handler and the activity
         */
        private WeakReference<SplashActivity> mActivityRef;
        public UiHandler(final SplashActivity srcActivity) {
            this.mActivityRef=new WeakReference<SplashActivity>(srcActivity);
        }
        @Override
        public void handleMessage(Message message){
            final SplashActivity srcActivity=this.mActivityRef.get();
            if(srcActivity==null)
            {
                return;
            }
            switch(message.what) {
                case GO_AHEAD_WHAT:
                    long elapsedTime= SystemClock.uptimeMillis();
                    if(elapsedTime >= MIN_WAIT_INTERVAL && !srcActivity.mIsDone){
                        srcActivity.mIsDone=true;
                        srcActivity.goAhead();
                    }
                    break;
            }

        }
    }

    public void goAhead() {
        Intent intent=new Intent(this,LocatorSelector.class);
        startActivity(intent);
        finish(); //finish() method terms the activity, so we can't go back to this layout by pressing the "Back" button
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(savedInstanceState!=null){
            this.mStartTime=savedInstanceState.getLong(START_TIME_KEY);
        }
        mHandler= new UiHandler(this);
        final ImageView logoImageView= (ImageView) findViewById(R.id.splash_imageView);
        logoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long elapsedTime= SystemClock.uptimeMillis()-mStartTime;
                if(elapsedTime>= MIN_WAIT_INTERVAL && !mIsDone){
                    mIsDone=true;
                    goAhead();
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //Check that it's really the first launch, otherwise i send the message with the old value
        if(mStartTime==-1){
            mStartTime= SystemClock.uptimeMillis();
        }
        mStartTime= SystemClock.uptimeMillis(); //set the application start time at System time value
        final Message goAheadMessage=mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage,mStartTime+MAX_WAIT_INTERVAL); //After waiting for max time, mStartTime+MAX_WAIT_INTERVALg.
    }
    //Saving status when necessary to save (e.g. putting the app in background/rotating screen)
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_DONE_KEY,mIsDone);
        outState.putLong(START_TIME_KEY,mStartTime);
    }
    //Restoring the variable mIsDone, to keep track of the spent time
    @Override
    public void onRestoreInstanceState(Bundle savedInstance){
        super.onRestoreInstanceState(savedInstance);
        this.mIsDone=savedInstance.getBoolean(IS_DONE_KEY);
    }
}
