# Java Mid - Abysalto

This is a demo Java Spring Boot application for the Java Mid position at Abysalto. It provides authentication, product management, favourites, and cart functionality, with integration to the DummyJSON API for product data.

## Running Locally

To run the application locally, use the `local` Spring profile. This will configure the application to use an in-memory H2 database and enable the H2 console at `/h2-console`.

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Getting Started

1. **Register**: Use the `/api/auth/register` endpoint to create a new user account.
2. **Login**: Use the `/api/auth/login` endpoint to authenticate and obtain access.
3. **Use the API**: Once logged in, you can access the rest of the application's features (products, favourites, cart, etc.).

You can explore and test the API using the built-in Swagger UI at `/swagger-ui/`.

---

- **Profile**: `local` (in-memory H2 database)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

For any issues, please contact the project maintainer.

