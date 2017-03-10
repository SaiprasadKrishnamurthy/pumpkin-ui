package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by saipkri on 08/03/17.
 */
@Data
public class ReleaseArtifact implements Serializable {
    private String id;
    private String name;
    private String version;
    private List<MavenCoordinates> mavenArtifacts;
}
