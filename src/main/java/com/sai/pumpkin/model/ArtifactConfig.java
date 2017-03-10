package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class ArtifactConfig implements Serializable {
    public String id;
    private String name;
    private String repoName;
    private String repoUrl;
    private String pomPath;
    private String branch;

    public String moduleDir() {
        return pomPath.replace("/pom.xml", "");
    }

}
