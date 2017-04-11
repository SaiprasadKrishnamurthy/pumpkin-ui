package com.sai.pumpkin.controller;

import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.model.chart.MeterGaugeChartModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 10/04/17.
 */
@Data
public class RepoActivityController {

    private final PumpkinService pumpkinService = new PumpkinService();

    private List<MeterGaugeChartModel> gauges = new ArrayList<>();


    public RepoActivityController() throws Exception {
        long _48_hours_ago = System.currentTimeMillis() - (1000 * 60 * 60 * 48);

        Map<String, Integer> repoActivities = pumpkinService.repoActivities(_48_hours_ago);
        for (Map.Entry<String, Integer> entry : repoActivities.entrySet()) {
            List<Number> intervals = new ArrayList<Number>() {
                {
                    add(1);
                    add(5);
                    add(10);
                    add(15);
                    add(20);
                    add(25);
                    add(30);
                    add(35);
                    add(40);
                    add(45);
                    add(50);
                    add(55);
                    add(60);
                }
            };
            MeterGaugeChartModel m = new MeterGaugeChartModel(entry.getValue() / 2, intervals);
            m.setValue(entry.getValue() / 2);
            m.setTitle("Repo: " + entry.getKey());
            m.setGaugeLabel("commits/day");
            m.setShadow(true);
            m.setGaugeLabelPosition("bottom");
            m.setSeriesColors("66cc66,66cc66,66cc66,66cc66,66cc66,66cc66,66cc66,93b75f,93b75f,93b75f,93b75f,93b75f,E7E658");
            gauges.add(m);
        }
    }
}
