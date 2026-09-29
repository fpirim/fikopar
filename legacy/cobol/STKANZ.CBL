       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'EKSTRE'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'sayim.cpy'.
       copy 'hareket.cpy'.
       copy 'stok.cpy'.
       copy 'depo.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'sayim.cpz'.
       copy 'hareket.cpz'.
       copy 'stok.cpz'.
       copy 'depo.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-KOD            PIC X.
               03 TP-TARIH.
                    04 TP-YIL       PIC 9999.
                    04 TP-AY        PIC 99.
                    04 TP-GUN       PIC 99.
               03 TP-SIRA           PIC 9(10).
           02 TP-HESAPNO            PIC X(15).
           02 TP-HESAPADI           PIC X(50).
           02 TP-YETKILI            PIC X(40).
           02 TP-BAG OCCURS 20 TIMES.
               03 TP-BAG-TIP            PIC 99.
               03 TP-BAG-DEPONO         PIC 9999.
               03 TP-BAG-DPADI          PIC X(30).
               03 TP-BAG-EVRAKNO        PIC X(15).
               03 TP-BAG-ISK1           PIC 9(4)V9999.
               03 TP-BAG-ISK2           PIC 9(4)V9999.
               03 TP-BAG-KDV            PIC 9(4)V9999.
               03 TP-BAG-ADET           PIC 9(12)V9999.
               03 TP-BAG-FIYAT          PIC 9(10)V9999.
       
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 9999.
           02 DUR                   PIC X.
           02 T-KOD                 PIC S9999.
           02 T-ADI                 PIC X(40).
           02 T-KDV                 PIC 9(4)V9999.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999.
           02 SON                   PIC 9999.
           02 XSON                  PIC 9999.
           02 YILK                  PIC 9999.
           02 S                     PIC 99.
           02 CUR                   PIC X.
           02 FIND                  PIC 9.
           02 FLAG                  PIC 9.
           02 CUR-LIS               PIC 99.
           02 CUR-SATIR             PIC 99.
           02 TEMP-SATIR            PIC 99.
           02 TEMP-LIS              PIC 99.
           02 TX-FIYAT              PIC 9(10)V99.
           02 TX-KDV                PIC 9(4)V99.
           02 ISVISIBLE             PIC 9 VALUE 0.
		   02 GT-YIL				PIC 9999.
           02 CUR-KEY.
               03 CUR-KOD            PIC X.
               03 CUR-TARIH.
                    04 CUR-YIL       PIC 9999.
                    04 CUR-AY        PIC 99.
                    04 CUR-GUN       PIC 99.
               03 CUR-SIRA           PIC 9(10).
           02 TEMP-KEY.
               03 TEMP-KOD            PIC X.
               03 TEMP-TARIH.
                    04 TEMP-YIL       PIC 9999.
                    04 TEMP-AY        PIC 99.
                    04 TEMP-GUN       PIC 99.
               03 TEMP-SIRA           PIC 9(10).
       01 BILGI.
           02 B-TARIH           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TIP             PIC X(3).
           02 FILLER            PIC X VALUE 'º'.
           02 B-EVRAKNO         PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-DEPONO          PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-HESAPNO         PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-HESAPADI        PIC X(20).
           02 FILLER            PIC X VALUE 'º'.
           02 B-YETKILI         PIC X(20).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ADET            PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-FIYAT           PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-KDV             PIC ZZ,ZZ.
           
       01 BILGI2-BASLIK0.
           02 FILLER            PIC X VALUE 'É'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(15) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(7) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ë'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE '»'.
       
       01 BILGI2-BASLIK1.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(5) VALUE ' T˜P'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(15) VALUE ' EVRAK NO'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(7) VALUE ' DEPO'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(10) VALUE ' ADET'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(10) VALUE ' F˜YAT'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(5) VALUE '%˜SK1'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(5) VALUE '%˜SK2'.
           02 FILLER            PIC X VALUE 'º'.
           02 FILLER            PIC X(5) VALUE '%KDV'.
           02 FILLER            PIC X VALUE 'º'.
           
       01 BILGI2-BASLIK2.
           02 FILLER            PIC X VALUE 'Ì'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(15) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(7) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Î'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE '¹'.
       
       01 BILGI2-BASLIK3.
           02 FILLER            PIC X VALUE 'È'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(15) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(7) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(10) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE 'Ê'.
           02 FILLER            PIC X(5) VALUE ALL 'Í'.
           02 FILLER            PIC X VALUE '¼'.
       
           
       01 BILGI2.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-TIP         PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-EVRAKNO     PIC X(15).
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-DEPONO      PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-ADET        PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-FIYAT       PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-ISK1        PIC ZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-ISK2        PIC ZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAG-KDV         PIC ZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           
       copy 'status.cpy'.    
       copy 'onayvar.cpy'.
       copy 'fileop.var'.
	   copy 'win.cpy'.
       LINKAGE SECTION.
       77 G-KATNO           PIC 9999.
       77 G-STNO            PIC X(15).
       77 G-YIL             PIC 9999.
       77 ISLEM             PIC 9.
       SCREEN SECTION.
       01 SCR-ADET.
           02 LINE 10 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'ÉÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍ»' HIGHLIGHT.
           02 LINE 11 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º KAT NO  [    ]                         º' HIGHLIGHT.
           02 LINE 12 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º STOK NO [                    ]         º' HIGHLIGHT.
           02 LINE 13 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º YIL     [    ]                         º' HIGHLIGHT.
           02 LINE 14 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º DEPO NO [    ]                         º' HIGHLIGHT.
           02 LINE 15 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º G˜R˜LEN [                  ]           º' HIGHLIGHT.
           02 LINE 16 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'º €IKILAN [                  ]           º' HIGHLIGHT.
           02 LINE 17 COLUMN 12 BACKGROUND BLUE FOREGROUND WHITE
              'ÈÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍ¸' HIGHLIGHT.
           02 LINE 15 COLUMN 23 BACKGROUND BLUE FOREGROUND BROWN
              PIC Z.ZZZ.ZZZ.ZZZ,ZZZZ USING STA-GIR HIGHLIGHT.
           02 LINE 16 COLUMN 23 BACKGROUND BLUE FOREGROUND BROWN
              PIC Z.ZZZ.ZZZ.ZZZ,ZZZZ USING STA-CIK HIGHLIGHT.
       01 SCR.
           02 LINE 2 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 3 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(114) FROM ' STOK HAREKETLER˜ ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 4 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 5 COLUMN 3 BACKGROUND BLACK FOREGROUND WHITE
             ' STOK NO  [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(15) USING ST-STNO HIGHLIGHT.
           02 FOREGROUND WHITE '] - PAR€A NO [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(15) USING ST-PRCNO HIGHLIGHT.
           02 FOREGROUND WHITE '] - MIN ADET [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZZ.ZZZ,ZZZZ USING ST-MIN HIGHLIGHT.
           02 FOREGROUND WHITE '] - MAX ADET [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZZ.ZZZ,ZZZZ USING ST-MAX HIGHLIGHT.
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 LINE 6 COLUMN 3 BACKGROUND BLACK FOREGROUND WHITE
             ' T˜P˜     [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(20) USING ST-TIPI HIGHLIGHT.
           02 FOREGROUND WHITE '] - MARKA [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(20) USING ST-MARKA HIGHLIGHT.
           02 FOREGROUND WHITE '] - NOT [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(10) USING ST-NOTE HIGHLIGHT.
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 LINE 7 COLUMN 3 BACKGROUND BLACK FOREGROUND WHITE
             ' STOK ADI [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(60) USING ST-CINSI HIGHLIGHT.
           02 FOREGROUND WHITE '] - F˜YAT [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC Z.ZZZ.ZZZ,ZZZZ USING ST-FIYAT
              HIGHLIGHT.
           02 FOREGROUND WHITE '] - YIL [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC 9999 USING GT-YIL HIGHLIGHT.
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 LINE 8 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(3) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 9 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(10) FROM ' TAR˜H' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(3) FROM 'T˜P' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' EVRAK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(20) FROM ' HESAP ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(20) FROM ' YETK˜L˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' ADET' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' F˜YAT' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM ' KDV' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 LINE 10 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(3) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(3) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(10) FROM ' TAR˜H' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(3) FROM 'T˜P' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' EVRAK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(20) FROM ' HESAP ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(20) FROM ' YETK˜L˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' ADET' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' F˜YAT' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM ' KDV' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(3) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 34 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(3) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 36 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 
           02 PIC X(114) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
           
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING G-KATNO G-STNO G-YIL ISLEM.
	   START-YIL-PROGRAM.
	       MOVE G-YIL TO GT-YIL.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
       BEGIN-PROGRAM.
           MOVE 'STOKLAR HAREKETLER˜ YšKLEN˜YOR ... (ESC - DURDUR)' 
            TO ONAY-MESSAGE.
           PERFORM SHOW-LOAD-MESSAGE.
           
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
           
           OPEN INPUT SAYIM DEPO.
           
           MOVE GT-YIL TO SY-YIL.
           MOVE G-KATNO TO SY-KATNO.
           MOVE G-STNO TO SY-STNO.
           MOVE ZEROS TO SY-SIRA.
           
           START SAYIM KEY IS NOT LESS THAN SY-KEY
                INVALID KEY GO SAYIM-SON.
       SAYIM-OKU.
           READ SAYIM NEXT AT END GO SAYIM-SON.
           PERFORM IS-INTERRUPT.
           IF INTERRUPT = 1 CLOSE SAYIM TEMPFILE 
                PERFORM DISPOSE-LOAD-MESSAGE
                DELETE FILE TEMPFILE MOVE 1 TO ISLEM GO STOP-PROGRAM.
           IF SY-YIL NOT = GT-YIL OR SY-KATNO NOT = G-KATNO
              OR SY-STNO NOT = G-STNO GO SAYIM-SON.
           PERFORM BAG-TEMIZLE.
           MOVE 1 TO TP-AY TP-GUN.
           MOVE SY-YIL TO TP-YIL.
           MOVE SY-DEPONO TO TP-BAG-DEPONO(1) DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE DP-ADI TO TP-BAG-DPADI(1).
           MOVE 'G' TO TP-KOD.
           MOVE 99 TO TP-BAG-TIP(1).
           MOVE SY-YIL TO TP-BAG-EVRAKNO(1).
           MOVE ALL SPACES TO TP-HESAPADI.
		   MOVE SY-MARKA TO TP-HESAPADI.
           MOVE 'Devir' TO TP-HESAPNO.
           MOVE SY-RAF TO TP-YETKILI.
           MOVE 0 TO TP-BAG-ISK1(1).
           MOVE 0 TO TP-BAG-ISK2(1).
           MOVE SY-KDV TO TP-BAG-KDV(1).
           MOVE SY-ADET TO TP-BAG-ADET(1).
           MOVE SY-FIYAT TO TP-BAG-FIYAT(1).
           IF DP-KDV NOT = SY-KDV AND SY-FIYAT NOT = ZEROS
            PERFORM BAG-HESAPLA THRU BAG-HESAPLA-SON.
           MOVE 1 TO TP-SIRA.
       TEMP-YAZ1.
           WRITE TP-KAYIT INVALID KEY ADD 1 TO TP-SIRA GO TEMP-YAZ1.
           GO SAYIM-OKU.
       BAG-TEMIZLE.
           PERFORM VARYING S FROM 1 BY 1 UNTIL S > 20
             INITIALIZE TP-BAG(S)
           END-PERFORM.
       BAG-HESAPLA.
           COMPUTE TX-FIYAT ROUNDED = 
               ((TP-BAG-FIYAT(1) * DP-KDV / 100) - 
               (TP-BAG-FIYAT(1) * (DP-KDV - TP-BAG-KDV(1)) / 100))
                    * 100 / DP-KDV.
           COMPUTE TP-BAG-FIYAT(2) = TP-BAG-FIYAT(1) - TX-FIYAT.
           MOVE TX-FIYAT TO TP-BAG-FIYAT(1).           
           MOVE DP-KDV TO TP-BAG-KDV(1).
           MOVE 2 TO TP-BAG-DEPONO(2) DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE 99 TO TP-BAG-TIP(2).
           MOVE DP-ADI TO TP-BAG-DPADI(2).
           MOVE TP-BAG-EVRAKNO(1) TO TP-BAG-EVRAKNO(2).
           MOVE ZEROS TO TP-BAG-ISK1(2) TP-BAG-ISK2(2)
           MOVE DP-KDV TO TP-BAG-KDV(2).
           MOVE TP-BAG-ADET(1) TO TP-BAG-ADET(2).
           
       BAG-HESAPLA-SON.    
       SAYIM-SON.
           CLOSE SAYIM.       
           
           OPEN INPUT HAREKET-DETAY HAREKET-SICIL.
           
           MOVE G-KATNO TO HD-KATNO.
           MOVE G-STNO TO HD-STNO.
           
           START HAREKET-DETAY KEY IS EQUAL TO HD-KATKEY
                INVALID KEY GO STOK-SON.
       STOK-OKU.
           READ HAREKET-DETAY NEXT AT END GO STOK-SON.
           PERFORM IS-INTERRUPT.
           IF INTERRUPT = 1 GO STOK-SON.
           IF HD-STNO NOT = G-STNO OR HD-KATNO NOT = G-KATNO
                GO STOK-SON.
           
           MOVE HD-EVRAKNO TO HS-EVRAKNO.
           MOVE HD-TIP TO HS-TIP.
           READ HAREKET-SICIL.
           IF HS-DYIL NOT = GT-YIL GO STOK-OKU.
           EVALUATE HS-TIP
            WHEN 1 WHEN 2 WHEN 7 WHEN 8 MOVE 'C' TO TP-KOD
            WHEN 4 WHEN 5 WHEN 10 WHEN 11 MOVE 'G' TO TP-KOD
            WHEN OTHER GO STOK-OKU
           END-EVALUATE.
      * 01 - ˜RSAL˜YEL˜ SATIž FATURASI
      * 02 - SATIž ˜RSAL˜YES˜
      * 03 - SATIž FATURASI     
      * 
      * 04 - ˜RSAL˜YEL˜ ALIž FATURASI
      * 05 - ALIž ˜RSAL˜YES˜
      * 06 - ALIž FATURASI
      * 
      * 07 - ˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI
      * 08 - ˜ADE €IKIž ˜RSAL˜YES˜
      * 09 - ˜ADE €IKIž FATURASI
      *
      * 10 - ˜RSAL˜YEL˜ ˜ADE ALIž FATURASI
      * 11 - ˜ADE ALIž ˜RSAL˜YES˜
      * 12 - ˜ADE ALIž FATURASI
      *
      * 13 - SATIž FARK FATURASI
      * 14 - ALIž FARK FATURASI    
           PERFORM BAG-TEMIZLE.
           MOVE HS-TARIH TO TP-TARIH.
           MOVE HS-HESAPNO TO TP-HESAPNO.
           MOVE HS-HESAPADI TO TP-HESAPADI.
           MOVE HS-YETKILI TO TP-YETKILI.
           MOVE HD-SIRA TO TP-SIRA.
           
           MOVE 1 TO S.
           PERFORM BAG-EKLE.
           
           MOVE HS-BAGTIPI(1) TO HS-TIP.
           MOVE HS-BEVRAKNO(1) TO HS-EVRAKNO.
           READ HAREKET-SICIL INVALID KEY CONTINUE
                  NOT INVALID KEY
                  PERFORM FIND-HAREKET-STOK THRU FIND-HAREKET-STOK-SON
                  IF FIND = 1
                    PERFORM BAG-EKLE
                  END-IF
           END-READ.
           
           MOVE HS-BAGTIPI(2) TO HS-TIP.
           MOVE HS-BEVRAKNO(2) TO HS-EVRAKNO.
           READ HAREKET-SICIL INVALID KEY CONTINUE
                  NOT INVALID KEY
                  PERFORM FIND-HAREKET-STOK THRU FIND-HAREKET-STOK-SON
                  IF FIND = 1
                    PERFORM BAG-EKLE
                  END-IF
           END-READ.
           
           MOVE G-KATNO TO HD-KATNO.
           MOVE G-STNO TO HD-STNO.
           START HAREKET-DETAY KEY IS EQUAL TO HD-KATKEY.
           READ HAREKET-DETAY NEXT.
           PERFORM UNTIL (HD-STNO = G-STNO AND HD-KATNO = G-KATNO 
              AND HD-SIRA = TP-SIRA AND HD-EVRAKNO = TP-BAG-EVRAKNO(1)
              AND HD-TIP = TP-BAG-TIP(1))
              READ HAREKET-DETAY NEXT AT END DISPLAY 'ERROR' END-READ
              PERFORM IS-INTERRUPT
              IF INTERRUPT = 1 GO STOK-SON END-IF
           END-PERFORM.
           MOVE 1 TO TP-SIRA.
       TEMP-YAZ2.
           WRITE TP-KAYIT INVALID KEY ADD 1 TO TP-SIRA GO TEMP-YAZ2.
           GO STOK-OKU.
       BAG-EKLE.
           MOVE HS-DEPONO TO TP-BAG-DEPONO(S) DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE DP-ADI TO TP-BAG-DPADI(S).
           MOVE HS-TIP TO TP-BAG-TIP(S).
           MOVE HS-EVRAKNO TO TP-BAG-EVRAKNO(S).
           MOVE HD-ISKONTO1 TO TP-BAG-ISK1(S).
           MOVE HD-ISKONTO2 TO TP-BAG-ISK2(S).
           MOVE HD-KDV TO TP-BAG-KDV(S).
           MOVE HD-ADET TO TP-BAG-ADET(S).
           MOVE HD-FIYAT TO TP-BAG-FIYAT(S).
           ADD 1 TO S.
       
       FIND-HAREKET-STOK.
           MOVE 0 TO FIND.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE ZEROS TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
            INVALID KEY GO FIND-HAREKET-STOK-SON.
       FIND-HAREKET-STOK-OKU.
           READ HAREKET-DETAY NEXT AT END 
                GO FIND-HAREKET-STOK-SON.
           PERFORM IS-INTERRUPT.
           IF INTERRUPT = 1 GO STOK-SON.
           IF HS-TIP NOT = HD-TIP OR HS-EVRAKNO NOT = HD-EVRAKNO
                GO FIND-HAREKET-STOK-SON.
           IF G-KATNO NOT = HD-KATNO OR G-STNO NOT = HD-STNO
                GO FIND-HAREKET-STOK-OKU.
           MOVE 1 TO FIND.
       FIND-HAREKET-STOK-SON.
            
       STOK-SON.
           CLOSE HAREKET-DETAY HAREKET-SICIL TEMPFILE DEPO.
           PERFORM DISPOSE-LOAD-MESSAGE.
           IF INTERRUPT = 1 DELETE FILE TEMPFILE MOVE 1 TO ISLEM
                GO STOP-PROGRAM.
           OPEN INPUT STOK.
           MOVE G-KATNO TO ST-KATNO.
           MOVE G-STNO TO ST-STNO.
           READ STOK INVALID KEY INITIALIZE ST-KAYIT.
           CLOSE STOK.
           PERFORM VARYING SATIR FROM 2 BY 1 UNTIL SATIR > 35
            DISPLAY 'º' LINE SATIR POSITION 2
                    CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                    SPACES SIZE 114 'º'
            IF SATIR >= 11 AND SATIR <= 33
              PERFORM BOS-SATIR
            END-IF
           END-PERFORM.
           
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
           CALL 'GOLGE' USING BY CONTENT 0002 0002 0036 0117.
           CANCEL 'GOLGE'.
           OPEN INPUT TEMPFILE.
           INITIALIZE CUR-KEY.
           PERFORM SON-AL.
      *     IF FIND = 0 CLOSE TEMPFILE DELETE FILE TEMPFILE
      *          MOVE 1 TO ISLEM GO STOP-PROGRAM.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            BEFORE TIME 150 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 99 AND ISVISIBLE = 1 GO TUS.
           
           IF ISVISIBLE = 0 AND F = 99
              MOVE WCB TO NESTED-WCB (5)
              DISPLAY NESTED-WCB (5)
                LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'
              PERFORM DETAY-GOSTER
              MOVE 1 TO ISVISIBLE GO TUS.
           
           IF ISVISIBLE = 1
              MOVE WCB TO NESTED-WCB (5)
              DISPLAY NESTED-WCB (5)
                LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
              MOVE 0 TO ISVISIBLE.
 
           IF F = 69 CLOSE TEMPFILE DELETE FILE TEMPFILE 
                SUBTRACT 1 FROM GT-YIL GO BEGIN-PROGRAM.
           IF F = 70 CLOSE TEMPFILE DELETE FILE TEMPFILE 
                ADD 1 TO GT-YIL GO BEGIN-PROGRAM.
           IF F = 27 MOVE 0 TO ISLEM CLOSE TEMPFILE DELETE FILE TEMPFILE 
                GO STOP-PROGRAM.
           IF F = 13 MOVE 1 TO ISLEM CLOSE TEMPFILE DELETE FILE TEMPFILE
                GO STOP-PROGRAM.
           IF F = 5 GO ADET-AL.
		   IF FIND = 0 GO TUS.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 58 GO TAB-DEGIS.
           GO TUS.
       ADET-AL.
           OPEN INPUT STOKADET.
           MOVE GT-YIL TO STA-YIL.
           MOVE G-KATNO TO STA-KATNO.
           MOVE G-STNO TO STA-STNO.
           MOVE WCB TO NESTED-WCB (6).
           DISPLAY NESTED-WCB (6)
             LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SCR-ADET.
           DISPLAY STA-KATNO LINE 11 POSITION 23
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                   STA-STNO LINE 12 POSITION 23
                   STA-YIL LINE 13 POSITION 23.
       DEPONO-AL.
           ACCEPT STA-DEPONO LINE 14 POSITION 23 NO BEEP
             CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE STOKADET GO ADET-AL-CIKIS.
           READ STOKADET INVALID KEY GO DEPONO-AL.
           DISPLAY SCR-ADET.
           DISPLAY STA-KATNO LINE 11 POSITION 23
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                   STA-STNO LINE 12 POSITION 23
                   STA-YIL LINE 13 POSITION 23
                   STA-DEPONO LINE 14 POSITION 23.
       ADETLER-AL.
           ACCEPT SCR-ADET.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO DEPONO-AL.
           CLOSE STOKADET.
           OPEN I-O STOKADET.
           PERFORM REWRITE-STOKADET.
           CLOSE STOKADET.
       ADET-AL-CIKIS.          
           MOVE WCB TO NESTED-WCB (6).
           DISPLAY NESTED-WCB (6)
             LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO TUS.
              
       DETAY-GOSTER.
           IF SATIR >= 30 COMPUTE F = SATIR - 7
             ELSE COMPUTE F = SATIR + 1.
           
           
           DISPLAY BILGI2-BASLIK0 LINE F POSITION 46
              CONTROL 'BCOLOR=WHITE, FCOLOR=RED' LOW.
           ADD 1 TO F. 
           DISPLAY BILGI2-BASLIK1 LINE F POSITION 46
              CONTROL 'BCOLOR=WHITE, FCOLOR=RED' LOW.
           ADD 1 TO F. 
           DISPLAY BILGI2-BASLIK2 LINE F POSITION 46
              CONTROL 'BCOLOR=WHITE, FCOLOR=RED' LOW.
           
           MOVE 1 TO S.
           PERFORM UNTIL TP-BAG-TIP(S) = ZEROS
            EVALUATE TP-BAG-TIP(S)
                WHEN 1 WHEN 4 WHEN 7 WHEN 10
                    MOVE '˜ft.' TO B-BAG-TIP
                WHEN 2 WHEN 5 WHEN 8 WHEN 11
                    MOVE '˜rs.' TO B-BAG-TIP
                WHEN 3 WHEN 6 WHEN 9 WHEN 12 WHEN 13 WHEN 14        
                    MOVE 'Fat.' TO B-BAG-TIP
                WHEN 99
                    MOVE 'Dev.' TO B-BAG-TIP
                WHEN OTHER
                    MOVE TP-BAG-TIP(S) TO B-BAG-TIP
            END-EVALUATE
            MOVE TP-BAG-EVRAKNO(S) TO B-BAG-EVRAKNO
            MOVE TP-BAG-DPADI(S) TO B-BAG-DEPONO
            MOVE TP-BAG-ADET(S) TO B-BAG-ADET
            MOVE TP-BAG-FIYAT(S) TO B-BAG-FIYAT
            MOVE TP-BAG-ISK1(S) TO B-BAG-ISK1
            MOVE TP-BAG-ISK2(S) TO B-BAG-ISK2
            MOVE TP-BAG-KDV(S) TO B-BAG-KDV
            ADD 1 TO S
            ADD 1 TO F
            DISPLAY BILGI2 LINE F POSITION 46
              CONTROL 'BCOLOR=WHITE, FCOLOR=RED' LOW
           END-PERFORM.
           ADD 1 TO F. 
           DISPLAY BILGI2-BASLIK3 LINE F POSITION 46
              CONTROL 'BCOLOR=WHITE, FCOLOR=RED' LOW.
           
           COMPUTE XSON = (LENGTH OF BILGI2) + 45.
           COMPUTE YILK = SATIR - 7.
           CALL 'GOLGE' USING BY CONTENT YILK 0046 F XSON.
           CANCEL 'GOLGE'.
                      
       copy 'onayper.cpy'.
       TAB-DEGIS.
           IF CUR-KOD = ' ' GO TUS.
           PERFORM SERITSIZ.
           MOVE TP-KEY TO TEMP-KEY.
           MOVE SATIR TO TEMP-SATIR.
           MOVE LIS TO TEMP-LIS.
           IF CUR = 'G' 
              MOVE 'C' TO CUR 
              MOVE 24 TO ILK MOVE 33 TO SON
           ELSE 
              MOVE 'G' TO CUR 
              MOVE 11 TO ILK MOVE 20 TO SON
           END-IF.
           
           MOVE CUR-LIS TO LIS.
           MOVE CUR-KEY TO TP-KEY.
           MOVE CUR-SATIR TO SATIR.
           READ TEMPFILE.
           PERFORM AKTAR.
           PERFORM SERITLI.
           
           MOVE TEMP-LIS TO CUR-LIS.
           MOVE TEMP-KEY TO CUR-KEY.
           MOVE TEMP-SATIR TO CUR-SATIR.
           GO TUS.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK AND FLAG = 1 
                PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ TEMPFILE NEXT AT END 
                    READ TEMPFILE PREVIOUS GO A-LISTE-SON.
           IF TP-KOD NOT = CUR READ TEMPFILE PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING SATIR FROM SATIR BY 1 UNTIL SATIR > SON
                 PERFORM BOS-SATIR
             END-PERFORM MOVE LIS TO SATIR.
       SON-AL.
           MOVE 0 TO FLAG.
           INITIALIZE CUR-KEY.
           PERFORM GSON-AL THRU GSON-AL-SON.
           PERFORM CSON-AL THRU CSON-AL-SON.
           MOVE 1 TO FLAG.
           IF FIND = 1 PERFORM SERITLI.
       GSON-AL.    
           MOVE 0 TO FIND.
           INITIALIZE TP-KEY.
           MOVE 'G' TO TP-KOD.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
               INVALID KEY GO GSON-AL-SON.
           READ TEMPFILE NEXT.
           IF TP-KOD NOT = 'G' GO GSON-AL-SON.
           MOVE 11 TO ILK SATIR. MOVE 20 TO SON.
           MOVE 'G' TO CUR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           PERFORM SON-SATIR-AL THRU SON-SATIR-AL-SON.
           MOVE LIS TO CUR-LIS.
           MOVE TP-KEY TO CUR-KEY.
           MOVE SATIR TO CUR-SATIR.
           MOVE 1 TO FIND.
       GSON-AL-SON.
       CSON-AL.
           INITIALIZE TP-KEY.
           MOVE 'C' TO TP-KOD.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
                INVALID KEY MOVE CUR-KEY TO TP-KEY
                 READ TEMPFILE INVALID KEY CONTINUE
                    NOT INVALID KEY PERFORM AKTAR
                END-READ
                GO CSON-AL-SON.
           READ TEMPFILE NEXT.
           IF TP-KOD NOT = 'C' MOVE CUR-KEY TO TP-KEY
                READ TEMPFILE INVALID KEY CONTINUE
                    NOT INVALID KEY PERFORM AKTAR
                END-READ
                GO CSON-AL-SON.
           MOVE 24 TO ILK SATIR. MOVE 33 TO SON.
           MOVE 'C' TO CUR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           PERFORM SON-SATIR-AL THRU SON-SATIR-AL-SON.
           MOVE 1 TO FIND.
       CSON-AL-SON.
       SON-SATIR-AL.
           IF LIS NOT = SON
             PERFORM SERITSIZ
             COMPUTE SATIR = SATIR - LIS
              PERFORM SATIR TIMES
                 READ TEMPFILE NEXT 
                 END-READ 
              END-PERFORM
             MOVE LIS TO SATIR PERFORM AKTAR
             GO SON-SATIR-AL-SON
           END-IF.
           MOVE CUR TO TP-KOD.
           MOVE 9999999 TO TP-TARIH.
           MOVE 9999999999 TO TP-SIRA.
           START TEMPFILE KEY IS NOT GREATER THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ TEMPFILE NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
       SON-SATIR-AL-SON.
       CTHO-TUSU.
           MOVE CUR TO TP-KOD.
           MOVE ZEROS TO TP-TARIH TP-SIRA.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE F = LIS - ILK.
           PERFORM F TIMES
              READ TEMPFILE PREVIOUS
           END-PERFORM.
           PERFORM AKTAR.
           MOVE ILK TO SATIR.
           GO TUS.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE CUR TO TP-KOD.
           MOVE 9999999 TO TP-TARIH.
           MOVE 9999999999 TO TP-SIRA.
           START TEMPFILE KEY IS NOT GREATER THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ TEMPFILE NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
       PGUP-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ TEMPFILE PREVIOUS
             END-PERFORM.
           READ TEMPFILE PREVIOUS AT END
              READ TEMPFILE NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           IF TP-KOD NOT = CUR 
              READ TEMPFILE NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ TEMPFILE PREVIOUS AT END 
                    READ TEMPFILE NEXT 
                    GO PG-OKU-SON.
           IF TP-KOD NOT = CUR READ TEMPFILE NEXT
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ TEMPFILE PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ TEMPFILE NEXT
           END-PERFORM.
           READ TEMPFILE NEXT AT END
              READ TEMPFILE PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           IF TP-KOD NOT = CUR 
              READ TEMPFILE PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ TEMPFILE NEXT AT END 
             READ TEMPFILE PREVIOUS MOVE ILK TO SATIR.
           IF TP-KOD NOT = CUR
             READ TEMPFILE PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ TEMPFILE NEXT 
                 END-READ 
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ TEMPFILE PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           READ TEMPFILE PREVIOUS AT END READ TEMPFILE NEXT GO TUS.
           IF TP-KOD NOT = CUR READ TEMPFILE NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ TEMPFILE PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           READ TEMPFILE NEXT AT END READ TEMPFILE PREVIOUS GO TUS.
           IF TP-KOD NOT = CUR READ TEMPFILE PREVIOUS GO TUS.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ TEMPFILE NEXT
               END-PERFORM
                 MOVE SON TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = SON PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = ILK GO Y-LISTE-SON.
           READ TEMPFILE PREVIOUS AT END READ TEMPFILE NEXT 
                GO Y-LISTE-SON.
           IF TP-KOD NOT = CUR READ TEMPFILE NEXT 
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       BOS-SATIR.
           DISPLAY SPACES LINE SATIR POSITION 3 
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' SIZE 10
            'º' SPACES SIZE 3 'º' SPACES SIZE 10 
            'º' SPACES SIZE 7 'º' SPACES SIZE 10 
            'º' SPACES SIZE 20 'º' SPACES SIZE 20 
            'º' SPACES SIZE 10 'º' SPACES SIZE 10 
            'º' SPACES SIZE 5.
                
       AKTAR.
           STRING TP-GUN '/' TP-AY '/' TP-YIL
                DELIMITED BY SIZE INTO B-TARIH.
           MOVE TP-HESAPNO TO B-HESAPNO.
           MOVE TP-HESAPADI TO B-HESAPADI.
           MOVE TP-YETKILI TO B-YETKILI.
           MOVE TP-BAG-ADET(1) TO B-ADET.
           MOVE TP-BAG-FIYAT(1) TO TX-FIYAT.
           MOVE TP-BAG-DPADI(1) TO B-DEPONO.
           MOVE TP-BAG-EVRAKNO(1) TO B-EVRAKNO.
           MOVE TP-BAG-KDV(1) TO TX-KDV.
           COMPUTE TX-FIYAT ROUNDED = 
               (TP-BAG-FIYAT(1) 
               * (1 - TP-BAG-ISK1(1) / 100) 
               * (1 - TP-BAG-ISK2(1) / 100)).
           EVALUATE TP-BAG-TIP(1)
           WHEN 1 WHEN 4 WHEN 7 WHEN 10
                MOVE '˜ft.' TO B-TIP
           WHEN 2 WHEN 5 WHEN 8 WHEN 11
                MOVE '˜rs.' TO B-TIP
           WHEN 3 WHEN 6 WHEN 9 WHEN 12 WHEN 13 WHEN 14     
                MOVE 'Fat.' TO B-TIP
           WHEN 99
                MOVE 'Dev.' TO B-TIP
           WHEN OTHER
                MOVE TP-BAG-TIP(1) TO B-TIP
           END-EVALUATE.
      
      * 01 - ˜RSAL˜YEL˜ SATIž FATURASI
      * 02 - SATIž ˜RSAL˜YES˜
      * 03 - SATIž FATURASI     
      * 
      * 04 - ˜RSAL˜YEL˜ ALIž FATURASI
      * 05 - ALIž ˜RSAL˜YES˜
      * 06 - ALIž FATURASI
      * 
      * 07 - ˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI
      * 08 - ˜ADE €IKIž ˜RSAL˜YES˜
      * 09 - ˜ADE €IKIž FATURASI
      *
      * 10 - ˜RSAL˜YEL˜ ˜ADE ALIž FATURASI
      * 11 - ˜ADE ALIž ˜RSAL˜YES˜
      * 12 - ˜ADE ALIž FATURASI
      *
      * 13 - SATIž FARK FATURASI
      * 14 - ALIž FARK FATURASI      
           MOVE 2 TO S.
           PERFORM UNTIL TP-BAG-TIP(S) = ZEROS
             EVALUATE TP-BAG-TIP(S - 1) ALSO TP-BAG-TIP(S)
             WHEN 2 ALSO 3
             WHEN 5 ALSO 6
             WHEN 8 ALSO 9
             WHEN 12 ALSO 11
               MOVE TP-BAG-KDV(S) TO TX-KDV
               MOVE TP-BAG-EVRAKNO(S) TO B-EVRAKNO
               MOVE 'Fat.' TO B-TIP
               MOVE TP-BAG-DPADI(S) TO B-DEPONO
               COMPUTE TX-FIYAT ROUNDED = 
               (TP-BAG-FIYAT(S) 
               * (1 - TP-BAG-ISK1(S) / 100) 
               * (1 - TP-BAG-ISK2(S) / 100))
             WHEN OTHER
               MOVE '(€oklu)' TO B-EVRAKNO B-DEPONO
               MOVE '€ok' TO B-TIP
               MOVE TP-BAG-KDV(S) TO TX-KDV
               MOVE TP-BAG-FIYAT(S) TO TX-FIYAT
               COMPUTE TX-FIYAT ROUNDED = 
                    (TP-BAG-FIYAT(S - 1) 
                    * (1 - TP-BAG-ISK1(S - 1) / 100) 
                    * (1 - TP-BAG-ISK2(S - 1) / 100))
                    + (TP-BAG-FIYAT(S) 
                    * (1 - TP-BAG-ISK1(S) / 100) 
                    * (1 - TP-BAG-ISK2(S) / 100))
               COMPUTE TX-KDV ROUNDED 
                     = TP-BAG-FIYAT(S - 1) * TP-BAG-KDV(S - 1) 
                       / TX-FIYAT
              END-EVALUATE
              ADD 1 TO S
           END-PERFORM.
           MOVE TX-FIYAT TO B-FIYAT.
           MOVE TX-KDV TO B-KDV.
           
        
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 3
                CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 3
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
          
           