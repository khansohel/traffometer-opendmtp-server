// ----------------------------------------------------------------------------
// Copyright 2006, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Thread pool manager
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/03  Martin D. Flynn
//      Removed reference to JavaMail api imports
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.util.Vector;

/**
 * A thread pool simulator that simulates threads manipulation. Contains <tt>main</tt> method so
 * that it can be executed independently. The output is like the following:
 * 
 * <pre>
 *  [INFO |10/31 00:58:45] Job 0
 *  [INFO |10/31 00:58:45] Starting Job: [Test_1] 0
 *  [INFO |10/31 00:58:45] Job 1
 *  [INFO |10/31 00:58:45] Starting Job: [Test_2] 1
 *  [INFO |10/31 00:58:46] Job 2
 *  [INFO |10/31 00:58:46] Starting Job: [Test_3] 2
 *  [INFO |10/31 00:58:47] Job 3
 *  [INFO |10/31 00:58:47] Stopping Job:                [Test_1] 0
 *  [INFO |10/31 00:58:47] Starting Job: [Test_1] 3
 *  [INFO |10/31 00:58:47] Job 4
 *  [INFO |10/31 00:58:48] Stopping Job:                [Test_2] 1
 *  [INFO |10/31 00:58:48] Starting Job: [Test_2] 4
 *  [INFO |10/31 00:58:48] Job 5
 *  [INFO |10/31 00:58:49] Job 6
 *  [INFO |10/31 00:58:49] Stopping Job:                [Test_3] 2
 *  [INFO |10/31 00:58:49] Starting Job: [Test_3] 5
 *  [INFO |10/31 00:58:50] Job 7
 *  [INFO |10/31 00:58:50] Stopping Job:                [Test_1] 3
 *  [INFO |10/31 00:58:50] Starting Job: [Test_1] 6
 *  [INFO |10/31 00:58:51] Job 8
 *  [INFO |10/31 00:58:52] Job 9
 *  [INFO |10/31 00:58:52] Stopping Job:                [Test_2] 4
 *  [INFO |10/31 00:58:52] Starting Job: [Test_2] 7
 *  [INFO |10/31 00:58:53] Job 10
 *  [INFO |10/31 00:58:53] Stopping Job:                [Test_3] 5
 *  [INFO |10/31 00:58:53] Starting Job: [Test_3] 8
 *  [INFO |10/31 00:58:54] Job 11
 *  [INFO |10/31 00:58:55] Stop Threads
 *  [INFO |10/31 00:58:55] Stopping Job:                [Test_1] 6
 *  [INFO |10/31 00:58:55] Starting Job: [Test_1] 9
 *  [INFO |10/31 00:58:57] Stopping Job:                [Test_2] 7
 *  [INFO |10/31 00:58:57] Starting Job: [Test_2] 10
 *  [INFO |10/31 00:58:59] Stopping Job:                [Test_3] 8
 *  [INFO |10/31 00:58:59] Starting Job: [Test_3] 11
 *  [INFO |10/31 00:59:02] Stopping Job:                [Test_1] 9
 *  [INFO |10/31 00:59:04] Stopping Job:                [Test_2] 10
 *  [INFO |10/31 00:59:06] Stopping Job:                [Test_3] 11
 * </pre>
 * 
 * <p>
 * Note that this class is not referenced by any other class.
 * 
 * @author Martin D. Flynn
 * @author Guanghong Yang
 * 
 */
public class ThreadPool {

  /** The default size of the pool. */
  private static final int MAX_POOL_SIZE = 20;

  /**
   * Possible value of <tt>stopThreads</tt>. If a <tt>ThreadPool</tt>'s <tt>stopThreads</tt>'s
   * value is STOP_WAITING, then the <tt>ThreadJob</tt> will stop running after the all jobs in
   * the <tt>jobThreadPool</tt> are executed.
   */
  public static final int STOP_WAITING = -1;
  /**
   * Possible value of <tt>stopThreads</tt>. If a <tt>ThreadPool</tt>'s <tt>stopThreads</tt>'s
   * value is STOP_NEVER, then the <tt>ThreadJob</tt> will always keep on running.
   */
  public static final int STOP_NEVER = 0;
  /**
   * Possible value of <tt>stopThreads</tt>. If a <tt>ThreadPool</tt>'s <tt>stopThreads</tt>'s
   * value is STOP_NOW, then the <tt>ThreadJob</tt> will stop running immediately with all jobs
   * terminted.
   */
  public static final int STOP_NOW = 1;

  /**
   * Thread container. Note that no threads are put into this group, the only use of this element is
   * to hold the name of the ThreadPool and to initiate <tt>ThreadJob</tt> instances.
   */
  private ThreadGroup poolGroup = null;
  /**
   * Thread container. If a thread is added, the pool is not full (size does not exceed
   * <tt>maxPoolSize</tt>) and there is no waiting thread, then the thread is added into this
   * list.
   */
  private java.util.List jobThreadPool = null;
  /** Maximum size of <tt>jobThreadPool</tt>. */
  private int maxPoolSize = MAX_POOL_SIZE;
  /** Index associated with each thread in the queue. */
  private int threadId = 1;
  /** Job waiting queues. */
  private java.util.List jobQueue = null;
  /** Number of waiting jobs. */
  private int waitingCount = 0;
  /** Running state of <tt>JobThread</tt>. */
  private int stopThreads = STOP_NEVER;

  /**
   * Constructs a new ThreadPool instance and sets size as default.
   * 
   * @param name Name of the ThreadPool.
   */
  public ThreadPool(String name) {
    this(name, MAX_POOL_SIZE);
  }

  /**
   * Constructs a new ThreadPool instance, initiates jobThreadPool and jobQueue, and sets the name
   * and size.
   * 
   * @param name Name of the ThreadPool
   * @param maxPoolSize Size of the ThreadPool. If not positive, default value will be used.
   */
  public ThreadPool(String name, int maxPoolSize) {
    super();
    this.poolGroup = new ThreadGroup((name != null) ? name : "ThreadPool");
    this.maxPoolSize = (maxPoolSize > 0) ? maxPoolSize : MAX_POOL_SIZE;
    this.jobThreadPool = new Vector();
    this.jobQueue = new Vector();
  }

  /**
   * Returns the name of the ThreadPool.
   * 
   * @return Name of the ThreadPool.
   */
  public String getName() {
    return this.getThreadGroup().getName();
  }

  /**
   * Overides <tt>toString()</tt> method by returning the name.
   * 
   * @return Name of the ThreadPool.
   */
  public String toString() {
    return this.getName();
  }

  /**
   * Overides <tt>equals()</tt> method by checking if the two objects are the same one instead of
   * checking the content. return True if same object, or false otherwise.
   * 
   * @param other The object to be compared.
   * @return True if the same object, or false otherwise. 
   */
  public boolean equals(Object other) {
    return (this == other); // equals only if same object
  }

  /**
   * Returns the <tt>poolGroup</tt> member.
   * 
   * @return The <tt>pollGroup</tt> member.
   */
  public ThreadGroup getThreadGroup() {
    return this.poolGroup;
  }

  /**
   * Returns the size of the <tt>jobThreadPool</tt>. This is ensured as thread-safe.
   * 
   * @return The size of the <tt>jobThreadPool</tt>.
   */
  public int getSize() {
    int size = 0;
    synchronized (this.jobThreadPool) {
      size = this.jobThreadPool.size();
    }
    return size;
  }

  /**
   * Returns the maximum size of the <tt>jobThreadPool</tt>.
   * 
   * @return The maximum size of the <tt>jobThreadPool</tt>.
   */
  public int getMaxSize() {
    return this.maxPoolSize;
  }

  /**
   * Adds a new job. The job is firstly added to <tt>jobQueue</tt>. Then if
   * <tt>jobThreadPool</tt> is not full and there is no other waiting thread. Finally a waiting
   * thread will be called from <tt>jobQueue</tt>. This method is thread-safe with respect to
   * <tt>jobQueue</tt> and <tt>jobThreadPool</tt>.
   * 
   * @param job The thread to be added.
   */
  public void run(Runnable job) {
    synchronized (this.jobThreadPool) { // <-- modification of threadPool is likely
      synchronized (this.jobQueue) { // <-- modification of job queue mandatory
        // It's possible that we may end up adding more threads than we need if this
        // section executes multiple times before the newly added thread has a chance
        // to pull a job off the queue.
        this.jobQueue.add(job);
        if ((this.waitingCount == 0) && (this.jobThreadPool.size() < this.maxPoolSize)) {
          ThreadJob tj = new ThreadJob(this, (this.getName() + "_" + (this.threadId++)));
          this.jobThreadPool.add(tj);
          Print.logDebug("New Thread: " + tj.getName() + " [" + this.getMaxSize() + "]");
        }
        this.jobQueue.notify(); // notify a waiting thread
      }
    }
  }

  /**
   * Sets the state so that the threads will be stop running after current ones are executed.
   * 
   */
  public void stopThreads() {
    synchronized (this.jobQueue) {
      this.stopThreads = STOP_WAITING;
      this.jobQueue.notifyAll();
    }
  }

  /**
   * Removes a thread from <tt>jobThreadPool</tt>.
   * @param thread The thread to be removed.
   */
  protected void _removeThread(ThreadJob thread) {
    if (thread != null) {
      synchronized (this.jobThreadPool) {
        // Print.logDebug("Removing thread: " + thread.getName());
        this.jobThreadPool.remove(thread);
      }
    }
  }

  /**
   * Inner class definition for an extended thread which is associated with a <tt>ThreadPool</tt>.
   * 
   * @author Martin D. Flynn
   * @author Guanghong Yang
   */
  private static class ThreadJob extends Thread {
    private Runnable job = null;
    private ThreadPool threadPool = null;

    /**
     * Initiates the thread, sets the name and associated it with a <tt>ThreadPool</tt>.
     * 
     * @param pool The <tt>ThreadPool</tt> to be associated with.
     * @param name The name of the thread.
     */
    public ThreadJob(ThreadPool pool, String name) {
      super(pool.getThreadGroup(), name);
      this.threadPool = pool;
      this.start(); // auto start
    }

    /**
     * Keeps on executing thread in jobQueue until stopped by setting the state from outside codes.
     * 
     */
    public void run() {

      /* loop forever */
      while (true) {

        /* get next job */
        // 'this.job' is always null here
        boolean stop = false;
        synchronized (this.threadPool.jobQueue) {
          // Print.logDebug("Thread checking for jobs: " + this.getName());
          while (this.job == null) {
            if (this.threadPool.stopThreads == STOP_NOW) {
              stop = true;
              break;
            }
            else if (this.threadPool.jobQueue.size() > 0) {
              this.job = (Runnable) this.threadPool.jobQueue.remove(0);
            }
            else if (this.threadPool.stopThreads == STOP_WAITING) {
              stop = true;
              break;
            }
            else {
              this.threadPool.waitingCount++;
              try {
                this.threadPool.jobQueue.wait(20000);
              }
              catch (InterruptedException ie) {
              }
              this.threadPool.waitingCount--;
            }
          }
        }
        if (stop) {
          break;
        }

        /* run job */
        // Print.logDebug("Thread running: " + this.getName());
        this.job.run();
        this.job = null;

      }

      /* remove thread from pool */
      this.threadPool._removeThread(this);

    }

  }

  /**
   * Simulates execution of 12 threads with ThreadPool.
   * 
   * @param argv Command Line Argument to be set to <tt>RTConfig</tt>.
   */
  public static void main(String argv[]) {
    RTConfig.setCommandLineArgs(argv);
    ThreadPool pool = new ThreadPool("Test", 3);
    for (int i = 0; i < 12; i++) {
      final int n = i;
      Print.logInfo("Job " + i);
      Runnable job = new Runnable() {
        int num = n;

        public void run() {
          Print.logInfo("Starting Job: " + this.getName());
          try {
            Thread.sleep(2000 + (num * 479));
          }
          catch (Throwable t) {
          }
          Print.logInfo("Stopping Job:                " + this.getName());
        }

        public String getName() {
          return "[" + Thread.currentThread().getName() + "] " + num;
        }
      };
      pool.run(job);
      try {
        Thread.sleep(500 + (i * 58));
      }
      catch (Throwable t) {
      }
    }
    Print.logInfo("Stop Threads");
    pool.stopThreads();
  }

}