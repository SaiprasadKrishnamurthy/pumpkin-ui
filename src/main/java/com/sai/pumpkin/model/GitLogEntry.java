package com.sai.pumpkin.model;

import com.sai.pumpkin.controller.TeamController;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;

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
    private String defects;

    public String getDefects() {
        StringBuilder out = new StringBuilder();
        Matcher matcher = TeamController.defectIdPattern.matcher(commitMessage);
        System.out.println(commitMessage);
        while (matcher.find()) {
            out.append(matcher.group() + ", ");
        }
        if (out.length() == 0) {
            return null;
        }
        return out.toString();
    }

}
