package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class MavenGitVersionMapping implements Serializable {
    public String id;
    private ArtifactConfig artifactConfig;
    private MavenCoordinates mavenCoordinates;
    private String gitRevision;
    private long timestamp;
}
