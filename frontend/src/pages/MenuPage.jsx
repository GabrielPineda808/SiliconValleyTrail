import { useState } from 'react'
import ErrorBanner from '../components/ErrorBanner'
import Modal from '../components/Modal'
import { getApiErrorMessage } from '../utils/errors'

function MenuPage({ onStartNewGame, onLoadGame, onReplaceGame, onLogout }) {
  const [errorMessage, setErrorMessage] = useState('')
  const [pendingAction, setPendingAction] = useState('')
  const [showConflictModal, setShowConflictModal] = useState(false)

  const runMenuAction = async (actionName, action) => {
    setPendingAction(actionName)
    setErrorMessage('')

    try {
      await action()
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'The requested action could not be completed.'))
    } finally {
      setPendingAction('')
    }
  }

  const handleStart = async () => {
    setPendingAction('start')
    setErrorMessage('')

    try {
      const result = await onStartNewGame()
      if (result?.conflict) {
        setShowConflictModal(true)
      }
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, 'Unable to start a new game.'))
    } finally {
      setPendingAction('')
    }
  }

  return (
    <section className="page menu-page">
      <div className="menu-card">
        <div className="page-intro">
          <span className="eyebrow">Command Center</span>
          <h1>Choose your next move</h1>
          <p className="muted-text">
            Start a fresh run, jump back into an active save, or sign out.
          </p>
        </div>

        <ErrorBanner message={errorMessage} title="Menu action failed" />

        <div className="menu-actions">
          <button className="primary-button" disabled={pendingAction !== ''} onClick={handleStart} type="button">
            {pendingAction === 'start' ? 'Starting...' : 'Start New Game'}
          </button>

          <button
            className="secondary-button"
            disabled={pendingAction !== ''}
            onClick={() => runMenuAction('load', onLoadGame)}
            type="button"
          >
            {pendingAction === 'load' ? 'Loading...' : 'Load Game'}
          </button>

          <button
            className="ghost-button"
            disabled={pendingAction !== ''}
            onClick={onLogout}
            type="button"
          >
            Logout
          </button>
        </div>
      </div>

      <Modal
        disableClose={pendingAction === 'replace' || pendingAction === 'continue'}
        onClose={() => setShowConflictModal(false)}
        open={showConflictModal}
        subtitle="You already have an in-progress game."
        title="Active game found"
      >
        <p className="modal-copy">
          You can continue your current save or delete it and start over from day one.
        </p>

        <div className="modal-actions">
          <button
            className="secondary-button"
            disabled={pendingAction !== ''}
            onClick={() =>
              runMenuAction('continue', async () => {
                setShowConflictModal(false)
                await onLoadGame()
              })
            }
            type="button"
          >
            {pendingAction === 'continue' ? 'Opening save...' : 'Continue Current Game'}
          </button>

          <button
            className="primary-button danger-button"
            disabled={pendingAction !== ''}
            onClick={() =>
              runMenuAction('replace', async () => {
                setShowConflictModal(false)
                await onReplaceGame()
              })
            }
            type="button"
          >
            {pendingAction === 'replace' ? 'Replacing...' : 'Delete and Start New'}
          </button>
        </div>
      </Modal>
    </section>
  )
}

export default MenuPage
