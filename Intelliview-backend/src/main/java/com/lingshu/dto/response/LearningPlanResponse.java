package com.lingshu.dto.response;

import java.util.List;

public class LearningPlanResponse {

    private String planId;
    private String targetPosition;
    private String timeframe;
    private List<WeeklyGoal> weeklyGoals;
    private List<MilestoneInterview> milestoneInterviews;

    // Getters and Setters
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(String targetPosition) {
        this.targetPosition = targetPosition;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public List<WeeklyGoal> getWeeklyGoals() {
        return weeklyGoals;
    }

    public void setWeeklyGoals(List<WeeklyGoal> weeklyGoals) {
        this.weeklyGoals = weeklyGoals;
    }

    public List<MilestoneInterview> getMilestoneInterviews() {
        return milestoneInterviews;
    }

    public void setMilestoneInterviews(List<MilestoneInterview> milestoneInterviews) {
        this.milestoneInterviews = milestoneInterviews;
    }

    public static class WeeklyGoal {
        private Integer week;
        private List<String> focusAreas;
        private Integer practiceInterviews;
        private List<String> resources;

        // Getters and Setters
        public Integer getWeek() {
            return week;
        }

        public void setWeek(Integer week) {
            this.week = week;
        }

        public List<String> getFocusAreas() {
            return focusAreas;
        }

        public void setFocusAreas(List<String> focusAreas) {
            this.focusAreas = focusAreas;
        }

        public Integer getPracticeInterviews() {
            return practiceInterviews;
        }

        public void setPracticeInterviews(Integer practiceInterviews) {
            this.practiceInterviews = practiceInterviews;
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources = resources;
        }
    }

    public static class MilestoneInterview {
        private String date;
        private String type;
        private String focus;

        // Getters and Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFocus() {
            return focus;
        }

        public void setFocus(String focus) {
            this.focus = focus;
        }
    }
}

