import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    // 登录成功后保存信息
    setLoginInfo(data) {
      this.token = data.token
      this.userInfo = data.userInfo
      this.menus = data.userInfo?.menus || []

      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
      localStorage.setItem('menus', JSON.stringify(this.menus))
    },
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      this.menus = []

      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('menus')
    }
  }
})
