# SmartCity360

SmartCity360 is a Java 17 MVC web application packaged as a WAR for Payara Server 6. It uses Jakarta EE Servlets/JSP, JDBC, and MySQL. City operations are simulated; the application does not control external infrastructure.

## Requirements

- JDK 17
- Maven 3.9+
- Payara Server 6 (Jakarta EE 10)
- MySQL 8+
- MySQL Connector/J available to the Payara domain

## Database and Payara

1. Create a MySQL database named `smartcity360` and run [`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql). For an existing database, apply the migrations in order: V2 adds persisted zone power and usage state; V3 adds a root-safe sibling-name unique constraint (check for duplicate root-city names first); V4 adds authentication and role data; V5 adds the restore-power operation type to zone history.
2. Install MySQL Connector/J where Payara's JDBC connection pool can load it (for example, in the Payara domain `lib` directory), then restart the domain.
3. In the Payara Administration Console, create a MySQL JDBC connection pool with URL, database username, and password supplied through the server configuration. Create the JDBC resource `jdbc/SmartCity360` for that pool. The application looks it up as `java:comp/env/jdbc/SmartCity360`.
4. Build and deploy `target/SmartCity360.war` to Payara. The welcome route is `/dashboard`.

### Initial City Manager

There is no public registration or default password. After creating a new database from `schema.sql`, or applying V4 to an existing database, generate a BCrypt hash locally from an interactive terminal:

```powershell
mvn -q -DskipTests package
$bcrypt = "$env:USERPROFILE\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar"
java -cp "target\classes;$bcrypt" lk.jiat.smartcity360.auth.PasswordHashTool
```

Insert the generated hash using the database operator's normal secure MySQL session. Replace every placeholder locally; never commit the hash or a password:

```sql
INSERT INTO app_user (full_name, username, email, password_hash, role, active)
VALUES ('<manager name>', '<manager username>', '<manager email>', '<locally generated BCrypt hash>', 'CITY_MANAGER', TRUE);
```

Do not place database credentials or cryptographic keys in source control. The Security Streams module uses:

- `SMARTCITY_STREAM_AES_KEY`: Base64-encoded AES key of 16, 24, or 32 bytes.
- `SMARTCITY_STREAM_HMAC_KEY`: Base64-encoded HMAC key of at least 32 bytes.

Set these in the Payara process environment before starting the domain if you want to exercise the encryption or authentication decorators. Compression can be demonstrated without these keys.

## Build and tests

```powershell
mvn clean verify
```

The generated WAR contains application code and local static assets; Jakarta EE APIs are provided by Payara. Authentication uses BCrypt password hashes, HTTP-only cookie sessions, database-backed account validation, role checks, and CSRF tokens on state-changing forms.
