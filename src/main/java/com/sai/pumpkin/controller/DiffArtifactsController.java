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
import java.util.stream.Collectors;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class DiffArtifactsController {

    private List<MavenGitVersionMapping> allArtifacts;
    private String from;
    private String to;
    private final PumpkinService pumpkinService = new PumpkinService();
    private ReleaseDiffResponse releaseDiffResponse;
    private List<ReleaseDiffDisplayBean> modified;
    private boolean renderModified;
    private boolean renderDropDowns;
    private ViewSourceRequest viewSourceRequest = new ViewSourceRequest();
    private ViewSourceResponse viewSourceResponse;
    private String one;
    private String two;
    private String committersCsv;
    private List<GitLogEntry> detailedCommits;
    private LineChartModel trend;
    private HorizontalBarChartModel changeMagnitude;
    private ReleaseDiffResponse diffResponse;
    private String artifactId;
    private boolean includeSnapshots;


    public DiffArtifactsController() {

    }

    public void diff() {
        diffResponse = pumpkinService.diffArtifacts(from, to);
        modified = diffResponse.getDiffs().stream().map(a -> new ReleaseDiffDisplayBean(from, to, a)).collect(Collectors.toList());
        renderModified = true;
    }

    public void onKey() {
        if (artifactId.length() > 3) {
            allArtifacts = pumpkinService.allArtifacts(artifactId);
            if (!includeSnapshots) {
                allArtifacts = allArtifacts.stream().filter(m -> !m.getMavenCoordinates().getVersion().contains("SNAPSHOT")).collect(Collectors.toList());
            }
            renderDropDowns = true;
        } else {
            renderDropDowns = false;
        }
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
