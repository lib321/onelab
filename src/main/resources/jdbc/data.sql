INSERT INTO users (firstname, lastname) VALUES ('name1', 'surname1');
INSERT INTO users (firstname, lastname) VALUES ('name2', 'surname2');
INSERT INTO users (firstname, lastname) VALUES ('name3', 'surname3');

INSERT INTO orders (user_id) VALUES (1);
INSERT INTO orders (user_id) VALUES (1);
INSERT INTO orders (user_id) VALUES (2);

INSERT INTO product (name, price) VALUES ('prod1', 1000);
INSERT INTO product (name, price) VALUES ('prod2', 2000);
INSERT INTO product (name, price) VALUES ('prod3', 3000);

INSERT INTO order_products (order_id, product_id) VALUES (1, 1);
INSERT INTO order_products (order_id, product_id) VALUES (1, 2);
INSERT INTO order_products (order_id, product_id) VALUES (2, 3);

