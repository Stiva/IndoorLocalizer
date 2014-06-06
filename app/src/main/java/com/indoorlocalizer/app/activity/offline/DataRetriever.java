package com.indoorlocalizer.app.activity.offline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.indoorlocalizer.app.R;

public class DataRetriever extends ActionBarActivity {
    private Intent scanService;
    private int rpValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_retriever);
        final Button dataRetrieveButton=(Button) findViewById(R.id.data_retriving_button);
        final EditText rpValueText=(EditText)findViewById(R.id.rp_id_editText);

        rpValueText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                // you can call or do what you want with your EditText here
                rpValue=Integer.parseInt(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        scanService=new Intent(this.getApplicationContext(),ScannerService.class);
        scanService.putExtra("rpID",rpValue);
        dataRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanReferencePoint();
            }
        });
        final Button stopRetrieveButton=(Button) findViewById(R.id.stop_rp_button);
        stopRetrieveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                stopRetrieveService();
            }
        });
    }

    private void scanReferencePoint(){
        startService(scanService);
    }
    public void stopRetrieveService(){
        stopService(scanService);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_retriver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
