package com.indoorlocalizer.app.activity.common.xml;

import android.util.Xml;

import com.indoorlocalizer.app.activity.common.model.OptionElement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Federico on 09/06/2014.
 */
public class XmlParser {
    private static final String ns = null;
    public ArrayList<OptionElement> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readMenu(parser);
        } finally {
            in.close();
        }
    }
    private ArrayList<OptionElement> readMenu(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<OptionElement> entries = new ArrayList<OptionElement>();

        parser.require(XmlPullParser.START_TAG, ns, "menu");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("option")) {
                entries.add(readEntry(parser));
            }
        }
        return entries;
    }

    private OptionElement readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "option");
        String name = null;
        String description = null;
        String iconPath = null;
        String name2="";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String mTag = parser.getName();
            if (mTag.equals("name")) {
                name = readName(parser);
            } else if (mTag.equals("description")) {
                description = readDescription(parser);
            } else if (mTag.equals("iconPath")) {
                iconPath = readIconPath(parser);
            } else {
                //skip(parser);
            }
        }
        return new OptionElement(name, description, iconPath);
    }

    // Processes name tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String link=readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return link;
    }

    // Processes iconPath tags in the feed.
    private String readIconPath(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "iconPath");
        String icPath = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "iconPath");
        return icPath;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
