package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.*;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.*;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class DiffReleasesController {

    private List<ReleaseArtifact> releaseArtifacts;
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
    private List<GitLogEntry> detailedCommits;
    private LineChartModel trend;
    private HorizontalBarChartModel changeMagnitude;


    public DiffReleasesController() {
        releaseArtifacts = pumpkinService.allReleases();
    }

    public void diff() {
        releaseDiffResponse = pumpkinService.diffReleases(from, to);
        // Modified
        modified = releaseDiffResponse.getDiffs().stream().map(r -> new ReleaseDiffDisplayBean(from, to, r)).collect(toList());
        renderModified = true;
        changeMagnitude = buildChangeMagnitude();
    }

    private HorizontalBarChartModel buildChangeMagnitude() {
        changeMagnitude = new HorizontalBarChartModel();

        ChartSeries linesAdded = new ChartSeries();
        linesAdded.setLabel("# of lines inserted");

        ChartSeries linesRemoved = new ChartSeries();
        linesRemoved.setLabel("# of lines deleted");

        for (ReleaseDiffDisplayBean b : modified) {
            linesAdded.set(b.getArtifactName(), b.getNoOfLinesInserted());
            linesRemoved.set(b.getArtifactName(), b.getNoOfLinesDeleted());
        }

        changeMagnitude.addSeries(linesAdded);
        changeMagnitude.addSeries(linesRemoved);

        changeMagnitude.setTitle("Change Magnitude of every artifact");
        changeMagnitude.setLegendPosition("e");
        changeMagnitude.setStacked(true);

        Axis xAxis = changeMagnitude.getAxis(AxisType.X);
        xAxis.setLabel("# quantity");

        Axis yAxis = changeMagnitude.getAxis(AxisType.Y);
        yAxis.setLabel("Artifact name");
        changeMagnitude.setAnimate(true);
        changeMagnitude.setShowPointLabels(true);
        changeMagnitude.setShowDatatip(true);

        return changeMagnitude;
    }

    public void detailedCommits() throws Exception {
        System.out.println("Detailed commits: " + committersCsv);
        System.out.println("Detailed commits: " + to);
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
