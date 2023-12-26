package tutorial;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;

/**
 * Hello world!
 *
 */
public class GitHubAPIClient
{
    public static void main(String[] args) {
        try {
            // GitHub API URL
            String apiUrl = "https://api.github.com/repos/AdlinaKamilia/Project_STIW3044/issues/comments";
            Gson gson = new Gson();

            // Create a URL object
            URL url = new URL(apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response data
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response here
                String jsonResponse = response.toString();
                // Process the JSON data
                System.out.println(jsonResponse);
                IssueComment[] comments = gson.fromJson(jsonResponse, (Type) IssueComment[].class);
                for (IssueComment comment : comments) {
                    String userLogin = comment.getUser().getLogin();
                    String body = comment.getBody();

                    System.out.println("User login: " + userLogin);
                    System.out.println("Comment body: " + body);

                    // ... do something with the extracted data
                }
            } else {
                System.out.println("API request failed with response code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
 }
}

}
