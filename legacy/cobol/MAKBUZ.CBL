       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'MAKBUZ'.
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
       copy 'sicil.cpy'.
       copy 'makbuz.cpy'.
       copy 'carhar.cpy'.
       copy 'ayar.cpy'.
       copy 'ceksenet.cpy'.
       copy 'depo.cpy'.
       copy 'odeme.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'depo.cpz'.
       copy 'odeme.cpz'.
       copy 'sicil.cpz'.
       copy 'makbuz.cpz'.
       copy 'carhar.cpz'.
       copy 'ayar.cpz'.
       copy 'ceksenet.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-TARIH.
                    04 TP-YIL            PIC 9999.
                    04 TP-AY             PIC 99.
                    04 TP-GUN            PIC 99.
               03 TP-SIRA           PIC 9(10).
           02 TP-SEKILNO                PIC 9999.    
           02 TP-SEKILADI               PIC X(60).
           02 TP-HESAPNO            PIC X(15).
           02 TP-HESAPADI           PIC X(50).
           02 TP-YETKILI            PIC X(40).
           02 TP-EVRAKNO           PIC X(15).
           02 TP-DEPONO             PIC 9999.
           02 TP-DPADI              PIC X(30).
           02 TP-ACIKLAMA            PIC X(15).
           02 TP-TUTAR              PIC 9(13)V99.
           
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                 PIC 99.
           02 CIZ               PIC 99.
           02 DUR               PIC X.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999 VALUE 6.
           02 SON                   PIC 9999 VALUE 33. 
           02 BASLIK            PIC X(50).
           02 TIP               PIC 99.
           02 EVRAKNO           PIC X(15).
           02 GORUNTU           PIC 9.
           02 ISLEM             PIC 9.
           02 KILIT             PIC 9.
           02 FILTRE-VARIABLES.
               03 BA-TARIH.
                    04 BA-YIL            PIC 9999.
                    04 BA-AY             PIC 99.
                    04 BA-GUN            PIC 99.
               03 BT-TARIH.
                    04 BT-YIL            PIC 9999.
                    04 BT-AY             PIC 99.
                    04 BT-GUN            PIC 99.
               03 FT-HESAPNO            PIC X(15).
               03 FT-DEPONO         PIC 9999.
               03 FT-SEKILNO        PIC 9999.
               03 FT-ACIKLAMA       PIC X(40).
               03 TT                PIC 9(12)V9999.
               03 T1                PIC 9(12)V9999.
               03 T2                PIC 9(12)V9999.
           02 T-TOPLAM            PIC 9(13)V99.
           02 ZI-TUTAR          PIC ZZZZZZZZZZZZZ,ZZ.
           02 Z-TUTAR           PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
               
       01 BILGI.
           02 B-TARIH           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-EVRAKNO         PIC X(11).
           02 FILLER            PIC X VALUE 'º'.
           02 B-SEKILADI        PIC X(6).
           02 FILLER            PIC X VALUE 'º'.
           02 B-HESAPNO         PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-HESAPADI        PIC X(16).
           02 FILLER            PIC X VALUE 'º'.
           02 B-YETKILI         PIC X(15).
           02 FILLER            PIC X VALUE 'º'.
           02 B-DEPONO          PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ACIKLAMA        PIC X(19).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TUTAR           PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
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
              'º' HIGHLIGHT. 
           02 PIC X(118) USING BASLIK HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(6) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(19) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 4 COLUMN 2 BACKGROUND BLUE FOREGROUND WHITE
               PIC X(10) FROM ' TAR˜H' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(11) FROM 'EVRAK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(6) FROM ' T˜P' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(16) FROM ' HESAP ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(15) FROM ' YETK˜L˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO ' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(19) FROM ' A€IKLAMA ' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(16) FROM ' TUTAR ' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 LINE 5 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(6) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(19) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 34 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(6) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(19) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 35 COLUMN 90 BACKGROUND BLUE FOREGROUND WHITE
              'TOPLAM' HIGHLIGHT.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(101) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       01 SCR-MAKBUZ.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(75) FROM ' MAKBUZ ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º MAKBUZ NO  [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º TAR˜H      [  /  /    ]' HIGHLIGHT. 
           02 PIC X(51) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º DEPO NO    [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(31) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º ™DEME žEKL˜[' HIGHLIGHT. 
           02 PIC X(10) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(51) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º HESAP NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º HESAP ADI  [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º YETK˜L˜    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º A€IKLAMA   [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 24 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º TUTAR      [' HIGHLIGHT. 
           02 PIC X(16) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(45) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 25 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 26 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
           02 SCR-MAKBUZ-TARIH.
               03 LINE 17 COLUMN 16 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING MK-GUN HIGHLIGHT AUTO.
               03 LINE 17 COLUMN 19 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING MK-AY HIGHLIGHT AUTO.
               03 LINE 17 COLUMN 22 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZZZ USING MK-YIL HIGHLIGHT AUTO.
       01 SCR-TOPLAM.
           02 LINE 35 COLUMN 104 BACKGROUND BLUE FOREGROUND BROWN
              PIC Z.ZZZ.ZZZ.ZZZ,ZZ USING T-TOPLAM HIGHLIGHT.
       01 SCR-FILTRELEME.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(75) FROM ' MAKBUZ F˜LTRELE' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 SCR-FILTRE-TARIH.
               03 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
                 'º BAžLAMA    [' HIGHLIGHT. 
               03 PIC ZZ USING BA-GUN HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZ USING BA-AY HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZZZ USING BA-YIL HIGHLIGHT AUTO.
               03 ']' HIGHLIGHT.
               03 PIC X(51) FROM ALL ' '. 03 'º' HIGHLIGHT.
               03 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
                 'º B˜T˜ž      [' HIGHLIGHT. 
               03 PIC ZZ USING BT-GUN HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZ USING BT-AY HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZZZ USING BT-YIL HIGHLIGHT AUTO.
               03 ']' HIGHLIGHT.
               03 PIC X(51) FROM ALL ' '. 03 'º' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º DEPO NO    [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(31) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º ™DEME žEKL˜[' HIGHLIGHT. 
           02 PIC X(10) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(51) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º HESAP NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º HESAP ADI  [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º YETK˜L˜    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º A€IKLAMA   [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 24 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 25 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
               INTO TIP, EVRAKNO, GORUNTU, ISLEM
           END-UNSTRING.
           
           IF GORUNTU = 1 MOVE EVRAKNO TO TP-EVRAKNO
                PERFORM MAKBUZ-GORUNTU THRU MAKBUZ-GORUNTU-SON
                MOVE SPACES TO MAIN-ARGUMENT-VALUE
                STRING TIP ';' EVRAKNO 
                  ';' GORUNTU ';' ISLEM ';' 
                  DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
                END-STRING
                MOVE 100 TO MAIN-ARGUMENT-SIZE
                EXIT PROGRAM 
                STOP RUN
           END-IF.
           
           EVALUATE TIP
           WHEN 16 MOVE ' TAHS˜LAT MAKBUZLARI' TO BASLIK
           WHEN 17 MOVE ' TED˜YE MAKBUZLARI' TO BASLIK
           END-EVALUATE.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
           INITIALIZE FILTRE-VARIABLES.
           ACCEPT BA-TARIH FROM CENTURY-DATE.
           MOVE BA-TARIH TO BT-TARIH.
           MOVE 1 TO BA-AY BA-GUN.
           
       BEGIN-PROGRAM.
           PERFORM VARYING CIZ FROM 4 BY 1 UNTIL CIZ > 35
                IF CIZ < 35
                 DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 10 'º' SPACES SIZE 11 'º' 
                     SPACES SIZE 6 'º' SPACES SIZE 10 'º' 
                     SPACES SIZE 16 'º' SPACES SIZE 15 'º' 
                     SPACES SIZE 7 'º' SPACES SIZE 19 'º' 
                     SPACES SIZE 16 'º'
                ELSE
                DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    SPACES SIZE 101 'º' SPACES SIZE 16 'º'
                END-IF
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
                ' F2 ' LINE 37 POSITION 1 'YEN˜ MAKBUZ' REVERSE LOW
                ' F3 ' 'AYARLAR' REVERSE LOW
                ' F4 ' 'F˜LTRE' REVERSE LOW
                ' F5 ' '™DEME žEK˜LLER˜' REVERSE LOW
                ' F8 ' 'MAKBUZ L˜STE YAZDIR' REVERSE LOW
                ' F10 ' 'MAKBUZ S˜L' REVERSE LOW.
           
           
           COMPUTE T1 = BA-GUN + BA-AY * 30,41 + BA-YIL * 365.
           COMPUTE T2 = BT-GUN + BT-AY * 30,41 + BT-YIL * 365.
           
           OPEN INPUT MAKBUZ SICIL DEPO ODEME.

           MOVE ZEROS TO T-TOPLAM.
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
           
           MOVE BA-TARIH TO MK-TARIH.
           START MAKBUZ KEY IS NOT LESS THAN MK-TARIH
            INVALID KEY GO MAKBUZ-OKU-SON.
           
       MAKBUZ-OKU.
           READ MAKBUZ NEXT AT END GO MAKBUZ-OKU-SON.
           IF MK-TIP NOT = TIP GO MAKBUZ-OKU.
           COMPUTE TT =  MK-GUN + MK-AY * 30,41 + MK-YIL * 365.
           IF TT > T2 GO MAKBUZ-OKU-SON.
           IF FT-HESAPNO NOT = SPACES AND MK-HESAPNO NOT = FT-HESAPNO
                GO MAKBUZ-OKU.
           IF FT-SEKILNO NOT = ZEROS AND MK-SEKILNO NOT = FT-SEKILNO
                GO MAKBUZ-OKU.
           IF FT-DEPONO NOT = ZEROS AND MK-DEPONO NOT = FT-DEPONO
                GO MAKBUZ-OKU.
           IF FT-ACIKLAMA NOT = SPACES AND MK-ACIKLAMA NOT = FT-ACIKLAMA
                GO MAKBUZ-OKU.
           MOVE MK-TARIH TO TP-TARIH.
           MOVE MK-EVRAKNO TO TP-EVRAKNO.
           MOVE MK-SEKILNO TO TP-SEKILNO OD-NO.
           READ ODEME INVALID KEY MOVE SPACES TO OD-ADI.
           MOVE OD-ADI TO TP-SEKILADI.
           
           MOVE MK-DEPONO TO TP-DEPONO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE DP-ADI TO TP-DPADI.
           
           MOVE MK-ACIKLAMA TO TP-ACIKLAMA.
           MOVE MK-TUTAR TO TP-TUTAR.
           ADD MK-TUTAR TO T-TOPLAM.
           MOVE MK-HESAPNO TO TP-HESAPNO SC-HESAPNO.
           READ SICIL INVALID KEY GO MAKBUZ-OKU.
           MOVE SC-HESAPADI TO TP-HESAPADI.
           MOVE SC-YETKILI TO TP-YETKILI
           MOVE 1 TO TP-SIRA.
       TEMP-YAZ1.
           WRITE TP-KAYIT INVALID KEY ADD 1 TO TP-SIRA GO TEMP-YAZ1.
           GO MAKBUZ-OKU.
       MAKBUZ-OKU-SON.
           CLOSE MAKBUZ SICIL TEMPFILE ODEME DEPO.
       
           DISPLAY SCR-TOPLAM.
           MOVE ILK TO SATIR.
           OPEN INPUT TEMPFILE.
           MOVE ZEROS TO TP-KEY KILIT.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
            INVALID KEY GO A-LISTE-SON.
           READ TEMPFILE NEXT.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ TEMPFILE NEXT AT END 
                    READ TEMPFILE PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
               DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 10 'º' SPACES SIZE 11 'º' 
                     SPACES SIZE 6 'º' SPACES SIZE 10 'º' 
                     SPACES SIZE 16 'º' SPACES SIZE 15 'º' 
                     SPACES SIZE 7 'º' SPACES SIZE 19 'º' 
                     SPACES SIZE 16 'º'
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE ZEROS TO TP-KEY.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
            INVALID KEY MOVE 1 TO KILIT GO TUS.
           READ TEMPFILE NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE TEMPFILE DELETE FILE TEMPFILE 
                GO STOP-PROGRAM.
           IF F = 52 AND KILIT = 0 GO YUKARI-AL.
           IF F = 53 AND KILIT = 0 GO ASAGI-AL.
           IF F = 54 AND KILIT = 0 GO HOME-TUSU.
           IF F = 82 AND KILIT = 0 GO END-TUSU.
           IF F = 68 AND KILIT = 0 GO PGDN-TUSU.
           IF F = 67 AND KILIT = 0 GO PGUP-TUSU.
           IF F = 81 AND KILIT = 0 GO CTHO-TUSU.
           IF F = 83 AND KILIT = 0 GO CTEN-TUSU.
           IF F = 13 AND KILIT = 0 
                PERFORM MAKBUZ-GORUNTU THRU MAKBUZ-GORUNTU-SON.
           IF F = 8 AND KILIT = 0 GO MAKLIS-YAZDIR.
           IF F = 3 CALL 'AYAR' USING TIP CANCEL 'AYAR' GO TUS.
           IF F = 5 CALL 'SEKIL' USING 1 MK-SEKILNO CANCEL 'SEKIL'
                    GO TUS.
           IF F = 10 AND KILIT = 0 GO MAKBUZ-SIL.
           IF F = 2 GO YENI-MAKBUZ.
           IF F = 4 GO MAKBUZ-ARAMA.
           GO TUS.
       copy 'onayper.cpy'.
       MAKBUZ-ARAMA.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                ' F1 ' LINE 37 POSITION 1
                  'S˜C˜LLER' REVERSE LOW
           CALL 'GOLGE' USING BY CONTENT 0012 0002 0025 0078.
           CANCEL 'GOLGE'.
           DISPLAY SCR-FILTRELEME.
           MOVE FT-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0018 0030 0025 1.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           MOVE FT-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 1 T-KOD T-ADI
                  BY CONTENT 0016 0019 0030 0025 1.
           CANCEL 'SEKSEC'.
           DISPLAY T-ADI LINE 19 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY FT-HESAPNO LINE 20 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI
                SC-YETKILI.
           CLOSE SICIL.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY FT-ACIKLAMA LINE 23 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       FTARIH-AL.
           ACCEPT SCR-FILTRE-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 PERFORM MAKBUZ-ARAMA-SON GO TUS.
       FDEPONO-AL.
           MOVE FT-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0018 0030 0025 1.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO FTARIH-AL.
           MOVE T-KOD TO FT-DEPONO.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       FSEKIL-AL.
           MOVE FT-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 0 T-KOD T-ADI
                  BY CONTENT 0016 0019 0030 0025 1.
           CANCEL 'SEKSEC'.
           IF T-KOD = -1 GO FDEPONO-AL.
           MOVE T-KOD TO FT-SEKILNO.
           DISPLAY T-ADI LINE 19 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       FHESAPNO-AL.
           ACCEPT FT-HESAPNO LINE 20 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 21 POSITION 16 SIZE 50
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SPACES LINE 22 POSITION 16 SIZE 40
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           IF F = 27 OR F = 52 GO FSEKIL-AL.
           IF F = 1 CALL 'SICPEN' USING FT-HESAPNO
                CANCEL 'SICPEN' GO FHESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY CLOSE SICIL GO FACIKLAMA-AL.
           CLOSE SICIL.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       FACIKLAMA-AL.
           ACCEPT FT-ACIKLAMA LINE 23 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO FHESAPNO-AL.
           CLOSE TEMPFILE.
           DELETE FILE TEMPFILE.
           PERFORM MAKBUZ-ARAMA-SON.
           GO BEGIN-PROGRAM.
       MAKBUZ-ARAMA-SON.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       
 
       MAKBUZ-SIL.
           MOVE 'MAKBUZ S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           OPEN INPUT CEKSENETLOG.
           MOVE TP-EVRAKNO TO CSL-MAKBUZNO.
           MOVE TIP TO CSL-TIP.
           START CEKSENETLOG KEY IS EQUAL TO CSL-MAKBUZ
                INVALID KEY CONTINUE
             NOT INVALID KEY READ CEKSENETLOG NEXT
             CLOSE CEKSENETLOG
             STRING 'BU MAKBUZ ' CSL-EVRAKNO 
                ' NOLU ÇEK/SENET E BAÐLANMIÞ SÝLÝNEMEZ'
                DELIMITED BY SIZE INTO ONAY-MESSAGE
             PERFORM WARN-MESSAGE
             GO TUS
           END-START.
           CLOSE CEKSENETLOG.
           
           MOVE TP-HESAPNO TO TCH-HESAPNO.
           MOVE TP-TARIH TO TCH-TARIH.
           MOVE TP-DEPONO TO TCH-DEPONO.
           MOVE TP-EVRAKNO TO TCH-EVRAKNO.
           MOVE TIP TO TCH-TIP.
           PERFORM CARHAR-SIL.
           OPEN I-O MAKBUZ.
           MOVE TP-EVRAKNO TO MK-EVRAKNO.
           MOVE TIP TO MK-TIP.
           READ MAKBUZ INVALID KEY CONTINUE
            NOT INVALID KEY PERFORM DELETE-MAKBUZ
           CLOSE MAKBUZ TEMPFILE.
           DELETE FILE TEMPFILE.
           GO BEGIN-PROGRAM.
       MAKBUZ-GORUNTU.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           OPEN INPUT MAKBUZ.
           MOVE TIP TO MK-TIP.
           MOVE TP-EVRAKNO TO MK-EVRAKNO.
           READ MAKBUZ. CLOSE MAKBUZ.
           
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           
           DISPLAY SCR-MAKBUZ.
           CALL 'GOLGE' USING BY CONTENT 0012 0002 0026 0078.
           CANCEL 'GOLGE'.
           
           OPEN INPUT SICIL.
           MOVE MK-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           CLOSE SICIL.
           MOVE MK-TUTAR TO Z-TUTAR.
           DISPLAY MK-EVRAKNO LINE 16 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'
                  MK-HESAPNO LINE 20 POSITION 16
                  SC-HESAPADI LINE 21 POSITION 16
                  SC-YETKILI LINE 22 POSITION 16
                  MK-ACIKLAMA LINE 23 POSITION 16 
                  Z-TUTAR LINE 24 POSITION 16.
           DISPLAY SCR-MAKBUZ-TARIH.
           MOVE MK-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0018 0030 0025 0.
           CANCEL 'DEPSEC'.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           MOVE MK-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 1 T-KOD T-ADI
                  BY CONTENT 0016 0019 0030 0025 0.
           CANCEL 'SEKSEC'.
           DISPLAY T-ADI LINE 19 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       MAKBUZ-TUS.    
           MOVE ' ' TO DUR.
           ACCEPT DUR NO BEEP LINE 37 POSITION 1.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 13 
             PERFORM CEKSENETLOG-GORUNTU THRU CEKSENETLOG-GORUNTU-SON
             GO MAKBUZ-GORUNTU-SON.
           IF F = 27 GO MAKBUZ-GORUNTU-SON.
           GO MAKBUZ-TUS.
       MAKBUZ-GORUNTU-SON. 
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
            
       CEKSENETLOG-GORUNTU.
           OPEN INPUT CEKSENETLOG.
           MOVE MK-EVRAKNO TO CSL-MAKBUZNO.
           MOVE MK-TIP TO CSL-TIP.
           START CEKSENETLOG KEY IS EQUAL TO CSL-MAKBUZ
             INVALID KEY CLOSE CEKSENETLOG
                GO CEKSENETLOG-GORUNTU-SON.
           READ CEKSENETLOG NEXT.
           CLOSE CEKSENETLOG.
           CALL 'CSNLOG' USING BY CONTENT CSL-EVRAKNO CSL-CTIP 
                        BY REFERENCE ISLEM.
           CANCEL 'CSNLOG'.
       CEKSENETLOG-GORUNTU-SON.
       
            
            
       YENI-MAKBUZ.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.

           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                ' F1 ' LINE 37 POSITION 1
                  'S˜C˜LLER' REVERSE LOW
           CALL 'GOLGE' USING BY CONTENT 0012 0002 0026 0078.
           CANCEL 'GOLGE'.
           INITIALIZE MK-KAYIT.
           ACCEPT MK-TARIH FROM CENTURY-DATE.
           DISPLAY SCR-MAKBUZ.
           DISPLAY '(YEN˜ MAKBUZ)' LINE 16 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
      *     OPEN INPUT AYAR.
      *     MOVE TIP TO AY-TIP.
      *     READ AYAR.
      *     MOVE AY-EVRAKNO TO MK-EVRAKNO.
      *     CLOSE AYAR.
      * MAKBUZNO-AL.
      *     ACCEPT MK-EVRAKNO LINE 16 POSITION 16 UPDATE
      *      NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
      *     ACCEPT F FROM ESCAPE KEY.
      *     IF F = 27 PERFORM YENI-MAKBUZ-SON GO TUS.
      *     IF MK-EVRAKNO = SPACES GO MAKBUZNO-AL.
      *     OPEN INPUT MAKBUZ.
      *     MOVE TIP TO MK-TIP.
      *     READ MAKBUZ INVALID KEY CLOSE MAKBUZ GO TARIH-AL.
      *     CLOSE MAKBUZ.
      *     GO MAKBUZNO-AL.
       TARIH-AL.
           DISPLAY SCR-MAKBUZ-TARIH.
           ACCEPT SCR-MAKBUZ-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 PERFORM YENI-MAKBUZ-SON GO TUS.
       DEPONO-AL.
           MOVE MK-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0018 0030 0025 0.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO TARIH-AL.
           MOVE T-KOD TO MK-DEPONO.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       SEKIL-AL.
           MOVE MK-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 0 T-KOD T-ADI
                  BY CONTENT 0016 0019 0030 0025 0.
           CANCEL 'SEKSEC'.
           IF T-KOD = -1 GO DEPONO-AL.
           MOVE T-KOD TO MK-SEKILNO.
           DISPLAY T-ADI LINE 19 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       HESAPNO-AL.
           ACCEPT MK-HESAPNO LINE 20 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 OR F = 52 GO SEKIL-AL.
           IF F = 1 CALL 'SICPEN' USING MK-HESAPNO
                CANCEL 'SICPEN' GO HESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE MK-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY CLOSE SICIL GO HESAPNO-AL.
		   IF SC-PLKOD = 5 THEN
		    MOVE 'HESAP AKT˜F DE¦˜L !!!' TO
                ONAY-MESSAGE
            PERFORM WARN-MESSAGE
			CLOSE SICIL GO HESAPNO-AL.
           
           CLOSE SICIL.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       ACIKLAMA-AL.
           ACCEPT MK-ACIKLAMA LINE 23 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO HESAPNO-AL.
       TUTAR-AL.
           MOVE MK-TUTAR TO ZI-TUTAR.
           ACCEPT ZI-TUTAR LINE 24 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           MOVE ZI-TUTAR TO MK-TUTAR Z-TUTAR.
           DISPLAY Z-TUTAR LINE 24 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 OR F = 52 GO ACIKLAMA-AL.
           
           MOVE TIP TO DX-TIP.
           MOVE MK-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           IF AY-EVRAKNO = SPACES GO TUTAR-AL.
           
           MOVE AY-EVRAKNO TO MK-EVRAKNO.
           
           MOVE TIP TO MK-TIP.
           MOVE MK-SEKILNO TO T-KOD.
           CALL 'SEKSEC' USING 1 T-KOD T-ADI
                  BY CONTENT 0000 0000 0000 0000 0.
           CANCEL 'SEKSEC'.
           
           OPEN I-O MAKBUZ.
           PERFORM WRITE-MAKBUZ.
		   CLOSE MAKBUZ TEMPFILE.
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
           MOVE T-ADI TO TCH-VADE.
           PERFORM CARHAR-YAZ THRU CARHAR-YAZ-SON.
           DELETE FILE TEMPFILE.
           OPEN I-O AYAR.
           MOVE TIP TO DX-TIP.
           MOVE MK-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-ARTIR.
           CLOSE AYAR.
           DISPLAY MK-EVRAKNO LINE 16 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           ACCEPT DUR LINE 37 POSITION 1 NO BEEP.
           PERFORM YENI-MAKBUZ-SON.
           GO BEGIN-PROGRAM.
       copy 'trim.per'.
       copy 'numara.per'.
       copy 'chyaz.per'.    
       
       YENI-MAKBUZ-SON.    
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           
       MAKLIS-YAZDIR.
           MOVE 'MAKBUZ L˜STES˜ YAZDIRILACAK EM˜N M˜S˜N˜Z ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CALL 'CMAKLIS' USING TSAAT TIP.
           CANCEL 'CMAKLIS'.
           GO TUS.
       
       CTHO-TUSU.
           MOVE ZEROS TO TP-KEY.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 99999999 TO TP-TARIH.
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
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ TEMPFILE PREVIOUS AT END 
                    READ TEMPFILE NEXT 
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
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ TEMPFILE NEXT AT END 
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
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           STRING TP-GUN '/' TP-AY '/' TP-YIL
            DELIMITED BY SIZE INTO B-TARIH.
           MOVE TP-DEPONO TO T-KOD.
           MOVE TP-DPADI TO B-DEPONO.
           MOVE T-ADI TO B-DEPONO.
           MOVE TP-SEKILADI TO B-SEKILADI.
           MOVE TP-EVRAKNO TO B-EVRAKNO.
           MOVE TP-HESAPNO TO B-HESAPNO.
           MOVE TP-HESAPADI TO B-HESAPADI.
           MOVE TP-YETKILI TO B-YETKILI.
           MOVE TP-ACIKLAMA TO B-ACIKLAMA.
           MOVE TP-TUTAR TO B-TUTAR.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==MAKBUZ==
                                  "%T%"  BY "MAKBUZ"
                                 ==%R%== BY ==MK-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
          
           