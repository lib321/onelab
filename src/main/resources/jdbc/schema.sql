CREATE TABLE users
(
    id        INT         NOT NULL AUTO_INCREMENT,
    firstname VARCHAR(20) NOT NULL,
    lastname  VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id      int not null auto_increment,
    user_id int not null,
    primary key (id),
    foreign key (user_id) references users (id) ON DELETE CASCADE
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
    order_id   int not null,
    product_id int not null,
    primary key (order_id, product_id),
    foreign key (order_id) references orders (id) ON DELETE CASCADE,
    foreign key (product_id) references product (id) ON DELETE CASCADE
);