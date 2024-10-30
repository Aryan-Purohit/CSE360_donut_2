package application;

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

    private Stage window;
    private Login loginInstance = Login.getInstance();
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("CSE 360 Help System");

        // Display the login screen initially
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox vbox = new VBox(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (Login.getInstance().listUsers().isEmpty()) {
                User newUser = Login.getInstance().registerUser(username, password, false, null);
                currentUser = newUser; // Set currentUser here
                showRegistrationScreen(newUser);
            } else {
                boolean isAuthenticated = Login.getInstance().authenticate(username, password);
                if (isAuthenticated) {
                    User user = Login.getInstance().findUser(username);
                    currentUser = user; // Set currentUser
                    if (user != null && !user.isAccountSetupComplete()) {
                        showRegistrationScreen(user);
                    } else {
                        showRoleSelectionScreen(user);
                    }
                } else {
                    System.out.println("Login failed. If this is a one-time password, it may have expired.");
                }
            }
        });

        vbox.getChildren().addAll(usernameField, passwordField, loginButton);
        Scene loginScene = new Scene(vbox, 400, 300);
        window.setScene(loginScene);
        window.show();
    }

    private void showRegistrationScreen(User user) {
        VBox vbox = new VBox(10);

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

        // Proficiency levels for system-recognized topics
        Map<String, ComboBox<String>> topicComboBoxes = new HashMap<>();
        for (String topic : user.getTopics().keySet()) {
            ComboBox<String> topicLevelBox = new ComboBox<>();
            topicLevelBox.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
            topicLevelBox.setValue(user.getTopicProficiency(topic)); // Set to user's current proficiency
            topicComboBoxes.put(topic, topicLevelBox);

            Label topicLabel = new Label(topic);
            vbox.getChildren().addAll(topicLabel, topicLevelBox);
        }

        Button registerButton = new Button("Complete Setup");

        registerButton.setOnAction(e -> {
            user.setEmail(emailField.getText());
            user.setFirstName(firstNameField.getText());
            user.setMiddleName(middleNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setPreferredName(preferredNameField.getText());

            // Set the topic proficiency for each topic
            for (String topic : topicComboBoxes.keySet()) {
                user.setTopicProficiency(topic, topicComboBoxes.get(topic).getValue());
            }

            user.setAccountSetupComplete(true);
            System.out.println("Account setup completed.");
            showRoleSelectionScreen(user);
        });

        vbox.getChildren().addAll(emailField, firstNameField, middleNameField, lastNameField, preferredNameField, registerButton);
        Scene registerScene = new Scene(vbox, 400, 400);
        window.setScene(registerScene);
        window.show();
    }

    private void showRoleSelectionScreen(User user) {
        VBox vbox = new VBox(10);

        ComboBox<String> roleDropdown = new ComboBox<>();
        roleDropdown.getItems().addAll(user.getRole());
        roleDropdown.setPromptText("Select Role");

        Button selectButton = new Button("Select Role");

        selectButton.setOnAction(e -> {
            String selectedRole = roleDropdown.getValue();
            System.out.println("Role Selected: " + selectedRole);
            if ("Admin".equals(selectedRole)) {
                showAdminDashboard(user); // Pass the user object
            } else {
                showSimpleHomePage(user); // Pass the user object
            }
        });

        vbox.getChildren().addAll(new Label("Select Role"), roleDropdown, selectButton);
        Scene roleScene = new Scene(vbox, 400, 200);
        window.setScene(roleScene);
        window.show();
    }

    private void showSimpleHomePage(User user) {
        currentUser = user; // Set currentUser

        VBox vbox = new VBox(10);
        Button logoutButton = new Button("Logout");

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

        Button addArticleButton = new Button("Add Article");
        addArticleButton.setOnAction(e -> addArticle(
                titleField.getText(), descriptionField.getText(),
                Arrays.asList(keywordsField.getText().split(",")),
                bodyArea.getText(), new ArrayList<>(),
                Arrays.asList(groupsField.getText().split(",")), levelField.getText()));

        // Search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");

        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>();

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

        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
            }
        });

        Button backupButton = new Button("Backup Articles");
        backupButton.setOnAction(e -> backupArticles());

        Button restoreButton = new Button("Restore Articles");
        restoreButton.setOnAction(e -> restoreArticles());

        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen();
        });

        vbox.getChildren().addAll(
                new Label("Home Page"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                addArticleButton,
                new Separator(),
                searchField, searchButton, listArticlesButton, articlesListView,
                new Separator(),
                backupButton, restoreButton, logoutButton);

        Scene homeScene = new Scene(vbox, 600, 800);
        window.setScene(homeScene);
        window.show();
    }

    private void showArticleDashboard() {
        VBox vbox = new VBox(10);

        // Fields for Article Management
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

        Button addArticleButton = new Button("Add Article");
        addArticleButton.setOnAction(e -> addArticle(
                titleField.getText(), descriptionField.getText(),
                Arrays.asList(keywordsField.getText().split(",")),
                bodyArea.getText(), new ArrayList<>(),
                Arrays.asList(groupsField.getText().split(",")), levelField.getText()));

        // Search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");

        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>();

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

        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
            }
        });

        Button backupButton = new Button("Backup Articles");
        backupButton.setOnAction(e -> backupArticles());

        Button restoreButton = new Button("Restore Articles");
        restoreButton.setOnAction(e -> restoreArticles());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen();
        });

        vbox.getChildren().addAll(
                new Label("Article Dashboard"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                addArticleButton,
                new Separator(),
                searchField, searchButton, listArticlesButton, articlesListView,
                new Separator(),
                backupButton, restoreButton, logoutButton
        );

        Scene articleScene = new Scene(vbox, 600, 800);
        window.setScene(articleScene);
        window.show();
    }

    private void addArticle(String title, String description, List<String> keywords, String body, List<String> links, List<String> groups, String level) {
        if (currentUser != null) {
            User.HelpArticle newArticle = new User.HelpArticle(System.currentTimeMillis(), title, description, keywords, body, links, groups, level);
            currentUser.addHelpArticle(newArticle);
            System.out.println("Article Added: " + title);
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    private void backupArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            loginInstance.backupHelpArticles(file.getAbsolutePath());
        }
    }

    private void restoreArticles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Backup File");
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to merge with existing articles?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                boolean merge = response == ButtonType.YES;
                loginInstance.restoreHelpArticles(file.getAbsolutePath(), merge);
            });
        }
    }

    private void showAdminDashboard(User user) {
        currentUser = user; // Set currentUser
        VBox vbox = new VBox(10);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");

        TextField newUserPasswordField = new TextField();
        newUserPasswordField.setPromptText("Enter Password");

        CheckBox oneTimePasswordCheckBox = new CheckBox("One-Time Password");
        TextField otpExpiryField = new TextField();
        otpExpiryField.setPromptText("OTP Expiry (YYYY-MM-DD HH:MM)");

        Button addUserButton = new Button("Add User");
        Button deleteUserButton = new Button("Delete User");
        Button resetPasswordButton = new Button("Reset Password");
        Button listUsersButton = new Button("List Users");
        Button addArticleButton = new Button("Add Article");
        Button logoutButton = new Button("Logout");

        addUserButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = newUserPasswordField.getText();
            boolean isOneTimePassword = oneTimePasswordCheckBox.isSelected();
            LocalDateTime otpExpiry = null;

            if (isOneTimePassword) {
                try {
                    otpExpiry = LocalDateTime.parse(otpExpiryField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception ex) {
                    System.out.println("Invalid expiry format. Use: YYYY-MM-DD HH:MM");
                    return;
                }
            }

            if (!username.isEmpty() && !password.isEmpty()) {
                Login.getInstance().registerUser(username, password, isOneTimePassword, otpExpiry);
                System.out.println("User added successfully.");
            } else {
                System.out.println("Please enter a username and password.");
            }
        });

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

        listUsersButton.setOnAction(e -> {
            System.out.println("Listing all users:");
            for (User u : Login.getInstance().listUsers()) {
                System.out.println("Username: " + u.getUsername() + ", Role: " + u.getRole());
            }
        });

        addArticleButton.setOnAction(e -> {
            showArticleDashboard();
        });

        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen();
        });

        vbox.getChildren().addAll(
                new Label("Admin Dashboard"),
                usernameField,
                newUserPasswordField,
                oneTimePasswordCheckBox,
                otpExpiryField,
                addUserButton,
                deleteUserButton,
                resetPasswordButton,
                listUsersButton,
                addArticleButton,
                logoutButton
        );

        Scene adminScene = new Scene(vbox, 400, 600);
        window.setScene(adminScene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
