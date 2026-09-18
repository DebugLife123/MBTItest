import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { parseSseBlock, parseSseStream, streamChat } from '@/utils/sse'

const streamFrom = (...chunks: string[]) => new ReadableStream<Uint8Array>({
  start(controller) {
    const encoder = new TextEncoder()
    chunks.forEach(chunk => controller.enqueue(encoder.encode(chunk)))
    controller.close()
  }
})

describe('SSE parsing', () => {
  it('parses named events and joins multiline data', () => {
    expect(parseSseBlock('event: delta\ndata: first\ndata: second')).toEqual({
      event: 'delta',
      data: 'first\nsecond'
    })
  })

  it('ignores comments and uses message as the default event', () => {
    expect(parseSseBlock(': keep-alive\ndata: hello')).toEqual({ event: 'message', data: 'hello' })
    expect(parseSseBlock('')).toBeNull()
  })

  it('handles CRLF and event boundaries split across network chunks', async () => {
    const events = []
    for await (const event of parseSseStream(streamFrom('event: del', 'ta\r\ndata: {"data":"你', '好"}\r\n\r\n', 'event: done\ndata: {"sessionId":1}\n\n'))) {
      events.push(event)
    }
    expect(events).toEqual([
      { event: 'delta', data: '{"data":"你好"}' },
      { event: 'done', data: '{"sessionId":1}' }
    ])
  })

  it('dispatches delta, done and error events through streamChat', async () => {
    localStorage.setItem('token', 'access-token')
    const body = streamFrom(
      'event: delta\ndata: {"data":"你"}\n\n',
      'event: delta\ndata: {"content":"好"}\n\n',
      'event: done\ndata: {"sessionId":8,"reply":"你好"}\n\n'
    )
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      body,
      json: vi.fn()
    } as any)
    const onDelta = vi.fn()
    const onDone = vi.fn()
    const onError = vi.fn()

    await streamChat({ sessionId: null, message: '测试' }, { onDelta, onDone, onError })

    expect(onDelta).toHaveBeenNthCalledWith(1, '你')
    expect(onDelta).toHaveBeenNthCalledWith(2, '好')
    expect(onDone).toHaveBeenCalledWith({ sessionId: 8, reply: '你好' })
    expect(onError).not.toHaveBeenCalled()
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/ai/chat/stream', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({ Authorization: 'Bearer access-token' }),
      body: JSON.stringify({ sessionId: null, message: '测试' })
    }))
  })
})
