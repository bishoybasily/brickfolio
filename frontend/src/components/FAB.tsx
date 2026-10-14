import { useState } from 'react'
import SpeedDial from '@mui/material/SpeedDial'
import SpeedDialAction from '@mui/material/SpeedDialAction'
import SpeedDialIcon from '@mui/material/SpeedDialIcon'
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline'
import CalculateOutlinedIcon from '@mui/icons-material/CalculateOutlined'
import ChatModal from './ChatModal'
import ManualSimulationModal from './ManualSimulationModal'

type Tool = 'chat' | 'simulation' | null

export default function FAB() {
  const [open, setOpen] = useState(false)
  const [activeTool, setActiveTool] = useState<Tool>(null)

  function openTool(id: Exclude<Tool, null>) {
    setActiveTool(id)
    setOpen(false)
  }

  return (
    <>
      {activeTool === 'chat' && <ChatModal onClose={() => setActiveTool(null)} />}
      {activeTool === 'simulation' && <ManualSimulationModal onClose={() => setActiveTool(null)} />}

      <SpeedDial
        ariaLabel="Tools"
        open={open}
        onOpen={() => setOpen(true)}
        onClose={() => setOpen(false)}
        icon={<SpeedDialIcon />}
        sx={{ position: 'fixed', bottom: 24, right: 24, zIndex: 30 }}
        FabProps={{ color: 'primary', size: 'large' }}
      >
        <SpeedDialAction
          icon={<ChatBubbleOutlineIcon />}
          tooltipTitle="Chat"
          tooltipOpen
          onClick={() => openTool('chat')}
        />
        <SpeedDialAction
          icon={<CalculateOutlinedIcon />}
          tooltipTitle="Simulation"
          tooltipOpen
          onClick={() => openTool('simulation')}
        />
      </SpeedDial>
    </>
  )
}
