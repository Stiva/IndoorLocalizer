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
import com.indoorlocalizer.app.LocatorSelector;
import com.indoorlocalizer.app.R;

import java.lang.ref.WeakReference;

/**
 * Created by federicostivani on 08/11/13.
 */
public class SplashActivity extends Activity {

    private static final long MIN_WAIT_INTERVAL=1500L;
    private static final long MAX_WAIT_INTERVAL=3000L;
    private static final int GO_AHEAD_WHAT=1; //status che l'handler deve interpretare per far partire il metodo goAhead();

    //Gestione dello stato dell'applicazione:
    private static final String IS_DONE_KEY="com.fstiva.ugho.key.IS_DONE_KEY";
    private static final String START_TIME_KEY="com.fstiva.ugho.key.START_TIME_KEY";

    private long mStartTime=-1;
    private boolean mIsDone;
    private UiHandler mHandler;

    private static class UiHandler extends Handler {
        /* Per evitare un eccessivo memory leak, che il garbage collector non riesce a ben interpretare utilizzo un referenziamento weak fra l'handler
         * e l'attivita'. Questa modifica disaccoppia notevolmente l'Activity dall'handler, che deve comunque avviarla in fase di "creazione".
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
        finish(); //siccome non vogliamo che da una schermata successiva si torni alla splash invochiamo finish() che termina l'activity SplashActivity.
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
        //Controllo effettivamente che sia il primo avvio, altrimenti mando il messaggio con il vecchio valore
        if(mStartTime==-1){
            mStartTime= SystemClock.uptimeMillis();
        }
        mStartTime= SystemClock.uptimeMillis(); //setto il tempo di start dell'app all'uptime del dispositivo
        final Message goAheadMessage=mHandler.obtainMessage(GO_AHEAD_WHAT); // preparo il messaggio da mandare all'handler per attivarlo sulla procedura corretta da richiamare
        mHandler.sendMessageAtTime(goAheadMessage,mStartTime+MAX_WAIT_INTERVAL); //quando il tempo massimo e' trascorso, ovvero a mStartTime+MAX_WAIT_INTERVAL faccio scattare automaticamente il msg.
    }
    //Salvataggio dello stato delle variabili
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_DONE_KEY,mIsDone);
        outState.putLong(START_TIME_KEY,mStartTime);
    }
    //Ripristino della variabile mIsDone
    @Override
    public void onRestoreInstanceState(Bundle savedInstance){
        super.onRestoreInstanceState(savedInstance);
        this.mIsDone=savedInstance.getBoolean(IS_DONE_KEY);
    }
}
