import request from './request'

export function getCart() {
  return request.get('/user/cart')
}

export function addToCart(dishId, quantity = 1) {
  return request.post('/user/cart', { dishId, quantity })
}

export function updateCartQuantity(id, quantity) {
  return request.put(`/user/cart/${id}`, { quantity })
}

export function removeCartItem(id) {
  return request.delete(`/user/cart/${id}`)
}

export function clearCart() {
  return request.delete('/user/cart')
}
