package com.indoorlocalizer.app.activity.online;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;

import java.sql.SQLException;

public class Localization extends Activity implements AdapterView.OnItemSelectedListener {
    private Cursor mCursor;
    private SimpleCursorAdapter mAdapter;
    private static final String[] FROM = {  DatabaseHelper.KEY_MAP_NAME, DatabaseHelper.KEY_ID,
            DatabaseHelper.KEY_NUMBER_OF_RP,
            DatabaseHelper.KEY_MAP_IMAGE_PATH};

    private static final int[] TO = {android.R.id.text1};
    private Spinner spinner;
    private String mapName;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        spinner = (Spinner) findViewById(R.id.map_chooser);
        final Intent localizerServiceIntent=new Intent(this.getApplicationContext(),LocalizerService.class);
        DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            //dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        if(mCursor.getCount()>0){
            mAdapter= new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, mCursor, FROM, TO, 0);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(mAdapter);
        }
        Button localizeButton=(Button)findViewById(R.id.localize_button);
        localizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor temp=(Cursor)spinner.getSelectedItem();
                mapName=temp.getString(temp.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME));
                localizerServiceIntent.putExtra("mapName",mapName);
                startService(localizerServiceIntent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //TODO: qui non entra... perché??
        mapName=spinner.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
