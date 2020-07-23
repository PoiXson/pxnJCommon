package com.poixson.logger;

import static com.poixson.logger.xLog.XLog;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.poixson.logger.records.xLogRecord_Msg;
import com.poixson.tools.Keeper;


public class LoggerToXLog extends Handler {

	protected static final AtomicReference<LoggerToXLog> instance = new AtomicReference<LoggerToXLog>(null);



	public static void init() {
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.ALL);
		final Handler[] handlers = logger.getHandlers();
		boolean found = false;
		for (final Handler handler : handlers) {
			if (handler instanceof LoggerToXLog) {
				found = true;
				continue;
			}
			logger.removeHandler(handler);
		}
		if (!found) {
			logger.addHandler( Get() );
		}
	}



	protected static LoggerToXLog Get() {
		if (instance.get() == null) {
			final LoggerToXLog handler = new LoggerToXLog();
			if (instance.compareAndSet(null, handler))
				return handler;
		}
		return instance.get();
	}
	protected LoggerToXLog() {
		Keeper.add(this);
	}



	@Override
	public void publish(final LogRecord record) {
		final xLog log = this.log(record);
		final String msg = record.getMessage();
		final Level  lvl = record.getLevel();
		final xLevel level = xLevel.FromJavaLevel(lvl);
		log.publish(
			new xLogRecord_Msg(log, level, msg, null)
		);
	}



	@Override
	public void flush() {
		final xLog log = this.log();
		log.flush();
	}



	@Override
	public void close() throws SecurityException {
	}



	public static String GetLoggerAlias(final String name) {
		if (name.equals("org.jline"))    return "jline";
		if (name.startsWith("io.netty")) return "netty";
		if (name.startsWith("org.xeustechnologies.jcl"))
			return "jcl";
		return name;
	}



	public xLog log(final LogRecord record) {
		final String name = GetLoggerAlias( record.getLoggerName() );
		return XLog(name);
	}
	public xLog log() {
		return XLog();
	}



}