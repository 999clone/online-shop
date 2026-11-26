package org.example;

import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.repository.*;
import org.example.repository.inmemory.*;
import org.example.service.UserService;
import org.example.service.security.PasswordHasher;
import org.example.service.security.Sha256PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering User Stories 1 to 7 (Authentication, Profile, Address Management).
 */
public class UserAndAuthServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        PasswordHasher passwordHasher = new Sha256PasswordHasher();
        UserRepository userRepository = new InMemoryUserRepository();
        new SampleDataSeeder(
                new InMemoryCategoryRepository(),
                userRepository,
                new InMemoryProductRepository(),
                new InMemoryOrderRepository(),
                new InMemoryReviewRepository(),
                passwordHasher
        ).seed();
        userService = new UserService(userRepository, passwordHasher);
    }

    @Test
    @DisplayName("US 1: ثبت‌نام موفق با ایمیل و رمز عبور")
    void testRegisterSuccess() {
        User user = userService.register("علی رضایی", "ali@test.com", "09121112233", "pass1234", UserRole.CUSTOMER);
        assertNotNull(user.getId());
        assertEquals("علی رضایی", user.getFullName());
        assertEquals("ali@test.com", user.getEmail());
        assertNotEquals("pass1234", user.getPasswordHash(), "رمز عبور باید رمزنگاری شده ذخیره شود");
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("US 1: ثبت‌نام با ایمیل تکراری باید خطا دهد")
    void testRegisterDuplicateEmail() {
        assertThrows(BusinessRuleException.class, () ->
                userService.register("تکراری", "customer@shop.com", "09990000000", "pass123", UserRole.CUSTOMER)
        );
    }

    @Test
    @DisplayName("US 1: ثبت‌نام با ایمیل نامعتبر باید خطای اعتبارسنجی دهد")
    void testRegisterInvalidEmail() {
        assertThrows(ValidationException.class, () ->
                userService.register("ناقص", "invalid-email-format", "09990000000", "pass123", UserRole.CUSTOMER)
        );
    }

    @Test
    @DisplayName("US 2: ورود موفق با اطلاعات صحیح")
    void testLoginSuccess() {
        User user = userService.login("customer@shop.com", "user123");
        assertNotNull(user);
        assertEquals("user-cust", user.getId());
    }

    @Test
    @DisplayName("US 2: ورود با رمز اشتباه باید خطا دهد")
    void testLoginWrongPassword() {
        assertThrows(BusinessRuleException.class, () ->
                userService.login("customer@shop.com", "wrongpass")
        );
    }

    @Test
    @DisplayName("US 2: ورود با ایمیل ناموجود باید خطا دهد")
    void testLoginNonExistentUser() {
        assertThrows(NotFoundException.class, () ->
                userService.login("notfound@shop.com", "somepass")
        );
    }

    @Test
    @DisplayName("US 4: بازیابی رمز عبور با ایمیل ثبت شده")
    void testResetPasswordSuccess() {
        userService.resetPassword("customer@shop.com", "newpass123");
        // Login with new password should now succeed
        User u = userService.login("customer@shop.com", "newpass123");
        assertNotNull(u);
    }

    @Test
    @DisplayName("US 4: بازیابی رمز با ایمیل ثبت‌نشده باید خطا دهد")
    void testResetPasswordNonExistent() {
        assertThrows(NotFoundException.class, () ->
                userService.resetPassword("unregistered@shop.com", "newpass123")
        );
    }

    @Test
    @DisplayName("US 5: مشاهده پروفایل بدون نمایش رمز عبور خام")
    void testViewProfile() {
        User u = userService.getProfile("user-cust");
        assertNotNull(u);
        assertEquals("سجاد سارو", u.getFullName());
        assertFalse(u.getPasswordHash().contains("user123"));
    }

    @Test
    @DisplayName("US 6: ویرایش اطلاعات پروفایل")
    void testUpdateProfile() {
        User u = userService.updateProfile("user-cust", "سجاد سارو جدید", "sajjad.new@shop.com", "09351234567");
        assertEquals("سجاد سارو جدید", u.getFullName());
        assertEquals("sajjad.new@shop.com", u.getEmail());
    }

    @Test
    @DisplayName("US 7: مدیریت آدرس‌ها (افزودن، ویرایش، حذف)")
    void testAddressManagement() {
        Address newAddr = new Address("رضا", "09129998877", "ایران", "اصفهان",
                "خیابان چهارباغ، کوچه ۵", "8146512345", "محل کار", false);
        userService.addAddress("user-cust", newAddr);

        var addresses = userService.getUserAddresses("user-cust");
        assertEquals(2, addresses.size());

        // Update address
        Address updatedAddr = new Address(newAddr.getId(), "رضا علوی", "09129998877", "ایران", "اصفهان",
                "خیابان چهارباغ عباسی، پلاک ۱۰", "8146512345", "دفتر مرکزی", true);
        userService.updateAddress("user-cust", newAddr.getId(), updatedAddr);

        var updatedList = userService.getUserAddresses("user-cust");
        Address found = updatedList.stream().filter(a -> a.getId().equals(newAddr.getId())).findFirst().orElseThrow();
        assertEquals("دفتر مرکزی", found.getTitle());
        assertTrue(found.isDefault());

        // Delete address
        userService.removeAddress("user-cust", newAddr.getId());
        assertEquals(1, userService.getUserAddresses("user-cust").size());
    }
}
