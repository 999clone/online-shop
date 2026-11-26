package org.example.cli;

import org.example.model.exception.DomainException;

import java.util.List;

/**
 * Presentation Layer coordinator dispatching menu handling to role-specific {@link MenuHandler}s (SRP & OCP).
 */
public class ConsoleApp {
    private final List<MenuHandler> menuHandlers;
    private final SessionContext sessionContext;

    public ConsoleApp(List<MenuHandler> menuHandlers) {
        this.menuHandlers = menuHandlers;
        this.sessionContext = new SessionContext();
    }

    public void run() {
        System.out.println("==========================================================");
        System.out.println("      به سامانه فروشگاه آنلاین خوش آمدید      ");
        System.out.println("==========================================================");

        boolean running = true;
        while (running) {
            try {
                MenuHandler handler = menuHandlers.stream()
                        .filter(h -> h.supports(sessionContext.getCurrentUser()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("هیچ منوی مناسبی برای نقش کاربر یافت نشد."));
                running = handler.handle(sessionContext);
            } catch (DomainException e) {
                System.out.println("\n⚠️ خطای دامنه: " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("\n❌ خطای پیش‌بینی‌نشده: " + e.getMessage() + "\n");
            }
        }
        System.out.println("\nاز استفاده شما از فروشگاه آنلاین متشکریم. خدانگهدار!");
    }
}
