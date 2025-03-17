http запросы product-service:

- Получить все товары (GET)
http://localhost:8081/api/products/all

- Добавить товар (POST)
http://localhost:8081/api/products/add
Authorization: Basic Auth
Body: 
{ 
  "productName": "Игровая мышь Bazer",
  "description": "Быстрая откликаемость и RGB-подсветка",
  "price": 79.99,
  "quantity": 20
}

- Получить товар по id (GET)
http://localhost:8081/api/products/get/3

- Изменить товар по id (PUT)
http://localhost:8081/api/products/update/4
Authorization: Basic Auth
Body:
{
    "productName": "Updated Product Name",
    "description": "Updated product description",
    "price": 50.99,
    "quantity": 5
}

- Удалить товар по id (DELETE)
http://localhost:8081/api/products/delete/4
Authorization: Basic Auth


http запросы inventory-service:

- Получить все товары (GET)
http://localhost:8082/api/inventory/all

- Добавить товар (POST)
http://localhost:8082/api/inventory/add
Body:
{
    "productId": 5,
    "productName": "Ноутбук Dell",
    "quantity": 1
}

- Получить количество товара (GET)
http://localhost:8082/api/inventory/stock/18

- Изменить количество (PUT)
http://localhost:8082/api/inventory/restock
Body:
{
    "productId": 3,
    "quantity": 2
}

- Проверить количество (GET)
http://localhost:8082/api/inventory/check-stock/5

- Зарезервировать товар (POST)
http://localhost:8082/api/inventory/reserve
Body:
{
    "productName": "Наушники Bazer",
    "quantity": 5,
    "customerName": "Alex"
}

- Удалить товар (DELETE)
http://localhost:8082/api/inventory/delete/5

- Получить количество всех товаров (GET)
http://localhost:8082/api/inventory/all/stock


http запросы order-service

- Создать заказ (POST)
http://localhost:8083/api/orders/add
Authorization: Basic Auth
Body:
{
  "customerName": "Иван Иванов",
  "items": [
    {
      "productId": 5,
      "productName": "Игровая мышь Bazer",
      "quantity": 2
    }
  ]
}

- Получить заказ по id (GET)
http://localhost:8083/api/orders/get/1
Authorization: Basic Auth

- Удалить заказ по id (DELETE)
http://localhost:8083/api/orders/delete/1
Authorization: Basic Auth

- Получить заказы пользователя (GET)
http://localhost:8083/api/orders/all/Иван Иванов
Authorization: Basic Auth

- Изменить количество в заказе (PUT)
http://localhost:8083/api/orders/update/1
Authorization: Basic Auth
Body:
{
  "updatedItems": [
    {
            "productId": 5,
            "quantity": 0
        }
  ]
}


http запросы user-service:

- Зарегистрироваться (POST)
http://localhost:8084/register
{
  "username": "user6",
  "password": "pass6",
  "roles": [
    { "name": "USER" }
  ]
}

- Авторизоваться (GET)
http://localhost:8084/auth/validate
Authorization: Basic Auth























