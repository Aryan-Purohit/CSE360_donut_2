package application;

// Import necessary JavaFX and utility classes
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserInterface extends Application {

    private Stage window; // Primary stage for the application
    private Login loginInstance = Login.getInstance(); // Singleton instance of Login class
    private User currentUser; // Currently logged-in user

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("CSE 360 Help System");

        // Display the login screen when the application starts
        showLoginScreen();
    }

    // Method to display the login screen
    private void showLoginScreen() {
        VBox vbox = new VBox(10); // Vertical box layout with spacing of 10 pixels

        // Username input field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        // Password input field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Login button
        Button loginButton = new Button("Login");

        // Event handler for the login button
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // If no users exist, register the first user as an Admin
            if (Login.getInstance().listUsers().isEmpty()) {
                User newUser = Login.getInstance().registerUser(username, password, "Admin", false, null);
                currentUser = newUser; // Set the current user
                showRegistrationScreen(newUser);
            } else {
                // Authenticate the user
                boolean isAuthenticated = Login.getInstance().authenticate(username, password);
                if (isAuthenticated) {
                    User user = Login.getInstance().findUser(username);
                    currentUser = user; // Set the current user
                    if (user != null && !user.isAccountSetupComplete()) {
                        // If account setup is incomplete, show the registration screen
                        showRegistrationScreen(user);
                    } else {
                        // Show role selection screen
                        showRoleSelectionScreen(user);
                    }
                } else {
                    System.out.println("Login failed. If this is a one-time password, it may have expired.");
                }
            }
        });

        // Add components to the layout
        vbox.getChildren().addAll(usernameField, passwordField, loginButton);

        // Set the scene and display the login screen
        Scene loginScene = new Scene(vbox, 400, 300);
        window.setScene(loginScene);
        window.show();
    }

    // Method to display the registration screen for account setup
    private void showRegistrationScreen(User user) {
        VBox vbox = new VBox(10);

        // Input fields for user details
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField middleNameField = new TextField();
        middleNameField.setPromptText("Middle Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField preferredNameField = new TextField();
        preferredNameField.setPromptText("Preferred Name");

        // Map to store proficiency levels for topics
        Map<String, ComboBox<String>> topicComboBoxes = new HashMap<>();
        for (String topic : user.getTopics().keySet()) {
            // ComboBox for selecting proficiency level
            ComboBox<String> topicLevelBox = new ComboBox<>();
            topicLevelBox.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
            topicLevelBox.setValue(user.getTopicProficiency(topic)); // Set current proficiency level
            topicComboBoxes.put(topic, topicLevelBox);

            // Label and ComboBox for each topic
            Label topicLabel = new Label(topic);
            vbox.getChildren().addAll(topicLabel, topicLevelBox);
        }

        // Button to complete account setup
        Button registerButton = new Button("Complete Setup");

        // Event handler for the register button
        registerButton.setOnAction(e -> {
            // Set user details
            user.setEmail(emailField.getText());
            user.setFirstName(firstNameField.getText());
            user.setMiddleName(middleNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setPreferredName(preferredNameField.getText());

            // Set proficiency levels for topics
            for (String topic : topicComboBoxes.keySet()) {
                user.setTopicProficiency(topic, topicComboBoxes.get(topic).getValue());
            }

            user.setAccountSetupComplete(true); // Mark account setup as complete
            System.out.println("Account setup completed.");
            showRoleSelectionScreen(user); // Proceed to role selection screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(emailField, firstNameField, middleNameField, lastNameField, preferredNameField, registerButton);

        // Set the scene and display the registration screen
        Scene registerScene = new Scene(vbox, 400, 400);
        window.setScene(registerScene);
        window.show();
    }

    // Method to display the role selection screen
    private void showRoleSelectionScreen(User user) {
        VBox vbox = new VBox(10);

        // ComboBox to select the user's role
        ComboBox<String> roleDropdown = new ComboBox<>();
        roleDropdown.getItems().addAll(user.getRole()); // User's available roles
        roleDropdown.setPromptText("Select Role");

        // Button to confirm role selection
        Button selectButton = new Button("Select Role");

        // Event handler for the select button
        selectButton.setOnAction(e -> {
            String selectedRole = roleDropdown.getValue();
            System.out.println("Role Selected: " + selectedRole);
            if ("Admin".equals(selectedRole)) {
                showAdminDashboard(user); // Show admin dashboard
            } else if ("Instructor".equals(selectedRole)) {
                showInstructorDashboard(user); // Show instructor dashboard
            } else {
                showSimpleHomePage(user); // Show student home page
            }
        });

        // Add components to the layout
        vbox.getChildren().addAll(new Label("Select Role"), roleDropdown, selectButton);

        // Set the scene and display the role selection screen
        Scene roleScene = new Scene(vbox, 400, 200);
        window.setScene(roleScene);
        window.show();
    }

    // Method to display the student's home page
    private void showSimpleHomePage(User user) {
        currentUser = user; // Set the current user

        VBox vbox = new VBox(10);
        Button logoutButton = new Button("Logout");

        // Search functionality components
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");
        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            if (keyword != null && !keyword.isEmpty()) {
                List<User.HelpArticle> results = currentUser.searchHelpArticles(keyword.trim());
                articlesListView.getItems().clear();
                for (User.HelpArticle article : results) {
                    articlesListView.getItems().add(article.getTitle());
                }
            }
        });

        // Event handler for listing all articles
        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
            }
        });

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Home Page"),
                new Separator(), // Visual separator
                searchField, searchButton, listArticlesButton, articlesListView,
                new Separator(),
                logoutButton);

        // Set the scene and display the home page
        Scene homeScene = new Scene(vbox, 600, 800);
        window.setScene(homeScene);
        window.show();
    }

    // Method to display the instructor dashboard
    private void showInstructorDashboard(User user) {
        currentUser = user; // Set the current user

        VBox vbox = new VBox(10);
        Button logoutButton = new Button("Logout");

        // Input fields for article details
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Body of the article");

        TextField groupsField = new TextField();
        groupsField.setPromptText("Groups (comma-separated)");

        TextField levelField = new TextField();
        levelField.setPromptText("Level (Beginner, Intermediate, Advanced, Expert)");

        // Button to add a new article
        Button addArticleButton = new Button("Add Article");
        addArticleButton.setOnAction(e -> addArticle(
                titleField.getText(), descriptionField.getText(),
                Arrays.asList(keywordsField.getText().split(",")),
                bodyArea.getText(), new ArrayList<>(),
                Arrays.asList(groupsField.getText().split(",")), levelField.getText()));

        // Search functionality components
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");
        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles

        // Map to associate article titles with their IDs for deletion
        Map<String, Long> articleTitleToIdMap = new HashMap<>();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            if (keyword != null && !keyword.isEmpty()) {
                List<User.HelpArticle> results = currentUser.searchHelpArticles(keyword.trim());
                articlesListView.getItems().clear();
                articleTitleToIdMap.clear();
                for (User.HelpArticle article : results) {
                    articlesListView.getItems().add(article.getTitle());
                    articleTitleToIdMap.put(article.getTitle(), article.getId());
                }
            }
        });

        // Event handler for listing all articles
        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            articleTitleToIdMap.clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
                articleTitleToIdMap.put(article.getTitle(), article.getId());
            }
        });

        // Button to delete the selected article
        Button deleteArticleButton = new Button("Delete Selected Article");
        deleteArticleButton.setOnAction(e -> {
            String selectedTitle = articlesListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                Long articleId = articleTitleToIdMap.get(selectedTitle);
                if (articleId != null) {
                    currentUser.removeHelpArticle(articleId); // Remove article from user's list
                    System.out.println("Article Deleted: " + selectedTitle);
                    articlesListView.getItems().remove(selectedTitle); // Remove from ListView
                }
            } else {
                System.out.println("No article selected for deletion.");
            }
        });

        // Buttons for backup and restore functionality
        Button backupButton = new Button("Backup Articles");
        backupButton.setOnAction(e -> backupArticles());

        Button restoreButton = new Button("Restore Articles");
        restoreButton.setOnAction(e -> restoreArticles());

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Instructor Dashboard"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                addArticleButton,
                new Separator(),
                searchField, searchButton, listArticlesButton, articlesListView, deleteArticleButton,
                new Separator(),
                backupButton, restoreButton, logoutButton);

        // Set the scene and display the instructor dashboard
        Scene instructorScene = new Scene(vbox, 600, 800);
        window.setScene(instructorScene);
        window.show();
    }

    // Method to display the admin's article management dashboard
    private void showArticleDashboard() {
        VBox vbox = new VBox(10);

        // Input fields for article details
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Body of the article");

        TextField groupsField = new TextField();
        groupsField.setPromptText("Groups (comma-separated)");

        TextField levelField = new TextField();
        levelField.setPromptText("Level (Beginner, Intermediate, Advanced, Expert)");

        // Button to add a new article
        Button addArticleButton = new Button("Add Article");
        addArticleButton.setOnAction(e -> addArticle(
                titleField.getText(), descriptionField.getText(),
                Arrays.asList(keywordsField.getText().split(",")),
                bodyArea.getText(), new ArrayList<>(),
                Arrays.asList(groupsField.getText().split(",")), levelField.getText()));

        // Search functionality components
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");
        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles

        // Map to associate article titles with their IDs for deletion
        Map<String, Long> articleTitleToIdMap = new HashMap<>();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            if (keyword != null && !keyword.isEmpty()) {
                List<User.HelpArticle> results = currentUser.searchHelpArticles(keyword.trim());
                articlesListView.getItems().clear();
                articleTitleToIdMap.clear();
                for (User.HelpArticle article : results) {
                    articlesListView.getItems().add(article.getTitle());
                    articleTitleToIdMap.put(article.getTitle(), article.getId());
                }
            }
        });

        // Event handler for listing all articles
        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            articleTitleToIdMap.clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
                articleTitleToIdMap.put(article.getTitle(), article.getId());
            }
        });

        // Button to delete the selected article
        Button deleteArticleButton = new Button("Delete Selected Article");
        deleteArticleButton.setOnAction(e -> {
            String selectedTitle = articlesListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                Long articleId = articleTitleToIdMap.get(selectedTitle);
                if (articleId != null) {
                    currentUser.removeHelpArticle(articleId); // Remove article from user's list
                    System.out.println("Article Deleted: " + selectedTitle);
                    articlesListView.getItems().remove(selectedTitle); // Remove from ListView
                }
            } else {
                System.out.println("No article selected for deletion.");
            }
        });

        // Buttons for backup and restore functionality
        Button backupButton = new Button("Backup Articles");
        backupButton.setOnAction(e -> backupArticles());

        Button restoreButton = new Button("Restore Articles");
        restoreButton.setOnAction(e -> restoreArticles());

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Article Dashboard"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                addArticleButton,
                new Separator(),
                searchField, searchButton, listArticlesButton, articlesListView, deleteArticleButton,
                new Separator(),
                backupButton, restoreButton, logoutButton
        );

        // Set the scene and display the article dashboard
        Scene articleScene = new Scene(vbox, 600, 800);
        window.setScene(articleScene);
        window.show();
    }

    // Method to add a new article to the current user's list
    private void addArticle(String title, String description, List<String> keywords, String body, List<String> links, List<String> groups, String level) {
        if (currentUser != null) {
            User.HelpArticle newArticle = new User.HelpArticle(System.currentTimeMillis(), title, description, keywords, body, links, groups, level);
            currentUser.addHelpArticle(newArticle); // Add the new article
            System.out.println("Article Added: " + title);
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    // Method to backup articles to a file
    private void backupArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        File file = fileChooser.showSaveDialog(window); // Show save dialog
        if (file != null) {
            loginInstance.backupHelpArticles(file.getAbsolutePath()); // Backup articles
        }
    }

    // Method to restore articles from a file
    private void restoreArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Backup File");
        File file = fileChooser.showOpenDialog(window); // Show open dialog
        if (file != null) {
            // Confirmation dialog for merge option
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to merge with existing articles?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                boolean merge = response == ButtonType.YES;
                loginInstance.restoreHelpArticles(file.getAbsolutePath(), merge); // Restore articles
            });
        }
    }

    // Method to display the admin dashboard
    private void showAdminDashboard(User user) {
        currentUser = user; // Set the current user
        VBox vbox = new VBox(10);

        // Input fields for user management
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");

        TextField newUserPasswordField = new TextField();
        newUserPasswordField.setPromptText("Enter Password");

        // ComboBox to select user role
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Instructor");
        roleComboBox.setValue("Student"); // Default role

        // Checkbox and input for one-time password
        CheckBox oneTimePasswordCheckBox = new CheckBox("One-Time Password");
        TextField otpExpiryField = new TextField();
        otpExpiryField.setPromptText("OTP Expiry (YYYY-MM-DD HH:MM)");

        // Buttons for user management
        Button addUserButton = new Button("Add User");
        Button deleteUserButton = new Button("Delete User");
        Button resetPasswordButton = new Button("Reset Password");
        Button listUsersButton = new Button("List Users");
        Button addArticleButton = new Button("Manage Articles");
        Button logoutButton = new Button("Logout");

        // Event handler for adding a user
        addUserButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = newUserPasswordField.getText();
            String role = roleComboBox.getValue();
            boolean isOneTimePassword = oneTimePasswordCheckBox.isSelected();
            LocalDateTime otpExpiry = null;

            // Parse OTP expiry date if necessary
            if (isOneTimePassword) {
                try {
                    otpExpiry = LocalDateTime.parse(otpExpiryField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception ex) {
                    System.out.println("Invalid expiry format. Use: YYYY-MM-DD HH:MM");
                    return;
                }
            }

            // Register the new user
            if (!username.isEmpty() && !password.isEmpty()) {
                Login.getInstance().registerUser(username, password, role, isOneTimePassword, otpExpiry);
                System.out.println("User added successfully.");
            } else {
                System.out.println("Please enter a username and password.");
            }
        });

        // Event handler for deleting a user
        deleteUserButton.setOnAction(e -> {
            String usernameToDelete = usernameField.getText();
            if (!usernameToDelete.isEmpty()) {
                boolean isDeleted = Login.getInstance().deleteUser(usernameToDelete);
                if (isDeleted) {
                    System.out.println("User deleted successfully.");
                } else {
                    System.out.println("Failed to delete user.");
                }
            } else {
                System.out.println("Please enter a username.");
            }
        });

        // Event handler for resetting a user's password
        resetPasswordButton.setOnAction(e -> {
            String usernameToReset = usernameField.getText();
            if (!usernameToReset.isEmpty()) {
                String newPassword = newUserPasswordField.getText();
                if (!newPassword.isEmpty()) {
                    boolean isReset = Login.getInstance().resetPassword(usernameToReset, newPassword);
                    if (isReset) {
                        System.out.println("Password reset successfully.");
                    } else {
                        System.out.println("Failed to reset password. User may not exist.");
                    }
                } else {
                    System.out.println("Please enter a new password.");
                }
            } else {
                System.out.println("Please enter a username.");
            }
        });

        // Event handler for listing all users
        listUsersButton.setOnAction(e -> {
            System.out.println("Listing all users:");
            for (User u : Login.getInstance().listUsers()) {
                System.out.println("Username: " + u.getUsername() + ", Role: " + u.getRole());
            }
        });

        // Event handler to manage articles
        addArticleButton.setOnAction(e -> {
            showArticleDashboard(); // Show article management dashboard
        });

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Admin Dashboard"),
                usernameField,
                newUserPasswordField,
                roleComboBox,
                oneTimePasswordCheckBox,
                otpExpiryField,
                addUserButton,
                deleteUserButton,
                resetPasswordButton,
                listUsersButton,
                addArticleButton,
                logoutButton
        );

        // Set the scene and display the admin dashboard
        Scene adminScene = new Scene(vbox, 400, 600);
        window.setScene(adminScene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}
