package tr.fikopar.swing.binding;

import java.math.BigDecimal;

import tr.fikopar.swing.component.FNumericField;
import tr.fikopar.swing.view.FViewModel;

public class FNumericFieldBindingModel extends FTextFieldBindingModel {

	public FNumericFieldBindingModel(FNumericField component, FViewModel model, String propertyName, int type) {
		super(component, model, propertyName, type);
	}

	@Override
	public void setViewValue(Object value) {

		FNumericField numericField = (FNumericField) component;
		if(value instanceof BigDecimal && numericField.getBigDecimal().compareTo((BigDecimal)value) != 0)
			numericField.setText((BigDecimal) value);
		if(value instanceof Integer && numericField.getInt() != (Integer) value)
			numericField.setText((Integer) value);
	}
	
	@Override
	public void textFieldChange() {

		FNumericField numericField = (FNumericField) component;
		if (getFieldClass().isInstance(BigDecimal.class))
			setModelValue(numericField.getBigDecimal());
		if (getFieldClass().isInstance(Integer.class))
			setModelValue(numericField.getInt());

	}

}
