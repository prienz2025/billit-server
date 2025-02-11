-- 기존 테이블 삭제
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bookmark_stations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS membership_payments;
DROP TABLE IF EXISTS memberships;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS rental_history;
DROP TABLE IF EXISTS rental_items;
DROP TABLE IF EXISTS rental_item_types;
DROP TABLE IF EXISTS rental_payments;
DROP TABLE IF EXISTS rental_station_items;
DROP TABLE IF EXISTS rental_stations;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;


-- 테이블 생성

CREATE TABLE users
(
    id            BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) UNIQUE,
    password      VARCHAR(255),
    profile_image VARCHAR(255),
    nickname      VARCHAR(255) UNIQUE,
    token         VARCHAR(255),
    role          VARCHAR(255),
    provider_type VARCHAR(255),
    create_at     DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at     DATETIME
);

CREATE TABLE memberships
(
    id              BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    status          BOOLEAN,
    start_date      DATETIME,
    expiration_date DATETIME,
    extension_count INTEGER,
    user_id         BIGINT UNIQUE,
    create_at       DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at       DATETIME
);

CREATE TABLE membership_payments
(
    id              BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    payment_date    DATETIME,
    start_date      DATETIME,
    expiration_date DATETIME,
    extension_count INTEGER,
    total_amount    INTEGER,
    order_id        VARCHAR(255),
    payment_key     VARCHAR(255),
    user_id         BIGINT,
    create_at       DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at       DATETIME
);


CREATE TABLE bookmark_stations
(
    id                BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    user_id           BIGINT,
    rental_station_id BIGINT,
    create_at         DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at         DATETIME
);


CREATE TABLE rental_stations
(
    id         BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255),
    address    VARCHAR(255),
    latitude   DECIMAL(9, 6),
    longitude  DECIMAL(9, 6),
    open_time  VARCHAR(255),
    close_time VARCHAR(255),
    close_day  VARCHAR(255),
    status     varchar(255),
    grade      varchar(255),
    create_at  DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at  DATETIME
);

CREATE TABLE rental_item_types
(
    id          BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    category    VARCHAR(255),
    name        VARCHAR(255),
    image       VARCHAR(255),
    description VARCHAR(255),
    price       INTEGER,
    create_at   DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at   DATETIME
);

CREATE TABLE rental_items
(
    id                  BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    status              VARCHAR(255),
    token               VARCHAR(255),
    rental_item_type_id BIGINT,
    current_station_id  BIGINT,
    create_at           DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at           DATETIME
);

CREATE TABLE rental_station_items
(
    id                  BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    stock               INTEGER,
    rental_station_id   BIGINT,
    rental_item_type_id BIGINT,
    create_at           DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at           DATETIME
);

CREATE TABLE rental_payments
(
    id                BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    rental_history_id BIGINT,
    order_id          VARCHAR(255),
    order_name        VARCHAR(255),
    payment_key       VARCHAR(255),
    total_amount      INTEGER,
    payment_date      DATETIME,
    payment_method    VARCHAR(255),
    payment_type      VARCHAR(255),
    status            VARCHAR(255),
    create_at         DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at         DATETIME
);

CREATE TABLE rental_history
(
    id                   BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    status               VARCHAR(255),
    rental_time_hour     INTEGER,
    start_time           DATETIME,
    expected_return_time DATETIME,
    return_time          DATETIME,
    token                VARCHAR(255),
    user_id              BIGINT,
    rental_item_id       BIGINT,
    rental_station_id    BIGINT,
    return_station_id    BIGINT,
    create_at            DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at            DATETIME
);

CREATE TABLE events
(
    id            BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    title         VARCHAR(255),
    banner_image  VARCHAR(255),
    content_image VARCHAR(255),
    status        VARCHAR(255),
    start_date    DATETIME,
    end_date      DATETIME,
    create_at     DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at     DATETIME
);

CREATE TABLE notices
(
    id        BIGINT PRIMARY KEY                                             NOT NULL AUTO_INCREMENT,
    title     VARCHAR(255),
    content   VARCHAR(255),
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    update_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    delete_at DATETIME
);


-- 외래 키 추가
ALTER TABLE bookmark_stations
    ADD CONSTRAINT FK_bookmark_stations_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE bookmark_stations
    ADD CONSTRAINT FK_bookmark_stations_rental_station FOREIGN KEY (rental_station_id) REFERENCES rental_stations (id);

ALTER TABLE membership_payments
    ADD CONSTRAINT FK_membership_payments_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE memberships
    ADD CONSTRAINT FK_memberships_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rental_history
    ADD CONSTRAINT FK_rental_history_user FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT FK_rental_history_item FOREIGN KEY (rental_item_id) REFERENCES rental_items (id),
    ADD CONSTRAINT FK_rental_history_rental_station FOREIGN KEY (rental_station_id) REFERENCES rental_stations (id),
    ADD CONSTRAINT FK_rental_history_return_station FOREIGN KEY (return_station_id) REFERENCES rental_stations (id);

ALTER TABLE rental_items
    ADD CONSTRAINT FK_rental_items_type FOREIGN KEY (rental_item_type_id) REFERENCES rental_item_types (id);

ALTER TABLE rental_payments
    ADD CONSTRAINT FK_rental_payments_history FOREIGN KEY (rental_history_id) REFERENCES rental_history (id);

ALTER TABLE rental_station_items
    ADD CONSTRAINT FK_rental_station_items_type FOREIGN KEY (rental_item_type_id) REFERENCES rental_item_types (id),
    ADD CONSTRAINT FK_rental_station_items_station FOREIGN KEY (rental_station_id) REFERENCES rental_stations (id);