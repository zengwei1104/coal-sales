import request from '../utils/request'

// 角色列表（不分页，内置 6 个角色）
export function getRoles() {
  return request.get('/roles')
}

// 查询角色已分配的菜单 id（用于权限树回显）
export function getRoleMenuIds(id) {
  return request.get(`/roles/${id}/menus`)
}

// 给角色分配菜单（全量重设）
export function assignRoleMenus(id, menuIds) {
  return request.put(`/roles/${id}/menus`, { menuIds })
}
