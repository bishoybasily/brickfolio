import { clsx, type ClassValue } from 'clsx'
import { type SxProps, type Theme } from '@mui/material/styles'
import { type Thresholds, type ThresholdKey, type Project, type UnitRow } from './types'

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs)
}

export function egp(value: number): string {
  return new Intl.NumberFormat('en-EG', {
    style: 'currency',
    currency: 'EGP',
    maximumFractionDigits: 0,
  }).format(value)
}

export function floorLabel(floor: number): string {
  if (floor === 0) return 'Ground'
  if (floor === 1) return '1st'
  if (floor === 2) return '2nd'
  if (floor === 3) return '3rd'
  return `${floor}th`
}

export function deliveryLabel(months: number): string {
  if (months % 12 === 0) return `${months / 12}y`
  return `${months}mo`
}

export function cellSx(
  value: number | null | undefined,
  key: ThresholdKey,
  thresholds: Thresholds,
): SxProps<Theme> {
  if (value == null) return {}
  const [min, max] = thresholds[key]
  if (min == null && max == null) return {}
  const inRange = (min == null || value >= min) && (max == null || value <= max)
  return inRange
    ? { bgcolor: 'rgba(52,168,83,0.15)', color: 'success.light' }
    : { bgcolor: 'rgba(234,67,53,0.15)', color: 'error.light' }
}

export function flattenProjects(projects: Project[]): UnitRow[] {
  return projects.flatMap((project) =>
    project.units.map((unit) => {
      const total = (unit.indoorArea ?? 0) + (unit.outdoorArea ?? 0)
      return {
        unitId: unit.id,
        developerName: project.developer?.name ?? '—',
        projectId: project.id,
        projectName: project.name ?? '—',
        location: project.location ?? '—',
        deliveryMonths: project.deliveryMonths,
        unitName: unit.name ?? '—',
        type: unit.type,
        floor: unit.floor,
        indoorArea: unit.indoorArea,
        outdoorArea: unit.outdoorArea,
        totalArea: total,
        price: unit.price,
        pricePerIndoor: unit.price / unit.indoorArea,
        pricePerTotal: total > 0 ? unit.price / total : unit.price / unit.indoorArea,
        paymentPlans: project.paymentPlans,
      }
    }),
  )
}

export const defaultThresholds: Thresholds = {
  indoorArea: [null, null],
  outdoorArea: [null, null],
  totalArea: [null, null],
  floor: [null, null],
  price: [null, null],
  pricePerIndoor: [null, null],
  pricePerTotal: [null, null],
  deliveryMonths: [null, null],
}
