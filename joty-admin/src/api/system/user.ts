import type { ApiModule } from '@/utils/api'
//用户接口
export const user: ApiModule = {
  get: { url: '/api/user/get', method: 'get' },
  list: { url: '/user/list', method: 'post' },
  add: { url: '/user/add', method: 'post' },
  update: { url: '/user/update', method: 'put' },
  del: { url: '/user/del', method: 'delete' },
  login: { url: '/api/user/login', method: 'post' },
  register: { url: '/api/user/reg', method: 'post' },
  logout: { url: '/api/user/logout', method: 'post' },
  getApiKey: { url: '/api/user/get-api-key', method: 'get' },
  resetApikey: { url: '/api/user/reset-api-key', method: 'put' },
  uploadAvatar: { url: '/user/upload-avatar', method: 'post' },
} as const
