import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '../views/AuthView.vue'
import MainView from '../views/MainView.vue'
import RentalView from '../views/RentalView.vue'
import MyPageView from '../views/MyPageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'auth',
      component: AuthView,
    },
    {
      path: '/main',
      name: 'main',
      component: MainView,
    },
    {
      path: '/rental',
      name: 'rental',
      component: RentalView,
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
    },
  ],
})
router.beforeEach((to, from) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'

  // 로그인을 안 했는데 다른 페이지로 가려고 하면 가로막고 로그인 페이지로 보냄
  if (to.path !== '/' && !isLoggedIn) {
    alert('로그인이 필요한 페이지입니다.')
    return '/'
  }
})


export default router
