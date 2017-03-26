package com.sai.pumpkin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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
    private long noOfFilesChanged;
    private long noOfLinesInserted;
    private long noOfLinesDeleted;
    private Set<String> defectIds;
    private Set<String> featureIds;
    private List<PullRequest> pullRequests;
}
