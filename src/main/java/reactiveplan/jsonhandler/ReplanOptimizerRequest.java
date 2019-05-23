package reactiveplan.jsonhandler;

import com.atlassian.jira.issue.Issue;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;
import reactiveplan.entities.PlannedFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class ReplanOptimizerRequest {


    /* Aquí se define el Endpoint de REPLAN
    * TODO Hacer que el Endpoint se coja de un archivo de propiedades o algo así */

    static String endpoint = "http://gessi-sw.essi.upc.edu:8080/replan_optimizer-0.0.1/replan";

 //   static String endpoint = "https://replan-optimizer-llavor.herokuapp.com/replan";


    private Set<Employee> resources;
    private Collection<Feature> features;
    private String requestResponse;

    public ReplanOptimizerRequest(Set<Employee> employeesRequest, Collection<Feature> featureRequest){
        this.resources = employeesRequest;
        this.features = featureRequest;
    }

    public String doRequest() throws IOException {
        Gson gson = new Gson();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setEntity(new StringEntity(gson.toJson(this)));

        /* Este handler está copiado de internet */
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        String responseBody = httpClient.execute(httpPost,responseHandler);
        requestResponse = requestResponse;
       ReplanOptimizerResponse responseObject = gson.fromJson(responseBody,ReplanOptimizerResponse.class);
       return responseBody;
    }

    public Set<Employee> getEmployeesRequest() {
        return resources;
    }

    public Collection<Feature> getFeatureRequest() {
        return features;
    }
}
