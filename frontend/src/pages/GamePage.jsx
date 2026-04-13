import { useEffect, useMemo, useState } from 'react'
import ErrorBanner from '../components/ErrorBanner'
import Modal from '../components/Modal'
import StatCard from '../components/StatCard'
import { getApiErrorMessage } from '../utils/errors'
import { ACTION_OPTIONS, isGameOver } from '../utils/game'

function GamePage({ game, latestOutcome, onPerformAction, onResolveEvent, onQuit, onLogout }) {
  const [actionError, setActionError] = useState('')
  const [actionLoading, setActionLoading] = useState('')
  const [eventError, setEventError] = useState('')
  const [resolvingChoice, setResolvingChoice] = useState('')

  const gameFinished = isGameOver(game.status)
  const pendingEvent = game.pendingEvent
  const [displayedEvent, setDisplayedEvent] = useState(pendingEvent)

  useEffect(() => {
    if (pendingEvent) {
      setDisplayedEvent(pendingEvent)
      return
    }

    if (!resolvingChoice) {
      setDisplayedEvent(null)
    }
  }, [pendingEvent, resolvingChoice])

  const statusCopy = useMemo(() => {
    if (game.status === 'WON') {
      return 'You won the startup race.'
    }

    if (game.status === 'LOST') {
      return 'This run is over. Return to the menu to start again.'
    }

    return 'The run is still in progress.'
  }, [game.status])

  const handleAction = async (action) => {
    setActionLoading(action)
    setActionError('')

    try {
      await onPerformAction(action)
    } catch (error) {
      setActionError(getApiErrorMessage(error, 'The action could not be completed.'))
    } finally {
      setActionLoading('')
    }
  }

  const handleResolve = async (choice) => {
    setResolvingChoice(choice)
    setEventError('')

    try {
      await onResolveEvent(choice)
    } catch (error) {
      setEventError(getApiErrorMessage(error, 'The event choice could not be applied.'))
    } finally {
      setResolvingChoice('')
    }
  }

  return (
    <section className="page game-page">
      <div className="game-layout">
        <div className="panel-card">
          <div className="panel-header">
            <div>
              <span className="eyebrow">Active Game</span>
              <h1>{game.locationName}</h1>
              <p className="muted-text">
                Day {game.day} • Status: <strong>{game.status}</strong>
              </p>
            </div>

            <div className="header-actions">
              <button className="secondary-button" onClick={onQuit} type="button">
                Quit to Menu
              </button>
              <button className="ghost-button" onClick={onLogout} type="button">
                Logout
              </button>
            </div>
          </div>

          <div className={`status-banner ${gameFinished ? 'is-finished' : ''}`}>
            {statusCopy}
          </div>

          <div className="stats-grid">
            <StatCard label="Gas" value={game.gas} />
            <StatCard label="Cash" value={game.cash} />
            <StatCard label="Bugs" value={game.bugs} />
            <StatCard label="Coffee" value={game.coffee} />
            <StatCard label="Motivation" value={game.motivation} />
            <StatCard label="Location Index" value={game.locationIndex} />
          </div>
        </div>

        <div className="panel-card">
          <div className="section-heading">
            <h2>Latest Result</h2>
            <p className="muted-text">Most recent backend response for this run.</p>
          </div>

          {latestOutcome?.message ? (
            <div className="result-card">
              <strong>{latestOutcome.message}</strong>
              {latestOutcome.effects?.length ? (
                <ul className="effect-list">
                  {latestOutcome.effects.map((effect) => (
                    <li key={effect}>{effect}</li>
                  ))}
                </ul>
              ) : (
                <p className="muted-text">No additional effects were returned.</p>
              )}
            </div>
          ) : (
            <p className="muted-text">No actions taken yet in this session.</p>
          )}
        </div>

        <div className="panel-card">
          <div className="section-heading">
            <h2>Available Actions</h2>
            <p className="muted-text">
              Actions are disabled while an event is pending or after the run ends.
            </p>
          </div>

          <ErrorBanner message={actionError} title="Action failed" />

          <div className="action-grid">
            {ACTION_OPTIONS.map((action) => (
              <button
                key={action.value}
                className="action-card"
                disabled={Boolean(pendingEvent) || gameFinished || actionLoading !== ''}
                onClick={() => handleAction(action.value)}
                type="button"
              >
                <strong>
                  {actionLoading === action.value ? `Running ${action.label}...` : action.label}
                </strong>
                <span>{action.description}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      <Modal
        disableClose
        open={Boolean(displayedEvent)}
        subtitle={displayedEvent?.type ?? 'Pending event'}
        title={displayedEvent?.title ?? 'Resolve current event'}
      >
        <p className="modal-copy">{displayedEvent?.description}</p>

        <ErrorBanner message={eventError} title="Event resolution failed" />

        <div className="event-choice-list">
          {displayedEvent?.choices?.map((choice) => (
            <button
              key={choice.id}
              className="action-card"
              disabled={resolvingChoice !== ''}
              onClick={() => handleResolve(choice.value)}
              type="button"
            >
              <strong>
                {resolvingChoice === choice.value ? 'Resolving...' : choice.label}
              </strong>
              <span>{choice.description || `Submit ${choice.value}`}</span>
            </button>
          ))}
        </div>

        <div className="modal-actions">
          <button className="ghost-button" disabled={resolvingChoice !== ''} onClick={onQuit} type="button">
            Quit Game
          </button>
        </div>
      </Modal>
    </section>
  )
}

export default GamePage
