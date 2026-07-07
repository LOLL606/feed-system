<template>
  <div class="phone-stage">
    <LoginPage v-if="!currentUser" @select="handleLogin" :current-id="currentUser?.id" />
    <IPhoneFrame v-else>
      <HeaderBar
        :user="currentUser"
        @post="postModalOpen = true"
        @switch="showLogin = true"
      />
      <TabBar :active="tab" @change="tab = $event" />

      <FeedView v-if="tab === 'feed'" :user="currentUser" />
      <FollowView v-else-if="tab === 'follow'" :user="currentUser" />
      <DashboardView v-else-if="tab === 'dashboard'" :user="currentUser" />

      <PostModal v-if="postModalOpen" @close="postModalOpen = false" @submitted="postModalOpen = false" />

      <LoginPage
        v-if="showLogin"
        class="fixed inset-0 z-50"
        :current-id="currentUser?.id"
        @select="handleSwitch"
      />
    </IPhoneFrame>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUser } from './composables/useUser.js'
import LoginPage from './components/LoginPage.vue'
import HeaderBar from './components/HeaderBar.vue'
import TabBar from './components/TabBar.vue'
import FeedView from './components/FeedView.vue'
import FollowView from './components/FollowView.vue'
import DashboardView from './components/DashboardView.vue'
import PostModal from './components/PostModal.vue'
import IPhoneFrame from './components/IPhoneFrame.vue'

const { currentUser, login, restore } = useUser()
const tab = ref('feed')
const postModalOpen = ref(false)
const showLogin = ref(false)

async function handleLogin(userId) {
  await login(userId)
  tab.value = 'feed'
}

async function handleSwitch(userId) {
  await login(userId)
  showLogin.value = false
  tab.value = 'feed'
}

onMounted(async () => {
  await restore()
})
</script>
