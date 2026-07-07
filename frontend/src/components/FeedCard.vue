<template>
  <div class="bg-white mb-px px-4 py-3 relative" :class="{ 'pin-top': post.isPinned }">
    <!-- 置顶标识 -->
    <div
      v-if="post.isPinned"
      class="absolute top-0 left-0 w-full h-0.5 bg-gradient-to-r from-yellow-400 to-yellow-300"
    />

    <div class="flex items-start gap-2.5">
      <img :src="avatarSVG(post.userId, userName, 40)" class="w-10 h-10 rounded-md flex-shrink-0" :alt="userName" />
      <div class="flex-1 min-w-0">
        <div class="flex items-center justify-between">
          <div class="font-medium text-wc-link text-[15px] truncate">{{ userName }}</div>
          <!-- 更多操作按钮 -->
          <div class="relative flex-shrink-0 ml-2">
            <button
              class="w-7 h-7 flex items-center justify-center rounded-full hover:bg-gray-100 text-[#b0b0b0] transition-colors"
              @click.stop="toggleMenu"
            >
              <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <circle cx="8" cy="3" r="1.5"/>
                <circle cx="8" cy="8" r="1.5"/>
                <circle cx="8" cy="13" r="1.5"/>
              </svg>
            </button>
            <!-- 下拉菜单 -->
            <div
              v-if="menuOpen"
              class="absolute right-0 top-8 z-20 bg-white rounded-lg shadow-lg border border-gray-100 py-1 min-w-[110px]"
              @click.stop
            >
              <button
                class="w-full px-4 py-2 text-[13px] text-left hover:bg-gray-50 flex items-center gap-2 transition-colors"
                @click="togglePin"
              >
                <span v-if="post.isPinned">📌 取消置顶</span>
                <span v-else>📌 置顶</span>
              </button>
            </div>
          </div>
        </div>
        <div class="flex items-center text-xs text-wc-text-light mt-0.5">
          <span v-if="post.isPinned" class="text-yellow-500 font-medium mr-1.5">📌 置顶</span>
          <span>{{ formatTime(post.createdAt) }}</span>
          <span
            class="inline-block text-[10px] px-1.5 py-px rounded-sm ml-1.5"
            :class="post.source === 'pull' ? 'bg-blue-50 text-blue-800' : 'bg-green-50 text-green-800'"
          >
            {{ post.source === 'pull' ? '拉模式' : '推模式' }}
          </span>
        </div>
        <div class="mt-1.5 text-[15px] leading-relaxed break-all whitespace-pre-wrap">{{ post.content }}</div>
      </div>
    </div>

    <!-- 点击菜单外关闭 -->
    <div
      v-if="menuOpen"
      class="fixed inset-0 z-10"
      @click="menuOpen = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { avatarSVG, formatTime } from '../utils/index.js'

const props = defineProps({
  post: { type: Object, required: true },
  userName: { type: String, default: '未知' },
})

const emit = defineEmits(['pin-toggle'])

const menuOpen = ref(false)

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function togglePin() {
  menuOpen.value = false
  emit('pin-toggle', props.post.id, !props.post.isPinned)
}
</script>

<style scoped>
.pin-top {
  box-shadow: inset 0 0 0 1px rgba(250, 204, 21, 0.25);
}
</style>
