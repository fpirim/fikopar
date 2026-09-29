package tr.fikopar.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import tr.fikopar.modules.alim.controller.AlimViewController;
import tr.fikopar.modules.alim.model.AlimViewModel;
import tr.fikopar.modules.alim.view.AlimView;
import tr.fikopar.modules.depolist.controller.DepoListController;
import tr.fikopar.modules.depolist.model.DepoListViewModel;
import tr.fikopar.modules.depolist.view.DepoListView;
import tr.fikopar.modules.etiket.controller.EtiketController;
import tr.fikopar.modules.etiket.model.EtiketViewModel;
import tr.fikopar.modules.etiket.view.EtiketView;
import tr.fikopar.modules.sayim.controller.SayimController;
import tr.fikopar.modules.sayim.model.SayimViewModel;
import tr.fikopar.modules.sayim.view.SayimView;
import tr.fikopar.modules.siparislist.controller.SiparisListController;
import tr.fikopar.modules.siparislist.model.SiparisListViewModel;
import tr.fikopar.modules.siparislist.view.SiparisListView;
import tr.fikopar.swing.view.FApp;
import tr.fikopar.swing.view.FController;
import tr.fikopar.swing.view.FView;

public class MyApp extends FApp implements ActionListener {

	private String[] menus = new String[] { 
			"Yedek Parça Kataloðu", 
			"Yedek Parça Tanýmlama Ýþlemleri", 
			"-", 
			"Sipariþ Alma", 
			"Sipariþ Verme", 
			"-",
			"Depo Giriþi",
			"-",
			"Sayým Giriþi", 
			"Etiket Yazdýr", 
			"-",
			// "Eksik Stoklar",
			// "-",
			"Çek Senet Dökümleri", 
			"Makbuz Dökümleri", 
			"-", 
			"Hesap Toplamlarý (Mizan)", 
			"Hesap Ekstresi" 
		};

	public void createMenu() {
		JMenu menu = new JMenu("Ýþlemler");

		for (String menuName : this.menus) {
			if (menuName.equals("-"))
				menu.addSeparator();
			else {
				JMenuItem menuItem = new JMenuItem(menuName);
				menuItem.addActionListener(this);
				menu.add(menuItem);
			}

		}
		addMenu(menu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FController controller = null;
		FView view = null;
		if (e.getActionCommand().equals("Sipariþ Alma")) {
			SiparisListViewModel siparisListViewModel = new SiparisListViewModel();
			siparisListViewModel.setTip(SiparisListViewModel.Tip.ALMA);
			
			controller = new SiparisListController();
			controller.setModel(siparisListViewModel);
			
			view = new SiparisListView();
			view.setName("Sipariþ Alma");
			
			controller.setView(view);
			
		} else if (e.getActionCommand().equals("Sipariþ Verme")) {
			SiparisListViewModel siparisListViewModel = new SiparisListViewModel();
			siparisListViewModel.setTip(SiparisListViewModel.Tip.VERME);
			
			controller = new SiparisListController();
			controller.setModel(siparisListViewModel);
			
			view = new SiparisListView();
			view.setName("Sipariþ Verme");
			
			controller.setView(view);
		} else if (e.getActionCommand().equals("Depo Giriþi")) {
			DepoListViewModel depoListViewModel = new DepoListViewModel();
			
			controller = new DepoListController();
			controller.setModel(depoListViewModel);
			
			view = new DepoListView();
			view.setName("Depo Giriþi");
			
			controller.setView(view);
		} else if (e.getActionCommand().equals("Sayým Giriþi")) { 
			SayimViewModel sayimViewModel = new SayimViewModel();
			
			controller = new SayimController();
			controller.setModel(sayimViewModel);
			
			view = new SayimView();
			view.setName("Sayým Giriþi");
			
			controller.setView(view);
			
		} else if(e.getActionCommand().equals("Etiket Yazdýr")) {
			EtiketViewModel etiketModel = new EtiketViewModel();
			
			controller = new EtiketController();
			controller.setModel(etiketModel);
			
			view = new EtiketView();
			view.setName("Etiket Yazdýr");
			
			controller.setView(view);
		} else if(e.getActionCommand().equals("Yedek Parça Tanýmlama Ýþlemleri")) {
			AlimViewModel alimViewModel = new AlimViewModel();
			
			controller = new AlimViewController();
			controller.setModel(alimViewModel);
			
			view = new AlimView();
			view.setName("Yedek Parça Tanýmlama Ýþlemleri");
			
			controller.setView(view);
		}
		

		/*
		 * else if (e.getActionCommand().equals("Yedek Parça Kataloðu")) {
		 * controller = new ListeController(); view = new ListeView(controller);
		 * view.setName("Yedek Parça Kataloðu"); } else if
		 * (e.getActionCommand().equals("Hesap Toplamlarý (Mizan)")) {
		 * controller = new MizanController(); view = new MizanView(controller);
		 * view.setName("Hesap Toplamlarý (Mizan)"); } else if
		 * (e.getActionCommand().equals("Eksik Stoklar")) { controller = new
		 * EksikController(); view = new EksikListView(controller);
		 * view.setName("Eksik Stoklar"); } else if
		 * (e.getActionCommand().equals("Sayým Giriþi")) { controller = new
		 * SayimController(new SayimViewModel()); view = new
		 * SayimView(controller); view.setName("Sayým Giriþi"); } else if
		 * (e.getActionCommand().equals("Etiket Yazdýr")) { controller = new
		 * EtiketController(); view = new EtiketView(controller); view.setName(
		 * "Etiket Yazdýr"); } else if (e.getActionCommand().equals(
		 * "Çek Senet Dökümleri")) { controller = new CekSenetController(); view
		 * = new CekSenetView(controller); view.setName("Çek Senet Dökümleri");
		 * } else if (e.getActionCommand().equals("Makbuz Dökümleri")) {
		 * controller = new MakbuzController(); view = new
		 * MakbuzView(controller); view.setName("Makbuz Dökümleri"); } else if
		 * (e.getActionCommand().equals("Hesap Ekstresi")) { controller = new
		 * EkstreController(); view = new EkstreView(controller); view.setName(
		 * "Hesap Ekstresi"); }
		 */
		if (view != null) {
			addView(view.getName(), view);
		}
	}

	public static void main(String[] args) {
		appHandler = new MyApp();
		appHandler.start(MyProperties.createDefaultProperties());
		
	}

}
