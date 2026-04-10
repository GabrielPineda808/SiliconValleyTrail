import { useCallback, useMemo, useState } from 'react'
import { loginUser, signupUser } from './api/auth'
import {
  createGame,
  deleteActiveGame,
  getCurrentGame,
  performGameAction,
  resolvePendingEvent,
} from './api/game'
import { useAuth } from './hooks/useAuth'
import AuthPage from './pages/AuthPage'
import GamePage from './pages/GamePage'
import MenuPage from './pages/MenuPage'
import { ApiError } from './api/client'
import { extractOutcome, normalizeGameState } from './utils/game'

function App() {
  const { token, setToken, logout, isAuthenticated } = useAuth()
  const [game, setGame] = useState(null)
  const [latestOutcome, setLatestOutcome] = useState(null)

  const withSessionHandling = useCallback(
    (error) => {
      if (error?.status === 401) {
        logout()
        setGame(null)
        setLatestOutcome(null)
        throw new ApiError('Your session expired. Please log in again.', {
          status: 401,
          data: error.data,
        })
      }

      throw error
    },
    [logout],
  )

  const handleLogin = useCallback(
    async (credentials) => {
      try {
        const response = await loginUser(credentials)
        setToken(response.token)
        setGame(null)
        setLatestOutcome(null)
      } catch (error) {
        withSessionHandling(error)
      }
    },
    [setToken, withSessionHandling],
  )

  const handleSignup = useCallback(async (payload) => {
    await signupUser(payload)
  }, [])

  const handleLogout = useCallback(() => {
    logout()
    setGame(null)
    setLatestOutcome(null)
  }, [logout])

  const openGame = useCallback((payload, fallbackMessage = '') => {
    const normalizedGame = normalizeGameState(payload)
    setGame(normalizedGame)
    setLatestOutcome(extractOutcome(payload, fallbackMessage))
  }, [])

  const handleStartNewGame = useCallback(async () => {
    try {
      const response = await createGame(token)
      openGame(response, 'Started a new game.')
      return { conflict: false }
    } catch (error) {
      if (error?.status === 409 || error?.data?.error === 'GAME_ALREADY_EXISTS') {
        return { conflict: true }
      }

      withSessionHandling(error)
    }
  }, [openGame, token, withSessionHandling])

  const handleLoadGame = useCallback(async () => {
    try {
      const response = await getCurrentGame(token)
      openGame(response, 'Loaded your active game.')
    } catch (error) {
      withSessionHandling(error)
    }
  }, [openGame, token, withSessionHandling])

  const handleReplaceGame = useCallback(async () => {
    try {
      await deleteActiveGame(token)
      const response = await createGame(token)
      openGame(response, 'Deleted the previous game and started a new one.')
    } catch (error) {
      withSessionHandling(error)
    }
  }, [openGame, token, withSessionHandling])

  const handleQuitToMenu = useCallback(() => {
    setGame(null)
  }, [])

  const handlePerformAction = useCallback(
    async (action) => {
      try {
        const response = await performGameAction(token, action)
        openGame(response)
      } catch (error) {
        withSessionHandling(error)
      }
    },
    [openGame, token, withSessionHandling],
  )

  const handleResolveEvent = useCallback(
    async (choice) => {
      try {
        const response = await resolvePendingEvent(token, choice)
        openGame(response)
      } catch (error) {
        withSessionHandling(error)
      }
    },
    [openGame, token, withSessionHandling],
  )

  const currentView = useMemo(() => {
    if (!isAuthenticated) {
      return 'auth'
    }

    if (game) {
      return 'game'
    }

    return 'menu'
  }, [game, isAuthenticated])

  return (
    <div className="app-shell">
      <div className="app-frame">
        {currentView === 'auth' ? (
          <AuthPage onLogin={handleLogin} onSignup={handleSignup} />
        ) : null}

        {currentView === 'menu' ? (
          <MenuPage
            onLoadGame={handleLoadGame}
            onLogout={handleLogout}
            onReplaceGame={handleReplaceGame}
            onStartNewGame={handleStartNewGame}
          />
        ) : null}

        {currentView === 'game' ? (
          <GamePage
            game={game}
            latestOutcome={latestOutcome}
            onLogout={handleLogout}
            onPerformAction={handlePerformAction}
            onQuit={handleQuitToMenu}
            onResolveEvent={handleResolveEvent}
          />
        ) : null}
      </div>
    </div>
  )
}

export default App
