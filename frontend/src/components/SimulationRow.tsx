import { useState } from 'react'
import { useLazyQuery } from '@apollo/client'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Collapse,
  FormControl,
  Grid,
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
  Tooltip,
  Typography,
} from '@mui/material'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined'
import CloseIcon from '@mui/icons-material/Close'
import { ASSESS_UNIT_CASH_BURN } from '../graphql/queries'
import { type UnitRow, type CashBurnResult, type PaymentPlan } from '../lib/types'
import { egp } from '../lib/utils'

interface Props {
  row: UnitRow
}

export default function SimulationRow({ row }: Props) {
  const [initialDeposit, setInitialDeposit] = useState('')
  const [monthlyDeposit, setMonthlyDeposit] = useState('')
  const [interval, setInterval] = useState(3)

  const [runSimulation, { loading, data, error }] = useLazyQuery(ASSESS_UNIT_CASH_BURN, {
    fetchPolicy: 'no-cache',
  })

  const results: CashBurnResult[] = data?.assessUnitSimulateCashBurn ?? []

  function handleSimulate() {
    if (!initialDeposit) return
    runSimulation({
      variables: {
        input: {
          unitId: row.unitId,
          initialDeposit: parseFloat(initialDeposit),
          monthlyDeposit: parseFloat(monthlyDeposit || '0'),
          installmentIntervalMonths: interval,
        },
      },
    })
  }

  return (
    <Box sx={{ px: 3, py: 3, bgcolor: 'action.hover' }}>
      {/* Inputs */}
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        alignItems={{ sm: 'flex-end' }}
        sx={{ mb: 3 }}
      >
        <TextField
          label="Initial deposit (EGP)"
          type="number"
          value={initialDeposit}
          onChange={(e) => setInitialDeposit(e.target.value)}
          placeholder="e.g. 5000000"
          size="small"
          sx={{ minWidth: 200 }}
        />
        <TextField
          label="Monthly deposit (EGP)"
          type="number"
          value={monthlyDeposit}
          onChange={(e) => setMonthlyDeposit(e.target.value)}
          placeholder="e.g. 50000"
          size="small"
          sx={{ minWidth: 200 }}
        />
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Installment interval</InputLabel>
          <Select
            value={interval}
            label="Installment interval"
            onChange={(e) => setInterval(Number(e.target.value))}
          >
            <MenuItem value={1}>Monthly</MenuItem>
            <MenuItem value={3}>Quarterly</MenuItem>
            <MenuItem value={6}>Semi-annual</MenuItem>
            <MenuItem value={12}>Annual</MenuItem>
          </Select>
        </FormControl>
        <Button
          variant="contained"
          startIcon={<PlayArrowIcon />}
          onClick={handleSimulate}
          disabled={!initialDeposit || loading}
          size="small"
        >
          {loading ? 'Simulating…' : 'Simulate'}
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 3, fontSize: '0.8125rem' }}>
          {error.message}
        </Alert>
      )}

      {/* Plan cards */}
      {results.length > 0 && (
        <Grid container spacing={2}>
          {row.paymentPlans.map((plan, i) => {
            const result = results[i]
            if (!result) return null
            return (
              <Grid key={plan.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                <PlanCard plan={plan} result={result} />
              </Grid>
            )
          })}
        </Grid>
      )}
    </Box>
  )
}

function PlanCard({ plan, result }: { plan: PaymentPlan; result: CashBurnResult }) {
  const [showDetail, setShowDetail] = useState(false)

  const survived = result.monthsSurvived
  const total = plan.months
  const progress = Math.min((survived / total) * 100, 100)
  const fullyFunded = survived >= total

  return (
    <Card variant="outlined" sx={{ borderRadius: 1, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flex: 1, p: 2.5, '&:last-child': { pb: 2.5 } }}>

        {/* Header */}
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
          <Typography variant="subtitle2" fontWeight={600}>{plan.months} mo</Typography>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="caption" color="text.secondary">
              {(plan.discountPercentage ?? 0) > 0 ? `${plan.discountPercentage}% off` : 'No discount'}
            </Typography>
            <Tooltip title={showDetail ? 'Hide breakdown' : 'Payment breakdown'}>
              <IconButton size="small" onClick={() => setShowDetail((v) => !v)}>
                {showDetail ? <CloseIcon fontSize="small" /> : <InfoOutlinedIcon fontSize="small" />}
              </IconButton>
            </Tooltip>
          </Stack>
        </Stack>

        {/* Key metrics */}
        <Stack spacing={1} sx={{ mb: 2 }}>
          <Typography variant="caption" color="text.secondary">
            Effective price{' '}
            <Typography component="span" variant="caption" fontWeight={600} color="text.primary">
              {egp(result.effectiveUnitPrice)}
            </Typography>
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Monthly installment{' '}
            <Typography component="span" variant="caption" fontWeight={600} color="text.primary">
              {egp(result.monthlyInstallment)}
            </Typography>
          </Typography>
        </Stack>

        {/* Scheduled payments table */}
        {plan.payments.length > 0 && (
          <Paper variant="outlined" sx={{ mb: 2, borderRadius: 1, overflow: 'hidden' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Payment</TableCell>
                  <TableCell align="right">%</TableCell>
                  <TableCell align="right">Due</TableCell>
                  <TableCell align="right">Amount</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {plan.payments.map((p, pi) => (
                  <TableRow key={pi}>
                    <TableCell sx={{ textTransform: 'capitalize' }}>{p.name}</TableCell>
                    <TableCell align="right" sx={{ color: 'text.secondary' }}>{p.percentage}%</TableCell>
                    <TableCell align="right" sx={{ color: 'text.secondary' }}>
                      {p.due === 0 ? 'Signing' : `Mo ${p.due}`}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 500 }}>
                      {egp((result.effectiveUnitPrice * p.percentage) / 100)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        )}

        {/* Progress bar */}
        <Box sx={{ mb: 2 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
            <Typography variant="caption" color="text.secondary">
              Survived{' '}
              <Typography component="span" variant="caption" fontWeight={600} color="text.primary">
                {survived}
              </Typography>
              <Typography component="span" variant="caption" color="text.disabled"> / {total} mo</Typography>
            </Typography>
            {fullyFunded && (
              <Typography variant="caption" color="success.main" fontWeight={600}>
                Fully funded
              </Typography>
            )}
          </Stack>
          <LinearProgress
            variant="determinate"
            value={progress}
            color={fullyFunded ? 'success' : 'warning'}
            sx={{ height: 8, borderRadius: 4 }}
          />
        </Box>

        {/* Stats */}
        <Grid container spacing={2} sx={{ pt: 2, borderTop: 1, borderColor: 'divider' }}>
          <Grid size={4}>
            <Typography variant="caption" color="text.secondary" display="block">Paid</Typography>
            <Typography variant="caption" fontWeight={500}>{egp(result.totalPaidToDeveloper)}</Typography>
          </Grid>
          <Grid size={4}>
            <Typography variant="caption" color="text.secondary" display="block">Unpaid</Typography>
            <Typography variant="caption" fontWeight={500}>{egp(result.totalUnpaidToDeveloper)}</Typography>
          </Grid>
          <Grid size={4}>
            <Typography variant="caption" color="text.secondary" display="block">Balance</Typography>
            <Typography
              variant="caption"
              fontWeight={600}
              sx={{ color: result.remainingBalance >= 0 ? 'success.main' : 'error.main' }}
            >
              {egp(result.remainingBalance)}
            </Typography>
          </Grid>
        </Grid>

        {/* Monthly payment detail */}
        <Collapse in={showDetail}>
          {result.paidAmounts?.length > 0 && (
            <Paper variant="outlined" sx={{ mt: 2, borderRadius: 1, overflow: 'hidden' }}>
              <Box sx={{ maxHeight: 220, overflow: 'auto' }}>
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
                        <TableCell sx={{ color: 'text.secondary' }}>
                          {entry.month === 0 ? 'Signing' : `Mo ${entry.month}`}
                        </TableCell>
                        <TableCell align="right">
                          {entry.amounts.map((a, ai) => (
                            <Box key={ai} sx={{ fontSize: '0.75rem' }}>{egp(a)}</Box>
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
          )}
        </Collapse>
      </CardContent>
    </Card>
  )
}
