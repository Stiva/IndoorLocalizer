package com.indoorlocalizer.app.activity.offline;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
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
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.utils.CommonUtils;
import com.indoorlocalizer.app.activity.offline.utils.OfflineUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class DataRetriever2 extends ActionBarActivity implements InsertMapNameDialog.InsertMapNameDialogListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_retriever2);
        final Button dataRetrieveButton = (Button) findViewById(R.id.data_retrieving_button);
        final EditText rpValueText = (EditText)findViewById(R.id.rp_name_editText);
        mapSpinner = (Spinner)findViewById(R.id.map_chooser_retrieve);
        optionNames = new ArrayList<String>();
        final DbManager dbManager = new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            //dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        while(mCursor.moveToNext()){
            optionNames.add(mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)));
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
                        Bitmap resizedImage = Bitmap.createScaledBitmap(image, 600, 600, true);
                        imagePreview.setImageBitmap(resizedImage);
                        //dbManager.close();
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
        imagePreview=(ImageView)findViewById(R.id.map_image_preview);
        try {
            Drawable dr = Drawable.createFromStream(getAssets().open("map_default_icon.png"), null);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 600, 600, true));
            imagePreview.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
   /* @Override
    protected void onRestart(){
        //restoreManagedDialogs(savedInstanceState);
    }*/
    private void scanReferencePoint(){
        startService(scanService);
        launchBarDialog();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri _uri = data.getData();

            //User had pick an image.
            Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();

            //Link to the image
            imageFilePath = cursor.getString(0);
            if (imageFilePath==null) {
                imageFilePath= "map_default_icon.png";
                Drawable dr;
                try {
                    dr = Drawable.createFromStream(getAssets().open("map_default_icon.png"), null);
                    Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 600, 600, true));
                    imagePreview.setImageDrawable(d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Bitmap image= BitmapFactory.decodeFile(imageFilePath);
                Bitmap resizedImage = Bitmap.createScaledBitmap(image, 600, 600, true);
                imagePreview.setImageBitmap(resizedImage);
            }
            cursor.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void launchBarDialog() {
        barProgressDialog.setTitle("Generating Reference point ...");
        barProgressDialog.setMessage("Scan in progress ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setMax(CommonUtils.scanNumber);
        barProgressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                        Thread.sleep(CommonUtils.durationMS);
                        updateBarHandler.post(new Runnable() {
                            public void run() {
                                barProgressDialog.incrementProgressBy(1);
                            }
                        });
                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();
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
        OfflineUtils.insertNewMap(this,mapName,imageFilePath);
        arrayAdapter.clear();
        DbManager dbManager = new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            //dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        while(mCursor.moveToNext()){
            optionNames.add(mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)));
        }
        optionNames.add("<Create a new Map>");
        arrayAdapter.notifyDataSetChanged();
        mapSpinner.setSelection(optionNames.size()-2);

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
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
