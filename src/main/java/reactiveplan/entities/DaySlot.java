package reactiveplan.entities;

import reactiveplan.jiraconverter.ReplanToJiraConverter;

import java.util.Date;

public class DaySlot implements Comparable<DaySlot> {


    private int id;
    private int week;
    private int dayOfWeek;
    private double beginHour;
    private double endHour;
    private SlotStatus status;
    private String featureId;

    public DaySlot() {

    }

    public DaySlot(int id, int week, int dayOfWeek, double beginHour, double endHour,
                   SlotStatus status) {
        this.id = id;
        this.week = week;
        this.dayOfWeek = dayOfWeek;
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.status = status;
    }

    public DaySlot(DaySlot daySlot) {
        this.id = daySlot.getId();
        this.week = daySlot.getWeek();
        this.dayOfWeek = daySlot.getDayOfWeek();
        this.beginHour = daySlot.getBeginHour();
        this.endHour = daySlot.getEndHour();
        this.status = daySlot.getStatus();
        this.featureId = daySlot.getFeature();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWeek() { return week; }
    public void setWeek(int week) { this.week = week; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public double getBeginHour() { return beginHour; }
    public void setBeginHour(double beginHour) { this.beginHour = beginHour; }

    public double getEndHour() { return endHour; }
    public void setEndHour(double endHour) { this.endHour = endHour; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }

    public String getFeature() { return featureId; }
    public void setFeature(Feature feature) { this.featureId = feature.getName(); }
    public void setFeatureId(String featureId) { this.featureId = featureId; }

    public double getDuration() {
        return endHour - beginHour;
    }

    public int compareByDay(DaySlot daySlot) {
        return (this.week * 7 + this.dayOfWeek) - (daySlot.week * 7 + daySlot.dayOfWeek);
    }

    public double getEndHourAbsolute() {
        return this.week * 7 * 24
                + this.dayOfWeek * 24
                + this.endHour;
    }

    public double getBeginHourAbsolute() {
        return this.week * 7 * 24
                + this.dayOfWeek * 24
                + this.beginHour;
    }

    @Override
    public int compareTo(DaySlot daySlot) {
        return (int) (this.week * 7 * 24 * 60
                        + this.dayOfWeek * 24 * 60
                        + this.beginHour * 60) -
                (int) (daySlot.week * 7 * 24 * 60
                        + daySlot.dayOfWeek * 24 * 60
                        + daySlot.beginHour * 60);
    }

    @Override
    public String toString() {
        if(this.getFeature() != null) {

        return "[SLOT] week: " + week + " | day: " + dayOfWeek + " | beginHour: " + beginHour
                + " | endHour: " + endHour + " | status: " + status + " | Feature: " + this.getFeature();

        } else {
            return "[SLOT] week: " + week + " | day: " + dayOfWeek + " | beginHour: " + beginHour
                    + " | endHour: " + endHour + " | status: " + status;
        }
    }

    public String getDate(Date startDate){
        if(startDate != null){
            return ReplanToJiraConverter.daySlotToDate(startDate,this);
        } else{
            return ReplanToJiraConverter.daySlotToDate(new Date(), this);
        }
    }

    public double getTime() {
        return this.week*7*24 + this.dayOfWeek*24 + this.beginHour;
    }
}
