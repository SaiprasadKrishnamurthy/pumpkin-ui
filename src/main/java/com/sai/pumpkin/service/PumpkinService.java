package com.sai.pumpkin.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.pumpkin.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public List<GitLogResponse> changeWithinRange(final long from, final long until) {
        String url = "http://10.126.219.143:9990/changes?fromTimestamp=%s&untilTimestamp=%s";
        System.out.println(String.format(url, from, until));
        List response = restTemplate.getForObject(String.format(url, from, until), List.class);
        return (List<GitLogResponse>) response.stream().map(r -> MAPPER.convertValue(r, GitLogResponse.class)).collect(toList());
    }

    public List<GitLogResponse> changeRelative(final long scale, final TimeUnit unit) {
        String url = "http://10.126.219.143:9990/changes?relativeTime=%s&relativeTimeUnit=%s";
        System.out.println(String.format(url, scale, unit.toString()));
        List response = restTemplate.getForObject(String.format(url, scale, unit.toString()), List.class);
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
        Map<String, Integer> response = restTemplate.getForObject(String.format(url, startDate), Map.class);
        return response;
    }

    public List<CollectionJob> collectedJobs() {
        String url = "http://10.126.219.143:9990/collection-job-stats";
        List response = restTemplate.getForObject(url, List.class);
        return (List<CollectionJob>) response.stream().map(r -> MAPPER.convertValue(r, CollectionJob.class)).collect(toList());
    }

    public void collectAll() {
        String url = "http://10.126.219.143:9990/collectall";
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(new HashMap<>()), Map.class);
    }

    public void clearCache() {
        String url = "http://10.126.219.143:9990/cache-refresh";
        System.out.println(String.format(url));
        restTemplate.getForObject(String.format(url), Map.class);
    }

    public ReleaseDiffResponse diffSnapshots(String from, String to, int timeWindowInMinutes) {
        String url = "http://10.126.219.143:9990/snapshot-diff?releaseCoordinates1=%s&releaseCoordinates2=%s&snapshotGoBackUpToMinutes=%s";
        Map response = restTemplate.getForObject(String.format(url, from, to, timeWindowInMinutes), Map.class);
        return MAPPER.convertValue(response, ReleaseDiffResponse.class);
    }

    public List<Team> allTeams() {
        String url = "http://10.126.219.143:9990/teams";
        List<Map> response = restTemplate.getForObject(url, List.class);
        return response.stream().map(r -> MAPPER.convertValue(r, Team.class)).collect(toList());
    }

    public List<GitLogEntry> teamActivity(String from, String to, String teamName) {
        String url = "http://10.126.219.143:9990/teamstats?releaseCoordinates1=%s&releaseCoordinates2=%s&teamName=%s";
        System.out.println(String.format(url, from, to, teamName.trim()));
        List response = restTemplate.getForObject(String.format(url, from, to, teamName.trim()), List.class);
        return (List<GitLogEntry>) response.stream().map(r -> MAPPER.convertValue(r, GitLogEntry.class)).collect(toList());
    }

    public Map<String, Integer> repoActivities(long hours_ago) {
        String url = "http://10.126.219.143:9990/commit-counts-per-repo?sinceTimestamp=%s";
        System.out.println(String.format(url, hours_ago));
        Map<String, Integer> response = restTemplate.getForObject(String.format(url, hours_ago), Map.class);
        return response;
    }
}
