/**
 * 轻量文本渲染：先完整转义 HTML，再生成受控标签，避免 AI 输出造成 XSS。
 */
export function escapeHtml(value: string): string {
  return (value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function renderSafeText(value: string): string {
  const escaped = escapeHtml(value)
  return escaped
    .replace(/^###\s+(.+)$/gm, '<h5 class="md-heading">$1</h5>')
    .replace(/^##\s+(.+)$/gm, '<h4 class="md-heading">$1</h4>')
    .replace(/^#\s+(.+)$/gm, '<h3 class="md-heading">$1</h3>')
    .replace(/^(\s*)[-*]\s+(.+)$/gm, '<div class="md-item">• $2</div>')
    .replace(/^(\s*)(\d+)\.\s+(.+)$/gm, '<div class="md-item">$2. $3</div>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br/>')
}
