import { computed, ref } from 'vue'
import service from '@/utils/axios'

export interface AuthUser {
  id?: number
  username: string
  email?: string
  nickname?: string
  avatar?: string
  role?: string
  emailVerified?: boolean
  phone?: string
  bio?: string
  points?: number
  level?: number
  experience?: number
  targetJobRoleId?: number
  experienceLevel?: string
  preferredCompanyType?: string
  targetCity?: string
  expectedSalary?: string
  resumeSummary?: string
  latestOverallScore?: number
  latestJobMatchScore?: number
  createTime?: string
  lastLoginTime?: string
}

const readStoredUser = (): AuthUser | null => {
  const raw = localStorage.getItem('currentUser')
  if (raw) {
    try {
      return JSON.parse(raw) as AuthUser
    } catch {
      localStorage.removeItem('currentUser')
    }
  }

  const username = localStorage.getItem('username')
  if (!username) return null

  return {
    username,
    role: localStorage.getItem('userRole') === 'admin' ? 'ROLE_ADMIN' : 'ROLE_USER'
  }
}

const hasToken = () => Boolean(localStorage.getItem('token'))

export const authUser = ref<AuthUser | null>(readStoredUser())
const authRevision = ref(0)
export const isAuthenticated = computed(() => {
  authRevision.value
  return hasToken() && localStorage.getItem('isLoggedIn') === 'true'
})

export const getDisplayName = (user = authUser.value) =>
  user?.nickname || user?.username || '用户'

export const getAvatarInitial = (user = authUser.value) => {
  const name = getDisplayName(user)
  return name.trim().slice(0, 1).toUpperCase() || '用'
}

export const refreshAuthState = () => {
  authUser.value = readStoredUser()
  authRevision.value += 1
}

export const saveAuthSession = (token: string, user: AuthUser) => {
  const userRole = user.role === 'ROLE_ADMIN' ? 'admin' : 'user'

  localStorage.setItem('isLoggedIn', 'true')
  localStorage.setItem('username', user.username)
  localStorage.setItem('userRole', userRole)
  localStorage.setItem('token', token)
  localStorage.setItem('currentUser', JSON.stringify(user))
  refreshAuthState()
}

export const clearAuthSession = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('username')
  localStorage.removeItem('userRole')
  localStorage.removeItem('token')
  localStorage.removeItem('currentUser')
  refreshAuthState()
}

export const loadCurrentUser = async () => {
  if (!hasToken()) {
    refreshAuthState()
    return null
  }

  const response = await service.get('/api/user/profile')
  const user = response.data as AuthUser
  localStorage.setItem('currentUser', JSON.stringify(user))
  localStorage.setItem('username', user.username)
  localStorage.setItem('userRole', user.role === 'ROLE_ADMIN' ? 'admin' : 'user')
  localStorage.setItem('isLoggedIn', 'true')
  refreshAuthState()
  return user
}
