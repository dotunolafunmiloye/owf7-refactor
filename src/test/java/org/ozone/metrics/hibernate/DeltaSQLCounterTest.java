package org.ozone.metrics.hibernate;

import org.junit.Test;
import static org.junit.Assert.*;

public class DeltaSQLCounterTest {
	
	private static final String SELECT = "SELECT";
	private static final String INSERT = "INSERT";

	@Test
	public void countDeltaFromZero() throws Exception {
		SQLCounter c= new SQLCounter();
		DeltaSQLCounter d=new DeltaSQLCounter(c);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);

		assertEquals(3, d.getCount());
		assertEquals(3,d.getVerbCount(SELECT));
	}
	
	@Test
	public void countDeltaFromFive() throws Exception {
		SQLCounter c= new SQLCounter();
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		
		DeltaSQLCounter d=new DeltaSQLCounter(c);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);

		assertEquals(3, d.getCount());
		assertEquals(3,d.getVerbCount(SELECT));
	}

	@Test
	public void countZeroVerbs() throws Exception {
		SQLCounter c= new SQLCounter();
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		c.inc(SELECT);
		
		DeltaSQLCounter d=new DeltaSQLCounter(c);
		c.inc(INSERT);
		c.inc(INSERT);
		c.inc(INSERT);

		assertEquals(3, d.getCount());
		assertEquals(3,d.getVerbCount(INSERT));
		assertEquals(0,d.getVerbCount(SELECT));
	}
	
}
