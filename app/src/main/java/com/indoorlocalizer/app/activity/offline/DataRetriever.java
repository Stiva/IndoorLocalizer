package com.indoorlocalizer.app.activity.offline;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.indoorlocalizer.app.R;

public class DataRetriever extends ActionBarActivity {
    private Intent scanService;
    private int rpValue;
    private String mapName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_retriever);
        final Button dataRetrieveButton=(Button) findViewById(R.id.data_retrieving_button);
        final EditText rpValueText=(EditText)findViewById(R.id.rp_id_editText);
        final EditText mapNameValueText=(EditText)findViewById(R.id.map_name_editText);
        scanService=new Intent(this.getApplicationContext(),ScannerService.class);
        dataRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mapNameValueText.getText().toString().isEmpty()) {
                    if (!rpValueText.getText().toString().isEmpty()) {
                        mapName=mapNameValueText.getText().toString();
                        rpValue = Integer.parseInt(rpValueText.getText().toString());
                        scanService.putExtra("rpID", rpValue);
                        scanService.putExtra("mapName", mapName.toUpperCase());
                        scanReferencePoint();
                    } else {
                        Toast.makeText(getApplicationContext(), "You've to select a valid ID for RP", Toast.LENGTH_SHORT).show();
                        rpValueText.setBackgroundColor(Color.CYAN);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You've to select a valid name for your map", Toast.LENGTH_SHORT).show();
                    rpValueText.setBackgroundColor(Color.CYAN);
                }
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
