package com.sai.pumpkin.model;

import lombok.Data;

/**
 * Created by saipkri on 18/03/17.
 */
@Data
public class ArtifactCollection {
    private MavenCoordinates mavenCoordinates;
    private ArtifactCollectionStatusType status;
}
