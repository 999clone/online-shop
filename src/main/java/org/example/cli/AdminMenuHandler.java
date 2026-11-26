package org.example.cli;

import org.example.model.entity.Category;
import org.example.model.entity.Order;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.enums.OrderStatus;
import org.example.model.enums.UserRole;
import org.example.service.CategoryService;
import org.example.service.InventoryService;
import org.example.service.OrderService;
import org.example.service.ProductService;

import java.util.List;

/**
 * CLI menu handler for administrator users (SRP).
 */
public class AdminMenuHandler implements MenuHandler {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final CatalogBrowserView catalogView;
    private final InputHelper input;

    public AdminMenuHandler(ProductService productService,
                            CategoryService categoryService,
                            InventoryService inventoryService,
                            OrderService orderService,
                            CatalogBrowserView catalogView,
                            InputHelper input) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.catalogView = catalogView;
        this.input = input;
    }

    @Override
    public boolean supports(User currentUser) {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    @Override
    public boolean handle(SessionContext context) {
        User currentUser = context.getCurrentUser();
        System.out.println("\n--- [پنل مدیریت سامانه - " + currentUser.getFullName() + "] ---");
        System.out.println("1. مشاهده موجودی انبار کالاها (US 28)");
        System.out.println("2. افزایش یا کاهش موجودی کالا (US 29)");
        System.out.println("3. ایجاد و ثبت محصول جدید (US 33)");
        System.out.println("4. ویرایش مشخصات محصول (US 34)");
        System.out.println("5. غیرفعال‌سازی محصول (US 35)");
        System.out.println("6. فعال‌سازی مجدد محصول");
        System.out.println("7. مدیریت سفارش‌های فروشگاه (US 36)");
        System.out.println("8. ایجاد دسته‌بندی جدید");
        System.out.println("9. خروج از حساب کاربری (Logout) (US 3)");
        System.out.println("0. خروج کامل از برنامه");

        int choice = input.readInt("گزینه مورد نظر را انتخاب کنید", 0, 9);
        switch (choice) {
            case 1 -> viewInventory();
            case 2 -> adjustInventory();
            case 3 -> createProduct();
            case 4 -> editProduct();
            case 5 -> deactivateProduct();
            case 6 -> activateProduct();
            case 7 -> manageOrdersAdmin();
            case 8 -> createCategory();
            case 9 -> logout(context);
            case 0 -> { return false; }
        }
        return true;
    }

    private void viewInventory() {
        System.out.println("\n📦 -- گزارش موجودی انبار فروشگاه (US 28) --");
        List<Product> list = inventoryService.getInventoryList();
        for (Product p : list) {
            System.out.println("• [" + p.getId() + "] " + p.getName() + " (" + p.getBrand() +
                    ") | موجودی: " + p.getStock() + " عدد | قیمت: " + String.format("%,.0f", p.getPrice()) +
                    " تومان | وضعیت: " + (p.isActive() ? "فعال" : "غیرفعال"));
        }
    }

    private void adjustInventory() {
        viewInventory();
        System.out.println("\n-- تغییر موجودی کالا (US 29) --");
        String pid = input.readString("شناسه محصول");
        int delta = input.readInt("میزان تغییر (عدد مثبت برای افزایش، عدد منفی برای کاهش)", -1000, 1000);
        Product updated = inventoryService.adjustStock(pid, delta);
        System.out.println("✅ موجودی کالا به‌روزرسانی شد. موجودی جدید: " + updated.getStock() + " عدد");
    }

    private void createProduct() {
        System.out.println("\n-- ایجاد محصول جدید (US 33) --");
        catalogView.showCategories();
        String catId = input.readString("شناسه دسته‌بندی محصول");
        String name = input.readString("نام محصول");
        String desc = input.readString("توضیحات محصول");
        String brand = input.readString("برند");
        double price = input.readDouble("قیمت به تومان (نباید منفی باشد)", 0.0);
        int stock = input.readInt("موجودی اولیه در انبار", 0, 10000);

        Product p = productService.createProduct(name, desc, price, stock, catId, brand);
        System.out.println("✅ محصول با موفقیت ایجاد شد! شناسه تخصیص داده شده: " + p.getId());
    }

    private void editProduct() {
        System.out.println("\n-- ویرایش مشخصات محصول (US 34) --");
        viewInventory();
        String pid = input.readString("شناسه محصول برای ویرایش");
        Product p = productService.getProductById(pid);

        String name = input.readOptionalString("نام جدید", p.getName());
        String desc = input.readOptionalString("توضیحات جدید", p.getDescription());
        String brand = input.readOptionalString("برند جدید", p.getBrand());
        Double price = input.readOptionalDouble("قیمت جدید", 0.0);
        Integer stock = input.readOptionalInt("موجودی جدید", 0, 10000);
        String catId = input.readOptionalString("شناسه دسته‌بندی جدید", p.getCategoryId());

        productService.updateProduct(pid, name, desc, (price != null ? price : p.getPrice()),
                (stock != null ? stock : p.getStock()), catId, brand);
        System.out.println("✅ محصول با موفقیت ویرایش شد.");
    }

    private void deactivateProduct() {
        System.out.println("\n-- غیرفعال‌سازی محصول (US 35) --");
        viewInventory();
        String pid = input.readString("شناسه محصول جهت غیرفعال‌سازی");
        productService.deactivateProduct(pid);
        System.out.println("✅ محصول با موفقیت غیرفعال شد و دیگر در نتایج جستجو و خرید مشتریان نمایش داده نمی‌شود.");
    }

    private void activateProduct() {
        System.out.println("\n-- فعال‌سازی مجدد محصول --");
        viewInventory();
        String pid = input.readString("شناسه محصول جهت فعال‌سازی");
        productService.activateProduct(pid);
        System.out.println("✅ محصول مجدداً فعال شد.");
    }

    private void manageOrdersAdmin() {
        System.out.println("\n--- [مدیریت و نظارت بر سفارش‌ها (US 36)] ---");
        System.out.println("فیلتر سفارش‌ها بر اساس وضعیت:");
        System.out.println("1. همه سفارش‌ها");
        System.out.println("2. در انتظار پرداخت");
        System.out.println("3. پرداخت‌شده");
        System.out.println("4. پرداخت ناموفق");
        System.out.println("5. تحویل داده شده");

        int choice = input.readInt("انتخاب فیلتر", 1, 5);
        OrderStatus filter = switch (choice) {
            case 2 -> OrderStatus.PENDING_PAYMENT;
            case 3 -> OrderStatus.PAID;
            case 4 -> OrderStatus.FAILED_PAYMENT;
            case 5 -> OrderStatus.DELIVERED;
            default -> null;
        };

        List<Order> orders = orderService.getAllOrdersForAdmin(filter);
        if (orders.isEmpty()) {
            System.out.println("سفارشی با این وضعیت یافت نشد.");
            return;
        }

        System.out.println("\n📋 لیست سفارش‌ها (" + orders.size() + " مورد):");
        for (Order o : orders) {
            System.out.println("--------------------------------------------------");
            System.out.println("سفارش: " + o.getId() + " | کاربر: " + o.getUserId() +
                    " | تاریخ: " + o.getCreatedAt() + " | وضعیت: " + o.getStatus().getPersianLabel());
            System.out.println("مبلغ کل: " + String.format("%,.0f", o.getTotalAmount()) + " تومان");
            if (o.getPaymentTransaction() != null) {
                System.out.println("تراکنش پرداخت (اطلاعات حساس محافظت‌شده): " + o.getPaymentTransaction().toMaskedString());
            }
        }
    }

    private void createCategory() {
        System.out.println("\n-- ایجاد دسته‌بندی جدید --");
        String name = input.readString("نام دسته‌بندی");
        String desc = input.readString("توضیحات");
        Category cat = categoryService.createCategory(name, desc);
        System.out.println("✅ دسته‌بندی با شناسه '" + cat.getId() + "' با موفقیت ایجاد شد.");
    }

    private void logout(SessionContext context) {
        System.out.println("✅ کاربر " + context.getCurrentUser().getFullName() + " با موفقیت از سیستم خارج شد (US 3).");
        context.setCurrentUser(null);
    }
}
