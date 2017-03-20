package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class ReleaseExpectation implements Serializable {
    public String id;
    private String name;
    private String featureText;
    private String failureSlackWebhook;
}
