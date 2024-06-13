package app.lamport_mutex;

public class LamportClock {

	private int d;
	private int clock;
	
	public LamportClock(int d) {
		this.d = d;
		this.clock = 0;
	}
	
	public LamportClock() {
		this.d = 1;
		this.clock = 0;
	}
	
	public synchronized void localEvent() {
		clock += d;
	}
	
	public synchronized void messageEvent(int mesageClock) {
		this.localEvent();
		this.clock = Math.max(clock, mesageClock + d);
	}
	
	public int getClock() {
		return clock;
	}
	
}
