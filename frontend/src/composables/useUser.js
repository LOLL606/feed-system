import { ref, readonly } from 'vue'
import * as API from '../api/index.js'

const currentUser = ref(null)

export function useUser() {
  async function login(userId) {
    currentUser.value = await API.switchUser(userId)
    sessionStorage.setItem('currentUserId', userId)
  }

  function logout() {
    currentUser.value = null
    sessionStorage.removeItem('currentUserId')
  }

  async function restore() {
    const saved = sessionStorage.getItem('currentUserId')
    if (saved) {
      await login(parseInt(saved))
      return true
    }
    return false
  }

  return { currentUser: readonly(currentUser), login, logout, restore }
}
