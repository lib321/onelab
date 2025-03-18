http запросы inventory-service:

- Получить все товары (GET)
http://localhost:8082/api/new-inventory/products/Ноутбуки и ультрабуки

- Получить все товары между цен (GET)
http://localhost:8082/api/new-inventory/products/by-price?minPrice=500&maxPrice=2500

- Добавить категорию (POST)
http://localhost:8082/api/new-inventory/add/category
Body:
{
    "name": "Переходники"
}

- Добавить товар (POST)
http://localhost:8082/api/new-inventory/add/item
Body:
{
    "productId": 51, 
    "productName": "Переходник HDMI-VGA", 
    "quantity": 20, 
    "categoryName": "Переходники", 
    "price": 15, 
    "addedAt": "2024-03-19"
}

- Получить товары больше или равной цене (GET)
http://localhost:8082/api/new-inventory/filter?minPrice=500

- Сравнение производительности при последовательном и параллельном потоке (GET)
http://localhost:8082/api/new-inventory/compare

- Получить общую стоимость всех товаров (GET)
http://localhost:8082/api/new-inventory/total-price

- Группировка товаров по категориям (GET)
http://localhost:8082/api/new-inventory/group-by-category

- Группировка товаров по цене (выше, либо ниже) (GET)
http://localhost:8082/api/new-inventory/partition-by-price?price=500





















