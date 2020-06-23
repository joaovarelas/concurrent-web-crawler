package pc.bqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.cooperari.CSystem;
import org.cooperari.config.CMaxTrials;
import org.cooperari.config.CRaceDetection;
import org.cooperari.config.CScheduling;
import org.cooperari.core.scheduling.CProgramStateFactory;
import org.cooperari.core.scheduling.CSchedulerFactory;
import org.cooperari.junit.CJUnitRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@RunWith(CJUnitRunner.class)
@CMaxTrials(25)
@CRaceDetection(false)
@CScheduling(schedulerFactory = CSchedulerFactory.MEMINI, stateFactory = CProgramStateFactory.RAW)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BQueueTest {

	abstract <T> BQueue<T> createBQueue(int capacity);

	@Test
	public void test1() {
		BQueue<Integer> q = createBQueue(3);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		CSystem.forkAndJoin(() -> q.add(1), () -> q.add(10), () -> q.add(100));
		assertEquals(3, q.size());
		CSystem.forkAndJoin(() -> a.set(q.size()), () -> b.set(q.size()), () -> c.set(q.size()));
		assertEquals(3, a.get());
		assertEquals(3, b.get());
		assertEquals(3, c.get());
		CSystem.forkAndJoin(() -> a.set(q.remove()), () -> b.set(q.remove()), () -> c.set(q.remove()));
		assertEquals(0, q.size());
		assertEquals(111, a.get() + b.get() + c.get());
	}

	void test2(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger(), b = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(1);
		}, () -> {
			q.add(10);
		}, () -> {
			a.set(q.remove());
		}, () -> {
			b.set(q.remove());
		});
		assertEquals(0, q.size());
		assertEquals(11, a.get() + b.get());
	}

	@Test
	public void test2_1() {
		test2(1);
	}

	@Test
	public void test2_2() {
		test2(2);
	}

	void test3(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(1);
		}, () -> {
			q.add(10);
		}, () -> {
			q.add(100);
		}, () -> {
			a.set(q.remove());
		}, () -> {
			b.set(q.remove());
		}, () -> {
			c.set(q.remove());
		});
		assertEquals(0, q.size());
		assertEquals(111, a.get() + b.get() + c.get());
	}

	@Test
	public void test3_1() {
		test3(1);
	}

	@Test
	public void test3_2() {
		test3(2);
	}

	@Test
	public void test3_3() {
		test3(3);
	}

	void test4(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger(), b = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(1);
		}, () -> {
			q.add(10);
		}, () -> {
			int n;
			while ((n = q.size()) == 0) {
				// wait until at least one add()
				// operation completes
			}
			if (q.hasFixedCapacity()) {
				// If queue has fixed capacity of one, only one add()
				// may have succeeded at this point.
				assertEquals(1, n);
			}
			a.set(q.remove());
			b.set(q.remove());
		});
		assertEquals(0, q.size());
		assertEquals(11, a.get() + b.get());
	}

	@Test
	public void test4_1() {
		test4(1);
	}

	@Test
	public void test4_2() {
		test4(1);
	}

	void test5(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(1);
			q.add(10);
			q.add(100);
		}, () -> {
			a.set(q.remove());
		}, () -> {
			b.set(q.remove());
		}, () -> {
			c.set(q.remove());
		});
		assertEquals(0, q.size());
		assertEquals(111, a.get() + b.get() + c.get());
	}

	@Test
	public void test5_1() {
		test5(1);
	}

	@Test
	public void test5_2() {
		test5(2);
	}

	@Test
	public void test5_3() {
		test5(3);
	}

	void test6(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			a.set(q.remove());
			b.set(q.remove());
			c.set(q.remove());
		}, () -> {
			q.add(1);
		}, () -> {
			q.add(10);
		}, () -> {
			q.add(100);
		});
		assertEquals(0, q.size());
		assertEquals(111, a.get() + b.get() + c.get());
	}

	@Test
	public void test6_1() {
		test6(1);
	}

	@Test
	public void test6_2() {
		test6(2);
	}

	@Test
	public void test6_3() {
		test6(3);
	}

	void test7(int capacity) {
		BQueue<Integer> q = createBQueue(capacity);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		AtomicInteger d = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(1);
			q.add(2);
			q.add(3);
			q.add(4);
			a.set(q.size());
		}, () -> {
			b.set(q.remove());
			c.set(q.remove());
			d.set(q.size());
		});
		assertEquals(2, q.size());
		assertTrue(a.get() >= 2 && a.get() <= 4);
		assertEquals(1, b.get());
		assertEquals(2, c.get());
		assertTrue(d.get() >= 0 && d.get() <= 2);
		CSystem.forkAndJoin(() -> {
			q.add(5);
			a.set(q.size());
		}, () -> {
			b.set(q.remove());
			c.set(q.remove());
			d.set(q.size());
		});
		assertEquals(1, q.size());
		assertTrue(a.get() >= 1 && a.get() <= 3);
		assertEquals(3, b.get());
		assertEquals(4, c.get());
		assertTrue(d.get() >= 0 && d.get() <= 1);
		CSystem.forkAndJoin(() -> {
			a.set(q.size());
		}, () -> {
			b.set(q.remove());
			q.add(6);
			c.set(q.remove());
			d.set(q.size());
		});
		assertEquals(0, q.size());
		assertTrue(a.get() == 0 || a.get() == 1);
		assertEquals(5, b.get());
		assertEquals(6, c.get());
		assertEquals(0, d.get());
	}

	@Test
	public void test7_4() {
		test7(4);
	}

	@Test
	public void test7_8() {
		test7(8);
	}

	@Test
	public void test8() {
		BQueue<Integer> q = createBQueue(3);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		AtomicInteger d = new AtomicInteger();
		CSystem.forkAndJoin(() -> q.add(1), () -> q.add(10), () -> q.add(100), () -> q.add(1000),
				() -> a.set(q.remove()), () -> b.set(q.remove()), () -> c.set(q.remove()), () -> d.set(q.remove()));
		assertEquals(0, q.size());
		assertEquals(1111, a.get() + b.get() + c.get() + d.get());
	}

	@Test
	public void test9() {
		BQueue<Integer> q = createBQueue(4);
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		AtomicInteger c = new AtomicInteger();
		AtomicInteger d = new AtomicInteger();
		CSystem.forkAndJoin(() -> {
			q.add(3);
			a.set(q.remove());
		}, () -> {
			b.set(q.remove());
		}, () -> {
			int n = q.size();
			q.add(n);
			q.add(n + 1);
		});
		assertEquals(1, q.size());

		int[][] abPossibilities = { { 0, 1 }, { 1, 0 }, { 0, 3 }, { 3, 0 }, { 3, 1 } };
		boolean found = false;
		for (int[] p : abPossibilities) {
			if (a.get() == p[0] && b.get() == p[1]) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail("Did you consider a = " + a.get() + " and  b=" + b.get() + " ?");
		}

		CSystem.forkAndJoin(() -> {
			c.set(q.remove());
		}, () -> {
			q.add(a.get());
		}, () -> {
			int n = q.size();
			q.add(n);
			d.set(q.remove());
		});

		assertEquals(1, q.size());

		int[][] cdPossibilities = { { 0, 0 }, { 0, 1 }, { 0, 3 }, { 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 }, { 3, 0 },
				{ 3, 1 }, { 3, 2 } };
		found = false;
		for (int[] p : cdPossibilities) {
			if (c.get() == p[0] && d.get() == p[1]) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail("Did you consider c = " + c.get() + " and  d=" + d.get() + " ?");
		}

		assertEquals(1, q.size());

	}
}
