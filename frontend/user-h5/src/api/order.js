import request from './request'

export function createOrder(data) {
  return request.post('/user/orders', data)
}

export function getOrders(params) {
  return request.get('/user/orders', { params })
}

export function getOrder(id) {
  return request.get(`/user/orders/${id}`)
}

export function payOrder(id) {
  return request.put(`/user/orders/${id}/pay`)
}

export function cancelOrder(id, reason) {
  return request.put(`/user/orders/${id}/cancel`, { reason })
}

export function completeOrder(id) {
  return request.put(`/user/orders/${id}/complete`)
}
