package tutorial;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GitHubAPIClient
{
    public static void main(String[] args) {
        try {
            // GitHub API URL
            String apiUrl = "https://api.github.com/repos/AdlinaKamilia/Project_STIW3044/issues/comments";
            Gson gson = new Gson();
            // Kafka producer configuration
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092"); // Replace with your Kafka broker address
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("bootstrap.servers", "localhost:9092"); // Replace with your Kafka broker address
            props.put("group.id", "pixelpuff"); // Choose a unique group ID
            props.put("key.deserializer", StringDeserializer.class.getName());
            props.put("value.deserializer", StringDeserializer.class.getName());
            props.put("auto.offset.reset", "earliest"); // Start reading from the beginning of the
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
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
                IssueComment[] comments = gson.fromJson(jsonResponse, (Type) IssueComment[].class);
                // Map to store the count of comments for each user
                Map<String, Integer> commentCountMap = new HashMap<>();
                // Map to store the count of words in comments
                Map<String, Integer> wordCountMap = new HashMap<>();

                for (IssueComment comment : comments) {
                    String userLogin = comment.getUser().getLogin();
                    String body = comment.getBody();

                    // Update the comment count for the user
                    commentCountMap.put(userLogin, commentCountMap.getOrDefault(userLogin, 0) + 1);
                    String message = createMessage(userLogin, body); // Customize message format if needed
                    ProducerRecord<String, String> record = new ProducerRecord<>("github_comments", message); // Replace with your Kafka topic
                    producer.send(record);
                    // Update the word count
                    String[] words = body.split("\\s+");
                    for (String word : words) {
                        // Remove non-alphabetic characters and convert to lowercase
                        word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                        wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                    }
                }

                // Print the list of active commenters
                System.out.println("\nList of active commenters");
                int rank = 1;
                for (Map.Entry<String, Integer> entry : commentCountMap.entrySet()) {
                    System.out.println(rank + ". " + entry.getKey() + " [" + entry.getValue() + " comments]");
                    rank++;
                }

                // Print the list of word counts
                System.out.println("\nList of word counts");
                rank = 1;
                for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
                    System.out.println(rank + ". " + entry.getKey() + " [" + entry.getValue() + " times]");
                    rank++;
                }
                producer.flush(); // Ensure all messages are sent
                producer.close();
                consumer.subscribe(Arrays.asList("github_comments")); // Replace with your Kafka topic
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        String key = record.key();
                        String value = record.value();
                        System.out.printf("Received message: key = %s, value = %s\n", key, value);

                        // Process the message here
                    }
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
    // Helper method to create a message for Kafka (optional customization)
    private static String createMessage(String userLogin, String body) {
        return "User: " + userLogin + ", Comment: " + body;
    }
}
