package com.indoorlocalizer.app.activity.common.utils;

import android.content.Context;

import com.indoorlocalizer.app.activity.common.model.OptionElement;
import com.indoorlocalizer.app.activity.common.xml.XmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    //TODO: portare le costanti su un file di configurazione esterno
    public static int scanNumber=12;
    public static double tolerance=70.00;
    public static int durationMS=10000;

    public static void copy(InputStream src, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = src.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        src.close();
        out.close();
    }
    /*
    * You can easy manage the menu by adding a new tag to optionsMenu.xml contained in assets folder
    */
    public static void createOptionListXML(Context context,String filename,List<Map<String, Object>> mModel){
        ArrayList<OptionElement> results=new ArrayList<OptionElement>();
        try {
            XmlParser parser=new XmlParser();
            InputStream in_s = context.getAssets().open(filename);
            results = parser.parse(in_s);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!results.isEmpty()){
            for(OptionElement element:results){
                HashMap<String,Object> temp=new HashMap<String, Object>();
                temp.put("option_name",element.getOptionName());
                temp.put("option_description",element.getOptionDescription());
                mModel.add(temp);
            }
        }
    }
}
