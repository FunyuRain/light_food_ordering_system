import request from './request'

export function getDishes(params) {
  return request.get('/admin/dishes', { params })
}

export function getDish(id) {
  return request.get(`/admin/dishes/${id}`)
}

export function createDish(data) {
  return request.post('/admin/dishes', data)
}

export function updateDish(id, data) {
  return request.put(`/admin/dishes/${id}`, data)
}

export function deleteDish(id) {
  return request.delete(`/admin/dishes/${id}`)
}

export function changeDishStatus(id, status) {
  return request.patch(`/admin/dishes/${id}/status`, { status })
}
