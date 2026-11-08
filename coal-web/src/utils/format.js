/**
 * 后端 LocalDateTime 序列化成 "2026-11-08T17:57:11"，
 * 这里统一转成 "2026-11-08 17:57:11" 展示。
 */
export function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 19)
}

/**
 * 只取日期部分，用于列表里弱化显示的字段。
 */
export function formatDate(value) {
  if (!value) return '—'
  return String(value).slice(0, 10)
}
