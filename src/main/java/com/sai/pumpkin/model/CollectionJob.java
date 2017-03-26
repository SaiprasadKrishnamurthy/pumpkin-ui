package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by saipkri on 26/03/17.
 */
@Data
public class CollectionJob implements Serializable {
    private String id;
    private String configName;
    private long startTime;
    private long endTime;
    private long totalTime;

    public String getStart() {
        return new Date(startTime).toString();
    }

    public String getEnd() {
        return new Date(endTime).toString();
    }

    public double getTotal() {
        return (endTime - startTime) / 1000L;
    }


}
