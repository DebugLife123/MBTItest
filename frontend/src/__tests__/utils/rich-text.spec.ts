import { describe, expect, it } from 'vitest'
import { escapeHtml, renderSafeText } from '@/utils/rich-text'

describe('safe rich text rendering', () => {
  it('escapes HTML special characters', () => {
    expect(escapeHtml('<script>alert("x")</script>')).toBe('&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;')
    expect(escapeHtml("A&B 'quoted'")).toBe('A&amp;B &#39;quoted&#39;')
  })

  it('renders supported markdown without allowing executable markup', () => {
    const html = renderSafeText('# 标题\n\n- 项目\n\n**重点** 和 `code`\n\n<script>alert(1)</script>')
    expect(html).toContain('<h3 class="md-heading">标题</h3>')
    expect(html).toContain('<div class="md-item">• 项目</div>')
    expect(html).toContain('<strong>重点</strong>')
    expect(html).toContain('<code>code</code>')
    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(html).not.toContain('<script>')
  })
})
