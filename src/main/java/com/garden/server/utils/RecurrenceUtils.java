package com.garden.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garden.server.dto.TaskDto;
import com.garden.server.model.RecurrenceRule;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class RecurrenceUtils {
    private final ObjectMapper objectMapper;

    public RecurrenceUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TaskDto.Response> expandTasks(List<TaskDto.Response> tasks, LocalDate startDate, LocalDate endDate) {
        List<TaskDto.Response> expandedTasks = new ArrayList<>();
        long startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1;

        for (TaskDto.Response task : tasks) {
            if (task.getRecurrenceRuleJson() == null || task.getRecurrenceRuleJson().trim().isEmpty()) {
                if (task.getDate() >= startMillis && task.getDate() <= endMillis) {
                    expandedTasks.add(task);
                }
                continue;
            }

            try {
                RecurrenceRule rule = objectMapper.readValue(task.getRecurrenceRuleJson(), RecurrenceRule.class);
                if ("NONE".equals(rule.getType()) || rule.getType() == null) {
                    if (task.getDate() >= startMillis && task.getDate() <= endMillis) {
                        expandedTasks.add(task);
                    }
                    continue;
                }

                List<TaskDto.Response> occurrences = generateOccurrences(task, rule, startMillis, endMillis);
                expandedTasks.addAll(occurrences);
            } catch (Exception e) {
                if (task.getDate() >= startMillis && task.getDate() <= endMillis) {
                    expandedTasks.add(task);
                }
            }
        }

        expandedTasks.sort(Comparator.comparing(TaskDto.Response::getIsCompleted)
                .thenComparing(TaskDto.Response::getDate));

        return expandedTasks;
    }

    private List<TaskDto.Response> generateOccurrences(TaskDto.Response baseTask, RecurrenceRule rule, long startMillis, long endMillis) {
        List<TaskDto.Response> occurrences = new ArrayList<>();
        long currentDate = baseTask.getDate();
        int occurrenceIndex = 1;
        long hardLimit = Math.max(endMillis, rule.getEndDate() != null ? rule.getEndDate() : 0) + 31536000000L * 5L;

        while (currentDate <= hardLimit) {
            if (rule.getEndType() != null && "DATE".equals(rule.getEndType()) && rule.getEndDate() != null && currentDate > rule.getEndDate()) {
                break;
            }
            if (rule.getEndType() != null && "COUNT".equals(rule.getEndType()) && rule.getEndCount() != null && occurrenceIndex > rule.getEndCount()) {
                break;
            }

            if (currentDate >= startMillis && currentDate <= endMillis) {
                TaskDto.Response occurrence = new TaskDto.Response();
                occurrence.setId(-(baseTask.getId() * 10000L + occurrenceIndex));
                occurrence.setUserId(baseTask.getUserId());
                occurrence.setPlotId(baseTask.getPlotId());
                occurrence.setTitle(baseTask.getTitle());
                occurrence.setDescription(baseTask.getDescription());
                occurrence.setDate(currentDate);
                occurrence.setCategoryName(baseTask.getCategoryName());
                occurrence.setPlantedItemId(baseTask.getPlantedItemId());
                occurrence.setPlantedItemType(baseTask.getPlantedItemType());
                occurrence.setRecurrenceRuleJson(baseTask.getRecurrenceRuleJson());

                boolean isCompleted = rule.getCompletedInstanceDates() != null && rule.getCompletedInstanceDates().contains(currentDate);
                occurrence.setIsCompleted(isCompleted);

                occurrences.add(occurrence);
            }

            currentDate = getNextOccurrenceDate(currentDate, rule, baseTask.getDate());
            if (currentDate == -1) break;
            occurrenceIndex++;
        }
        return occurrences;
    }

    private long getNextOccurrenceDate(long currentDateMillis, RecurrenceRule rule, long baseDateMillis) {
        LocalDate currentDate = Instant.ofEpochMilli(currentDateMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate baseDate = Instant.ofEpochMilli(baseDateMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        int interval = rule.getInterval() != null ? rule.getInterval() : 1;

        try {
            switch (rule.getType()) {
                case "DAILY":
                    return currentDate.plusDays(interval).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                case "WEEKLY":
                    if (rule.getWeekDays() == null || rule.getWeekDays().isEmpty()) {
                        return currentDate.plusWeeks(interval).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    } else {
                        int currentDayOfWeek = currentDate.getDayOfWeek().getValue();
                        List<Integer> sortedDays = new ArrayList<>(rule.getWeekDays());
                        sortedDays.sort(Integer::compareTo);
                        Integer nextDay = sortedDays.stream().filter(d -> d > currentDayOfWeek).findFirst().orElse(null);
                        if (nextDay != null) {
                            return currentDate.plusDays(nextDay - currentDayOfWeek).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        } else {
                            int daysToFirst = (7 - currentDayOfWeek) + sortedDays.get(0);
                            return currentDate.plusDays(daysToFirst + (interval - 1) * 7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        }
                    }
                case "MONTHLY":
                    if ("BY_DAY_OF_WEEK".equals(rule.getMonthType()) && rule.getMonthWeekOrdinal() != null && rule.getMonthDayOfWeek() != null) {
                        LocalDate nextMonth = currentDate.plusMonths(interval).withDayOfMonth(1);
                        int targetDay = rule.getMonthDayOfWeek();
                        int diff = targetDay - nextMonth.getDayOfWeek().getValue();
                        if (diff < 0) diff += 7;
                        LocalDate targetDate = nextMonth.plusDays(diff);
                        if (rule.getMonthWeekOrdinal() > 0) {
                            targetDate = targetDate.plusWeeks(rule.getMonthWeekOrdinal() - 1);
                        } else {
                            int maxDay = nextMonth.lengthOfMonth();
                            LocalDate lastDay = nextMonth.withDayOfMonth(maxDay);
                            diff = lastDay.getDayOfWeek().getValue() - targetDay;
                            if (diff < 0) diff += 7;
                            targetDate = lastDay.minusDays(diff);
                        }
                        return targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    } else {
                        LocalDate nextMonth = currentDate.plusMonths(interval);
                        int day = Math.min(baseDate.getDayOfMonth(), nextMonth.lengthOfMonth());
                        return nextMonth.withDayOfMonth(day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    }
                case "YEARLY":
                    LocalDate nextYear = currentDate.plusYears(interval);
                    int day = Math.min(baseDate.getDayOfMonth(), nextYear.lengthOfMonth());
                    return nextYear.withDayOfMonth(day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                default:
                    return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }
}