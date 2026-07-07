<template>
  <div class="px-4 py-3">
    <div v-if="loading" class="text-center py-5 text-wc-text-light text-[13px]">加载中...</div>
    <div
      v-for="u in otherUsers" :key="u.id"
      class="bg-white rounded-lg p-3 mb-2 flex items-center justify-between"
    >
      <div class="flex items-center gap-2.5">
        <img :src="avatarSVG(u.id, u.name, 36)" class="w-9 h-9 rounded-full flex-shrink-0" :alt="u.name" />
        <div>
          <div class="font-medium">{{ u.name }}</div>
          <div class="text-[11px] text-wc-text-light">
            {{ u.isBigV ? '大V · ' : '' }}{{ u.followerCount }} 粉丝
          </div>
        </div>
      </div>
      <button
        class="px-3 py-1 border rounded text-xs cursor-pointer transition-all flex-shrink-0"
        :class="blockedIds.has(u.id)
          ? 'bg-wc-red text-white border-wc-red'
          : 'bg-white text-wc-text border-wc-border hover:bg-gray-100'"
        @click="toggleBlock(u.id)"
      >
        {{ blockedIds.has(u.id) ? '已屏蔽' : '屏蔽' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import * as API from '../api/index.js'
import { avatarSVG } from '../utils/index.js'

const props = defineProps({ user: { type: Object, required: true } })

const users = ref([])
const blockedIds = ref(new Set())
const loading = ref(false)

const otherUsers = computed(() => users.value.filter(u => u.id !== props.user.id))

async function loadUsers() {
  loading.value = true
  try {
    users.value = await API.getUserList()
  } finally {
    loading.value = false
  }
}

async function toggleBlock(userId) {
  if (blockedIds.value.has(userId)) {
    await API.unblockUser(userId)
    blockedIds.value.delete(userId)
  } else {
    await API.blockUser(userId)
    blockedIds.value.add(userId)
  }
  blockedIds.value = new Set(blockedIds.value)
}

watch(() => props.user, loadUsers, { immediate: true })
</script>
