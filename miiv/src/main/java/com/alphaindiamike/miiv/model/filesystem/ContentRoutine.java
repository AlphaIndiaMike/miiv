package com.alphaindiamike.miiv.model.filesystem;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ContentRoutine {
    VERSION_CONTROLLED_BY_DATE,
    VERSION_CONTROLLED_BY_DATE_AND_TYPE,
    VERSION_CONTROLLED_BY_DATE_AND_TITLE;
    
    @JsonCreator
    public static ContentRoutine forValue(String value) {
        switch (value) {
            case "version_controlled_by_date":
                return VERSION_CONTROLLED_BY_DATE;
            case "version_controlled_by_date_and_type":
                return VERSION_CONTROLLED_BY_DATE_AND_TYPE;
            case "version_controlled_by_date_and_title":
                return VERSION_CONTROLLED_BY_DATE_AND_TITLE;
            default:
                throw new IllegalArgumentException("Unknown content routine: " + value);
        }
    }
}
