
import java.lang.reflect.Method;

public class GenericThread implements Runnable {
	public boolean running;
	public Thread looper = new Thread(this);
	public int value;
	public Object sourceobj;
	public String runningmethod;
	public Method method;

	GenericThread(Object sourceobj, String runningmethod) {
		this.sourceobj = sourceobj;
		this.runningmethod = runningmethod;
		running = false;
	}

	public void start() {
		if (!running) {
			running = true;
			looper.start();
		}
	}

	public void run() {
		try {
			method = sourceobj.getClass().getDeclaredMethod(runningmethod,
					(Class[]) null);
			method.invoke(sourceobj, (Class[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}