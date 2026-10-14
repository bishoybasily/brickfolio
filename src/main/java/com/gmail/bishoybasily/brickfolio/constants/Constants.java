package com.gmail.bishoybasily.brickfolio.constants;

public interface Constants {

  interface Entities {
    String DEVELOPERS = "developers",
        PAYMENT_PLANS = "payment_plans",
        PROJECTS = "projects",
        UNITS = "units",
        CONVERSATIONS = "conversations";
  }

  interface Tables {
    String PAYMENT_PLAN_PAYMENTS = "payment_plan_payments",
        CONVERSATION_MESSAGES = "conversation_messages";
  }

  interface Columns {
    String PAYMENT_PLAN_ID = "payment_plan_id",
        CONVERSATION_ID = "conversation_id";
  }

}
