<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { getCompaniesByRegion, getCarsByCompany } from '../api/company';

const router = useRouter();
const regions = ['서울', '대전', '제주', '경기', '부산'];
const selectedRegion = ref('');
const companies = ref([]);
const selectedCompany = ref(null);
const cars = ref([]);
const loadingCompanies = ref(false);
const loadingCars = ref(false);

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

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true });
  renderLoop();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
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
  loadingCars.value = true;
  
  try {
    const response = await getCarsByCompany(company.companyId);
    cars.value = response.data || [];
  } catch (error) {
    console.error('Failed to fetch cars:', error);
    alert('자동차 목록을 불러오는데 실패했습니다.');
  } finally {
    loadingCars.value = false;
  }
};

const handleLogout = () => {
  localStorage.removeItem('token');
  router.push('/');
};
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <!-- Header -->
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <h1 class="text-body font-semibold tracking-body text-ink">Camping Car Rental</h1>
        <button @click="handleLogout" class="text-cobalt-link text-body-sm font-medium hover:underline">Logout</button>
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
      
      <!-- Region Selector -->
      <section>
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
      <section v-if="selectedRegion">
        <div class="flex items-end justify-between mb-6">
          <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink">Companies in {{ selectedRegion }}</h2>
        </div>
        
        <div v-if="loadingCompanies" class="text-graphite py-8">Loading...</div>
        <div v-else-if="companies.length === 0" class="text-graphite py-8">No companies found in this region.</div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div 
            v-for="company in companies" 
            :key="company.companyId"
            @click="selectCompany(company)"
            :class="[
              'p-7 rounded-cards cursor-pointer transition-transform hover:scale-[1.02]',
              selectedCompany?.companyId === company.companyId
                ? 'bg-ink text-snow'
                : 'bg-snow text-ink'
            ]"
          >
            <h3 class="text-subheading font-bold tracking-subheading mb-2">{{ company.companyName }}</h3>
            <p :class="selectedCompany?.companyId === company.companyId ? 'text-snow/80' : 'text-graphite'" class="text-body-sm mb-4">
              {{ company.companyAddress }}
            </p>
            <div class="flex flex-col gap-1 text-caption">
              <span>📞 {{ company.companyPhone }}</span>
              <span>✉️ {{ company.representativeEmail }}</span>
              <span>👤 {{ company.representativeName }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Car List -->
      <section v-if="selectedCompany" class="pt-8 border-t border-silver-mist">
        <h2 class="text-heading font-bold tracking-heading text-ink mb-2">{{ selectedCompany.companyName }}'s Cars</h2>
        <p class="text-subheading tracking-subheading text-graphite mb-8">Choose your perfect ride.</p>
        
        <div v-if="loadingCars" class="text-graphite py-8">Loading...</div>
        <div v-else-if="cars.length === 0" class="text-graphite py-8">No cars available from this company.</div>
        
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div 
            v-for="car in cars" 
            :key="car.carId"
            class="bg-snow rounded-cards p-8 flex flex-col md:flex-row gap-8"
          >
            <div class="flex-shrink-0 w-full md:w-48 h-48 bg-fog rounded-lg overflow-hidden flex items-center justify-center">
              <img v-if="car.carImage" :src="car.carImage" :alt="car.carName" class="w-full h-full object-cover" />
              <span v-else class="text-graphite text-caption">No Image</span>
            </div>
            <div class="flex-grow flex flex-col justify-between">
              <div>
                <h3 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-1">{{ car.carName }}</h3>
                <p class="text-body-sm text-graphite mb-4">{{ car.carNumber }} • {{ car.numberOfRider }} Riders</p>
                <p class="text-body text-ink line-clamp-3">{{ car.carDetail }}</p>
              </div>
              <div class="mt-6 flex items-center justify-between">
                <span class="text-subheading font-bold tracking-subheading text-ink">₩{{ car.carRentalCost.toLocaleString() }}<span class="text-body-sm text-graphite font-normal">/day</span></span>
                <button class="bg-azure text-snow px-6 py-2 rounded-buttons text-body-sm font-medium hover:bg-cobalt-link transition-colors">
                  Rent
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Why Choose Us Section -->
      <section class="py-16 border-t border-silver-mist">
        <h2 class="text-heading font-bold tracking-heading text-ink mb-10 text-center">Why Choose Us</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="flex flex-col items-center text-center p-6 bg-snow rounded-cards shadow-sm">
            <div class="text-4xl mb-4">🚐</div>
            <h3 class="text-subheading font-bold tracking-subheading text-ink mb-2">Premium Vehicles</h3>
            <p class="text-body-sm text-graphite">Experience the outdoors with our top-tier, fully equipped camping cars.</p>
          </div>
          <div class="flex flex-col items-center text-center p-6 bg-snow rounded-cards shadow-sm">
            <div class="text-4xl mb-4">🗺️</div>
            <h3 class="text-subheading font-bold tracking-subheading text-ink mb-2">Anywhere Access</h3>
            <p class="text-body-sm text-graphite">Pick up and drop off your vehicle at any of our branches nationwide.</p>
          </div>
          <div class="flex flex-col items-center text-center p-6 bg-snow rounded-cards shadow-sm">
            <div class="text-4xl mb-4">🛡️</div>
            <h3 class="text-subheading font-bold tracking-subheading text-ink mb-2">24/7 Support</h3>
            <p class="text-body-sm text-graphite">Our customer service team is always here to assist you during your journey.</p>
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
