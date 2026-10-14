import { useRef, useState, useCallback } from 'react'
import { Box, Paper, Typography, IconButton, Divider, useMediaQuery, useTheme, Drawer } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import ChatView from '../views/ChatView'

interface Props {
  onClose: () => void
}

const MIN_W = 340
const MIN_H = 400

export default function ChatModal({ onClose }: Props) {
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))

  const [size, setSize] = useState({ w: 440, h: 600 })
  const dragRef = useRef<{
    edge: 'left' | 'top' | 'corner'
    startX: number
    startY: number
    startW: number
    startH: number
  } | null>(null)

  const onMouseDown = useCallback(
    (edge: 'left' | 'top' | 'corner') => (e: React.MouseEvent) => {
      e.preventDefault()
      dragRef.current = { edge, startX: e.clientX, startY: e.clientY, startW: size.w, startH: size.h }

      function onMove(ev: MouseEvent) {
        if (!dragRef.current) return
        const { edge, startX, startY, startW, startH } = dragRef.current
        const dx = startX - ev.clientX
        const dy = startY - ev.clientY
        setSize({
          w: edge === 'top' ? startW : Math.max(MIN_W, startW + dx),
          h: edge === 'left' ? startH : Math.max(MIN_H, startH + dy),
        })
      }

      function onUp() {
        dragRef.current = null
        window.removeEventListener('mousemove', onMove)
        window.removeEventListener('mouseup', onUp)
      }

      window.addEventListener('mousemove', onMove)
      window.addEventListener('mouseup', onUp)
    },
    [size],
  )

  if (isMobile) {
    return (
      <Drawer
        anchor="bottom"
        open
        onClose={onClose}
        slotProps={{
          paper: {
            sx: { height: '85dvh', borderRadius: '8px 8px 0 0', display: 'flex', flexDirection: 'column' },
          },
        }}
      >
        <ModalHeader onClose={onClose} />
        <Box sx={{ flex: 1, overflow: 'hidden' }}>
          <ChatView />
        </Box>
      </Drawer>
    )
  }

  return (
    <>
      {/* Backdrop */}
      <Box
        sx={{ position: 'fixed', inset: 0, zIndex: 40, bgcolor: 'rgba(0,0,0,0.4)' }}
        onClick={onClose}
      />

      {/* Floating panel anchored bottom-right */}
      <Paper
        elevation={8}
        sx={{
          position: 'fixed',
          bottom: 88,
          right: 24,
          zIndex: 50,
          width: size.w,
          height: size.h,
          display: 'flex',
          flexDirection: 'column',
          borderRadius: 1,
          overflow: 'hidden',
          border: 1,
          borderColor: 'divider',
        }}
      >
        {/* Top resize handle */}
        <Box
          onMouseDown={onMouseDown('top')}
          sx={{
            position: 'absolute', top: 0, left: 16, right: 16, height: 6,
            cursor: 'n-resize', zIndex: 10,
            '&:hover': { bgcolor: 'action.hover' },
          }}
        />
        {/* Left resize handle */}
        <Box
          onMouseDown={onMouseDown('left')}
          sx={{
            position: 'absolute', left: 0, top: 16, bottom: 16, width: 6,
            cursor: 'w-resize', zIndex: 10,
            '&:hover': { bgcolor: 'action.hover' },
          }}
        />
        {/* Corner handle */}
        <Box
          onMouseDown={onMouseDown('corner')}
          sx={{ position: 'absolute', top: 0, left: 0, width: 16, height: 16, cursor: 'nw-resize', zIndex: 20 }}
        />

        <ModalHeader onClose={onClose} />

        <Box sx={{ flex: 1, overflow: 'hidden' }}>
          <ChatView />
        </Box>
      </Paper>
    </>
  )
}

function ModalHeader({ onClose }: { onClose: () => void }) {
  return (
    <>
      <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 1.5, flexShrink: 0 }}>
        <Typography variant="subtitle2" fontWeight={600} sx={{ flex: 1 }}>
          Chat
        </Typography>
        <IconButton size="small" onClick={onClose}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </Box>
      <Divider />
    </>
  )
}
