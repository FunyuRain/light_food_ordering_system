import request from './request'

export function getCategories() {
  return request.get('/user/categories')
}

export function getDishes(categoryId) {
  return request.get('/user/dishes', { params: categoryId ? { categoryId } : {} })
}

export function getHotDishes() {
  return request.get('/user/dishes/hot')
}

export function getDish(id) {
  return request.get(`/user/dishes/${id}`)
}
