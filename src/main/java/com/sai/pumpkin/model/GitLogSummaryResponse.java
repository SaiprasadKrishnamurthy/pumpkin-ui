package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
public class GitLogSummaryResponse implements Serializable {
    private String id;
    private MavenGitVersionMapping from;
    private MavenGitVersionMapping to;
    private Map<String, Set<ChangeSetEntry>> authorsToChangeSet = new HashMap<>();
}
