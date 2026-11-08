import request from '../utils/request'

// 菜单树（目录 + 页面，仅管理员）
export function getMenuTree() {
  return request.get('/menus')
}
