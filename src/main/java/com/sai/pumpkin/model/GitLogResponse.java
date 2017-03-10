package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class GitLogResponse implements Serializable {
    private String id;
    private MavenCoordinates from;
    private MavenCoordinates to;
    private List<GitLogEntry> gitLogEntries;
    private List<String> gitLogUUIDs;
}
