<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createRental, getCarRentals } from '../api/rental'
import { getMyCoupons } from '../api/coupons'
import DateRangePicker from '../components/DateRangePicker.vue'
const router = useRouter()
const carData = history.state.car
const companyId = history.state.companyId
const username = ref('')
const coupons = ref([])
const selectedUserCouponId = ref(null)
const selectedUserCoupon = computed(() => {
  if (!selectedUserCouponId.value) return null
  return coupons.value.find(c => c.userCouponId === selectedUserCouponId.value) || null
})
// If user navigates directly without state, redirect back
if (!carData) {
  alert('잘못된 접근입니다.')
  router.replace('/main')
}
const startDateTime = ref('')
const endDateTime = ref('')
const isSubmitting = ref(false)
const bookedRanges = ref([])
onMounted(async () => {
  username.value = localStorage.getItem('username')
  if (!username.value) {
    alert('사용자 정보가 없습니다. 다시 로그인해주세요.')
    router.replace('/')
    return
  }
  if (carData?.carId) {
    try {
      const response = await getCarRentals(carData.carId)
      if (response.data) {
        bookedRanges.value = response.data.map((rental) => ({
          start: rental.startDateTime,
          end: rental.endDateTime,
        }))
      }
    } catch (error) {
      console.error('Failed to load existing rentals:', error)
    }
  }
  // 사용 가능한 쿠폰 가져오기
  try {
    const couponsResponse = await getMyCoupons(username.value)
    coupons.value = (couponsResponse.data || []).filter(c => !c.isUsed)
  } catch (error) {
    console.error('Failed to load coupons:', error)
  }
})
// Mock add-ons
const insuranceOption = ref('standard')
const addCampingSet = ref(false)
const addBbqGrill = ref(false)
const calculateDays = (start, end) => {
  if (!start || !end) return 0
  const startDate = new Date(start)
  const endDate = new Date(end)
  const diffTime = endDate - startDate
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 0
}
const rentalDays = computed(() => calculateDays(startDateTime.value, endDateTime.value))
const baseCost = computed(() => rentalDays.value * (carData?.carRentalCost || 0))
const insuranceCost = computed(() => {
  if (insuranceOption.value === 'premium') return rentalDays.value * 20000
  return 0 // Standard is included
})
const addonsCost = computed(() => {
  let cost = 0
  if (addCampingSet.value) cost += 30000
  if (addBbqGrill.value) cost += 15000
  return cost
})
// 쿠폰 적용 전 금액
const preCouponTotal = computed(() => baseCost.value + insuranceCost.value + addonsCost.value)
// 쿠폰 할인 금액 계산
const couponDiscount = computed(() => {
  if (!selectedUserCoupon.value) return 0
  const coupon = selectedUserCoupon.value
  
  // 최소 결제 금액 검증
  if (preCouponTotal.value < coupon.minOrderAmount) return 0
  
  if (coupon.discountType === 'percent') {
    return Math.floor(preCouponTotal.value * (coupon.discountValue / 100))
  } else if (coupon.discountType === 'amount') {
    return coupon.discountValue
  }
  return 0
})
// 최종 금액 (할인 적용 후)
const totalCost = computed(() => Math.max(0, preCouponTotal.value - couponDiscount.value))
// 결제 금액이 변경될 때 쿠폰 최소 이용 금액 조건 다시 체크
watch(preCouponTotal, (newTotal) => {
  if (selectedUserCoupon.value && newTotal < selectedUserCoupon.value.minOrderAmount) {
    alert(`선택하신 쿠폰의 최소 주문금액(₩${selectedUserCoupon.value.minOrderAmount.toLocaleString()}) 조건을 충족하지 않아 쿠폰 선택이 해제됩니다.`)
    selectedUserCouponId.value = null
  }
})
const handleRentalSubmit = async () => {
  if (rentalDays.value <= 0) {
    alert('반납일은 대여일보다 이후여야 합니다.')
    return
  }
  isSubmitting.value = true
  try {
    const payload = {
      carId: carData.carId,
      companyId: companyId,
      username: username.value,
      startDateTime: `${startDateTime.value}T00:00:00`,
      endDateTime: `${endDateTime.value}T23:59:59`,
      totalCost: totalCost.value,
      userCouponId: selectedUserCouponId.value || null
    }
    await createRental(payload)
    alert('렌트 예약이 성공적으로 완료되었습니다!')
    router.push('/mypage')
  } catch (error) {
    console.error('Rental failed:', error)
    alert('예약 요청에 실패했습니다. 다시 시도해주세요.')
  } finally {
    isSubmitting.value = false
  }
}
</script>
<template>
  <div class="min-h-screen bg-fog pb-20">
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <button
          @click="router.back()"
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
          뒤로가기
        </button>
        <h1 class="text-body font-semibold tracking-body text-ink">예약 진행</h1>
        <div class="w-16"></div>
        <!-- spacer -->
      </div>
    </header>
    <main class="max-w-[1200px] mx-auto px-6 mt-12" v-if="carData">
      <h1 class="text-heading font-bold tracking-heading text-ink mb-8">차량 렌트 예약</h1>
      <div class="flex flex-col lg:flex-row gap-12">
        <!-- Left: Form Sections -->
        <div class="flex-1 space-y-8 min-w-0">
          <!-- 1. Date Selection -->
          <div class="bg-snow rounded-cards p-[28px]">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-6">
              1. 일정 선택
            </h2>
            <DateRangePicker
              v-model:startDate="startDateTime"
              v-model:endDate="endDateTime"
              :bookedRanges="bookedRanges"
            />
            <div
              class="mt-4 flex gap-4 text-body-sm bg-fog p-4 rounded-lg border border-silver-mist"
            >
              <div class="flex-1">
                <div class="text-caption text-graphite mb-1">대여 시작일</div>
                <div class="font-bold text-ink">{{ startDateTime || '날짜를 선택하세요' }}</div>
              </div>
              <div class="w-px bg-silver-mist"></div>
              <div class="flex-1">
                <div class="text-caption text-graphite mb-1">반납 예정일</div>
                <div class="font-bold text-ink">{{ endDateTime || '날짜를 선택하세요' }}</div>
              </div>
            </div>
          </div>
          <!-- 2. Insurance Options -->
          <div class="bg-snow rounded-cards p-[32px] md:p-[40px] shadow-sm">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-8">
              2. 보험 옵션
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <label class="cursor-pointer group">
                <input
                  type="radio"
                  value="standard"
                  v-model="insuranceOption"
                  class="peer sr-only"
                />
                <div
                  class="h-full border-2 border-silver-mist rounded-2xl p-6 peer-checked:border-azure peer-checked:bg-azure/5 transition-all duration-300 group-hover:border-azure/40 group-hover:shadow-md"
                >
                  <div class="flex justify-between items-center mb-3">
                    <span class="font-bold text-body text-ink">일반 자차</span>
                    <span
                      class="text-body-sm font-medium text-graphite bg-silver-mist/30 px-3 py-1 rounded-full"
                      >포함</span
                    >
                  </div>
                  <p class="text-body-sm text-graphite leading-relaxed">자기부담금 최대 50만원</p>
                </div>
              </label>
              <label class="cursor-pointer group">
                <input
                  type="radio"
                  value="premium"
                  v-model="insuranceOption"
                  class="peer sr-only"
                />
                <div
                  class="h-full border-2 border-silver-mist rounded-2xl p-6 peer-checked:border-azure peer-checked:bg-azure/5 transition-all duration-300 group-hover:border-azure/40 group-hover:shadow-md"
                >
                  <div class="flex justify-between items-center mb-3">
                    <span class="font-bold text-body text-ink">완전 자차</span>
                    <span
                      class="text-body-sm text-azure font-bold bg-azure/10 px-3 py-1 rounded-full"
                      >+ ₩20,000/일</span
                    >
                  </div>
                  <p class="text-body-sm text-graphite leading-relaxed">
                    자기부담금 전액 면제 (0원)
                  </p>
                </div>
              </label>
            </div>
          </div>
          <!-- 3. Add-ons -->
          <div class="bg-snow rounded-cards p-[32px] md:p-[40px] shadow-sm">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-8">
              3. 추가 캠핑 용품
            </h2>
            <div class="space-y-5">
              <label
                class="flex items-center justify-between p-6 border-2 border-silver-mist rounded-2xl cursor-pointer hover:border-azure/40 hover:bg-fog transition-all duration-300 group hover:shadow-md"
              >
                <div class="flex items-center gap-6">
                  <div class="relative flex items-center justify-center">
                    <input
                      type="checkbox"
                      v-model="addCampingSet"
                      class="w-6 h-6 text-azure bg-white border-2 border-silver-mist rounded-md focus:ring-azure focus:ring-offset-2 transition-colors cursor-pointer group-hover:border-azure/60"
                    />
                  </div>
                  <div>
                    <span class="block font-bold text-body text-ink mb-1"
                      >캠핑 의자 & 테이블 세트</span
                    >
                    <span class="block text-body-sm text-graphite"
                      >4인용 폴딩 테이블과 릴렉스 체어 4개</span
                    >
                  </div>
                </div>
                <span
                  class="text-body font-bold text-ink bg-silver-mist/20 px-4 py-2 rounded-xl group-hover:bg-white transition-colors"
                  >+ ₩30,000</span
                >
              </label>
              <label
                class="flex items-center justify-between p-6 border-2 border-silver-mist rounded-2xl cursor-pointer hover:border-azure/40 hover:bg-fog transition-all duration-300 group hover:shadow-md"
              >
                <div class="flex items-center gap-6">
                  <div class="relative flex items-center justify-center">
                    <input
                      type="checkbox"
                      v-model="addBbqGrill"
                      class="w-6 h-6 text-azure bg-white border-2 border-silver-mist rounded-md focus:ring-azure focus:ring-offset-2 transition-colors cursor-pointer group-hover:border-azure/60"
                    />
                  </div>
                  <div>
                    <span class="block font-bold text-body text-ink mb-1">BBQ 그릴 세트</span>
                    <span class="block text-body-sm text-graphite"
                      >화로대, 숯, 석쇠, 집게 포함</span
                    >
                  </div>
                </div>
                <span
                  class="text-body font-bold text-ink bg-silver-mist/20 px-4 py-2 rounded-xl group-hover:bg-white transition-colors"
                  >+ ₩15,000</span
                >
              </label>
            </div>
          </div>
          <!-- 4. 쿠폰 적용 -->
          <div class="bg-snow rounded-cards p-[32px] md:p-[40px] shadow-sm">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-6">
              4. 쿠폰 적용
            </h2>
            <div class="space-y-4">
              <div v-if="coupons.length === 0" class="text-graphite text-body-sm bg-fog p-4 rounded-xl border border-silver-mist">
                사용 가능한 쿠폰이 없습니다.
              </div>
              <div v-else class="space-y-4">
                <label class="block text-caption font-semibold text-graphite mb-1 ml-1">보유 쿠폰 선택</label>
                <select 
                  v-model="selectedUserCouponId" 
                  class="w-full bg-silver-mist/50 border-0 rounded-lg px-4 py-3 text-body text-ink focus:ring-2 focus:ring-azure focus:bg-snow transition-all cursor-pointer"
                >
                  <option :value="null">쿠폰을 적용하지 않음</option>
                  <option 
                    v-for="coupon in coupons" 
                    :key="coupon.userCouponId" 
                    :value="coupon.userCouponId"
                    :disabled="preCouponTotal < coupon.minOrderAmount"
                  >
                    {{ coupon.couponName }} 
                    ({{ coupon.discountType === 'percent' ? `${coupon.discountValue}%` : `₩${coupon.discountValue.toLocaleString()}` }} 할인)
                    {{ preCouponTotal < coupon.minOrderAmount ? ` (최소 결제액 ₩${coupon.minOrderAmount.toLocaleString()} 미달)` : '' }}
                  </option>
                </select>
                
                <!-- Selected Coupon Details Card -->
                <div 
                  v-if="selectedUserCoupon" 
                  class="flex items-center justify-between p-5 bg-azure/5 border border-azure/20 rounded-2xl animate-fade-in"
                >
                  <div>
                    <span class="block font-bold text-body text-azure">{{ selectedUserCoupon.couponName }}</span>
                    <span class="block text-caption text-graphite mt-1">
                      코드: {{ selectedUserCoupon.couponCode }} 
                      <span v-if="selectedUserCoupon.minOrderAmount > 0"> • ₩{{ selectedUserCoupon.minOrderAmount.toLocaleString() }} 이상 결제 시 사용 가능</span>
                    </span>
                  </div>
                  <div class="text-right">
                    <span class="text-heading-sm font-bold text-azure">- ₩{{ couponDiscount.toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- Right: Receipt / Cost Summary -->
        <div class="lg:w-[400px] flex-shrink-0">
          <div class="bg-ink text-snow p-[32px] rounded-[32px] sticky top-[100px]">
            <h3
              class="text-subheading font-bold tracking-subheading mb-6 border-b border-snow/20 pb-4"
            >
              {{ carData.carName }}
            </h3>
            <div class="space-y-4 mb-6 text-body-sm text-snow/80">
              <div class="flex justify-between items-center">
                <span>차량 렌트비 ({{ rentalDays }}일)</span>
                <span class="text-snow">₩{{ baseCost.toLocaleString() }}</span>
              </div>
              <div class="flex justify-between items-center" v-if="insuranceCost > 0">
                <span>완전 자차 보험</span>
                <span class="text-snow">₩{{ insuranceCost.toLocaleString() }}</span>
              </div>
              <div class="flex justify-between items-center" v-if="addonsCost > 0">
                <span>추가 옵션</span>
                <span class="text-snow">₩{{ addonsCost.toLocaleString() }}</span>
              </div>
              <div class="flex justify-between items-center text-azure font-semibold" v-if="couponDiscount > 0">
                <span>쿠폰 할인 ({{ selectedUserCoupon?.couponCode }})</span>
                <span>- ₩{{ couponDiscount.toLocaleString() }}</span>
              </div>
            </div>
            <div class="border-t border-snow/20 pt-4 mb-8">
              <div class="text-caption text-snow/60 mb-1">총 결제 금액</div>
              <div class="text-heading-sm font-bold tracking-heading-sm text-snow">
                ₩{{ totalCost.toLocaleString() }}
              </div>
            </div>
            <p class="text-[11px] text-snow/50 mb-4 text-center leading-relaxed">
              결제 시 캠핑카 대여 약관 및 취소/환불 규정에 동의하는 것으로 간주됩니다.
            </p>
            <button
              @click="handleRentalSubmit"
              :disabled="isSubmitting || rentalDays <= 0"
              :class="[
                'w-full py-4 rounded-buttons text-body font-bold transition-transform',
                isSubmitting || rentalDays <= 0
                  ? 'bg-snow/20 text-snow/50 cursor-not-allowed'
                  : 'bg-azure text-snow active:scale-[0.98] hover:brightness-110',
              ]"
            >
              {{ isSubmitting ? '예약 중...' : '결제 및 예약하기' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>