export interface SseEvent {
  event: string
  data: string
}

/** Parses an SSE byte stream without assuming one network chunk equals one event. */
export async function* parseSseStream(
  stream: ReadableStream<Uint8Array>,
  signal?: AbortSignal
): AsyncGenerator<SseEvent> {
  const reader = stream.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (!signal?.aborted) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      buffer = buffer.replace(/\r\n/g, '\n')

      let boundary = buffer.indexOf('\n\n')
      while (boundary >= 0) {
        const raw = buffer.slice(0, boundary)
        buffer = buffer.slice(boundary + 2)
        const event = parseSseBlock(raw)
        if (event) yield event
        boundary = buffer.indexOf('\n\n')
      }
    }
    buffer += decoder.decode()
    const tail = parseSseBlock(buffer)
    if (tail) yield tail
  } finally {
    reader.releaseLock()
  }
}

export function parseSseBlock(raw: string): SseEvent | null {
  if (!raw.trim()) return null
  let event = 'message'
  const data: string[] = []
  for (const line of raw.split('\n')) {
    if (line.startsWith(':')) continue
    const separator = line.indexOf(':')
    const field = separator >= 0 ? line.slice(0, separator) : line
    let value = separator >= 0 ? line.slice(separator + 1) : ''
    if (value.startsWith(' ')) value = value.slice(1)
    if (field === 'event') event = value
    if (field === 'data') data.push(value)
  }
  return { event, data: data.join('\n') }
}

export async function streamChat(
  payload: { sessionId: number | null; message: string },
  handlers: {
    onDelta: (delta: string) => void
    onDone: (payload: any) => void
    onError: (error: any) => void
  },
  signal?: AbortSignal
): Promise<void> {
  const token = localStorage.getItem('token')
  const response = await fetch('/api/v1/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload),
    signal
  })

  if (!response.ok || !response.body) {
    let body: any = null
    try { body = await response.json() } catch { /* non-JSON proxy/error body */ }
    throw new Error(body?.detail || body?.message || `AI 请求失败（HTTP ${response.status}）`)
  }

  for await (const event of parseSseStream(response.body, signal)) {
    if (event.event === 'delta') {
      try {
        const parsed = JSON.parse(event.data)
        handlers.onDelta(typeof parsed === 'string' ? parsed : parsed?.data ?? parsed?.content ?? '')
      } catch {
        handlers.onDelta(event.data)
      }
    } else if (event.event === 'done') {
      handlers.onDone(parseJson(event.data))
    } else if (event.event === 'error') {
      handlers.onError(parseJson(event.data))
    }
  }
}

function parseJson(value: string): any {
  try { return JSON.parse(value) } catch { return { message: value } }
}
