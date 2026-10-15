// 表单验证工具函数

const validator = {
  // 验证手机号
  isValidPhone: (phone) => {
    return /^1[3-9]\d{9}$/.test(phone)
  },
  // 验证邮箱
  isValidEmail: (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  },
  // 验证密码强度
  isStrongPassword: (password) => {
    return password.length >= 8 && /[a-zA-Z]/.test(password) && /[0-9]/.test(password)
  },
  // 验证非空
  isNotEmpty: (value) => {
    return value !== null && value !== undefined && value.trim() !== ''
  },
  // 验证数字范围
  isInRange: (value, min, max) => {
    const num = Number(value)
    return !isNaN(num) && num >= min && num <= max
  }
}

export default validator
