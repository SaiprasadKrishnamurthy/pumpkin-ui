package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class MavenGitVersionMapping implements Serializable {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyy'@'HH:mm");
    public String id;
    private ArtifactConfig artifactConfig;
    private MavenCoordinates mavenCoordinates;
    private String gitRevision;
    private long timestamp;
    private String displayDate;

    public String getDisplayDate() {
        return dateFormat.format(new Date(timestamp));
    }

}
