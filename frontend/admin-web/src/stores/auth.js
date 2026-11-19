import { defineStore } from 'pinia'

const TOKEN_KEY = 'lf_admin_token'
const PROFILE_KEY = 'lf_admin_profile'

function readProfile() {
  try {
    return JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null')
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    profile: readProfile()
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.profile?.name || state.profile?.username || '管理员'
  },
  actions: {
    setSession(payload) {
      this.token = payload.token
      this.profile = {
        id: payload.id,
        username: payload.username,
        name: payload.name,
        role: payload.role
      }
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(PROFILE_KEY)
    }
  }
})
