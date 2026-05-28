<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  startDate: {
    type: String,
    default: ''
  },
  endDate: {
    type: String,
    default: ''
  },
  bookedRanges: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:startDate', 'update:endDate']);

const currentDate = new Date();
const currentMonth = ref(currentDate.getMonth());
const currentYear = ref(currentDate.getFullYear());

const hoverDate = ref(null);

const daysInMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value + 1, 0).getDate();
});

const firstDayOfMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value, 1).getDay();
});

const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
};

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
};

const formatDate = (year, month, day) => {
  const m = String(month + 1).padStart(2, '0');
  const d = String(day).padStart(2, '0');
  return `${year}-${m}-${d}`;
};

const parseDate = (dateStr) => {
  if (!dateStr) return null;
  const [y, m, d] = dateStr.split('-');
  return new Date(y, m - 1, d);
};

// Check if a specific date is booked
const isBooked = (dateStr) => {
  const targetDate = parseDate(dateStr);
  targetDate.setHours(0,0,0,0);
  
  for (const range of props.bookedRanges) {
    const rStart = parseDate(range.start.split('T')[0]);
    rStart.setHours(0,0,0,0);
    const rEnd = parseDate(range.end.split('T')[0]);
    rEnd.setHours(0,0,0,0);
    
    if (targetDate >= rStart && targetDate <= rEnd) {
      return true;
    }
  }
  return false;
};

// Check if any date between start and end (inclusive) is booked
const hasBookedDateInRange = (startStr, endStr) => {
  const start = parseDate(startStr);
  const end = parseDate(endStr);
  if (start > end) return false;
  
  let curr = new Date(start);
  while (curr <= end) {
    const m = String(curr.getMonth() + 1).padStart(2, '0');
    const d = String(curr.getDate()).padStart(2, '0');
    const dateStr = `${curr.getFullYear()}-${m}-${d}`;
    if (isBooked(dateStr)) return true;
    curr.setDate(curr.getDate() + 1);
  }
  return false;
};

const handleDateClick = (day) => {
  const clickedDateStr = formatDate(currentYear.value, currentMonth.value, day);
  
  // Prevent click if booked
  if (isBooked(clickedDateStr)) return;

  const clickedDate = new Date(currentYear.value, currentMonth.value, day);
  clickedDate.setHours(0,0,0,0);
  
  const start = parseDate(props.startDate);
  if (start) start.setHours(0,0,0,0);

  // Cancel selection
  if (props.startDate && !props.endDate && props.startDate === clickedDateStr) {
    emit('update:startDate', '');
    emit('update:endDate', '');
    return;
  }

  // Start new selection
  if (!props.startDate || props.endDate || (start && clickedDate < start)) {
    emit('update:startDate', clickedDateStr);
    emit('update:endDate', ''); 
  } 
  // Set end date
  else {
    // Check if there are booked dates between start and clickedDate
    if (hasBookedDateInRange(props.startDate, clickedDateStr)) {
      // If there are booked dates in between, just start a new selection from clickedDate
      emit('update:startDate', clickedDateStr);
      emit('update:endDate', ''); 
    } else {
      emit('update:endDate', clickedDateStr);
    }
  }
};

const handleMouseEnter = (day) => {
  const hoverDateStr = formatDate(currentYear.value, currentMonth.value, day);
  if (isBooked(hoverDateStr)) return;
  
  if (props.startDate && !props.endDate) {
    hoverDate.value = hoverDateStr;
  }
};

const handleMouseLeave = () => {
  hoverDate.value = null;
};

const isStartDate = (dateStr) => props.startDate === dateStr;
const isEndDate = (dateStr) => props.endDate === dateStr;

const isInRange = (dateStr) => {
  if (!props.startDate) return false;
  
  const current = parseDate(dateStr);
  const start = parseDate(props.startDate);
  
  if (props.endDate) {
    const end = parseDate(props.endDate);
    return current > start && current < end;
  } else if (hoverDate.value) {
    const hover = parseDate(hoverDate.value);
    // Don't show range if there's a booked date between start and hover
    if (current > start && current <= hover) {
      if (hasBookedDateInRange(props.startDate, hoverDate.value)) {
        return false;
      }
      return true;
    }
  }
  return false;
};

</script>

<template>
  <div class="bg-white rounded-xl shadow-sm border border-silver-mist p-4">
    <!-- Header -->
    <div class="flex justify-between items-center mb-4 px-2">
      <button @click="prevMonth" class="p-2 hover:bg-fog rounded-full transition-colors">
        <svg class="w-5 h-5 text-graphite" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
      </button>
      <div class="text-body font-bold text-ink">
        {{ currentYear }}년 {{ monthNames[currentMonth] }}
      </div>
      <button @click="nextMonth" class="p-2 hover:bg-fog rounded-full transition-colors">
        <svg class="w-5 h-5 text-graphite" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
      </button>
    </div>

    <!-- Days of Week -->
    <div class="grid grid-cols-7 gap-1 mb-2">
      <div v-for="day in ['일', '월', '화', '수', '목', '금', '토']" :key="day" class="text-center text-caption font-medium text-graphite py-1">
        {{ day }}
      </div>
    </div>

    <!-- Calendar Grid -->
    <div class="grid grid-cols-7 gap-y-6 mt-4">
      <!-- Empty cells before start of month -->
      <div v-for="empty in firstDayOfMonth" :key="`empty-${empty}`"></div>
      
      <!-- Days -->
      <div 
        v-for="day in daysInMonth" 
        :key="day"
        class="relative h-14 flex items-center justify-center select-none"
        :class="[isBooked(formatDate(currentYear, currentMonth, day)) ? 'cursor-not-allowed opacity-80' : 'cursor-pointer']"
        @click="handleDateClick(day)"
        @mouseenter="handleMouseEnter(day)"
        @mouseleave="handleMouseLeave"
      >
        <!-- Range Background (spanning full width for visual connection) -->
        <div 
          v-if="!isBooked(formatDate(currentYear, currentMonth, day)) && (isInRange(formatDate(currentYear, currentMonth, day)) || isStartDate(formatDate(currentYear, currentMonth, day)) && endDate || isEndDate(formatDate(currentYear, currentMonth, day)))"
          class="absolute inset-y-0 w-full bg-azure/10"
          :class="{
            'rounded-l-full': isStartDate(formatDate(currentYear, currentMonth, day)),
            'rounded-r-full': isEndDate(formatDate(currentYear, currentMonth, day)) || (hoverDate === formatDate(currentYear, currentMonth, day) && !endDate && !hasBookedDateInRange(startDate, hoverDate))
          }"
        ></div>

        <!-- Date Number Circle -->
        <div 
          class="relative z-10 flex items-center justify-center rounded-full transition-all duration-200"
          :class="{
            'w-11 h-11 bg-azure text-white font-bold text-body shadow-md': isStartDate(formatDate(currentYear, currentMonth, day)) || isEndDate(formatDate(currentYear, currentMonth, day)),
            'w-10 h-10 text-ink hover:bg-silver-mist/50 text-body-sm': !isStartDate(formatDate(currentYear, currentMonth, day)) && !isEndDate(formatDate(currentYear, currentMonth, day)) && !isBooked(formatDate(currentYear, currentMonth, day)),
            'w-10 h-10 bg-red-50 text-red-500 line-through text-body-sm font-medium border border-red-100': isBooked(formatDate(currentYear, currentMonth, day))
          }"
        >
          {{ day }}
        </div>
      </div>
    </div>
    
    <div class="mt-4 pt-4 border-t border-silver-mist flex justify-between text-caption text-graphite">
      <div class="flex gap-4">
        <div>
          <span class="inline-block w-3 h-3 rounded-full bg-azure mr-1 align-middle"></span> 
          시작/종료일
        </div>
        <div>
          <span class="inline-block w-3 h-3 rounded-full bg-red-100 border border-red-200 mr-1 align-middle text-center line-through decoration-red-500"></span> 
          예약불가
        </div>
      </div>
      <div>
        <span class="inline-block w-3 h-3 rounded bg-azure/10 mr-1 align-middle"></span>
        대여 기간
      </div>
    </div>
  </div>
</template>
