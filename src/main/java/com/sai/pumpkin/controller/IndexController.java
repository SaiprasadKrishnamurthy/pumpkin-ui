package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.GitLogResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class IndexController {

    private List<GitLogResponse> responses;
    private final PumpkinService pumpkinService = new PumpkinService();
    private HorizontalBarChartModel activity;

    public IndexController() {
        long curr = System.currentTimeMillis();
        long twentyDaysAgo = curr - (1000 * 60 * 60 * 24 * 20);
        Map<String, Integer> activities = pumpkinService.activitySince(twentyDaysAgo);

        activity = new HorizontalBarChartModel();

        ChartSeries commits = new ChartSeries();
        commits.setLabel("# of commits");

        activities.forEach(commits::set);
        activity.addSeries(commits);

        activity.setTitle("Last 20 days activity based on the number of commits");
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
