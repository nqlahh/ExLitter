# ExLitter 💸
> *Split all your expenses more easily — free from your troublesome EXpenses.*

ExLitter is a **group expense splitting web application** built with Java Spring Boot and Thymeleaf. It allows users to create groups, log shared expenses, and automatically calculate simplified debt settlements — all rendered server-side with zero frontend framework complexity.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [SOLID Principles](#-solid-principles)
- [Database Design](#-database-design)
- [Getting Started](#-getting-started)
- [How to Use](#-how-to-use)
- [API Routes](#-api-routes)
- [Known Limitations](#-known-limitations)

---

## ✨ Features

### Feature 1 — Group & Expense Management
- Create named groups (e.g. *Bali Trip 2025*, *House Shared Costs*)
- Add multiple members to each group via comma-separated input
- Log expenses with:
  - A description and total amount (RM)
  - Who paid for the expense
  - A flexible split strategy — split equally among **all** members, or choose **specific participants**
- Delete individual expenses
- View total spent, number of expenses, and per-expense share breakdown

### Feature 2 — Settle Up Dashboard
- Automatically calculates **simplified net balances** per member
- Uses a **debt-simplification algorithm** that reduces N×N debt matrices to the minimum number of transactions needed
- Per-member cards showing:
  - **Owes** section — who they owe money to, with a settle button
  - **Should Receive** section — who owes them (awaiting payment)
- **Partial settlement** support — enter any amount ≤ the full debt before hitting Settle
- Balances update automatically after each settlement

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| View Engine | Thymeleaf 3.1 |
| ORM | Spring Data JPA (Hibernate) |
| Database | SQLite (via `sqlite-jdbc` + Hibernate Community Dialects) |
| Styling | Custom CSS + Tabler Icons |
| Build Tool | Maven |

---

## 📁 Project Structure

```
splitapp/
├── pom.xml
└── src/
    └── main/
        ├── java/com/splitapp/
        │   ├── SplitAppApplication.java          # Entry point
        │   │
        │   ├── model/                            # @Entity classes (Data Layer)
        │   │   ├── Group.java
        │   │   ├── Member.java
        │   │   └── Expense.java
        │   │
        │   ├── repository/                       # Spring Data JPA (Data Layer)
        │   │   ├── GroupRepository.java
        │   │   ├── MemberRepository.java
        │   │   └── ExpenseRepository.java
        │   │
        │   ├── service/                          # Interfaces (Business Layer)
        │   │   ├── GroupService.java
        │   │   ├── ExpenseService.java
        │   │   ├── DebtCalculatorService.java
        │   │   └── SplitStrategy.java
        │   │
        │   ├── service/impl/                     # Implementations (Business Layer)
        │   │   ├── GroupServiceImpl.java
        │   │   ├── ExpenseServiceImpl.java
        │   │   ├── DebtCalculatorServiceImpl.java
        │   │   └── EqualSplitStrategy.java
        │   │
        │   ├── controller/                       # Spring MVC (Presentation Layer)
        │   │   ├── HomeController.java
        │   │   ├── GroupController.java
        │   │   ├── ExpenseController.java
        │   │   └── SettleController.java
        │   │
        │   └── dto/
        │       └── DebtEntry.java                # Data Transfer Object
        │
        └── resources/
            ├── application.properties            # SQLite datasource config
            ├── static/css/
            │   └── app.css                       # Global styles (ExLitter theme)
            └── templates/
                ├── groups/
                │   └── list.html                 # Groups list + create form
                ├── expenses/
                │   └── list.html                 # Expense log + add form
                └── settle/
                    └── index.html                # Settle up dashboard
```

---

## 🏗 Architecture

ExLitter follows a strict **3-Layer Architecture**:

```
┌─────────────────────────────────────────────────────┐
│              PRESENTATION LAYER                     │
│   Thymeleaf Templates (.html) + Spring Controllers  │
│   GroupController · ExpenseController · SettleController │
└────────────────────────┬────────────────────────────┘
                         │ calls interfaces only
┌────────────────────────▼────────────────────────────┐
│               BUSINESS LOGIC LAYER                  │
│   Service Interfaces + Implementations              │
│   GroupService · ExpenseService                     │
│   DebtCalculatorService · SplitStrategy             │
└────────────────────────┬────────────────────────────┘
                         │ calls repositories only
┌────────────────────────▼────────────────────────────┐
│                DATA ACCESS LAYER                    │
│   Spring Data JPA Repositories → SQLite             │
│   GroupRepository · MemberRepository                │
│   ExpenseRepository                                 │
└─────────────────────────────────────────────────────┘
```

**Key rule:** Each layer only communicates with the layer directly below it. Controllers never touch repositories; services never render HTML.

---

## 🧱 SOLID Principles

ExLitter was designed with all five SOLID principles in mind, which are documented here for reviewer reference.

### S — Single Responsibility Principle
`DebtCalculatorServiceImpl` has exactly **one job**: calculate and simplify debts. It contains zero persistence logic and zero controller logic. Similarly, `EqualSplitStrategy` only does split math.

### O — Open/Closed Principle
The `SplitStrategy` interface allows new split types to be added **without modifying existing code**:
```java
public interface SplitStrategy {
    Map<Member, BigDecimal> calculateSplit(BigDecimal totalAmount, List<Member> participants);
}
```
`EqualSplitStrategy` is the current implementation. A future `PercentageSplitStrategy` or `ExactAmountSplitStrategy` can be added by simply creating a new class — no existing code changes needed.

### L — Liskov Substitution Principle
Any implementation of `SplitStrategy` can be injected into `ExpenseServiceImpl` interchangeably. The service only depends on the interface contract, not the concrete class.

### I — Interface Segregation Principle
Service interfaces are kept small and focused:
- `GroupService` — only group CRUD operations
- `ExpenseService` — only expense CRUD + totals
- `DebtCalculatorService` — only balance calculation and settlement recording

No service is forced to implement methods it doesn't need.

### D — Dependency Inversion Principle
All controllers and services depend on **interfaces**, not concrete implementations. Spring's IoC container handles injection:
```java
// Controller depends on interface, not GroupServiceImpl
public GroupController(GroupService groupService, ...) { ... }

// Service depends on interface, not EqualSplitStrategy directly
public ExpenseServiceImpl(SplitStrategy splitStrategy, ...) { ... }
```

---

## 🗄 Database Design

ExLitter uses **SQLite** — a lightweight, file-based database that requires zero installation. The database file `splitapp.db` is created automatically on first run.

### Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────┐         ┌───────────────────┐
│    groups    │         │   members    │         │     expenses      │
├──────────────┤         ├──────────────┤         ├───────────────────┤
│ PK id        │◄──┐     │ PK id        │◄──┐     │ PK id             │
│    name      │   │     │    name      │   │     │    description    │
└──────────────┘   └─────│ FK group_id  │   │     │    total_amount   │
                         └──────────────┘   └─────│ FK paid_by_member │
                                                  │ FK group_id       │
                                                  └─────────┬─────────┘
                                                            │
                                              ┌──────────────▼──────────────┐
                                              │    expense_participants     │
                                              ├─────────────────────────────┤
                                              │ FK expense_id               │
                                              │ FK member_id                │
                                              └─────────────────────────────┘
```

### Tables

| Table | Description |
|---|---|
| `groups` | Stores group name |
| `members` | Each member belongs to one group |
| `expenses` | Each expense has a payer, amount, description, and group |
| `expense_participants` | Join table — which members share a given expense |

### Settlement Mechanism
Settlements are stored as **special expenses** in the `expenses` table:
- `description` = `"Settlement: DebtorName → CreditorName"`
- `paid_by` = the debtor (they are now paying)
- `participants` = the creditor only
- `amount` = the settled amount

This correctly adjusts net balances without needing a separate settlements table — the debt algorithm recalculates balances fresh from all expenses on every page load.

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17 or higher |
| Maven | 3.6+ (or use the included `mvnw` wrapper) |

No database installation needed — SQLite is embedded.

### Installation & Run

```bash
# 1. Clone or unzip the project
cd Exlitter

# 2. Run with Maven wrapper (no Maven installation needed)
./mvnw spring-boot:run

# OR if Maven is installed globally
mvn spring-boot:run
```

```bash
# On Windows
mvnw.cmd spring-boot:run
```

### Access the App

Open your browser and go to:
```
http://localhost:8080
```

The SQLite database file `splitapp.db` will be created automatically in the project root on first run.

### Build a JAR (optional)

```bash
./mvnw clean package
java -jar target/splitapp-0.0.1-SNAPSHOT.jar
```

---

## 📖 How to Use

### Step 1 — Create a Group
1. Click **+ NEW GROUP** on the Groups page
2. Enter a group name (e.g. `Bali Trip 2025`)
3. Enter member names separated by commas (e.g. `Aqilah, Naz, Farah, Hafiz`)
4. Click **Create Group**

### Step 2 — Log Expenses
1. Click **SEE DETAILS** on any group → goes to the Expenses page
2. Click **+ADD EXPENSES**
3. Fill in:
   - **Expense Name** — what was bought
   - **Amount (RM)** — total cost
   - **Paid By** — who paid
   - **Split Among** — check specific members, or leave all unchecked to split equally among everyone
4. Click **Add Expense**

### Step 3 — Settle Up
1. Click the **Settle Up** tab from any group page
2. Each member card shows:
   - **Owes** — debts they need to pay, with an amount field and Settle button
   - **Should Receive** — amounts owed to them (shown as "Awaiting")
3. To settle a debt:
   - Leave the amount as-is for a **full settlement**, or type a smaller amount for a **partial settlement**
   - Click **✓ Settle** — the balance updates immediately

---

## 🔗 API Routes

All routes are server-side rendered (no REST/JSON responses).

| Method | URL | Description |
|---|---|---|
| `GET` | `/` | Redirects to `/groups` |
| `GET` | `/groups` | List all groups |
| `POST` | `/groups` | Create a new group |
| `POST` | `/groups/{id}/delete` | Delete a group |
| `GET` | `/groups/{id}/expenses` | List expenses for a group |
| `POST` | `/groups/{id}/expenses` | Add a new expense |
| `POST` | `/groups/{id}/expenses/{eid}/delete` | Delete an expense |
| `GET` | `/groups/{id}/settle` | View settle up dashboard |
| `POST` | `/groups/{id}/settle/partial` | Record a (partial) settlement |

---

## ⚠️ Known Limitations

- **No user authentication** — this is a single-user local app. There are no login accounts.
- **No real-time updates** — the page must be refreshed (or the Settle button submits a form) to see updated balances.
- **SQLite concurrency** — SQLite is not suited for high-concurrency production use. For a production deployment, swap the datasource to MySQL or PostgreSQL by updating `application.properties` and the `pom.xml` driver dependency.
- **Single split strategy** — only equal splitting is implemented. Percentage-based or exact-amount splits can be added by implementing the `SplitStrategy` interface.

---

## 👩‍💻 Developer Notes

- `spring.thymeleaf.cache=false` is set in `application.properties` for hot-reload during development. Set it to `true` for production.
- `spring.jpa.hibernate.ddl-auto=update` means the schema is created/updated automatically on startup. Never use `update` or `create` in production — use `validate` and manage schema with a migration tool like Flyway.
- The debt simplification algorithm in `DebtCalculatorServiceImpl.calculateDebts()` uses a **greedy two-pointer approach** on sorted creditor/debtor lists, reducing N debts to the theoretical minimum number of transactions.

---

*Built as part of a Software Engineering coursework project · Java Spring Boot · Thymeleaf · SQLite*
