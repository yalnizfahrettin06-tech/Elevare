package com.elevare.active

import java.time.LocalDate

/** Legacy summaries remain explicitly unreviewed; approval requires a valid review window. */
fun factVisible(f:ScienceFact,today:LocalDate):Boolean {
 if(f.status=="legacy_source_summary")return true
 if(f.status !in setOf("published","publishable")||f.reviewer.isBlank())return false
 val reviewed=runCatching{LocalDate.parse(f.reviewedAt)}.getOrNull()?:return false
 val due=runCatching{LocalDate.parse(f.nextReviewAt)}.getOrNull()?:return false
 return reviewed<=today && due>=today && due>=reviewed
}

