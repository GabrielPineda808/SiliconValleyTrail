# Silicon Valley Trail

A turn-based backend game inspired by **The Oregon Trail**, reimagined as a startup road trip from **San Jose to San Francisco**. The player manages a scrappy startup team across a sequence of real Bay Area locations, balancing resources, handling semi-random events, and resolving API-driven travel surprises.

This repository currently contains a **Spring Boot 4 REST API** implementation. The main application lives in the `game/` directory.

## Demo

- Repo: `https://github.com/GabrielPineda808/SiliconValleyTrail`
- Recording / deployed URL: **Coming Soon**

## What the game does

- Supports **JWT-based signup and login**
- Starts and loads a single **in-progress** game per user
- Tracks multiple game resources:
    - `gas`
    - `cash`
    - `bugs`
    - `coffee`
    - `motivation`
- Progresses across **11 real locations** from San Jose to San Francisco
- Lets the player choose one action per turn:
    - `TRAVEL`
    - `FIX_BUGS`
    - `FREELANCE`
    - `REST`
    - `BUY_SUPPLIES`
    - `PITCH_VC`
- Triggers an **arrival event after travel**
- Blocks normal actions while an event choice is pending
- Marks the game as **WON** when the player reaches the final destination
- Marks the game as **LOST** when key resources collapse

## API-driven gameplay

This project integrates public APIs that affect gameplay rather than only displaying data:

- **Open-Meteo**: weather can influence the `WEATHER_DELAY` event
- **OpenSky Network**: nearby flight activity can influence the `FLIGHT_DROP` event

The game also includes **mock/fallback clients** so it can continue running when external APIs fail or are unavailable.

## Tech stack

- Java 25
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Lombok
- SpringDoc / Swagger UI
- Maven Wrapper

## Project structure

```text
    -backend
      - controller/ – REST endpoints for auth and game actions
      - service/ – business logic and coordination between layers
      - repository/ – database access through Spring Data JPA
      - entity/ – persisted domain models such as User and GameState
      - dto/ – request and response objects used by the API
      - security/ – JWT authentication, filters, and token handling
      - gameLogic/ – turn processing, actions, events, and travel logic
      - exceptions/ – custom exceptions and centralized error handling
      - enums/ – shared enums like ActionType and GameStatus
      - config/ – Spring and application configuration
      - resources/ – app config and database-related files
      - test/ – unit and integration tests
    -frontend
      - src/    - app source code
      - api/     - backend requests
      - components/  - reusable UI pieces
      - pages/        - main screens
      - hooks/        - custom React hooks
      - context/      - shared auth state
      - utils/        - helper functions
      - App.jsx       - root app component
      - main.jsx      - app entry point
      - index.css     - global styles
      - package.json  - project scripts
      - .env.example  - sample env values
      - README.md     - frontend notes
```

## Quick start

### 1. Prerequisites

Install the following on a fresh machine:

- **Java 25**
- **PostgreSQL**
- **Git**

No local Maven installation is required because the repo includes the Maven Wrapper.

### 2. Clone the repository

```bash
git clone https://github.com/GabrielPineda808/SiliconValleyTrail.git
cd SiliconValleyTrail/game
```

### 3. Create a PostgreSQL database

Create a database and user locally, then plug those values into your environment variables.

Example:

```sql
CREATE DATABASE silicon;
```

### 4. Create a backend `.env`

The backend imports environment variables from a local `.env` file and uses Spring profiles to choose the database connection.

Create `backend/.env` with values like these:

```env
SPRING_PROFILES_ACTIVE=local
LOCAL_DB_URL=jdbc:postgresql://localhost:5432/silicon
LOCAL_DB_USERNAME=postgres
LOCAL_DB_PASSWORD=your_password_here
SUPABASE_DB_URL=jdbc:postgresql://db.your-project-ref.supabase.co:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=postgres
SUPABASE_DB_PASSWORD=your_supabase_password_here
JWT_SECRET_KEY=replace_with_a_long_random_secret
JWT_EXP_TIME=3600000
```

Use `local` for a local PostgreSQL database and switch `SPRING_PROFILES_ACTIVE` to `prod` when you want the backend to use the Supabase connection values instead.

### 5. Run the application

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will start on:

```text
http://localhost:8080
```

## Running with mocks / API fallback behavior

This project is designed so the game still runs even when public APIs are unavailable.

Current behavior:

- `MockWeatherClient` calls the live Open-Meteo client first and falls back to a random chance when the call fails.
- `MockFlightClient` calls the live OpenSky client first and falls back to a random chance when the call fails.

That means **no API key is required** for normal local gameplay.

## Frontend

The project also includes a React + Vite frontend in `frontend/`.

For frontend setup details, environment configuration, and additional notes, see `frontend/README.md`.

Minimal startup:

```bash
cd frontend
npm install
npm run dev
```

## Authentication flow

### Sign up

```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "Test123!"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "Test123!"
  }'
```

Save the returned JWT and use it as:

```text
Authorization: Bearer <token>
```

## Example game API usage

### Start a new game

```bash
curl -X POST http://localhost:8080/game/start \
  -H "Authorization: Bearer <token>"
```

### Load the current in-progress game

```bash
curl -X GET http://localhost:8080/game/findGame \
  -H "Authorization: Bearer <token>"
```

### Perform an action

```bash
curl -X POST http://localhost:8080/game/action \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "TRAVEL"
  }'
```

Other valid actions:

- `FIX_BUGS`
- `FREELANCE`
- `REST`
- `BUY_SUPPLIES`
- `PITCH_VC`

### Resolve a pending event choice

If the action response includes a pending event, resolve it with:

```bash
curl -X POST http://localhost:8080/game/event/choice \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "choice": "ACCEPT"
  }'
```

The valid `choice` values depend on the event returned by the backend.

### Delete the active game

```bash
curl -X DELETE http://localhost:8080/game/delete \
  -H "Authorization: Bearer <token>"
```

## OpenAPI / Swagger

The project includes SpringDoc for interactive API documentation. If enabled in your local runtime, use the generated OpenAPI/Swagger UI to inspect endpoints and payloads.

## Tests

Run tests with:

```bash
./mvnw test
```

or on Windows:

```bash
mvnw.cmd test
```

## Dependency overview

Key dependencies in `pom.xml`:

- `spring-boot-starter-webmvc`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-restclient`
- `spring-boot-starter-flyway`
- `springdoc-openapi-starter-webmvc-ui`
- `postgresql`
- `h2`
- `jjwt-*`
- `lombok`

## Architecture overview

### High-level flow

1. User signs up or logs in.
2. User starts a game.
3. Each turn the user selects one action.
4. The engine validates the action against current state.
5. The selected action mutates resources and advances the turn.
6. If the action is `TRAVEL`, the engine may trigger an arrival event.
7. While an event is pending, the player must resolve it before taking another action.
8. The win/loss service evaluates whether the run should remain in progress, be won, or be lost.

### Main components

- **Controllers**: authentication and game endpoints
- **Services**: orchestration for auth, persistence, and event resolution
- **Game engine**: turn execution, passive rule handling, event triggering, win/loss evaluation
- **Actions**: one class per player action
- **Events**: one class per event type plus API-backed clients
- **Repositories**: persistence via JPA
- **Security**: JWT auth for protected endpoints

## Design notes
#### A design doc is available in 'SiliconValleyTrail/DESIGN_DOC.md'

### Game loop and balance

The game is designed as a turn-based resource management loop:

- travel progresses the route but spends resources
- non-travel actions let the player recover or optimize one part of the state at the cost of another
- travel can trigger semi-random events, creating short risk/reward decision moments

The key balancing idea is that **no action is purely free**:

- fixing bugs costs time and/or other resources
- freelancing adds cash but can create other pressure
- travel moves the player toward victory but can trigger events and resource drain

### Why these APIs

I chose weather and flight data because both can affect gameplay in intuitive ways:

- **weather** naturally affects travel risk and delay
- **flight activity** naturally enables supply-drop style events

Both APIs change the actual game state instead of only decorating the UI.

### Data modeling

The main persisted entity is `GameState`, which stores:

- user 
- resources
- current location index and name
- day count
- status (`IN_PROGRESS`, `WON`, `LOST`)
- coffee-zero streak for one of the loss conditions
- pending event state, including serialized event payload JSON

Pending events are stored so that the player can leave and resume without losing the current decision context.

### Error handling and resilience

- JWT protects gameplay endpoints
- invalid actions raise domain-specific exceptions
- event resolution is blocked unless an event is actually pending
- live API failures fall back to mock/randomized behavior so local development does not depend on external uptime

### If I had more time

- improve API caching and rate-limit handling
- formalize profiles for `mock`, `live`, and `test`
