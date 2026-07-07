<template>
  <div class="px-4 py-3">
    <!-- Current User Card -->
    <div class="bg-white rounded-[10px] p-4 mb-3">
      <h4 class="text-[13px] text-wc-text-secondary mb-2 uppercase tracking-wider">当前用户</h4>
      <div class="flex items-center gap-3">
        <img :src="avatarSVG(user.id, user.name, 48)" class="w-12 h-12 rounded-full" alt="" />
        <div>
          <div class="text-lg font-semibold">{{ user.name }}</div>
          <span
            class="inline-block px-2.5 py-0.5 rounded text-xs font-semibold"
            :class="user.isBigV ? 'bg-blue-50 text-blue-800' : 'bg-green-50 text-green-800'"
          >
            {{ user.isBigV ? '大V — 拉模式' : '普通用户 — 推模式' }}
          </span>
          <div class="text-xs text-wc-text-light mt-1">粉丝 {{ (user.followerCount || 0).toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-2 gap-3 mb-3 max-[480px]:grid-cols-1">
      <!-- Fanout Card -->
      <div class="bg-white rounded-[10px] p-4">
        <h4 class="text-[13px] text-wc-text-secondary mb-2 uppercase tracking-wider">最近写扩散</h4>
        <div class="flex items-baseline gap-1.5">
          <span class="text-3xl font-bold" :class="stats.isBigV ? 'text-wc-blue' : 'text-wc-green'">
            {{ (stats.lastPostFanoutCount || 0).toLocaleString() }}
          </span>
          <span class="text-[13px] text-wc-text-secondary">次写入</span>
        </div>
        <div class="h-2 bg-gray-100 rounded mt-2 overflow-hidden">
          <div
            class="h-full rounded transition-all duration-500"
            :class="stats.isBigV ? 'bg-wc-blue' : 'bg-wc-green'"
            :style="{ width: Math.min(100, (stats.lastPostFanoutCount || 0) / 500 * 100) + '%' }"
          />
        </div>
        <div class="text-[11px] text-wc-text-light mt-1">
          模式：{{ stats.mode === 'pull' ? '拉（只写1次）' : '推（写N次）' }}
        </div>
      </div>

      <!-- Feed Source Card -->
      <div class="bg-white rounded-[10px] p-4">
        <h4 class="text-[13px] text-wc-text-secondary mb-2 uppercase tracking-wider">Feed 来源分布</h4>
        <div class="mt-1">
          <div class="flex justify-between text-[13px] mb-1">
            <span class="text-green-800">推模式 {{ pushCount }}</span>
            <span class="text-blue-800">拉模式 {{ pullCount }}</span>
          </div>
          <div class="h-2 bg-gray-100 rounded flex overflow-hidden">
            <div class="h-full bg-green-500 rounded-l" :style="{ width: feedPushWidth + '%' }" />
            <div class="h-full bg-blue-500 rounded-r" :style="{ width: (100 - feedPushWidth) + '%' }" />
          </div>
        </div>
      </div>
    </div>

    <!-- DB Route Card -->
    <div class="bg-white rounded-[10px] p-4">
      <h4 class="text-[13px] text-wc-text-secondary mb-2 uppercase tracking-wider">读写分离</h4>
      <div class="flex gap-5 mt-2">
        <div>
          <div class="text-xs text-wc-text-light">最近写入</div>
          <div class="text-[15px] font-semibold text-wc-red">master（主库）</div>
        </div>
        <div>
          <div class="text-xs text-wc-text-light">最近读取</div>
          <div class="text-[15px] font-semibold text-wc-blue">slave（从库）</div>
        </div>
        <div>
          <div class="text-xs text-wc-text-light">主从延迟</div>
          <div class="text-[15px] font-semibold">{{ dbRoute.currentSource || "—" }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import * as API from '../api/index.js'
import { avatarSVG } from '../utils/index.js'

const props = defineProps({ user: { type: Object, required: true } })

const stats = ref({})
const feedSource = ref({})
const dbRoute = ref({})

const pushCount = computed(() => feedSource.value.pushCount || 0)
const pullCount = computed(() => feedSource.value.pullCount || 0)
const feedPushWidth = computed(() => {
  const total = pushCount.value + pullCount.value
  return total ? (pushCount.value / total * 100) : 50
})

async function loadDashboard() {
  const [s, fs, dr] = await Promise.all([
    API.getFanoutStats(),
    API.getFeedSource(),
    API.getDbRoute(),
  ])
  stats.value = s
  feedSource.value = fs
  dbRoute.value = dr
}

watch(() => props.user, loadDashboard, { immediate: true })
</script>
