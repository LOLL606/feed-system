<template>
  <header class="sticky top-0 z-100 bg-wc-green text-white px-4 py-3 flex items-center justify-between shadow-sm">
    <h1 class="text-[17px] font-semibold">朋友圈</h1>
    <div class="flex items-center gap-3">
      <div class="flex items-center gap-1.5 bg-white/20 px-2.5 py-1 rounded-full text-[13px] cursor-pointer hover:bg-white/30 transition-colors">
        <img :src="avatarSVG(user.id, user.name, 24)" class="w-6 h-6 rounded-full" alt="" />
        <span>{{ user.name }}</span>
        <span v-if="user.isBigV" class="bg-wc-orange text-white text-[10px] px-1.5 py-px rounded-sm font-semibold">大V</span>
      </div>
      <button class="bg-transparent border-none text-white text-xl cursor-pointer p-1 rounded-full hover:bg-white/20 transition-colors" @click="$emit('post')">✚</button>
      <button class="bg-transparent border-none text-white text-base cursor-pointer p-1 rounded-full hover:bg-white/20 transition-colors" @click="$emit('switch')">🔄</button>
    </div>
  </header>
</template>

<script setup>
import { avatarSVG } from '../utils/index.js'

defineProps({ user: { type: Object, required: true } })
defineEmits(['post', 'switch'])
</script>
