package com.cedarsoftware.ncube;

import com.cedarsoftware.ncube.proximity.LatLon;
import com.cedarsoftware.ncube.proximity.Point2D;
import com.cedarsoftware.ncube.proximity.Point3D;
import com.cedarsoftware.util.io.JsonObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by kpartlow on 9/23/2014.
 */
public class TestCellInfo
{

    @Test
    public void testFormatForEditing() {
        assertEquals("4.56", CellInfo.formatForEditing(4.56));
        assertEquals("0.0", CellInfo.formatForEditing(0.0));
        assertEquals("4.0", CellInfo.formatForEditing(new Float(4)));
        assertEquals("4.0", CellInfo.formatForEditing(new Double(4)));

        assertEquals("4.56", CellInfo.formatForEditing(new BigDecimal("4.56000")));
        assertEquals("4.56", CellInfo.formatForEditing(new BigDecimal("4.56")));

        Calendar c = Calendar.getInstance();
        c.set(2005, 10, 21, 12, 15, 19);
        assertEquals("\"2005-11-21 12:15:19\"", CellInfo.formatForEditing(c.getTime()));
    }

    @Test
    public void testCollapseToUISupportedTypes() {
        CellInfo info = new CellInfo(5);
        assertEquals(CellTypes.Integer.desc(), info.dataType);
        info.collapseToUiSupportedTypes();
        assertEquals(CellTypes.Long.desc(), info.dataType);

        info = new CellInfo(new Short((short)5));
        assertEquals(CellTypes.Short.desc(), info.dataType);
        info.collapseToUiSupportedTypes();
        assertEquals(CellTypes.Long.desc(), info.dataType);

        info = new CellInfo(new Byte((byte)5));
        assertEquals(CellTypes.Byte.desc(), info.dataType);
        info.collapseToUiSupportedTypes();
        assertEquals(CellTypes.Long.desc(), info.dataType);


        info = new CellInfo(new Float(5));
        assertEquals(CellTypes.Float.desc(), info.dataType);
        info.collapseToUiSupportedTypes();
        assertEquals(CellTypes.Double.desc(), info.dataType);

        info = new CellInfo(new BigInteger("100", 10));
        assertEquals(CellTypes.BigInteger.desc(), info.dataType);
        info.collapseToUiSupportedTypes();
        assertEquals(CellTypes.BigDecimal.desc(), info.dataType);

    }

    @Test
    public void testFormatForDisplay() {
        assertEquals("4.56", CellInfo.formatForEditing(4.560));
        assertEquals("4.5", CellInfo.formatForEditing(4.5));

        assertEquals("4.56", CellInfo.formatForEditing(new BigDecimal("4.5600")));
        assertEquals("4", CellInfo.formatForEditing(new BigDecimal("4.00")));
        assertEquals("4", CellInfo.formatForEditing(new BigDecimal("4")));

        assertEquals("4.56", CellInfo.formatForDisplay(new BigDecimal("4.5600")));
        assertEquals("4", CellInfo.formatForDisplay(new BigDecimal("4.00")));
        assertEquals("4", CellInfo.formatForDisplay(new BigDecimal("4")));
    }

    @Test
    public void testRecreate() {
        assertNull(new CellInfo(null).recreate());

        performRecreateAssertion(new StringUrlCmd("http://www.google.com", true));
        performRecreateAssertion(new Double(4.56));
        performRecreateAssertion(new Float(4.56));
        performRecreateAssertion(new Short((short)4));
        performRecreateAssertion(new Long(4));
        performRecreateAssertion(new Integer(4));
        performRecreateAssertion(new Byte((byte)4));
        performRecreateAssertion(new BigDecimal("4.56"));
        performRecreateAssertion(new BigInteger("900"));
        performRecreateAssertion(Boolean.TRUE);
        performRecreateAssertion(new GroovyExpression("0", null));
        performRecreateAssertion(new GroovyMethod("0", null));
        performRecreateAssertion(new GroovyTemplate(null, "http://www.google.com", false));
        performRecreateAssertion(new BinaryUrlCmd("http://www.google.com", false));
        performArrayRecreateAssertion(new byte[]{0, 4, 5, 6});
        performRecreateAssertion("foo");

        //  Have to special create this because milliseconds are not saved
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        performRecreateAssertion(c.getTime());
    }

    @Test
    public void testBooleanValue() {
        assertTrue(CellInfo.booleanValue("true"));
        assertFalse(CellInfo.booleanValue("false"));
    }

    @Test
    public void testConstructor() {
        CellInfo info = new CellInfo(new Point2D(5.0, 6.0));
        assertEquals(CellTypes.Point2D.desc(), info.dataType);

        info = new CellInfo(new Point3D(5.0, 6.0, 7.0));
        assertEquals(CellTypes.Point3D.desc(), info.dataType);

        info = new CellInfo(new LatLon(5.5, 5.9));
        assertEquals(CellTypes.LatLon.desc(), info.dataType);

        info = new CellInfo(new Range(5.5, 5.9));
        assertNull(info.dataType);
        assertFalse(info.isCached);
        assertFalse(info.isUrl);

        RangeSet set = new RangeSet();
        set.add(new Range(0, 5));
        set.add(new Range(10, 20));
        set.add(50);
        info = new CellInfo(set);
        assertNull(info.dataType);
        assertFalse(info.isUrl);
    }

    @Test
    public void testConstructorWithUnrecognizedType() {
        try {
            new CellInfo(new UnrecognizedConstructorObject());
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown cell value type"));
        }
    }

    @Test
    public void testParseJsonValue() {
        assertEquals(Boolean.TRUE, CellInfo.parseJsonValue("boolean", "true"));
        assertEquals(Boolean.FALSE, CellInfo.parseJsonValue("boolean", "false"));
        assertEquals((byte)2, CellInfo.parseJsonValue("byte", "2"));
        assertEquals((short)5, CellInfo.parseJsonValue("short", "5"));
        assertEquals(9L, CellInfo.parseJsonValue("long", "9"));
        assertEquals(9, CellInfo.parseJsonValue("int", "9"));
        assertEquals(9.87d, CellInfo.parseJsonValue("double", "9.87"));
        assertEquals(9.65f, CellInfo.parseJsonValue("float", "9.65"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseJsonValueBinaryWithOddNumberString() {
        CellInfo.parseJsonValue("binary", "0");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseJsonValueInvalidHexString() {
        CellInfo.parseJsonValue("binary", "GF");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseJsonValueWithInvalidBoolean() {
        CellInfo.parseJsonValue("boolean", "yes");

    }

    @Test
    public void testInvalidJsonObjectType() {
        try {
            JsonObject o = new JsonObject();
            Object[] items = new Object[1];
            items[0] = new CellInfo("string", null, false, false);
            o.put("@items", items);

            CellInfo.javaToGroovySource(o);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown Groovy Type"));
        }

    }

    @Test
    public void testNullItemOnFormatForDisplay() {
        assertEquals("Default", CellInfo.formatForDisplay(null));
    }

    public void performRecreateAssertion(Object o) {
        assertEquals(o, new CellInfo(o).recreate());
    }

    public void performArrayRecreateAssertion(byte[] o) {
        assertArrayEquals(o, (byte[])new CellInfo(o).recreate());
    }

    public static class UnrecognizedConstructorObject {
    }

}
