package com.fpt.init;

import com.fpt.entity.*;
import com.fpt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SubscriptionPackageRepository subscriptionPackageRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final LicenseRepository licenseRepository;
    private final VersionRepository versionRepository;
    private final CategoryRepository categoryRepository;
    private final DocRepository docRepository;
    private final OptionRepository optionRepository;

    @Override
    public void run(String... args) throws Exception {

        if (optionRepository.count() == 0) {
            List<Option> options = Arrays.asList(
                    Option.builder().name("5 documents per month").isActive(true).isDeleted(false).build(),
                    Option.builder().name("Unlimited access").isActive(true).isDeleted(false).build(),
                    Option.builder().name("Priority support").isActive(true).isDeleted(false).build()
            );
            optionRepository.saveAll(options);
        }

        if (userRepository.count() == 0) {
            List<User> users = Arrays.asList(
                    User.builder().userName("admin").email("admin@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Nguyen Van").lastName("A").phoneNumber("0987654321")
                            .role(Role.ADMIN).status(UserStatus.ACTIVE).isActive(true)
                            .avatarUrl("").isDeleted(false).build(),
                    User.builder().userName("CaoVanBay").email("Bay@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Cao Van").lastName("Bay").phoneNumber("0999999999")
                            .role(Role.CUSTOMER).status(UserStatus.ACTIVE).isActive(true)
                            .avatarUrl("").isDeleted(false).build(),
                    User.builder().userName("LeThiTam").email("Tam@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Le Thi").lastName("Tam").phoneNumber("0912345678")
                            .role(Role.CUSTOMER).status(UserStatus.ACTIVE).isActive(true)
                            .avatarUrl("").isDeleted(false).build()
            );
            userRepository.saveAll(users);
        }

        if (subscriptionPackageRepository.count() == 0) {
            Option option1 = optionRepository.findById(1L).orElseThrow();
            Option option2 = optionRepository.findById(2L).orElseThrow();
            Option option3 = optionRepository.findById(3L).orElseThrow();

            List<SubscriptionPackage> packagesToSave = new ArrayList<>();

            for (SubscriptionPackage.BillingCycle cycle : SubscriptionPackage.BillingCycle.values()) {
                SubscriptionPackage runtimePackage = SubscriptionPackage.builder()
                        .name("Runtime Package")
                        .description("hihi")
                        .price(getFixedPrice(cycle))
                        .discount(0f)
                        .billingCycle(cycle)
                        .typePackage(SubscriptionPackage.TypePackage.RUNTIME)
                        .isActive(true)
                        .isDeleted(false)
                        .build();
                packagesToSave.add(runtimePackage);

                SubscriptionPackage devPackage = SubscriptionPackage.builder()
                        .name("Dev Package")
                        .description("hihi")
                        .price(getFixedPrice(cycle))
                        .discount(10f)
                        .billingCycle(cycle)
                        .typePackage(SubscriptionPackage.TypePackage.DEV)
                        .isActive(true)
                        .isDeleted(false)
                        .build();
                packagesToSave.add(devPackage);
            }

            subscriptionPackageRepository.saveAll(packagesToSave);

            for (SubscriptionPackage pkg : packagesToSave) {
                if (pkg.getTypePackage() == SubscriptionPackage.TypePackage.RUNTIME) {
                    pkg.setOptions(List.of(option1));
                } else {
                    pkg.setOptions(List.of(option2, option3));
                }
            }

            subscriptionPackageRepository.saveAll(packagesToSave);
        }

        if (paymentOrderRepository.count() == 0) {
            PaymentOrder order1 = PaymentOrder.builder()
                    .user(userRepository.findById(1L).orElse(null))
                    .subscriptionPackage(subscriptionPackageRepository.findById(2L).orElse(null))
                    .orderId(123456)
                    .price(4000f)
                    .paymentLink("https://payment.example.com/checkout/ORDER-123456")
                    .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                    .paymentMethod(PaymentOrder.PaymentMethod.BANK)
                    .licenseCreated(false)
                    .bin("970415")
                    .accountName("NGUYEN THI HUONG")
                    .accountNumber("0386331971")
                    .cancelReason("00020101021138540010A00000072701240006970415011003863319710208QRIBFTTA53037045802VN63046733")
                    .dateTransfer("2025-07-30 11:09:43")
                    .isDeleted(false)
                    .build();

            paymentOrderRepository.saveAll(List.of(order1));
        }

        if (licenseRepository.count() == 0) {
            PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(123456).orElse(null);

            License license = License.builder()
                    .user(userRepository.findById(1L).orElse(null))
                    .subscriptionPackage(subscriptionPackageRepository.findById(2L).orElse(null))
                    .licenseKey("LIC-ABCDEF-123456")
                    .duration(60)
                    .ip("203.113.78.9")
                    .hardwareId("203.113.78.9")
                    .canUsed(false)
                    .activatedAt(null)
                    .orderId(paymentOrder != null ? paymentOrder.getOrderId() : null)
                    .isDeleted(false)
                    .build();

            licenseRepository.save(license);
        }

        if (versionRepository.count() == 0) {
            List<Version> versions = Arrays.asList(
                    Version.builder().version("v1.0").description("Phiên bản đầu tiên của hệ thống").isActive(true).isDeleted(false).build(),
                    Version.builder().version("v1.1").description("Bổ sung thêm chức năng tìm kiếm").isActive(true).isDeleted(false).build()
            );
            versionRepository.saveAll(versions);
        }

        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                    Category.builder().version(versionRepository.findById(1L).orElse(null)).name("Hướng dẫn sử dụng").slug("huong-dan-su-dung").order(1L).isActive(true).isDeleted(false).build(),
                    Category.builder().version(versionRepository.findById(1L).orElse(null)).name("FAQ").slug("cau-hoi-thuong-gap").order(2L).isActive(true).isDeleted(false).build(),
                    Category.builder().version(versionRepository.findById(2L).orElse(null)).name("Changelog").slug("thay-doi-phien-ban").order(1L).isActive(true).isDeleted(false).build()
            );
            categoryRepository.saveAll(categories);
        }

        if (docRepository.count() == 0) {
            List<Doc> docs = Arrays.asList(
                    Doc.builder().category(categoryRepository.findById(1L).orElse(null)).title("Cách đăng ký tài khoản").slug("dang-ky-tai-khoan").content("Bạn cần điền email và mật khẩu...").order(1).isActive(true).isDeleted(false).build(),
                    Doc.builder().category(categoryRepository.findById(2L).orElse(null)).title("Tôi quên mật khẩu, làm sao lấy lại?").slug("quen-mat-khau").content("Bạn có thể nhấn vào 'Quên mật khẩu'.").order(1).isActive(true).isDeleted(false).build(),
                    Doc.builder().category(categoryRepository.findById(3L).orElse(null)).title("v1.1 - Thêm chức năng tìm kiếm").slug("v1-1-search-update").content("Chúng tôi đã thêm chức năng tìm kiếm...").order(1).isActive(true).isDeleted(false).build()
            );
            docRepository.saveAll(docs);
        }
    }

    private float getFixedPrice(SubscriptionPackage.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> 2000f;
            case HALF_YEARLY -> 2500f;
            case YEARLY -> 3000f;
        };
    }
}