const TOKEN_KEY = 'token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function isTokenExpired(token) {
  if (!token) {
    return true
  }

  try {
    const payloadPart = token.split('.')[1]

    if (!payloadPart) {
      return true
    }

    const normalizedPayload = payloadPart
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(Math.ceil(payloadPart.length / 4) * 4, '=')
    const payload = JSON.parse(atob(normalizedPayload))

    if (!payload.exp) {
      return false
    }

    return payload.exp * 1000 <= Date.now()
  } catch (error) {
    return true
  }
}

export function clearAuthState(api = null) {
  clearToken()

  if (api) {
    delete api.defaults.headers.common.Authorization
  }
}
