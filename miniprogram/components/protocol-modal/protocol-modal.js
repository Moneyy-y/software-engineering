Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    }
  },
  data: {
    checked: false
  },
  observers: {
    show(val) {
      if (val) this.setData({ checked: false })
    }
  },
  methods: {
    noop() {},
    toggleCheck() {
      this.setData({ checked: !this.data.checked })
    },
    openUser() {
      wx.navigateTo({ url: '/pages/protocol-detail/protocol-detail?type=user' })
    },
    openPrivacy() {
      wx.navigateTo({ url: '/pages/protocol-detail/protocol-detail?type=privacy' })
    },
    onAgree() {
      if (!this.data.checked) {
        wx.showToast({ title: '请先勾选同意协议', icon: 'none' })
        return
      }
      this.triggerEvent('agree')
    },
    onReject() {
      this.triggerEvent('reject')
    }
  }
})
