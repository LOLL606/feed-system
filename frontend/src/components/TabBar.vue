<template>
  <div class="flex bg-white border-b border-wc-border sticky top-[52px] z-90">
    <div
      v-for="t in tabs" :key="t.key"
      class="flex-1 text-center py-2.5 text-sm cursor-pointer border-b-2 transition-all"
      :class="active === t.key
        ? 'text-wc-green border-wc-green font-semibold'
        : 'text-wc-text-secondary border-transparent hover:text-wc-text'"
      @click="$emit('change', t.key)"
    >
      {{ t.label }}
    </div>
  </div>
</template>

<script setup>
defineProps({ active: { type: String, required: true } })
defineEmits(['change'])

const tabs = [
  { key: 'feed', label: '朋友圈' },
  { key: 'follow', label: '关注管理' },
  { key: 'dashboard', label: '架构面板' },
]
</script>
