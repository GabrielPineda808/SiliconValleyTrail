function ErrorBanner({ message, title = 'Something went wrong' }) {
  if (!message) {
    return null
  }

  return (
    <div className="error-banner" role="alert">
      <strong>{title}</strong>
      <span>{message}</span>
    </div>
  )
}

export default ErrorBanner
