import request from './request'

export function getAddresses() {
  return request.get('/user/addresses')
}

export function createAddress(data) {
  return request.post('/user/addresses', data)
}

export function setDefaultAddress(id) {
  return request.put(`/user/addresses/${id}/default`)
}

export function deleteAddress(id) {
  return request.delete(`/user/addresses/${id}`)
}
