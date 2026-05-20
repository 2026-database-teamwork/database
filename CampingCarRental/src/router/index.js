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
      component: AuthView
    },
    {
      path: '/main',
      name: 'main',
      component: MainView
    },
    {
      path: '/rental',
      name: 'rental',
      component: RentalView
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView
    }
  ]
})

// Simple navigation guard to check token
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token');
  if ((to.name === 'main' || to.name === 'rental' || to.name === 'mypage') && !isAuthenticated) {
    next({ name: 'auth' });
  } else if (to.name === 'auth' && isAuthenticated) {
    next({ name: 'main' });
  } else {
    next();
  }
});

export default router
