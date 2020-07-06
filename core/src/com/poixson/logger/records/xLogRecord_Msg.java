package com.poixson.logger.records;

import com.poixson.logger.xLevel;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogRecord_Msg implements xLogRecord {

	public final xLevel   xlevel;
	public final long     timestamp;
	public final String[] lines;
	public final Object[] args;



	public xLogRecord_Msg(final xLevel xlevel,
			final String line, final Object[] args) {
		this(
			xlevel,
			new String[] { line },
			args
		);
	}
	public xLogRecord_Msg(final xLevel xlevel,
			final String[] lines, final Object[] args) {
		this.xlevel = xlevel;
		this.timestamp = Utils.getSystemMillis();
		this.lines = lines;
		this.args  = args;
	}



//TODO: remove this
//	protected static String[] PrepareLines(final String[] lines, final Object[] args) {
//		final String[] linesSplit = StringUtils.SplitLines(lines);
//		if (linesSplit == null) {
//			// empty message
//			if (Utils.isEmpty(args))
//				return null;
//			// args only message
//			return
//				new String[] {
//					StringUtils.MergeObjects(", ", args)
//				};
//		}
//		// lines only message
//		if (Utils.isEmpty(args)) {
//			return linesSplit;
//		}
//		// insert args into lines
//		return
//			StringUtils.ReplaceTags(
//				linesSplit,
//				args
//			);
//	}



	@Override
	public String[] getLines() {
		return StringUtils.ReplaceTags(this.lines, this.args);
	}
	@Override
	public String toString() {
		final String[] lines = this.getLines();
		return StringUtils.MergeStrings('\n', lines);
	}



	@Override
	public xLevel getLevel() {
		return this.xlevel;
	}
	public String getLevelName() {
		return (
			this.xlevel == null
			? "<null>"
			: this.xlevel.toString()
		);
	}



	@Override
	public long getTimestamp() {
		return this.timestamp;
	}



	@Override
	public boolean isEmpty() {
		return Utils.isEmpty(this.lines);
	}
	@Override
	public boolean notEmpty() {
		return Utils.notEmpty(this.lines);
	}



//TODO: remove this
//	// [logger] [crumbs]
//	public String[] getNameTree() {
//		if (this.log == null)
//			return null;
//		return this.log.getNameTree();
//	}



//TODO: is this needed?
//	// java util level type
//	public java.util.logging.Level getJavaLevel() {
//		if (this.level == null)
//			return null;
//		return this.level.getJavaLevel();
//	}



}