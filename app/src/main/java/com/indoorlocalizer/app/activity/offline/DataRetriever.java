package com.indoorlocalizer.app.activity.offline;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.SettingsActivity;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.offline.utils.OfflineUtils;

import java.sql.SQLException;
import java.util.ArrayList;

public class DataRetriever extends Activity implements InsertMapNameDialog.InsertMapNameDialogListener{
    private Intent scanService;
    private String rpValue;
    private String mapName;
    private String imageFilePath= "map_default_icon.png";
    private ImageView imagePreview;
    private ProgressDialog barProgressDialog;
    private Handler updateBarHandler;
    private Cursor mCursor;
    private ArrayList<String> optionNames;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner mapSpinner;
    private static final int PICK_IMAGE = 1;
    private DbManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_retriever);
        final Button dataRetrieveButton = (Button) findViewById(R.id.data_retrieving_button);
        final EditText rpValueText = (EditText)findViewById(R.id.rp_name_editText);
        imagePreview=(ImageView)findViewById(R.id.map_image_preview);
        mapSpinner = (Spinner)findViewById(R.id.map_chooser_retrieve);
        optionNames = new ArrayList<String>();
        dbManager = new DbManager(getApplicationContext());
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);

        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            while(mCursor.moveToNext()){
                optionNames.add(mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            mCursor.close();
        }
        optionNames.add("<Create a new Map>");
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,optionNames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapSpinner.setAdapter(arrayAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().toString().equals("<Create a new Map>")){
                    InsertMapNameDialog dialog=new InsertMapNameDialog();
                    dialog.show(getFragmentManager(), "Insert map name");
                }else{
                    try{
                        dbManager.open();
                        imageFilePath = dbManager.getImagePath(adapterView.getSelectedItem().toString());
                        Bitmap image= BitmapFactory.decodeFile(imageFilePath);
                        if(image.getHeight()>4096 || image.getWidth()>4096)
                            image=Bitmap.createScaledBitmap(image,4096,4096,false);
                        imagePreview.setImageBitmap(image);
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgress(0);
        updateBarHandler=new Handler();
        scanService=new Intent(this.getApplicationContext(),ScannerService.class);
        dataRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mapSpinner.getSelectedItem().toString().isEmpty()) {
                    if (!rpValueText.getText().toString().isEmpty()) {
                        mapName=mapSpinner.getSelectedItem().toString();
                        rpValue = rpValueText.getText().toString();
                        scanService.putExtra("rpName", rpValue);
                        scanService.putExtra("mapName", mapName.toUpperCase());
                        scanService.putExtra("mapImage",imageFilePath);
                        scanReferencePoint();
                    } else {
                        Toast.makeText(getApplicationContext(), "You've to select a valid name for RP", Toast.LENGTH_SHORT).show();
                        rpValueText.setBackgroundColor(Color.CYAN);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You've to select a valid name for your map", Toast.LENGTH_SHORT).show();
                    rpValueText.setBackgroundColor(getResources().getColor(R.color.dat_yellow));
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
   /* @Override
    protected void onRestart(){
        //restoreManagedDialogs(savedInstanceState);
    }*/
    private void scanReferencePoint(){
        startService(scanService);
        launchBarDialog();
    }
    public void stopRetrieveService(){
        try{
            stopService(scanService);
            Toast.makeText(this, "Scanning aborted by the user", Toast.LENGTH_LONG).show();
        } catch (NullPointerException e){
            Toast.makeText(this, "No service to stop", Toast.LENGTH_LONG).show();
        }
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
            Intent intent=new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri _uri = data.getData();
            //User had pick an image.
            Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            //Link to the image
            imageFilePath = cursor.getString(0);
            cursor.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void launchBarDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int scanNumber = Integer.parseInt(prefs.getString("scan_number", "0"));
        final int  durationMS = Integer.parseInt(prefs.getString("duration_ms","0"));
        barProgressDialog.setTitle("Generating Reference point ...");
        barProgressDialog.setMessage("Scan in progress ...");
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setMax(scanNumber);
        barProgressDialog.setCancelable(false);
        barProgressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                        Thread.sleep(durationMS);
                        updateBarHandler.post(new Runnable() {
                            public void run() {
                                barProgressDialog.incrementProgressBy(1);
                            }
                        });
                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            if(ScannerService.finish) {
                                barProgressDialog.dismiss();
                                finish();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText edit=(EditText)dialog.getDialog().findViewById(R.id.map_name_editText);
        String mapName=edit.getText().toString();
        OfflineUtils.insertNewMap(this,mapName.toUpperCase(),imageFilePath);
        arrayAdapter.clear();
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            while(mCursor.moveToNext()){
                optionNames.add(mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            mCursor.close();
        }
        optionNames.add("<Create a new Map>");
        arrayAdapter.notifyDataSetChanged();
        mapSpinner.setSelection(optionNames.size()-2);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        mapSpinner.setSelection(0);
        dialog.dismiss();
    }
    @Override
    public void onButtonClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
}
