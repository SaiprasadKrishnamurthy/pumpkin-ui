package com.sai.pumpkin.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.pumpkin.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
        String url = "http://10.126.219.143:9990/releases";
        List response = restTemplate.getForObject(url, List.class);
        return (List<ReleaseArtifact>) response.stream().map(r -> MAPPER.convertValue(r, ReleaseArtifact.class)).collect(toList());
    }

    public ReleaseDiffResponse diffReleases(String from, String to) {
        String url = "http://10.126.219.143:9990/release-diff?releaseCoordinates1=%s&releaseCoordinates2=%s";
        Map response = restTemplate.getForObject(String.format(url, from, to), Map.class);
        return MAPPER.convertValue(response, ReleaseDiffResponse.class);
    }

    public ViewSourceResponse source(final ViewSourceRequest viewSourceRequest) {
        String url = "http://10.126.219.143:9990/source?filePath=%s&groupId=%s&artifactId=%s&gitRevision=%s";
        Map response = restTemplate.getForObject(String.format(url, viewSourceRequest.getFilePath(), viewSourceRequest.getGroupId(), viewSourceRequest.getArtifactId(), viewSourceRequest.getOldGit() == null ? viewSourceRequest.getNewGit() : viewSourceRequest.getOldGit()), Map.class);
        return MAPPER.convertValue(response, ViewSourceResponse.class);
    }

    public String diffSource(String groupId, String artifactId, String filePath, String oldGit, String newGit) {
        String url = "http://10.126.219.143:9990/diffSource?filePath=%s&groupId=%s&artifactId=%s&gitRevisionFrom=%s&gitRevisionTo=%s";
        Map response = restTemplate.getForObject(String.format(url, filePath, groupId, artifactId, oldGit, newGit), Map.class);
        return response.get("diff").toString();

    }

    public List<GitLogEntry> detailedCommits(String from, String to, String committersCsv) {
        String url = "http://10.126.219.143:9990/detailedcommits?releaseCoordinates1=%s&releaseCoordinates2=%s&committersCsv=%s";
        System.out.println(String.format(url, from, to, committersCsv.trim()));
        List response = restTemplate.getForObject(String.format(url, from, to, committersCsv.trim()), List.class);
        return (List<GitLogEntry>) response.stream().map(r -> MAPPER.convertValue(r, GitLogEntry.class)).collect(toList());
    }

    public String bulkDiffSource(String groupId, String artifactId, String gitRevision) {
        String url = "http://10.126.219.143:9990/bulkDiff?groupId=%s&artifactId=%s&gitRevision=%s";
        Map response = restTemplate.getForObject(String.format(url, groupId, artifactId, gitRevision), Map.class);
        return response.get("diff").toString();
    }

    public List<MavenGitVersionMapping> allArtifacts(final String artifactId) {
        String url = "http://10.126.219.143:9990/artifacts?artifactId=%s";
        List response = restTemplate.getForObject(String.format(url, artifactId.trim()), List.class);
        return (List<MavenGitVersionMapping>) response.stream().map(r -> MAPPER.convertValue(r, MavenGitVersionMapping.class)).collect(toList());
    }

    public ReleaseDiffResponse diffArtifacts(String from, String to) {
        String url = "http://10.126.219.143:9990/artifact-diff?mavenCoordinates1=%s&mavenCoordinates2=%s";
        System.out.println(String.format(url, from, to));
        Map response = restTemplate.getForObject(String.format(url, from, to), Map.class);
        return MAPPER.convertValue(response, ReleaseDiffResponse.class);
    }

    public List<GitLogResponse> changes(final long timestamp) {
        String url = "http://10.126.219.143:9990/changes?timestamp=%s";
        System.out.println(String.format(url, timestamp));
        List response = restTemplate.getForObject(String.format(url, timestamp), List.class);
        return (List<GitLogResponse>) response.stream().map(r -> MAPPER.convertValue(r, GitLogResponse.class)).collect(toList());
    }

    public Map<String, Integer> activitySince(final long twentyDaysAgo) {
        String url = "http://10.126.219.143:9990/activity?sinceTimestamp=%s";
        System.out.println(String.format(url, twentyDaysAgo));
        Map response = restTemplate.getForObject(String.format(url, twentyDaysAgo), Map.class);
        return response;

    }

    public Map<String, Integer> committerActivitySince(long twentyDaysAgo) {
        String url = "http://10.126.219.143:9990/committer-activities?sinceTimestamp=%s";
        System.out.println(String.format(url, twentyDaysAgo));
        Map response = restTemplate.getForObject(String.format(url, twentyDaysAgo), Map.class);
        return response;
    }

    public List<ArtifactConfig> registered() {
        String url = "http://10.126.219.143:9990/configs";
        List response = restTemplate.getForObject(String.format(url), List.class);
        return (List<ArtifactConfig>) response.stream().map(r -> MAPPER.convertValue(r, ArtifactConfig.class)).collect(toList());


    }

    public ReleaseMetadata releaseMeta(final String from) {
        String name = from.split(":")[0];
        String version = from.split(":")[1];
        String url = "http://10.126.219.143:9990/release-meta?version=%s&name=%s";
        Map response = restTemplate.getForObject(String.format(url, version, name), Map.class);
        return MAPPER.convertValue(response, ReleaseMetadata.class);
    }

    public List<ReleaseExpectation> allTests() {
        String url = "http://10.126.219.143:9990/tests";
        List response = restTemplate.getForObject(String.format(url), List.class);
        return (List<ReleaseExpectation>) response.stream().map(r -> MAPPER.convertValue(r, ReleaseExpectation.class)).collect(toList());
    }

    public String execTest(final String name) {
        String url = "http://10.126.219.143:9990/testresult?testName=%s";
        String response = restTemplate.getForObject(String.format(url, name), String.class);
        return response;
    }

    public void saveTest(final ReleaseExpectation r) {
        String url = "http://10.126.219.143:9990/test";
        HttpEntity<ReleaseExpectation> entity = new HttpEntity<>(r);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
    }

    public String testTemplate() {
        String url = "http://10.126.219.143:9990/testtemplate";
        String response = restTemplate.getForObject(String.format(url), String.class);
        return response;

    }

    public Map<String, Integer> commitTrends(long startDate) {
        String url = "http://10.126.219.143:9990/commitsTrend?sinceTimestamp=%s";
        System.out.println(String.format(url, startDate));
        Map<String,Integer> response = restTemplate.getForObject(String.format(url, startDate), Map.class);
        return response;
    }
}
