package edu.univ.erp.domain;

public enum Status {
    Pending,
    Confirmed,
    ENROLLED,      // <-- MUST MATCH DB VALUE EXACTLY (UPPERCASE)
    waitlisted,
    dropped
}
