package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.*;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.*;
import org.springframework.util.StopWatch;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class DiffReleasesController {

    private List<ReleaseArtifact> releaseArtifacts;
    private List<ReleaseArtifact> snapshotArtifacts;
    private String from;
    private String to;
    private final PumpkinService pumpkinService = new PumpkinService();
    private ReleaseDiffResponse releaseDiffResponse;
    private List<ReleaseDiffDisplayBean> modified;
    private boolean renderModified;
    private ViewSourceRequest viewSourceRequest = new ViewSourceRequest();
    private ViewSourceResponse viewSourceResponse;
    private String one;
    private String two;
    private String committersCsv;
    private String ignoreFilesCsv = "pom.xml,tigerstripe.xml,org.eclipse.core.resources.prefs";
    private List<GitLogEntry> detailedCommits;
    private LineChartModel trend;
    private ReleaseMetadata fromColl;
    private ReleaseMetadata toColl;
    private int timeWindowStartinMinutes = 120;
    private int timeWindowEndinMinutes = 120;


    public DiffReleasesController() {
        List<ReleaseArtifact> releaseArtifacts = pumpkinService.allReleases();
        this.releaseArtifacts = releaseArtifacts.stream().filter(r -> !r.getName().contains("SNAPSHOT")).collect(Collectors.toList());
        Collections.reverse(this.releaseArtifacts);
        this.snapshotArtifacts = releaseArtifacts.stream().filter(r -> r.getName().contains("SNAPSHOT")).collect(Collectors.toList());
        Collections.reverse(this.snapshotArtifacts);

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        from = request.getParameter("from");
        to = request.getParameter("to");
        if (StringUtils.isNoneBlank(from, to)) {
            diff();
        }
        if (request.getParameter("snapshotDiff") != null) {
            if (this.snapshotArtifacts.size() > 1) {
                from = this.snapshotArtifacts.get(this.snapshotArtifacts.size() - 2).getName() + ":" + this.snapshotArtifacts.get(this.snapshotArtifacts.size() - 2).getVersion();
                to = this.snapshotArtifacts.get(this.snapshotArtifacts.size() - 1).getName() + ":" + this.snapshotArtifacts.get(this.snapshotArtifacts.size() - 1).getVersion();
                snapshotDiff();
            }
        }
    }

    public void diff() {
        releaseDiffResponse = pumpkinService.diffReleases(from, to);
        // Modified
        modified = releaseDiffResponse.getDiffs().stream().map(r -> new ReleaseDiffDisplayBean(from, to, r)).collect(toList());
        if (StringUtils.isNotBlank(ignoreFilesCsv)) {
            modified = modified.stream().filter(rdf ->
                    rdf.getFilesModified().stream().filter(file -> Stream.of(ignoreFilesCsv.split(",")).anyMatch(ig -> file.contains(ig))).count() < rdf.getFilesModified().size())
                    .collect(toList());
        }
        renderModified = true;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        fromColl = pumpkinService.releaseMeta(from);
        stopWatch.stop();
        System.out.println(" ---- " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        toColl = pumpkinService.releaseMeta(to);
        stopWatch.stop();
        System.out.println(" ---- " + stopWatch.getTotalTimeSeconds());
    }

    public void snapshotDiff() {
        releaseDiffResponse = pumpkinService.diffSnapshots(from, to, timeWindowStartinMinutes);
        // Modified
        modified = releaseDiffResponse.getDiffs().stream().map(r -> new ReleaseDiffDisplayBean(from, to, r)).collect(toList());
        renderModified = true;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        fromColl = pumpkinService.releaseMeta(from);
        stopWatch.stop();
        System.out.println(" ---- " + stopWatch.getTotalTimeSeconds());
        stopWatch.start();
        toColl = pumpkinService.releaseMeta(to);
        stopWatch.stop();
        System.out.println(" ---- " + stopWatch.getTotalTimeSeconds());
    }


    public void detailedCommits() throws Exception {
        detailedCommits = pumpkinService.detailedCommits(from, to, committersCsv);
        buildTrends(detailedCommits);
        renderModified = true;
    }

    private void buildTrends(List<GitLogEntry> detailedCommits) throws Exception {
        trend = new LineChartModel();
        Map<String, ChartSeries> seriesMapping = new HashMap<>();
        Collections.reverse(detailedCommits);
        for (GitLogEntry gle : detailedCommits) {
            String author = gle.getAuthor().trim().replaceAll("\\P{Alnum}", "");
            ChartSeries series = seriesMapping.compute(author, (k, v) -> (v == null) ? new ChartSeries() : v);
            series.setLabel(author);
            series.set(gle.getDateTime(), gle.getChanges().size());
            if (!trend.getSeries().stream().anyMatch(s -> s.getLabel().equals(author))) {
                trend.addSeries(series);
            }
        }
        Axis x = trend.getAxis(AxisType.X);
        x.setLabel("Date time");
        trend.getAxes().put(AxisType.X, new CategoryAxis("Date"));
        trend.getAxis(AxisType.X).setTickAngle(-80);


        Axis y = trend.getAxis(AxisType.Y);
        y.setLabel("# of commits");
        trend.setLegendPosition("e");
        trend.setTitle("Commit trends");
        trend.setAnimate(true);
        trend.setMouseoverHighlight(true);
        trend.setShowDatatip(true);
    }

    public void viewFile() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        viewSourceRequest.setArtifactId(params.get("artifactId").toString());
        viewSourceRequest.setGroupId(params.get("groupId").toString());
        viewSourceRequest.setNewGit((String) params.get("newGit"));
        viewSourceRequest.setOldGit((String) params.get("oldGit"));
        viewSourceRequest.setFilePath(params.get("filePath").toString());
        viewSourceResponse = pumpkinService.source(viewSourceRequest);
        ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true)).setAttribute("viewSourceResponse", viewSourceResponse);
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("draggable", true);
        options.put("maximizable", true);
        options.put("height", "500");
        options.put("width", "700");
        options.put("title", viewSourceResponse.getFileName());
        RequestContext.getCurrentInstance().openDialog("source", options, null);
    }
}
