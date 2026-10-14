export type UnitType = 'RETAIL' | 'APARTMENT' | 'OFFICE' | 'COMMERCIAL'

export interface Payment {
  id: string
  name: string
  percentage: number
  due: number
}

export interface PaymentPlan {
  id: string
  months: number
  discountPercentage: number
  payments: Payment[]
}

export interface Developer {
  id: string
  name: string
}

export interface Project {
  id: string
  name: string
  location: string
  deliveryMonths: number
  developer: Developer
  units: Unit[]
  paymentPlans: PaymentPlan[]
}

export interface Unit {
  id: string
  name: string
  indoorArea: number
  outdoorArea: number | null
  floor: number
  type: UnitType
  price: number
}

// Flat row used in the compare table
export interface UnitRow {
  unitId: string
  developerName: string
  projectId: string
  projectName: string
  location: string
  deliveryMonths: number
  unitName: string
  type: UnitType
  floor: number
  indoorArea: number
  outdoorArea: number | null
  totalArea: number
  price: number
  pricePerIndoor: number
  pricePerTotal: number
  paymentPlans: PaymentPlan[]
}

export interface MonthlyPayments {
  month: number
  amounts: number[]
}

export interface CashBurnResult {
  effectiveUnitPrice: number
  monthlyInstallment: number
  monthsSurvived: number
  totalPaidToDeveloper: number
  totalUnpaidToDeveloper: number
  remainingBalance: number
  paidAmounts: MonthlyPayments[]
}

export interface Thresholds {
  indoorArea: [number | null, number | null]
  outdoorArea: [number | null, number | null]
  totalArea: [number | null, number | null]
  floor: [number | null, number | null]
  price: [number | null, number | null]
  pricePerIndoor: [number | null, number | null]
  pricePerTotal: [number | null, number | null]
  deliveryMonths: [number | null, number | null]
}

export type ThresholdKey = keyof Thresholds
