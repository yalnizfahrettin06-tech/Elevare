package com.elevare.active
const val TRIAL_DAYS=3
const val ONBOARDING_STEPS=11
fun normalizeTrialDays(value:Int)=if(value>0)TRIAL_DAYS else 0
// Legacy validation retained for old tests; height is not collected or used.
fun validHeight(value:String)=value.isBlank()||(value.toIntOrNull()?.let{it in 90..230}==true)
