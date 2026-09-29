package tr.fikopar.modules.etiket.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.hibernate.Session;

import com.jinvoke.CallingConvention;
import com.jinvoke.JInvoke;
import com.jinvoke.NativeImport;

import tr.fikopar.app.MyApp;
import tr.fikopar.app.MyProperties;
import tr.fikopar.db.models.cobol.stok.StokModel;
import tr.fikopar.db.models.cobol.stok.StokPK;
import tr.fikopar.db.persistence.HibernateUtil;
import tr.fikopar.modules.etiket.model.EtiketDetayModel;
import tr.fikopar.modules.etiket.model.EtiketViewModel;
import tr.fikopar.swing.view.FController;

public class EtiketController extends FController {

	public static final int HOR_LABEL_WIDTH = 209;
	public static final int SPLIT_STRING_LENGTH = 30;

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_CreatePrn", convention = CallingConvention.STDCALL)
	public static native int A_CreatePrn(int selection, byte[] filename);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_CreateUSBPort", convention = CallingConvention.STDCALL)
	public static native int A_CreateUSBPort(int nport);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Set_Darkness", convention = CallingConvention.STDCALL)
	public static native int A_Set_Darkness(int darkness);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Print_Out", convention = CallingConvention.STDCALL)
	public static native int A_Print_Out(int width, int height, int copies, int amount);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Prn_Text", convention = CallingConvention.STDCALL)
	public static native int A_Prn_Text(int x, int y, int ori, int font, int typee, int hor_factor, int ver_factor,
			byte mode, int numeric, byte[] data);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Prn_Barcode", convention = CallingConvention.STDCALL)
	public static native int A_Prn_Barcode(int x, int y, int ori, byte typee, int narrow, int width, int height,
			byte mode, int numeric, byte[] data);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Prn_Text_TrueType", convention = CallingConvention.STDCALL)
	public static native int A_Prn_Text_TrueType(int x, int y, int FSize, byte[] FType, int Fspin, int FWeight,
			int FItalic, int FUnline, int FStrikeOut, byte[] id_name, byte[] data, int mem_code);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Get_Graphic", convention = CallingConvention.STDCALL)
	public static native int A_Get_Graphic(int x, int y, int mem_mode, byte format, byte[] filename);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Draw_Box", convention = CallingConvention.STDCALL)
	public static native int A_Draw_Box(int mode, int x, int y, int width, int height, int top, int side);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Draw_Line", convention = CallingConvention.STDCALL)
	public static native int A_Draw_Line(int mode, int x, int y, int width, int height);

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_ClosePrn", convention = CallingConvention.STDCALL)
	public static native void A_ClosePrn();

	@NativeImport(library = "c:/windows/system32/winppla.dll", function = "A_Set_Backfeed", convention = CallingConvention.STDCALL)
	public static native int A_Set_Backfeed(int back);

	static {
		JInvoke.initialize();
	}

	private static byte[] str2byte(String value) {
		byte[] arr = new byte[value.length() + 1];
		for (int i = 0; i < arr.length - 1; i++)
			arr[i] = value.getBytes()[i];
		arr[arr.length - 1] = '\0';
		return arr;
	}

	public void loadStokModel(int index) throws Exception {

		EtiketViewModel etiketModel = (EtiketViewModel) model;
		Session session = HibernateUtil.getSessionFactory().openSession();

		EtiketDetayModel etiketDetayModel = etiketModel.getEtiketDetayModels().get(index);
		StokModel stokModel = (StokModel) session.get(StokModel.class,
				new StokPK(etiketDetayModel.getKatno(), etiketDetayModel.getStno()));
		if (stokModel == null) {
			session.clear();
			session.close();
			throw new Exception(
					etiketDetayModel.getKatno() + "," + etiketDetayModel.getStno() + " ait stok kartý bulunamadý!.");
		}

		etiketDetayModel.setKatno(stokModel.getStokPK().getKatno());
		etiketDetayModel.setMarka(stokModel.getMarka());
		etiketDetayModel.setOemno(stokModel.getOemno());
		etiketDetayModel.setCinsi(stokModel.getCinsi());
		etiketDetayModel.setPrcno(stokModel.getPrcno());
		etiketDetayModel.setStno(stokModel.getStokPK().getStno());
		etiketDetayModel.setTipi(stokModel.getTipi());

		session.clear();
		session.close();

	}

	private int i = 0;
	private String date;

	public void openPrinter() {
		String etiketport = MyApp.getProperties().getUserProperties(MyProperties.LABEL_PORT);
		if (etiketport.startsWith("\\\\")) {
			A_CreatePrn(10, str2byte(etiketport));
		} else if (etiketport.startsWith("USB")) {
			int usb_port = Integer.parseInt(etiketport.substring(etiketport.indexOf(",") + 1));
			A_CreatePrn(11, null);
			A_CreateUSBPort(usb_port);
		} else
			throw new RuntimeException("Etiket yazýcý makinasý portu hatalý [" + etiketport + "]");

		A_Set_Backfeed(320);
		A_Set_Darkness(12);

		i = 0;
		date = new SimpleDateFormat("dd/MM/yy").format(new Date());
	}

	public void print(EtiketDetayModel etiketDetayModel) {

		A_Prn_Barcode(52 + (i * 209), 149, 4, (byte) 'A', 2, 4, 25, (byte) 'n', 0,
				str2byte(etiketDetayModel.getStno().trim()));

		A_Prn_Text(125 + (i * 209), 15, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte(etiketDetayModel.getTipi().trim()));

		String stokadi = etiketDetayModel.getCinsi().trim();
		String tempadi = stokadi;
		if (stokadi.length() > 30) {
			tempadi = "";
			StringTokenizer tokenizer = new StringTokenizer(stokadi, " ");
			int l = 1;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (tempadi.length() + token.length() > 30 * l) {
					tempadi += "\n" + token;
					l++;
				} else
					tempadi += (tempadi.equals("") ? "" : " ") + token;
			}
		}
		StringTokenizer tokenizer = new StringTokenizer(tempadi, "\n");
		int s = 0;
		while (tokenizer.hasMoreElements()) {
			A_Prn_Text(141 + (i * 209) + s, 15, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte(tokenizer.nextToken().trim()));
			s += 17;
		}

		A_Prn_Text(16 + (i * 209), 257, 1, 2, 0, 1, 1, (byte) 'n', 0, String.valueOf(date).getBytes());
		A_Prn_Text(70 + (i * 209), 257, 1, 1, 0, 1, 1, (byte) 'n', 0, str2byte(etiketDetayModel.getMarka()));
		A_Prn_Text(115 + (i * 209), 215, 4, 3, 0, 1, 1, (byte) 'n', 0,
				str2byte(String.valueOf(etiketDetayModel.getPaketAdet())));
		i++;
		if (i == 2) {
			i = 0;
			A_Print_Out(1, 1, 1, 1);
		}

	}

	private void printOut() {
		if (i > 0)
			A_Print_Out(1, 1, 1, 1);
		A_ClosePrn();
	}

	public void printLabels() {
		EtiketViewModel etiketModel = (EtiketViewModel) model;
		openPrinter();
		for (Iterator<EtiketDetayModel> itr = etiketModel.getEtiketDetayModels().iterator(); itr.hasNext();) {
			EtiketDetayModel etiketDetayModel = itr.next();
			for (int k = 0; k < etiketDetayModel.getEtiketAdet().intValue(); k++)
				print(etiketDetayModel);
		}
		printOut();
	}

	/*
	 * public static void main(String[] args) { A_CreatePrn(11, null);
	 * A_CreateUSBPort(1); A_Set_Backfeed(320); A_Set_Darkness(12);
	 * 
	 * EtiketModel etiketModel = new EtiketModel(); etiketModel.setKatno(1);
	 * etiketModel.setStno("021032"); etiketModel.setPrcno("3610269");
	 * etiketModel.setTipi("240-255-260"); etiketModel.setCinsi(
	 * "DEB.BASKISI 12'' SPLIT TORK"); etiketModel.setMarka("VALEO");
	 * etiketModel.setPaketAdet(BigDecimal.ONE); etiketModel.setEtiketAdet(new
	 * BigDecimal(10));
	 * 
	 * // BÝRÝNCÝ SATIR A_Prn_Barcode(52 + (0 * 209), 149, 4, (byte) 'A', 2, 4,
	 * 25, (byte) 'n', 0, str2byte("021032")); A_Prn_Text(108 + (0 * 209), 15,
	 * 4, 5, 0, 1, 1, (byte) 'n', 0, str2byte("3610269")); A_Prn_Text(125 + (0 *
	 * 209), 15, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte("240-255-260"));
	 * A_Prn_Text(141 + (0 * 209), 15, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte(
	 * "DEB.BASKISI 12'' SPLIT TORK"));
	 * 
	 * String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
	 * 
	 * A_Prn_Text(16 + (0 * 209), 257, 1, 2, 0, 1, 1, (byte) 'n', 0,
	 * String.valueOf(date).getBytes()); A_Prn_Text(70 + (0 * 209), 257, 1, 1,
	 * 0, 1, 1, (byte) 'n', 0, str2byte("VALEO")); A_Prn_Text(115 + (0 * 209),
	 * 215, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte("1"));
	 * 
	 * // ÝKÝNCÝ SATIR
	 * 
	 * A_Prn_Barcode(52 + (1 * 209), 149, 4, (byte) 'A', 2, 4, 25, (byte) 'n',
	 * 0, str2byte("021032")); A_Prn_Text(108 + (1 * 209), 15, 4, 5, 0, 1, 1,
	 * (byte) 'n', 0, str2byte("3610269")); A_Prn_Text(125 + (1 * 209), 15, 4,
	 * 3, 0, 1, 1, (byte) 'n', 0, str2byte("240-255-260")); A_Prn_Text(141 + (1
	 * * 209), 15, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte(
	 * "DEB.BASKISI 12'' SPLIT TORK"));
	 * 
	 * date = new SimpleDateFormat("dd/MM/yy").format(new Date());
	 * 
	 * A_Prn_Text(16 + (1 * 209), 257, 1, 2, 0, 1, 1, (byte) 'n', 0,
	 * String.valueOf(date).getBytes()); A_Prn_Text(70 + (1 * 209), 257, 1, 1,
	 * 0, 1, 1, (byte) 'n', 0, str2byte("VALEO")); A_Prn_Text(115 + (1 * 209),
	 * 215, 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte("1"));
	 * 
	 * // ÝKÝNCÝ KOLON
	 * 
	 * A_Prn_Barcode(52 + (0 * 209), 149 + (1 * 295), 4, (byte) 'A', 2, 4, 25,
	 * (byte) 'n', 0, str2byte("021032")); A_Prn_Text(108 + (0 * 209), 15 + (1 *
	 * 295), 4, 5, 0, 1, 1, (byte) 'n', 0, str2byte("3610269")); A_Prn_Text(125
	 * + (0 * 209), 15 + (1 * 295), 4, 3, 0, 1, 1, (byte) 'n', 0,
	 * str2byte("240-255-260")); A_Prn_Text(141 + (0 * 209), 15 + (1 * 295), 4,
	 * 3, 0, 1, 1, (byte) 'n', 0, str2byte("DEB.BASKISI 12'' SPLIT TORK"));
	 * 
	 * date = new SimpleDateFormat("dd/MM/yy").format(new Date());
	 * 
	 * A_Prn_Text(16 + (0 * 209), 257 + (1 * 295), 1, 2, 0, 1, 1, (byte) 'n', 0,
	 * String.valueOf(date).getBytes()); A_Prn_Text(70 + (0 * 209), 257 + (1 *
	 * 295), 1, 1, 0, 1, 1, (byte) 'n', 0, str2byte("VALEO")); A_Prn_Text(115 +
	 * (0 * 209), 215 + (1 * 295), 4, 3, 0, 1, 1, (byte) 'n', 0, str2byte("1"));
	 * 
	 * A_Print_Out(1, 1, 1, 1);
	 * 
	 * A_ClosePrn();
	 * 
	 * }
	 */
}
