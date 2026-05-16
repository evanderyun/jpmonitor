import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Must use vi.hoisted() because vi.mock() is hoisted to the top of the file
const { mockToken, clearAuthData, getAuthToken } = vi.hoisted(() => ({
  mockToken: 'test-jwt-token',
  clearAuthData: vi.fn(),
  getAuthToken: vi.fn(() => 'test-jwt-token'),
}))

vi.mock('../services/authStorage', () => ({
  getAuthToken: getAuthToken,
  clearAuthData: clearAuthData,
}))

vi.mock('../config', () => ({
  API_BASE_URL: '/api',
}))

import { fetchJson } from '../lib/http'

describe('fetchJson', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getAuthToken.mockReturnValue(mockToken)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('constructs correct headers with auth token', async () => {
    const mockResponse = { data: 'test' }
    global.fetch = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      json: () => Promise.resolve(mockResponse),
    })

    const result = await fetchJson<{ data: string }>('/test-endpoint')

    expect(result).toEqual(mockResponse)
    expect(global.fetch).toHaveBeenCalledWith(
      '/api/test-endpoint',
      expect.objectContaining({
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
          Authorization: `Bearer ${mockToken}`,
        }),
      })
    )
  })

  it('does not add auth header when auth option is false', async () => {
    const mockResponse = { data: 'test' }
    global.fetch = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      json: () => Promise.resolve(mockResponse),
    })

    const result = await fetchJson<{ data: string }>('/test-endpoint', { auth: false })

    expect(result).toEqual(mockResponse)
    const callArgs = vi.mocked(global.fetch).mock.calls[0]
    const headers = callArgs[1]?.headers as Record<string, string>
    expect(headers['Authorization']).toBeUndefined()
    expect(headers['Content-Type']).toBe('application/json')
  })

  it('handles 401 by clearing auth data and redirecting to login', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 401,
      ok: false,
      json: () => Promise.resolve({ error: 'Unauthorized' }),
    })

    // Mock window.location
    delete (window as any).location
    window.location = { href: '' } as any

    let caught: Error | null = null
    try {
      await fetchJson('/test-endpoint')
    } catch (e: any) {
      caught = e
    }

    expect(caught).not.toBeNull()
    expect(caught!.message).toBe('Unauthorized')
    expect(clearAuthData).toHaveBeenCalledTimes(1)
    expect(window.location.href).toBe('/login')
  })

  it('handles 403 by clearing auth data', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 403,
      ok: false,
      json: () => Promise.resolve({ error: 'Forbidden' }),
    })

    delete (window as any).location
    window.location = { href: '' } as any

    let caught: Error | null = null
    try {
      await fetchJson('/test-endpoint')
    } catch (e: any) {
      caught = e
    }

    expect(caught).not.toBeNull()
    expect(clearAuthData).toHaveBeenCalledTimes(1)
    expect(window.location.href).toBe('/login')
  })

  it('aborts previous GET requests to the same URL', async () => {
    // Mock AbortController
    const OriginalAbortController = global.AbortController
    let abortCount = 0
    global.AbortController = class extends OriginalAbortController {
      constructor() {
        super()
        abortCount++
      }
    } as any

    const fn = global.fetch as any
    global.fetch = vi.fn().mockImplementation((_url: string, options: any) => {
      return new Promise((resolve) => {
        options.signal?.addEventListener('abort', () => {
          resolve({
            status: 0,
            ok: false,
            json: () => Promise.reject(new Error('Aborted')),
          })
        })
      })
    })

    fetchJson('/same-url') // First call - no await
    const secondResponse = { data: 'second' }
    global.fetch = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      json: () => Promise.resolve(secondResponse),
    })
    const result = await fetchJson<{ data: string }>('/same-url')

    expect(result).toEqual(secondResponse)
    expect(abortCount).toBeGreaterThanOrEqual(1)
    // Restore
    global.AbortController = OriginalAbortController
  })

  it('throws parsed error message on non-OK response', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 400,
      ok: false,
      json: () => Promise.resolve({ error: 'Bad request: invalid data' }),
    })

    let caught: Error | null = null
    try {
      await fetchJson('/test-endpoint')
    } catch (e: any) {
      caught = e
    }

    expect(caught).not.toBeNull()
    expect(caught!.message).toBe('Bad request: invalid data')
  })

  it('throws HTTP status text when error body cannot be parsed', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 500,
      ok: false,
      json: () => Promise.reject(new Error('parse error')),
    })

    let caught: Error | null = null
    try {
      await fetchJson('/test-endpoint')
    } catch (e: any) {
      caught = e
    }

    expect(caught).not.toBeNull()
    expect(caught!.message).toBe('HTTP 500')
  })

  it('sends POST body as JSON', async () => {
    const mockResponse = { id: 1 }
    global.fetch = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      json: () => Promise.resolve(mockResponse),
    })

    const body = { name: 'Test', value: 42 }
    const result = await fetchJson<{ id: number }>('/create', {
      method: 'POST',
      body: JSON.stringify(body),
    })

    expect(result).toEqual(mockResponse)
    expect(global.fetch).toHaveBeenCalledWith(
      '/api/create',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(body),
      })
    )
  })

  it('does not redirect to login when window is undefined (SSR)', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 401,
      ok: false,
      json: () => Promise.resolve({ error: 'Unauthorized' }),
    })

    // Simulate SSR by temporarily removing window
    const win = window
    ;(global as any).window = undefined

    let caught: Error | null = null
    try {
      await fetchJson('/test-endpoint')
    } catch (e: any) {
      caught = e
    }

    // Restore window
    (global as any).window = win

    expect(caught).not.toBeNull()
    expect(caught!.message).toBe('Unauthorized')
    expect(clearAuthData).toHaveBeenCalledTimes(1)
  })
})
