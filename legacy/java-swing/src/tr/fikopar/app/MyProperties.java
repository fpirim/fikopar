package tr.fikopar.app;

import java.awt.Color;
import java.awt.Font;

import tr.fikopar.swing.db.models.FTableCellStyle;
import tr.fikopar.swing.db.models.FTableStyle;
import tr.fikopar.swing.view.FProperties;

public class MyProperties {
	public static final String LABEL_PORT = "LABEL_PORT";
	public static final String COMPANY_NAME = "COMPANY_NAME";
	public static final String SIPARIS_REFRESH_RATE = "SIPARIS_REFRESH_RATE";
	public static FProperties createDefaultProperties() {
		FProperties properties = new FProperties();
		properties.putUserProperties(LABEL_PORT, "\\\\192.168.1.4\\argox");
		properties.putUserProperties(COMPANY_NAME, "COMPANY_NAME");
		properties.putUserProperties(SIPARIS_REFRESH_RATE, "30000");
		
		properties.putTableStyle(new FTableStyle("tr.fikopar.ceksenet.view.CekSenetTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.DATE,"Vade Tarihi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.DATE,"Giriþ Tarihi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Depo",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak Tipi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING,"Adres",Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Tutar",Font.PLAIN, 130,"#,###.00"),
						new FTableCellStyle(FTableCellStyle.STRING,"Çýkýþ Hesap No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Çýkýþ Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING,"Durum",Font.PLAIN, 150)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.ceksenet.view.CekSenetTableModel$CekSenetTableToplamModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.DATE,"Vade Tarihi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.DATE,"Giriþ Tarihi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Depo",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak Tipi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING,"Adres",Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Tutar",Font.PLAIN, 130,"#,###.00"),
						new FTableCellStyle(FTableCellStyle.STRING,"Çýkýþ Hesap No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Çýkýþ Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING,"Durum",Font.PLAIN, 150)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.ekstre.view.EkstreTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.DATE,"Tarih",Font.PLAIN, 92),
						new FTableCellStyle(FTableCellStyle.STRING,"Tipi",Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak No",Font.PLAIN, 135),
						new FTableCellStyle(FTableCellStyle.DATE,"Vade",Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING,"Depo",Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING,"Açýklama",Font.PLAIN, 254),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Borç",Font.PLAIN, 110, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Alacak",Font.PLAIN, 110, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Borç Bakiye",Font.BOLD, 108, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Alacak Bakiye",Font.BOLD, 122, "#,##0.00")
						
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.ekstre.view.EkstreToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 92),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 135),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 75),
						new FTableCellStyle(FTableCellStyle.STRING,Font.PLAIN, 254),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.PLAIN, 110, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.PLAIN, 110, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.STRING, Font.BOLD, 108),
						new FTableCellStyle(FTableCellStyle.STRING, Font.BOLD, 122)
						
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.siparis.view.SiparisTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Id", Font.BOLD, 40, "###"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No", Font.PLAIN, 70, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý",Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "1.Ýsk%",Font.PLAIN, 60, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "2.Ýsk%",Font.PLAIN, 60, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kdv%",Font.PLAIN, 60, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet",Font.PLAIN, 70, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Fiyatý",Font.PLAIN, 120, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Tutar", Font.BOLD, 120, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Bakiye", Font.BOLD, 70, "#,##0.00", Color.BLUE)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.depo.view.DepoTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Id", Font.BOLD, 40, "###"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No", Font.PLAIN, 70, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý",Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet",Font.PLAIN, 70, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Bakiye", Font.BOLD, 70, "#,##0.00", Color.BLUE)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.siparis.view.SiparisToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 40),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 120, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 70, "#,##0.00")
		})); 
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.sayim.view.SayimTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Id",Font.BOLD, 40, "###"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No",Font.PLAIN, 70, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý", Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Raf No", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Sayým Zamaný", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kdv%", Font.PLAIN, 60, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet", Font.PLAIN, 70, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Maliyeti", Font.PLAIN, 120, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Tutar", Font.BOLD, 120, "#,##0.00")
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.sayim.view.SayimToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 40),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.BOLD, 120),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 120, "#,##0.00")
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.etiket.view.EtiketTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No",Font.PLAIN, 70, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý", Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Paket Adedi", Font.PLAIN, 70, "#,###"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Etiket Adedi", Font.PLAIN, 70, "#,###")
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.mizan.view.MizanTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap No",Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap Adý",Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Yetkili",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, "Mahalle", Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, "Ýlçe", Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, "Ýl", Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, "Ülke", Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, "Plasiyer", Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Borç", Font.PLAIN, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Alacak", Font.PLAIN, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Borç Bakiye", Font.PLAIN, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Alacak Bakiye", Font.PLAIN, 100, "#,###.00")
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.mizan.view.MizanTableModel$MizanToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 100, "#,###.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 100, "#,###.00")
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.liste.view.ListeTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No", Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý", Font.PLAIN, 300),
						new FTableCellStyle(FTableCellStyle.STRING, "Not", Font.PLAIN, 50),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka", Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet", Font.PLAIN, 70, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Fiyatý", Font.PLAIN, 100, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.STRING, "Deð.", Font.PLAIN, 40),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Min Adet", Font.PLAIN, 50, "#,##0"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Max Adet", Font.PLAIN, 50, "#,##0"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Sip.Adet", Font.BOLD, 50, "#,##0"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Ver.Adet", Font.BOLD, 50, "#,##0"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kampanya", Font.PLAIN, 80, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.BOOLEAN, "K.Bit.", Font.BOLD, 30)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.contentasist.sicilmodel.SicilModelContentAsistTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap No",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap Adý",Font.PLAIN, 250),
						new FTableCellStyle(FTableCellStyle.STRING, "Yetkili",Font.PLAIN, 200)
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.makbuz.view.MakbuzTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.DATE,"Tarih",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Evrak No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Tip",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.DATE,"Ödeme Tipi",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Depo",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap No",Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING,"Adres",Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.STRING,"Açýklama",Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Tutar",Font.BOLD, 130, "#,##0.00")
						
		}));
		
		properties.putTableStyle(new FTableStyle("tr.fikopar.makbuz.view.MakbuzTableModel$MakbuzTableToplamModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 120),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 150),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 130, "#,##0.00")
						
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.siparislist.view.SiparisListTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Sipariþ No",Font.PLAIN, 50, "###"),
						new FTableCellStyle(FTableCellStyle.DATE,"Tarihi",Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap No",Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap Adý",Font.PLAIN, 450),
						new FTableCellStyle(FTableCellStyle.STRING,"Adres",Font.PLAIN, 268),
						new FTableCellStyle(FTableCellStyle.STRING,"Sevk Þekli",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING,"Sipariþi Alan",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Bakiye",Font.PLAIN, 100, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.STRING,"Not",Font.PLAIN, 230),
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.siparislist.view.SiparisListTableModel$SiparisListToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 50),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 450),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 268),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.NUMERIC,Font.BOLD, 100, "#,##0.00", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 230),
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.depolist.view.DepoListTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC,"Depo No",Font.PLAIN, 50, "###"),
						new FTableCellStyle(FTableCellStyle.DATE,"Tarihi",Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap No",Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING,"Hesap Adý",Font.PLAIN, 450),
						new FTableCellStyle(FTableCellStyle.STRING,"Adres",Font.PLAIN, 268),
						new FTableCellStyle(FTableCellStyle.STRING,"Depo Alan",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING,"Not",Font.PLAIN, 230),
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.stokkart.view.HareketTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.DATE, "Tarih", Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, "Tip", Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, "Evrak No", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Depo", Font.PLAIN, 40),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap No", Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap Adý", Font.PLAIN, 315),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet", Font.BOLD, 70, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "1.Ýsk%", Font.PLAIN, 50, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "2.Ýsk%", Font.PLAIN, 50, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kdv%", Font.PLAIN, 50, "#,##0.0000"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Brüt Fiyat", Font.PLAIN, 70, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Net Fiyat", Font.BOLD, 70, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Dahil Fiyat", Font.BOLD, 70, "#,##0.00")
						
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.stokkart.view.HareketToplamTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 40),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 70),
						new FTableCellStyle(FTableCellStyle.STRING, Font.PLAIN, 315),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 70, "#,##0.0000", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 50, "#,##0.0000", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 50, "#,##0.0000", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 50, "#,##0.0000", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 70, "#,##0.00", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 70, "#,##0.00", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, Font.BOLD, 70, "#,##0.00", Color.BLUE)
						
		}));
		
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.contentasist.stokmodel.StokModelContentAsistTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No",Font.PLAIN, 20, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No", Font.PLAIN, 80),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý", Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Not", Font.BOLD, 50),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Adet", Font.BOLD, 100, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Fiyatý", Font.PLAIN, 100, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kampanya", Font.BOLD, 100, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Paket", Font.PLAIN, 40, "#,###")
						
		}));
		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.alim.view.AlimTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Id",Font.BOLD, 30, "###", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kat.No",Font.PLAIN, 40, "###"),
						new FTableCellStyle(FTableCellStyle.STRING, "Stok No",Font.PLAIN, 60),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça No",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Tipi", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Parça Adý", Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, "Marka", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Not", Font.BOLD, 60),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Fiyatý", Font.BOLD, 90, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.STRING, "Oem No", Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "1.Ýsk%", Font.PLAIN, 50, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "2.Ýsk%", Font.PLAIN, 50, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Kdv", Font.PLAIN, 50, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Maliyet", Font.PLAIN, 90, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.STRING, "Dvz", Font.PLAIN, 30),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Marj", Font.BOLD, 60, "#,##0.00"),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Net Maliyet", Font.BOLD, 90, "#,##0.00", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Liste", Font.BOLD, 90, "#,##0.00", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Fark", Font.BOLD, 90, "#,##0.00", Color.RED)
						
		}));

		properties.putTableStyle(new FTableStyle("tr.fikopar.modules.alim.view.AlimViewListTableModel", 
				new FTableCellStyle[] {
						new FTableCellStyle(FTableCellStyle.NUMERIC, "Id",Font.BOLD, 60, "###", Color.BLUE),
						new FTableCellStyle(FTableCellStyle.DATE, "Tarih",Font.PLAIN, 90),
						new FTableCellStyle(FTableCellStyle.BOOLEAN, "D",Font.PLAIN, 30),
						new FTableCellStyle(FTableCellStyle.BOOLEAN, "Y",Font.PLAIN, 30),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap No",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Hesap Adý",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, "Yetkili",Font.PLAIN, 200),
						new FTableCellStyle(FTableCellStyle.STRING, "Not",Font.PLAIN, 100),
						new FTableCellStyle(FTableCellStyle.STRING, "Excel",Font.PLAIN, 100)
		}));

		
		return properties;
	}

}
