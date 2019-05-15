package reactiveplan.jsonhandler;

import com.atlassian.jira.issue.Issue;
import com.google.gson.Gson;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

public class ReplanOptimizerRequest {


    /* Aquí se define el Endpoint de REPLAN */

    //static String endpoint = "http://gessi-sw.essi.upc.edu:8080/replan_optimizer-0.0.1/replan";

    static String endpoint = "https://replan-optimizer-llavor.herokuapp.com/replan";


    private Set<Employee> resources;
    private Collection<Feature> features;


    public ReplanOptimizerRequest(Set<Employee> employeesRequest, Collection<Feature> featureRequest){
        this.resources = employeesRequest;
        this.features = featureRequest;
    }

    public String doRequest(){
        //TODO añadir biblioteca decente
        Gson gson = new Gson();
        String request = gson.toJson(this);
        StringBuilder sb = new StringBuilder();
        try {
            URL replanURL = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) replanURL.openConnection();
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(request.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(con.getResponseMessage());
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
        //TODO Esto debería hacer el Request de Replan y devolverlo. Incluso debería devolver el objeto ya medio transformado.
    }

    public Set<Employee> getEmployeesRequest() {
        return resources;
    }

    public Collection<Feature> getFeatureRequest() {
        return features;
    }
}
