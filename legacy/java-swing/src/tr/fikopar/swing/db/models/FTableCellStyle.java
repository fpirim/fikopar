package tr.fikopar.swing.db.models;

import java.awt.Color;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import tr.fikopar.swing.db.adapter.FTableCellAdapter;

@Entity
@Table(name="tablecellstyle")
public class FTableCellStyle implements Serializable{
	@Id
	@Column(name="id", columnDefinition="integer")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column(name="type", columnDefinition="integer")
	private Integer type = STRING;
	@Column(name="title", columnDefinition="string")
	private String title;
	@Column(name="fontStyle", columnDefinition="integer")
	private Integer fontStyle;
	@Column(name="width", columnDefinition="integer")
	private Integer width;
	@Column(name="format", columnDefinition="string")
	private String format;
	@Column(name="foregroundColor", columnDefinition="string")
	private String foregroundColor;
	@Column(name="backgroundColor", columnDefinition="string")
	private String backgroundColor;
	@ManyToOne(targetEntity=FTableStyle.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "classKey")
	private FTableStyle tableStyle;
	
	public FTableStyle getTableStyle() {
		return tableStyle;
	}
	public void setTableStyle(FTableStyle tableStyle) {
		this.tableStyle = tableStyle;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public FTableCellStyle() {
	}

	public FTableCellStyle(Integer type, Integer fontStyle) {
		this(type, fontStyle, "");
	}
	public FTableCellStyle(Integer type, Integer fontStyle, String format) {
		this(type, null, fontStyle, 0, format, null, null);
	}

	public FTableCellStyle(Integer type, Integer fontStyle, Integer width) {
		this(type, null, fontStyle, width);
	}

	public FTableCellStyle(Integer type, Integer fontStyle, Integer width, String format) {
		this(type, null, fontStyle, width, format);
	}

	public FTableCellStyle(Integer type, Integer fontStyle, Integer width, String format, Color foreGroundColor) {
		this(type, null, fontStyle, width, format, foreGroundColor);
	}

	public FTableCellStyle(Integer type, Integer fontStyle, String format, Color foregroundColor) {
		this(type, null, fontStyle, 0, format, foregroundColor, null);
	}

	public FTableCellStyle(Integer type, Integer fontStyle, String format, Color foregroundColor, Color backgroundColor) {
		this(type, null, fontStyle, 0, format, foregroundColor, backgroundColor);
	}

	
	public FTableCellStyle(Integer type, String title, Integer fontStyle, Integer width) {
		this(type, title, fontStyle, width, null, null, null);
	}

	public FTableCellStyle(Integer type, String title, Integer fontStyle, Integer width, String format) {
		this(type, title, fontStyle, width, format, null, null);
	}
	
	public FTableCellStyle(Integer type, String title, Integer fontStyle, Integer width, String format, Color foregroundColor) {
		this(type, title, fontStyle, width, format, foregroundColor, null);
	}

	public FTableCellStyle(Integer type, String title, Integer fontStyle, Integer width, String format, Color foregroundColor, Color backgroundColor) {
		this.title = title;
		this.fontStyle = fontStyle;
		this.width = width;
		this.format = format;
		this.foregroundColor = FTableCellAdapter.colorToString(foregroundColor);
		this.backgroundColor = FTableCellAdapter.colorToString(backgroundColor);
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(Integer fontStyle) {
		this.fontStyle = fontStyle;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	
	public static final int NUMERIC = 0;
	public static final int DATE = 1;
	public static final int STRING = 2;
	public static final int BOOLEAN = 3;


}
