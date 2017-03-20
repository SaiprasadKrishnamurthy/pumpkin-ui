package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.ChangeSetEntry;
import com.sai.pumpkin.model.GitLogSummaryResponse;
import com.sai.pumpkin.model.ReleaseArtifact;
import com.sai.pumpkin.model.ReleaseDiffResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.chart.*;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class ReleaseSummaryController {

    private final PumpkinService pumpkinService = new PumpkinService();
    private List<ReleaseArtifact> releaseArtifacts;
    private ReleaseArtifact from;
    private ReleaseArtifact to;
    private ReleaseDiffResponse currentDiff;
    private long totalFilesChanged;
    private long totalCommitters;
    private long totalDefectFixes;
    private HorizontalBarChartModel changeMagnitude;
    private LineChartModel model;
    private DashboardModel dash;
    private List<String> arr = Arrays.asList("1", "2", "3", "4");
    private PieChartModel fileTypesPie;



    public ReleaseSummaryController() {
        releaseArtifacts = pumpkinService.allReleases();
        diff();
        dash = new DefaultDashboardModel();
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        DashboardColumn column4 = new DefaultDashboardColumn();

        column1.addWidget("metrics");
        column1.addWidget("change");

        column2.addWidget("trending");
        column2.addWidget("filetypes");

        dash.addColumn(column1);
        dash.addColumn(column2);
        dash.addColumn(column3);
        dash.addColumn(column4);
    }

    public void diff() {
        List<ReleaseDiffResponse> diffs = new ArrayList<>();
        if (releaseArtifacts.size() > 1) {
            from = releaseArtifacts.get(releaseArtifacts.size() - 2);
            to = releaseArtifacts.get(releaseArtifacts.size() - 1);

            currentDiff = pumpkinService.diffReleases(from.getName() + ":" + from.getVersion(), to.getName() + ":" + to.getVersion());
            totalFilesChanged = currentDiff.getDiffs().stream().mapToLong(g -> g.getNoOfFilesChanged()).sum();
            totalCommitters = currentDiff.getDiffs().stream().mapToLong(g -> g.getAuthorsToChangeSet().size()).sum();
            totalDefectFixes = currentDiff.getDiffs().stream().mapToLong(g -> g.getDefectIds().size()).sum();

            changeMagnitude = buildChangeMagnitude();

            List<ReleaseDiffResponse> trending = new ArrayList<>();
            int start = (releaseArtifacts.size() >= 5) ? releaseArtifacts.size() - 5 : 0;
            model = new LineChartModel();

            LineChartSeries series1 = new LineChartSeries();
            series1.setLabel("# of files changed");

            LineChartSeries series2 = new LineChartSeries();
            series2.setLabel("# of committers");

            LineChartSeries series3 = new LineChartSeries();
            series3.setLabel("# of defect fixes");

            for (int i = start; i < releaseArtifacts.size(); i++) {
                if (i == releaseArtifacts.size() - 1) {
                    break;
                }
                ReleaseDiffResponse rdf = pumpkinService.diffReleases(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), releaseArtifacts.get(i + 1).getName() + ":" + releaseArtifacts.get(i + 1).getVersion());
                series1.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getNoOfFilesChanged()).sum());
                series2.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getAuthorsToChangeSet().size()).sum());
                series3.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getDefectIds().size()).sum());
            }

            model.setTitle("Release Trending");
            model.setLegendPosition("e");
            Axis yAxis = model.getAxis(AxisType.Y);

            yAxis.setLabel("count");

            model.getAxes().put(AxisType.X, new CategoryAxis("Release name and version"));
            model.getAxis(AxisType.X).setTickAngle(-60);
            model.setAnimate(true);

            model.addSeries(series1);
            model.addSeries(series2);
            model.addSeries(series3);

            fileTypesPie = new PieChartModel();
            fileTypesPie.setTitle("File types modified");
            fileTypesPie.setLegendPosition("e");
            fileTypesPie.setShowDataLabels(true);
            fileTypesPie.setDiameter(300);

            Set<String> filesModified = currentDiff.getDiffs().stream()
                    .flatMap(gl -> gl.getAuthorsToChangeSet().values().stream())
                    .flatMap(Collection::stream)
                    .map(ChangeSetEntry::getFilePath)
                    .collect(toSet());
            Function<String, String> identity = filePath -> {
                if (filePath.contains(".")) {
                    return filePath.substring(filePath.lastIndexOf("."));
                } else {
                    return "not known";
                }
            };
            Map<String, Long> fileTypesCount = filesModified.stream().collect(groupingBy(identity, counting()));
            fileTypesCount.entrySet().forEach(entry -> {
                fileTypesPie.set(entry.getKey().trim(), entry.getValue());
            });

        }
    }

    private HorizontalBarChartModel buildChangeMagnitude() {
        changeMagnitude = new HorizontalBarChartModel();

        ChartSeries filesChanged = new ChartSeries();
        filesChanged.setLabel("# of files changed");


        for (GitLogSummaryResponse b : currentDiff.getDiffs()) {
            filesChanged.set(b.getTo().getMavenCoordinates().getArtifactId() + " (" + b.getTo().getMavenCoordinates().getVersion() + ") ", b.getNoOfFilesChanged());
        }

        changeMagnitude.addSeries(filesChanged);

        changeMagnitude.setTitle("Change Magnitude of every artifact");
        changeMagnitude.setLegendPosition("e");
        changeMagnitude.setStacked(true);

        Axis xAxis = changeMagnitude.getAxis(AxisType.X);
        xAxis.setLabel("# no of files changed");

        Axis yAxis = changeMagnitude.getAxis(AxisType.Y);
        yAxis.setLabel("Artifact name");
        changeMagnitude.setAnimate(true);
        changeMagnitude.setShowPointLabels(true);
        changeMagnitude.setShowDatatip(true);

        return changeMagnitude;
    }
}
