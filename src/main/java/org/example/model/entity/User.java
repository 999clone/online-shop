package org.example.model.entity;

import org.example.model.enums.UserRole;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entity representing an account in the system (Customer or Admin).
 * Encapsulates address management and profile editing rules.
 */
public class User {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private final List<Address> addresses = new ArrayList<>();
    private final LocalDateTime createdAt;

    public User(String id, String fullName, String email, String phoneNumber,
                String passwordHash, UserRole role, boolean active, LocalDateTime createdAt) {
        validateRegistration(email, phoneNumber, passwordHash);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.fullName = (fullName == null || fullName.isBlank()) ? "کاربر گرامی" : fullName.trim();
        this.email = (email != null && !email.isBlank()) ? email.trim() : null;
        this.phoneNumber = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber.trim() : null;
        this.passwordHash = passwordHash;
        this.role = (role == null) ? UserRole.CUSTOMER : role;
        this.active = active;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
    }

    public User(String fullName, String email, String phoneNumber, String passwordHash, UserRole role) {
        this(UUID.randomUUID().toString(), fullName, email, phoneNumber, passwordHash, role, true, LocalDateTime.now());
    }

    private void validateRegistration(String email, String phoneNumber, String passwordHash) {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phoneNumber != null && !phoneNumber.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new ValidationException("کاربر باید حداقل ایمیل یا شماره موبایل وارد کند.");
        }
        if (hasEmail && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("فرمت ایمیل نامعتبر است.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("رمز عبور نباید خالی باشد.");
        }
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public void updateProfile(String newFullName, String newEmail, String newPhone) {
        if (newFullName == null || newFullName.isBlank()) {
            throw new ValidationException("نام کامل نمی‌تواند خالی باشد.");
        }
        boolean hasEmail = newEmail != null && !newEmail.isBlank();
        boolean hasPhone = newPhone != null && !newPhone.isBlank();
        if (!hasEmail && !hasPhone) {
            throw new ValidationException("حداقل ایمیل یا شماره موبایل باید ثبت شده باشد.");
        }
        if (hasEmail && !EMAIL_PATTERN.matcher(newEmail.trim()).matches()) {
            throw new ValidationException("فرمت ایمیل نامعتبر است.");
        }
        this.fullName = newFullName.trim();
        this.email = hasEmail ? newEmail.trim() : null;
        this.phoneNumber = hasPhone ? newPhone.trim() : null;
    }

    public void setPasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new ValidationException("رمز عبور جدید نمی‌تواند خالی باشد.");
        }
        this.passwordHash = newPasswordHash;
    }

    public void addAddress(Address address) {
        if (address == null) {
            throw new ValidationException("آدرس نامعتبر است.");
        }
        if (address.isDefault() || addresses.isEmpty()) {
            clearDefaultAddresses();
            addresses.add(address.withDefault(true));
        } else {
            addresses.add(address);
        }
    }

    public void updateAddress(String addressId, Address updatedAddress) {
        int index = findAddressIndex(addressId);
        if (index == -1) {
            throw new ValidationException("آدرس موردنظر برای ویرایش یافت نشد.");
        }
        if (updatedAddress.isDefault()) {
            clearDefaultAddresses();
        }
        addresses.set(index, updatedAddress);
    }

    public void removeAddress(String addressId) {
        int index = findAddressIndex(addressId);
        if (index == -1) {
            throw new ValidationException("آدرس موردنظر برای حذف یافت نشد.");
        }
        boolean wasDefault = addresses.get(index).isDefault();
        addresses.remove(index);
        if (wasDefault && !addresses.isEmpty()) {
            // Set the first remaining address as default
            Address first = addresses.get(0);
            addresses.set(0, first.withDefault(true));
        }
    }

    public Address getAddressById(String addressId) {
        int index = findAddressIndex(addressId);
        if (index == -1) {
            return null;
        }
        return addresses.get(index);
    }

    public Address getDefaultAddress() {
        for (Address addr : addresses) {
            if (addr.isDefault()) {
                return addr;
            }
        }
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    private void clearDefaultAddresses() {
        for (int i = 0; i < addresses.size(); i++) {
            Address cur = addresses.get(i);
            if (cur.isDefault()) {
                addresses.set(i, cur.withDefault(false));
            }
        }
    }

    private int findAddressIndex(String addressId) {
        if (addressId == null) return -1;
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getId().equals(addressId)) {
                return i;
            }
        }
        return -1;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "کاربر: " + fullName + " | نقش: " + role + " | تماس: " + (email != null ? email : phoneNumber);
    }
}
