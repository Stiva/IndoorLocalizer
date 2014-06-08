package com.indoorlocalizer.app.activity.common.model;

import android.graphics.drawable.Drawable;

/**
 * Created by federicostivani on 07/06/14.
 */
public class OptionElement {
    private String optionName;
    private String optionDescription;
    private Drawable optionIcon;

    public OptionElement(String optionName, String optionDescription, Drawable optionIcon) {
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.optionIcon = optionIcon;
    }
}
