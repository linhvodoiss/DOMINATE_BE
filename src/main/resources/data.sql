-- Sample data
INSERT INTO `User`(username, email, `password`, firstName, lastName, phoneNumber, `role`, `status`, isActive, avatarUrl)
VALUES
    ('admin', 'admin@gmail.com', '$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62', 'Nguyen Van', 'A', '0987654321', 'ADMIN', 1, 1, ''),
    ('CaoVanBay', 'Bay@gmail.com', '$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62', 'Cao Van', 'Bay', '0999999999', 'CUSTOMER', 1, 1, ''),
    ('LeThiTam', 'Tam@gmail.com', '$2a$10$RAU5Vl1A6Iheyeg2MSBlVeLRLpH2kRSpredJkzJIm72ZscI6pg/62', 'Le Thi', 'Tam', '0912345678', 'CUSTOMER', 1, 1, '');

INSERT INTO SubscriptionPackage (name, price, discount, billingCycle, isActive, options)
VALUES
    ('Basic Plan', 9.99, 0, 'MONTHLY', 1, '5 documents per month'),
    ('Pro Plan', 19.99, 10, 'MONTHLY', 1, 'Unlimited access');

INSERT INTO PaymentOrder (user_id, subscription_id, orderId, paymentLink, paymentStatus, paymentMethod)
VALUES
    (1, 2, 123456, 'https://payment.example.com/checkout/ORDER-123456', 'SUCCESS','BANK'),
    (1, 2, 1234567, 'https://payment.example.com/checkout/ORDER-78912', 'SUCCESS','BANK');

INSERT INTO License (user_id, subscription_id, licenseKey, duration, ip, hardware_id)
VALUES
    (1, 2, 'LIC-ABCDEF-123456', 60, '203.113.78.9','203.113.78.9');

INSERT INTO Version (version, description)
VALUES
    ('v1.0', 'Phiên bản đầu tiên của hệ thống'),
    ('v1.1', 'Bổ sung thêm chức năng tìm kiếm');

INSERT INTO Category (version_id, name, slug, `order`, isActive)
VALUES
    (1, 'Hướng dẫn sử dụng', 'huong-dan-su-dung', 1, 1),
    (1, 'FAQ', 'cau-hoi-thuong-gap', 2, 1),
    (2, 'Changelog', 'thay-doi-phien-ban', 1, 1);

INSERT INTO Doc (version_id, category_id, title, slug, content, `order`, isActive)
VALUES
    (1, 1, 'Cách đăng ký tài khoản', 'dang-ky-tai-khoan', 'Bạn cần điền email và mật khẩu...', 1, 1),
    (1, 2, 'Tôi quên mật khẩu, làm sao lấy lại?', 'quen-mat-khau', 'Bạn có thể nhấn vào “Quên mật khẩu”.', 1, 1),
    (2, 3, 'v1.1 - Thêm chức năng tìm kiếm', 'v1-1-search-update', 'Chúng tôi đã thêm chức năng tìm kiếm...', 1, 1);
