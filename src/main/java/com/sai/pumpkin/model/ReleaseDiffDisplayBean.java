package com.sai.pumpkin.model;

import lombok.Data;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class ReleaseDiffDisplayBean {
    private String uuid;
    private int tabIndex;
    private String from;
    private String to;
    private String groupId;
    private String artifactId;
    private String artifactName;
    private String oldMavenVersion;
    private String newMavenVersion;
    private boolean added;
    private boolean removed;
    private Set<String> filesModified;
    private String oldGitRevision;
    private String newGitRevision;
    private Set<String> committers;
    private PieChartModel committersPie;
    private PieChartModel fileTypesPie;
    private Set<FileToAuthorsBean> fileToAuthors = new TreeSet<>();
    private TagCloudModel committersCloud;
    private long noOfFilesChanged;
    private long noOfLinesInserted;
    private long noOfLinesDeleted;
    private Set<String> defectIds;
    private Set<String> featureIds;


    public ReleaseDiffDisplayBean(final String from, final String to, final GitLogSummaryResponse gitLogSummaryResponse) {
        uuid = UUID.randomUUID().toString();
        this.from = from.replace(":", " (") + ")";
        this.to = to.replace(":", " (") + ")";
        artifactId = gitLogSummaryResponse.getTo().getMavenCoordinates().getArtifactId();
        artifactName = gitLogSummaryResponse.getTo().getArtifactConfig().getName();
        groupId = gitLogSummaryResponse.getTo().getMavenCoordinates().getGroupId();
        oldMavenVersion = gitLogSummaryResponse.getFrom().getMavenCoordinates().getVersion();
        newMavenVersion = gitLogSummaryResponse.getTo().getMavenCoordinates().getVersion();

        filesModified = gitLogSummaryResponse.getAuthorsToChangeSet().values().stream()
                .flatMap(Collection::stream)
                .map(ChangeSetEntry::getFilePath)
                .collect(toSet());
        committers = gitLogSummaryResponse.getAuthorsToChangeSet().keySet();
        oldGitRevision = gitLogSummaryResponse.getFrom().getGitRevision();
        newGitRevision = gitLogSummaryResponse.getTo().getGitRevision();
        noOfFilesChanged = gitLogSummaryResponse.getNoOfFilesChanged();
        noOfLinesInserted = gitLogSummaryResponse.getNoOfLinesInserted();
        noOfLinesDeleted = gitLogSummaryResponse.getNoOfLinesDeleted();
        System.out.println(gitLogSummaryResponse.getDefectIds());
        defectIds = gitLogSummaryResponse.getDefectIds();


        Function<String, String> identity = filePath -> {
            if (filePath.contains(".")) {
                return filePath.substring(filePath.lastIndexOf("."));
            } else {
                return "not known";
            }
        };
        Map<String, Long> fileTypesCount = filesModified.stream().collect(groupingBy(identity, counting()));

        committersPie = new PieChartModel();
        committersPie.setTitle("Committers contribution");
//        committersPie.setLegendPosition("e");
        committersPie.setShowDataLabels(true);
        committersPie.setDiameter(350);

        fileTypesPie = new PieChartModel();
        fileTypesCount.entrySet().forEach(entry -> {
            fileTypesPie.set(entry.getKey().trim(), entry.getValue());
        });
        fileTypesPie.setTitle("File types modified");
        fileTypesPie.setLegendPosition("e");
        fileTypesPie.setShowDataLabels(true);
        fileTypesPie.setDiameter(350);

        filesModified.forEach(f -> {
            Optional<FileToAuthorsBean> existing = fileToAuthors.stream().filter(fl -> fl.getFile().equals(f)).findFirst();
            FileToAuthorsBean bean = null;
            if (!existing.isPresent()) {
                bean = new FileToAuthorsBean();
            }
            Set<String> authors = new TreeSet<>();

            for (Map.Entry<String, Set<ChangeSetEntry>> entry : gitLogSummaryResponse.getAuthorsToChangeSet().entrySet()) {
                if (entry.getValue().stream().anyMatch(cse -> cse.getFilePath().equals(f))) {
                    authors.add(entry.getKey());
                }
            }
            bean.setFile(f);
            bean.setAuthors(authors);

            fileToAuthors.add(bean);
        });

        committersCloud = new DefaultTagCloudModel();
        final List<Integer> flattenedCounts = new ArrayList<>();
        gitLogSummaryResponse.getAuthorsToChangeSet().entrySet().forEach(entry -> {
            committersPie.set(entry.getKey().trim(), entry.getValue().size());
            flattenedCounts.add(entry.getValue().size());
        });
        int min = flattenedCounts.stream().min(Integer::compare).get();
        int max = flattenedCounts.stream().max(Integer::compare).get();

        System.out.println("Min: " + min);
        System.out.println("Max: " + max);

        gitLogSummaryResponse.getAuthorsToChangeSet().entrySet().forEach(entry -> {
            committersCloud.addTag(new DefaultTagCloudItem(entry.getKey().trim(), scale(entry.getValue().size(), min, max, 1, 5)));
        });

    }

    public static int scale(final int valueIn, final int baseMin, final int baseMax, final int limitMin, final int limitMax) {
        int i = baseMax - baseMin;
        if (i == 0) {
            i = 1;
        }
        return ((limitMax - limitMin) * (valueIn - baseMin) / i) + limitMin;
    }
}
