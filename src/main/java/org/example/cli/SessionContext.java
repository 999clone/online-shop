package org.example.cli;

import org.example.model.entity.User;

/**
 * Holds the active user session state for the CLI (SRP).
 */
public class SessionContext {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isGuest() {
        return currentUser == null;
    }
}
