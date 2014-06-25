package com.indoorlocalizer.app.activity.common.model;

/**
 * Created by federicostivani on 07/06/14.
 */
public class OptionElement {
    private String optionName;
    private String optionDescription;
    private String optionIconPath;

    public OptionElement(String optionName, String optionDescription, String optionIcon) {
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.optionIconPath = optionIcon;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public String getOptionIconPath() {
        return optionIconPath;
    }
}
