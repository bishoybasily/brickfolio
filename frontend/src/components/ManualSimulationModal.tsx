import { useState } from 'react'
import { useLazyQuery } from '@apollo/client'
import {
  Alert,
  Box,
  Button,
  Collapse,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  IconButton,
  InputLabel,
  LinearProgress,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined'
import { SIMULATE_CASH_BURN } from '../graphql/queries'
import { type CashBurnResult } from '../lib/types'
import { egp } from '../lib/utils'

interface Props {
  onClose: () => void
}

interface ScheduledPayment {
  dueMonth: string
  amountPercentage: string
}

export default function ManualSimulationModal({ onClose }: Props) {
  const [unitPrice, setUnitPrice] = useState('')
  const [initialDiscount, setInitialDiscount] = useState('0')
  const [installmentMonths, setInstallmentMonths] = useState('')
  const [installmentInterval, setInstallmentInterval] = useState(3)
  const [initialDeposit, setInitialDeposit] = useState('')
  const [monthlyDeposit, setMonthlyDeposit] = useState('0')
  const [scheduledPayments, setScheduledPayments] = useState<ScheduledPayment[]>([
    { dueMonth: '0', amountPercentage: '' },
  ])
  const [showPaidAmounts, setShowPaidAmounts] = useState(false)

  const [simulate, { loading, data, error }] = useLazyQuery(SIMULATE_CASH_BURN, {
    fetchPolicy: 'no-cache',
  })

  const result: CashBurnResult | null = data?.simulateCashBurn ?? null

  function addPayment() {
    setScheduledPayments((p) => [...p, { dueMonth: '', amountPercentage: '' }])
  }

  function removePayment(i: number) {
    setScheduledPayments((p) => p.filter((_, idx) => idx !== i))
  }

  function updatePayment(i: number, field: keyof ScheduledPayment, value: string) {
    setScheduledPayments((p) => p.map((row, idx) => (idx === i ? { ...row, [field]: value } : row)))
  }

  function handleSimulate() {
    if (!unitPrice || !installmentMonths || !initialDeposit) return
    simulate({
      variables: {
        input: {
          unitPrice: parseFloat(unitPrice),
          initialDiscountPercentage: parseFloat(initialDiscount || '0'),
          installmentMonths: parseInt(installmentMonths),
          installmentIntervalMonths: installmentInterval,
          initialDeposit: parseFloat(initialDeposit),
          monthlyDeposit: parseFloat(monthlyDeposit || '0'),
          scheduledPayments: scheduledPayments
            .filter((p) => p.dueMonth !== '' && p.amountPercentage !== '')
            .map((p) => ({
              dueMonth: parseInt(p.dueMonth),
              amountPercentage: parseFloat(p.amountPercentage),
            })),
        },
      },
    })
  }

  const survived = result?.monthsSurvived ?? 0
  const total = installmentMonths ? parseInt(installmentMonths) : 0
  const progress = total > 0 ? Math.min((survived / total) * 100, 100) : 0
  const fullyFunded = total > 0 && survived >= total
  const canSimulate = Boolean(unitPrice && installmentMonths && initialDeposit && !loading)

  return (
    <Dialog open onClose={onClose} maxWidth="sm" fullWidth slotProps={{ paper: { sx: { maxHeight: '90vh' } } }}>
      <DialogTitle sx={{ py: 2, px: 3, fontWeight: 600 }}>Simulation</DialogTitle>
      <Divider />

      <DialogContent sx={{ py: 2.5, px: 3 }}>
        <Stack spacing={2}>
          {/* Row 1 */}
          <Stack direction="row" spacing={2}>
            <TextField
              label="Unit price (EGP)"
              type="number"
              value={unitPrice}
              onChange={(e) => setUnitPrice(e.target.value)}
              placeholder="e.g. 9727778"
              size="small"
              sx={{ flex: 1 }}
            />
            <TextField
              label="Initial discount (%)"
              type="number"
              value={initialDiscount}
              onChange={(e) => setInitialDiscount(e.target.value)}
              size="small"
              sx={{ flex: 1 }}
            />
          </Stack>

          {/* Row 2 */}
          <Stack direction="row" spacing={2}>
            <TextField
              label="Installment months"
              type="number"
              value={installmentMonths}
              onChange={(e) => setInstallmentMonths(e.target.value)}
              placeholder="e.g. 72"
              size="small"
              sx={{ flex: 1 }}
            />
            <FormControl size="small" sx={{ flex: 1 }}>
              <InputLabel>Interval</InputLabel>
              <Select
                value={installmentInterval}
                label="Interval"
                onChange={(e) => setInstallmentInterval(Number(e.target.value))}
              >
                <MenuItem value={1}>Monthly</MenuItem>
                <MenuItem value={3}>Quarterly</MenuItem>
                <MenuItem value={6}>Semi-annual</MenuItem>
                <MenuItem value={12}>Annual</MenuItem>
              </Select>
            </FormControl>
          </Stack>

          {/* Row 3 */}
          <Stack direction="row" spacing={2}>
            <TextField
              label="Initial deposit (EGP)"
              type="number"
              value={initialDeposit}
              onChange={(e) => setInitialDeposit(e.target.value)}
              placeholder="e.g. 5000000"
              size="small"
              sx={{ flex: 1 }}
            />
            <TextField
              label="Monthly deposit (EGP)"
              type="number"
              value={monthlyDeposit}
              onChange={(e) => setMonthlyDeposit(e.target.value)}
              size="small"
              sx={{ flex: 1 }}
            />
          </Stack>

          {/* Scheduled payments */}
          <Box>
            <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 1.5 }}>
              <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                Scheduled Payments
              </Typography>
              <Button size="small" startIcon={<AddIcon />} onClick={addPayment}>
                Add
              </Button>
            </Stack>
            <Stack spacing={1.5}>
              {scheduledPayments.map((p, i) => (
                <Stack key={i} direction="row" spacing={1.5} alignItems="center">
                  <TextField
                    type="number"
                    value={p.dueMonth}
                    onChange={(e) => updatePayment(i, 'dueMonth', e.target.value)}
                    placeholder="Due month (0 = signing)"
                    size="small"
                    sx={{ flex: 1 }}
                  />
                  <TextField
                    type="number"
                    value={p.amountPercentage}
                    onChange={(e) => updatePayment(i, 'amountPercentage', e.target.value)}
                    placeholder="Amount (%)"
                    size="small"
                    sx={{ flex: 1 }}
                  />
                  <IconButton size="small" onClick={() => removePayment(i)} color="error">
                    <DeleteOutlineIcon fontSize="small" />
                  </IconButton>
                </Stack>
              ))}
            </Stack>
          </Box>
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error.message}
          </Alert>
        )}

        {/* Result */}
        {result && (
          <Paper variant="outlined" sx={{ mt: 2.5, p: 2, borderRadius: 1 }}>
            {/* Key metrics */}
            <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>
                  Effective price
                </Typography>
                <Typography variant="body2" fontWeight={600}>{egp(result.effectiveUnitPrice)}</Typography>
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>
                  Monthly installment
                </Typography>
                <Typography variant="body2" fontWeight={600}>{egp(result.monthlyInstallment)}</Typography>
              </Box>
            </Stack>

            {/* Progress */}
            <Box sx={{ mb: 2 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Survived{' '}
                  <Typography component="span" variant="caption" fontWeight={600} color="text.primary">
                    {survived}
                  </Typography>
                  {total > 0 && (
                    <Typography component="span" variant="caption" color="text.disabled"> / {total} mo</Typography>
                  )}
                </Typography>
                {fullyFunded && (
                  <Typography variant="caption" color="success.main" fontWeight={600}>Fully funded</Typography>
                )}
              </Stack>
              <LinearProgress
                variant="determinate"
                value={progress}
                color={fullyFunded ? 'success' : 'warning'}
                sx={{ height: 6, borderRadius: 1 }}
              />
            </Box>

            {/* Stats */}
            <Stack direction="row" spacing={2} sx={{ pt: 2, borderTop: 1, borderColor: 'divider' }}>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>Paid</Typography>
                <Typography variant="body2" fontWeight={500}>{egp(result.totalPaidToDeveloper)}</Typography>
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>Unpaid</Typography>
                <Typography variant="body2" fontWeight={500}>{egp(result.totalUnpaidToDeveloper)}</Typography>
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>Balance</Typography>
                <Typography
                  variant="body2"
                  fontWeight={600}
                  sx={{ color: result.remainingBalance >= 0 ? 'success.main' : 'error.main' }}
                >
                  {egp(result.remainingBalance)}
                </Typography>
              </Box>
            </Stack>

            {/* Paid amounts breakdown */}
            {result.paidAmounts?.length > 0 && (
              <Box sx={{ mt: 1.5 }}>
                <Button
                  size="small"
                  startIcon={<InfoOutlinedIcon />}
                  onClick={() => setShowPaidAmounts((v) => !v)}
                  sx={{ color: 'text.secondary', px: 0, fontSize: '0.75rem' }}
                >
                  {showPaidAmounts ? 'Hide' : 'Show'} payment breakdown
                </Button>
                <Collapse in={showPaidAmounts}>
                  <Paper variant="outlined" sx={{ mt: 1, borderRadius: 1, overflow: 'hidden' }}>
                    <Box sx={{ maxHeight: 200, overflow: 'auto' }}>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Month</TableCell>
                            <TableCell align="right">Amounts</TableCell>
                            <TableCell align="right">Total</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {result.paidAmounts.map((entry) => (
                            <TableRow key={entry.month}>
                              <TableCell>{entry.month === 0 ? 'Signing' : `Mo ${entry.month}`}</TableCell>
                              <TableCell align="right">
                                {entry.amounts.map((a, ai) => (
                                  <Box key={ai}>{egp(a)}</Box>
                                ))}
                              </TableCell>
                              <TableCell align="right" sx={{ fontWeight: 600 }}>
                                {egp(entry.amounts.reduce((s, a) => s + a, 0))}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </Box>
                  </Paper>
                </Collapse>
              </Box>
            )}
          </Paper>
        )}
      </DialogContent>

      <Divider />
      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose} color="inherit">Cancel</Button>
        <Button variant="contained" startIcon={<PlayArrowIcon />} onClick={handleSimulate} disabled={!canSimulate}>
          {loading ? 'Simulating…' : 'Simulate'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
