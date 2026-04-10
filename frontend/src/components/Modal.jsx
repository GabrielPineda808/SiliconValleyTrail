function Modal({ open, title, subtitle, children, onClose, disableClose = false }) {
  if (!open) {
    return null
  }

  return (
    <div className="modal-backdrop" role="presentation" onClick={disableClose ? undefined : onClose}>
      <div
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2 id="modal-title">{title}</h2>
            {subtitle ? <p className="muted-text">{subtitle}</p> : null}
          </div>

          {!disableClose && onClose ? (
            <button className="icon-button" onClick={onClose} type="button">
              Close
            </button>
          ) : null}
        </div>

        <div className="modal-body">{children}</div>
      </div>
    </div>
  )
}

export default Modal
