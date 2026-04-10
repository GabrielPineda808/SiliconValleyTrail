export function getApiErrorMessage(error, fallbackMessage) {
  if (error?.status === 401) {
    return 'Your session expired. Please log in again.'
  }

  if (typeof error?.data === 'string' && error.data) {
    return error.data
  }

  if (error?.data?.message) {
    return error.data.message
  }

  if (error?.message) {
    return error.message
  }

  return fallbackMessage
}

export function toFieldErrorMap(fieldErrors = []) {
  return fieldErrors.reduce((accumulator, item) => {
    if (!accumulator[item.field]) {
      accumulator[item.field] = item.message
    }

    return accumulator
  }, {})
}
