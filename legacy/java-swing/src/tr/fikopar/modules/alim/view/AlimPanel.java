package tr.fikopar.modules.alim.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class AlimPanel extends JPanel {
	private JTextField idTextField;
	private JTextField hesapnoTextField;
	private JTextField hesapadiTextField;
	private JTextField textField;
	private JTextField textField_1;
	public AlimPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{98, 130, 222, 0, 45, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel idLabel = new JLabel("Al\u0131m Id :");
		GridBagConstraints gbc_idLabel = new GridBagConstraints();
		gbc_idLabel.anchor = GridBagConstraints.WEST;
		gbc_idLabel.insets = new Insets(5, 5, 5, 5);
		gbc_idLabel.gridx = 0;
		gbc_idLabel.gridy = 0;
		add(idLabel, gbc_idLabel);
		
		idTextField = new JTextField();
		GridBagConstraints gbc_idTextField = new GridBagConstraints();
		gbc_idTextField.insets = new Insets(5, 0, 5, 5);
		gbc_idTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_idTextField.gridx = 1;
		gbc_idTextField.gridy = 0;
		add(idTextField, gbc_idTextField);
		idTextField.setColumns(10);
		
		JLabel lblTarh = new JLabel("Tarh :");
		GridBagConstraints gbc_lblTarh = new GridBagConstraints();
		gbc_lblTarh.anchor = GridBagConstraints.WEST;
		gbc_lblTarh.insets = new Insets(0, 0, 5, 5);
		gbc_lblTarh.gridx = 4;
		gbc_lblTarh.gridy = 0;
		add(lblTarh, gbc_lblTarh);
		
		textField_1 = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.anchor = GridBagConstraints.WEST;
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.gridx = 5;
		gbc_textField_1.gridy = 0;
		add(textField_1, gbc_textField_1);
		textField_1.setColumns(10);
		
		JLabel hesapnoLabel = new JLabel("Hesap No :");
		GridBagConstraints gbc_hesapnoLabel = new GridBagConstraints();
		gbc_hesapnoLabel.anchor = GridBagConstraints.WEST;
		gbc_hesapnoLabel.insets = new Insets(0, 5, 5, 5);
		gbc_hesapnoLabel.gridx = 0;
		gbc_hesapnoLabel.gridy = 1;
		add(hesapnoLabel, gbc_hesapnoLabel);
		
		hesapnoTextField = new JTextField();
		GridBagConstraints gbc_hesapnoTextField = new GridBagConstraints();
		gbc_hesapnoTextField.insets = new Insets(0, 0, 5, 5);
		gbc_hesapnoTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hesapnoTextField.gridx = 1;
		gbc_hesapnoTextField.gridy = 1;
		add(hesapnoTextField, gbc_hesapnoTextField);
		hesapnoTextField.setColumns(10);
		
		JCheckBox chckbxListedenDkseAktar = new JCheckBox("Listeden d\u00FC\u015F\u00FCkse aktar");
		GridBagConstraints gbc_chckbxListedenDkseAktar = new GridBagConstraints();
		gbc_chckbxListedenDkseAktar.anchor = GridBagConstraints.WEST;
		gbc_chckbxListedenDkseAktar.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxListedenDkseAktar.gridx = 5;
		gbc_chckbxListedenDkseAktar.gridy = 1;
		add(chckbxListedenDkseAktar, gbc_chckbxListedenDkseAktar);
		
		JLabel hesapadiLabel = new JLabel("Hesap Ad\u0131 :");
		GridBagConstraints gbc_hesapadiLabel = new GridBagConstraints();
		gbc_hesapadiLabel.anchor = GridBagConstraints.WEST;
		gbc_hesapadiLabel.insets = new Insets(0, 5, 5, 5);
		gbc_hesapadiLabel.gridx = 0;
		gbc_hesapadiLabel.gridy = 2;
		add(hesapadiLabel, gbc_hesapadiLabel);
		
		hesapadiTextField = new JTextField();
		GridBagConstraints gbc_hesapadiTextField = new GridBagConstraints();
		gbc_hesapadiTextField.gridwidth = 2;
		gbc_hesapadiTextField.insets = new Insets(0, 0, 5, 5);
		gbc_hesapadiTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hesapadiTextField.gridx = 1;
		gbc_hesapadiTextField.gridy = 2;
		add(hesapadiTextField, gbc_hesapadiTextField);
		hesapadiTextField.setColumns(10);
		
		JCheckBox chckbxListedenYksekseAktar = new JCheckBox("Listeden Y\u00FCksekse aktar");
		GridBagConstraints gbc_chckbxListedenYksekseAktar = new GridBagConstraints();
		gbc_chckbxListedenYksekseAktar.anchor = GridBagConstraints.WEST;
		gbc_chckbxListedenYksekseAktar.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxListedenYksekseAktar.gridx = 5;
		gbc_chckbxListedenYksekseAktar.gridy = 2;
		add(chckbxListedenYksekseAktar, gbc_chckbxListedenYksekseAktar);
		
		JLabel label = new JLabel("Hesap Ad\u0131 :");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 3;
		add(label, gbc_label);
		
		textField = new JTextField();
		textField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 3;
		add(textField, gbc_textField);
		
		JLabel lblNot = new JLabel("Not :");
		GridBagConstraints gbc_lblNot = new GridBagConstraints();
		gbc_lblNot.insets = new Insets(0, 0, 5, 5);
		gbc_lblNot.gridx = 4;
		gbc_lblNot.gridy = 3;
		add(lblNot, gbc_lblNot);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 5;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);
		
		JLabel excelLabel = new JLabel("Excel Modeli :");
		GridBagConstraints gbc_excelLabel = new GridBagConstraints();
		gbc_excelLabel.insets = new Insets(0, 5, 5, 5);
		gbc_excelLabel.anchor = GridBagConstraints.WEST;
		gbc_excelLabel.gridx = 0;
		gbc_excelLabel.gridy = 4;
		add(excelLabel, gbc_excelLabel);
		
		JComboBox excelCombo = new JComboBox();
		GridBagConstraints gbc_excelCombo = new GridBagConstraints();
		gbc_excelCombo.gridwidth = 2;
		gbc_excelCombo.insets = new Insets(0, 0, 5, 5);
		gbc_excelCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_excelCombo.gridx = 1;
		gbc_excelCombo.gridy = 4;
		add(excelCombo, gbc_excelCombo);
		
		JPanel tablePanel = new JPanel();
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.insets = new Insets(0, 5, 0, 0);
		gbc_tablePanel.gridwidth = 6;
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 5;
		add(tablePanel, gbc_tablePanel);
	}

}
