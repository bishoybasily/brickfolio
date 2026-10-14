import { useState, useRef, useEffect, useCallback } from 'react'
import { useApolloClient, useQuery } from '@apollo/client'
import {
  Box,
  IconButton,
  TextField,
  Typography,
  Paper,
  CircularProgress,
  Tooltip,
} from '@mui/material'
import SendIcon from '@mui/icons-material/Send'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import ReactMarkdown from 'react-markdown'
import { PARSE_OFFER, GET_CONVERSATION } from '../graphql/queries'

const CONVERSATION_ID_KEY = 'brickfolio_conversation_id'

function getOrCreateConversationId(): string {
  let id = localStorage.getItem(CONVERSATION_ID_KEY)
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem(CONVERSATION_ID_KEY, id)
  }
  return id
}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

export default function ChatView() {
  const apolloClient = useApolloClient()
  const [conversationId] = useState<string>(getOrCreateConversationId)
  const [messages, setMessages] = useState<Message[]>([])
  const [draft, setDraft] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const [inputHeight, setInputHeight] = useState(96)
  const [historyLoaded, setHistoryLoaded] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)
  const idRef = useRef(0)
  const subscriptionRef = useRef<{ unsubscribe: () => void } | null>(null)
  const inputDragRef = useRef<{ startY: number; startH: number } | null>(null)

  const { data: historyData, error: historyError } = useQuery(GET_CONVERSATION, {
    variables: { id: conversationId },
    skip: historyLoaded,
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (!historyData || historyLoaded) return
    const msgs = historyData.conversation?.messages ?? []
    const loaded: Message[] = msgs
      .filter((m: { type: string; content: string }) => m.type === 'user' || m.type === 'assistant')
      .map((m: { type: string; content: string }) => ({
        id: idRef.current++,
        role: m.type as 'user' | 'assistant',
        content: m.content,
      }))
    setMessages(loaded)
    setHistoryLoaded(true)
  }, [historyData, historyLoaded])

  useEffect(() => {
    if (historyError && !historyLoaded) setHistoryLoaded(true)
  }, [historyError, historyLoaded])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const onInputHandleMouseDown = useCallback(
    (e: React.MouseEvent) => {
      e.preventDefault()
      inputDragRef.current = { startY: e.clientY, startH: inputHeight }
      function onMove(ev: MouseEvent) {
        if (!inputDragRef.current) return
        const dy = inputDragRef.current.startY - ev.clientY
        setInputHeight(Math.max(64, Math.min(400, inputDragRef.current.startH + dy)))
      }
      function onUp() {
        inputDragRef.current = null
        window.removeEventListener('mousemove', onMove)
        window.removeEventListener('mouseup', onUp)
      }
      window.addEventListener('mousemove', onMove)
      window.addEventListener('mouseup', onUp)
    },
    [inputHeight],
  )

  function handleSend() {
    const text = draft.trim()
    if (!text || isStreaming) return

    setDraft('')
    setMessages((prev) => [...prev, { id: idRef.current++, role: 'user', content: text }])
    const streamMsgId = idRef.current++
    setMessages((prev) => [
      ...prev,
      { id: streamMsgId, role: 'assistant', content: '', streaming: true },
    ])
    setIsStreaming(true)

    subscriptionRef.current = apolloClient
      .subscribe({ query: PARSE_OFFER, variables: { conversationId, message: text } })
      .subscribe({
        next({ data }) {
          const token: string = data?.parseOffer ?? ''
          setMessages((prev) =>
            prev.map((m) => (m.id === streamMsgId ? { ...m, content: m.content + token } : m)),
          )
        },
        error(err) {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === streamMsgId
                ? { ...m, content: `**Error:** ${err.message}`, streaming: false }
                : m,
            ),
          )
          setIsStreaming(false)
        },
        complete() {
          setMessages((prev) =>
            prev.map((m) => (m.id === streamMsgId ? { ...m, streaming: false } : m)),
          )
          setIsStreaming(false)
        },
      })
  }

  function handleClear() {
    subscriptionRef.current?.unsubscribe()
    subscriptionRef.current = null
    const newId = crypto.randomUUID()
    localStorage.setItem(CONVERSATION_ID_KEY, newId)
    setMessages([])
    setIsStreaming(false)
    window.location.reload()
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLDivElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Header */}
      {messages.length > 0 && (
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'flex-end',
            px: 2,
            py: 1,
            borderBottom: 1,
            borderColor: 'divider',
            flexShrink: 0,
          }}
        >
          <Tooltip title="New conversation">
            <IconButton size="small" onClick={handleClear} color="inherit">
              <DeleteOutlineIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      )}

      {/* Messages */}
      <Box sx={{ flex: 1, overflowY: 'auto', px: 2, py: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
        {messages.length === 0 && historyLoaded && (
          <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Typography variant="body2" color="text.secondary" align="center">
              Paste a broker offer message to get started.
            </Typography>
          </Box>
        )}

        {messages.map((msg) => (
          <Box
            key={msg.id}
            sx={{ display: 'flex', justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start' }}
          >
            <Paper
              elevation={0}
              sx={{
                maxWidth: '85%',
                px: 2,
                py: 1.5,
                borderRadius: msg.role === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                bgcolor: msg.role === 'user' ? 'primary.main' : 'action.hover',
                color: msg.role === 'user' ? 'primary.contrastText' : 'text.primary',
                fontSize: '0.8125rem',
                lineHeight: 1.6,
                '& p': { m: 0 },
                '& p + p': { mt: 1 },
                '& pre': {
                  bgcolor: 'rgba(0,0,0,0.15)',
                  borderRadius: 1,
                  p: 1.5,
                  overflowX: 'auto',
                  fontSize: '0.75rem',
                },
                '& code': { fontSize: '0.75rem' },
                '& ul, & ol': { pl: 2.5, my: 0.5 },
              }}
            >
              {msg.role === 'user' ? (
                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', fontSize: 'inherit', color: 'inherit' }}>
                  {msg.content}
                </Typography>
              ) : msg.content ? (
                <ReactMarkdown>{msg.content}</ReactMarkdown>
              ) : (
                <TypingDots />
              )}
            </Paper>
          </Box>
        ))}

        <div ref={bottomRef} />
      </Box>

      {/* Input area */}
      <Box
        sx={{ flexShrink: 0, borderTop: 1, borderColor: 'divider', height: inputHeight }}
      >
        {/* Drag handle */}
        <Box
          onMouseDown={onInputHandleMouseDown}
          sx={{
            height: 6,
            cursor: 'n-resize',
            '&:hover': { bgcolor: 'action.hover' },
            transition: 'background-color 0.15s',
          }}
        />
        <Box sx={{ display: 'flex', gap: 1.5, px: 2, pt: 1.5, pb: 2, height: 'calc(100% - 6px)' }}>
          <TextField
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Paste broker message… (Enter to send, Shift+Enter for newline)"
            disabled={isStreaming}
            multiline
            fullWidth
            sx={{
              flex: 1,
              '& .MuiOutlinedInput-root': { height: '100%', alignItems: 'flex-start' },
              '& textarea': { height: '100% !important', resize: 'none' },
            }}
            slotProps={{
              input: {
                sx: { height: '100%', alignItems: 'flex-start', fontSize: '0.8125rem', py: 1.5 },
              },
            }}
          />
          <IconButton
            onClick={handleSend}
            disabled={!draft.trim() || isStreaming}
            color="primary"
            sx={{ alignSelf: 'flex-end', bgcolor: 'primary.main', color: 'primary.contrastText',
              '&:hover': { bgcolor: 'primary.dark' },
              '&.Mui-disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' },
            }}
          >
            {isStreaming ? <CircularProgress size={18} color="inherit" /> : <SendIcon fontSize="small" />}
          </IconButton>
        </Box>
      </Box>
    </Box>
  )
}

function TypingDots() {
  return (
    <Box sx={{ display: 'flex', gap: 0.5, alignItems: 'center', py: 0.25 }}>
      {[0, 150, 300].map((delay) => (
        <Box
          key={delay}
          sx={{
            width: 6,
            height: 6,
            borderRadius: '50%',
            bgcolor: 'text.secondary',
            animation: 'bounce 1s infinite',
            animationDelay: `${delay}ms`,
            '@keyframes bounce': {
              '0%, 100%': { transform: 'translateY(0)' },
              '50%': { transform: 'translateY(-4px)' },
            },
          }}
        />
      ))}
    </Box>
  )
}
