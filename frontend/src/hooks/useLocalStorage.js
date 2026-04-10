import { useCallback, useState } from 'react'

function readValue(key, initialValue) {
  try {
    const stored = window.localStorage.getItem(key)
    return stored ?? initialValue
  } catch {
    return initialValue
  }
}

export function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => readValue(key, initialValue))

  const updateValue = useCallback(
    (nextValue) => {
      setValue((currentValue) => {
        const resolvedValue =
          typeof nextValue === 'function' ? nextValue(currentValue) : nextValue

        try {
          if (resolvedValue === '' || resolvedValue == null) {
            window.localStorage.removeItem(key)
          } else {
            window.localStorage.setItem(key, resolvedValue)
          }
        } catch {
          // Ignore storage access errors and keep in-memory state.
        }

        return resolvedValue
      })
    },
    [key],
  )

  return [value, updateValue]
}
