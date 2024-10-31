package application;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Login {

    private List<User> users = new ArrayList<>();
    private static Login instance = null;

    public static Login getInstance() {
        if (instance == null) {
            instance = new Login();
        }
        return instance;
    }

    private Login() {}

    // Authenticate user with a check for one-time password validity
    public boolean authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                // Check if the password is a one-time password and if it's expired
                if (user.isOneTimePassword()) {
                    if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
                        System.out.println("One-time password has expired. Please reset your password.");
                        return false;
                    }
                }
                // Authenticate based on the password
                if (new String(user.getPassword()).equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    public User registerUser(String username, String password, String role, boolean isOneTimePassword, LocalDateTime otpExpiry) {
        User newUser = new User(username, password, role);
        newUser.setOneTimePassword(isOneTimePassword);
        newUser.setOtpExpiry(otpExpiry);
        users.add(newUser);
        return newUser;
    }

    // Delete a user by username
    public boolean deleteUser(String usernameToDelete) {
        return users.removeIf(user -> user.getUsername().equals(usernameToDelete));
    }

    // Reset a user's password
    public boolean resetPassword(String usernameToReset, String newPassword) {
        for (User user : users) {
            if (user.getUsername().equals(usernameToReset)) {
                user.setPassword(newPassword.getBytes());
                user.setOneTimePassword(false);  // Reset to regular password
                user.setOtpExpiry(null);  // Clear OTP expiry
                return true;
            }
        }
        return false;
    }

    // List all users
    public List<User> listUsers() {
        return new ArrayList<>(users);
    }

    // Find user by username
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    // Backup HelpArticles to a specified file
    public void backupHelpArticles(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            List<User.HelpArticle> allArticles = new ArrayList<>();
            for (User user : users) {
                allArticles.addAll(user.getAllHelpArticles());
            }
            oos.writeObject(allArticles);
            System.out.println("Backup completed successfully.");
        } catch (IOException e) {
            System.out.println("Error during backup: " + e.getMessage());
        }
    }

    // Restore HelpArticles from a backup file
    public void restoreHelpArticles(String filename, boolean merge) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<User.HelpArticle> restoredArticles = (List<User.HelpArticle>) ois.readObject();
            for (User user : users) {
                if (merge) {
                    for (User.HelpArticle article : restoredArticles) {
                        boolean exists = user.getAllHelpArticles().stream()
                                .anyMatch(existingArticle -> existingArticle.getId() == article.getId());
                        if (!exists) {
                            user.addHelpArticle(article);
                        }
                    }
                } else {
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
