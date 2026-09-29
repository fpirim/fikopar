       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KASA'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           CLASS DIGIT IS "0123456789"
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'kasa.cpy'.
       copy 'makbuz.cpy'.
       copy 'carhar.cpy'.
       copy 'sicil.cpy'.
       copy 'depo.cpy'.
       copy 'ayar.cpy'.
       copy 'odeme.cpy'.
       copy 'ceksenet.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'kasa.cpz'.
       copy 'makbuz.cpz'.
       copy 'carhar.cpz'.
       copy 'sicil.cpz'.
       copy 'depo.cpz'.
       copy 'ayar.cpz'.
       copy 'odeme.cpz'.
       copy 'ceksenet.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 SATIR                 PIC 9999.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 6.
           02 SON                   PIC 9999 VALUE 31.
           02 YSON                  PIC 9999.
           02 E-HESAPNO             PIC X(15).
           02 E-DEPONO              PIC 9999.
           02 E-SEKILNO             PIC 9999.
           02 E-ACIKLAMA            PIC X(60).
           02 E-GELIR               PIC 9(13)V99.
           02 E-GIDER               PIC 9(13)V99.
           
           02 TX-GELIR               PIC 9(13)V99.
           02 TX-GIDER               PIC 9(13)V99.
           02 D-GELIR               PIC 9(16)V99.
           02 D-GIDER               PIC 9(16)V99.
           02 T-GELIR               PIC 9(16)V99.
           02 T-GIDER               PIC 9(16)V99.
           02 D-BAKIYE              PIC S9(13)V99.
           02 T-BAKIYE              PIC S9(13)V99.
           02 S-BAKIYE              PIC S9(13)V99.
           02 T-SIRA                PIC 9(5).
           02 Z-BAKIYE              PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           
           02 Z-TUTAR             PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 Z-GELIR             PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 Z-GIDER             PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
           02 T-ERROR           PIC 9.
           02 T-TARIH.
               03 T-YIL               PIC 9999.
               03 T-AY                PIC 99.
               03 T-GUN               PIC 99.
           02 B-BAKIYE          PIC ---.---.---,--.
           02 DP-BAKIYE         PIC S9(12)V99 OCCURS 2 TIMES.
           02 DPI               PIC 9.

       01 BILGI-EK.
           02 BEK-DEPONO            PIC 9999.
           02 BEK-SEKILNO           PIC 9999.
       01 BILGI.
           02 B-HESAPNO               PIC X(10).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-DEPONO                PIC X(7).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-SEKILNO               PIC X(7).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-HESAPADI              PIC X(24).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ACIKLAMA              PIC X(32).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-GELIR                 PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-GIDER                 PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
       
       copy 'trim.var'.
       copy 'numara.var'.
       copy 'chyaz.var'.
       copy 'status.cpy'.    
       copy 'onayvar.cpy'.
       copy 'win.cpy'.
       LINKAGE SECTION.
	   01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
	   SCREEN SECTION.
       01 SCR.
           02 LINE 1 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 2 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(92) FROM 'KASA DEFTER˜' 
               HIGHLIGHT.
           02 'Kasa Tarihi  [  /  /    ] º' HIGHLIGHT. 
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(24) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(32) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 4 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' žEK˜L' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 PIC X(24) FROM ' HESAP ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 PIC X(32) FROM ' A€IKLAMA' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(16) FROM ' GEL˜R' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(16) FROM ' G˜DER' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 5 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(24) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(32) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           
           
           02 LINE 32 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(24) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(32) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 33 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(60) FROM ALL ' ' HIGHLIGHT.
           02 PIC X(58) FROM 'DEVREDEN' HIGHLIGHT. 02 'º' HIGHLIGHT. 
           02 LINE 34 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(60) FROM ALL ' ' HIGHLIGHT.
           02 PIC X(58) FROM 'KASA TOPLAM' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 LINE 35 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(60) FROM ALL ' ' HIGHLIGHT.
           02 PIC X(58) FROM 'KASA BAK˜YE' HIGHLIGHT. 02 'º' HIGHLIGHT. 
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
           02 SCR-TOPLAM.
                03 LINE 33 COLUMN 87 BACKGROUND WHITE
                    PIC X(16) FROM ALL ' '.
                03 BACKGROUND BLUE ' '.
                03 BACKGROUND WHITE PIC X(16) FROM ALL ' '.
                03 LINE 34 COLUMN 87 BACKGROUND WHITE
                    PIC X(16) FROM ALL ' '.
                03 BACKGROUND BLUE ' '.
                03 BACKGROUND WHITE PIC X(16) FROM ALL ' '.
                03 LINE 35 COLUMN 87 BACKGROUND WHITE
                    PIC X(16) FROM ALL ' '.
                03 BACKGROUND BLUE ' '.
                03 BACKGROUND WHITE PIC X(16) FROM ALL ' '.
           
       01 SCR-TARIH-AL.
           02 LINE 2 COLUMN 108 BACKGROUND BLUE FOREGROUND BROWN
                PIC ZZ USING T-GUN HIGHLIGHT AUTO.
           02 LINE 2 COLUMN 111 BACKGROUND BLUE FOREGROUND BROWN
                PIC ZZ USING T-AY HIGHLIGHT AUTO.
           02 LINE 2 COLUMN 114 BACKGROUND BLUE FOREGROUND BROWN
                PIC ZZZZ USING T-YIL HIGHLIGHT AUTO.
       copy 'onayscr.cpy'.         
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           OPEN INPUT KASA SICIL DEPO MAKBUZ ODEME.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
       BEGIN-PROGRAM.
           PERFORM VARYING SATIR FROM ILK BY 1 UNTIL SATIR > SON
              PERFORM YENI-SERITSIZ
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F1 ' LINE 37 POSITION 1 'S˜C˜LLER' REVERSE LOW
               ' F2 ' 'SATIR S˜L' REVERSE LOW.
               
           ACCEPT T-TARIH FROM CENTURY-DATE.
           
       TARIH-AL.
           DISPLAY SCR-TARIH-AL.
           ACCEPT SCR-TARIH-AL.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE KASA SICIL DEPO MAKBUZ ODEME GO STOP-PROGRAM.
       DETAY-AL.  
           
           
           PERFORM ALT-TOPLAM.
           MOVE ILK TO SATIR.
           MOVE T-TARIH TO KS-TARIH.
           MOVE 1 TO KS-SIRA.
           START KASA KEY IS NOT LESS THAN KS-KEY
             INVALID KEY GO A-LISTE-SON.
           READ KASA NEXT.
           IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS GO A-LISTE-SON.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ KASA NEXT AT END 
					READ KASA PREVIOUS GO A-LISTE-SON.
		   IF T-TARIH NOT = KS-TARIH OR KS-SIRA = ZEROS
		            READ KASA PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING SATIR FROM SATIR BY 1 UNTIL SATIR > SON
                  PERFORM YENI-SERITSIZ
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE T-TARIH TO KS-TARIH.
           MOVE 1 TO KS-SIRA.
           START KASA KEY IS NOT LESS THAN KS-KEY
             INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ KASA NEXT.
           IF T-TARIH NOT = KS-TARIH OR KS-SIRA = ZEROS
            PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-HESAPNO E-ACIKLAMA
                    MOVE 2 TO E-DEPONO
                    MOVE ZEROS TO E-GELIR E-GIDER
               ELSE MOVE KS-HESAPNO TO E-HESAPNO 
                    MOVE BEK-DEPONO TO E-DEPONO
                    MOVE BEK-SEKILNO TO E-SEKILNO
                    MOVE KS-ACIKLAMA TO E-ACIKLAMA
                    MOVE KS-GELIR TO E-GELIR
                    MOVE KS-GIDER TO E-GIDER
           END-IF.
       E-HESAPNO-AL.
           DISPLAY SPACES LINE 34 POSITION 2 
            CONTROL 'BCOLOR=BLUE' SIZE 30
            SPACES LINE 35 POSITION 2 SIZE 30.
           ACCEPT E-HESAPNO LINE SATIR POSITION 2 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 1 CALL 'SICPEN' USING E-HESAPNO 
                    CANCEL 'SICPEN' GO E-HESAPNO-AL.
           IF F = 27 GO BEGIN-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO YENI-KASA.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 2 AND YENI = 0 GO KASA-SIL.
           GO TUS.
       copy 'trim.per'.
       copy 'numara.per'.
       copy 'chyaz.per'.
       copy 'onayper.cpy'.
       
       ALT-TOPLAM.
           MOVE KS-SIRA TO T-SIRA.
           
           MOVE ZEROS TO D-GELIR D-GIDER T-GELIR T-GIDER.
           PERFORM DEVIR-BAKIYE-AL THRU DEVIR-BAKIYE-AL-SON.
           PERFORM BAKIYE-AL THRU BAKIYE-AL-SON.
           
           MOVE T-TARIH TO KS-TARIH.
           MOVE T-SIRA TO KS-SIRA.
           IF KS-SIRA NOT = ZEROS
                READ KASA INVALID KEY CONTINUE
                  NOT INVALID KEY PERFORM AKTAR
                END-READ
           END-IF.
           DISPLAY SCR-TOPLAM.
           COMPUTE D-BAKIYE = D-GELIR - D-GIDER.
           COMPUTE T-BAKIYE = T-GELIR - T-GIDER.
           IF D-BAKIYE > 0 
                MOVE D-BAKIYE TO Z-BAKIYE
                DISPLAY Z-BAKIYE LINE 33 POSITION 87
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW
           ELSE
                MOVE D-BAKIYE TO Z-BAKIYE
                DISPLAY Z-BAKIYE LINE 33 POSITION 104
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW
           END-IF.
           
           MOVE T-GELIR TO Z-BAKIYE.
           DISPLAY Z-BAKIYE LINE 34 POSITION 87
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           
           MOVE T-GIDER TO Z-BAKIYE.
           DISPLAY Z-BAKIYE LINE 34 POSITION 104
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           
           COMPUTE S-BAKIYE = D-BAKIYE + T-BAKIYE.
           IF S-BAKIYE > 0 
                MOVE S-BAKIYE TO Z-BAKIYE
                DISPLAY Z-BAKIYE LINE 35 POSITION 87
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW
           ELSE
                MOVE S-BAKIYE TO Z-BAKIYE
                DISPLAY Z-BAKIYE LINE 35 POSITION 104
                  CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW
           END-IF.
           
       BAKIYE-AL.
           MOVE T-TARIH TO KS-TARIH.
           MOVE ZEROS TO KS-SIRA.
           READ KASA INVALID KEY MOVE ZEROS TO KS-GELIR KS-GIDER.
           MOVE KS-GELIR TO T-GELIR.
           MOVE KS-GIDER TO T-GIDER.
       BAKIYE-AL-SON.       
       DEVIR-BAKIYE-AL.
           MOVE ZEROS TO KS-TARIH.
           MOVE ZEROS TO KS-SIRA D-GELIR D-GIDER.
       DEVIR-BAKIYE-OKU.    
           START KASA KEY IS GREATER THAN KS-TARIH
              INVALID KEY GO DEVIR-BAKIYE-OKU-SON.
           READ KASA NEXT.
           IF KS-TARIH >= T-TARIH GO DEVIR-BAKIYE-OKU-SON.
           MOVE ZEROS TO KS-SIRA.
           READ KASA.
           ADD KS-GELIR TO D-GELIR.
           ADD KS-GIDER TO D-GIDER.
           GO DEVIR-BAKIYE-OKU.
       DEVIR-BAKIYE-OKU-SON.
       DEVIR-BAKIYE-AL-SON.
              
       
       YENI-KASA.
           PERFORM YENI-GOSTER.
           IF E-HESAPNO = SPACES MOVE ZEROS TO E-DEPONO
                GO ACIKLAMA-AL.
           MOVE E-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY GO E-HESAPNO-AL.
           IF SC-PLKOD = 5 THEN
      	    MOVE 'HESAP AKT˜F DE¦˜L !!!' TO
                ONAY-MESSAGE
            PERFORM WARN-MESSAGE
      		GO E-HESAPNO-AL.
           
           PERFORM CBAKIYE-OKU.
           MOVE DP-BAKIYE(1) TO B-BAKIYE.
           DISPLAY '1.HESAP :' LINE 34 POSITION 2
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
            B-BAKIYE CONTROL 'FCOLOR=WHITE'.
           MOVE DP-BAKIYE(2) TO B-BAKIYE.
           DISPLAY '2.HESAP :' LINE 35 POSITION 2
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
            B-BAKIYE CONTROL 'FCOLOR=WHITE'.
            
       DEPONO-AL.
           MOVE E-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           COMPUTE YSON = SATIR + 5.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0012 SATIR 0020 YSON 0.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO TUS.
           MOVE T-KOD TO E-DEPONO.
           PERFORM YENI-GOSTER.
       SEKILNO-AL.
           MOVE E-SEKILNO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           COMPUTE YSON = SATIR + 5.
           CALL 'SEKSEC' USING 0 T-KOD T-ADI
                  BY CONTENT 0020 SATIR 0028 YSON 0.
           CANCEL 'SEKSEC'.
           IF T-KOD = -1 GO DEPONO-AL.
           MOVE T-KOD TO E-SEKILNO.
           PERFORM YENI-GOSTER.
       ACIKLAMA-AL.
           ACCEPT E-ACIKLAMA LINE SATIR POSITION 54 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM YENI-GOSTER.
           IF (F = 27 OR F = 52) AND E-HESAPNO = SPACES GO TUS.
           IF F = 27 OR F = 52 GO SEKILNO-AL.
       GELIR-AL.
           MOVE E-GELIR TO Z-TUTAR.
           ACCEPT Z-TUTAR LINE SATIR POSITION 87 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE Z-TUTAR TO E-GELIR.
           PERFORM YENI-GOSTER.
           IF F = 52 OR F = 27 GO ACIKLAMA-AL.
           IF E-GELIR NOT = ZEROS MOVE ZEROS TO E-GIDER GO KASA-YAZ.
       GIDER-AL.
           MOVE E-GIDER TO Z-TUTAR.
           ACCEPT Z-TUTAR LINE SATIR POSITION 104 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE Z-TUTAR TO E-GIDER.
           PERFORM YENI-GOSTER.
           IF F = 52 OR F = 27 GO GELIR-AL.
           IF E-GIDER = ZEROS GO GELIR-AL.
           IF E-GIDER NOT = ZEROS MOVE ZEROS TO E-GELIR GO KASA-YAZ.
       KASA-YAZ.

           MOVE T-TARIH TO KS-TARIH.

           IF YENI = 0 PERFORM CEKSENET-KONTROL
                IF T-ERROR = 1 GO TUS END-IF
           END-IF.
           
           IF YENI = 0 PERFORM KASA-SIL-OK THRU KASA-SIL-OK-SON.
           
           IF E-GELIR NOT = ZEROS MOVE 16 TO KS-MTIP.
           IF E-GIDER NOT = ZEROS MOVE 17 TO KS-MTIP.
           MOVE E-HESAPNO TO KS-HESAPNO.
           MOVE E-ACIKLAMA TO KS-ACIKLAMA.
           MOVE E-GELIR TO KS-GELIR.
           MOVE E-GIDER TO KS-GIDER.
           IF KS-HESAPNO NOT = SPACES 
               PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           ADD 1 TO KS-SIRA.
           CLOSE KASA. OPEN I-O KASA.
       YENI-YAZ.
	       PERFORM WRITE-KASA.
		   IF WVAR = 0 ADD 1 TO KS-SIRA GO YENI-YAZ.
           IF YENI = 1 MOVE ZEROS TO KS-GELIR KS-GIDER
              PERFORM ALT-TOPLAM-KONTROL.
           CLOSE KASA. OPEN INPUT KASA.
           READ KASA.
           PERFORM ALT-TOPLAM.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-KASA-SON. 
       
       YENI-GOSTER.
           MOVE E-DEPONO TO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           IF DP-NO = ZEROS MOVE SPACES TO DP-ADI.
           MOVE E-SEKILNO TO OD-NO.
           READ ODEME INVALID KEY MOVE SPACES TO OD-ADI.
           IF OD-NO = ZEROS MOVE SPACES TO OD-ADI.
           MOVE E-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI.
           MOVE E-GELIR TO Z-GELIR.
           MOVE E-GELIR TO Z-GIDER.
           DISPLAY E-HESAPNO LINE SATIR POSITION 2 SIZE 10
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE'
                'º' DP-ADI SIZE 7 'º' OD-ADI SIZE 7 'º' 
                SC-HESAPADI SIZE 24 'º' E-ACIKLAMA SIZE 32 'º'
                Z-GELIR 'º' Z-GIDER.
       
       CEKSENET-KONTROL.
           MOVE 0 TO T-ERROR.
           OPEN INPUT CEKSENETLOG.
           IF KS-MEVRAKNO = SPACES
            MOVE KS-KEY TO CSL-MAKBUZNO
            MOVE ZEROS TO CSL-TIP
           ELSE
            MOVE KS-MEVRAKNO TO CSL-MAKBUZNO
            MOVE KS-MTIP TO CSL-TIP
           END-IF.
           START CEKSENETLOG KEY IS EQUAL TO CSL-MAKBUZ
               INVALID KEY CLOSE CEKSENETLOG
               NOT INVALID KEY 
               MOVE 'BU SATIR CEK/SENET ˜LE BA¦LANMIž !!!'
               TO ONAY-MESSAGE
               PERFORM WARN-MESSAGE
               CLOSE CEKSENETLOG
               MOVE 1 TO T-ERROR
           END-START.

       KASA-SIL.
           MOVE 'SATIR S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           PERFORM CEKSENET-KONTROL.
           IF T-ERROR = 1 GO TUS.
           MOVE ZEROS TO E-GELIR E-GIDER.
       KASA-SIL-OK.
           IF KS-HESAPNO NOT = SPACES
            CLOSE MAKBUZ
            OPEN I-O MAKBUZ
            MOVE KS-MEVRAKNO TO MK-EVRAKNO
            MOVE KS-MTIP TO MK-TIP
            READ MAKBUZ INVALID KEY CONTINUE
                NOT INVALID KEY PERFORM DELETE-MAKBUZ END-READ
            CLOSE MAKBUZ OPEN INPUT MAKBUZ
            MOVE KS-HESAPNO TO TCH-HESAPNO
            MOVE KS-TARIH TO TCH-TARIH
            MOVE MK-DEPONO TO TCH-DEPONO
            MOVE KS-MEVRAKNO TO TCH-EVRAKNO
            MOVE KS-MTIP TO TCH-TIP
            PERFORM CARHAR-SIL
           END-IF.
           CLOSE KASA. OPEN I-O KASA.
           PERFORM ALT-TOPLAM-KONTROL.
           READ KASA.
           PERFORM DELETE-KASA.
           CLOSE KASA.
           OPEN INPUT KASA.
       KASA-SIL-OK-SON.
       KASA-SIL-SON.
           GO DETAY-AL.
       
       ALT-TOPLAM-KONTROL.
           MOVE KS-SIRA TO T-SIRA.
           MOVE KS-GELIR TO TX-GELIR.
           MOVE KS-GIDER TO TX-GIDER.
           MOVE T-TARIH TO KS-TARIH.
           MOVE ZEROS TO KS-SIRA.
           READ KASA INVALID KEY  
                INITIALIZE KS-KAYIT
                MOVE T-TARIH TO KS-TARIH
                PERFORM WRITE-KASA
           END-READ.
           SUBTRACT TX-GELIR FROM KS-GELIR.
           SUBTRACT TX-GIDER FROM KS-GIDER.
           ADD E-GELIR TO KS-GELIR.
           ADD E-GIDER TO KS-GIDER.
           PERFORM REWRITE-KASA.
           MOVE T-SIRA TO KS-SIRA.
           MOVE T-TARIH TO KS-TARIH.
           
       ISLEM-YAZ.
           MOVE 0 TO T-ERROR.
           
           MOVE KS-MTIP TO DX-TIP.
           MOVE E-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           
           IF AY-EVRAKNO = SPACES
              MOVE 'KAFADAK˜ MAKBUZ EVRAK NUMARASI BOž !!!' 
                TO ONAY-MESSAGE
              PERFORM WARN-MESSAGE 
              MOVE 1 TO T-ERROR
              GO ISLEM-YAZ-SON
           END-IF.
           
           MOVE KS-MTIP TO AY-TIP.
           MOVE AY-EVRAKNO TO MK-EVRAKNO.
           READ MAKBUZ INVALID KEY CONTINUE
            NOT INVALID KEY
              MOVE 'KAFADAK˜ MAKBUZ EVRAK NO. YA A˜T EVRAK VAR !!!' 
              TO ONAY-MESSAGE 
              PERFORM WARN-MESSAGE
              MOVE 1 TO T-ERROR
              GO ISLEM-YAZ-SON
           END-READ.
           
           MOVE AY-EVRAKNO TO MK-EVRAKNO KS-MEVRAKNO.
           MOVE KS-MTIP TO MK-TIP.
           MOVE KS-HESAPNO TO MK-HESAPNO.
           MOVE E-DEPONO TO MK-DEPONO.
           MOVE KS-TARIH TO MK-TARIH.
           MOVE E-SEKILNO TO MK-SEKILNO.
           MOVE KS-ACIKLAMA TO MK-ACIKLAMA.
           IF KS-MTIP = 16 MOVE KS-GELIR TO MK-TUTAR.
           IF KS-MTIP = 17 MOVE KS-GIDER TO MK-TUTAR.
           PERFORM MAKBUZ-YAZ.
           OPEN I-O AYAR.
           PERFORM NUMARA-ARTIR.
           CLOSE AYAR.
       ISLEM-YAZ-SON.
           
           
       MAKBUZ-YAZ.
           CLOSE MAKBUZ.
           OPEN I-O MAKBUZ.
           PERFORM WRITE-MAKBUZ.
           CLOSE MAKBUZ. OPEN INPUT MAKBUZ.
           MOVE MK-HESAPNO TO TCH-HESAPNO.
           MOVE MK-DEPONO TO TCH-DEPONO.
           MOVE MK-TIP TO TCH-TIP.
           MOVE MK-EVRAKNO TO TCH-EVRAKNO.
           MOVE MK-TARIH TO TCH-TARIH.
           MOVE MK-ACIKLAMA TO TCH-ACIKLAMA.
           MOVE ZEROS TO TCH-BORC TCH-ALACAK.
           EVALUATE MK-TIP
             WHEN 16 MOVE MK-TUTAR TO TCH-ALACAK
             WHEN 17 MOVE MK-TUTAR TO TCH-BORC 
           END-EVALUATE.
           MOVE MK-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 1 T-KOD T-ADI
                  BY CONTENT 0000 0000 0000 0000 0.
           CANCEL 'SEKSEC'.
           MOVE T-ADI TO TCH-VADE.
           PERFORM CARHAR-YAZ THRU CARHAR-YAZ-SON.
       
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE T-TARIH TO KS-TARIH.
           MOVE 1 TO KS-SIRA.
           START KASA KEY IS NOT LESS THAN KS-KEY.
           READ KASA NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE T-TARIH TO KS-TARIH.
           MOVE 99999 TO KS-SIRA.
           START KASA KEY IS NOT GREATER THAN KS-KEY.
           READ KASA NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ KASA NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
           
       PGUP-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI
            ELSE
              PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ KASA PREVIOUS
             END-PERFORM.
           READ KASA PREVIOUS AT END
              READ KASA NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
		      READ KASA NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ KASA PREVIOUS AT END 
                    READ KASA NEXT 
                    GO PG-OKU-SON.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
                    READ KASA NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ KASA PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ KASA NEXT
           END-PERFORM.
           READ KASA NEXT AT END
              READ KASA PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
		      READ KASA PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ KASA NEXT AT END 
             READ KASA PREVIOUS MOVE ILK TO SATIR.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
		     READ KASA PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ KASA NEXT
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI
           ELSE
              PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ KASA PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ KASA PREVIOUS AT END READ KASA NEXT GO TUS.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
                READ KASA NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ KASA PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ KASA NEXT AT END 
		       READ KASA PREVIOUS GO ASAGI-YENI-AL.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
               READ KASA PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ KASA NEXT
               END-PERFORM
                 MOVE SON TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
	   ASAGI-YENI-AL.
	       MOVE 1 TO YENI.
           IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ KASA NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   COMPUTE LIS = SON - 1
                   PERFORM YENI-SERITLI
                   GO TUS
           END-IF.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM YENI-SERITLI.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = SON PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = ILK GO Y-LISTE-SON.
           READ KASA PREVIOUS AT END READ KASA NEXT 
                GO Y-LISTE-SON.
		   IF KS-TARIH NOT = T-TARIH OR KS-SIRA = ZEROS
              READ KASA NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE KS-HESAPNO TO B-HESAPNO SC-HESAPNO.
           MOVE KS-MTIP TO MK-TIP.
           MOVE KS-MEVRAKNO TO MK-EVRAKNO.
           READ MAKBUZ INVALID KEY MOVE ZEROS TO MK-DEPONO.
           MOVE MK-DEPONO TO DP-NO BEK-DEPONO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           IF DP-NO = ZEROS MOVE SPACES TO DP-ADI.
           MOVE MK-SEKILNO TO OD-NO BEK-SEKILNO.
           READ ODEME INVALID KEY MOVE SPACES TO OD-ADI.
           MOVE OD-ADI TO B-SEKILNO.
           MOVE DP-ADI TO B-DEPONO.
           MOVE KS-ACIKLAMA TO B-ACIKLAMA.
           MOVE KS-GELIR TO B-GELIR.
           MOVE KS-GIDER TO B-GIDER.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI.
           MOVE SC-HESAPADI TO B-HESAPADI. 
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       YENI-SERITLI.
           DISPLAY SPACES LINE SATIR POSITION 2 SIZE 10
                 CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE'
                'º' SPACES SIZE 7 'º' SPACES SIZE 7 'º' 
                SPACES SIZE 24 'º' SPACES SIZE 32 'º'
                SPACES SIZE 16 'º' SPACES SIZE 16.
       YENI-SERITSIZ.
           DISPLAY 'º' LINE SATIR POSITION 1
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                SPACES SIZE 10 'º' SPACES SIZE 7 'º' 
                SPACES SIZE 7 'º' 
                SPACES SIZE 24 'º' SPACES SIZE 32 'º'
                SPACES SIZE 16 'º' SPACES SIZE 16 'º'.
       CBAKIYE-OKU.
           OPEN INPUT CARHAR.
           PERFORM VARYING DPI FROM 1 BY 1 UNTIL DPI = 3
            MOVE SC-HESAPNO TO CH-HESAPNO
            MOVE ZEROS TO CH-SIRA CH-TARIH
            MOVE KS-YIL TO CH-YIL
            MOVE DPI TO CH-DEPONO
            READ CARHAR INVALID KEY
              START CARHAR KEY IS LESS THAN CH-KEY
                INVALID KEY MOVE ZEROS TO CH-BORC CH-ALACAK
              NOT INVALID KEY
                READ CARHAR NEXT
                IF CH-HESAPNO NOT = SC-HESAPNO 
                  OR CH-DEPONO NOT = DPI
                  MOVE ZEROS TO CH-BORC CH-ALACAK
                ELSE
                  MOVE ZEROS TO CH-AY CH-GUN CH-SIRA
                  READ CARHAR
                END-IF
              END-START
            END-READ
            MOVE ZEROS TO DP-BAKIYE(DPI)
            COMPUTE DP-BAKIYE(DPI) = CH-BORC - CH-ALACAK
           END-PERFORM.
           CLOSE CARHAR.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==KASA==
                                  "%T%"  BY "KASA"
                                 ==%R%== BY ==KS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==MAKBUZ==
                                  "%T%"  BY "MAKBUZ"
                                 ==%R%== BY ==MK-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
              