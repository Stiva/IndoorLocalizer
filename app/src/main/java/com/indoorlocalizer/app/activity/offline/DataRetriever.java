package com.indoorlocalizer.app.activity.offline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.indoorlocalizer.app.R;

public class DataRetriever extends ActionBarActivity {
    private Intent scanService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_retriever);
        final Button dataRetrieveButton=(Button) findViewById(R.id.data_retriving_button);
        final EditText rpValueText=(EditText)findViewById(R.id.rp_id_editText);
        int rpValue=Integer.parseInt(rpValueText.getText().toString());
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
