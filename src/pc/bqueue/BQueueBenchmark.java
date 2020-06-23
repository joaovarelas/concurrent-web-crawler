package pc.bqueue;

import java.util.concurrent.ThreadLocalRandom;

import pc.util.Benchmark;
import pc.util.Benchmark.BThread;

/**
 * Benchmark program for stack implementations.
 */
public class BQueueBenchmark {

	private static final int DURATION = 5;
	private static final int MAX_THREADS = 32;

	/**
	 * Program to run a benchmark over queue implementations.
	 * 
	 * @param args Arguments are ignored.
	 */
	public static void main(String[] args) {
		// double serial = runBenchmark(1, new UStack<Integer>());

		for (int t = 2; t <= MAX_THREADS; t = t * 2) {
			runBenchmark("Monitor-based", t, new MBQueueU<Integer>(MAX_THREADS));
			runBenchmark("Lock-free backoff=y", t, new LFBQueueU<Integer>(MAX_THREADS, true));
			runBenchmark("Lock-free backoff=n", t, new LFBQueueU<Integer>(MAX_THREADS, false));
			runBenchmark("STM", t, new STMBQueueU<Integer>(MAX_THREADS));
		}
	}

	private static void runBenchmark(String desc, int threads, BQueue<Integer> q) {
		Benchmark b = new Benchmark(threads, DURATION, new BQueueOperation(q));
		System.out.printf("%2d,%20s,%11s -> ", threads, desc, q.getClass().getSimpleName());
		System.out.printf("%10.2f thousand ops/s per thread%n", b.run());
	}

	private static class BQueueOperation implements Benchmark.Operation {
		private final BQueue<Integer> queue;

		BQueueOperation(BQueue<Integer> q) {
			this.queue = q;
		}

		public void teardown() {
			for (int i = 0; i <= MAX_THREADS; i++) {
				queue.add(i);
			}
			while (queue.size() > 0) {
				queue.remove();
			}
		}

		@Override
		public void step() {
			BThread t = (Benchmark.BThread) Thread.currentThread();
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			int role = t.getTId() % 2;
			if (role == 0) {
				int v = rng.nextInt(100);
				queue.add(v);
			} else if (queue.size() > MAX_THREADS) {
				queue.remove();
			}
			// Thread.yield();

		}
	}
}
