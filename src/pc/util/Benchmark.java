package pc.util;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class for performing throughput benchmarks.
 */
public final class Benchmark {

	/**
	 * Operation interface.
	 */
	public interface Operation {
		/**
		 * Setup.
		 */
		default void setup() {
		}

		/**
		 * Tear-down.
		 */
		default void teardown() {
		}

		/**
		 * Step.
		 */
		void step();
	}

	/**
	 * Number of threads.
	 */
	private final int n_threads;

	/**
	 * Duration of benchmark in seconds.
	 */
	private final int duration;

	/**
	 * Execution step.
	 */
	private final Operation operation;

	/**
	 * Synchronization barrier.
	 */
	private final CyclicBarrier barrier;

	/**
	 * Termination flag.
	 */
	private final AtomicBoolean completionFlag;

	/**
	 * Adder.
	 */
	private final AtomicLong stepCounter;

	/**
	 * Constructor.
	 * 
	 * @param n_threads Number of threads.
	 * @param duration  Duration.
	 * @param op        Step to execute.
	 */
	public Benchmark(int n_threads, int duration, Operation op) {
		this.n_threads = n_threads;
		this.duration = duration;
		this.operation = op;
		this.barrier = new CyclicBarrier(n_threads + 1);
		this.completionFlag = new AtomicBoolean();
		this.stepCounter = new AtomicLong();
	}

	@SuppressWarnings("javadoc")
	public class BThread extends Thread {
		long numberOfSteps = 0;
		final int tid;

		public BThread(int tid) {
			this.tid = tid;
		}

		public int getTId() {
			return tid;
		}

		@Override
		public void run() {
			try {
				barrier.await();
				while (!completionFlag.get()) {
					operation.step();
					numberOfSteps++;
				}
				stepCounter.getAndAdd(numberOfSteps);
				barrier.await();
			} catch (BrokenBarrierException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				barrier.reset();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Run the benchmark.
	 * 
	 * @return Throughput of operations in the scale of million operations per
	 *         second.
	 */
	public final synchronized double run() {
		completionFlag.set(false);
		stepCounter.set(0L);
		operation.setup();
		for (int i = 0; i < n_threads; i++) {
			Thread t = new BThread(i);
			t.setDaemon(true);
			t.start();
		}
		long startTime = System.currentTimeMillis();
		try {
			barrier.await();
			Thread.sleep(duration * 1000);
			completionFlag.set(true);
			operation.teardown();
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		double totalTime = 1e-03 * (System.currentTimeMillis() - startTime);
		return 1e-03 * stepCounter.get() / (totalTime * n_threads);
	}

}
