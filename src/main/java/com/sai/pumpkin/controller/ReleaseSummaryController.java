package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.ChangeSetEntry;
import com.sai.pumpkin.model.GitLogSummaryResponse;
import com.sai.pumpkin.model.ReleaseArtifact;
import com.sai.pumpkin.model.ReleaseDiffResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.chart.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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
    private BarChartModel changeMagnitude;
    private LineChartModel model;
    private LineChartModel commitTrends;
    private DashboardModel dash;
    private List<String> arr = Arrays.asList("1", "2", "3", "4");
    private PieChartModel fileTypesPie;
    private int totalModifiedArtifacts;

    public ReleaseSummaryController() {
        releaseArtifacts = pumpkinService.allReleases();
        // filter out snapshots. we don't want them here.
        releaseArtifacts = releaseArtifacts.stream().filter(ra -> ra.getSnapshot() == null || !ra.getSnapshot()).collect(Collectors.toList());
        diff();
        commitTrends();
    }

    private void commitTrends() {
        long twentyDaysAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 20);
        long rounded = DateUtils.round(new Date(twentyDaysAgo), Calendar.DAY_OF_MONTH).getTime();
        Map<String, Integer> trends = pumpkinService.commitTrends(rounded);
        commitTrends = new LineChartModel();
        LineChartSeries series1 = new LineChartSeries();
        series1.setLabel("# of commits into the main branch");
        trends.entrySet().stream().forEach(kv -> series1.set(kv.getKey(), kv.getValue()));
        commitTrends.setTitle("Commit trends into the main branch");
        commitTrends.setLegendPosition("e");
        Axis yAxis = commitTrends.getAxis(AxisType.Y);

        yAxis.setLabel("# of commits");

        commitTrends.getAxes().put(AxisType.X, new CategoryAxis("Day"));
        commitTrends.getAxis(AxisType.X).setTickAngle(-90);
        commitTrends.setAnimate(true);
        commitTrends.addSeries(series1);

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
            totalModifiedArtifacts = currentDiff.getDiffs().size();

            changeMagnitude = buildChangeMagnitude();

            int start = (releaseArtifacts.size() >= 5) ? releaseArtifacts.size() - 5 : 0;
            model = new LineChartModel();

            LineChartSeries series1 = new LineChartSeries();
            series1.setLabel("# of files changed");

            LineChartSeries series2 = new LineChartSeries();
            series2.setLabel("# of committers");

            LineChartSeries series3 = new LineChartSeries();
            series3.setLabel("# of defect fixes");

            ReleaseDiffResponse rdf = null;
            for (int i = 1; i < releaseArtifacts.size(); i++) {
                rdf = pumpkinService.diffReleases(releaseArtifacts.get(i - 1).getName() + ":" + releaseArtifacts.get(i - 1).getVersion(), releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion());
                series1.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getNoOfFilesChanged()).sum());
                series2.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getAuthorsToChangeSet().size()).sum());
                series3.set(releaseArtifacts.get(i).getName() + ":" + releaseArtifacts.get(i).getVersion(), rdf.getDiffs().stream().mapToLong(g -> g.getDefectIds().size()).sum());
            }

            model.setTitle("Release Trending");
            model.setLegendPosition("e");
            Axis yAxis = model.getAxis(AxisType.Y);

            yAxis.setLabel("count");

            model.getAxes().put(AxisType.X, new CategoryAxis("Release name and version"));
            model.getAxis(AxisType.X).setTickAngle(-90);
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

    private BarChartModel buildChangeMagnitude() {
        changeMagnitude = new BarChartModel();

        ChartSeries filesChanged = new ChartSeries();
        filesChanged.setLabel("# of files changed");

        List<GitLogSummaryResponse> topN = currentDiff.getDiffs().stream()
                .sorted((a, b) -> Long.valueOf(b.getNoOfFilesChanged()).compareTo(a.getNoOfFilesChanged()))
                .collect(Collectors.toList());
        topN = topN.stream()
                .limit(30L)
                .collect(Collectors.toList());


        for (GitLogSummaryResponse b : topN) {
            System.out.println(b.getFrom().getArtifactConfig().getName() + " --> " + b.getNoOfFilesChanged());
            filesChanged.set(b.getTo().getMavenCoordinates().getArtifactId() + " (" + b.getTo().getMavenCoordinates().getVersion() + ") ", b.getNoOfFilesChanged());
        }

        changeMagnitude.addSeries(filesChanged);

        changeMagnitude.setTitle("Top 30 Change Magnitude of every artifact (minimum 2 files changed)");
        changeMagnitude.setLegendPosition("e");
        changeMagnitude.setStacked(true);

        Axis xAxis = changeMagnitude.getAxis(AxisType.X);
        xAxis.setTickAngle(-75);

        Axis yAxis = changeMagnitude.getAxis(AxisType.Y);
        xAxis.setLabel("Artifact name");
        yAxis.setLabel("# no of files changed");

        changeMagnitude.setAnimate(true);
        changeMagnitude.setShowPointLabels(true);
        changeMagnitude.setShowDatatip(true);

        return changeMagnitude;
    }
}
