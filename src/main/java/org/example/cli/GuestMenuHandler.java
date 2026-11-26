package org.example.cli;

import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.service.ProductService;
import org.example.service.UserService;

/**
 * CLI menu handler for unauthenticated guest users (SRP).
 */
public class GuestMenuHandler implements MenuHandler {
    private final UserService userService;
    private final ProductService productService;
    private final CatalogBrowserView catalogView;
    private final InputHelper input;

    public GuestMenuHandler(UserService userService,
                            ProductService productService,
                            CatalogBrowserView catalogView,
                            InputHelper input) {
        this.userService = userService;
        this.productService = productService;
        this.catalogView = catalogView;
        this.input = input;
    }

    @Override
    public boolean supports(User currentUser) {
        return currentUser == null;
    }

    @Override
    public boolean handle(SessionContext context) {
        System.out.println("\n--- [منوی اصلی - کاربر مهمان] ---");
        System.out.println("1. مشاهده کاتالوگ محصولات (US 8)");
        System.out.println("2. مشاهده جزئیات یک محصول (US 9)");
        System.out.println("3. جستجوی محصولات (US 10)");
        System.out.println("4. فیلتر محصولات (US 11)");
        System.out.println("5. مرتب‌سازی محصولات (US 12)");
        System.out.println("6. مشاهده دسته‌بندی‌ها (US 13)");
        System.out.println("7. مشاهده محصولات یک دسته‌بندی (US 14)");
        System.out.println("8. ثبت‌نام در فروشگاه (US 1)");
        System.out.println("9. ورود به حساب کاربری (US 2)");
        System.out.println("10. فراموشی رمز عبور (بازیابی) (US 4)");
        System.out.println("0. خروج از برنامه");

        int choice = input.readInt("گزینه مورد نظر را انتخاب کنید", 0, 10);
        switch (choice) {
            case 1 -> catalogView.showProductCatalog(productService.getActiveProducts());
            case 2 -> catalogView.showProductDetails();
            case 3 -> catalogView.searchProducts();
            case 4 -> catalogView.filterProducts();
            case 5 -> catalogView.sortProducts();
            case 6 -> catalogView.showCategories();
            case 7 -> catalogView.showProductsInCategory();
            case 8 -> register();
            case 9 -> login(context);
            case 10 -> forgotPassword();
            case 0 -> { return false; }
        }
        return true;
    }

    private void register() {
        System.out.println("\n-- ثبت‌نام حساب جدید (US 1) --");
        String fullName = input.readString("نام و نام خانوادگی");
        String email = input.readString("ایمیل (یا اینتر برای خالی)");
        String phone = input.readString("شماره موبایل (یا اینتر برای خالی)");
        String pass = input.readString("رمز عبور");

        User u = userService.register(fullName, email, phone, pass, UserRole.CUSTOMER);
        System.out.println("✅ ثبت‌نام با موفقیت انجام شد! حساب کاربری برای شما ایجاد گردید.");
        System.out.println("شناسه کاربری شما: " + u.getId());
    }

    private void login(SessionContext context) {
        System.out.println("\n-- ورود به حساب کاربری (US 2) --");
        String contact = input.readString("ایمیل یا شماره موبایل");
        String pass = input.readString("رمز عبور");

        User loggedIn = userService.login(contact, pass);
        context.setCurrentUser(loggedIn);
        System.out.println("✅ خوش آمدید، " + loggedIn.getFullName() + "! ورود شما موفقیت‌آمیز بود.");
    }

    private void forgotPassword() {
        System.out.println("\n-- بازیابی رمز عبور (US 4) --");
        String contact = input.readString("ایمیل یا شماره موبایل ثبت شده در سیستم");
        String newPass = input.readString("رمز عبور جدید");

        userService.resetPassword(contact, newPass);
        System.out.println("✅ رمز عبور با موفقیت به‌روزرسانی شد. اکنون می‌توانید وارد شوید.");
    }
}
