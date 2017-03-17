package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.ArtifactConfig;
import com.sai.pumpkin.model.GitLogResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

import java.util.List;
import java.util.Map;

@Data
public class IndexController {

    private List<GitLogResponse> responses;
    private final PumpkinService pumpkinService = new PumpkinService();
    private HorizontalBarChartModel activity;
    private HorizontalBarChartModel comitterActivity;
    private List<ArtifactConfig> registered;

    public IndexController() {
        long curr = System.currentTimeMillis();
        long twentyDaysAgo = curr - (1000 * 60 * 60 * 24 * 20);
        Map<String, Integer> activities = pumpkinService.activitySince(twentyDaysAgo);
        Map<String, Integer> committerActivities = pumpkinService.committerActivitySince(twentyDaysAgo);
        registered = pumpkinService.registered();

        activity = new HorizontalBarChartModel();
        comitterActivity = new HorizontalBarChartModel();

        buildActivity(activities, activity, "Last 20 days activity based on the number of commits", "# of commits");
        buildActivity(committerActivities, comitterActivity, "Last 20 days committer activity based on # of commits", "# of commits");
    }

    private void buildActivity(Map<String, Integer> activities, HorizontalBarChartModel activity, String title, String yAxixs) {
        ChartSeries commits = new ChartSeries();
        commits.setLabel(yAxixs);

        activities.forEach(commits::set);
        activity.addSeries(commits);

        activity.setTitle(title);
        activity.setLegendPosition("e");

        Axis xAxis = activity.getAxis(AxisType.X);
        xAxis.setLabel("# number of commits");

        Axis yAxis = activity.getAxis(AxisType.Y);
        yAxis.setLabel("Artifact name");
        activity.setAnimate(true);
        activity.setShowPointLabels(true);
        activity.setShowDatatip(true);

    }


}
