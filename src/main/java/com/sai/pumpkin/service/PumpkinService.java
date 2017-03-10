package com.sai.pumpkin.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.pumpkin.model.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 09/03/17.
 */
public class PumpkinService {

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<ReleaseArtifact> allReleases() {
        String url = "http://localhost:9990/releases";
        List response = restTemplate.getForObject(url, List.class);
        return (List<ReleaseArtifact>) response.stream().map(r -> MAPPER.convertValue(r, ReleaseArtifact.class)).collect(toList());
    }

    public ReleaseDiffResponse diffReleases(String from, String to) {
        String url = "http://localhost:9990/release-diff?releaseCoordinates1=%s&releaseCoordinates2=%s";
        Map response = restTemplate.getForObject(String.format(url, from, to), Map.class);
        return MAPPER.convertValue(response, ReleaseDiffResponse.class);
    }

    public ViewSourceResponse source(final ViewSourceRequest viewSourceRequest) {
        String url = "http://localhost:9990/source?filePath=%s&groupId=%s&artifactId=%s&gitRevision=%s";
        Map response = restTemplate.getForObject(String.format(url, viewSourceRequest.getFilePath(), viewSourceRequest.getGroupId(), viewSourceRequest.getArtifactId(), viewSourceRequest.getOldGit() == null ? viewSourceRequest.getNewGit() : viewSourceRequest.getOldGit()), Map.class);
        return MAPPER.convertValue(response, ViewSourceResponse.class);
    }

    public String diffSource(String groupId, String artifactId, String filePath, String oldGit, String newGit) {
        String url = "http://localhost:9990/diffSource?filePath=%s&groupId=%s&artifactId=%s&gitRevisionFrom=%s&gitRevisionTo=%s";
        Map response = restTemplate.getForObject(String.format(url, filePath, groupId, artifactId, oldGit, newGit), Map.class);
        return response.get("diff").toString();

    }

    public List<GitLogEntry> detailedCommits(String from, String to, String committersCsv) {
        String url = "http://localhost:9990/detailedcommits?releaseCoordinates1=%s&releaseCoordinates2=%s&committersCsv=%s";
        System.out.println(String.format(url, from, to, committersCsv.trim()));
        List response = restTemplate.getForObject(String.format(url, from, to, committersCsv.trim()), List.class);
        return (List<GitLogEntry>) response.stream().map(r -> MAPPER.convertValue(r, GitLogEntry.class)).collect(toList());
    }

    public String bulkDiffSource(String groupId, String artifactId, String gitRevision) {
        String url = "http://localhost:9990/bulkDiff?groupId=%s&artifactId=%s&gitRevision=%s";
        Map response = restTemplate.getForObject(String.format(url, groupId, artifactId, gitRevision), Map.class);
        return response.get("diff").toString();
    }
}
