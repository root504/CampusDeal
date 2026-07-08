import { ref, onMounted, onUnmounted } from 'vue'

/**
 * WebSocket 依赖说明：
 * 需要在 index.html 中通过 CDN 引入 SockJS 和 Stomp.js：
 *   <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
 *   <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>
 * 或者通过 npm 安装：
 *   npm install sockjs-client @stomp/stompjs
 *   然后在组件中 import：
 *   import SockJS from 'sockjs-client'
 *   import { Client } from '@stomp/stompjs'
 */

export function useWebSocket() {
  const connected = ref(false)
  const message = ref(null)
  let stompClient = null
  let reconnectTimer = null

  function connect() {
    const token = localStorage.getItem('token')
    if (!token) return

    if (typeof SockJS === 'undefined' && typeof window !== 'undefined') {
      console.warn('SockJS not loaded, WebSocket will use mock mode')
      return
    }

    try {
      const socket = new SockJS('/ws/chat')
      stompClient = Stomp.over(socket)
      stompClient.debug = null

      stompClient.connect(
        { Authorization: `Bearer ${token}` },
        () => {
          connected.value = true
          subscribeToMessages()
        },
        () => {
          connected.value = false
          scheduleReconnect()
        }
      )
    } catch (e) {
      console.warn('WebSocket connection failed:', e)
      scheduleReconnect()
    }
  }

  function subscribeToMessages() {
    if (!stompClient?.connected) return

    stompClient.subscribe('/user/queue/messages', (msg) => {
      try {
        const body = JSON.parse(msg.body)
        message.value = body
      } catch {
        message.value = msg.body
      }
    })
  }

  function sendMessage(destination, body) {
    if (stompClient?.connected) {
      stompClient.send(destination, {}, JSON.stringify(body))
    }
  }

  function disconnect() {
    clearTimeout(reconnectTimer)
    if (stompClient?.connected) {
      stompClient.disconnect()
    }
    connected.value = false
  }

  function scheduleReconnect() {
    clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      connect()
    }, 5000)
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    message,
    sendMessage,
    connect,
    disconnect
  }
}
