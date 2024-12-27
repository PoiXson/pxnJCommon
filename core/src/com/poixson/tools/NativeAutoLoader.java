/*
package com.poixson.tools;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.exceptions.IORuntimeException;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.ErrorMode;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;


public class NativeAutoLoader {

	public static final ErrorMode DEFAULT_ERROR_MODE = ErrorMode.LOG;
	private ErrorMode errorMode = null;

	public static final boolean DEFAULT_ENABLE_EXTRACT = false;
	public static final boolean DEFAULT_ENABLE_REPLACE = false;
	public static final AutoMode DEFAULT_AUTO_MODE = AutoMode.AUTO_MODE_SYSTEM_OR_LOCAL;

	private final AtomicBoolean hasLoaded = new AtomicBoolean(false);
	private final Object loadLock = new Object();

	public enum AutoMode {
		AUTO_MODE_SELF_CONTAINED,
		AUTO_MODE_SYSTEM_OR_LOCAL
	};
	private AutoMode autoMode = null;

	private Class<?> classRef   = null;

	private String fileName  = null;
	private String localPath = null;
	private String resPath   = null;
	private final List<String> searchPaths = new ArrayList<String>();

	private Boolean enableExtract = null;
	private Boolean enableReplace = null;



	public static NativeAutoLoader getNew(final AutoMode mode) {
		final NativeAutoLoader loader = getNew();
		return loader.setAutoMode(mode);
	}
	public static NativeAutoLoader getNew() {
		return new NativeAutoLoader();
	}
	public NativeAutoLoader() {
	}
	@Override
	public NativeAutoLoader clone() {
		final NativeAutoLoader loader =
			getNew()
				.setErrorMode(this.errorMode)
				.setClassRef(this.classRef)
				.setFileName(this.fileName)
				.setLocalLibPath(this.localPath)
				.setResourcesPath(this.resPath);
		{
			final String[] searchPaths =
				this.searchPaths.toArray(new String[0]);
			if (!IsEmpty(searchPaths)) {
				for (final String path : searchPaths)
					loader.addSearchPath(path);
			}
		}
		if (this.enableExtract != null)
			loader.enableExtract(this.enableExtract.booleanValue());
		if (this.enableReplace != null)
			loader.enableReplace(this.enableReplace.booleanValue());
		return loader;
	}



	// extract/load library
	public boolean load(final String fileName) {
		if (!IsEmpty(fileName))
			this.setFileName(fileName);
		return this.load();
	}
	public boolean load() {
		if (this.hasLoaded.get())
			return true;
		final boolean result;
		synchronized (this.loadLock) {
			this.hasLoaded.set(true);
			result = this.doLoad();
		}
		return result;
	}
	private boolean doLoad() {
		final ErrorMode errorMode = this.getErrorMode();
		final String fileName = this.getFileName();
		if (IsEmpty(fileName)) {
			if (ErrorMode.EXCEPTION.equals(errorMode))
				throw new RequiredArgumentException("fileName");
			if (ErrorMode.LOG.equals(errorMode)) {
				this.log().severe("Library fileName is missing!");
			}
			return false;
		}
		final Class<?> classRef     = this.getClassRef();
		final String  localPath     = this.getLocalLibPath();
		final String  resPath       = this.getResourcesPath();
		final String[] searchPaths  = this.getSearchPaths();
		final boolean enableExtract = this.getEnableExtract();
		final boolean enableReplace = this.getEnableReplace();
		final String localFilePath =
			FileUtils.MergePaths(
				".",
				localPath,
				fileName
			);
		final File localFile = new File(localFilePath);

		// load existing local
		if (localFile.exists()) {
			this.log()
				.fine(
					"Found library %s at local path %s",
					fileName,
					localPath
				);
			return NativeUtils.SafeLoad(localFilePath, errorMode);
		}

		// load from search paths
		if (!IsEmpty(searchPaths)) {
			for (final String path : searchPaths) {
				if (IsEmpty(path)) continue;
				final String searchPath =
					FileUtils.MergePaths(
						path,
						fileName
					);
				final File searchFile = new File(searchPath);
System.out.println("SEARCH PATH: "+FileUtils.MergePaths(path, fileName));
				if (searchFile.exists()) {
					this.log()
						.fine(
							"Found library %s at search path %s",
							fileName,
							path
						);
					return NativeUtils.SafeLoad(searchPath, errorMode);
				}
			}
		}

		// create library directory
		{
			final File dir = new File(localPath);
			if (!dir.isDirectory()) {
				if (dir.mkdirs()) {
					this.log().info("Created libraries dir:", localPath);
				} else {
					if (ErrorMode.EXCEPTION.equals(errorMode)) {
						throw new IORuntimeException("Failed to create directory: "+localPath);
					} else
					if (ErrorMode.LOG.equals(errorMode)) {
						this.log().severe("Failed to create directory:", localPath);
					}
					return false;
				}
			}
		}

		// remove existing file
		if (enableExtract && enableReplace) {
			final boolean exists = localFile.isFile();
			if (exists) {
				if (enableReplace && enableExtract) {
					this.log().fine("Replacing existing library file:", fileName);
					localFile.delete();
				}
			}
		}

		// extract file
		if (enableExtract) {
			final boolean exists = localFile.isFile();
			if (!exists) {
				try {
					NativeUtils.ExtractLibrary(
						localPath,
						resPath,
						fileName,
						classRef
					);
				} catch (IOException e) {
					this.log()
						.severe( e.getMessage() );
					if (ErrorMode.EXCEPTION.equals(errorMode)) {
						throw new IORuntimeException("Failed to extract library file: "+localFilePath, e);
					} else
					if (ErrorMode.LOG.equals(errorMode)) {
						this.log().severe("Failed to extract library file:", localFilePath);
					}
					return false;
				}
			}
		}

		// load extracted library
		{
			final boolean exists = localFile.isFile();
			if (!exists) {
				if (ErrorMode.EXCEPTION.equals(errorMode))
					throw new IORuntimeException("Library file not found: "+localFilePath);
				if (ErrorMode.LOG.equals(errorMode)) {
					this.log().severe("Library file not found:", localFilePath);
				}
				return false;
			}
		}
		return NativeUtils.SafeLoad(localFilePath, errorMode);
	}



	public NativeAutoLoader setDefaults(final Class<?> clss) {
		this.addDefaultSearchPaths()
			.setErrorMode(ErrorMode.EXCEPTION)
			.setAutoMode(AutoMode.AUTO_MODE_SYSTEM_OR_LOCAL)
			.enableExtract()
			.enableReplace()
			.setClassRef(clss)
			.setResourcesPath("/")
//TODO: include os and arch
			.setLocalLibPath("libs/");
		return this;
	}



	// error mode
	public ErrorMode getErrorMode() {
		final ErrorMode mode = this.errorMode;
		return (
			mode == null
			? DEFAULT_ERROR_MODE
			: mode
		);
	}
	public NativeAutoLoader setErrorMode(final ErrorMode mode) {
		this.errorMode = mode;
		return this;
	}



	// auto mode
	public AutoMode getAutoMode() {
		return this.autoMode;
	}
	public NativeAutoLoader setAutoMode(final AutoMode mode) {
		this.autoMode = mode;
		return this;
	}



	// library file name
	public String getFileName() {
		String fileName = this.fileName;
		if (IsEmpty(fileName))
			return null;
//TODO: append file extension per os
//		switch (os) {
//		case LINUX:
//			fileName = StringUtils.ForceEnds(".so", fileName);
//			break;
//		}
		return fileName;
	}
	public NativeAutoLoader setFileName(final String fileName) {
		if (!StringUtils.StrEqualsExact(this.fileName, fileName)) {
			this.hasLoaded.set(false);
			this.fileName = fileName;
		}
		return this;
	}
	public NativeAutoLoader setFileNameIfNull(final String fileName) {
		if (IsEmpty(this.fileName)) {
			return this.setFileName(fileName);
		}
		return this;
	}



	// class reference
	public Class<?> getClassRef() {
		final Class<?> clss = this.classRef;
		return (
			clss == null
			? NativeAutoLoader.class
			: clss
		);
	}
	public NativeAutoLoader setClassRef(final Class<?> clss) {
		if (this.classRef != clss) {
			this.hasLoaded.set(false);
			this.classRef = clss;
		}
		return this;
	}



	// resource library path
	public String getResourcesPath() {
		final String path = this.resPath;
		return (
			IsEmpty(path)
			? null
			: StringUtils.ForceStarts("/", path)
		);
	}
	public NativeAutoLoader setResourcesPath(final String path) {
		if (!StringUtils.StrEqualsExact(this.resPath, path)) {
			this.hasLoaded.set(false);
			this.resPath = path;
		}
		return this;
	}



	// extracted library path
	public String getLocalLibPath() {
		final String path = this.localPath;
		return Utils.ifEmpty(path, ".");
	}
	public NativeAutoLoader setLocalLibPath(final String path) {
		if (!StringUtils.StrEqualsExact(this.localPath, path)) {
			this.hasLoaded.set(false);
			this.localPath = path;
		}
		return this;
	}



	// library search paths
	public String[] getSearchPaths() {
		return this.searchPaths.toArray(new String[0]);
	}
	public NativeAutoLoader addSearchPath(final String path) {
		if (!IsEmpty(path)) {
			if (!this.searchPaths.contains(path)) {
				this.hasLoaded.set(false);
				this.searchPaths.add(path);
			}
		}
		return this;
	}
	public NativeAutoLoader addDefaultSearchPaths() {
		final AutoMode mode = this.getAutoMode();
		switch (mode) {
		case AUTO_MODE_SELF_CONTAINED:
			break;
		case AUTO_MODE_SYSTEM_OR_LOCAL:
			this.addSearchPath("/usr/local/bin");
			this.addSearchPath("/usr/bin");
			break;
		default:
		}
		return this;
	}



	// enable extract file
	public boolean getEnableExtract() {
		final Boolean enabled = this.enableExtract;
		return (
			enabled == null
			? DEFAULT_ENABLE_EXTRACT
			: enabled.booleanValue()
		);
	}
	public NativeAutoLoader enableExtract() {
		return this.enableExtract(true);
	}
	public NativeAutoLoader disableExtract() {
		return this.enableExtract(false);
	}
	public NativeAutoLoader enableExtract(final boolean enable) {
		if (this.enableExtract == null
		|| this.enableExtract.booleanValue() != enable) {
			this.hasLoaded.set(false);
			this.enableExtract = Boolean.valueOf(enable);
		}
		return this;
	}



	// enable replace file
	public boolean getEnableReplace() {
		final Boolean enabled = this.enableReplace;
		return (
			enabled == null
			? DEFAULT_ENABLE_REPLACE
			: enabled.booleanValue()
		);
	}
	public NativeAutoLoader enableReplace() {
		return this.enableReplace(true);
	}
	public NativeAutoLoader disableReplace() {
		return this.enableReplace(false);
	}
	public NativeAutoLoader enableReplace(final boolean enable) {
		if (this.enableReplace == null
		|| this.enableReplace.booleanValue() != enable) {
			this.hasLoaded.set(false);
			this.enableReplace = Boolean.valueOf(enable);
		}
		return this;
	}



	// -------------------------------------------------------------------------------
	// logger



	private final AtomicReference<SoftReference<xLog>> _log =
			new AtomicReference<SoftReference<xLog>>(null);

	public xLog log() {
		// cached logger
		final SoftReference<xLog> ref = this._log.get();
		if (ref != null) {
			final xLog log = ref.get();
			if (log == null) this._log.set(null);
			else             return log;
		}
		// get logger
		{
			final xLog log = XLog("LibLoader");
			this._log.set( new SoftReference<xLog>(log) );
			return log;
		}
	}



}
*/
