export const ORDER_STATUS = {
  0: { label: '待付款', type: 'info', tone: 'muted' },
  1: { label: '待接单', type: 'warning', tone: 'amber' },
  2: { label: '已接单', type: 'primary', tone: 'green' },
  3: { label: '已完成', type: 'success', tone: 'green' },
  4: { label: '已取消', type: 'danger', tone: 'red' }
}

export function orderStatus(status) {
  return ORDER_STATUS[status] || { label: '未知', type: 'info', tone: 'muted' }
}

export function money(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

export function dateText(value) {
  return value || '—'
}

export function cancelByText(value) {
  if (value === 1) return '用户取消'
  if (value === 2) return '管理员取消'
  return '—'
}
