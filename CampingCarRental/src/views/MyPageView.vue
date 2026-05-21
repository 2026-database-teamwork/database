<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRentals } from '../api/rental'

const router = useRouter()
const rentals = ref([])
const isLoading = ref(true)

onMounted(async () => {
  try {
    const response = await getMyRentals()
    rentals.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch rentals:', error)
    alert('예약 내역을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
  }).format(date)
}
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <button
          @click="router.push('/main')"
          class="text-cobalt-link text-body-sm font-medium hover:underline flex items-center"
        >
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            ></path>
          </svg>
          메인으로
        </button>
        <h1 class="text-body font-semibold tracking-body text-ink">마이페이지</h1>
        <div class="w-20"></div>
        <!-- spacer -->
      </div>
    </header>

    <main class="max-w-[800px] mx-auto px-6 mt-12">
      <h1 class="text-heading font-bold tracking-heading text-ink mb-2">예약 내역</h1>
      <p class="text-subheading tracking-subheading text-graphite mb-8">
        나의 캠핑카 예약 내역을 확인하세요.
      </p>

      <div v-if="isLoading" class="text-graphite py-8 text-center">불러오는 중...</div>
      <div v-else-if="rentals.length === 0" class="bg-snow rounded-cards p-[28px] text-center">
        <div class="text-4xl mb-4">🚐</div>
        <h3 class="text-subheading font-bold text-ink mb-2">예약된 내역이 없습니다</h3>
        <p class="text-body-sm text-graphite mb-6">첫 캠핑카 여행을 예약해보세요!</p>
        <button
          @click="router.push('/main')"
          class="bg-azure text-snow px-6 py-2 rounded-buttons text-body-sm font-medium hover:bg-cobalt-link transition-colors"
        >
          차량 둘러보기
        </button>
      </div>

      <div v-else class="space-y-6">
        <div
          v-for="(rental, index) in rentals"
          :key="index"
          class="bg-snow rounded-cards p-[28px] flex flex-col md:flex-row justify-between gap-6"
        >
          <div>
            <div class="flex items-center gap-3 mb-2">
              <span
                class="bg-silver-mist/50 text-ink text-caption font-semibold px-3 py-1 rounded-full"
                >예약완료</span
              >
            </div>
            <h3 class="text-subheading font-bold tracking-subheading text-ink mb-1">
              {{ rental.carName }}
            </h3>
            <p class="text-body-sm text-graphite mb-4">
              {{ rental.companyName }} | 차량번호: {{ rental.license }}
            </p>

            <div class="flex flex-col gap-1 text-body-sm text-ink bg-fog p-4 rounded-lg">
              <div>
                <span class="font-semibold text-graphite mr-2">대여일시</span>
                {{ formatDate(rental.startDateTime) }}
              </div>
              <div>
                <span class="font-semibold text-graphite mr-2">반납일시</span>
                {{ formatDate(rental.endDateTime) }}
              </div>
            </div>
          </div>

          <div
            class="flex flex-col justify-end md:text-right border-t border-silver-mist md:border-0 pt-4 md:pt-0 mt-2 md:mt-0"
          >
            <span class="text-body-sm text-graphite mb-1">총 결제 금액</span>
            <span class="text-heading-sm font-bold tracking-heading-sm text-azure"
              >₩{{ rental.totalCost.toLocaleString() }}</span
            >
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
