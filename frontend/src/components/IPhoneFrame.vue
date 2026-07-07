<template>
  <div class="iphone-frame">
    <!-- 外壳 -->
    <div class="frame-shell">
      <!-- Dynamic Island -->
      <div class="dynamic-island"></div>

      <!-- 屏幕区域 -->
      <div class="screen-container">
        <!-- Status Bar -->
        <div v-if="showStatusBar" class="status-bar">
          <div class="status-left">
            <span class="time">{{ currentTime }}</span>
          </div>
          <div class="status-right">
            <!-- 信号图标 -->
            <svg class="icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M2 22h2V10H2v12zm4-12h2V8H6v10zm4-8h2V5h-2v9zm4-4h2V2h-2v10zm4 8h2V10h-2v10zm4-4h2V6h-2v14z"/>
            </svg>
            <!-- WiFi 图标 -->
            <svg class="icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
            </svg>
            <!-- 电池图标 -->
            <svg class="icon battery" viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.67 4H14V2h-4v2H8.33C7.6 4 7 4.6 7 5.33v15.33C7 21.4 7.6 22 8.33 22h7.33c.74 0 1.34-.6 1.34-1.33V5.33C17 4.6 16.4 4 15.67 4zM15 20H9V6h6v14z"/>
            </svg>
          </div>
        </div>

        <!-- 屏幕内容 -->
        <div class="screen-content">
          <slot></slot>
        </div>

        <!-- Home Indicator -->
        <div class="home-indicator"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

defineProps({
  showStatusBar: {
    type: Boolean,
    default: true
  }
})

const currentTime = ref('')

const updateTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

let timer = null

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.iphone-frame {
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 56px;
}

.frame-shell {
  width: 418px;
  height: 844px;
  background: #1a1a1a;
  padding: 14px;
  border-radius: 50px; /* 宽 418 × 12% ≈ 50；用像素而非百分比，避免矩形产生椭圆角 */
  position: relative;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
}

.dynamic-island {
  position: absolute;
  top: 11px;
  left: 50%;
  transform: translateX(-50%);
  width: 125px;
  height: 37px;
  background: #000;
  border-radius: 20px;
  z-index: 10;
}

.screen-container {
  width: 390px;
  height: 816px;
  background: #EDEDED;
  border-radius: 47px; /* 宽 390 × 12% ≈ 47，与外壳同比例相适配 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  /* 半透明毛玻璃：内容滚动经过时呈磨砂质感 */
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.status-left {
  display: flex;
  align-items: center;
}

.status-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time {
  font-size: 15px;
  font-weight: 600;
  color: #000;
}

.icon {
  width: 18px;
  height: 18px;
  color: #000;
}

.battery {
  width: 26px;
  height: 18px;
}

.screen-content {
  flex: 1;
  min-height: 0; /* flex 子项需 min-height:0 才能让 overflow-y:auto 真正滚动 */
  overflow-y: auto;
  max-height: 844px;
  /* 隐藏滚动条（iOS 真机无可见滚动条） */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.screen-content::-webkit-scrollbar {
  display: none;
}

.home-indicator {
  width: 134px;
  height: 5px;
  background: #fff;
  border-radius: 3px;
  margin: 8px auto 0;
  flex-shrink: 0;
}

/* Phase 2：移动端 < 500px 全屏，去掉外壳边框（真机无需再套假壳） */
@media (max-width: 500px) {
  .frame-shell {
    width: 100vw;
    height: 100vh;
    padding: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .dynamic-island {
    display: none;
  }

  .screen-container {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }

  .home-indicator {
    display: none;
  }
}
</style>
