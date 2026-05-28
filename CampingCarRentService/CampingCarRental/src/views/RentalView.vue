<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { createRental, getCarRentals, getUserInfo, getMyCoupons } from '../api/rental';
import DateRangePicker from '../components/DateRangePicker.vue';
import { formatToKoreanDate } from '../utils/date';

const router = useRouter();
const carData = history.state.car;
const companyId = history.state.companyId;

// If user navigates directly without state, redirect back
if (!carData) {
  alert('잘못된 접근입니다.');
  router.replace('/main');
}

const startDateTime = ref(history.state?.startDate || '');
const endDateTime = ref(history.state?.endDate || '');
const isSubmitting = ref(false);
const bookedRanges = ref([]);

const coupons = ref([]);
const selectedCoupon = ref(null);
const userLicense = ref('');
const isCouponsLoading = ref(false);

const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('Failed to parse JWT token:', e);
    return null;
  }
};

onMounted(async () => {
  window.scrollTo(0, 0);
  if (carData?.carId) {
    try {
      const response = await getCarRentals(carData.carId);
      if (response.data) {
        bookedRanges.value = response.data.map(rental => ({
          start: rental.startDateTime,
          end: rental.endDateTime
        }));
      }
    } catch (error) {
      console.error('Failed to load existing rentals:', error);
    }
  }

  // Load user coupons using license
  const token = localStorage.getItem('token');
  if (token) {
    const parsed = parseJwt(token);
    if (parsed && parsed.username) {
      try {
        isCouponsLoading.value = true;
        const userRes = await getUserInfo(parsed.username);
        if (userRes.data && userRes.data.license) {
          userLicense.value = userRes.data.license;
          const couponsRes = await getMyCoupons(userLicense.value);
          coupons.value = couponsRes.data || [];
        }
      } catch (error) {
        console.error('Failed to load user license or coupons:', error);
      } finally {
        isCouponsLoading.value = false;
      }
    }
  }
});

// Mock add-ons
const insuranceOption = ref('standard');
const addCampingSet = ref(false);
const addBbqGrill = ref(false);

const calculateDays = (start, end) => {
  if (!start || !end) return 0;
  const startDate = new Date(start);
  const endDate = new Date(end);
  const diffTime = endDate - startDate;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
};

const rentalDays = computed(() => calculateDays(startDateTime.value, endDateTime.value));

const baseCost = computed(() => rentalDays.value * (carData?.carRentalCost || 0));
const insuranceCost = computed(() => {
  if (insuranceOption.value === 'premium') return rentalDays.value * 20000;
  return 0; // Standard is included
});
const addonsCost = computed(() => {
  let cost = 0;
  if (addCampingSet.value) cost += 30000;
  if (addBbqGrill.value) cost += 15000;
  return cost;
});

const originalCost = computed(() => baseCost.value + insuranceCost.value + addonsCost.value);

const isMinOrderAmountSatisfied = computed(() => {
  if (!selectedCoupon.value) return true;
  return originalCost.value >= (selectedCoupon.value.minOrderAmount || 0);
});

const rawDiscountAmount = computed(() => {
  if (!selectedCoupon.value) return 0;
  if (!isMinOrderAmountSatisfied.value) return 0;
  
  if (selectedCoupon.value.discountType === 'PERCENT') {
    return Math.floor(originalCost.value * (selectedCoupon.value.discountValue / 100));
  } else if (selectedCoupon.value.discountType === 'FIXED') {
    return selectedCoupon.value.discountValue || 0;
  }
  return 0;
});

const isMaxDiscountExceeded = computed(() => {
  if (!selectedCoupon.value) return false;
  if (selectedCoupon.value.discountType !== 'PERCENT') return false;
  const maxLimit = selectedCoupon.value.maxDiscountAmount;
  return maxLimit && maxLimit > 0 && rawDiscountAmount.value > maxLimit;
});

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0;
  if (!isMinOrderAmountSatisfied.value) return 0;
  
  let discount = rawDiscountAmount.value;
  const maxLimit = selectedCoupon.value.maxDiscountAmount;
  if (maxLimit && maxLimit > 0 && discount > maxLimit) {
    discount = maxLimit;
  }
  return Math.min(discount, originalCost.value);
});

const finalCost = computed(() => Math.max(0, originalCost.value - discountAmount.value));

const handleRentalSubmit = async () => {
  if (rentalDays.value <= 0) {
    alert('반납일은 대여일보다 이후여야 합니다.');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    const payload = {
      carId: carData.carId,
      companyId: companyId,
      startDateTime: `${startDateTime.value}T00:00:00`,
      endDateTime: `${endDateTime.value}T23:59:59`,
      totalCost: finalCost.value,
      couponId: selectedCoupon.value ? selectedCoupon.value.userCouponId : null
    };
    
    await createRental(payload);
    alert('렌트 예약이 성공적으로 완료되었습니다!');
    router.push('/mypage');
  } catch (error) {
    console.error('Rental failed:', error);
    alert('예약 요청에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <button @click="router.back()" class="text-cobalt-link text-body-sm font-medium hover:underline flex items-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
          뒤로가기
        </button>
        <h1 class="text-body font-semibold tracking-body text-ink">예약 진행</h1>
        <div class="w-16"></div> <!-- spacer -->
      </div>
    </header>

    <main class="max-w-[1200px] mx-auto px-6 mt-12" v-if="carData">
      <h1 class="text-heading font-bold tracking-heading text-ink mb-8">차량 렌트 예약</h1>
      
      <div class="flex flex-col lg:flex-row gap-12">
        
        <!-- Left: Form Sections -->
        <div class="flex-1 space-y-8 min-w-0">
          
          <!-- 1. Date Selection -->
          <div class="bg-snow rounded-cards p-[28px]">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-6">1. 일정 선택</h2>
            <DateRangePicker 
              v-model:startDate="startDateTime" 
              v-model:endDate="endDateTime" 
              :bookedRanges="bookedRanges"
            />
            
            <div class="mt-4 flex gap-4 text-body-sm bg-fog p-4 rounded-lg border border-silver-mist">
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
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-8">2. 보험 옵션</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <label class="cursor-pointer group">
                <input type="radio" value="standard" v-model="insuranceOption" class="peer sr-only" />
                <div class="h-full border-2 border-silver-mist rounded-2xl p-6 peer-checked:border-azure peer-checked:bg-azure/5 transition-all duration-300 group-hover:border-azure/40 group-hover:shadow-md">
                  <div class="flex justify-between items-center mb-3">
                    <span class="font-bold text-body text-ink">일반 자차</span>
                    <span class="text-body-sm font-medium text-graphite bg-silver-mist/30 px-3 py-1 rounded-full">포함</span>
                  </div>
                  <p class="text-body-sm text-graphite leading-relaxed">자기부담금 최대 50만원</p>
                </div>
              </label>
              <label class="cursor-pointer group">
                <input type="radio" value="premium" v-model="insuranceOption" class="peer sr-only" />
                <div class="h-full border-2 border-silver-mist rounded-2xl p-6 peer-checked:border-azure peer-checked:bg-azure/5 transition-all duration-300 group-hover:border-azure/40 group-hover:shadow-md">
                  <div class="flex justify-between items-center mb-3">
                    <span class="font-bold text-body text-ink">완전 자차</span>
                    <span class="text-body-sm text-azure font-bold bg-azure/10 px-3 py-1 rounded-full">+ ₩20,000/일</span>
                  </div>
                  <p class="text-body-sm text-graphite leading-relaxed">자기부담금 전액 면제 (0원)</p>
                </div>
              </label>
            </div>
          </div>

          <!-- 3. Add-ons -->
          <div class="bg-snow rounded-cards p-[32px] md:p-[40px] shadow-sm">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-8">3. 추가 캠핑 용품</h2>
            <div class="space-y-5">
              <label class="flex items-center justify-between p-6 border-2 border-silver-mist rounded-2xl cursor-pointer hover:border-azure/40 hover:bg-fog transition-all duration-300 group hover:shadow-md">
                <div class="flex items-center gap-6">
                  <div class="relative flex items-center justify-center">
                    <input type="checkbox" v-model="addCampingSet" class="w-6 h-6 text-azure bg-white border-2 border-silver-mist rounded-md focus:ring-azure focus:ring-offset-2 transition-colors cursor-pointer group-hover:border-azure/60" />
                  </div>
                  <div>
                    <span class="block font-bold text-body text-ink mb-1">캠핑 의자 & 테이블 세트</span>
                    <span class="block text-body-sm text-graphite">4인용 폴딩 테이블과 릴렉스 체어 4개</span>
                  </div>
                </div>
                <span class="text-body font-bold text-ink bg-silver-mist/20 px-4 py-2 rounded-xl group-hover:bg-white transition-colors">+ ₩30,000</span>
              </label>
              
              <label class="flex items-center justify-between p-6 border-2 border-silver-mist rounded-2xl cursor-pointer hover:border-azure/40 hover:bg-fog transition-all duration-300 group hover:shadow-md">
                <div class="flex items-center gap-6">
                  <div class="relative flex items-center justify-center">
                    <input type="checkbox" v-model="addBbqGrill" class="w-6 h-6 text-azure bg-white border-2 border-silver-mist rounded-md focus:ring-azure focus:ring-offset-2 transition-colors cursor-pointer group-hover:border-azure/60" />
                  </div>
                  <div>
                    <span class="block font-bold text-body text-ink mb-1">BBQ 그릴 세트</span>
                    <span class="block text-body-sm text-graphite">화로대, 숯, 석쇠, 집게 포함</span>
                  </div>
                </div>
                <span class="text-body font-bold text-ink bg-silver-mist/20 px-4 py-2 rounded-xl group-hover:bg-white transition-colors">+ ₩15,000</span>
              </label>
            </div>
          </div>

          <!-- 4. 쿠폰 적용 -->
          <div class="bg-snow rounded-cards p-[32px] md:p-[40px] shadow-sm">
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-8">4. 쿠폰 적용</h2>
            
            <div v-if="isCouponsLoading" class="text-graphite py-4 flex items-center gap-2 text-body-sm">
              <svg class="animate-spin h-5 w-5 text-azure" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
              </svg>
              쿠폰 정보를 불러오는 중입니다...
            </div>
            
            <div v-else-if="coupons.length === 0" class="text-graphite text-body-sm py-8 bg-fog rounded-2xl border-2 border-dashed border-silver-mist text-center">
              사용 가능한 쿠폰이 없습니다.
            </div>
            
            <div v-else class="space-y-4">
              <!-- No Coupon Option -->
              <label 
                class="flex items-center justify-between p-5 border-2 rounded-2xl cursor-pointer transition-all duration-300 group hover:shadow-md"
                :class="[!selectedCoupon ? 'border-azure bg-azure/5' : 'border-silver-mist hover:border-azure/40 hover:bg-fog']"
              >
                <div class="flex items-center gap-4">
                  <input type="radio" :value="null" v-model="selectedCoupon" class="w-5 h-5 text-azure border-silver-mist focus:ring-azure cursor-pointer" />
                  <div>
                    <span class="block font-bold text-body text-ink">쿠폰 적용 안 함</span>
                    <span class="block text-caption text-graphite">쿠폰을 사용하지 않고 원래 금액으로 예약합니다.</span>
                  </div>
                </div>
              </label>

              <!-- Coupon List Options -->
              <label 
                v-for="coupon in coupons" 
                :key="coupon.userCouponId"
                class="flex flex-col p-5 border-2 rounded-2xl cursor-pointer transition-all duration-300 group hover:shadow-md relative"
                :class="[
                  selectedCoupon?.userCouponId === coupon.userCouponId 
                    ? 'border-azure bg-azure/5' 
                    : 'border-silver-mist hover:border-azure/40 hover:bg-fog'
                ]"
              >
                <div class="flex items-start justify-between">
                  <div class="flex items-start gap-4">
                    <input 
                      type="radio" 
                      :value="coupon" 
                      v-model="selectedCoupon" 
                      class="mt-1 w-5 h-5 text-azure border-silver-mist focus:ring-azure cursor-pointer" 
                    />
                    <div>
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-bold text-body text-ink">{{ coupon.name }}</span>
                        <span class="text-caption font-bold bg-azure/10 text-azure px-2 py-0.5 rounded-full">
                          {{ coupon.discountType === 'PERCENT' ? `${coupon.discountValue}%` : `₩${coupon.discountValue.toLocaleString()}` }}
                        </span>
                      </div>
                      
                      <div class="text-caption text-graphite space-y-1 mt-1">
                        <div v-if="coupon.minOrderAmount > 0">
                          • 최소 결제금액: ₩{{ coupon.minOrderAmount.toLocaleString() }}
                        </div>
                        <div v-if="coupon.maxDiscountAmount > 0">
                          • 최대 할인금액: ₩{{ coupon.maxDiscountAmount.toLocaleString() }}
                        </div>
                        <div>
                          • 사용 기한: ~{{ formatToKoreanDate(coupon.endDate) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Warnings and applied state -->
                <div 
                  v-if="selectedCoupon?.userCouponId === coupon.userCouponId" 
                  class="mt-3 pl-9 border-t border-silver-mist/40 pt-3 text-caption space-y-1.5"
                >
                  <!-- Min Order Amount Error -->
                  <div v-if="!isMinOrderAmountSatisfied" class="text-caution font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                    </svg>
                    최소 결제 금액(₩{{ coupon.minOrderAmount.toLocaleString() }}) 미달로 쿠폰을 사용할 수 없습니다.
                  </div>
                  
                  <!-- Max Discount limit warning -->
                  <div v-else-if="isMaxDiscountExceeded" class="text-caution font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                    </svg>
                    최대 할인 금액(₩{{ coupon.maxDiscountAmount.toLocaleString() }})을 초과하여 최대 금액까지만 할인 적용됩니다.
                  </div>
                  
                  <!-- Success Applied -->
                  <div v-else class="text-emerald-500 font-semibold flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    쿠폰이 올바르게 적용되었습니다! (-₩{{ discountAmount.toLocaleString() }})
                  </div>
                </div>
              </label>
            </div>
          </div>

        </div>

        <!-- Right: Receipt / Cost Summary -->
        <div class="lg:w-[400px] flex-shrink-0">
          <div class="bg-ink text-snow p-[32px] rounded-[32px] sticky top-[100px]">
            <h3 class="text-subheading font-bold tracking-subheading mb-6 border-b border-snow/20 pb-4">{{ carData.carName }}</h3>
            
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
            </div>
            
            <div class="border-t border-snow/20 pt-4 mb-8">
              <div v-if="selectedCoupon && discountAmount > 0" class="space-y-2 mb-4">
                <div class="flex justify-between items-center text-caption text-snow/60">
                  <span>선택 상품 금액</span>
                  <span>₩{{ originalCost.toLocaleString() }}</span>
                </div>
                <div class="flex justify-between items-center text-caption text-emerald-400 font-semibold">
                  <span>쿠폰 할인</span>
                  <span>- ₩{{ discountAmount.toLocaleString() }}</span>
                </div>
              </div>
              <div class="text-caption text-snow/60 mb-1">최종 결제 금액</div>
              <div class="text-heading-sm font-bold tracking-heading-sm text-snow">₩{{ finalCost.toLocaleString() }}</div>
            </div>
            
            <p class="text-[11px] text-snow/50 mb-4 text-center leading-relaxed">
              결제 시 캠핑카 대여 약관 및 취소/환불 규정에 동의하는 것으로 간주됩니다.
            </p>
            
            <button 
              @click="handleRentalSubmit" 
              :disabled="isSubmitting || rentalDays <= 0"
              :class="[
                'w-full py-4 rounded-buttons text-body font-bold transition-transform',
                (isSubmitting || rentalDays <= 0) ? 'bg-snow/20 text-snow/50 cursor-not-allowed' : 'bg-azure text-snow active:scale-[0.98] hover:brightness-110'
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
