import request from './request'

export function getOrders(params) {
  return request.get('/admin/orders', { params })
}

export function getOrder(id) {
  return request.get(`/admin/orders/${id}`)
}

export function acceptOrder(id) {
  return request.put(`/admin/orders/${id}/accept`)
}

export function cancelOrder(id, reason) {
  return request.put(`/admin/orders/${id}/cancel`, { reason })
}
