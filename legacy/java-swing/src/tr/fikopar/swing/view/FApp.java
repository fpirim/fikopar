package tr.fikopar.swing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import tr.fikopar.swing.component.FTabbedPane;
import tr.fikopar.swing.component.FToolBar;
import tr.fikopar.swing.table.FTable;

public abstract class FApp extends JFrame implements ChangeListener {

	private JMenuBar menuBar;
	private FToolBar myToolBar;
	private FTabbedPane tabbedPane;
	
	
	private static FProperties properties;
	protected static FApp appHandler;
	
	public static FApp getAppHandler() {
		return appHandler;
	}
	
	public void addMenu(JMenu menu) {
		menuBar.add(menu);
	}
	
	public static FProperties getProperties() {
		return properties;
	}
	
	
	protected void init(FProperties defaultProperties) {
		properties = FPropertiesAdapter.readProperties(defaultProperties);
		menuBar = new JMenuBar();
		createMenu();
		setJMenuBar(menuBar);
		getContentPane().setLayout(new BorderLayout());

		myToolBar = new FToolBar();
		getContentPane().add(myToolBar, BorderLayout.NORTH);
		myToolBar.setVisible(false);

		tabbedPane = new FTabbedPane(FTabbedPane.TOP);
		tabbedPane.setTabShape(FTabbedPane.SHAPE_WINDOWS);
		tabbedPane.setColorTheme(FTabbedPane.COLOR_THEME_WINXP);
		tabbedPane.setShowCloseButtonOnTab(true);
		tabbedPane.setCloseAction(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPane.remove((FView) e.getSource());

			}
		});
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(this);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				FPropertiesAdapter.writeProperties(properties);
				System.exit(0);
			}
		});
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {				
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				properties.putUserProperties(FProperties.APPLICATION_WIDTH, String.valueOf(appHandler.getWidth()));
				properties.putUserProperties(FProperties.APPLICATION_HEIGHT, String.valueOf(appHandler.getHeight()));
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				properties.putUserProperties(FProperties.APPLICATION_X, String.valueOf(appHandler.getX()));
				properties.putUserProperties(FProperties.APPLICATION_Y, String.valueOf(appHandler.getY()));
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

	}

	public abstract void createMenu();

	
	public void start(final FProperties defaultProperties) {
		
		DefaultJasperReportsContext context = DefaultJasperReportsContext.getInstance();
		JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.font.name", "DejaVu Sans");
		JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.pdf.embedded", "true");
		JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.pdf.font.name", "DejaVu Sans");

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				UIManager.put("ascendingIcon", new ImageIcon(FTable.class.getResource("resources/arrow_down.png")));
				UIManager.put("descendingIcon", new ImageIcon(FTable.class.getResource("resources/arrow_up.png")));
				UIManager.put("closeIcon", new ImageIcon(FTable.class.getResource("resources/cancel.png")));
				UIManager.put("nextIcon", new ImageIcon(FTable.class.getResource("resources/arrow_right.png")));
				UIManager.put("previousIcon", new ImageIcon(FTable.class.getResource("resources/arrow_left.png")));
				UIManager.put("kaydetIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/diskette.png")));
				UIManager.put("yazdirIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/printer.png")));
				UIManager.put("satirsilIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/table_row_delete.png")));
				UIManager.put("satirekleIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/table_row_insert.png")));
				UIManager.put("silIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/cancel.png")));
				UIManager.put("yeniIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/page.png")));
				UIManager.put("duzenleIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/page_edit.png")));
				UIManager.put("logoIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/logo.png")));
				UIManager.put("listIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/list.png")));
				UIManager.put("secIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/sec.png")));
				UIManager.put("kapatIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/cross.png")));
				UIManager.put("aktarIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/report_go.png")));
				UIManager.put("kaydetaktarIcon",
						new ImageIcon(FApp.class.getResource("/tr/fikopar/swing/resources/report_disk.png")));
				
				UIManager.put("FTextField.focusBackground", Color.YELLOW);

				try {
					// saveUrl("properties.xml",
					// "https://fikopar.example.com/properties.xml");
					appHandler.init(defaultProperties);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				appHandler.setSize(Integer.parseInt(properties.getUserProperties(FProperties.APPLICATION_WIDTH)),
						Integer.parseInt(properties.getUserProperties(FProperties.APPLICATION_HEIGHT)));
				appHandler.setLocation(Integer.parseInt(properties.getUserProperties(FProperties.APPLICATION_X)),
						Integer.parseInt(properties.getUserProperties(FProperties.APPLICATION_Y)));
				appHandler.setVisible(true);

			}
		});
	}
	public static void addView(String title, FView view) {
		appHandler.tabbedPane.add(title, view);
		appHandler.tabbedPane.setSelectedComponent(view);
	}
	public static void removeView(final FView view) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				appHandler.tabbedPane.remove(view);		
			}
		});
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		FView view = (FView) tabbedPane.getSelectedComponent();
		myToolBar.removeAll();
		if (view != null) {
			for (JComponent component : view.getMyComponents())
				myToolBar.add(component);
		}
		if (view == null || view.getMyComponents().size() == 0)
			myToolBar.setVisible(false);
		else {
			myToolBar.setVisible(true);
			myToolBar.repaint();
		}
	}
	public void showDialog(String title, FView view) {
		showDialog(title, view, 1200, 700);
	}
	public void showDialog(String title, FView view, int width, int height) {
		FDialog dialog = new FDialog(appHandler, title, true);
		dialog.addView(view.getName(), view);
		dialog.setSize(width, height);
		dialog.setLocationRelativeTo(appHandler);
		dialog.setVisible(true);
	}
	/*
	 * public static void saveUrl(final String filename, final String urlString)
	 * throws MalformedURLException, IOException { BufferedInputStream in =
	 * null; FileOutputStream fout = null; try { in = new
	 * BufferedInputStream(new URL(urlString).openStream()); fout = new
	 * FileOutputStream(filename);
	 * 
	 * final byte data[] = new byte[1024]; int count; while ((count =
	 * in.read(data, 0, 1024)) != -1) { fout.write(data, 0, count); } } catch
	 * (Exception e) { e.printStackTrace(); } finally { if (in != null) {
	 * in.close(); } if (fout != null) { fout.close(); } } }
	 */

}
