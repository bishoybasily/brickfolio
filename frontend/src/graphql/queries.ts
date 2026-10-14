import { gql } from '@apollo/client'

export const GET_ALL_UNITS = gql`
  query GetAllUnits {
    projects {
      id
      name
      location
      deliveryMonths
      developer {
        id
        name
      }
      units {
        id
        name
        indoorArea
        outdoorArea
        floor
        type
        price
      }
      paymentPlans {
        id
        months
        discountPercentage
        payments {
          name
          percentage
          due
        }
      }
    }
  }
`

export const ASSESS_UNIT_CASH_BURN = gql`
  query AssessUnitCashBurn($input: AssesUnitCashBurnRequestDto!) {
    assessUnitSimulateCashBurn(assesUnitCashBurnRequestDto: $input) {
      effectiveUnitPrice
      monthlyInstallment
      monthsSurvived
      totalPaidToDeveloper
      totalUnpaidToDeveloper
      remainingBalance
      paidAmounts {
        month
        amounts
      }
    }
  }
`

export const SIMULATE_CASH_BURN = gql`
  query SimulateCashBurn($input: CashBurnRequestDto!) {
    simulateCashBurn(cashBurnRequestDto: $input) {
      effectiveUnitPrice
      monthlyInstallment
      monthsSurvived
      totalPaidToDeveloper
      totalUnpaidToDeveloper
      remainingBalance
      paidAmounts {
        month
        amounts
      }
    }
  }
`

export const PARSE_OFFER = gql`
  subscription ParseOffer($conversationId: String!, $message: String!) {
    parseOffer(conversationId: $conversationId, message: $message)
  }
`

export const GET_CONVERSATION = gql`
  query GetConversation($id: ID!) {
    conversation(id: $id) {
      id
      messages {
        type
        content
        timestamp
      }
    }
  }
`

export const GET_CONVERSATIONS = gql`
  query GetConversations {
    conversations {
      id
      messages {
        type
        content
        timestamp
      }
    }
  }
`
