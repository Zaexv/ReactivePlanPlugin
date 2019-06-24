package reactiveplan.jsonhandler;

import com.atlassian.jira.issue.Issue;
import reactiveplan.entities.DaySlot;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReplanOptimizerResponse {
    private double priorityQuality;
    private double performanceQuality;
    private double similarityQuality;
    private double globalQuality;
    private Set<Employee> employees;

    public ReplanOptimizerResponse(double priorityQuality, double performanceQuality,
                                   double similarityQuality, double globalQuality, Set<Employee> employees){
        this.priorityQuality = priorityQuality;
        this.performanceQuality = performanceQuality;
        this.similarityQuality = similarityQuality;
        this.globalQuality = globalQuality;
        this.employees = employees;

    }



    public double getPriorityQuality() {
        return priorityQuality;
    }

    public double getPerformanceQuality() {
        return performanceQuality;
    }

    public double getSimilarityQuality() {
        return similarityQuality;
    }

    public double getGlobalQuality() {
        return globalQuality;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }


    public Set<String> getPlannedFeatures(){
        Set<String> plannedIssueKeys = new HashSet<>();

        for(Employee e : employees){
          List<DaySlot> plannedSlots =   e.getCalendar().stream()
                    .filter(day -> day.getFeature() != null).
                    filter(day-> !plannedIssueKeys.contains(day.getFeature())).collect(Collectors.toList());
            if(!plannedSlots.isEmpty()){
                for(DaySlot day : plannedSlots){
                    plannedIssueKeys.add(day.getFeature());
                }
            }

        }


        return plannedIssueKeys;
    }

    public Collection<Feature> getUnplannedFeatures(Collection<Feature> featuresToPlan){

        return featuresToPlan
                .stream().filter(feature -> !getPlannedFeatures().contains(feature.getName()))
                .collect(Collectors.toList());
    }



    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Employee e : employees){
         sb.append(e.toString());
         sb.append(e.getCalendar().toString());
        }
        return sb.toString();
    }
}
