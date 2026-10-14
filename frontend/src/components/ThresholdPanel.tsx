import { Box, Typography, TextField, Stack } from '@mui/material'
import { type Thresholds, type ThresholdKey } from '../lib/types'

interface Props {
  thresholds: Thresholds
  onChange: (key: ThresholdKey, minOrMax: 0 | 1, value: string) => void
}

const FIELDS: { key: ThresholdKey; label: string; unit?: string }[] = [
  { key: 'indoorArea', label: 'Indoor Area', unit: 'm²' },
  { key: 'outdoorArea', label: 'Outdoor Area', unit: 'm²' },
  { key: 'totalArea', label: 'Total Area', unit: 'm²' },
  { key: 'floor', label: 'Floor' },
  { key: 'price', label: 'Price', unit: 'EGP' },
  { key: 'pricePerIndoor', label: 'Price / Indoor m²', unit: 'EGP' },
  { key: 'pricePerTotal', label: 'Price / Total m²', unit: 'EGP' },
  { key: 'deliveryMonths', label: 'Delivery', unit: 'mo' },
]

export default function ThresholdPanel({ thresholds, onChange }: Props) {
  return (
    <Box
      sx={{
        px: 2,
        py: 1.5,
        display: 'flex',
        flexWrap: 'wrap',
        gap: 2,
      }}
    >
      {FIELDS.map(({ key, label, unit }) => (
        <Box key={key} sx={{ minWidth: 180, flex: '1 1 180px' }}>
          <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block', whiteSpace: 'nowrap' }}>
            {label}
            {unit && (
              <Typography component="span" variant="caption" color="text.disabled" sx={{ ml: 0.5 }}>
                ({unit})
              </Typography>
            )}
          </Typography>
          <Stack direction="row" spacing={0.5} alignItems="center">
            <TextField
              type="number"
              placeholder="min"
              size="small"
              value={thresholds[key][0] ?? ''}
              onChange={(e) => onChange(key, 0, e.target.value)}
              sx={{ flex: 1, minWidth: 0 }}
              slotProps={{ htmlInput: { style: { fontSize: '0.8125rem', padding: '5px 8px' } } }}
            />
            <Typography variant="caption" color="text.disabled" sx={{ flexShrink: 0 }}>—</Typography>
            <TextField
              type="number"
              placeholder="max"
              size="small"
              value={thresholds[key][1] ?? ''}
              onChange={(e) => onChange(key, 1, e.target.value)}
              sx={{ flex: 1, minWidth: 0 }}
              slotProps={{ htmlInput: { style: { fontSize: '0.8125rem', padding: '5px 8px' } } }}
            />
          </Stack>
        </Box>
      ))}
    </Box>
  )
}
