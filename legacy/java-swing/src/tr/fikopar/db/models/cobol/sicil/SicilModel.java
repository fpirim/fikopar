package tr.fikopar.db.models.cobol.sicil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "`COB_SICIL`", catalog="`COBOL`")
public class SicilModel implements Serializable {
	@Id
	@Column(name="`SC-HESAPNO`", columnDefinition="VARCHAR", length=15, nullable=false)
	private String hesapno;
	@Column(name="`SC-HESAPADI`", columnDefinition="VARCHAR", length=50, nullable=false)
	private String hesapadi;
	@Column(name="`SC-YETKILI`", columnDefinition="VARCHAR", length=50, nullable=false)
	private String yetkili;
	@Column(name="`SC-ADRES1`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String adres1;
	@Column(name="`SC-ADRES2`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String adres2;
	@Column(name="`SC-ADRES3`", columnDefinition="VARCHAR", length=40, nullable=false)
	private String adres3;
	@Column(name="`SC-POSKOD`", columnDefinition="VARCHAR", length=6, nullable=false)
	private String poskod;
	@Column(name="`SC-MAHALLE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String mahalle;
	@Column(name="`SC-ILCE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String ilce;
	@Column(name="`SC-IL`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String il;
	@Column(name="`SC-ULKE`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String ulke;
	@Column(name="`SC-VDA`", columnDefinition="VARCHAR", length=30, nullable=false)
	private String vda;
	@Column(name="`SC-VNO`", columnDefinition="VARCHAR", length=10, nullable=false)
	private String vno;
	@Column(name="`SC-TCNO`", columnDefinition="VARCHAR", length=11, nullable=false)
	private String tcno;
	@Column(name="`SC-TSNO`", columnDefinition="VARCHAR", length=11, nullable=false)
	private String tsno;
	@Column(name="`SC-MERSIS`", columnDefinition="VARCHAR", length=20, nullable=false)
	private String mersis;
	@Column(name="`SC-TUR`", columnDefinition="VARCHAR", length=3, nullable=false)
	private String tur;
	@Column(name = "`SC-KREDI`", columnDefinition = "DECIMAL", precision = 18, scale = 4, nullable = false)
	private BigDecimal kredi;
	@Column(name = "`SC-GUN`", columnDefinition = "DECIMAL", precision = 5, scale = 0, nullable = false)
	private BigDecimal gun;
	@Column(name = "`SC-ISKONTO1`", columnDefinition = "DECIMAL", precision = 8, scale = 4, nullable = false)
	private BigDecimal iskonto1;
	@Column(name = "`SC-ISKONTO2`", columnDefinition = "DECIMAL", precision = 8, scale = 4, nullable = false)
	private BigDecimal iskonto2;
	@Column(name = "`SC-PLKOD`", columnDefinition = "DECIMAL", precision = 4, scale = 0, nullable = false)
	private BigDecimal plkod;
	
	@OneToMany(targetEntity = SiciltelModel.class, fetch = FetchType.LAZY, cascade= CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="`SCT-HESAPNO`", referencedColumnName="`SC-HESAPNO`")
	private Set<SiciltelModel> siciltelModels = new HashSet<SiciltelModel>();
	
	@OneToMany(targetEntity = SicilHareket.class, fetch = FetchType.LAZY)
	@JoinColumn(name="`CH-HESAPNO`", referencedColumnName="`SC-HESAPNO`")
	private Set<SicilHareket> sicilHarekets = new HashSet<>();
	
	public Set<SiciltelModel> getSiciltelModels() {
		return siciltelModels;
	}

	public void setSiciltelModels(Set<SiciltelModel> siciltelModels) {
		this.siciltelModels = siciltelModels;
	}

	
	public Set<SicilHareket> getSicilHarekets() {
		return sicilHarekets;
	}

	public void setSicilHarekets(Set<SicilHareket> sicilHarekets) {
		this.sicilHarekets = sicilHarekets;
	}
	
	
	public static String maskNumber(BigDecimal number, String mask) {
		// format the number
		int index = 0;
		StringBuilder maskedNumber = new StringBuilder();
		for (int i = 0; i < mask.length(); i++) {
			char c = mask.charAt(i);
			if (c == '#') {
				maskedNumber.append(String.valueOf(number).charAt(index));
				index++;
			} else if (c == '+' || c == '-') {
				maskedNumber.append(c);
				index++;
			} else {
				maskedNumber.append(c);
			}
		}

		// return the masked number
		return maskedNumber.toString();
	}
	
	public String getDefaultTelefonString() {
		BigDecimal hMillion = new BigDecimal(100000000);
		BigDecimal tenThousand = new BigDecimal(100000000);
		String temp = "";
		for (SiciltelModel siciltelModel : getSiciltelModels()) {
			if (siciltelModel.getSira().compareTo(BigDecimal.ZERO) == 0) {
				
				BigDecimal telsira = siciltelModel.getTel().divide(hMillion);
				BigDecimal faxsira = (siciltelModel.getTel().subtract(telsira.multiply(hMillion))).divide(tenThousand);
				BigDecimal cepsira = siciltelModel.getTel().subtract(telsira.multiply(hMillion)).subtract(faxsira.multiply(tenThousand));
				for (SiciltelModel i : siciltelModels) {
					if (siciltelModel.getSira().compareTo(BigDecimal.ZERO) == 0)
						continue;
					if (siciltelModel.getSira().compareTo(telsira) == 0)
						temp += " Tel:" + maskNumber(i.getTel(), "+##-###-#######");
					if (siciltelModel.getSira().compareTo(faxsira) == 0)
						temp += " Fax:" + maskNumber(i.getTel(), "+##-###-#######");
					if (siciltelModel.getSira().compareTo(cepsira) == 0)
						temp += " Cep:" + maskNumber(i.getTel(), "+##-###-#######");
				}
			}

		}
		return temp;
	}

	public String getHesapno() {
		return hesapno;
	}

	public void setHesapno(String hesapno) {
		this.hesapno = hesapno;
	}

	public String getHesapadi() {
		return hesapadi;
	}

	public void setHesapadi(String hesapadi) {
		this.hesapadi = hesapadi;
	}

	public String getYetkili() {
		return yetkili;
	}

	public void setYetkili(String yetkili) {
		this.yetkili = yetkili;
	}

	public String getAdres1() {
		return adres1;
	}

	public void setAdres1(String adres1) {
		this.adres1 = adres1;
	}

	public String getAdres2() {
		return adres2;
	}

	public void setAdres2(String adres2) {
		this.adres2 = adres2;
	}

	public String getAdres3() {
		return adres3;
	}

	public void setAdres3(String adres3) {
		this.adres3 = adres3;
	}

	public String getPoskod() {
		return poskod;
	}

	public void setPoskod(String poskod) {
		this.poskod = poskod;
	}

	public String getMahalle() {
		return mahalle;
	}

	public void setMahalle(String mahalle) {
		this.mahalle = mahalle;
	}

	public String getIlce() {
		return ilce;
	}

	public void setIlce(String ilce) {
		this.ilce = ilce;
	}

	public String getIl() {
		return il;
	}

	public void setIl(String il) {
		this.il = il;
	}

	public String getUlke() {
		return ulke;
	}

	public void setUlke(String ulke) {
		this.ulke = ulke;
	}

	public String getVda() {
		return vda;
	}

	public void setVda(String vda) {
		this.vda = vda;
	}

	public String getVno() {
		return vno;
	}

	public void setVno(String vno) {
		this.vno = vno;
	}

	public String getTcno() {
		return tcno;
	}

	public void setTcno(String tcno) {
		this.tcno = tcno;
	}

	public String getTsno() {
		return tsno;
	}

	public void setTsno(String tsno) {
		this.tsno = tsno;
	}

	public String getMersis() {
		return mersis;
	}

	public void setMersis(String mersis) {
		this.mersis = mersis;
	}

	public String getTur() {
		return tur;
	}

	public void setTur(String tur) {
		this.tur = tur;
	}

	public BigDecimal getKredi() {
		return kredi;
	}

	public void setKredi(BigDecimal kredi) {
		this.kredi = kredi;
	}

	public BigDecimal getGun() {
		return gun;
	}
	public void setGun(BigDecimal gun) {
		this.gun = gun;
	}
	
	public BigDecimal getIskonto1() {
		return iskonto1;
	}

	public void setIskonto1(BigDecimal iskonto1) {
		this.iskonto1 = iskonto1;
	}

	public BigDecimal getIskonto2() {
		return iskonto2;
	}

	public void setIskonto2(BigDecimal iskonto2) {
		this.iskonto2 = iskonto2;
	}

	public BigDecimal getPlkod() {
		return plkod;
	}
	public void setPlkod(BigDecimal plkod) {
		this.plkod = plkod;
	}

	
}
