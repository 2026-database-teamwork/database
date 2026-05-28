<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRentals } from '../api/rental'
import { getMyCoupons } from '../api/coupons'

const router = useRouter()
const rentals = ref([])
const coupons = ref([])
const isLoading = ref(true)
const username = ref('')
const activeTab = ref('rentals') // 'rentals' | 'coupons'

onMounted(async () => {
  try {
    username.value = localStorage.getItem('username')
    if (!username.value) {
      alert('사용자 정보가 없습니다. 다시 로그인해주세요.')
      router.replace('/')
      return
    }
    
    // 예약 이력과 쿠폰 목록을 병렬로 가져옵니다.
    const [rentalsResponse, couponsResponse] = await Promise.all([
      getMyRentals(username.value),
      getMyCoupons(username.value)
    ])
    
    rentals.value = rentalsResponse.data || []
    coupons.value = couponsResponse.data || []
  } catch (error) {
    console.error('Failed to fetch data:', error)
    alert('정보를 불러오는데 실패했습니다.')
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
      <h1 class="text-heading font-bold tracking-heading text-ink mb-2">마이페이지</h1>
      <p class="text-subheading tracking-subheading text-graphite mb-8">
        {{ username }}님의 예약 내역과 쿠폰 보관함을 관리합니다.
      </p>

      <!-- Tabs Header -->
      <div class="flex gap-6 border-b border-silver-mist mb-8">
        <button 
          @click="activeTab = 'rentals'"
          :class="[
            'pb-4 text-body font-semibold transition-all relative border-b-2',
            activeTab === 'rentals' 
              ? 'text-azure border-azure font-bold' 
              : 'text-graphite border-transparent hover:text-ink'
          ]"
        >
          예약 내역 ({{ rentals.length }})
        </button>
        <button 
          @click="activeTab = 'coupons'"
          :class="[
            'pb-4 text-body font-semibold transition-all relative border-b-2',
            activeTab === 'coupons' 
              ? 'text-azure border-azure font-bold' 
              : 'text-graphite border-transparent hover:text-ink'
          ]"
        >
          쿠폰 보관함 ({{ coupons.length }})
        </button>
      </div>

      <div v-if="isLoading" class="text-graphite py-8 text-center">불러오는 중...</div>
      
      <!-- Rentals Tab -->
      <div v-else-if="activeTab === 'rentals'" class="space-y-6">
        <div v-if="rentals.length === 0" class="bg-snow rounded-cards p-[28px] text-center">
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

        <div
          v-else
          v-for="(rental, index) in rentals"
          :key="index"
          class="bg-snow rounded-cards p-[28px] flex flex-col md:flex-row justify-between gap-6 hover:shadow-md transition-shadow duration-300"
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

      <!-- Coupons Tab -->
      <div v-else-if="activeTab === 'coupons'">
        <div v-if="coupons.length === 0" class="bg-snow rounded-cards p-[40px] text-center shadow-sm">
          <div class="text-5xl mb-4">🎫</div>
          <h3 class="text-subheading font-bold text-ink mb-2">보유한 쿠폰이 없습니다</h3>
          <p class="text-body-sm text-graphite">가입 환영 또는 다양한 이벤트 쿠폰이 지급됩니다.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div 
            v-for="coupon in coupons" 
            :key="coupon.userCouponId"
            :class="[
              'relative flex bg-snow rounded-[20px] overflow-hidden border border-silver-mist/80 shadow-sm transition-all duration-300',
              coupon.isUsed ? 'opacity-50 saturate-50' : 'hover:shadow-md hover:border-azure/30 group'
            ]"
          >
            <!-- Ticket cutouts on the sides -->
            <div class="absolute left-[-10px] top-1/2 transform -translate-y-1/2 w-5 h-5 rounded-full bg-fog border-r border-silver-mist/80 z-10"></div>
            <div class="absolute right-[-10px] top-1/2 transform -translate-y-1/2 w-5 h-5 rounded-full bg-fog border-l border-silver-mist/80 z-10"></div>

            <!-- Left section: Discount Value badge -->
            <div 
              :class="[
                'w-[110px] flex flex-col items-center justify-center text-snow font-bold py-6 pl-4 pr-3 text-center transition-colors',
                coupon.isUsed ? 'bg-graphite' : 'bg-gradient-to-br from-azure to-cobalt-link'
              ]"
            >
              <div class="text-2xl tracking-tight leading-none mb-1">
                {{ coupon.discountType === 'percent' ? `${coupon.discountValue}%` : `₩${(coupon.discountValue / 1000).toFixed(0)}k` }}
              </div>
              <div class="text-[10px] font-semibold opacity-95 tracking-wider uppercase">
                {{ coupon.discountType === 'percent' ? '할인' : '할인' }}
              </div>
            </div>

            <!-- Dashed separator -->
            <div class="border-l-2 border-dashed border-silver-mist/60 h-full my-auto"></div>

            <!-- Right section: Info details -->
            <div class="flex-grow p-5 pl-6 flex flex-col justify-between min-w-0">
              <div>
                <div class="flex items-center gap-2 mb-1.5 flex-wrap">
                  <span 
                    :class="[
                      'text-[9px] font-bold px-2 py-0.5 rounded-full tracking-wider',
                      coupon.isUsed 
                        ? 'bg-silver-mist text-graphite' 
                        : 'bg-azure/10 text-azure'
                    ]"
                  >
                    {{ coupon.isUsed ? '사용 완료' : '사용 가능' }}
                  </span>
                  <span class="text-[10px] text-graphite font-mono font-bold truncate max-w-[80px]" :title="coupon.couponCode">{{ coupon.couponCode }}</span>
                </div>
                <h4 class="font-bold text-body text-ink leading-tight mb-2 group-hover:text-azure transition-colors truncate">
                  {{ coupon.couponName }}
                </h4>
              </div>
              <div class="text-[11px] text-graphite mt-2">
                <span v-if="coupon.minOrderAmount > 0">₩{{ coupon.minOrderAmount.toLocaleString() }} 이상 결제 시 사용 가능</span>
                <span v-else>최소 대여금액 제한 없음</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
