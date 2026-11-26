package org.example.cli;

import org.example.model.entity.User;

/**
 * Abstraction for role-specific CLI menu handlers (OCP, LSP, ISP).
 */
public interface MenuHandler {
    boolean supports(User currentUser);
    boolean handle(SessionContext context);
}
