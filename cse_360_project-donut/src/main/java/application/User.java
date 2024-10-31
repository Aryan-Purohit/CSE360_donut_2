package application;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class User {

    private String username;
    private byte[] password; // Password stored as bytes
    private String role;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private boolean isOneTimePassword;
    private LocalDateTime otpExpiry;
    private boolean isAccountSetupComplete = false; // Flag for account setup completion

    private Map<String, String> topics = new HashMap<>(); // Map of topics and proficiency levels

    private List<HelpArticle> helpArticles = new ArrayList<>(); // List of help articles

    // Constructor to create a new user
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password.getBytes();
        this.role = role;
        this.isOneTimePassword = false; // Default value
        this.otpExpiry = null; // No expiry by default

        // Initialize default topics with "Intermediate" level
        topics.put("Topic 1", "Intermediate");
        topics.put("Topic 2", "Intermediate");
        topics.put("Topic 3", "Intermediate");
    }

    // Getters and Setters for user attributes

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getMiddleName() {
        return middleName;
    }
    
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPreferredName() {
        return preferredName;
    }
    
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    // Methods related to one-time password

    public boolean isOneTimePassword() {
        return isOneTimePassword;
    }

    public void setOneTimePassword(boolean isOneTimePassword) {
        this.isOneTimePassword = isOneTimePassword;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    // Methods for account setup completion

    public boolean isAccountSetupComplete() {
        return isAccountSetupComplete;
    }

    public void setAccountSetupComplete(boolean isAccountSetupComplete) {
        this.isAccountSetupComplete = isAccountSetupComplete;
    }

    // Methods for topic proficiency

    public Map<String, String> getTopics() {
        return topics;
    }

    public String getTopicProficiency(String topic) {
        return topics.getOrDefault(topic, "Intermediate");
    }

    public void setTopicProficiency(String topic, String level) {
        topics.put(topic, level);
    }

    // Nested class representing a help article
    public static class HelpArticle implements Serializable {
        private static final long serialVersionUID = 1L;

        private long id; // Unique identifier
        private String title;
        private String description;
        private List<String> keywords;
        private String body;
        private List<String> links;
        private List<String> groups;
        private String level;

        // Constructor to create a new help article
        public HelpArticle(long id, String title, String description, List<String> keywords, String body, List<String> links, List<String> groups, String level) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.keywords = keywords;
            this.body = body;
            this.links = links;
            this.groups = groups;
            this.level = level;
        }

        // Getters and Setters for help article attributes

        public long getId() { return id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public List<String> getLinks() { return links; }
        public void setLinks(List<String> links) { this.links = links; }

        public List<String> getGroups() { return groups; }
        public void setGroups(List<String> groups) { this.groups = groups; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }

    // Methods for managing help articles

    // Add a help article to the user's list
    public void addHelpArticle(HelpArticle article) {
        helpArticles.add(article);
    }

    // Remove a help article by its ID
    public void removeHelpArticle(long id) {
        helpArticles.removeIf(article -> article.getId() == id);
    }

    // Update an existing help article
    public void updateHelpArticle(long id, String title, String description, List<String> keywords, String body, List<String> links, List<String> groups, String level) {
        for (HelpArticle article : helpArticles) {
            if (article.getId() == id) {
                article.setTitle(title);
                article.setDescription(description);
                article.setKeywords(keywords);
                article.setBody(body);
                article.setLinks(links);
                article.setGroups(groups);
                article.setLevel(level);
            }
        }
    }

    // Get help articles by group
    public List<HelpArticle> getHelpArticlesByGroup(String group) {
        if ("all".equalsIgnoreCase(group)) {
            return new ArrayList<>(helpArticles);
        }
        List<HelpArticle> filteredArticles = new ArrayList<>();
        for (HelpArticle article : helpArticles) {
            if (article.getGroups().contains(group)) {
                filteredArticles.add(article);
            }
        }
        return filteredArticles;
    }

    // Get all help articles
    public List<HelpArticle> getAllHelpArticles() {
        return new ArrayList<>(helpArticles);
    }

    // Search help articles by keyword in title or keywords
    public List<HelpArticle> searchHelpArticles(String keyword) {
        List<HelpArticle> results = new ArrayList<>();
        for (HelpArticle article : helpArticles) {
            if (article.getKeywords().contains(keyword) || article.getTitle().contains(keyword)) {
                results.add(article);
            }
        }
        return results;
    }
}
