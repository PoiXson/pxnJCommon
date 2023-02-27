package com.poixson.tools.dao;

import java.io.Serializable;


public class Fabc implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	public float a;
	public float b;
	public float c;



	public Fabc() {
		this.a = 0;
		this.b = 0;
		this.c = 0;
	}
	public Fabc(final float a, final float b, final float c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	public Fabc(final Fabc dao) {
		this.a = dao.a;
		this.b = dao.b;
		this.c = dao.c;
	}



	@Override
	public Object clone() {
		return new Fabc(this.a, this.b, this.c);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj instanceof Fabc) {
			final Fabc dao = (Fabc) obj;
			return (
				this.a == dao.a &&
				this.b == dao.b &&
				this.c == dao.c
			);
		}
		return false;
	}



	@Override
	public String toString() {
		return (new StringBuilder())
				.append('(') .append(this.a)
				.append(", ").append(this.b)
				.append(", ").append(this.c)
				.append(')')
				.toString();
	}
	@Override
	public int hashCode() {
		long bits =    31L  + Float.floatToIntBits(this.a);
		bits = (bits * 31L) + Float.floatToIntBits(this.b);
		bits = (bits * 31L) + Float.floatToIntBits(this.c);
		return (int) (bits ^ (bits >> 32L));
	}



}
