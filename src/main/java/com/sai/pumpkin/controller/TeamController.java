package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.GitLogEntry;
import com.sai.pumpkin.model.MavenCoordinates;
import com.sai.pumpkin.model.ReleaseArtifact;
import com.sai.pumpkin.model.Team;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.chart.*;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Created by saipkri on 10/04/17.
 */
@Data
public class TeamController {

    private final PumpkinService pumpkinService = new PumpkinService();
    private List<ReleaseArtifact> releaseArtifacts;
    private List<Team> teams;
    private String from;
    private String to;
    private String teamName;
    private List<GitLogEntry> detailedCommits;
    private boolean renderModified;
    private Set<String> files;
    private Set<String> defects;
    private Set<MavenCoordinates> artifacts;
    private TagCloudModel committersCloud;

    private PieChartModel fileTypesPie;
    private LineChartModel trend;
    public static final Pattern defectIdPattern = Pattern.compile(System.getProperty("defectIdRegex").trim());
    private Team teamDetail;
    private MapModel simpleModel;
    private String latestRelease;


    public TeamController() throws Exception {
        releaseArtifacts = pumpkinService.allReleases().stream().filter(r -> r.getSnapshot() == null || !r.getSnapshot()).collect(Collectors.toList());
        if (!releaseArtifacts.isEmpty()) {
            ReleaseArtifact last = releaseArtifacts.get(releaseArtifacts.size() - 1);
            latestRelease = last.getName() + ":" + last.getVersion();
        }
        teams = pumpkinService.allTeams();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        from = request.getParameter("from");
        to = request.getParameter("to");
        teamName = request.getParameter("teamName");
        if (StringUtils.isNoneBlank(from, to, teamName)) {
            search();
        }
    }

    public void search() throws Exception {
        Optional<Team> first = teams.stream().filter(t -> t.getName().equals(teamName)).findFirst();
        if (!first.isPresent()) {
            return;
        }
        teamDetail = first.get();
        Function<String, String> identity = filePath -> {
            if (filePath.contains(".")) {
                return filePath.substring(filePath.lastIndexOf("."));
            } else {
                return "not known";
            }
        };

        if (to.equalsIgnoreCase("NOW")) {
            detailedCommits = pumpkinService.teamLatestActivity(teamName);
        } else {
            detailedCommits = pumpkinService.teamActivity(from, to, teamName);
        }
        renderModified = true;
        if (!detailedCommits.isEmpty()) {
            files = detailedCommits.stream().flatMap(gl -> gl.getChanges().stream()).map(cs -> cs.getFilePath()).collect(toSet());
            artifacts = detailedCommits.stream().map(gl -> gl.getMavenCoordinates()).collect(Collectors.toSet());
            defects = new TreeSet<>();
            detailedCommits.stream().forEach(gitLogEntry -> {
                Matcher matcher = defectIdPattern.matcher(gitLogEntry.getCommitMessage());
                while (matcher.find()) {
                    defects.add(matcher.group());
                }
            });

            committersCloud = new DefaultTagCloudModel();
            Map<String, Long> filesToAuthorsCount = detailedCommits.stream().map(gl -> gl.getAuthor()).collect(groupingBy(Function.identity(), counting()));

            long min = filesToAuthorsCount.values().stream().min(Long::compare).get();
            long max = filesToAuthorsCount.values().stream().max(Long::compare).get();
            filesToAuthorsCount.entrySet().forEach(entry -> {
                committersCloud.addTag(new DefaultTagCloudItem(entry.getKey().trim(), (int) scale(entry.getValue(), min, max, 1, 5)));
            });


            Map<String, Long> fileTypesCount = files.stream().collect(groupingBy(identity, counting()));
            fileTypesPie = new PieChartModel();
            fileTypesCount.entrySet().forEach(entry -> {
                fileTypesPie.set(entry.getKey().trim(), entry.getValue());
            });
            fileTypesPie.setTitle("File types modified");
            fileTypesPie.setLegendPosition("e");
            fileTypesPie.setShowDataLabels(true);
            fileTypesPie.setDiameter(350);

            buildTrends(detailedCommits);

            simpleModel = new DefaultMapModel();
            teamDetail.getMembers().forEach(tm -> {
                LatLng coord1 = new LatLng(tm.getLocationLat(), tm.getLocationLong());
                simpleModel.addOverlay(new Marker(coord1, teamName));
            });
        }
    }

    public static long scale(final long valueIn, final long baseMin, final long baseMax, final long limitMin, final long limitMax) {
        long i = baseMax - baseMin;
        if (i == 0) {
            i = 1;
        }
        return ((limitMax - limitMin) * (valueIn - baseMin) / i) + limitMin;
    }

    private void buildTrends(List<GitLogEntry> detailedCommits) throws Exception {
        final SimpleDateFormat in = new SimpleDateFormat("dd-MMM-yyyy");
        final SimpleDateFormat out = new SimpleDateFormat("EEE MMM dd H:m:s yyyy Z");

        Map<String, Integer> counts = new TreeMap<>();

        detailedCommits.stream().forEach(gl -> {
            String formatted = null;
            try {
                formatted = in.format(out.parse(gl.getDateTime().trim()));
                System.out.println(gl.getRevision() + " ---> " + gl.getDateTime().trim() + " --> " + formatted);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (counts.get(formatted) == null) {
                counts.put(formatted, 1);
            } else {
                counts.put(formatted, counts.get(formatted) + 1);
            }
        });
        trend = new LineChartModel();
        ChartSeries series = new LineChartSeries("Commits per day");
        series.setLabel("# of Commits per day");
        for (Map.Entry<String, Integer> gle : counts.entrySet()) {
            series.set(gle.getKey(), gle.getValue());
        }
        Axis x = trend.getAxis(AxisType.X);
        x.setLabel("Date");
        trend.getAxes().put(AxisType.X, new CategoryAxis("Date"));
        trend.getAxis(AxisType.X).setTickAngle(-80);
        trend.addSeries(series);


        Axis y = trend.getAxis(AxisType.Y);
        y.setLabel("# of commits");
        trend.setLegendPosition("e");
        trend.setTitle("Mainline Commit trends of the team");
        trend.setAnimate(true);
        trend.setMouseoverHighlight(true);
        trend.setShowDatatip(true);
    }
}
