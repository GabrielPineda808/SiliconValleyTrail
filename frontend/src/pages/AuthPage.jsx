import { useMemo, useState } from 'react'
import ErrorBanner from '../components/ErrorBanner'
import { getApiErrorMessage, toFieldErrorMap } from '../utils/errors'

function validateCredentials({ username, password }) {
  const errors = {}

  if (!username.trim()) {
    errors.username = 'Username is required'
  } else if (username.trim().length < 3 || username.trim().length > 15) {
    errors.username = 'Username must be between 3 and 15 characters'
  }

  if (!password) {
    errors.password = 'Password is required'
  } else if (password.length < 8 || password.length > 128) {
    errors.password = 'Password must be between 8 and 128 characters'
  }

  return errors
}

function AuthPage({ onLogin, onSignup }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ username: '', password: '' })
  const [fieldErrors, setFieldErrors] = useState({})
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const title = useMemo(
    () => (mode === 'login' ? 'Log in to continue the trail' : 'Create your founder account'),
    [mode],
  )

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    setFieldErrors((current) => ({ ...current, [name]: '' }))
    setErrorMessage('')
    setSuccessMessage('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const trimmedForm = {
      username: form.username.trim(),
      password: form.password,
    }

    const nextFieldErrors = validateCredentials(trimmedForm)
    if (Object.keys(nextFieldErrors).length > 0) {
      setFieldErrors(nextFieldErrors)
      setErrorMessage('Please fix the highlighted fields.')
      return
    }

    setIsSubmitting(true)
    setFieldErrors({})
    setErrorMessage('')
    setSuccessMessage('')

    try {
      if (mode === 'login') {
        await onLogin(trimmedForm)
      } else {
        await onSignup(trimmedForm)
        setMode('login')
        setForm({ username: trimmedForm.username, password: '' })
        setSuccessMessage('Account created. Log in with your new credentials.')
      }
    } catch (error) {
      const backendFieldErrors = toFieldErrorMap(error?.data?.fieldErrors)
      setFieldErrors(backendFieldErrors)
      setErrorMessage(
        getApiErrorMessage(
          error,
          mode === 'login'
            ? 'Unable to log in with those credentials.'
            : 'Unable to create your account.',
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="page auth-page">
      <div className="auth-card">
        <div className="page-intro">
          <span className="eyebrow">Silicon Valley Trail</span>
          <h1>{title}</h1>
          <p className="muted-text">
            Sign up or log in to manage your startup run from the browser.
          </p>
        </div>

        <div className="segmented-control" role="tablist" aria-label="Authentication mode">
          <button
            className={mode === 'login' ? 'is-active' : ''}
            onClick={() => {
              setMode('login')
              setErrorMessage('')
              setSuccessMessage('')
              setFieldErrors({})
            }}
            type="button"
          >
            Log In
          </button>
          <button
            className={mode === 'signup' ? 'is-active' : ''}
            onClick={() => {
              setMode('signup')
              setErrorMessage('')
              setSuccessMessage('')
              setFieldErrors({})
            }}
            type="button"
          >
            Sign Up
          </button>
        </div>

        <ErrorBanner
          message={errorMessage}
          title={mode === 'login' ? 'Login failed' : 'Signup failed'}
        />

        {successMessage ? <div className="success-banner">{successMessage}</div> : null}

        <form className="form-grid" onSubmit={handleSubmit}>
          <label className="field">
            <span>Username</span>
            <input
              autoComplete="username"
              name="username"
              onChange={handleChange}
              placeholder="test"
              value={form.username}
            />
            {fieldErrors.username ? <small>{fieldErrors.username}</small> : null}
          </label>

          <label className="field">
            <span>Password</span>
            <input
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              name="password"
              onChange={handleChange}
              placeholder="password123"
              type="password"
              value={form.password}
            />
            {fieldErrors.password ? <small>{fieldErrors.password}</small> : null}
          </label>

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting
              ? mode === 'login'
                ? 'Logging in...'
                : 'Creating account...'
              : mode === 'login'
                ? 'Log In'
                : 'Create Account'}
          </button>
        </form>
      </div>
    </section>
  )
}

export default AuthPage
