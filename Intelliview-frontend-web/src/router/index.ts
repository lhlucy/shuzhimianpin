import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { refreshAuthState } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/user'
  },

  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../pages/admin/Auth/LoginPage.vue'),
    meta: { guestOnly: true, adminAuthPage: true }
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('../pages/admin/Home/index.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../pages/admin/Users/index.vue')
      },
      {
        path: 'questions',
        name: 'AdminQuestions',
        component: () => import('../pages/admin/Questions/index.vue')
      },
      {
        path: 'interviews',
        name: 'AdminInterviews',
        component: () => import('../pages/admin/Interviews/index.vue')
      },
      {
        path: 'analytics',
        name: 'AdminAnalytics',
        component: () => import('../pages/admin/Home/index.vue'),
        meta: { analyticsMode: true }
      },
      {
        path: 'bank/:id',
        name: 'AdminBankDetail',
        component: () => import('../pages/admin/BankDetail/index.vue')
      },
      {
        path: 'bank/:bankId/question/:questionId/edit',
        name: 'AdminQuestionEdit',
        component: () => import('../pages/admin/QuestionEdit/index.vue')
      }
    ]
  },

  { path: '/user', name: 'User', component: () => import('../pages/user/Home.vue') },
  { path: '/user/practice', name: 'PracticeCenter', component: () => import('../pages/user/PracticeCenter.vue') },
  { path: '/user/interview/ai/create', name: 'AIInterviewCreate', component: () => import('../pages/user/AIInterview/AIInterviewCreate.vue') },
  { path: '/user/interview/ai/loading', name: 'AIInterviewLoading', component: () => import('../pages/user/AIInterview/AIInterviewLoading.vue') },
  { path: '/user/interview/ai/session/:id', name: 'AIInterviewSession', component: () => import('../pages/user/AIInterview/AIInterviewSession.vue') },
  { path: '/user/growth', name: 'GrowthCenter', component: () => import('../pages/user/GrowthCenter.vue') },
  { path: '/user/profile', name: 'UserProfile', component: () => import('../pages/user/Profile.vue') },
  { path: '/user/favorites', name: 'UserFavorites', component: () => import('../pages/user/Favorites.vue') },
  { path: '/user/history', name: 'UserHistory', component: () => import('../pages/user/PracticeHistory.vue') },
  { path: '/user/hot-questions', name: 'HotQuestions', component: () => import('../pages/user/HotQuestions.vue') },
  { path: '/user/user-rankings', name: 'UserRankings', component: () => import('../pages/user/UserRankings.vue') },
  { path: '/question-bank/:id', name: 'QuestionBankDetail', component: () => import('../pages/user/QuestionBankDetail.vue') },
  { path: '/question/:id', name: 'QuestionDetail', component: () => import('../pages/user/QuestionDetail.vue') },
  { path: '/privacy', name: 'PrivacyNotice', component: () => import('../pages/PrivacyNotice.vue') },

  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/user/Login/LoginPage.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/user/Login/RegisterPage.vue'),
    meta: { guestOnly: true }
  },

  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/user'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  refreshAuthState()
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const userRole = localStorage.getItem('userRole') || 'user'

  if (to.meta.guestOnly && isLoggedIn) {
    next(userRole === 'admin' ? '/admin' : '/user')
    return
  }

  if (to.meta.requiresAuth && to.meta.role === 'admin') {
    if (!isLoggedIn) {
      next('/admin/login')
      return
    }

    if (userRole !== 'admin') {
      next('/user')
      return
    }

    next()
    return
  }

  if (isLoggedIn && userRole === 'admin' && !to.path.startsWith('/admin')) {
    next('/admin')
    return
  }

  if (to.meta.adminAuthPage && isLoggedIn && userRole === 'admin') {
    next('/admin')
    return
  }

  if (to.path === '/login' && isLoggedIn) {
    if (userRole === 'admin') {
      next('/admin')
    } else {
      next('/user')
    }
    return
  }

  if (to.path === '/admin/login' && isLoggedIn) {
    if (userRole === 'admin') {
      next('/admin')
    } else {
      next('/user')
    }
    return
  }

  next()
})

export default router
