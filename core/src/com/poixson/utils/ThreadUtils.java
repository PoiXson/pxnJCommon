package com.poixson.utils;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import com.poixson.logger.xLog;
import com.poixson.tools.Keeper;
import com.poixson.tools.xTime;
import com.poixson.tools.abstractions.xCallable;


public final class ThreadUtils {
	private ThreadUtils() {}
	static { Keeper.add(new ThreadUtils()); }

	public static final String[] ignoreThreadNames = new String[] {
		"EndThread",
		"DestroyJavaVM"
//		"main-w1",
//		"Main-Server-Thread",
//		"Reference Handler",
//		"NonBlockingInputStreamThread",
//		"process reaper",
//		"Signal Dispatcher",
//		"Java2D Disposer",
//		"AWT-EventQueue-0",
//		"AWT-XAWT",
//		"AWT-Shutdown",
//		"Finalizer",
//		"Exit"
	};



	// list running thread names
	public static String[] GetThreadNames() {
		return GetThreadNames(true);
	}
	public static String[] GetThreadNames(final boolean includeDaemon) {
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		if (threadSet.isEmpty()) return null;
		final Set<String> list = new HashSet<String>();
		THREAD_LOOP:
		for (final Thread thread : threadSet) {
			if (!includeDaemon && thread.isDaemon())
				continue THREAD_LOOP;
			final String name = thread.getName();
			if (Utils.isEmpty(name))
				continue THREAD_LOOP;
			if (!includeDaemon && name.startsWith("main:"))
				continue THREAD_LOOP;
			// check ignore list
			//IGNORE_LOOP:
			for (final String str : ignoreThreadNames) {
				if (name.equals(str)) {
					continue THREAD_LOOP;
				}
			} // end IGNORE_LOOP
			// add to list
			list.add(thread.getName());
		} // end THREAD_LOOP
		if (list.isEmpty())
			return null;
		return list.toArray(new String[0]);
	}
	public static int CountStillRunning() {
		final String[] threadNames = GetThreadNames(false);
		if (threadNames == null)
			return -1;
		return threadNames.length;
	}
	// display threads still running
	public static void DisplayStillRunning(final xLog log) {
//TODO
//		if (xVars.notDebug()) return;
		final String[] threadNames = GetThreadNames(false);
		// no threads still running
		if (Utils.isEmpty(threadNames)) return;
		// build message
		final StringBuilder msg = new StringBuilder();
		msg.append("Threads still running: [")
			.append(threadNames.length)
			.append(']');
		boolean hasDestroyJavaVM = false;
		for (final String name : threadNames) {
			if ("DestroyJavaVM".equals(name))
				hasDestroyJavaVM = true;
			msg.append("\n  ").append(name);
		}
		if (hasDestroyJavaVM) {
			msg.append("\n\nShould use xApp.waitUntilClosed() when main() is finished.\n");
		}
		log.warning( msg.toString() );
	}



	public static Thread getDispatchThreadSafe() {
		try {
			return getDispatchThread();
		} catch (InvocationTargetException ignore) {
		} catch (InterruptedException ignore) {
		}
		return null;
	}
	public static Thread getDispatchThread()
			throws InvocationTargetException, InterruptedException {
		final xCallable<Thread> call = new xCallable<Thread>() {
			@Override
			public Thread call() {
				return Thread.currentThread();
			}
		};
		EventQueue.invokeAndWait(call);
		return call.getResult();
	}



	// sleep thread
	public static void Sleep(final long ms) {
		if (ms < 1) return;
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignore) {}
	}
	public static void Sleep(final String time) {
		Sleep( new xTime(time) );
	}
	public static void Sleep(final xTime time) {
		if (time == null) return;
		Sleep( time.ms() );
	}



	public static int getSystemCores() {
		return Runtime.getRuntime().availableProcessors();
	}
	public static int getSystemCoresPlus(final int add) {
		return getSystemCores() + add;
	}



}
