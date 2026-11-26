package org.example.model.entity;

import org.example.model.exception.ValidationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a product category in the store according to User Stories 13 & 14.
 */
public class Category {
    private final String id;
    private String name;
    private String description;
    private boolean active;

    public Category(String id, String name, String description, boolean active) {
        validateName(name);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.name = name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.active = active;
    }

    public Category(String name, String description) {
        this(UUID.randomUUID().toString(), name, description, true);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("نام دسته‌بندی نباید خالی باشد.");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + (active ? "" : " (غیرفعال)");
    }
}
