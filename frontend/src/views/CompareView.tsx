import { useState } from 'react'
import { useQuery } from '@apollo/client'
import { useMediaQuery, useTheme } from '@mui/material'
import {
  Box,
  Paper,
  Toolbar,
  Typography,
  Button,
  Menu,
  MenuItem,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Chip,
  Collapse,
  CircularProgress,
  Alert,
  Badge,
  Divider,
} from '@mui/material'
import ViewColumnIcon from '@mui/icons-material/ViewColumn'
import TuneIcon from '@mui/icons-material/Tune'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'
import { GET_ALL_UNITS } from '../graphql/queries'
import { type Thresholds, type ThresholdKey, type UnitRow } from '../lib/types'
import { cellSx, egp, floorLabel, deliveryLabel, flattenProjects, defaultThresholds } from '../lib/utils'
import ThresholdPanel from '../components/ThresholdPanel'
import SimulationRow from '../components/SimulationRow'

type SortKey = keyof Pick<
  UnitRow,
  | 'developerName'
  | 'projectName'
  | 'floor'
  | 'indoorArea'
  | 'outdoorArea'
  | 'totalArea'
  | 'price'
  | 'pricePerIndoor'
  | 'pricePerTotal'
  | 'deliveryMonths'
>

type ColumnId =
  | 'developer'
  | 'project'
  | 'location'
  | 'delivery'
  | 'unitName'
  | 'type'
  | 'floor'
  | 'indoorArea'
  | 'outdoorArea'
  | 'totalArea'
  | 'price'
  | 'pricePerIndoor'
  | 'pricePerTotal'
  | 'plans'

interface ColDef {
  id: ColumnId
  label: string
  sortKey?: SortKey
  desktopDefault: boolean
  mobileDefault: boolean
}

const COLUMNS: ColDef[] = [
  { id: 'developer', label: 'Developer', sortKey: 'developerName', desktopDefault: true, mobileDefault: false },
  { id: 'project', label: 'Project', sortKey: 'projectName', desktopDefault: true, mobileDefault: true },
  { id: 'location', label: 'Location', desktopDefault: true, mobileDefault: false },
  { id: 'delivery', label: 'Delivery', sortKey: 'deliveryMonths', desktopDefault: true, mobileDefault: false },
  { id: 'unitName', label: 'Unit', desktopDefault: true, mobileDefault: true },
  { id: 'type', label: 'Type', desktopDefault: true, mobileDefault: false },
  { id: 'floor', label: 'Floor', sortKey: 'floor', desktopDefault: true, mobileDefault: false },
  { id: 'indoorArea', label: 'Indoor m²', sortKey: 'indoorArea', desktopDefault: true, mobileDefault: false },
  { id: 'outdoorArea', label: 'Outdoor m²', sortKey: 'outdoorArea', desktopDefault: true, mobileDefault: false },
  { id: 'totalArea', label: 'Total m²', sortKey: 'totalArea', desktopDefault: true, mobileDefault: false },
  { id: 'price', label: 'Price', sortKey: 'price', desktopDefault: true, mobileDefault: true },
  { id: 'pricePerIndoor', label: 'EGP/m² (In)', sortKey: 'pricePerIndoor', desktopDefault: true, mobileDefault: false },
  { id: 'pricePerTotal', label: 'EGP/m² (Tot)', sortKey: 'pricePerTotal', desktopDefault: true, mobileDefault: false },
  { id: 'plans', label: 'Plans', desktopDefault: true, mobileDefault: true },
]

const UNIT_TYPE_COLORS = {
  RETAIL: 'primary',
  APARTMENT: 'success',
  OFFICE: 'info',
  COMMERCIAL: 'warning',
} as const

function activeFilterCount(thresholds: Thresholds): number {
  return Object.values(thresholds).filter(([min, max]) => min != null || max != null).length
}

export default function CompareView() {
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))

  const { loading, error, data } = useQuery(GET_ALL_UNITS)
  const [thresholds, setThresholds] = useState<Thresholds>(defaultThresholds)
  const [sortKey, setSortKey] = useState<SortKey>('pricePerIndoor')
  const [sortAsc, setSortAsc] = useState(true)
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set())
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [colsAnchor, setColsAnchor] = useState<HTMLElement | null>(null)

  const [visibleCols, setVisibleCols] = useState<Record<ColumnId, boolean>>(() =>
    Object.fromEntries(
      COLUMNS.map((c) => [c.id, isMobile ? c.mobileDefault : c.desktopDefault]),
    ) as Record<ColumnId, boolean>,
  )

  function handleThresholdChange(key: ThresholdKey, minOrMax: 0 | 1, value: string) {
    setThresholds((prev) => {
      const pair = [...prev[key]] as [number | null, number | null]
      pair[minOrMax] = value === '' ? null : Number(value)
      return { ...prev, [key]: pair }
    })
  }

  function handleSort(key: SortKey) {
    if (sortKey === key) setSortAsc((v) => !v)
    else { setSortKey(key); setSortAsc(true) }
  }

  function toggleRow(unitId: string) {
    setExpandedRows((prev) => {
      const next = new Set(prev)
      next.has(unitId) ? next.delete(unitId) : next.add(unitId)
      return next
    })
  }

  function toggleCol(id: ColumnId) {
    setVisibleCols((prev) => ({ ...prev, [id]: !prev[id] }))
  }

  const rows: UnitRow[] = data ? flattenProjects(data.projects) : []
  const sorted = [...rows].sort((a, b) => {
    const av = a[sortKey] as number | string
    const bv = b[sortKey] as number | string
    if (av < bv) return sortAsc ? -1 : 1
    if (av > bv) return sortAsc ? 1 : -1
    return 0
  })

  const visibleColumns = COLUMNS.filter((c) => visibleCols[c.id])
  const colSpan = visibleColumns.length + 1
  const filterCount = activeFilterCount(thresholds)

  if (loading) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 2 }}>
        <CircularProgress size={24} />
        <Typography variant="body2" color="text.secondary">Loading units…</Typography>
      </Box>
    )
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error.message}</Alert>
      </Box>
    )
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>

      {/* Toolbar */}
      <Paper elevation={0} sx={{ borderBottom: 1, borderColor: 'divider', borderRadius: 0, flexShrink: 0 }}>
        <Toolbar variant="dense" sx={{ minHeight: 48, px: 2, gap: 1 }}>
          <Typography variant="body2" color="text.secondary" sx={{ flexGrow: 1, fontSize: '0.8125rem' }}>
            {sorted.length} unit{sorted.length !== 1 ? 's' : ''}
          </Typography>

          <Badge badgeContent={filterCount || undefined} color="primary">
            <Button
              size="small"
              startIcon={filtersOpen ? <ExpandLessIcon fontSize="small" /> : <TuneIcon fontSize="small" />}
              variant={filterCount > 0 ? 'contained' : 'outlined'}
              onClick={() => setFiltersOpen((v) => !v)}
            >
              Filters
            </Button>
          </Badge>

          <Button
            size="small"
            startIcon={<ViewColumnIcon fontSize="small" />}
            variant="outlined"
            onClick={(e) => setColsAnchor(e.currentTarget)}
          >
            Columns
          </Button>
        </Toolbar>

        {/* Inline threshold panel */}
        <Collapse in={filtersOpen}>
          <Divider />
          <ThresholdPanel thresholds={thresholds} onChange={handleThresholdChange} />
        </Collapse>
      </Paper>

      {/* Table */}
      <TableContainer sx={{ flex: 1, overflow: 'auto' }}>
        <Table stickyHeader size="small">
          <TableHead>
            <TableRow>
              <TableCell sx={{ width: 36, px: 1 }} />
              {visibleColumns.map((col) => (
                <TableCell key={col.id} sx={{ whiteSpace: 'nowrap' }}>
                  {col.sortKey ? (
                    <TableSortLabel
                      active={sortKey === col.sortKey}
                      direction={sortKey === col.sortKey ? (sortAsc ? 'asc' : 'desc') : 'asc'}
                      onClick={() => handleSort(col.sortKey!)}
                    >
                      {col.label}
                    </TableSortLabel>
                  ) : (
                    col.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>

          <TableBody>
            {sorted.map((row) => {
              const expanded = expandedRows.has(row.unitId)
              return (
                <>
                  <TableRow
                    key={row.unitId}
                    onClick={() => toggleRow(row.unitId)}
                    hover
                    selected={expanded}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell sx={{ width: 36, px: 1, color: 'text.secondary' }}>
                      {expanded
                        ? <KeyboardArrowDownIcon fontSize="small" />
                        : <KeyboardArrowRightIcon fontSize="small" />}
                    </TableCell>

                    {visibleCols.developer && (
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>{row.developerName}</TableCell>
                    )}
                    {visibleCols.project && (
                      <TableCell sx={{ whiteSpace: 'nowrap', fontWeight: 500 }}>{row.projectName}</TableCell>
                    )}
                    {visibleCols.location && (
                      <TableCell sx={{ whiteSpace: 'nowrap', color: 'text.secondary' }}>{row.location}</TableCell>
                    )}
                    {visibleCols.delivery && (
                      <TableCell sx={{ whiteSpace: 'nowrap', ...cellSx(row.deliveryMonths, 'deliveryMonths', thresholds) }}>
                        {deliveryLabel(row.deliveryMonths)}
                      </TableCell>
                    )}
                    {visibleCols.unitName && (
                      <TableCell sx={{ fontFamily: '"Roboto Mono", monospace', fontSize: '0.75rem' }}>
                        {row.unitName}
                      </TableCell>
                    )}
                    {visibleCols.type && (
                      <TableCell>
                        <Chip
                          label={row.type}
                          size="small"
                          color={UNIT_TYPE_COLORS[row.type] ?? 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                    )}
                    {visibleCols.floor && (
                      <TableCell sx={cellSx(row.floor, 'floor', thresholds)}>
                        {floorLabel(row.floor)}
                      </TableCell>
                    )}
                    {visibleCols.indoorArea && (
                      <TableCell sx={cellSx(row.indoorArea, 'indoorArea', thresholds)}>
                        {row.indoorArea}
                      </TableCell>
                    )}
                    {visibleCols.outdoorArea && (
                      <TableCell sx={cellSx(row.outdoorArea ?? undefined, 'outdoorArea', thresholds)}>
                        {row.outdoorArea ?? '—'}
                      </TableCell>
                    )}
                    {visibleCols.totalArea && (
                      <TableCell sx={cellSx(row.totalArea, 'totalArea', thresholds)}>
                        {row.totalArea.toFixed(1)}
                      </TableCell>
                    )}
                    {visibleCols.price && (
                      <TableCell sx={{ whiteSpace: 'nowrap', ...cellSx(row.price, 'price', thresholds) }}>
                        {egp(row.price)}
                      </TableCell>
                    )}
                    {visibleCols.pricePerIndoor && (
                      <TableCell sx={{ whiteSpace: 'nowrap', ...cellSx(row.pricePerIndoor, 'pricePerIndoor', thresholds) }}>
                        {egp(row.pricePerIndoor)}
                      </TableCell>
                    )}
                    {visibleCols.pricePerTotal && (
                      <TableCell sx={{ whiteSpace: 'nowrap', ...cellSx(row.pricePerTotal, 'pricePerTotal', thresholds) }}>
                        {egp(row.pricePerTotal)}
                      </TableCell>
                    )}
                    {visibleCols.plans && (
                      <TableCell>
                        <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.8125rem' }}>
                          {row.paymentPlans.length} plan{row.paymentPlans.length !== 1 ? 's' : ''}
                        </Typography>
                      </TableCell>
                    )}
                  </TableRow>

                  <TableRow key={`${row.unitId}-sim`}>
                    <TableCell
                      colSpan={colSpan}
                      sx={{ p: 0, borderBottom: expanded ? 1 : 'none', borderColor: 'divider' }}
                    >
                      <Collapse in={expanded} unmountOnExit={false} mountOnEnter>
                        <SimulationRow row={row} />
                      </Collapse>
                    </TableCell>
                  </TableRow>
                </>
              )
            })}

            {sorted.length === 0 && (
              <TableRow>
                <TableCell colSpan={colSpan} sx={{ py: 8, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    No units yet. Use the chat to parse broker offers.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Column visibility menu */}
      <Menu
        anchorEl={colsAnchor}
        open={Boolean(colsAnchor)}
        onClose={() => setColsAnchor(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        slotProps={{ paper: { sx: { minWidth: 180 } } }}
      >
        {COLUMNS.map((col) => (
          <MenuItem key={col.id} dense onClick={() => toggleCol(col.id)} sx={{ px: 1.5, gap: 1 }}>
            <Checkbox size="small" checked={visibleCols[col.id]} sx={{ p: 0 }} disableRipple />
            <Typography variant="body2">{col.label}</Typography>
          </MenuItem>
        ))}
      </Menu>
    </Box>
  )
}
