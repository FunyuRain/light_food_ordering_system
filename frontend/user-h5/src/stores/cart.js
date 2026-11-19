import { defineStore } from 'pinia'
import { getCart } from '@/api/cart'

export const useCartStore = defineStore('userCart', {
  state: () => ({
    count: 0,
    amount: 0,
    loaded: false
  }),
  actions: {
    setFromCart(cart) {
      this.count = cart?.totalQuantity || 0
      this.amount = Number(cart?.totalAmount || 0)
      this.loaded = true
    },
    async refresh() {
      if (!localStorage.getItem('lf_user_token')) {
        this.reset()
        return
      }
      try {
        const cart = await getCart()
        this.setFromCart(cart)
      } catch {
        this.reset()
      }
    },
    reset() {
      this.count = 0
      this.amount = 0
      this.loaded = false
    }
  }
})
