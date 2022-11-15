package com.supermap.learning.minIO.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 解压进度条
 *
 * @author lty
 */
@Data
@Accessors(chain = true)
public class DecompressProgressBarBO {

    private List<String> decompressedFiles = new ArrayList<>();

    private long read;

    private long total;

    private String percentage;

    private Date startTime;

    private Date endTime;

    public String getPercentage() {
        return (int) ((read * 1.0 / total) * 100) + "%";
    }

}
