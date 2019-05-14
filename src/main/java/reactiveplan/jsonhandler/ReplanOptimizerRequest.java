package reactiveplan.jsonhandler;

import com.atlassian.jira.issue.Issue;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;

import java.util.Collection;
import java.util.Set;

public class ReplanOptimizerRequest {

    private Set<Employee> resources;
    private Collection<Feature> features;


    public ReplanOptimizerRequest(Set<Employee> employeesRequest, Collection<Feature> featureRequest){
        this.resources = employeesRequest;
        this.features = featureRequest;
    }

    public void doRequest(){
        //TODO Esto debería hacer el Request de Replan y devolverlo. Incluso debería devolver el objeto ya medio transformado.
    }

    public Set<Employee> getEmployeesRequest() {
        return resources;
    }

    public Collection<Feature> getFeatureRequest() {
        return features;
    }
}
