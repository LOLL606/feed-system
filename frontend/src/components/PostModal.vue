<template>
  <Teleport to="body">
    <div class="fixed inset-0 bg-black/50 z-200 flex items-center justify-center" @click.self="$emit('close')">
      <div class="bg-white rounded-lg w-[90%] max-w-[480px] p-5 shadow-xl">
        <h3 class="text-base mb-3 text-center">发朋友圈</h3>
        <textarea
          v-model="content"
          class="w-full h-30 border border-wc-border rounded-md p-2.5 text-[15px] resize-none font-[inherit] focus:outline-none focus:border-wc-green"
          placeholder="这一刻的想法..."
          @keydown.enter.ctrl="submit"
        />
        <div class="flex justify-between mt-3">
          <button
            class="px-6 py-2 border-none rounded-md text-sm cursor-pointer hover:opacity-85 bg-gray-100 text-wc-text-secondary"
            @click="$emit('close')"
          >
            取消
          </button>
          <button
            class="px-6 py-2 border-none rounded-md text-sm cursor-pointer hover:opacity-85 bg-wc-green text-white disabled:opacity-50"
            :disabled="submitting"
            @click="submit"
          >
            {{ submitting ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import * as API from '../api/index.js'

const emit = defineEmits(['close', 'submitted'])

const content = ref('')
const submitting = ref(false)

async function submit() {
  const text = content.value.trim()
  if (!text || submitting.value) return

  submitting.value = true
  try {
    await API.createPost(text)
    emit('submitted')
  } catch (e) {
    alert('发送失败：' + e.message)
  } finally {
    submitting.value = false
  }
}
</script>
