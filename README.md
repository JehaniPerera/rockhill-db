# Rockhill Reception Hall Booking & Management System

SE2012_GRP_26 - Spring Boot, Thymeleaf and MySQL web application for Rockhill Reception Hall & Caterers.

| Function | Owner |
|---|---|
| Booking & Event Scheduling | Kawmadee R.L.D. (IT25104035) |
| Package, Menu & Decor Selection | Dinuji S.A.P. (IT25103187) |
| Staff & Vendor Assignment | Perera L.D.U.J. (IT25300121) |
| Payments & Billing | Fernando W.S.H. (IT25102808) |

## How to run it

Requirements: JDK 17, Maven (the project includes `mvnw`), IntelliJ IDEA, and a local MySQL server (standalone MySQL or XAMPP).

1. Clone the repo and open the folder in IntelliJ **as a Project** (not "Open as File"). Use the `demo5` branch.
2. Wait for Maven to finish downloading dependencies (progress bar at the bottom).
3. Start your local MySQL server (port 3306). Each person runs their own; nothing is shared between machines.
4. `src/main/resources/application.properties` is already in the repo, so there is nothing to copy. It uses MySQL user `root` with an empty password, which is the XAMPP default. If your MySQL has a password, do **not** edit the file: in IntelliJ open Run > Edit Configurations, add the environment variable `DB_PASSWORD=your_password` (and `DB_USERNAME=...` if your user is not `root`), and run again. If you already had your own `application.properties` from before, delete it before you `git pull`.
5. You do not need to create the database. The app creates `rockhill_app` on the first start and fills it with sample data from `schema.sql` and `data.sql`. If an older run left a broken `rockhill_app` behind, drop it in MySQL Workbench (`DROP DATABASE rockhill_app;`) and start again.
6. Open `ReceptionHallApplication.java` and click the green run button. Wait for `Started ReceptionHallApplication` with no red errors. A line about "MySQL 5.5.5" is harmless.
7. Open `http://localhost:8081`. You are sent to the login page. Ask the team for the demo admin login, or register as a customer.

If the app does not start, the first red line in the console is the real error: "Access denied for user" means a wrong or missing `DB_PASSWORD`, and "Communications link failure" means MySQL is not running.

## Pages

- Customers: home (`/index.html`), book a wedding or event, choose package or menu, add-ons and decor, my bookings, my payments (`/customer.html`).
- Admin: `/admin/index.html` links to booking management, catalog management, staff and vendors, and payments and billing.

## Code structure

All code lives under `src/main/java/com/rockhill/reception_hall/`, one package per function: `booking`, `menu`, `staff`, `payment`, plus `portal` for login and roles. Do not put your code in someone else's package.

## Git workflow

- `git pull` before you start working each session.
- Work on your own branch (`feature/your-function`) and merge into the shared integration branch (`demo5`) with a pull request.
- Commit small, working chunks with clear messages. Add files by name; never run `git add .`.
- Never put a real password in `application.properties` (use the `DB_PASSWORD` environment variable), and never commit the `uploads/` folder.
