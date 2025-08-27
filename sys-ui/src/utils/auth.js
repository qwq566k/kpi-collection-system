// 解析JWT token获取用户信息
export const parseToken = (token) => {
  try {
    if (!token) return null

    // 检查是否是JWT格式（包含两个点）
    if (token.split('.').length !== 3) {
      return null
    }

    // base64url 解码并按 UTF-8 解析
    const base64UrlToUtf8Json = (base64Url) => {
      // 转成标准 base64 并补齐填充
      let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      while (base64.length % 4 !== 0) base64 += '=';
      const binary = atob(base64);
      // 按 UTF-8 还原
      try {
        const percentEncoded = Array.from(binary)
          .map((ch) => '%' + ch.charCodeAt(0).toString(16).padStart(2, '0'))
          .join('');
        const utf8String = decodeURIComponent(percentEncoded);
        return JSON.parse(utf8String);
      } catch (_) {
        // 回退：直接按 latin1 解析
        return JSON.parse(binary);
      }
    }

    // 解析JWT payload部分
    const payload = token.split('.')[1]
    const decodedPayload = base64UrlToUtf8Json(payload)

    return {
      id: decodedPayload.id || decodedPayload.userId || decodedPayload.sub,
      name: decodedPayload.name || decodedPayload.username,
      role: decodedPayload.role || decodedPayload.role_id || decodedPayload.userRole,
      exp: decodedPayload.exp,
      iat: decodedPayload.iat
    }
  } catch (error) {
    console.warn('解析JWT token失败:', error)
    return null
  }
}

// 检查token是否过期
export const isTokenExpired = (token) => {
  const tokenInfo = parseToken(token)
  if (!tokenInfo || !tokenInfo.exp) return true
  
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime >= tokenInfo.exp
}

// 从localStorage获取用户信息
export const getUserInfo = () => {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  const tokenInfo = parseToken(token)
  if (!tokenInfo) return null
  
  return {
    id: tokenInfo.id || localStorage.getItem('userId'),
    name: tokenInfo.name,
    role: tokenInfo.role || localStorage.getItem('userRole'),
    token: token
  }
}

// 清除用户信息
export const clearUserInfo = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userInfo')
}


