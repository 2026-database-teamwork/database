<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getMyRentals, getUserInfo, getMyCoupons } from '../api/rental';
import { formatToKoreanDateTime, formatToKoreanDate } from '../utils/date';

const router = useRouter();
const rentals = ref([]);
const isLoading = ref(true);

const activeTab = ref('rentals'); // 'rentals' or 'coupons'
const coupons = ref([]);
const isCouponsLoading = ref(false);
const userLicense = ref('');

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
  // Load rentals
  try {
    const response = await getMyRentals();
    rentals.value = response.data || [];
  } catch (error) {
    console.error('Failed to fetch rentals:', error);
    alert('예약 내역을 불러오는데 실패했습니다.');
  } finally {
    isLoading.value = false;
  }

  // Load coupons
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
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <button @click="router.push('/main')" class="text-cobalt-link text-body-sm font-medium hover:underline flex items-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
          메인으로
        </button>
        <h1 class="text-body font-semibold tracking-body text-ink">마이페이지</h1>
        <div class="w-20"></div> <!-- spacer -->
      </div>
    </header>

    <main class="max-w-[800px] mx-auto px-6 mt-12">
      <h1 class="text-heading font-bold tracking-heading text-ink mb-2">
        {{ activeTab === 'rentals' ? '마이페이지' : '나의 쿠폰함' }}
      </h1>
      <p class="text-subheading tracking-subheading text-graphite mb-8">
        {{ activeTab === 'rentals' ? '나의 캠핑카 예약 내역을 확인하세요.' : '내가 보유하고 있는 할인 쿠폰 목록입니다.' }}
      </p>

      <!-- Premium Tab Switcher -->
      <div class="flex border-b border-silver-mist mb-8">
        <button 
          @click="activeTab = 'rentals'"
          :class="[
            'flex-1 py-4 text-center font-semibold text-body transition-all border-b-2 outline-none',
            activeTab === 'rentals' 
              ? 'border-azure text-azure font-bold' 
              : 'border-transparent text-graphite hover:text-ink'
          ]"
        >
          예약 내역 ({{ rentals.length }})
        </button>
        <button 
          @click="activeTab = 'coupons'"
          :class="[
            'flex-1 py-4 text-center font-semibold text-body transition-all border-b-2 outline-none',
            activeTab === 'coupons' 
              ? 'border-azure text-azure font-bold' 
              : 'border-transparent text-graphite hover:text-ink'
          ]"
        >
          보유 쿠폰 ({{ coupons.length }})
        </button>
      </div>

      <!-- Tab Content: Rentals -->
      <div v-if="activeTab === 'rentals'" class="space-y-6">
        <div v-if="isLoading" class="text-graphite py-8 text-center">불러오는 중...</div>
        <div v-else-if="rentals.length === 0" class="bg-snow rounded-cards p-[28px] text-center shadow-sm border border-silver-mist">
          <div class="text-4xl mb-4">🚐</div>
          <h3 class="text-subheading font-bold text-ink mb-2">예약된 내역이 없습니다</h3>
          <p class="text-body-sm text-graphite mb-6">첫 캠핑카 여행을 예약해보세요!</p>
          <button @click="router.push('/main')" class="bg-azure text-snow px-6 py-2 rounded-buttons text-body-sm font-medium hover:bg-cobalt-link transition-colors">
            차량 둘러보기
          </button>
        </div>

        <div v-else class="space-y-6">
          <div v-for="(rental, index) in rentals" :key="index" class="bg-snow rounded-cards p-[28px] flex flex-col md:flex-row justify-between gap-6 shadow-sm border border-silver-mist">
            <div>
              <div class="flex items-center gap-3 mb-2">
                <span class="bg-silver-mist/50 text-ink text-caption font-semibold px-3 py-1 rounded-full">예약완료</span>
              </div>
              <h3 class="text-subheading font-bold tracking-subheading text-ink mb-1">{{ rental.carName }}</h3>
              <p class="text-body-sm text-graphite mb-4">{{ rental.companyName }} | 차량번호: {{ rental.license }}</p>
              
              <div class="flex flex-col gap-1 text-body-sm text-ink bg-fog p-4 rounded-lg border border-silver-mist/50">
                <div><span class="font-semibold text-graphite mr-2">대여일시</span> {{ formatToKoreanDateTime(rental.startDateTime) }}</div>
                <div><span class="font-semibold text-graphite mr-2">반납일시</span> {{ formatToKoreanDateTime(rental.endDateTime) }}</div>
              </div>
            </div>
            
            <div class="flex flex-col justify-end md:text-right border-t border-silver-mist md:border-0 pt-4 md:pt-0 mt-2 md:mt-0">
              <span class="text-body-sm text-graphite mb-1">총 결제 금액</span>
              <span class="text-heading-sm font-bold tracking-heading-sm text-azure">₩{{ rental.totalCost.toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab Content: Coupons -->
      <div v-if="activeTab === 'coupons'">
        <div v-if="isCouponsLoading" class="text-graphite py-8 text-center flex justify-center items-center gap-2">
          <svg class="animate-spin h-5 w-5 text-azure" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
          </svg>
          쿠폰을 불러오는 중...
        </div>
        <div v-else-if="coupons.length === 0" class="bg-snow rounded-cards p-[28px] text-center border-2 border-dashed border-silver-mist">
          <div class="text-4xl mb-4">🎫</div>
          <h3 class="text-subheading font-bold text-ink mb-2">보유하고 있는 쿠폰이 없습니다</h3>
          <p class="text-body-sm text-graphite">아직 발급받은 쿠폰이 없습니다.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6 animate-fade-in">
          <div 
            v-for="coupon in coupons" 
            :key="coupon.userCouponId"
            class="relative bg-snow border border-silver-mist/80 rounded-xl overflow-hidden p-6 flex flex-col justify-between shadow-sm transition-all duration-300 hover:shadow-md group"
          >
            <!-- Top and Bottom ticket cutouts (notches) -->
            <div class="absolute left-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-fog border-r border-silver-mist/80"></div>
            <div class="absolute right-[-10px] top-1/2 -translate-y-1/2 w-5 h-5 rounded-full bg-fog border-l border-silver-mist/80"></div>
            
            <div class="flex justify-between items-start gap-4">
              <div>
                <span class="text-caption font-semibold uppercase tracking-wider text-graphite block mb-1">Coupon</span>
                <h3 class="text-body font-bold text-ink group-hover:text-azure transition-colors">{{ coupon.name }}</h3>
              </div>
              <div class="text-right">
                <span class="text-heading-sm font-bold text-azure block leading-none">
                  {{ coupon.discountType === 'PERCENT' ? `${coupon.discountValue}%` : `₩${coupon.discountValue.toLocaleString()}` }}
                </span>
                <span class="text-caption font-semibold bg-azure/10 text-azure px-2 py-0.5 rounded-full inline-block mt-2">
                  {{ coupon.discountType === 'PERCENT' ? 'Percentage' : 'Fixed Amount' }}
                </span>
              </div>
            </div>
            
            <!-- Dashed divider -->
            <div class="border-t border-dashed border-silver-mist/80 my-5"></div>
            
            <div class="flex justify-between items-end text-caption text-graphite">
              <div class="space-y-1">
                <div v-if="coupon.minOrderAmount > 0" class="flex items-center gap-1.5 text-[13px]">
                  <span class="w-1.5 h-1.5 bg-silver-mist rounded-full"></span>
                  최소결제: ₩{{ coupon.minOrderAmount.toLocaleString() }}
                </div>
                <div v-if="coupon.maxDiscountAmount > 0" class="flex items-center gap-1.5 text-[13px]">
                  <span class="w-1.5 h-1.5 bg-silver-mist rounded-full"></span>
                  최대할인: ₩{{ coupon.maxDiscountAmount.toLocaleString() }}
                </div>
                <div class="flex items-center gap-1.5 text-[13px]">
                  <span class="w-1.5 h-1.5 bg-silver-mist rounded-full"></span>
                  사용기한: ~{{ formatToKoreanDate(coupon.endDate) }}
                </div>
              </div>
              
              <span 
                :class="[
                  'text-[12px] font-bold px-2.5 py-1 rounded border transition-colors',
                  coupon.status === 'ACTIVE' 
                    ? 'text-emerald-600 bg-emerald-50 border-emerald-200' 
                    : 'text-graphite bg-silver-mist/50 border-silver-mist'
                ]"
              >
                {{ coupon.status === 'ACTIVE' ? '사용 가능' : coupon.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
