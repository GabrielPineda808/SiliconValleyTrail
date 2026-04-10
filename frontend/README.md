# Silicon Valley Trail Frontend

Minimal React + Vite client for the existing Spring Boot backend.

## Requirements

- Node.js 20+
- The backend running locally

## Setup

1. Install dependencies:

```bash
npm install
```

2. Create an environment file if you need a custom backend URL:

```bash
cp .env.example .env
```

By default the frontend uses `http://localhost:8080`.

## Run

```bash
npm run dev
```

## Build

```bash
npm run build
```

## Notes

- JWT is stored in `localStorage` under `svt_jwt`.
- Auth requests use `/auth/signup` and `/auth/login`.
- Game requests use the existing `/game/*` backend endpoints.
- Loading a saved game uses `GET /game/findGame`.
- Starting a new game uses `POST /game/start`.
- Replacing an active game uses `DELETE /game/delete` and then `POST /game/start`.
- Pending events are handled from either:
  - `pendingEvent` in action responses
  - `eventJson` in the saved game response
# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
