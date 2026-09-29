package tr.fikopar.swing.view;

import java.util.HashMap;
import java.util.Map;

import tr.fikopar.swing.db.models.FTableStyle;
import tr.fikopar.swing.db.models.FUserPropModel;


public class FProperties {
	
	public final static String APPLICATION_X = "APPLICATION_X";
	public final static String APPLICATION_Y = "APPLICATION_Y";
	public final static String APPLICATION_WIDTH = "APPLICATION_WIDTH";
	public final static String APPLICATION_HEIGHT = "APPLICATION_HEIGHT";
	
	private Map<String, FUserPropModel> userPropertiesMap = new HashMap<>();
	private Map<String, FTableStyle> tableStyleMap = new HashMap<>();
	
	public Map<String, FTableStyle> getTableStyleMap() {
		return tableStyleMap;
	}

	public void setTableStyleMap(Map<String, FTableStyle> tableStyleMap) {
		this.tableStyleMap = tableStyleMap;
	}
	public Map<String, FUserPropModel> getUserPropertiesMap() {
		return userPropertiesMap;
	}
	public void putTableStyle(FTableStyle tableStyle) {
		tableStyleMap.put(tableStyle.getClassKey(), tableStyle);
	} 
	public void putUserProperties(FUserPropModel model) {
		userPropertiesMap.put(model.getPropKey(), model);
	}
	public void putUserProperties(String key, String value) {
		FUserPropModel userPropModel = null;
		if(userPropertiesMap.containsKey(key))
			userPropModel = userPropertiesMap.get(key);
		else
			userPropModel = new FUserPropModel();
		
		userPropModel.setPropKey(key);
		userPropModel.setPropValue(value);
		
		putUserProperties(userPropModel);
	}
	
	public String getUserProperties(String key) {
		if(userPropertiesMap.containsKey(key))
			return userPropertiesMap.get(key).getPropValue(); 
		return null;
	}
		
}
