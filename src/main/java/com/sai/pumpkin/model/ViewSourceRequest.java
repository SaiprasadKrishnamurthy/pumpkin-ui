package com.sai.pumpkin.model;

import lombok.Data;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class ViewSourceRequest {
    private String filePath;
    private String oldGit;
    private String newGit;
    private String groupId;
    private String artifactId;

}
