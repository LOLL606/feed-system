<template>
  <div
    class="feed-scroll"
    ref="scrollBox"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
  >
    <!-- 下拉刷新指示器 -->
    <div
      v-if="pullDist > 0"
      class="flex items-center justify-center text-[13px] text-wc-text-light transition-all"
      :style="{ height: pullDist + 'px', opacity: Math.min(1, pullDist / 48) }"
    >
      {{ pullDist >= 48 ? '🔁 释放刷新' : '⬇️ 下拉刷新' }}
    </div>

    <!-- 首次加载 -->
    <div v-if="loading" class="text-center py-5 text-wc-text-light text-[13px]">加载中...</div>

    <!-- 错误 -->
    <div v-else-if="error" class="text-center py-15 text-wc-text-light">
      <div class="text-5xl mb-3">⚠️</div>
      <div class="text-sm">加载失败：{{ error }}</div>
      <div class="mt-3 text-xs text-wc-blue cursor-pointer" @click="refresh">点击重试</div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="posts.length === 0" class="text-center py-15 text-wc-text-light">
      <div class="text-5xl mb-3">📭</div>
      <div class="text-sm">还没有动态，发一条吧！</div>
    </div>

    <!-- Feed 列表 -->
    <template v-else>
      <!-- 置顶帖 -->
      <FeedCard
        v-for="post in pinnedPosts" :key="post.id"
        :post="post"
        :user-name="resolvePostAuthor(post)"
        @pin-toggle="handlePinToggle"
      />

      <!-- 分割线（有置顶帖时） -->
      <div
        v-if="pinnedPosts.length > 0"
        class="flex items-center gap-2 px-4 py-2 text-[11px] text-wc-text-light"
      >
        <span class="flex-1 h-px bg-gray-100"/>
        <span>动态</span>
        <span class="flex-1 h-px bg-gray-100"/>
      </div>

      <!-- 普通帖 -->
      <FeedCard
        v-for="post in normalPosts" :key="post.id"
        :post="post"
        :user-name="resolvePostAuthor(post)"
        @pin-toggle="handlePinToggle"
      />

      <!-- 加载更多指示器 -->
      <div
        ref="loadMoreSentinel"
        class="text-center py-4 text-[12px] text-wc-text-light"
      >
        <template v-if="loadingMore">⏳ 加载中...</template>
        <template v-else-if="!hasMore">— 没有更多了 —</template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import * as API from '../api/index.js'
import FeedCard from './FeedCard.vue'

const PAGE_SIZE = 20

const props = defineProps({ user: { type: Object, required: true } })

const posts = ref([])
const users = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const error = ref(null)
const cursor = ref(null)
const hasMore = ref(true)

const pinnedPosts = computed(() => posts.value.filter(p => p.isPinned))
const normalPosts = computed(() => posts.value.filter(p => !p.isPinned))

// ====== 下拉刷新 ======
const scrollBox = ref(null)
const pullDist = ref(0)
let touchStartY = 0
let touchMoved = false

/** 获取实际滚动的父容器（IPhoneFrame 的 .screen-content） */
function getScrollContainer() {
  return scrollBox.value?.parentElement || null
}

function onTouchStart(e) {
  // 只在顶部且不在加载时触发
  const container = getScrollContainer()
  if (!container || container.scrollTop > 5 || loading.value || loadingMore.value) return
  touchStartY = e.touches[0].clientY
  touchMoved = false
}

function onTouchMove(e) {
  if (touchStartY === 0) return
  const dy = e.touches[0].clientY - touchStartY
  if (dy > 10) {
    touchMoved = true
    pullDist.value = Math.min(dy * 0.5, 64) // 阻尼 0.5，最大 64px
  }
}

async function onTouchEnd() {
  if (pullDist.value >= 48) {
    pullDist.value = 48 // 锁定高度
    await refresh()
  }
  pullDist.value = 0
  touchStartY = 0
}

// ====== 触底加载更多 ======
const loadMoreSentinel = ref(null)
let io = null

function setupObserver() {
  if (io) io.disconnect()
  const container = getScrollContainer()
  if (!container) return
  io = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !loadingMore.value && !loading.value) {
      loadMore()
    }
  }, { root: container, threshold: 0.1 })
  nextTick(() => {
    if (loadMoreSentinel.value) io.observe(loadMoreSentinel.value)
  })
}

onBeforeUnmount(() => {
  if (io) io.disconnect()
})

// ====== 数据加载 ======

async function handlePinToggle(postId, pinned) {
  try {
    await API.pinPost(postId, pinned)
    const post = posts.value.find(p => p.id === postId)
    if (post) post.isPinned = pinned
  } catch (e) {
    // 置顶失败静默处理
  }
}

function getUserName(userId) {
  const u = users.value.find(u => u.id === userId)
  return u ? u.name : '未知'
}

function resolvePostAuthor(post) {
  if (post.fromUserName) return post.fromUserName
  return getUserName(post.fromUserId || post.userId)
}

async function loadFeed() {
  loading.value = true
  error.value = null
  try {
    const [data, userList] = await Promise.all([
      API.getTimeline(null, PAGE_SIZE),
      API.getUserList(),
    ])
    posts.value = data.list || []
    users.value = userList
    cursor.value = data.nextCursor || null
    hasMore.value = data.hasMore !== undefined ? data.hasMore : !!data.nextCursor
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
    setupObserver()
  }
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  try {
    const data = await API.getTimeline(cursor.value, PAGE_SIZE)
    const newPosts = data.list || []
    posts.value = [...posts.value, ...newPosts]
    cursor.value = data.nextCursor || null
    hasMore.value = data.hasMore !== undefined ? data.hasMore : !!data.nextCursor
    await nextTick()
    setupObserver()
  } catch (e) {
    // 加载更多失败静默处理，不覆盖已有数据
  } finally {
    loadingMore.value = false
  }
}

async function refresh() {
  cursor.value = null
  hasMore.value = true
  await loadFeed()
}

watch(() => props.user, () => {
  cursor.value = null
  hasMore.value = true
  loadFeed()
}, { immediate: true })
</script>

<style scoped>
.feed-scroll {
  -webkit-overflow-scrolling: touch;
}
</style>
