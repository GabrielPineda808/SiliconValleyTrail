# Silicon Valley Trail - Design Notes

## 1. Game loop and balance approach

Silicon Valley Trail is built as a turn-based API-driven strategy game.

Each turn follows this sequence:

1. Load the current in-progress game.
2. Validate that the game is still active.
3. Ensure there is no unresolved pending event.
4. Apply a selected player action.
5. Update day count and ongoing state.
6. Evaluate win/loss conditions.
7. If the action was travel and the game is still active, generate an arrival event.
8. Persist the resulting game state.

### Balance philosophy

The balance model is intentionally simple:

- **Travel** is the primary progress action and the main way to win.
- **Non-travel actions** let the player trade one resource for another.
- **Events** create short, consequential decision points that interrupt the normal turn loop.

The goal is to make every action legible:

- travel moves the player forward but exposes them to uncertainty
- fixing bugs reduces long-term loss pressure
- rest helps stabilize morale/coffee pressure
- freelancing and pitching VCs support the money side of the system
- buying supplies supports future survival but consumes current cash

## 2. Why these APIs and how they affect gameplay

### Open-Meteo

Weather was chosen because it maps cleanly onto travel. Rain, poor conditions, or high precipitation probability can justify travel delays in a way players immediately understand.

Gameplay effect:

- influences whether a `WEATHER_DELAY` event can trigger
- changes the strategic cost of choosing travel on a given leg

### OpenSky Network

Flight activity was chosen because it supports a playful “supply drop” mechanic that fits the tone of the project while still being rooted in real external data.

Gameplay effect:

- influences whether a `FLIGHT_DROP` event can trigger
- can reward the player for being in the right place at the right time

### Fallback strategy

The game is still playable when APIs fail.

Both the weather and flight integrations are wrapped by mock/fallback clients that attempt the live call first and then degrade gracefully to randomized behavior. This keeps the game testable and runnable without requiring perfect network conditions.

## 3. Data modeling

### Core entity: `GameState`

The main persisted state stores:

- current user
- gas
- cash
- bugs
- coffee
- motivation
- location index
- location name
- current day
- overall status
- coffee zero streak
- pending event type
- pending event JSON payload
- whether an event is pending

### Route model

The route is modeled as a fixed registry of real locations from San Jose to San Francisco. This keeps routing deterministic while still grounding the game in real-world geography.

## 4. Event architecture

The event model is intentionally separate from ordinary actions.

### Normal turn actions

- `TRAVEL`
- `FIX_BUGS`
- `FREELANCE`
- `REST`
- `BUY_SUPPLIES`
- `PITCH_VC`

### Pending event lifecycle

1. Travel succeeds.
2. The game engine asks the event service to trigger an arrival event.
3. A pending event is persisted into the game state.
4. The API returns both the normal action result and the event payload.
5. The client must call the event-choice endpoint to resolve it.
6. While the event is pending, normal actions are blocked.

This gives the backend the authoritative control over the game state machine rather than the client.

## 5. Persistence model and tradeoffs

### Current approach

The project treats gameplay as one active run per user.

Only the `IN_PROGRESS` game is used for loading/resuming. Finished runs are treated differently from active state, which simplifies client behavior.

### Tradeoff

This is simpler than supporting multiple save slots or run history, but it is less flexible. If the project grows, a future version could support:

- multiple save slots
- archived finished runs
- leaderboards or analytics

## 6. Win and loss design

### Win condition

The player wins by reaching the final location in the registry.

### Loss conditions

The player loses if any of the following collapse:

- gas falls to zero or below
- cash falls to zero or below
- bugs reach a critical threshold
- motivation falls to zero or below
- the team runs out of coffee for too many turns in a row

This set of rules creates both immediate failure pressure and slower-burn failure pressure.

## 7. Error handling and resilience

### Domain-level errors

The code distinguishes game-domain failures from infrastructure problems:

- game already exists
- game not found
- invalid action
- no active game
- invalid pending event choice

### External API failures

API failures are handled as non-fatal gameplay issues:

- if live weather fails, fallback logic still returns a usable answer
- if live flight lookup fails, fallback logic still returns a usable answer

That design avoids making public API uptime a hard dependency for local development.

## 8. Security and API design

The backend uses JWT-based authentication so that each user’s game state is isolated behind authenticated endpoints.

The API surface is intentionally small:

- auth endpoints for signup/login
- game endpoints for start/load/delete/action
- event-choice endpoint for resolving pending events

This makes the core loop easy to test through curl, Swagger, or Postman.

## 9. Tradeoffs and what I would improve next

### Tradeoffs made

- prioritized backend correctness over UI polish
- used serialized event payloads for simplicity
- kept the route fixed rather than implementing dynamic routing
- used mock-capable API clients instead of requiring secrets or heavy infra

### With more time, I would:

- improve observability and logging around event generation
- add API response caching and rate-limit-aware retry behavior
- add richer balancing passes based on playtesting

## 11. AI usage note

## AI Usage

I used AI in a supporting, advisory role throughout development rather than as the primary author of the project. The game concept, core game loop, data model, security approach, persistence flow, and backend architecture were my own design decisions. I mainly used AI to validate ideas, pressure-test decisions, and help bring more structure and clarity to my implementation as the project grew.

Where AI was most helpful was in narrower areas such as brainstorming event ideas, suggesting route/location options, thinking through balancing decisions, and helping me articulate higher-level tradeoffs more clearly. I also used it to help draft parts of the test suite and documentation, but I reviewed and verified those outputs before including them.

I deliberately kept AI in a limited role because I want to continue strengthening my own ability to reason through problems, design systems, and build a deeper understanding of the choices I make.
