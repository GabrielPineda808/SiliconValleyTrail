import { apiRequest } from './client'

export function createGame(token) {
  return apiRequest('/game/start', {
    method: 'POST',
    token,
  })
}

export function getCurrentGame(token) {
  return apiRequest('/game/findGame', {
    method: 'GET',
    token,
  })
}

export function deleteActiveGame(token) {
  return apiRequest('/game/delete', {
    method: 'DELETE',
    token,
  })
}

export function performGameAction(token, action) {
  return apiRequest('/game/action', {
    method: 'POST',
    token,
    body: { action },
  })
}

export function resolvePendingEvent(token, choice) {
  return apiRequest('/game/event/choice', {
    method: 'POST',
    token,
    body: { choice },
  })
}
