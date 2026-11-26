package org.example.cli;

import org.example.model.entity.*;
import org.example.model.enums.PaymentStatus;
import org.example.model.enums.UserRole;
import org.example.model.value.Address;
import org.example.model.value.CartItem;
import org.example.model.value.OrderItem;
import org.example.model.value.PaymentTransaction;
import org.example.service.*;

import java.util.List;

/**
 * CLI menu handler for authenticated customer users (SRP).
 */
public class CustomerMenuHandler implements MenuHandler {
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final CatalogBrowserView catalogView;
    private final InputHelper input;

    public CustomerMenuHandler(UserService userService,
                               ProductService productService,
                               CartService cartService,
                               OrderService orderService,
                               PaymentService paymentService,
                               ReviewService reviewService,
                               CatalogBrowserView catalogView,
                               InputHelper input) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
        this.catalogView = catalogView;
        this.input = input;
    }

    @Override
    public boolean supports(User currentUser) {
        return currentUser != null && currentUser.getRole() == UserRole.CUSTOMER;
    }

    @Override
    public boolean handle(SessionContext context) {
        User currentUser = context.getCurrentUser();
        System.out.println("\n--- [پنل مشتری: " + currentUser.getFullName() + "] ---");
        System.out.println("1. مرور کاتالوگ و جزئیات محصولات (US 8-12)");
        System.out.println("2. مدیریت سبد خرید (مشاهده، افزودن، تغییر، حذف، خالی کردن) (US 15-19)");
        System.out.println("3. ثبت سفارش و نهایی‌سازی خرید (Checkout) (US 20-22)");
        System.out.println("4. پرداخت سفارش در انتظار (US 23-25)");
        System.out.println("5. پیگیری و تاریخچه سفارش‌ها (US 26-27)");
        System.out.println("6. ثبت نظر و امتیاز برای خرید قبلی (US 30-31)");
        System.out.println("7. مدیریت پروفایل و آدرس‌ها (US 5-7)");
        System.out.println("8. خروج از حساب کاربری (Logout) (US 3)");
        System.out.println("0. خروج کامل از برنامه");

        int choice = input.readInt("گزینه مورد نظر را انتخاب کنید", 0, 8);
        switch (choice) {
            case 1 -> browseProductsCustomer();
            case 2 -> manageCart(currentUser);
            case 3 -> checkout(currentUser);
            case 4 -> payOrder(currentUser);
            case 5 -> viewOrders(currentUser);
            case 6 -> submitReviewAndRating(currentUser);
            case 7 -> manageProfileAndAddresses(context);
            case 8 -> logout(context);
            case 0 -> { return false; }
        }
        return true;
    }

    private void browseProductsCustomer() {
        System.out.println("\n1. کاتالوگ کامل | 2. مشاهده جزئیات | 3. جستجو | 4. فیلتر | 5. مرتب‌سازی | 6. دسته‌بندی‌ها");
        int sub = input.readInt("انتخاب", 1, 6);
        switch (sub) {
            case 1 -> catalogView.showProductCatalog(productService.getActiveProducts());
            case 2 -> catalogView.showProductDetails();
            case 3 -> catalogView.searchProducts();
            case 4 -> catalogView.filterProducts();
            case 5 -> catalogView.sortProducts();
            case 6 -> catalogView.showProductsInCategory();
        }
    }

    private void manageCart(User currentUser) {
        System.out.println("\n--- [مدیریت سبد خرید (US 15-19)] ---");
        System.out.println("1. مشاهده سبد خرید (US 16)");
        System.out.println("2. افزودن محصول به سبد (US 15)");
        System.out.println("3. تغییر تعداد محصول در سبد (US 17)");
        System.out.println("4. حذف محصول از سبد (US 18)");
        System.out.println("5. خالی کردن کامل سبد خرید (US 19)");
        System.out.println("0. بازگشت");

        int choice = input.readInt("انتخاب", 0, 5);
        switch (choice) {
            case 1 -> viewCart(currentUser);
            case 2 -> {
                catalogView.showProductCatalog(productService.getActiveProducts());
                String pid = input.readString("شناسه محصول");
                int qty = input.readInt("تعداد درخواستی", 1, 100);
                cartService.addToCart(currentUser.getId(), pid, qty);
                System.out.println("✅ محصول به سبد خرید افزوده شد.");
                viewCart(currentUser);
            }
            case 3 -> {
                viewCart(currentUser);
                String pid = input.readString("شناسه محصول در سبد");
                int qty = input.readInt("تعداد جدید", 1, 100);
                cartService.updateItemQuantity(currentUser.getId(), pid, qty);
                System.out.println("✅ تعداد محصول در سبد به‌روزرسانی شد.");
                viewCart(currentUser);
            }
            case 4 -> {
                viewCart(currentUser);
                String pid = input.readString("شناسه محصول برای حذف");
                cartService.removeFromCart(currentUser.getId(), pid);
                System.out.println("✅ محصول از سبد خرید حذف شد.");
                viewCart(currentUser);
            }
            case 5 -> {
                cartService.clearCart(currentUser.getId());
                System.out.println("✅ سبد خرید کاملاً خالی شد. تعداد و مبلغ کل به صفر تغییر یافت.");
            }
        }
    }

    private void viewCart(User currentUser) {
        Cart cart = cartService.getOrCreateCart(currentUser.getId());
        System.out.println("\n🛒 -- سبد خرید کاربر: " + currentUser.getFullName() + " (US 16) --");
        if (cart.isEmpty()) {
            System.out.println("سبد خرید شما خالی است.");
            return;
        }
        for (CartItem ci : cart.getItems()) {
            System.out.println("• [" + ci.getProductId() + "] " + ci.getProductName() +
                    " | تعداد: " + ci.getQuantity() +
                    " | قیمت واحد: " + String.format("%,.0f", ci.getUnitPrice()) + " تومان" +
                    " | جمع: " + String.format("%,.0f", ci.getSubtotal()) + " تومان");
        }
        System.out.println("--------------------------------------------------");
        System.out.println("💰 مبلغ کل محاسبه شده توسط سیستم: " + String.format("%,.0f", cart.getTotalPrice()) + " تومان");
    }

    private void checkout(User currentUser) {
        System.out.println("\n-- شروع فرایند خرید و ثبت سفارش (US 20-22) --");
        Cart cart = cartService.getOrCreateCart(currentUser.getId());
        if (cart.isEmpty()) {
            System.out.println("⚠️ سبد خرید شما خالی است! ابتدا محصولی اضافه کنید.");
            return;
        }

        List<Address> addresses = currentUser.getAddresses();
        if (addresses.isEmpty()) {
            System.out.println("⚠️ شما هنوز آدرس ارسالی ثبت نکرده‌اید. لطفاً ابتدا از منوی آدرس‌ها یک آدرس اضافه کنید.");
            return;
        }

        System.out.println("📍 آدرس‌های ارسال شما:");
        for (int i = 0; i < addresses.size(); i++) {
            System.out.println((i + 1) + ". " + addresses.get(i));
        }

        int addrIndex = input.readInt("شماره آدرس مورد نظر را انتخاب کنید", 1, addresses.size()) - 1;
        String addressId = addresses.get(addrIndex).getId();

        Order order = orderService.checkout(currentUser.getId(), addressId);
        System.out.println("\n✅ سفارش شما با موفقیت ثبت شد و در انتظار پرداخت قرار گرفت!");
        System.out.println("شماره سفارش: " + order.getId());
        System.out.println("مبلغ نهایی سفارش (محاسبه سیستمی): " + String.format("%,.0f", order.getTotalAmount()) + " تومان");
        System.out.println("وضعیت: " + order.getStatus().getPersianLabel());

        boolean payNow = input.readBoolean("آیا تمایل دارید اکنون پرداخت را انجام دهید؟");
        if (payNow) {
            executePayment(currentUser, order.getId());
        }
    }

    private void payOrder(User currentUser) {
        System.out.println("\n-- پرداخت سفارش‌های پرداخت‌نشده (US 23-25) --");
        List<Order> orders = orderService.getCustomerOrders(currentUser.getId());
        List<Order> payable = orders.stream().filter(Order::canBePaid).toList();

        if (payable.isEmpty()) {
            System.out.println("شما هیچ سفارشی در انتظار پرداخت ندارید.");
            return;
        }

        System.out.println("سفارش‌های قابل پرداخت:");
        for (Order o : payable) {
            System.out.println("• شناسه: " + o.getId() + " | مبلغ: " + String.format("%,.0f", o.getTotalAmount()) +
                    " تومان | وضعیت: " + o.getStatus().getPersianLabel());
        }

        String orderId = input.readString("شناسه سفارش را برای پرداخت وارد کنید");
        executePayment(currentUser, orderId);
    }

    private void executePayment(User currentUser, String orderId) {
        System.out.println("\n💳 شبیه‌سازی درگاه پرداخت بانکی:");
        System.out.println("1. پرداخت موفقیت‌آمیز (تراکنش تأیید شده)");
        System.out.println("2. شبیه‌سازی پرداخت ناموفق (عدم موجودی یا خطای کارت)");

        int choice = input.readInt("نتیجه شبیه‌سازی", 1, 2);
        boolean simulateSuccess = (choice == 1);

        PaymentTransaction trx = paymentService.processPayment(currentUser.getId(), orderId, simulateSuccess);
        if (trx.getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("✅ پرداخت با موفقیت انجام شد و سفارش نهایی گردید!");
            System.out.println("کد پیگیری درگاه: " + trx.getGatewayReference());
            System.out.println("موجودی انبار کالاها با موفقیت کسر شد.");
        } else {
            System.out.println("❌ پرداخت ناموفق بود: " + trx.getFailureReason());
            System.out.println("سفارش شما در حالت 'پرداخت ناموفق' باقی ماند و می‌توانید مجدداً اقدام به پرداخت کنید (US 24).");
        }
    }

    private void viewOrders(User currentUser) {
        System.out.println("\n-- تاریخچه سفارش‌های من (US 26) --");
        List<Order> orders = orderService.getCustomerOrders(currentUser.getId());
        if (orders.isEmpty()) {
            System.out.println("شما تا کنون سفارشی ثبت نکرده‌اید.");
            return;
        }

        for (Order o : orders) {
            System.out.println("--------------------------------------------------");
            System.out.println("شماره سفارش: " + o.getId());
            System.out.println("تاریخ ثبت: " + o.getCreatedAt());
            System.out.println("مبلغ کل: " + String.format("%,.0f", o.getTotalAmount()) + " تومان");
            System.out.println("وضعیت فعلی: " + o.getStatus().getPersianLabel());
            System.out.println("آدرس گیرنده: " + o.getShippingAddress().getTitle() + " (" + o.getShippingAddress().getCity() + ")");
        }

        boolean inspect = input.readBoolean("\nآیا مایل به مشاهده جزئیات کامل یک سفارش هستید؟ (US 27)");
        if (inspect) {
            String oid = input.readString("شناسه سفارش مورد نظر");
            Order o = orderService.getOrderDetails(currentUser.getId(), oid);
            System.out.println("\n📄 جزئیات فاکتور سفارش #" + o.getId());
            System.out.println("وضعیت: " + o.getStatus().getPersianLabel());
            System.out.println("آدرس کامل تحویل: " + o.getShippingAddress());
            System.out.println("اقلام سفارش:");
            for (OrderItem oi : o.getItems()) {
                System.out.println("  - " + oi);
            }
            System.out.println("جمع نهایی: " + String.format("%,.0f", o.getTotalAmount()) + " تومان");
            if (o.getPaymentTransaction() != null) {
                System.out.println("اطلاعات پرداخت: " + o.getPaymentTransaction().toMaskedString());
            }
        }
    }

    private void submitReviewAndRating(User currentUser) {
        System.out.println("\n-- ثبت نظر و امتیاز برای کالای خریداری‌شده (US 30-31) --");
        catalogView.showProductCatalog(productService.getActiveProducts());
        String pid = input.readString("شناسه محصول مورد نظر");

        int rating = input.readInt("امتیاز شما به کیفیت این محصول (۱ تا ۵)", 1, 5);
        String comment = input.readString("متن نظر و تجربه کاربری شما (حداکثر ۵۰۰ کاراکتر)");

        Review r = reviewService.addReview(currentUser.getId(), pid, comment, rating);
        System.out.println("✅ نظر شما با موفقیت ثبت شد:");
        System.out.println(r);
    }

    private void manageProfileAndAddresses(SessionContext context) {
        User currentUser = context.getCurrentUser();
        System.out.println("\n--- [پروفایل و آدرس‌ها (US 5-7)] ---");
        System.out.println("1. مشاهده اطلاعات پروفایل (بدون نمایش رمز عبور) (US 5)");
        System.out.println("2. ویرایش اطلاعات پروفایل (US 6)");
        System.out.println("3. مشاهده آدرس‌ها (US 7)");
        System.out.println("4. افزودن آدرس جدید (US 7)");
        System.out.println("5. ویرایش یک آدرس (US 7)");
        System.out.println("6. حذف یک آدرس (US 7)");
        System.out.println("0. بازگشت");

        int choice = input.readInt("انتخاب", 0, 6);
        switch (choice) {
            case 1 -> {
                User u = userService.getProfile(currentUser.getId());
                System.out.println("👤 مشخصات پروفایل:");
                System.out.println("نام: " + u.getFullName());
                System.out.println("ایمیل: " + (u.getEmail() != null ? u.getEmail() : "(ثبت نشده)"));
                System.out.println("شماره موبایل: " + (u.getPhoneNumber() != null ? u.getPhoneNumber() : "(ثبت نشده)"));
                System.out.println("نقش: " + u.getRole());
                System.out.println("رمز عبور: [محرمانه و رمزنگاری‌شده - مطابق US 5 نمایش داده نمی‌شود]");
            }
            case 2 -> {
                String newName = input.readString("نام کامل جدید");
                String newEmail = input.readString("ایمیل جدید (یا خالی)");
                String newPhone = input.readString("شماره موبایل جدید (یا خالی)");
                context.setCurrentUser(userService.updateProfile(currentUser.getId(), newName, newEmail, newPhone));
                System.out.println("✅ پروفایل با موفقیت ویرایش شد.");
            }
            case 3 -> {
                List<Address> addrs = userService.getUserAddresses(currentUser.getId());
                if (addrs.isEmpty()) {
                    System.out.println("هیچ آدرسی ثبت نشده است.");
                } else {
                    for (Address a : addrs) {
                        System.out.println("• [" + a.getId() + "] " + a);
                    }
                }
            }
            case 4 -> {
                String title = input.readString("عنوان آدرس (مثلاً منزل، محل کار)");
                String recipient = input.readString("نام گیرنده");
                String phone = input.readString("شماره تماس");
                String country = input.readString("کشور");
                String city = input.readString("شهر");
                String street = input.readString("آدرس دقیق پستی");
                String postal = input.readString("کد پستی ۱۰ رقمی");
                boolean isDef = input.readBoolean("به عنوان آدرس پیش‌فرض تنظیم شود؟");

                Address addr = new Address(recipient, phone, country, city, street, postal, title, isDef);
                userService.addAddress(currentUser.getId(), addr);
                System.out.println("✅ آدرس با موفقیت اضافه شد.");
            }
            case 5 -> {
                List<Address> addrs = userService.getUserAddresses(currentUser.getId());
                for (Address a : addrs) {
                    System.out.println("• [" + a.getId() + "] " + a);
                }
                String aid = input.readString("شناسه آدرس مورد نظر برای ویرایش");
                String title = input.readString("عنوان آدرس جدید");
                String recipient = input.readString("نام گیرنده جدید");
                String phone = input.readString("شماره تماس جدید");
                String country = input.readString("کشور");
                String city = input.readString("شهر");
                String street = input.readString("آدرس دقیق");
                String postal = input.readString("کد پستی");
                boolean isDef = input.readBoolean("آدرس پیش‌فرض؟");

                Address updated = new Address(aid, recipient, phone, country, city, street, postal, title, isDef);
                userService.updateAddress(currentUser.getId(), aid, updated);
                System.out.println("✅ آدرس با موفقیت به‌روزرسانی شد.");
            }
            case 6 -> {
                List<Address> addrs = userService.getUserAddresses(currentUser.getId());
                for (Address a : addrs) {
                    System.out.println("• [" + a.getId() + "] " + a);
                }
                String aid = input.readString("شناسه آدرس مورد نظر برای حذف");
                userService.removeAddress(currentUser.getId(), aid);
                System.out.println("✅ آدرس حذف شد.");
            }
        }
    }

    private void logout(SessionContext context) {
        System.out.println("✅ کاربر " + context.getCurrentUser().getFullName() + " با موفقیت از سیستم خارج شد (US 3).");
        context.setCurrentUser(null);
    }
}
