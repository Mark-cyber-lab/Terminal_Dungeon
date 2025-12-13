package com.terminaldungeon.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for managing passwords in a secure manner.
 * <p>
 * Features include:
 * <ul>
 *     <li>Setting and storing a password with SHA-256 hashing</li>
 *     <li>Verifying a password against the stored hash</li>
 *     <li>Prompting the user for password input silently (without echo), when supported</li>
 * </ul>
 * <p>
 * This class can be used for implementing sudo-like functionality or any
 * other password-protected commands in a sandboxed environment.
 */
public class PasswordManager {

    /** Stores the hashed password in Base64 encoding */
    private String hashedPassword;

    /**
     * Creates a PasswordManager with a custom initial password.
     *
     * @param password the initial password to set
     * @throws IllegalArgumentException if the password is null or empty
     */
    public PasswordManager(String password) {
        setPassword(password);
    }

    /**
     * Creates a PasswordManager with a default password "password".
     * <p>
     * Useful for development or testing purposes. Should be changed
     * to a secure password in production environments.
     */
    public PasswordManager() {
        setPassword("password");
    }

    /**
     * Sets a new password for this manager. The password is hashed and stored.
     *
     * @param newPassword the new password to set
     * @throws IllegalArgumentException if the password is null or empty
     */
    public void setPassword(String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.hashedPassword = hash(newPassword);
    }

    /**
     * Verifies a plain-text password against the stored hashed password.
     *
     * @param password the password to verify
     * @return true if the password matches the stored hash, false otherwise
     */
    public boolean verifyPassword(String password) {
        if (hashedPassword == null) return false;
        return hash(password).equals(hashedPassword);
    }

    /**
     * Hashes a password using SHA-256 and encodes the result in Base64.
     *
     * @param password the plain-text password to hash
     * @return the Base64-encoded hash of the password
     * @throws RuntimeException if SHA-256 algorithm is not supported
     */
    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    /**
     * Prompts the user to enter a password.
     * <p>
     * Attempts to read input silently (no echo) when possible. If the console
     * is not available (e.g., running inside an IDE), input will be visible
     * and a warning is printed.
     *
     * @param promptMessage the message to display before reading input
     * @return the password entered by the user as a String
     */
    public String promptPassword(String promptMessage) {
        System.out.print(promptMessage);

        if (System.console() != null) {
            // Silent input
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars);
        } else {
            // Fallback for IDEs
            System.out.println("[Warning: input will be visible]");
            return IO.readln().trim();
        }
    }
}
