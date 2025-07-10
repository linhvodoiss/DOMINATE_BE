-- Create table User
DROP TABLE IF EXISTS `User`;
CREATE TABLE IF NOT EXISTS `User` (
                                      id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      username        CHAR(50) NOT NULL UNIQUE,
    email           CHAR(50) NOT NULL UNIQUE,
    `password`      VARCHAR(800) NOT NULL,
    firstName       NVARCHAR(50) NOT NULL,
    lastName        NVARCHAR(50) NOT NULL,
    phoneNumber     VARCHAR(20) NOT NULL UNIQUE,
    `role`          ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
    `status`        TINYINT DEFAULT 0,
    isActive        TINYINT DEFAULT 1,
    avatarUrl       VARCHAR(500),
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW()
    );

-- Registration_User_Token
DROP TABLE IF EXISTS `Registration_User_Token`;
CREATE TABLE IF NOT EXISTS `Registration_User_Token` (
                                                         id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                         token       CHAR(36) NOT NULL UNIQUE,
    user_id     BIGINT UNSIGNED NOT NULL,
    expiryDate  DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `User`(id)
    );

-- Reset_Password_Token
DROP TABLE IF EXISTS `Reset_Password_Token`;
CREATE TABLE IF NOT EXISTS `Reset_Password_Token` (
                                                      id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                      token       CHAR(36) NOT NULL UNIQUE,
    user_id     BIGINT UNSIGNED NOT NULL,
    expiryDate  DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `User`(id)
    );

-- Version
DROP TABLE IF EXISTS Version;
CREATE TABLE IF NOT EXISTS Version (
                                       id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                       version         VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(255),
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW()
    );

-- Category
DROP TABLE IF EXISTS Category;
CREATE TABLE IF NOT EXISTS Category (
                                        id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        version_id      BIGINT UNSIGNED NOT NULL,
                                        name            NVARCHAR(50) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    `order`         INT DEFAULT 0,
    isActive        TINYINT DEFAULT 1,
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW(),
    FOREIGN KEY (version_id) REFERENCES Version(id)
    );

-- Doc
DROP TABLE IF EXISTS Doc;
CREATE TABLE IF NOT EXISTS Doc (
                                   id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   version_id      BIGINT UNSIGNED NOT NULL,
                                   category_id     BIGINT UNSIGNED NOT NULL,
                                   title           NVARCHAR(255) NOT NULL,
    slug            VARCHAR(150) NOT NULL UNIQUE,
    content         TEXT,
    `order`         INT DEFAULT 0,
    isActive        TINYINT DEFAULT 1,
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW(),
    FOREIGN KEY (version_id) REFERENCES Version(id),
    FOREIGN KEY (category_id) REFERENCES Category(id)
    );

-- SubscriptionPackage
DROP TABLE IF EXISTS SubscriptionPackage;
CREATE TABLE IF NOT EXISTS SubscriptionPackage (
                                                   id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                   name            NVARCHAR(100) NOT NULL,
    price           FLOAT NOT NULL CHECK (price >= 0),
    discount        FLOAT DEFAULT 0 CHECK (discount >= 0),
    billingCycle    ENUM('MONTHLY', 'YEARLY') DEFAULT 'MONTHLY',
    isActive        TINYINT DEFAULT 1,
    options         VARCHAR(500),
    simulatedCount  INT DEFAULT 0,
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW()
    );

-- PaymentOrder
DROP TABLE IF EXISTS PaymentOrder;
CREATE TABLE IF NOT EXISTS PaymentOrder (
                                            id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                            user_id         BIGINT UNSIGNED NOT NULL,
                                            subscription_id BIGINT UNSIGNED NOT NULL,
                                            orderId         INT NOT NULL UNIQUE,
                                            paymentLink     VARCHAR(500),
    paymentStatus   ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    paymentMethod   ENUM('MOMO', 'VNPAY', 'BANK') DEFAULT 'BANK',
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES `User`(id),
    FOREIGN KEY (subscription_id) REFERENCES SubscriptionPackage(id)
    );

-- License
DROP TABLE IF EXISTS License;
CREATE TABLE IF NOT EXISTS License (
                                       id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                       user_id         BIGINT UNSIGNED NOT NULL,
                                       subscription_id BIGINT UNSIGNED NOT NULL,
                                       licenseKey      CHAR(36) NOT NULL UNIQUE,
    duration        INT NOT NULL,
    ip              VARCHAR(500),
    hardware_id     VARCHAR(500),
    can_used        TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT NOW(),
    updated_at      DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES `User`(id),
    FOREIGN KEY (subscription_id) REFERENCES SubscriptionPackage(id)
    );
