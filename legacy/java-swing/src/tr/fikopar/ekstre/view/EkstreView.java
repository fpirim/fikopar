package tr.fikopar.ekstre.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import net.sf.jasperreports.engine.JasperPrint;
import tr.fikopar.app.MyApp;
import tr.fikopar.common.controller.AbstractController;
import tr.fikopar.common.view.AbstractView;
import tr.fikopar.common.view.PrintPreview;
import tr.fikopar.db.models.sicil.SicilModel;
import tr.fikopar.ekstre.controller.EkstreController;
import tr.fikopar.ekstre.model.EkstreViewModel;
import tr.fikopar.modules.contentasist.sicilmodel.SicilModelContentAsist;
import tr.fikopar.swing.component.FDatefield;
import tr.fikopar.swing.component.FTextField;
import tr.fikopar.swing.table.FTable;

public class EkstreView extends AbstractView implements FocusListener, ActionListener {

	private EkstreTableModel ekstreTableModel;
	private EkstreToplamTableModel ekstreToplamTableModel;
	private FTextField hesapno, hesapadi, yetkili, mahalle, ilce, sehir, ulke, telefon;
	private FDatefield baslama, bitis;
	private JComboBox<String> comboDepo;
	private SicilModel sicilModel;

	public EkstreView(AbstractController controller) {
		super(controller);

		ekstreTableModel = new EkstreTableModel();
		ekstreToplamTableModel = new EkstreToplamTableModel();

		FTable table = new FTable(ekstreTableModel);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel tablePanel = table.createPanelForFTable(true, ekstreToplamTableModel);

		JLabel lblBitis = new JLabel("Bitiþ :");
		JLabel lblBaslama = new JLabel("Baþlama :");
		JLabel lblHesapNo = new JLabel("Hesap No :");
		JLabel lblHesapAdi = new JLabel("Hesap Adý :");
		JLabel lblYetkili = new JLabel("Yetkili :");
		JLabel lblMahalle = new JLabel("Mahalle :");
		JLabel lblIlce = new JLabel("Ýlçe :");
		JLabel lblSehir = new JLabel("Þehir :");
		JLabel lblUlke = new JLabel("Ülke :");
		JLabel lblTelefon = new JLabel("Telefon :");
		JLabel lblDepoNo = new JLabel("Depo :");
		hesapno = new FTextField();

		hesapno.addFocusListener(this);

		hesapadi = new FTextField();
		yetkili = new FTextField();
		
		PropertyChangeListener listener = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if("contentSelected".equals(evt.getPropertyName())) {
					Object [] obj = (Object []) evt.getNewValue();
					hesapno.setText((String) obj[0]);
					sicilModelYukle();
				}
			}
		};
		hesapadi.addPropertyChangeListener(listener);
		yetkili.addPropertyChangeListener(listener);
		
		SicilModelContentAsist.addFTextFieldContentListener(hesapadi);
		SicilModelContentAsist.addFTextFieldContentListener(yetkili);
		
		mahalle = new FTextField();
		mahalle.setEditable(false);
		ilce = new FTextField();
		ilce.setEditable(false);
		sehir = new FTextField();
		sehir.setEditable(false);
		ulke = new FTextField();
		ulke.setEditable(false);
		telefon = new FTextField();
		telefon.setEditable(false);
		baslama = new FDatefield();
		bitis = new FDatefield();
		comboDepo = new JComboBox<String>(new String[] { "Hepsi", "1.Depo", "2.Depo" });

		baslama.setDay(1);
		baslama.setMonth(1);

		baslama.addActionListener(this);
		bitis.addActionListener(this);
		comboDepo.addActionListener(this);

		registerComponent(new JButton(baskiOnizlemeAction));

		GroupLayout gl_panel = new GroupLayout(this);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup().addContainerGap().addGroup(gl_panel.createParallelGroup(
						Alignment.LEADING)
				.addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
				.addGroup(gl_panel.createSequentialGroup().addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false).addComponent(lblMahalle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblHesapAdi, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lblHesapNo, GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
								.addComponent(lblYetkili, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(lblSehir, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE).addComponent(lblTelefon, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false).addComponent(yetkili, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(hesapno, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel.createSequentialGroup()
										.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_panel.createSequentialGroup().addComponent(mahalle, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblIlce, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
												.addGroup(gl_panel.createSequentialGroup().addComponent(sehir, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblUlke, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addComponent(ulke, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE).addComponent(ilce,
												GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)))
								.addComponent(hesapadi, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE).addComponent(telefon, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
								.addGroup(gl_panel.createSequentialGroup()
										.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false).addComponent(lblBaslama, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblBitis, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING).addComponent(baslama, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(bitis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
								.addGroup(
										gl_panel.createSequentialGroup().addPreferredGap(ComponentPlacement.RELATED).addComponent(lblDepoNo, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(comboDepo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
						.addContainerGap()));
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup().addContainerGap()
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblHesapNo)
												.addComponent(hesapno, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblBaslama))
										.addComponent(baslama, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createSequentialGroup()
												.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblHesapAdi).addComponent(hesapadi, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(6)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblYetkili).addComponent(yetkili, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblMahalle, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
												.addComponent(mahalle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblIlce, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
												.addComponent(ilce, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblSehir, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
												.addComponent(sehir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblUlke, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
												.addComponent(ulke, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(7)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblTelefon, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
												.addComponent(telefon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
										.addGroup(gl_panel.createSequentialGroup().addComponent(bitis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
														.addComponent(comboDepo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblDepoNo)))
										.addComponent(lblBitis))
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE).addContainerGap()));
		setLayout(gl_panel);

	}

	private AbstractAction baskiOnizlemeAction = new AbstractAction("Baský Önizleme") {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JasperPrint jasperPrint = ((EkstreController) controller).print(sicilModel, baslama.getText(), bitis.getText(), (String) comboDepo.getSelectedItem(),
					ekstreTableModel.getList());
			if (jasperPrint == null)
				return;
			MyApp.addView(new PrintPreview(jasperPrint));
		}
	};

	private void sicilModelYukle() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doProcess();
				try {
					SicilModel sicilModel = ((EkstreController) controller).loadSicilModel(hesapno.getText());

					EkstreView.this.sicilModel = sicilModel;

					ekstreTableModel.getList().clear();
					ekstreToplamTableModel.setAlacakToplam(BigDecimal.ZERO);
					ekstreToplamTableModel.setBorcToplam(BigDecimal.ZERO);

					if (EkstreView.this.sicilModel == null) {
						hesapno.setText("");
						hesapadi.setText("");
						yetkili.setText("");
						mahalle.setText("");
						ilce.setText("");
						sehir.setText("");
						ulke.setText("");
						telefon.setText("");
					} else {
						hesapadi.setText(sicilModel.getHesapadi());
						yetkili.setText(sicilModel.getYetkili());
						mahalle.setText(sicilModel.getMahalle());
						ilce.setText(sicilModel.getIlce());
						sehir.setText(sicilModel.getIl());
						ulke.setText(sicilModel.getUlke());
						telefon.setText(sicilModel.getDefaultTelefonString());
						((EkstreController) controller).refreshEkstreViewModel(ekstreTableModel.getList(), EkstreView.this.sicilModel, baslama.getText(), bitis.getText(),
								String.valueOf(comboDepo.getSelectedItem()));

						for (Object obj : ekstreTableModel.getList()) {
							EkstreViewModel ekstreViewModel = (EkstreViewModel) obj;
							ekstreToplamTableModel.setBorcToplam(ekstreToplamTableModel.getBorcToplam().add(ekstreViewModel.getBorc()));
							ekstreToplamTableModel.setAlacakToplam(ekstreToplamTableModel.getAlacakToplam().add(ekstreViewModel.getAlacak()));
						}

					}

					ekstreTableModel.fireTableDataChanged();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(MyApp.appHandler, e.getMessage());
				} finally {
					endProcess();
				}
			}
		});
		thread.start();

	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		sicilModelYukle();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		sicilModelYukle();

	}

}
