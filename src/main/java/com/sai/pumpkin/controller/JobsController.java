package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.CollectionJob;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

@Data
public class JobsController {

    private List<CollectionJob> responses;
    private final PumpkinService pumpkinService = new PumpkinService();
    private HorizontalBarChartModel activity;

    public JobsController() {
        responses = pumpkinService.collectedJobs();
        activity = new HorizontalBarChartModel();
        ChartSeries commits = new ChartSeries();
        commits.setLabel("Artifact");

        List<String> read = new ArrayList<>();
        responses.forEach(cj -> {
            if (!read.contains(cj.getConfigName())) {
                read.add(cj.getConfigName());
                commits.set(cj.getConfigName(), cj.getTotal());
            }
        });
        activity.addSeries(commits);

        activity.setTitle("Artifacts collection time taken in seconds");
        activity.setLegendPosition("e");

        Axis xAxis = activity.getAxis(AxisType.X);
        xAxis.setLabel("collection time taken in seconds");

        Axis yAxis = activity.getAxis(AxisType.Y);
        yAxis.setLabel("Artifact name");
        activity.setAnimate(true);
        activity.setShowPointLabels(true);
        activity.setShowDatatip(true);
    }

    public void collect() {
        pumpkinService.collectAll();
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage("Job submitted for collection."));
    }

    public void clearCache() {
        pumpkinService.clearCache();
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage("All cache cleared!"));
    }
}
