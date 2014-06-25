package com.indoorlocalizer.app.activity.offline;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.offline.fragment.ShowWifiFragment;


public class ShowWifiList extends FragmentActivity implements InsertMapNameDialog.InsertMapNameDialogListener {
    private static final int PICK_IMAGE = 1;
    private ShowWifiFragment showWifiFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_wifi_list);
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_wifi, menu);
        return true;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_list_option:
                Toast.makeText(this, "Refresh in action", Toast.LENGTH_SHORT).show();
                showWifiFragment = (ShowWifiFragment) getFragmentManager().findFragmentById(R.id.show_wifi_fragment);
                showWifiFragment.refresh();
                return true;
            case R.id.save_fingerprint_option:
                InsertMapNameDialog dialog = new InsertMapNameDialog();
                dialog.show(getFragmentManager(), "Insert map name");
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText edit = (EditText) dialog.getDialog().findViewById(R.id.map_name_editText);
        String mapName = edit.getText().toString();
        showWifiFragment.saveFingerprint(this, mapName);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri _uri = data.getData();

            //User had pick an image.
            Cursor cursor = getContentResolver().query(_uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();

            //Link to the image
            String imageFilePath = cursor.getString(0);
            if (imageFilePath == null) {
                imageFilePath = "map_default_icon.png";
            }
            cursor.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
