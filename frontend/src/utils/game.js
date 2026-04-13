export const ACTION_OPTIONS = [
  {
    value: 'TRAVEL',
    label: 'Travel',
    description: 'Move to the next city and possibly trigger an arrival event.',
  },
  {
    value: 'FIX_BUGS',
    label: 'Fix Bugs',
    description: 'Reduce bugs and stabilize the product.',
  },
  {
    value: 'FREELANCE',
    label: 'Freelance',
    description: 'Trade time for cash to keep the startup alive.',
  },
  {
    value: 'REST',
    label: 'Rest',
    description: 'Recover motivation and take a breather.',
  },
  {
    value: 'BUY_SUPPLIES',
    label: 'Buy Supplies',
    description: 'Spend cash to replenish key resources.',
  },
  {
    value: 'PITCH_VC',
    label: 'Pitch VC',
    description: 'Try to raise money by pitching investors.',
  },
]

function normalizePendingEventChoice(choice, index) {
  const value = choice.choice ?? choice.optionType ?? choice.value ?? null

  return {
    id: choice.id ?? `${String(value ?? 'choice')}-${index}`,
    label: choice.label ?? choice.code ?? String(value ?? `Choice ${index + 1}`),
    value,
    description: choice.description ?? '',
  }
}

export function normalizePendingEvent(event) {
  if (!event) {
    return null
  }

  return {
    type: event.type ?? 'UNKNOWN_EVENT',
    title: event.title ?? 'Pending event',
    description:
      event.description ?? 'Resolve the current event before taking another action.',
    choices: Array.isArray(event.choices)
      ? event.choices.map(normalizePendingEventChoice).filter((choice) => choice.value !== null)
      : [],
  }
}

export function parsePendingEventJson(eventJson) {
  if (!eventJson) {
    return null
  }

  try {
    return normalizePendingEvent(JSON.parse(eventJson))
  } catch {
    return {
      type: 'UNKNOWN_EVENT',
      title: 'Pending event',
      description: 'A pending event exists, but its details could not be parsed.',
      choices: [],
    }
  }
}

export function normalizeGameState(payload) {
  if (!payload) {
    return null
  }

  return {
    gameId: payload.gameId,
    gas: payload.gas,
    cash: payload.cash,
    bugs: payload.bugs,
    coffee: payload.coffee,
    motivation: payload.motivation,
    locationIndex: payload.locationIndex,
    locationName: payload.locationName,
    day: payload.day,
    status: payload.status,
    eventJson: payload.eventJson ?? null,
    pendingEvent:
      normalizePendingEvent(payload.pendingEvent) ?? parsePendingEventJson(payload.eventJson),
  }
}

export function extractOutcome(payload, fallbackMessage = '') {
  if (!payload) {
    return null
  }

  if (payload.message || Array.isArray(payload.effects)) {
    return {
      message: payload.message ?? fallbackMessage,
      effects: Array.isArray(payload.effects) ? payload.effects : [],
    }
  }

  if (!fallbackMessage) {
    return null
  }

  return {
    message: fallbackMessage,
    effects: [],
  }
}

export function isGameOver(status) {
  return status === 'WON' || status === 'LOST'
}
