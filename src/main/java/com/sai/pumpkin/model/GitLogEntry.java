package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class GitLogEntry implements Serializable {
    private String id;
    private String uuid;
    private String revision;
    private String author;
    private String dateTime;
    private String commitMessage;
    private List<ChangeSetEntry> changes;
    private MavenCoordinates mavenCoordinates;
    private List<String> changeUUIDs;
    private long timestamp;

}
