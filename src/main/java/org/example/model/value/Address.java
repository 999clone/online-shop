package org.example.model.value;

import org.example.model.exception.ValidationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a postal address for shipping according to User Story 7.
 */
public class Address {
    private final String id;
    private final String recipientName;
    private final String phoneNumber;
    private final String country;
    private final String city;
    private final String streetAddress;
    private final String postalCode;
    private final String title;
    private final boolean isDefault;

    public Address(String id, String recipientName, String phoneNumber, String country,
                   String city, String streetAddress, String postalCode, String title, boolean isDefault) {
        validate(recipientName, phoneNumber, country, city, streetAddress, postalCode, title);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.recipientName = recipientName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.country = country.trim();
        this.city = city.trim();
        this.streetAddress = streetAddress.trim();
        this.postalCode = postalCode.trim();
        this.title = title.trim();
        this.isDefault = isDefault;
    }

    public Address(String recipientName, String phoneNumber, String country,
                   String city, String streetAddress, String postalCode, String title, boolean isDefault) {
        this(UUID.randomUUID().toString(), recipientName, phoneNumber, country, city, streetAddress, postalCode, title, isDefault);
    }

    private void validate(String recipientName, String phoneNumber, String country,
                          String city, String streetAddress, String postalCode, String title) {
        if (recipientName == null || recipientName.isBlank()) {
            throw new ValidationException("نام گیرنده نمی‌تواند خالی باشد.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ValidationException("شماره تلفن گیرنده نمی‌تواند خالی باشد.");
        }
        if (country == null || country.isBlank()) {
            throw new ValidationException("کشور نمی‌تواند خالی باشد.");
        }
        if (city == null || city.isBlank()) {
            throw new ValidationException("شهر نمی‌تواند خالی باشد.");
        }
        if (streetAddress == null || streetAddress.isBlank()) {
            throw new ValidationException("آدرس خیابان نمی‌تواند خالی باشد.");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new ValidationException("کد پستی نمی‌تواند خالی باشد.");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("عنوان آدرس نمی‌تواند خالی باشد.");
        }
    }

    public String getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Address withDefault(boolean defaultStatus) {
        return new Address(this.id, this.recipientName, this.phoneNumber, this.country,
                this.city, this.streetAddress, this.postalCode, this.title, defaultStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return isDefault == address.isDefault &&
                Objects.equals(id, address.id) &&
                Objects.equals(recipientName, address.recipientName) &&
                Objects.equals(phoneNumber, address.phoneNumber) &&
                Objects.equals(country, address.country) &&
                Objects.equals(city, address.city) &&
                Objects.equals(streetAddress, address.streetAddress) &&
                Objects.equals(postalCode, address.postalCode) &&
                Objects.equals(title, address.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipientName, phoneNumber, country, city, streetAddress, postalCode, title, isDefault);
    }

    @Override
    public String toString() {
        return "[" + title + (isDefault ? " (پیش‌فرض)" : "") + "] " +
                recipientName + " (" + phoneNumber + ") - " +
                country + "، " + city + "، " + streetAddress + "، کد پستی: " + postalCode;
    }
}
