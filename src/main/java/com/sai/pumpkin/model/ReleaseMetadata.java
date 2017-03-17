package com.sai.pumpkin.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saipkri on 18/03/17.
 */
@Data
public class ReleaseMetadata {
    private String releaseName;
    private String version;
    private List<ArtifactCollection> artifacts = new ArrayList<>();
}
