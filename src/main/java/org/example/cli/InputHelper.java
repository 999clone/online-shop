package org.example.cli;

import java.util.Scanner;

/**
 * Utility helper for robust command-line inputs.
 */
public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String prompt) {
        System.out.print(prompt + ": ");
        String line = scanner.nextLine();
        return (line == null) ? "" : line.trim();
    }

    public String readOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String line = scanner.nextLine();
        if (line == null || line.isBlank()) {
            return defaultValue;
        }
        return line.trim();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " (" + min + " تا " + max + "): ");
            String line = scanner.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("❌ عدد وارد شده باید بین " + min + " و " + max + " باشد.");
            } catch (NumberFormatException e) {
                System.out.println("❌ لطفاً یک عدد معتبر وارد کنید.");
            }
        }
    }

    public Integer readOptionalInt(String prompt, Integer min, Integer max) {
        while (true) {
            System.out.print(prompt + " (اینتر برای رد شدن): ");
            String line = scanner.nextLine();
            if (line == null || line.isBlank()) {
                return null;
            }
            try {
                int val = Integer.parseInt(line.trim());
                if ((min == null || val >= min) && (max == null || val <= max)) {
                    return val;
                }
                System.out.println("❌ عدد خارج از بازه مجاز است.");
            } catch (NumberFormatException e) {
                System.out.println("❌ عدد نامعتبر است.");
            }
        }
    }

    public double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (val >= min) {
                    return val;
                }
                System.out.println("❌ مقدار نباید کمتر از " + min + " باشد.");
            } catch (NumberFormatException e) {
                System.out.println("❌ لطفاً یک عدد اعشاری یا صحیح معتبر وارد کنید.");
            }
        }
    }

    public Double readOptionalDouble(String prompt, Double min) {
        while (true) {
            System.out.print(prompt + " (اینتر برای رد شدن): ");
            String line = scanner.nextLine();
            if (line == null || line.isBlank()) {
                return null;
            }
            try {
                double val = Double.parseDouble(line.trim());
                if (min == null || val >= min) {
                    return val;
                }
                System.out.println("❌ مقدار نباید کمتر از " + min + " باشد.");
            } catch (NumberFormatException e) {
                System.out.println("❌ عدد نامعتبر است.");
            }
        }
    }

    public boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (بله/خیر یا y/n): ");
            String line = scanner.nextLine();
            if (line == null) return false;
            line = line.trim().toLowerCase();
            if (line.equals("y") || line.equals("yes") || line.equals("بله") || line.equals("1")) {
                return true;
            }
            if (line.equals("n") || line.equals("no") || line.equals("خیر") || line.equals("0")) {
                return false;
            }
            System.out.println("❌ پاسخ نامعتبر است. لطفاً 'بله' یا 'خیر' وارد کنید.");
        }
    }
}
