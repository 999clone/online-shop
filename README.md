# سامانه فروشگاه آنلاین (Online Shop) — فاز ۲ پروژه اصول طراحی نرم‌افزار

پروژه درس **اصول طراحی نرم‌افزار** — دانشکده ریاضی و علوم کامپیوتر، **دانشگاه صنعتی امیرکبیر (پلی‌تکنیک تهران)**  
**استاد درس:** دکتر هانیه فلاح | **سرتدریسیار:** امیرمحمد ذاکر

---

## 👥 اعضای گروه
- **عضو اول:** `[نام و نام خانوادگی]` — شماره دانشجویی: `[شماره دانشجویی]`
- **عضو دوم:** `[نام و نام خانوادگی]` — شماره دانشجویی: `[شماره دانشجویی]`

---

## 📖 معرفی پروژه
این پروژه پیاده‌سازی شیءگرای یک سامانه **فروشگاه آنلاین** به زبان **جاوا (Java 17)** و با ابزار ساخت **Maven** است که ۳۶ داستان کاربری (User Story) را در نقش‌های **مهمان (Guest)**، **مشتری (Customer)** و **مدیر سیستم (Admin)** پوشش می‌دهد.

در **فاز ۲**، معماری پروژه با رعایت کامل اصول پنج‌گانه **SOLID** بازطراحی (Refactor) شده، نمودارهای **UML** منطبق بر کد نهایی ترسیم گردیده و ساختار مخزن و مستندات پروژه تکمیل شده است.

---

## 🏗️ ساختار پروژه (پس از Refactor بر اساس SOLID)

```text
online-shop/
├── .gitignore
├── pom.xml
├── README.md
├── docs/
│   ├── DOMAIN_MODEL.md
│   ├── Design-Document-Phase2.html
│   ├── Design-Document-Phase2.pdf
│   └── diagrams/
│       ├── class-diagram.puml / .svg
│       ├── object-diagram.puml / .svg
│       ├── activity-diagram.puml / .svg
│       ├── component-diagram.puml / .svg
│       ├── deployment-diagram.puml / .svg
│       └── sequence-diagram.puml / .svg
└── src/
    ├── main/java/org/example/
    │   ├── Main.java                         # Composition Root (تزریق وابستگی‌ها - DIP)
    │   ├── cli/                              # لایه واسط کاربری خط فرمان
    │   │   ├── ConsoleApp.java               # هماهنگ‌کننده منوها (OCP)
    │   │   ├── MenuHandler.java              # رابط هندلرهای منو (ISP / LSP)
    │   │   ├── GuestMenuHandler.java         # منوی مهمان (SRP)
    │   │   ├── CustomerMenuHandler.java      # منوی مشتری (SRP)
    │   │   ├── AdminMenuHandler.java         # منوی مدیر (SRP)
    │   │   ├── CatalogBrowserView.java       # نمایش و جستجوی کاتالوگ (SRP)
    │   │   ├── SessionContext.java           # نگهداری وضعیت نشست کاربر (SRP)
    │   │   └── InputHelper.java              # اعتبارسنجی ورودی ترمینال
    │   ├── model/                            # لایه دامنه (مستقل از سایر لایه‌ها)
    │   │   ├── entity/                       # User, Product, Category, Cart, Order, Review
    │   │   ├── value/                        # Address, CartItem, OrderItem, PaymentTransaction
    │   │   ├── enums/                        # UserRole, OrderStatus, PaymentStatus, ProductSortOption
    │   │   └── exception/                    # DomainException و زیرکلاس‌های دامنه
    │   ├── repository/                       # رابط‌های مخزن داده (DIP & ISP)
    │   │   ├── UserRepository.java
    │   │   ├── CategoryRepository.java
    │   │   ├── ProductRepository.java
    │   │   ├── CartRepository.java
    │   │   ├── OrderRepository.java
    │   │   ├── ReviewRepository.java
    │   │   └── inmemory/                     # پیاده‌سازی درون‌حافظه‌ای + SampleDataSeeder
    │   └── service/                          # لایه سرویس و منطق کاربردی
    │       ├── UserService.java
    │       ├── CategoryService.java
    │       ├── ProductService.java
    │       ├── CartService.java
    │       ├── OrderService.java
    │       ├── PaymentService.java
    │       ├── InventoryService.java
    │       ├── ReviewService.java
    │       ├── security/                     # PasswordHasher, Sha256PasswordHasher (SRP & DIP)
    │       ├── payment/                      # PaymentGateway, SimulatedPaymentGateway (OCP & DIP)
    │       ├── rating/                       # ProductRatingProvider (ISP)
    │       └── sorting/                      # ProductSortStrategy, Factory و استراتژی‌ها (OCP & LSP)
    └── test/java/org/example/
        ├── UserAndAuthServiceTest.java
        ├── ProductAndCatalogServiceTest.java
        ├── CartOrderPaymentReviewTest.java
        └── SolidArchitectureTest.java
```

---

## 🚀 راهنمای اجرا و تست

### ۱. اجرای تست‌های واحد (Unit Tests)
```bash
mvn test
```
*(شامل ۲۹ تست واحد جامع برای پوشش تمام داستان‌های کاربری و اصول SOLID)*

### ۲. اجرای برنامه در خط فرمان
```bash
mvn compile exec:java
```
یا ساخت فایل JAR و اجرای آن:
```bash
mvn package
java -jar target/online-shop-1.0.0.jar
```

### ۳. حساب‌های کاربری پیش‌فرض
| نقش | ایمیل | رمز عبور |
|---|---|---|
| **مدیر (Admin)** | `admin@shop.com` | `admin123` |
| **مشتری (Customer)** | `customer@shop.com` | `user123` |
| **مهمان (Guest)** | بدون نیاز به ورود | — |

---

## 🌿 استراتژی Git و مدیریت شاخه‌ها
- `main`: نسخه نهایی و پایدار پروژه
- `develop`: شاخه اصلی یکپارچه‌سازی تغییرات گروه
- `member-1` / `member-2`: شاخه‌های شخصی اعضای گروه
- `feature/*`: شاخه‌های قابلیت ایجادشده از شاخه شخصی و ادغام‌شده از طریق Pull Request
