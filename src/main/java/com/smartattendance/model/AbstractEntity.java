// package com.smartattendance.model;

// import java.time.LocalDateTime;

// public class AbstractEntity {
//     private final LocalDateTime createdTime = LocalDateTime.now(); 
//     private LocalDateTime updatedTime = LocalDateTime.now(); // updated before every update

//     protected void updateTimestamp() {
//         updatedTime = LocalDateTime.now();
//     }

//     public LocalDateTime getCreatedTime() {
//         return createdTime;
//     }

//     public LocalDateTime getUpdatedTime() {
//         return updatedTime;
//     }
// }

// // explore how to use supabase with this