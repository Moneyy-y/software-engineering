import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const role = ref(localStorage.getItem('role') || '')
  const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))

  function setLogin(data) {
    token.value = data.token
    nickname.value = data.nickname
    role.value = data.role || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('role', data.role || '')
  }

  async function fetchMenus() {
    try {
      const data = await request.get('/api/permission/menus')
      menus.value = data || []
      localStorage.setItem('menus', JSON.stringify(data || []))
    } catch {
      menus.value = []
    }
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    role.value = ''
    menus.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('role')
    localStorage.removeItem('menus')
  }

  return { token, nickname, role, menus, setLogin, fetchMenus, logout }
})
