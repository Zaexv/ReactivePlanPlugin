package reactiveplan.jsonhandler;

import reactiveplan.entities.DaySlot;
import reactiveplan.entities.Employee;

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

    //TODO añadir métodos xd


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


    //Todo test, aunque debería funcionar
    public Set<String> getAllPlannedIssues(){
        Set<String> plannedIssueKeys = new HashSet<>();

        for(Employee e : employees){
          List<DaySlot> plannedSlots =   e.getCalendar().stream()
                    .filter(day -> day.getFeature() != null).
                    filter(day-> plannedIssueKeys.contains(day.getFeature())).collect(Collectors.toList());
            if(!plannedSlots.isEmpty()){
                for(DaySlot day : plannedSlots){
                    plannedIssueKeys.add(day.getFeature());
                }
            }

        }


        return plannedIssueKeys;
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
