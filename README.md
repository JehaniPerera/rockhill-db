# rockhill-db
reception hall booking and management system

## Getting Started

1. Clone the repo and open the folder in IntelliJ **as a Project** (not "Open as File").
2. Wait for Maven to finish resolving dependencies (progress bar at the bottom).
3. Install and start a local MySQL-compatible server — XAMPP's MySQL or standalone MySQL both work. Each person runs their own; this is not shared between machines.
4. In MySQL Workbench (or phpMyAdmin), run `00_setup.sql` against your local server. Confirm a `rockhill` schema/database now exists.
5. Copy `src/main/resources/application.properties.example` to a new file named `application.properties` in the same folder. Fill in your own DB username/password if different from the example. This file is personal per machine — don't commit it.
6. Open `ReceptionHallApplication.java` and click the green ▶️ run button. Look for `Started ReceptionHallApplication` in the console with no red errors.
7. Visit `http://localhost:8081` in a browser — a "Whitelabel Error Page" is expected and means it's working.

## Code structure
Each person's function lives in its own package under `src/main/java/com/rockhill/receptionhall/` — for example `menu`, `booking`, `staff`, `billing`. Don't put your code in someone else's package.

## Git workflow
- `git pull` before you start working each session.
- Prefer your own branch: `git checkout -b feature/your-function-name`.
- Commit small, working chunks with clear messages.
- `git push`, then merge into `main` once it runs cleanly for you.
