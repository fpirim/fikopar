package tr.fikopar.swing.db.adapter;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import tr.fikopar.swing.db.models.FTableCellStyle;

public class FTableCellAdapter {
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat rtnFormat = new SimpleDateFormat("dd/MM/yyyy");

	public static Object applyFormat(Object value, FTableCellStyle tableCellStyle) {
		try {
			return new DecimalFormat(tableCellStyle.getFormat()).format(value);
		} catch (Exception e) {
			// e.prIntegerStackTrace();
		}
		return value;
	}

	public static BigDecimal applyParse(String value, FTableCellStyle tableCellStyle) {
		try {
			Number number = new DecimalFormat(tableCellStyle.getFormat()).parse(value);
			return new BigDecimal(number.doubleValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}

	public static boolean isValidDate(String val) {
		try {
			dateFormat.parse(val);
			return true;
		} catch (ParseException e) {
		}
		return false;
	}

	public static String maskDate(String val) {
		try {
			return rtnFormat.format(dateFormat.parse(val));
		} catch (Exception e) {
		}
		return val;
	}

	
	public static Color stringToColor(String v) {
		return v == null ? null : Color.decode(v);
	}

	public static String colorToString(Color v) {
		return v == null ? null : "#"+Integer.toHexString(v.getRGB()).substring(2).toUpperCase();
	}
}
