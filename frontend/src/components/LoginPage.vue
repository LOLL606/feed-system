<template>
  <div class="min-h-screen bg-wc-bg flex flex-col items-center pt-15">
    <div class="text-[22px] font-semibold text-wc-text mb-2">仿微信朋友圈</div>
    <div class="text-[13px] text-wc-text-secondary mb-8">选择一个用户身份进入</div>
    <div class="grid grid-cols-4 gap-3 px-4 max-w-[480px] w-full max-[480px]:grid-cols-3">
      <div
        v-for="u in users" :key="u.id"
        class="bg-white rounded-[10px] py-4 px-2 text-center cursor-pointer transition-all hover:shadow-md hover:-translate-y-0.5"
        :class="{ 'border-2 border-wc-green': currentId === u.id, 'border-2 border-transparent': currentId !== u.id }"
        @click="$emit('select', u.id)"
      >
        <img :src="avatarSVG(u.id, u.name, 48)" class="w-12 h-12 rounded-full mx-auto mb-2" :alt="u.name" />
        <div class="text-[13px] font-medium mb-0.5">{{ u.name }}</div>
        <div class="text-[10px]">
          <span v-if="u.isBigV" class="bg-wc-orange text-white text-[10px] px-1.5 py-px rounded-sm font-semibold">大V</span>
          <span v-else class="text-wc-text-light">{{ u.followerCount }}粉</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as API from '../api/index.js'
import { avatarSVG } from '../utils/index.js'

defineProps({ currentId: { type: Number, default: null } })
defineEmits(['select'])

const users = ref([])

onMounted(async () => {
  users.value = await API.getUserList()
})
</script>
