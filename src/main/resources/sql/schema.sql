CREATE TABLE users
(
    id        INT         NOT NULL AUTO_INCREMENT,
    login     varchar(20) not null,
    password  varchar(20) not null,
    firstname VARCHAR(20) NOT NULL,
    lastname  VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id      int not null auto_increment,
    user_id int not null,
    primary key (id),
    foreign key (user_id) references users (id) on delete cascade
);

CREATE TABLE product
(
    id    int         not null auto_increment,
    name  varchar(20) not null,
    price int         not null,
    primary key (id)
);

create table order_products
(
    id         int not null auto_increment,
    order_id   int not null,
    product_id int not null,
    count      int not null,
    primary key (id),
    foreign key (order_id) references orders (id) on delete cascade,
    foreign key (product_id) references product (id) on delete cascade
);