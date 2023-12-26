package tutorial;

public class IssueComment {
    private String url;
    private String html_url;
    // ... other fields (excluding those you don't need)
    private User user;
    private String body;
    public String getUrl(){
        return url;
    }
    public String getHTMLUrl(){
        return html_url;
    }

    public String getBody() {
        return body;
    }
    // Getters and setters for the fields

    public User getUser() {
        return user;
    }
}

