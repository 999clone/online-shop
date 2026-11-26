package org.example.service;

import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.repository.UserRepository;
import org.example.service.security.PasswordHasher;

import java.util.List;

/**
 * Service managing user authentication, profile, and addresses (User Stories 1 to 7).
 */
public class UserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * User Story 1: Register a new guest user.
     */
    public User register(String fullName, String email, String phoneNumber, String password, UserRole role) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("رمز عبور الزامی است و نباید خالی باشد.");
        }
        if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new ValidationException("کاربر باید حداقل ایمیل یا شماره موبایل وارد کند.");
        }

        for (User u : userRepository.findAll()) {
            if (email != null && !email.isBlank() && email.trim().equalsIgnoreCase(u.getEmail())) {
                throw new BusinessRuleException("این ایمیل قبلاً در سیستم ثبت شده است.");
            }
            if (phoneNumber != null && !phoneNumber.isBlank() && phoneNumber.trim().equals(u.getPhoneNumber())) {
                throw new BusinessRuleException("این شماره موبایل قبلاً در سیستم ثبت شده است.");
            }
        }

        String hashedPassword = passwordHasher.hash(password);
        User newUser = new User(fullName, email, phoneNumber, hashedPassword, role);
        userRepository.save(newUser);
        return newUser;
    }

    /**
     * User Story 2: Login with contact (email/phone) and password.
     */
    public User login(String contact, String password) {
        if (contact == null || contact.isBlank()) {
            throw new ValidationException("ایمیل یا شماره موبایل نباید خالی باشد.");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("رمز عبور نباید خالی باشد.");
        }

        User user = userRepository.findByContact(contact)
                .orElseThrow(() -> new NotFoundException("اطلاعات ورود اشتباه است؛ حسابی با این ایمیل یا شماره موبایل یافت نشد."));

        if (!user.isActive()) {
            throw new BusinessRuleException("حساب کاربری شما غیرفعال شده است.");
        }
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new BusinessRuleException("رمز عبور وارد شده اشتباه است.");
        }
        return user;
    }

    /**
     * User Story 4: Password recovery.
     */
    public void resetPassword(String contact, String newPassword) {
        if (contact == null || contact.isBlank()) {
            throw new ValidationException("ایمیل یا شماره موبایل نباید خالی باشد.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("رمز عبور جدید نباید خالی باشد.");
        }

        User foundUser = userRepository.findByContact(contact)
                .orElseThrow(() -> new NotFoundException("ایمیل یا شماره موبایل در سیستم ثبت نشده است."));

        foundUser.setPasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(foundUser);
    }

    /**
     * User Story 5: View profile.
     */
    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("کاربر یافت نشد."));
    }

    /**
     * User Story 6: Edit profile.
     */
    public User updateProfile(String userId, String newFullName, String newEmail, String newPhone) {
        User user = getProfile(userId);

        for (User other : userRepository.findAll()) {
            if (!other.getId().equals(userId)) {
                if (newEmail != null && !newEmail.isBlank() && newEmail.trim().equalsIgnoreCase(other.getEmail())) {
                    throw new BusinessRuleException("ایمیل جدید قبلاً توسط کاربر دیگری ثبت شده است.");
                }
                if (newPhone != null && !newPhone.isBlank() && newPhone.trim().equals(other.getPhoneNumber())) {
                    throw new BusinessRuleException("شماره موبایل جدید قبلاً توسط کاربر دیگری ثبت شده است.");
                }
            }
        }

        user.updateProfile(newFullName, newEmail, newPhone);
        userRepository.save(user);
        return user;
    }

    /**
     * User Story 7: Add address.
     */
    public void addAddress(String userId, Address address) {
        User user = getProfile(userId);
        user.addAddress(address);
        userRepository.save(user);
    }

    /**
     * User Story 7: Update address.
     */
    public void updateAddress(String userId, String addressId, Address updatedAddress) {
        User user = getProfile(userId);
        user.updateAddress(addressId, updatedAddress);
        userRepository.save(user);
    }

    /**
     * User Story 7: Delete address.
     */
    public void removeAddress(String userId, String addressId) {
        User user = getProfile(userId);
        user.removeAddress(addressId);
        userRepository.save(user);
    }

    /**
     * User Story 7: List user addresses.
     */
    public List<Address> getUserAddresses(String userId) {
        User user = getProfile(userId);
        return user.getAddresses();
    }
}
