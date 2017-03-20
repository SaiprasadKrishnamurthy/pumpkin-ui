package com.sai.pumpkin.model;

import lombok.Data;

/**
 * Created by saipkri on 20/03/17.
 */
@Data
public class ReleaseExpectationResult {
    private String id;
    private String testName;
    private long executionDateTime;
    private String htmlReport;
    private boolean failure;
}
