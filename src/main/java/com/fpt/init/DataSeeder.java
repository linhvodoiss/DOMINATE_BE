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
    private final OptionRepository optionRepository; // ✅ Add this

    @Override
    public void run(String... args) throws Exception {

        // Seed Option table first
        if (optionRepository.count() == 0) {
            List<Option> options = Arrays.asList(
                    Option.builder().name("5 documents per month").build(),
                    Option.builder().name("Unlimited access").build(),
                    Option.builder().name("Priority support").build()
            );
            optionRepository.saveAll(options);
        }

        // Seed Users
        if (userRepository.count() == 0) {
            List<User> users = Arrays.asList(
                    User.builder()
                            .userName("admin")
                            .email("admin@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Nguyen Van")
                            .lastName("A")
                            .phoneNumber("0987654321")
                            .role(Role.ADMIN)
                            .status(UserStatus.ACTIVE)
                            .isActive(true)
                            .avatarUrl("")
                            .build(),
                    User.builder()
                            .userName("CaoVanBay")
                            .email("Bay@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Cao Van")
                            .lastName("Bay")
                            .phoneNumber("0999999999")
                            .role(Role.CUSTOMER)
                            .status(UserStatus.ACTIVE)
                            .isActive(true)
                            .avatarUrl("")
                            .build(),
                    User.builder()
                            .userName("LeThiTam")
                            .email("Tam@gmail.com")
                            .password("$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62")
                            .firstName("Le Thi")
                            .lastName("Tam")
                            .phoneNumber("0912345678")
                            .role(Role.CUSTOMER)
                            .status(UserStatus.ACTIVE)
                            .isActive(true)
                            .avatarUrl("")
                            .build()
            );
            userRepository.saveAll(users);
        }

        // Seed Subscription Packages with options
        if (subscriptionPackageRepository.count() == 0) {
            Option option1 = optionRepository.findById(1L).orElseThrow();
            Option option2 = optionRepository.findById(2L).orElseThrow();
            Option option3 = optionRepository.findById(3L).orElseThrow();

            // Bước 1: Tạo SubscriptionPackage KHÔNG GÁN option
            SubscriptionPackage basicPlan = SubscriptionPackage.builder()
                    .name("Basic Plan")
                    .price(2000f)
                    .discount(0f)
                    .billingCycle(SubscriptionPackage.BillingCycle.MONTHLY)
                    .isActive(true)
                    .build();

            SubscriptionPackage proPlan = SubscriptionPackage.builder()
                    .name("Pro Plan")
                    .price(2500f)
                    .discount(10f)
                    .billingCycle(SubscriptionPackage.BillingCycle.MONTHLY)
                    .isActive(true)
                    .build();

            // Lưu lần đầu không có option
            basicPlan = subscriptionPackageRepository.save(basicPlan);
            proPlan = subscriptionPackageRepository.save(proPlan);

            // Gán option (sử dụng List thay vì Set)
            basicPlan.setOptions(List.of(option1));
            proPlan.setOptions(List.of(option2, option3));

            // Lưu lại sau khi gán option
            subscriptionPackageRepository.save(basicPlan);
            subscriptionPackageRepository.save(proPlan);
        }

        if (paymentOrderRepository.count() == 0) {
            PaymentOrder order1 = PaymentOrder.builder()
                    .user(userRepository.findById(1L).orElse(null))
                    .subscriptionPackage(subscriptionPackageRepository.findById(2L).orElse(null))
                    .orderId(123456)
                    .paymentLink("https://payment.example.com/checkout/ORDER-123456")
                    .paymentStatus(PaymentOrder.PaymentStatus.SUCCESS)
                    .paymentMethod(PaymentOrder.PaymentMethod.BANK)
                    .licenseCreated(false)
                    .bin("970415")
                    .accountName("NGUYEN THI HUONG")
                    .accountNumber("0386331971")
                    .qrCode("00020101021138540010A00000072701240006970415011003863319710208QRIBFTTA53037045802VN63046733")
                    .build();

            paymentOrderRepository.saveAll(List.of(order1));
        }

        if (licenseRepository.count() == 0) {
            License license = License.builder()
                    .user(userRepository.findById(1L).orElse(null))
                    .subscriptionPackage(subscriptionPackageRepository.findById(2L).orElse(null))
                    .licenseKey("LIC-ABCDEF-123456")
                    .duration(60)
                    .ip("203.113.78.9")
                    .hardwareId("203.113.78.9")
                    .canUsed(false)
                    .build();
            licenseRepository.save(license);
        }

        if (versionRepository.count() == 0) {
            List<Version> versions = Arrays.asList(
                    Version.builder().version("v1.0").description("Phiên bản đầu tiên của hệ thống").build(),
                    Version.builder().version("v1.1").description("Bổ sung thêm chức năng tìm kiếm").build()
            );
            versionRepository.saveAll(versions);
        }

        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                    Category.builder().version(versionRepository.findById(1L).orElse(null)).name("Hướng dẫn sử dụng").slug("huong-dan-su-dung").order(1L).isActive(true).build(),
                    Category.builder().version(versionRepository.findById(1L).orElse(null)).name("FAQ").slug("cau-hoi-thuong-gap").order(2L).isActive(true).build(),
                    Category.builder().version(versionRepository.findById(2L).orElse(null)).name("Changelog").slug("thay-doi-phien-ban").order(1L).isActive(true).build()
            );
            categoryRepository.saveAll(categories);
        }

        if (docRepository.count() == 0) {
            List<Doc> docs = Arrays.asList(
                    Doc.builder().version(versionRepository.findById(1L).orElse(null)).category(categoryRepository.findById(1L).orElse(null)).title("Cách đăng ký tài khoản").slug("dang-ky-tai-khoan").content("Bạn cần điền email và mật khẩu...").order(1).isActive(true).build(),
                    Doc.builder().version(versionRepository.findById(1L).orElse(null)).category(categoryRepository.findById(2L).orElse(null)).title("Tôi quên mật khẩu, làm sao lấy lại?").slug("quen-mat-khau").content("Bạn có thể nhấn vào 'Quên mật khẩu'.").order(1).isActive(true).build(),
                    Doc.builder().version(versionRepository.findById(2L).orElse(null)).category(categoryRepository.findById(3L).orElse(null)).title("v1.1 - Thêm chức năng tìm kiếm").slug("v1-1-search-update").content("Chúng tôi đã thêm chức năng tìm kiếm...").order(1).isActive(true).build()
            );
            docRepository.saveAll(docs);
        }
    }
}
