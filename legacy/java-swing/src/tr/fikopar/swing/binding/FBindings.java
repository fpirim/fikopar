package tr.fikopar.swing.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JComponent;

import tr.fikopar.swing.component.FCheckBox;
import tr.fikopar.swing.component.FComboBox;
import tr.fikopar.swing.component.FDateField;
import tr.fikopar.swing.component.FNumericField;
import tr.fikopar.swing.component.FTextArea;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.view.FViewModel;

public class FBindings {

	private static List<FBindingModel> bindingModels = new ArrayList<>();

	public static void add(JComponent component, FViewModel model, String propertyName) {
		add(component, model, propertyName, FBindingModel.BOTH);
	}

	public static void add(JComponent component, FViewModel model, String propertyName, int type) {
		FBindingModel bindingModel = null;
		if (component instanceof FNumericField)
			bindingModel = new FNumericFieldBindingModel((FNumericField) component, model, propertyName, type);
		else if (component instanceof FTextField)
			bindingModel = new FTextFieldBindingModel((FTextField) component, model, propertyName, type);
		else if (component instanceof FTextArea)
			bindingModel = new FTextAreaBindingModel((FTextArea) component, model, propertyName, type);
		else if (component instanceof FDateField)
			bindingModel = new FDateFieldBindingModel((FDateField) component, model, propertyName, type);
		else if (component instanceof FComboBox<?>)
			bindingModel = new FComboBoxBindingModel((FComboBox<?>) component, model, propertyName, type);
		else if (component instanceof FCheckBox)
			bindingModel = new FCheckBoxBindingModel((FCheckBox) component, model, propertyName, type);

		if (bindingModel == null)
			throw new RuntimeException("Binding is not supported for " + component.getClass().getName());
		bindingModels.add(bindingModel);

	}

	public static void remove(FViewModel model) {
		Stack<Integer> stack = new Stack<>();
		for (int i = 0; i < bindingModels.size(); i++) {
			FBindingModel bindingModel = bindingModels.get(i);
			if (bindingModel.getModel().equals(model)) {
				bindingModel.removeListeners();
				stack.push(i);
			}
		}
		while (!stack.isEmpty())
			bindingModels.remove(stack.pop());
	}

	public static void remove(JComponent component) {
		Stack<Integer> stack = new Stack<>();
		for (int i = 0; i < bindingModels.size(); i++) {
			FBindingModel bindingModel = bindingModels.get(i);
			if (bindingModel.getComponent().equals(component))
				stack.push(i);
		}
		while (!stack.isEmpty())
			bindingModels.remove(stack.pop());
	}

}
