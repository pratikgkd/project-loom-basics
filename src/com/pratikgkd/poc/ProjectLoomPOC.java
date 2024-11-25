package com.pratikgkd.poc;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectLoomPOC {

  private static final Logger logger = Logger.getLogger(ProjectLoomPOC.class.getName());

  private static final int NUM_TASKS = 10000;
  private static final int IO_TASK_DURATION_MS = 100; // Simulate 100ms I/O task

  // Simulated I/O-bound task (e.g., waiting for I/O to complete)
  private static void ioBoundTask() {
    try {
      Thread.sleep(IO_TASK_DURATION_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.log(Level.SEVERE, "Thread interrupted", e);
    }
  }

  // Measure the performance of traditional threads
  private static void measureTraditionalThreadPerformance() throws InterruptedException {
    logger.info("Running tasks with traditional threads...");

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
    Instant start = Instant.now();

    for (int i = 0; i < NUM_TASKS; i++) {
      executor.submit(ProjectLoomPOC::ioBoundTask);
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);
    logger.info("Time taken with traditional threads: " + duration.toMillis() + " ms");

    long totalTasks = executor.getTaskCount();
    logger.info("Total tasks completed: " + totalTasks);
    logger.info("Average time per task: " + (double) duration.toMillis() / totalTasks + " ms");
  }

  // Measure the performance of virtual threads
  private static void measureVirtualThreadPerformance() throws InterruptedException {
    logger.info("Running tasks with virtual threads...");

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    Instant start = Instant.now();

    for (int i = 0; i < NUM_TASKS; i++) {
      executor.submit(ProjectLoomPOC::ioBoundTask);
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);
    logger.info("Time taken with virtual threads: " + duration.toMillis() + " ms");

    // For virtual threads, task count may not be easily retrieved, so we assume all tasks finished
    logger.info("Total tasks completed: " + NUM_TASKS);
    logger.info("Average time per task: " + (double) duration.toMillis() / NUM_TASKS + " ms");
  }

  public static void main(String[] args) throws InterruptedException {
    // Run with traditional threads
    measureTraditionalThreadPerformance();

    // Run with virtual threads
    measureVirtualThreadPerformance();

    // Calculate the percentage improvement
    logger.info("Comparing performance...");
    Instant traditionalStart = Instant.now();
    Instant virtualStart = Instant.now();

    measureTraditionalThreadPerformance();
    Duration traditionalDuration = Duration.between(traditionalStart, Instant.now());

    measureVirtualThreadPerformance();
    Duration virtualDuration = Duration.between(virtualStart, Instant.now());

    double improvement = ((double) (traditionalDuration.toMillis() - virtualDuration.toMillis()) / traditionalDuration.toMillis()) * 100;
    logger.info("Performance improvement with virtual threads: " + improvement + "%");
  }
}
