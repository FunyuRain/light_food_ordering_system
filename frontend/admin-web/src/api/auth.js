import request from './request'

export function login(data) {
  return request.post('/admin/login', data)
}

export function register(data) {
  return request.post('/api/admin/register', data)
}
