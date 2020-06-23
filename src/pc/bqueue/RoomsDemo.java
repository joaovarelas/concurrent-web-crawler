package pc.bqueue;

import pc.util.UnexpectedException;

/**
 * Example program using the Rooms class.
 */
@SuppressWarnings("javadoc")
public class RoomsDemo {

	public static void main(String[] args) {
		new RoomsDemo().run();
	}

	Rooms rooms;

	RoomsDemo() {
		rooms = new Rooms(3, false);
	}

	void accessRoom(int r) {
		info("Waiting to enter room %d", r);
		rooms.enter(r);
		info("Entered room %d", r);
		delay();
		info("Leaving room %d", r);
		rooms.leave(r);
		info("Left room %d", r);
	}

	void run() {
		Thread t1 = new Thread(() -> {
			accessRoom(0);
			accessRoom(1);
			accessRoom(2);
		});
		Thread t2 = new Thread(() -> {
			accessRoom(0);
			accessRoom(2);
			accessRoom(0);
		});
		Thread t3 = new Thread(() -> {
			accessRoom(1);
			accessRoom(2);
			accessRoom(0);
		});
		Thread t4 = new Thread(() -> {
			accessRoom(2);
			accessRoom(1);
			accessRoom(0);
		});

		t1.start();
		t2.start();
		t3.start();
		t4.start();
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			throw new UnexpectedException(e);
		}
	}

	void delay() {
		info("pausing");
		try {
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			throw new UnexpectedException(e);
		}
	}

	synchronized void info(String format, Object... args) {
		System.out.printf("%s | ", Thread.currentThread().getName());
		System.out.printf(format, args);
		System.out.println();
	}
}
