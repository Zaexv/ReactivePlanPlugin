package reactiveplan.jsonhandler;

import reactiveplan.entities.Employee;

import java.util.Set;

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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Employee e : employees){
         sb.append(e.toString());
         sb.append(e.getCalendar().toString());
        }
        return sb.toString();
    }
}
