export const getAuthToken = (): string | null => {
  return localStorage.getItem('auth_token')
}

export const getCurrentUser = () => {
  const s = localStorage.getItem('current_user')
  return s ? JSON.parse(s) : null
}

export const setAuthData = (token: string, user: any) => {
  localStorage.setItem('auth_token', token)
  localStorage.setItem('current_user', JSON.stringify(user))
}

export const clearAuthData = () => {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('current_user')
}
