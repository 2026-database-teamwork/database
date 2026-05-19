<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login, join } from '../api/auth';

const router = useRouter();
const isLoginMode = ref(true);

const loginForm = ref({
  name: '',
  password: ''
});

const joinForm = ref({
  name: '',
  license: '',
  password: '',
  address: '',
  phone: '',
  email: ''
});

const handleLogin = async () => {
  try {
    const response = await login(loginForm.value);
    
    // 📌 1. Axios는 헤더명을 소문자로 관리하므로 'authorization'으로 꺼냅니다.
    const authHeader = response.headers['authorization']; 
    
    if (authHeader && authHeader.startsWith('Bearer ')) {
      // 📌 2. 'Bearer ' (7글자) 뒤의 순수 JWT 토큰만 잘라냅니다.
      const token = authHeader.substring(7);
      
      // 📌 3. 로컬 스토리지에 저장하고 메인으로 이동!
      localStorage.setItem('token', token);
      router.push('/main');
    } else {
      // 200 OK는 떴는데 헤더에 토큰이 없는 이상 상황 방어 코드
      console.error("응답 헤더에 Authorization 토큰이 없습니다:", response.headers);
      alert('로그인 세션 처리에 실패했습니다.');
    }
  } catch (error) {
    alert('로그인에 실패했습니다. 아이디/비밀번호를 확인해주세요.');
    console.error(error);
  }
};

const handleJoin = async () => {
  try {
    await join(joinForm.value);
    alert('회원가입이 완료되었습니다. 로그인해주세요.');
    isLoginMode.value = true;
  } catch (error) {
    alert('회원가입에 실패했습니다.');
    console.error(error);
  }
};

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value;
};
</script>

<template>
  <div class="min-h-screen bg-fog flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="bg-snow p-12 rounded-[28px] max-w-md w-full shadow-sm flex flex-col gap-8">
      
      <div class="text-center">
        <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-2">
          {{ isLoginMode ? 'Welcome back' : 'Create your account' }}
        </h2>
        <p class="text-body-sm text-graphite tracking-body-sm">
          Camping Car Rental Service
        </p>
      </div>

      <!-- Login Form -->
      <form v-if="isLoginMode" @submit.prevent="handleLogin" class="flex flex-col gap-4">
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Name</label>
          <input v-model="loginForm.name" type="text" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Password</label>
          <input v-model="loginForm.password" type="password" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        
        <button type="submit" 
          class="mt-4 w-full bg-azure text-snow rounded-buttons py-3 px-6 text-body font-medium transition-transform active:scale-[0.98]">
          Log In
        </button>
      </form>

      <!-- Join Form -->
      <form v-else @submit.prevent="handleJoin" class="flex flex-col gap-4">
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Name</label>
          <input v-model="joinForm.name" type="text" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Password</label>
          <input v-model="joinForm.password" type="password" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">License</label>
          <input v-model="joinForm.license" type="text" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Email</label>
          <input v-model="joinForm.email" type="email" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Phone</label>
          <input v-model="joinForm.phone" type="tel" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>
        <div>
          <label class="block text-caption font-semibold text-graphite mb-1 ml-2">Address</label>
          <input v-model="joinForm.address" type="text" required
            class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all" />
        </div>

        <button type="submit" 
          class="mt-4 w-full bg-azure text-snow rounded-buttons py-3 px-6 text-body font-medium transition-transform active:scale-[0.98]">
          Sign Up
        </button>
      </form>

      <div class="text-center pt-4 border-t border-silver-mist">
        <button @click="toggleMode" class="text-cobalt-link text-body-sm font-medium hover:underline">
          {{ isLoginMode ? "Don't have an account? Sign up" : "Already have an account? Log in" }}
        </button>
      </div>

    </div>
  </div>
</template>
