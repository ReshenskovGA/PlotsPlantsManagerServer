package com.garden.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurrenceRule {
    private String type;
    private Integer interval;
    private List<Integer> weekDays;
    private String monthType;
    private Integer monthWeekOrdinal;
    private Integer monthDayOfWeek;
    private Integer monthDay;
    private String endType;
    private Long endDate;
    private Integer endCount;
    private List<Long> completedInstanceDates;
}