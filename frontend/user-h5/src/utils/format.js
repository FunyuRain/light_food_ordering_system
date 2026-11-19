export const ORDER_STATUS = {
  0: { label: '待付款', type: 'warning', tone: 'amber', hint: '订单已提交，等待完成模拟支付' },
  1: { label: '待接单', type: 'primary', tone: 'green', hint: '已付款，正在等待商家接单' },
  2: { label: '已接单', type: 'primary', tone: 'green', hint: '商家已接单，正在准备你的餐食' },
  3: { label: '已完成', type: 'success', tone: 'deep', hint: '订单已完成，感谢你的选择' },
  4: { label: '已取消', type: 'danger', tone: 'red', hint: '订单已取消' }
}

export function orderStatus(status) {
  return ORDER_STATUS[status] || { label: '未知', type: 'default', tone: 'muted', hint: '' }
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
  return '取消'
}

export function fullAddress(address) {
  if (!address) return ''
  return `${address.province || ''}${address.city || ''}${address.district || ''}${address.detailAddress || ''}`
}
