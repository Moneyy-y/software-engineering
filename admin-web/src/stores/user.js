import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')

  function setLogin(data) {
    token.value = data.token
    nickname.value = data.nickname
    localStorage.setItem('token', data.token)
    localStorage.setItem('nickname', data.nickname)
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
  }

  return { token, nickname, setLogin, logout }
})
