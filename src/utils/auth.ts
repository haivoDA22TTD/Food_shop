export interface AuthPayload {
  token: string
  userId: number
  username: string
  email: string
  role: string
}

export const normalizeAuthPayload = (
  raw: any,
  fallbackUsername = ''
): AuthPayload | null => {
  const token = raw?.token
  const userId = Number(raw?.userId ?? raw?.id)
  const username = raw?.username ?? fallbackUsername
  const email = raw?.email ?? ''
  const role = raw?.role ?? 'USER'

  if (!token || !Number.isFinite(userId) || !username) {
    return null
  }

  return {
    token,
    userId,
    username,
    email,
    role,
  }
}

export const extractErrorMessage = (err: any, fallback: string): string => {
  const responseData = err?.response?.data

  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData
  }

  if (typeof responseData?.message === 'string' && responseData.message.trim()) {
    return responseData.message
  }

  if (typeof err?.message === 'string' && err.message.trim()) {
    return err.message
  }

  return fallback
}
