package com.poixson.exceptions;


public class MismatchedVersionException extends Exception {
	private static final long serialVersionUID = 1L;

	public final String expectedVersion;
	public final String libraryVersion;



	public MismatchedVersionException(
			final String expectedVersion, final String libraryVersion) {
		super(
			(new StringBuilder())
				.append("Expected version: ")
				.append(expectedVersion)
				.append(" found library version: ")
				.append(libraryVersion)
				.toString()
		);
		this.expectedVersion = expectedVersion;
		this.libraryVersion  = libraryVersion;
	}



}
