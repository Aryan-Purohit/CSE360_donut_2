package application;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Login {

    private List<User> users = new ArrayList<>(); // List to store users
    private static Login instance = null; // Singleton instance

    // Method to get the singleton instance
    public static Login getInstance() {
        if (instance == null) {
            instance = new Login();
        }
        return instance;
    }

    private Login() {} // Private constructor to prevent instantiation

    // Method to authenticate a user
    public boolean authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                // Check for one-time password expiry
                if (user.isOneTimePassword()) {
                    if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
                        System.out.println("One-time password has expired. Please reset your password.");
                        return false;
                    }
                }
                // Check if the password matches
                if (new String(user.getPassword()).equals(password)) {
                    return true; // Authentication successful
                }
            }
        }
        return false; // Authentication failed
    }

    // Method to register a new user
    public User registerUser(String username, String password, String role, boolean isOneTimePassword, LocalDateTime otpExpiry) {
        User newUser = new User(username, password, role);
        newUser.setOneTimePassword(isOneTimePassword);
        newUser.setOtpExpiry(otpExpiry);
        users.add(newUser); // Add the new user to the list
        return newUser;
    }

    // Method to delete a user
    public boolean deleteUser(String usernameToDelete) {
        return users.removeIf(user -> user.getUsername().equals(usernameToDelete));
    }

    // Method to reset a user's password
    public boolean resetPassword(String usernameToReset, String newPassword) {
        for (User user : users) {
            if (user.getUsername().equals(usernameToReset)) {
                user.setPassword(newPassword.getBytes());
                user.setOneTimePassword(false); // Reset OTP flag
                user.setOtpExpiry(null); // Clear OTP expiry
                return true;
            }
        }
        return false;
    }

    // Method to list all users
    public List<User> listUsers() {
        return new ArrayList<>(users); // Return a copy of the users list
    }

    // Method to find a user by username
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user; // User found
            }
        }
        return null; // User not found
    }

    // Method to backup help articles to a file
    public void backupHelpArticles(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            List<User.HelpArticle> allArticles = new ArrayList<>();
            // Collect all help articles from all users
            for (User user : users) {
                allArticles.addAll(user.getAllHelpArticles());
            }
            oos.writeObject(allArticles); // Serialize the articles list
            System.out.println("Backup completed successfully.");
        } catch (IOException e) {
            System.out.println("Error during backup: " + e.getMessage());
        }
    }

    // Method to restore help articles from a file
    public void restoreHelpArticles(String filename, boolean merge) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<User.HelpArticle> restoredArticles = (List<User.HelpArticle>) ois.readObject();
            for (User user : users) {
                if (merge) {
                    // Merge articles without duplicates
                    for (User.HelpArticle article : restoredArticles) {
                        boolean exists = user.getAllHelpArticles().stream()
                                .anyMatch(existingArticle -> existingArticle.getId() == article.getId());
                        if (!exists) {
                            user.addHelpArticle(article);
                        }
                    }
                } else {
                    // Replace existing articles with restored ones
                    user.getAllHelpArticles().clear();
                    user.getAllHelpArticles().addAll(restoredArticles);
                }
            }
            System.out.println("Restore completed successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during restore: " + e.getMessage());
        }
    }
}
