import { apiRequest } from './client'

export function signupUser(payload) {
  return apiRequest('/auth/signup', {
    method: 'POST',
    body: payload,
  })
}

export function loginUser(payload) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: payload,
  })
}
