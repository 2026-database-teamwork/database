<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getCompaniesByRegion, getCarsByCompany } from '../api/company';
import { getCarRentals } from '../api/rental';
import DateRangePicker from '../components/DateRangePicker.vue';

const router = useRouter();
const regions = ['서울', '대전', '제주', '경기', '부산'];
const selectedRegion = ref('');
const companies = ref([]);
const selectedCompany = ref(null);
const cars = ref([]);
const rawCars = ref([]);
const loadingCompanies = ref(false);
const loadingCars = ref(false);

const selectedStartDate = ref('');
const selectedEndDate = ref('');
const isDatePickerOpen = ref(false);

const heroVideo = ref(null);
const heroContainer = ref(null);

let animationFrameId = null;
let targetTime = 0;
let currentVideoTime = 0;

const handleScroll = () => {
  if (!heroVideo.value || !heroContainer.value) return;
  const rect = heroContainer.value.getBoundingClientRect();
  const offsetTop = 52; // Header height
  const maxScroll = rect.height - window.innerHeight + offsetTop;
  let progress = -(rect.top - offsetTop) / maxScroll;
  progress = Math.max(0, Math.min(1, progress));
  
  if (heroVideo.value.duration) {
    targetTime = heroVideo.value.duration * progress;
  }
};

const renderLoop = () => {
  if (heroVideo.value && !isNaN(targetTime)) {
    // Lerp (선형 보간)을 사용하여 현재 시간을 목표 시간으로 부드럽게 이동시킵니다.
    currentVideoTime += (targetTime - currentVideoTime) * 0.1; // 0.1은 보간 속도 (수정 가능)
    
    // 비디오의 currentTime 업데이트 (성능을 위해 의미있는 변화가 있을 때만)
    if (Math.abs(currentVideoTime - heroVideo.value.currentTime) > 0.05) {
      heroVideo.value.currentTime = currentVideoTime;
    }
  }
  animationFrameId = requestAnimationFrame(renderLoop);
};

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true });
  renderLoop();
  
  // Restore date states
  selectedStartDate.value = sessionStorage.getItem('selectedStartDate') || '';
  selectedEndDate.value = sessionStorage.getItem('selectedEndDate') || '';
  
  // Restore region and company state
  const savedRegion = sessionStorage.getItem('selectedRegion');
  if (savedRegion) {
    selectedRegion.value = savedRegion;
    loadingCompanies.value = true;
    try {
      const response = await getCompaniesByRegion(savedRegion);
      companies.value = response.data || [];
      
      const savedCompanyStr = sessionStorage.getItem('selectedCompany');
      if (savedCompanyStr) {
        selectedCompany.value = JSON.parse(savedCompanyStr);
        await fetchCarsForSelectedCompany();
      }
    } catch (error) {
      console.error('Failed to restore company state:', error);
    } finally {
      loadingCompanies.value = false;
    }
  }
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
  }
});

// Reactively save states to sessionStorage
watch(selectedStartDate, (newVal) => {
  sessionStorage.setItem('selectedStartDate', newVal);
});

watch(selectedEndDate, (newVal) => {
  sessionStorage.setItem('selectedEndDate', newVal);
});

watch(selectedRegion, (newVal) => {
  sessionStorage.setItem('selectedRegion', newVal);
});

watch(selectedCompany, (newVal) => {
  if (newVal) {
    sessionStorage.setItem('selectedCompany', JSON.stringify(newVal));
  } else {
    sessionStorage.removeItem('selectedCompany');
  }
});

watch([selectedStartDate, selectedEndDate], () => {
  if (selectedCompany.value) {
    applyCarDateFiltering();
  }
});

const selectRegion = async (region) => {
  selectedRegion.value = region;
  selectedCompany.value = null;
  cars.value = [];
  loadingCompanies.value = true;
  
  try {
    const response = await getCompaniesByRegion(region);
    companies.value = response.data || [];
  } catch (error) {
    console.error('Failed to fetch companies:', error);
    alert('회사 목록을 불러오는데 실패했습니다.');
  } finally {
    loadingCompanies.value = false;
  }
};

const selectCompany = async (company) => {
  selectedCompany.value = company;
  await fetchCarsForSelectedCompany();
};

const fetchCarsForSelectedCompany = async () => {
  if (!selectedCompany.value) return;
  loadingCars.value = true;
  
  try {
    const response = await getCarsByCompany(selectedCompany.value.companyId);
    rawCars.value = response.data || [];
    await applyCarDateFiltering();
  } catch (error) {
    console.error('Failed to fetch cars:', error);
    alert('자동차 목록을 불러오는데 실패했습니다.');
  } finally {
    loadingCars.value = false;
  }
};

const applyCarDateFiltering = async () => {
  if (!selectedStartDate.value || !selectedEndDate.value) {
    cars.value = rawCars.value;
    return;
  }
  
  try {
    loadingCars.value = true;
    
    // Fetch rental history for all cars in parallel
    const rentalPromises = rawCars.value.map(car => getCarRentals(car.carId));
    const rentalResults = await Promise.all(rentalPromises);
    
    // Map rental history back to cars
    const carsWithRentals = rawCars.value.map((car, idx) => ({
      ...car,
      rentals: rentalResults[idx].data || []
    }));
    
    // Overlap checking function
    const hasOverlap = (car) => {
      const targetStart = new Date(selectedStartDate.value);
      targetStart.setHours(0, 0, 0, 0);
      const targetEnd = new Date(selectedEndDate.value);
      targetEnd.setHours(23, 59, 59, 999);
      
      for (const rental of car.rentals) {
        const rentalStart = new Date(rental.startDateTime);
        const rentalEnd = new Date(rental.endDateTime);
        
        // Overlap condition: targetStart <= rentalEnd && targetEnd >= rentalStart
        if (targetStart <= rentalEnd && targetEnd >= rentalStart) {
          return true; // Overlaps!
        }
      }
      return false; // Free!
    };
    
    // Filter cars that have no overlapping rentals
    cars.value = carsWithRentals.filter(car => !hasOverlap(car));
  } catch (error) {
    console.error('Failed to filter cars by date:', error);
    alert('날짜 기준 예약 현황을 필터링하는데 실패했습니다.');
    cars.value = rawCars.value; // Fallback to all cars
  } finally {
    loadingCars.value = false;
  }
};

const clearSelectedDates = () => {
  selectedStartDate.value = '';
  selectedEndDate.value = '';
};

const handleLogout = () => {
  localStorage.removeItem('token');
  router.push('/');
};

const goToRental = (car) => {
  router.push({
    name: 'rental',
    state: {
      car: JSON.parse(JSON.stringify(car)), // prevent proxy issues
      companyId: selectedCompany.value.companyId,
      startDate: selectedStartDate.value,
      endDate: selectedEndDate.value
    }
  });
};

const goToRepair = (car) => {
  router.push({
    name: 'repair',
    params: { carId: car.carId },
    state: {
      carName: car.carName,
      carNumber: car.carNumber
    }
  });
};

const getImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
};
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <!-- Header -->
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <h1 class="text-body font-semibold tracking-body text-ink">Camping Car Rental</h1>
        <div class="flex items-center gap-5">
          <button @click="router.push('/mypage')" class="text-ink hover:text-azure transition-colors" title="My Page">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
          </button>
          <button @click="handleLogout" class="text-cobalt-link text-body-sm font-medium hover:underline">Logout</button>
        </div>
      </div>
    </header>

    <!-- Hero Section with Scroll Video -->
    <div class="h-[250vh] relative" ref="heroContainer">
      <div class="sticky top-[52px] h-[calc(100vh-52px)] w-full overflow-hidden bg-ink">
        <video 
          ref="heroVideo"
          src="@/assets/campingcarScrollAnimation.mp4"
          class="w-full h-full object-cover opacity-80"
          muted
          playsinline
          preload="auto"
          @loadedmetadata="handleScroll"
        ></video>
        <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
          <h1 class="text-snow text-5xl md:text-7xl font-bold tracking-heading text-center mb-6 drop-shadow-lg">Start Your Journey</h1>
          <p class="text-snow/90 text-xl md:text-2xl tracking-subheading drop-shadow-md">Scroll to explore</p>
        </div>
      </div>
    </div>

    <main class="max-w-[1200px] mx-auto px-6 mt-12 space-y-16">
      
      <!-- 📅 Airbnb-Style Compact Date Picker Bar -->
      <div class="relative flex flex-col items-center">
        <!-- Floating Glassmorphic Search Bar -->
        <div 
          @click="isDatePickerOpen = true"
          class="w-full max-w-[600px] bg-snow border border-silver-mist shadow-md hover:shadow-lg hover:border-azure/40 rounded-full py-4 px-8 cursor-pointer flex items-center justify-between transition-all duration-300 select-none group"
        >
          <!-- Left side: Select Start Date -->
          <div class="flex-1 flex items-center gap-3">
            <span class="text-xl group-hover:scale-110 transition-transform">🛫</span>
            <div class="flex flex-col text-left">
              <span class="text-[11px] text-graphite font-bold uppercase tracking-wider">대여 시작일</span>
              <span class="text-body-sm font-bold" :class="[selectedStartDate ? 'text-azure' : 'text-slate']">
                {{ selectedStartDate || '날짜 선택' }}
              </span>
            </div>
          </div>
          
          <!-- Middle Divider -->
          <div class="h-8 w-px bg-silver-mist mx-4"></div>
          
          <!-- Right side: Select End Date -->
          <div class="flex-1 flex items-center gap-3">
            <span class="text-xl group-hover:scale-110 transition-transform">🛬</span>
            <div class="flex flex-col text-left">
              <span class="text-[11px] text-graphite font-bold uppercase tracking-wider">반납 예정일</span>
              <span class="text-body-sm font-bold" :class="[selectedEndDate ? 'text-azure' : 'text-slate']">
                {{ selectedEndDate || '날짜 선택' }}
              </span>
            </div>
          </div>
          
          <!-- Reset / Action button inside search bar -->
          <div v-if="selectedStartDate || selectedEndDate" class="ml-4 pl-4 border-l border-silver-mist">
            <button 
              @click.stop="clearSelectedDates" 
              class="bg-silver-mist/50 hover:bg-silver-mist text-ink hover:text-azure px-3.5 py-1.5 rounded-full text-caption font-bold transition-all"
            >
              비우기
            </button>
          </div>
        </div>

        <!-- Backdrop Overlay (Dismiss on click) -->
        <div 
          v-if="isDatePickerOpen" 
          class="fixed inset-0 z-40 bg-ink/10 backdrop-blur-[2px] transition-opacity duration-300"
          @click="isDatePickerOpen = false"
        ></div>

        <!-- Floating Popover Calendar -->
        <div 
          v-if="isDatePickerOpen" 
          class="absolute top-[110%] left-1/2 -translate-x-1/2 w-[95%] max-w-[500px] bg-snow rounded-[28px] border border-silver-mist p-6 shadow-2xl z-50 animate-fade-in-up mt-2"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="text-left">
              <h3 class="text-body font-bold text-ink">대여 일정 선택</h3>
              <p class="text-[11px] text-graphite">원하시는 렌트 시작일과 반납일을 지정해 주세요.</p>
            </div>
            <button 
              @click="isDatePickerOpen = false" 
              class="bg-azure hover:bg-cobalt-link text-snow px-4 py-1.5 rounded-full text-caption font-bold transition-colors"
            >
              선택 완료
            </button>
          </div>
          
          <DateRangePicker 
            v-model:startDate="selectedStartDate"
            v-model:endDate="selectedEndDate"
            :bookedRanges="[]"
          />
        </div>
      </div>
      
      <!-- Region Selector -->
      <section v-if="!selectedCompany">
        <h2 class="text-heading font-bold tracking-heading text-ink mb-6">Select a Region</h2>
        <div class="flex flex-wrap gap-3">
          <button 
            v-for="region in regions" 
            :key="region"
            @click="selectRegion(region)"
            :class="[
              'px-6 py-3 rounded-full text-body font-medium transition-all',
              selectedRegion === region 
                ? 'bg-ink text-snow shadow-md' 
                : 'bg-silver-mist/50 text-ink hover:bg-silver-mist'
            ]"
          >
            {{ region }}
          </button>
        </div>
      </section>

      <!-- Company List -->
      <section v-if="selectedRegion && !selectedCompany">
        <div class="flex items-end justify-between mb-6">
          <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink">Companies in {{ selectedRegion }}</h2>
        </div>
        
        <div v-if="loadingCompanies" class="text-graphite py-8">Loading...</div>
        <div v-else-if="companies.length === 0" class="text-graphite py-8">No companies found in this region.</div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div 
            v-for="company in companies" 
            :key="company.companyId"
            class="bg-snow p-[28px] rounded-cards flex flex-col justify-between group"
          >
            <div>
              <h3 class="text-[28px] font-semibold tracking-[-0.005em] leading-[1.1] text-ink mb-2">{{ company.companyName }}</h3>
              <p class="text-[17px] text-graphite mb-8 leading-[1.47] tracking-[-0.003em]">{{ company.companyAddress }}</p>
            </div>
            <div class="flex items-end justify-between">
               <div class="flex flex-col gap-1 text-[12px] text-graphite">
                 <span>📞 {{ company.companyPhone }}</span>
                 <span>✉️ {{ company.representativeEmail }}</span>
               </div>
               <button @click="selectCompany(company)" class="text-[17px] text-ink flex items-center gap-1 group-hover:text-cobalt-link transition-colors">
                 차량 보기
                 <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
               </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Car List -->
      <section v-if="selectedCompany" class="animate-fade-in">
        <button @click="selectedCompany = null; cars = []" class="mb-8 inline-flex items-center text-cobalt-link text-body font-medium hover:underline transition-all">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
          </svg>
          회사 목록으로 돌아가기
        </button>
        <h2 class="text-heading font-bold tracking-heading text-ink mb-2">{{ selectedCompany.companyName }}'s Cars</h2>
        <p class="text-subheading tracking-subheading text-graphite mb-8">Choose your perfect ride.</p>
        
        <div v-if="loadingCars" class="text-graphite py-8">Loading...</div>
        <div v-else-if="cars.length === 0" class="text-graphite py-8">No cars available from this company.</div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div 
            v-for="car in cars" 
            :key="car.carId"
            class="bg-snow rounded-cards flex flex-col overflow-hidden"
          >
            <div class="w-full aspect-[4/3] bg-fog flex items-center justify-center">
              <img v-if="car.carImageUrl" :src="getImageUrl(car.carImageUrl)" :alt="car.carName" class="w-full h-full object-cover" />
              <span v-else class="text-graphite text-[12px]">No Image</span>
            </div>
            <div class="p-[28px] flex flex-col flex-grow justify-between">
              <div>
                <h3 class="text-[24px] font-semibold tracking-[-0.015em] text-ink mb-1">{{ car.carName }}</h3>
                <p class="text-[14px] text-graphite mb-4">{{ car.carNumber }} • {{ car.numberOfRider }} Riders</p>
                <p class="text-[17px] text-ink line-clamp-2 leading-[1.47]">{{ car.carDetail }}</p>
              </div>
              <div class="mt-8 flex items-center justify-between border-t border-silver-mist pt-6">
                <div class="flex flex-col">
                  <span class="text-[12px] text-graphite mb-1">일일 대여료</span>
                  <span class="text-[20px] font-semibold text-ink">₩{{ car.carRentalCost.toLocaleString() }}</span>
                </div>
                <div class="flex gap-2">
                  <button @click="goToRepair(car)" class="bg-silver-mist/60 text-ink px-4 py-2 rounded-buttons text-body-sm font-medium hover:bg-silver-mist transition-colors active:scale-[0.98]">
                    정비정보
                  </button>
                  <button @click="goToRental(car)" class="bg-azure text-snow px-5 py-2 rounded-buttons text-body-sm font-medium hover:bg-cobalt-link transition-colors active:scale-[0.98]">
                    Rent
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Why Choose Us Section -->
      <section class="py-16 border-t border-silver-mist">
        <h2 class="text-heading font-bold tracking-heading text-ink mb-10 text-center">Why Choose Us</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="flex flex-col items-center text-center p-[28px] bg-snow rounded-cards">
            <div class="text-4xl mb-4">🚐</div>
            <h3 class="text-[20px] font-semibold tracking-[-0.01em] text-ink mb-2">Premium Vehicles</h3>
            <p class="text-[17px] text-graphite leading-[1.47]">Experience the outdoors with our top-tier, fully equipped camping cars.</p>
          </div>
          <div class="flex flex-col items-center text-center p-[28px] bg-snow rounded-cards">
            <div class="text-4xl mb-4">🗺️</div>
            <h3 class="text-[20px] font-semibold tracking-[-0.01em] text-ink mb-2">Anywhere Access</h3>
            <p class="text-[17px] text-graphite leading-[1.47]">Pick up and drop off your vehicle at any of our branches nationwide.</p>
          </div>
          <div class="flex flex-col items-center text-center p-[28px] bg-snow rounded-cards">
            <div class="text-4xl mb-4">🛡️</div>
            <h3 class="text-[20px] font-semibold tracking-[-0.01em] text-ink mb-2">24/7 Support</h3>
            <p class="text-[17px] text-graphite leading-[1.47]">Our customer service team is always here to assist you during your journey.</p>
          </div>
        </div>
      </section>

    </main>

    <!-- Footer -->
    <footer class="bg-ink text-snow py-12 mt-20">
      <div class="max-w-[1200px] mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-8">
        <div class="md:col-span-2">
          <h2 class="text-subheading font-bold tracking-subheading mb-4">Camping Car Rental</h2>
          <p class="text-body-sm text-snow/70 max-w-sm">
            Your ultimate partner for road trips and outdoor adventures. Start your journey with the best camping cars available in Korea.
          </p>
        </div>
        <div>
          <h3 class="text-body font-bold mb-4">Quick Links</h3>
          <ul class="space-y-2 text-body-sm text-snow/70">
            <li><a href="#" class="hover:text-snow transition-colors">About Us</a></li>
            <li><a href="#" class="hover:text-snow transition-colors">Destinations</a></li>
            <li><a href="#" class="hover:text-snow transition-colors">FAQ</a></li>
          </ul>
        </div>
        <div>
          <h3 class="text-body font-bold mb-4">Contact</h3>
          <ul class="space-y-2 text-body-sm text-snow/70">
            <li>support@campingcar.com</li>
            <li>+82 10-1234-5678</li>
            <li>Seoul, South Korea</li>
          </ul>
        </div>
      </div>
      <div class="max-w-[1200px] mx-auto px-6 mt-12 pt-8 border-t border-snow/20 text-center text-caption text-snow/50">
        &copy; 2026 Camping Car Rental. All rights reserved.
      </div>
    </footer>
  </div>
</template>
