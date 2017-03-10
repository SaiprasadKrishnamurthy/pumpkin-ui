package com.sai.pumpkin.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by saipkri on 08/03/17.
 */
@Data
@EqualsAndHashCode(exclude = "authors")
public class FileToAuthorsBean implements Serializable, Comparable<FileToAuthorsBean> {
    private String file;
    private Set<String> authors = new TreeSet<>();

    public String getFile() {
        if (file.contains("/")) {
            return file.substring(file.lastIndexOf("/") + 1);
        }
        return file;
    }

    @Override
    public int compareTo(FileToAuthorsBean o) {
        return this.file.compareTo(o.file);
    }
}
