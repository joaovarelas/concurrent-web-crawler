package pc.bqueue;

import static org.junit.Assert.*;

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
@CScheduling(schedulerFactory = CSchedulerFactory.MEMINI, stateFactory = CProgramStateFactory.GROUP)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RoomsTest {

	static final boolean DEBUG = true;

	static void d(String format, Object... args) {
		if (DEBUG) {
			System.out.printf("%s | ", Thread.currentThread().getName());
			System.out.printf(format, args);
			System.out.println();
		}
	}

	void doTest(int numberOfRooms, int threadsPerRoom) {
		Rooms rooms = new Rooms(numberOfRooms, false);
		AtomicInteger[] counts = new AtomicInteger[numberOfRooms];
		boolean[] inRoom = new boolean[numberOfRooms];
		for (int i = 0; i < numberOfRooms; i++) {
			counts[i] = new AtomicInteger();
		}
		Runnable[] arr = new Runnable[numberOfRooms * threadsPerRoom];
		for (int i = 0; i < arr.length; i++) {
			int r = i % numberOfRooms;
			arr[i] = () -> {
				d("Entering %d", r);
				rooms.enter(r);
				try {
					d("Entered %d", r);
					counts[r].getAndIncrement();
					inRoom[r] = true;
					for (int j = 0; j < numberOfRooms; j++) {
						if (j != r) {
							assertFalse(inRoom[j]);
						}
					}
				} finally {
					d("Leaving %d", r);
					inRoom[r] = false;
					rooms.leave(r);
					d("Left %d", r);
				}
			};
		}

		d("== %d threads, %d rooms, %d threads/room", arr.length, numberOfRooms, threadsPerRoom);
		CSystem.forkAndJoin(arr);

		for (int i = 0; i < numberOfRooms; i++) {
			assertEquals(threadsPerRoom, counts[i].get());
		}
	}

	@Test
	public void test21() {
		doTest(2, 1);
	}

	@Test
	public void test22() {
		doTest(2, 2);
	}

	@Test
	public void test31() {
		doTest(3, 1);
	}

	@Test
	public void test32() {
		doTest(3, 2);
	}

	@Test
	public void test33() {
		doTest(3, 3);
	}

	@Test
	@CMaxTrials(5)
	public void test57() {
		doTest(5, 7);
	}

	@Test
	@CMaxTrials(5)
	public void test75() {
		doTest(7, 5);
	}

}
