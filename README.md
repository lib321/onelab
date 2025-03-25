----- HTTP-запросы для product-service

1. Получить все товары (GET)
   URL: http://localhost:8085/products/all

2. Добавить товар (POST)
   URL: http://localhost:8085/products/add
   Аутентификация: Bearer Token
   Body:
   {
   "productName": "Игровая мышь UUUUU",
   "price": 59990,
   "quantity": 24,
   "categoryId": 23
   }
3. Получить товар по ID (GET)
   URL: http://localhost:8085/products/get/4

4. Обновить товар (PUT)
   URL: http://localhost:8085/products/update/106
   Аутентификация: Bearer Token
   Body:
   {
   "productName": "Updated Product Name",
   "price": 99999,
   "quantity": 10
   }
5. Удалить товар (DELETE)
   URL: http://localhost:8085/products/delete/106
   Аутентификация: Bearer Token

6. Добавить категорию (POST)
   URL: http://localhost:8085/products/add/category
   Аутентификация: Bearer Token
   Body:
   {
   "categoryName": "Коврики для мыши"
   }
7. Получить товары, сгруппированные по категориям (GET)
   URL: http://localhost:8085/products/grouped-by-category

8. Получить товары с пагинацией (GET)
   URL: http://localhost:8085/products?page=1&size=30



----- HTTP-запросы для user-service

1. Регистрация пользователя (POST)
   URL: http://localhost:8085/register
   Body:
   {
   "username": "admin2",
   "password": "admin2",
   "roles": [
   { "name": "USER" },
   { "name": "ADMIN" }
   ]
   }
2. Получить токен (POST)
   URL: http://localhost:8085/token
   Аутентификация: Basic Auth

3. Проверка токена (GET)
   URL: http://localhost:8085/validate
   Аутентификация: Bearer Token

4. Проверка роли пользователя (GET)
   URL: http://localhost:8085/validate-role?role=USER
   Аутентификация: Bearer Token

5. Главная страница (GET)
   URL: http://localhost:8085/home
   Аутентификация: Bearer Token



----- HTTP-запросы для inventory-service
1. Получить все товары (GET)
   URL: http://localhost:8085/inventory/all

2. Добавить товар (POST)
   URL: http://localhost:8085/inventory/add
   Body:
   {
   "productId": 108,
   "productName": "ASUS ROG Zephyrus E16",
   "price": 279990,
   "quantity": 5,
   "categoryName": "Ноутбуки",
   "addedAt": "2019-10-23",
   "updatedAt": "2019-10-23"
   }

3. Проверить наличие товара по productId (GET)
   URL: http://localhost:8085/inventory/stock/108

4. Зарезервировать товар (POST)
   URL: http://localhost:8085/inventory/reserve
   Body:
   {
   "productName": "Игровая мышь Logitech",
   "quantity": 5,
   "customerName": "Alex"
   }

5. Пополнить склад (PUT)
   URL: http://localhost:8085/inventory/restock
   Body:
   {
   "productName": "ASUS ROG Zephyrus E16",
   "productId": 108,
   "quantity": 1
   }

6. Проверить запас товара по productId (GET)
   URL: http://localhost:8085/inventory/check-stock/108

7. Удалить товар по productId (DELETE)
   URL: http://localhost:8085/inventory/delete/104

8. Получить все запасы (GET)
   URL: http://localhost:8085/inventory/all-stock

9. Проверить доступность товаров на складе для заказа (POST)
   URL: http://localhost:8085/inventory/check-stock-order
   Body:
   [
   {
   "productId": 100,
   "productName": "DJI RS 3 Pro Combo",
   "price": 999990,
   "quantity": 2,
   "categoryName": "Стабилизаторы"
   },
   {
   "productId": 101,
   "productName": "Игровая мышь RAZER",
   "price": 69990,
   "quantity": 5,
   "categoryName": "Мыши"
   },
   {
   "productId": 102,
   "productName": "Игровая мышь GAZER",
   "price": 79990,
   "quantity": 22,
   "categoryName": "Мыши"
   }
   ]

10. Фильтр товаров по категории и цене (GET)
    URL: http://localhost:8085/inventory/filter?category=Ноутбуки&minPrice=150000&maxPrice=450000

11. Поиск товаров по ключевому слову (GET)
    URL: http://localhost:8085/inventory/search?keyword=Apple




----- HTTP-запросы для order-service

1. Создать заказ (POST)
   URL: http://localhost:8085/orders/add
   Authorization: Bearer Token
   Body:
   {
   "customerName": "Иван Иванов",
   "items": [
   {
   "productId": 101,
   "productName": "Игровая мышь RAZER",
   "quantity": 2
   },
   {
   "productId": 102,
   "productName": "Игровая мышь GAZER",
   "quantity": 2
   }
   ]
   }

2. Получить заказ по orderId (GET)
   URL: http://localhost:8085/orders/get/3
   Authorization: Bearer Token

3. Получить все заказы клиента (GET)
   URL: http://localhost:8085/orders/all/Иван Иванов
   Authorization: Bearer Token

4. Удалить заказ по orderId (DELETE)
   URL: http://localhost:8085/orders/delete/4
   Authorization: Bearer Token

5. Обновить заказ по orderId (PUT)
   URL: http://localhost:8085/orders/update/3
   Authorization: Bearer Token
   Body:
   {
   "updatedItems": [
   {
   "productId": 101,
   "quantity": 6
   },
   {
   "productId": 102,
   "quantity": 5
   }
   ]
   }

    



