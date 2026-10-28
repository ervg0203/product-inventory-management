# Product Inventory Management API

A Spring Boot REST API for managing products, warehouses, and stock held across warehouses. The API supports CRUD operations, bulk writes, paginated and advanced searches, Excel imports, stock adjustments, low-stock reporting, and consistent JSON responses.

## Contents

- [Features](#features)
- [Technology stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Running locally](#running-locally)
- [Configuration](#configuration)
- [API conventions](#api-conventions)
- [API reference](#api-reference)
- [Request models](#request-models)
- [Search](#search)
- [Excel imports](#excel-imports)
- [Validation and errors](#validation-and-errors)
- [Testing and coverage](#testing-and-coverage)
- [Project structure](#project-structure)

## Features

- Product catalog management with unique SKU handling.
- Warehouse management with active/inactive status.
- Inventory records linking products to warehouses.
- Exact stock replacement and atomic stock increases/decreases.
- Low-stock queries, including optional warehouse filtering.
- Zero-based pagination with configurable page-size limits.
- Query-parameter and JSON-based advanced searches.
- Bulk create/update operations.
- Excel (`.xlsx`) import for products and warehouses.
- OpenAPI/Swagger documentation.
- MongoDB persistence with automatic index creation.
- Centralized validation and error responses.

## Technology stack

| Component | Version/technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Build tool | Gradle Wrapper |
| Database | MongoDB |
| API documentation | SpringDoc OpenAPI 3.1.0 |
| Excel processing | Apache POI 5.4.1 |
| Testing | JUnit Platform, Spring Boot test support |
| Coverage | JaCoCo 0.8.13 |

## Prerequisites

- JDK 21 or newer.
- MongoDB running locally or an accessible MongoDB instance.
- Windows users can use `gradlew.bat`; macOS/Linux users can use `./gradlew`.

By default, the application connects to:

```text
mongodb://localhost:27017/inventory
```

## Running locally

1. Start MongoDB and ensure the `inventory` database is reachable.
2. Clone the repository and change into the project directory.
3. Start the application:

   ```powershell
   .\gradlew.bat bootRun
   ```

4. The API is available at:

   ```text
   http://localhost:8090
   ```

Build an executable JAR and run it with:

```powershell
.\gradlew.bat clean build
java -jar build\libs\inventory-0.0.1-SNAPSHOT.jar
```

## Configuration

Configuration is in `src/main/resources/application.properties` and can be overridden with environment-specific Spring configuration.

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | `8090` | HTTP port. |
| `spring.mongodb.uri` | `mongodb://localhost:27017/inventory` | MongoDB connection URI. |
| `spring.data.mongodb.auto-index-creation` | `true` | Enables MongoDB index creation. |
| `inventory.pagination.default-page` | `0` | Default zero-based page number. |
| `inventory.pagination.default-page-size` | `20` | Default page size. |
| `inventory.pagination.max-page-size` | `100` | Maximum accepted page size. |
| `inventory.import.batch-size` | `100` | Maximum number of validated records written in one import batch. |

For a different database, override the URI when starting the application:

```powershell
.\gradlew.bat bootRun --args="--spring.mongodb.uri=mongodb://localhost:27017/inventory"
```

## API conventions

All API routes are under `/api/v1`. Unless otherwise noted, successful JSON responses use this envelope:

```json
{
  "success": true,
  "message": "Product fetched successfully",
  "data": {}
}
```

Paginated responses have this shape inside `data`:

```json
{
  "content": [],
  "totalRecords": 0,
  "page": 0,
  "pageSize": 20,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Page numbering starts at `0`. The default page size is `20`; sizes from `1` through `100` are supported.

## API reference

### Products

Base path: `/api/v1/products`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/` | Browse products with pagination. Query: `page`, `size`. | `200` |
| `GET` | `/search` | Search with query parameters. | `200` |
| `POST` | `/advance-search` | Search using JSON filters, operators, pagination, and sorting. | `200` |
| `GET` | `/{id}` | Get one product. | `200` |
| `POST` | `/create` | Create one product. | `201` |
| `POST` | `/create-multi` | Create multiple products. | `201` |
| `PUT` | `/update` | Update one product; include its `id`. | `200` |
| `PUT` | `/update-multi` | Update multiple products; each item must include its `id`. | `200` |
| `POST` | `/import` | Import products from an `.xlsx` multipart file. | `200` |
| `DELETE` | `/{id}` | Delete one product and its related inventory. | `200` |
| `DELETE` | `/delete-all` | Delete all products and related inventory. | `200` |
| `DELETE` | `/delete-by-filters` | Delete products matching an advanced-search request. | `200` |

Example product:

```json
{
  "sku": "SKU-1001",
  "title": "Wireless Headphones",
  "description": "Noise-cancelling over-ear headphones",
  "category": "audio",
  "price": 149.99,
  "discountPercentage": 10,
  "rating": 4.5,
  "brand": "Example Audio",
  "returnPolicy": "30 days"
}
```

Create a product:

```bash
curl -X POST http://localhost:8090/api/v1/products/create \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-1001","title":"Wireless Headphones","description":"Noise-cancelling over-ear headphones","category":"audio","price":149.99,"discountPercentage":10,"rating":4.5,"brand":"Example Audio","returnPolicy":"30 days"}'
```

### Warehouses

Base path: `/api/v1/warehouses`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/` | Browse warehouses with pagination. Query: `page`, `size`. | `200` |
| `GET` | `/search` | Search with query parameters. | `200` |
| `POST` | `/advance-search` | Search using JSON filters, operators, pagination, and sorting. | `200` |
| `GET` | `/{id}` | Get one warehouse. | `200` |
| `POST` | `/create` | Create one warehouse. | `201` |
| `POST` | `/create-multi` | Create multiple warehouses. | `201` |
| `PUT` | `/update` | Update one warehouse; include its `id`. | `200` |
| `PUT` | `/update-multi` | Update multiple warehouses; each item must include its `id`. | `200` |
| `POST` | `/import` | Import warehouses from an `.xlsx` multipart file. | `200` |
| `DELETE` | `/{id}` | Delete one warehouse and its inventory. | `200` |
| `DELETE` | `/delete-all` | Delete all warehouses and related inventory. | `200` |
| `DELETE` | `/delete-by-filters` | Delete warehouses matching an advanced-search request. | `200` |

Example warehouse:

```json
{
  "code": "WH-001",
  "name": "Central Warehouse",
  "address": "100 Main Street",
  "city": "London",
  "state": "Greater London",
  "postalCode": "SW1A 1AA",
  "active": true
}
```

### Inventory

Base path: `/api/v1/inventory`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/create` | Create stock for a product in an active warehouse. | `201` |
| `POST` | `/create-multi` | Create multiple inventory records. | `201` |
| `PUT` | `/update-multi` | Update multiple inventory records by ID. | `200` |
| `GET` | `/{inventoryId}` | Get one inventory record. | `200` |
| `GET` | `/products/{productId}` | Get all stock for a product and its total stock. | `200` |
| `GET` | `/warehouses/{warehouseId}` | Get all inventory in a warehouse. | `200` |
| `PUT` | `/{inventoryId}/stock` | Set exact stock quantity. | `200` |
| `PATCH` | `/{inventoryId}/stock/increase` | Increase stock by a positive quantity. | `200` |
| `PATCH` | `/{inventoryId}/stock/decrease` | Decrease stock; insufficient stock returns `409`. | `200` |
| `GET` | `/low-stock` | Find records at or below `threshold`; optional `warehouseId`. | `200` |
| `DELETE` | `/{inventoryId}` | Delete one inventory record. | `204` |

Example inventory requests:

```json
{
  "productId": "PRODUCT_ID",
  "warehouseId": "WAREHOUSE_ID",
  "quantity": 25
}
```

```json
{
  "quantity": 5,
  "reason": "New shipment"
}
```

Example stock calls:

```bash
curl -X PATCH http://localhost:8090/api/v1/inventory/INVENTORY_ID/stock/increase \
  -H "Content-Type: application/json" \
  -d '{"quantity":5,"reason":"New shipment"}'

curl "http://localhost:8090/api/v1/inventory/low-stock?threshold=10"
```

## Request models

### Product fields

`sku`, `title`, `description`, `category`, `price`, `discountPercentage`, `rating`, `brand`, and `returnPolicy` are required for product validation. `id` is required when updating.

| Field | Rules |
| --- | --- |
| `sku` | Required, maximum 50 characters, unique. |
| `title` | Required, maximum 150 characters. |
| `description` | Required, maximum 2,000 characters. |
| `category` | Required, maximum 100 characters. |
| `price` | Required, greater than zero, maximum 10 integer digits, at most 2 decimal places. |
| `discountPercentage` | Required, from 0 to 100, at most 2 decimal places. |
| `rating` | Required, from 0 to 5, at most 2 decimal places. |
| `brand` | Required, maximum 100 characters. |
| `returnPolicy` | Required, maximum 150 characters. |

### Warehouse fields

`code`, `name`, `address`, `city`, `state`, and `postalCode` are required. `id` is required when updating. `active` controls whether stock can be created or adjusted in the warehouse.

| Field | Maximum length |
| --- | ---: |
| `code` | 50 |
| `name` | 150 |
| `address` | 250 |
| `city` | 100 |
| `state` | 100 |
| `postalCode` | 20 |

### Inventory fields

- `productId` and `warehouseId` must be non-blank.
- Inventory creation quantity may be zero or greater.
- Exact stock quantity may be zero or greater.
- Increase/decrease quantities must be greater than zero.
- Decrease operations cannot make stock negative.
- Inventory creation and stock adjustments require an active warehouse.

## Search

### Query-parameter search

`GET /api/v1/products/search` accepts:

- Text: `query`, `title`, `description`.
- Exact values: `sku`, `brand`, `category`, `returnPolicy`.
- Numeric ranges: `minPrice`, `maxPrice`, `minRating`, `maxRating`, `minDiscountPercentage`, `maxDiscountPercentage`.
- Numeric comparisons: `priceOperator`/`priceValue`, `ratingOperator`/`ratingValue`, `discountOperator`/`discountValue`.
- Lists: `brands`, `categories` as comma-separated values.
- Ranges: `priceBetween` and `ratingBetween` as `min,max`.
- Pagination/sorting: `page`, `size`, `sortBy`, `sortDirection` (`ASC` or `DESC`).

Example:

```text
GET /api/v1/products/search?query=phone&minPrice=500&maxPrice=1500&page=0&size=10&sortBy=price&sortDirection=ASC
```

`GET /api/v1/warehouses/search` accepts `query`, `code`, `name`, `address`, `city`, `state`, `postalCode`, `active`, comma-separated `codes`, `names`, `cities`, `states`, plus `page`, `size`, `sortBy`, and `sortDirection`.

### Advanced JSON search

Use `POST /api/v1/products/advance-search` or `POST /api/v1/warehouses/advance-search`.

```json
{
  "filters": [
    {
      "field": "price",
      "operator": "GREATER_THAN_OR_EQUAL",
      "value": 100
    },
    {
      "field": "category",
      "operator": "EQUALS",
      "value": "audio"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "price",
  "sortDirection": "ASC"
}
```

Filters use `AND` semantics. Supported operators are `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`, `GREATER_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `IN`, and `BETWEEN`.

Product fields allowed in advanced search are `id`, `sku`, `title`, `description`, `category`, `price`, `discountPercentage`, `rating`, `brand`, `returnPolicy`, `createdAt`, and `updatedAt`. Warehouse fields are `id`, `code`, `name`, `address`, `city`, `state`, `postalCode`, and `active`.

## Excel imports

The first worksheet is read. The header names are case-insensitive and spaces, underscores, and hyphens are ignored.

Upload a file using the form field name `file`:

```bash
curl -X POST http://localhost:8090/api/v1/products/import \
  -F "file=@products.xlsx"

curl -X POST http://localhost:8090/api/v1/warehouses/import \
  -F "file=@warehouses.xlsx"
```

Required product columns:

```text
sku, title, description, category, price, discountPercentage, rating, brand, returnPolicy
```

Optional product column:

```text
id
```

Required warehouse columns:

```text
code, name, address, city, state, postalCode
```

Optional warehouse columns:

```text
id, active
```

Rows with an `id` are treated as updates; rows without an `id` are treated as creates. All rows are validated before persistence. A successful import returns:

```json
{
  "success": true,
  "message": "Products imported successfully",
  "data": {
    "created": [],
    "updated": []
  }
}
```

## Validation and errors

Errors use the same response envelope with `success: false`:

```json
{
  "success": false,
  "message": "Request validation failed",
  "data": null
}
```

Common status codes:

| Status | Meaning |
| ---: | --- |
| `200` | Successful read, update, import, or delete. |
| `201` | Successful create operation. |
| `204` | Inventory deleted successfully; no response body. |
| `400` | Malformed request, invalid pagination, unsupported search value, or invalid stock quantity. |
| `404` | Product, warehouse, or inventory record was not found. |
| `409` | Duplicate SKU/code, inactive warehouse, duplicate inventory relationship, or insufficient stock. |
| `422` | One or more product/warehouse records failed business validation. |
| `503` | Database/service is temporarily unavailable. |
| `500` | Unexpected server error. |

Validation failures for bulk product and warehouse operations include field-level errors on the affected records. Destructive operations such as `delete-all` permanently remove data and should be protected appropriately before production use.

## OpenAPI and Swagger UI

When the application is running:

- Swagger UI: <http://localhost:8090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8090/v3/api-docs>

The generated documentation also contains endpoint response examples.

## Testing and coverage

Run the test suite:

```powershell
.\gradlew.bat test
```

Generate the JaCoCo HTML coverage report:

```powershell
.\gradlew.bat jacocoTestReport
```

The report is generated at:

```text
build/reports/jacoco/test/html/index.html
```

## Project structure

```text
src/
  main/
    java/com/example/inventory/
      config/       Application and OpenAPI configuration
      controller/   HTTP endpoints
      dto/          Request, response, pagination, and error models
      entity/       MongoDB documents
      enums/        Search fields, operators, and error messages
      exception/    Domain exceptions and global exception handling
      mapper/       Entity/DTO mapping
      repository/   Spring Data MongoDB repositories
      service/      Business logic
      util/         Validation, search, and Excel parsing
    resources/
      application.properties
  test/             Unit and MVC/service tests
```

## License

No license file is currently included in the repository.

## API Documentation and Presentation Files

[Product-Inventory-Management-System.pptx](../Product-Inventory-Management-System.pptx)

[PRODUCT INVENTORY MANAGEMENT SYSTEM_3562.docx](../PRODUCT%20INVENTORY%20MANAGEMENT%20SYSTEM_3562.docx)
