<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getBasicUserApi, getMessagesApi, sendMessageApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 在线咨询页：农户与专家的消息对话
 * 采用轮询方式（每 3 秒拉取一次新消息），简单可靠
 */
const route = useRoute()
const userStore = useUserStore()

const other = ref(null)
const messages = ref([])
const input = ref('')
const boxRef = ref(null)
let timer = null

onMounted(async () => {
  try {
    const res = await getBasicUserApi(route.params.userId)
    other.value = res.data
  } catch (e) {
    // 拿不到对方信息也不影响聊天
  }
  await loadMessages()
  timer = setInterval(loadMessages, 3000)   // 每 3 秒轮询新消息
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

async function loadMessages() {
  try {
    const res = await getMessagesApi(route.params.userId)
    messages.value = res.data || []
    scrollToBottom()
  } catch (e) {
    // 拦截器已提示（登录过期会自动跳转登录页）
  }
}

async function send() {
  const content = input.value.trim()
  if (!content) {
    return
  }
  input.value = ''
  try {
    await sendMessageApi(route.params.userId, content)
    await loadMessages()
  } catch (e) {
    // 拦截器已提示
  }
}

function scrollToBottom() {
  setTimeout(() => {
    if (boxRef.value) {
      boxRef.value.scrollTop = boxRef.value.scrollHeight
    }
  }, 50)
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}
</script>

<template>
  <div class="page-container chat-page">
    <div class="chat-box">
      <!-- 对方信息 -->
      <div class="chat-head">
        <img v-if="other?.avatar" :src="other.avatar" class="chat-avatar" alt="头像" />
        <div>
          <div class="chat-name">{{ other?.realName || '在线咨询' }}</div>
          <div class="chat-tip">消息每 3 秒自动刷新，Enter 发送</div>
        </div>
      </div>

      <!-- 消息区 -->
      <div ref="boxRef" class="chat-body">
        <div v-if="!messages.length" class="chat-empty">开始对话吧，向专家描述你的问题 💬</div>
        <div
          v-for="m in messages"
          :key="m.id"
          class="msg-row"
          :class="{ mine: m.senderId === userStore.userInfo?.id }"
        >
          <div class="msg-bubble">
            <div class="msg-content">{{ m.content }}</div>
            <div class="msg-time">{{ m.createTime }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          @keydown="onKeydown"
        />
        <el-button type="primary" class="send-btn" @click="send">发 送</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  max-width: 860px;
}
.chat-box {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 72vh;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}
.chat-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f2f5;
}
.chat-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
}
.chat-name {
  font-size: 16px;
  font-weight: 700;
}
.chat-tip {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 2px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f5f7fa;
}
.chat-empty {
  text-align: center;
  color: #c0c4cc;
  padding-top: 60px;
  font-size: 14px;
}
.msg-row {
  display: flex;
  margin-bottom: 14px;
}
.msg-row.mine {
  justify-content: flex-end;
}
.msg-bubble {
  max-width: 65%;
  background: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.mine .msg-bubble {
  background: #95d475;
  color: #fff;
}
.msg-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  text-align: right;
}
.mine .msg-time {
  color: rgba(255, 255, 255, 0.8);
}

.chat-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 12px 20px;
  border-top: 1px solid #f0f2f5;
}
.send-btn {
  flex-shrink: 0;
}
</style>
