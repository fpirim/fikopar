package tr.fikopar.swing.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import tr.fikopar.swing.view.FViewModel;

public abstract class FBindingModel implements PropertyChangeListener {
	public static final int BOTH = 0;
	public static final int MODEL_TO_VIEW = 1;
	public static final int VIEW_TO_MODEL = 2;
	protected int type;
	protected JComponent component;
	protected FViewModel model;
	protected String propertyName;

	public FBindingModel(JComponent component, FViewModel model, String propertyName, int type) {
		this.component = component;
		this.model = model;
		this.propertyName = propertyName;
		this.type = type;

		model.addPropertyChangeListener(this);
	}

	public void removeListeners() {
		model.removePropertyChangeListener(this);
		removeViewListeners();
	}

	public JComponent getComponent() {
		return component;
	}

	public FViewModel getModel() {
		return model;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public int getType() {
		return type;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(propertyName) && (type == BOTH || type == MODEL_TO_VIEW)
				&& !(evt.getOldValue() == null ? evt.getNewValue() == null : evt.getOldValue().equals(evt.getNewValue())))
			setViewValue(evt.getNewValue());
		

	}

	public abstract void setViewValue(Object value); // MODEL_TO_VIEW

	public abstract void removeViewListeners();

	protected Class<?> getFieldClass() {
		for (Field field : model.getClass().getDeclaredFields())
			if (field.getName().equals(propertyName))
				return field.getType();
		return null;
	}

	protected Object getModelValue() {
		Object rtnValue = null;
		try {
			Method method = model.getClass().getMethod(
					"get" + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1));
			rtnValue = method.invoke(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnValue;
	}

	protected void setModelValue(Object value) {
		try {
			if(type == MODEL_TO_VIEW)
				return;
			Object modelValue = getModelValue();
			if (modelValue == null ? value == null : modelValue.equals(value))
				return;
			Method method = model.getClass().getMethod(
					"set" + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1),
					getFieldClass());
			method.invoke(model, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}