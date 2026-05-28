<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCarRepairHistory } from '../api/repair';
import { formatToKoreanDateTime } from '../utils/date';

const route = useRoute();
const router = useRouter();
const carId = route.params.carId;

const repairs = ref([]);
const isLoading = ref(true);

const carName = ref(history.state?.carName || '선택한 차량');
const carNumber = ref(history.state?.carNumber || '정보 없음');

onMounted(async () => {
  if (carId) {
    try {
      isLoading.value = true;
      const response = await getCarRepairHistory(carId);
      repairs.value = response.data || [];
    } catch (error) {
      console.error('Failed to load car repair history:', error);
      alert('정비 정보를 불러오는데 실패했습니다.');
    } finally {
      isLoading.value = false;
    }
  }
});

const totalCost = computed(() => {
  return repairs.value.reduce((sum, item) => sum + (item.repairCost || 0), 0);
});
</script>

<template>
  <div class="min-h-screen bg-fog pb-20">
    <header class="bg-snow/80 backdrop-blur-[20px] sticky top-0 z-50 border-b border-silver-mist">
      <div class="max-w-[1200px] mx-auto px-6 h-[52px] flex items-center justify-between">
        <button @click="router.back()" class="text-cobalt-link text-body-sm font-medium hover:underline flex items-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
          뒤로가기
        </button>
        <h1 class="text-body font-semibold tracking-body text-ink">정비 이력 상세 조회</h1>
        <div class="w-20"></div> <!-- spacer -->
      </div>
    </header>

    <main class="max-w-[900px] mx-auto px-6 mt-12">
      <!-- Section Title -->
      <h1 class="text-heading font-bold tracking-heading text-ink mb-2">차량 정비 내역</h1>
      <p class="text-subheading tracking-subheading text-graphite mb-8">차량의 검사 및 정비 보수 기록을 확인하여 안심하고 대여하세요.</p>

      <div v-if="isLoading" class="text-graphite py-16 text-center flex flex-col justify-center items-center gap-3">
        <svg class="animate-spin h-8 w-8 text-azure" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <span class="text-body font-medium">정비 데이터를 안전하게 불러오는 중...</span>
      </div>

      <div v-else>
        <!-- Car details and statistics summary card -->
        <div class="bg-snow rounded-[28px] border border-silver-mist p-[28px] mb-12 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-8 animate-fade-in">
          <div>
            <span class="text-caption font-bold text-azure bg-azure/10 px-3 py-1 rounded-full inline-block mb-3">Vehicle Details</span>
            <h2 class="text-heading-sm font-bold tracking-heading-sm text-ink mb-2">{{ carName }}</h2>
            <p class="text-body-sm text-graphite">차량 번호: <span class="font-semibold text-ink">{{ carNumber }}</span></p>
          </div>
          
          <div class="flex gap-6 border-t border-silver-mist/60 md:border-t-0 pt-6 md:pt-0">
            <div class="flex-1 bg-fog px-6 py-4 rounded-xl border border-silver-mist/40 min-w-[140px]">
              <span class="text-caption text-graphite block mb-1">총 정비 건수</span>
              <span class="text-[28px] font-bold text-ink">
                {{ repairs.length }}<span class="text-body font-normal text-graphite ml-1">건</span>
              </span>
            </div>
            <div class="flex-1 bg-fog px-6 py-4 rounded-xl border border-silver-mist/40 min-w-[180px]">
              <span class="text-caption text-graphite block mb-1">누적 정비 비용</span>
              <span class="text-[28px] font-bold text-azure">
                ₩{{ totalCost.toLocaleString() }}
              </span>
            </div>
          </div>
        </div>

        <!-- Empty State (No Repair History) -->
        <div v-if="repairs.length === 0" class="bg-snow rounded-cards p-[40px] text-center border border-silver-mist shadow-sm flex flex-col items-center justify-center gap-4 animate-fade-in">
          <div class="w-16 h-16 rounded-full bg-emerald-50 border-2 border-emerald-200 flex items-center justify-center text-3xl">
            🛡️
          </div>
          <h3 class="text-subheading font-bold text-ink">정비 내역이 없는 무결점 차량</h3>
          <p class="text-body-sm text-graphite max-w-md leading-relaxed">
            해당 차량은 최근 등록되었거나 정비 보수가 필요 없을 정도로 매우 철저하게 관리되고 있는 안전한 차량입니다. 안심하고 즐거운 여행을 떠나세요!
          </p>
        </div>

        <!-- Timeline View (Has Repairs) -->
        <div v-else class="relative mt-8 pb-12 animate-fade-in">
          <!-- Central Connecting Line -->
          <div class="absolute left-6 md:left-1/2 top-0 bottom-0 w-0.5 bg-silver-mist -translate-x-1/2"></div>
          
          <div class="space-y-12">
            <div 
              v-for="(repair, index) in repairs" 
              :key="repair.repairId"
              class="relative flex flex-col md:flex-row md:items-center"
              :class="[index % 2 === 0 ? 'md:flex-row-reverse' : '']"
            >
              <!-- Timeline Dot -->
              <div class="absolute left-6 md:left-1/2 -translate-x-1/2 w-4 h-4 rounded-full bg-azure border-4 border-snow shadow-md z-10"></div>
              
              <!-- Content Card Wrapper -->
              <div class="w-full md:w-1/2 pl-12 md:pl-0 md:px-8">
                <div class="bg-snow p-6 rounded-cards border border-silver-mist shadow-sm hover:shadow-md transition-all duration-300 hover:scale-[1.01]">
                  <!-- Card Header -->
                  <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
                    <span class="text-caption font-bold text-azure bg-azure/10 px-2.5 py-1 rounded-full border border-azure/20">
                      정비소 #{{ repair.repairShopId }}
                    </span>
                    <span class="text-caption text-graphite font-medium">
                      {{ formatToKoreanDateTime(repair.repairDate) }}
                    </span>
                  </div>
                  
                  <!-- Detail Description (Bubble Card) -->
                  <div class="bg-fog/50 p-4 rounded-xl border border-silver-mist/50 mb-4">
                    <p class="text-body text-ink leading-relaxed font-medium">
                      {{ repair.repairDetail }}
                    </p>
                  </div>
                  
                  <!-- Cost Section -->
                  <div class="flex justify-between items-center border-t border-silver-mist/50 pt-4 mt-2">
                    <span class="text-caption text-graphite font-semibold">정비 비용</span>
                    <span class="text-subheading font-bold text-ink">₩{{ repair.repairCost.toLocaleString() }}</span>
                  </div>
                </div>
              </div>
              
              <!-- Empty spacer to push content on alternate side for desktop -->
              <div class="hidden md:block w-1/2"></div>
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>
