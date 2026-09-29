       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'MIZAN'.
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
       copy 'carhar.cpy'.
       copy 'ayar.cpy'.
       copy 'plasiyer.cpy'.
       copy 'tipkod.cpy'.
       copy 'siciltel.cpy'.
       copy 'sicilkod.cpy'.
       copy 'depo.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       copy 'exls.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicil.cpz'.
       copy 'carhar.cpz'.
       copy 'ayar.cpz'.
       copy 'plasiyer.cpz'.
       copy 'tipkod.cpz'.
       copy 'siciltel.cpz'.
       copy 'sicilkod.cpz'.
       copy 'exls.cpz'.
       copy 'depo.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-HESAPNO            PIC X(15).
               03 TP-DEPONO             PIC 9999.
           02 TP-DPADI              PIC X(30).               
           02 TP-HESAPADI           PIC X(50).
           02 TP-YETKILI            PIC X(40).
           02 TP-TEL                PIC X(60).
           02 TP-MAHALLE            PIC X(20).
           02 TP-ILCE               PIC X(20).
           02 TP-IL                 PIC X(20).
           02 TP-ULKE               PIC X(20).
           02 TP-PLADI              PIC X(60).
           02 TP-KOD                PIC X(60).
           02 TP-BAKIYE             PIC S9(13)V99.
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
           02 TIP               PIC 99.
           02 EVRAKNO           PIC X(15).
           02 KILIT             PIC 9.
           02 ISLEM             PIC 9.
           02 GORUNTU           PIC 9.
           02 FILTRE-VARIABLES.
               03 BA-TARIH.
                    04 BA-YIL            PIC 9999.
                    04 BA-AY             PIC 99.
                    04 BA-GUN            PIC 99.
               03 BT-TARIH.
                    04 BT-YIL            PIC 9999.
                    04 BT-AY             PIC 99.
                    04 BT-GUN            PIC 99.
               03 FT-HESAPNO             PIC X(15).
               03 FT-DEPONO              PIC 9999.
               03 FT-PLNO                PIC 9999.
               03 FT-MAHALLE             PIC X(20).
               03 FT-ILCE                PIC X(20).
               03 FT-IL                  PIC X(20).
               03 FT-ULKE                PIC X(20).
               03 FT-KOD                 PIC 9999.
               03 TT                PIC 9(12)V9999.
               03 T1                PIC 9(12)V9999.
               03 T2                PIC 9(12)V9999.
           02 T-TOPLAM            PIC S9(13)V99.
           02 ZI-TUTAR          PIC ZZZZZZZZZZZZZ,ZZ.
           02 Z-TUTAR           PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 B-TEL             PIC +ZZBZZZBZZZZZZZ.
           02 B-CEP             PIC +ZZBZZZBZZZZZZZ.
           02 B-FAX             PIC +ZZBZZZBZZZZZZZ.
           02 V-TEL             PIC 9999 VALUE ZEROS.
           02 V-FAX             PIC 9999 VALUE ZEROS.
           02 V-CEP             PIC 9999 VALUE ZEROS.
           02 YZ-TUTAR          PIC -------------,--.
           02 T-KEY.
               03 T-HESAPNO            PIC X(15).
               03 T-DEPONO             PIC 9999.    
         
               
       01 BILGI.
           02 B-HESAPNO         PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-DEPONO          PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-HESAPADI        PIC X(13).
           02 FILLER            PIC X VALUE 'º'.
           02 B-YETKILI         PIC X(13).
           02 FILLER            PIC X VALUE 'º'.
           02 BI-TEL            PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-MAHALLE         PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-IL              PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ILCE            PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ULKE            PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-PLADI           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-KOD             PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TUTAR           PIC ---.---.---,--.
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
           02 PIC X(118) FROM 'CAR˜ HESAP KARTLARI M˜ZAN' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 4 COLUMN 2 BACKGROUND BLUE FOREGROUND WHITE
               PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(13) FROM ' HESAP ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(13) FROM ' YETK˜L˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' TEL' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM 'MAH.' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM '˜L' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM '˜L€E' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM 'šLKE' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' PLAS˜YER' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' KOD' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(14) FROM ' BAK˜YE' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 LINE 5 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 34 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 35 COLUMN 90 BACKGROUND BLUE FOREGROUND WHITE
              'TOPLAM' HIGHLIGHT.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(103) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       01 SCR-TOPLAM.
           02 LINE 35 COLUMN 106 BACKGROUND BLUE FOREGROUND BROWN
              PIC ---.---.---,-- USING T-TOPLAM HIGHLIGHT.
       01 SCR-FILTRELEME.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(75) FROM ' M˜ZAN F˜LTRELE' HIGHLIGHT.
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
              'º HESAP NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º HESAP ADI  [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º YETK˜L˜    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º MAHALLE    [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º ˜L€E       [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 24 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º žEH˜R      [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 25 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º šLKE       [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 26 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º ™ZEL KOD   [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 27 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º PLAS˜YER   [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 28 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 29 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
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
                     SPACES SIZE 10 'º' SPACES SIZE 7 'º' 
                     SPACES SIZE 13 'º' SPACES SIZE 13 'º' 
                     SPACES SIZE 10 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 5 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 5 'º' SPACES SIZE 10 'º'
                     SPACES SIZE 10 'º' SPACES SIZE 14 'º'
                ELSE
                DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    SPACES SIZE 103 'º' SPACES SIZE 14 'º'
                END-IF
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
                ' F11 ' LINE 37 POSITION 1 
                'EXCEL €IKTI' REVERSE LOW.
           
           COMPUTE T1 = BA-GUN + BA-AY * 30,41 + BA-YIL * 365.
           COMPUTE T2 = BT-GUN + BT-AY * 30,41 + BT-YIL * 365.
           
           OPEN INPUT CARHAR SICIL PLASIYER SICILTEL SICILKOD TIPKOD
                    DEPO.
           MOVE 'EKSTRELER OKUNUYOR ...' TO ONAY-MESSAGE.
           PERFORM SHOW-LOAD-MESSAGE.
           
           MOVE ZEROS TO T-TOPLAM.
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
           
           MOVE FT-HESAPNO TO CH-HESAPNO.
           START CARHAR KEY IS NOT LESS THAN CH-HESAPNO
            INVALID KEY GO CARHAR-OKU-SON.
       CARHAR-OKU.
           READ CARHAR NEXT AT END GO CARHAR-OKU-SON.
           IF CH-SIRA = ZEROS GO CARHAR-OKU.
           MOVE CH-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY GO CARHAR-OKU.
           IF FT-DEPONO NOT = ZEROS AND FT-DEPONO NOT = CH-DEPONO
                    GO CARHAR-OKU.
           COMPUTE TT =  CH-GUN + CH-AY * 30,41 + CH-YIL * 365.
           IF TT > T2 GO CARHAR-OKU.
           IF FT-HESAPNO NOT = SPACES AND SC-HESAPNO NOT = FT-HESAPNO
                    GO CARHAR-OKU.
           IF FT-MAHALLE NOT = SPACES AND FT-MAHALLE NOT = SC-MAHALLE
                    GO CARHAR-OKU.
           IF FT-ILCE NOT = SPACES AND FT-ILCE NOT = SC-ILCE
                    GO CARHAR-OKU.
           IF FT-IL NOT = SPACES AND FT-IL NOT = SC-IL
                    GO CARHAR-OKU.
           IF FT-ULKE NOT = SPACES AND FT-ULKE NOT = SC-ULKE
                    GO CARHAR-OKU.
           IF FT-PLNO NOT = ZEROS AND FT-PLNO NOT = SC-PLKOD
                    GO CARHAR-OKU.
           IF FT-KOD NOT = ZEROS
                MOVE SC-HESAPNO TO SK-HESAPNO
                MOVE FT-KOD TO SK-KOD
                READ SICILKOD INVALID KEY GO CARHAR-OKU.
           
           MOVE SC-HESAPNO TO TP-HESAPNO.
           IF FT-DEPONO = ZEROS
                MOVE ZEROS TO CH-DEPONO.
           
           MOVE CH-DEPONO TO TP-DEPONO.
           READ TEMPFILE INVALID KEY INITIALIZE TP-KAYIT
                MOVE CH-DEPONO TO TP-DEPONO
                MOVE SC-HESAPNO TO TP-HESAPNO
                MOVE SC-HESAPADI TO TP-HESAPADI
                MOVE SC-YETKILI TO TP-YETKILI
                MOVE SC-MAHALLE TO TP-MAHALLE
                MOVE SC-ILCE TO TP-ILCE
                MOVE SC-IL TO TP-IL
                MOVE SC-ULKE TO TP-ULKE
                MOVE SC-PLKOD TO PL-NO
                MOVE TP-DEPONO TO DP-NO
                READ DEPO INVALID KEY MOVE SPACES TO DP-ADI END-READ
                MOVE DP-ADI TO TP-DPADI
                READ PLASIYER INVALID KEY
                  MOVE SPACES TO PL-ADI
                END-READ
                MOVE PL-ADI TO TP-PLADI
                PERFORM TEL-AL
                PERFORM KOD-AL THRU KOD-AL-SON
                WRITE TP-KAYIT END-WRITE
           END-READ.
           
           COMPUTE TP-BAKIYE = (CH-BORC - CH-ALACAK) + TP-BAKIYE.
           REWRITE TP-KAYIT.
           
           ADD CH-BORC TO T-TOPLAM.
           SUBTRACT CH-ALACAK FROM T-TOPLAM.
           
           GO CARHAR-OKU.
       KOD-AL.
           MOVE TP-HESAPNO TO SK-HESAPNO.
           MOVE ZEROS TO SK-KOD.
           MOVE SPACES TO TP-KOD.
           START SICILKOD KEY IS NOT LESS THAN SK-KEY
                INVALID KEY GO KOD-AL-SON.
       KOD-AL-OKU.
           READ SICILKOD NEXT AT END GO KOD-AL-SON.
           IF TP-HESAPNO NOT = SK-HESAPNO GO KOD-AL-SON.
           MOVE SK-KOD TO TK-KOD.
           READ TIPKOD INVALID KEY GO KOD-AL-OKU.
           IF TP-KOD = SPACES MOVE TK-TIP TO TP-KOD
             ELSE
             STRING TP-KOD ',' TK-TIP DELIMITED BY SPACES
              INTO TP-KOD
           END-IF.
           GO KOD-AL-OKU.
       KOD-AL-SON.
       
       TEL-AL.
           MOVE ZEROS TO SCT-SIRA.
           MOVE SC-HESAPNO TO SCT-HESAPNO.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL.
           COMPUTE V-TEL = SCT-TEL / 100000000.
           COMPUTE V-FAX = (SCT-TEL - V-TEL * 100000000) / 10000.
           COMPUTE V-CEP = SCT-TEL - V-TEL * 100000000 - V-FAX * 10000.
           MOVE ZEROS TO B-TEL B-CEP B-FAX.
           IF V-TEL NOT = ZEROS MOVE V-TEL TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-TEL.
           IF V-FAX NOT = ZEROS MOVE V-FAX TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-FAX.
           IF V-CEP NOT = ZEROS MOVE V-CEP TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-CEP.
           MOVE SPACES TO TP-TEL.
           STRING 'Tel:' B-TEL ' Cep:' B-CEP ' Fax:' B-FAX
                DELIMITED BY SIZE INTO TP-TEL.
           
       CARHAR-OKU-SON.
           CLOSE CARHAR SICIL PLASIYER SICILTEL 
                 TEMPFILE SICILKOD DEPO TIPKOD.
           PERFORM DISPOSE-LOAD-MESSAGE.
           DISPLAY SCR-TOPLAM.
           MOVE ILK TO SATIR.
           OPEN INPUT TEMPFILE.
           INITIALIZE TP-KEY.
           MOVE ZEROS TO KILIT.
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
                     SPACES SIZE 10 'º' SPACES SIZE 7 'º' 
                     SPACES SIZE 13 'º' SPACES SIZE 13 'º' 
                     SPACES SIZE 10 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 5 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 5 'º' SPACES SIZE 10 'º'
                     SPACES SIZE 10 'º' SPACES SIZE 14 'º'
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
           IF F = 8  AND KILIT = 0 GO MIZAN-YAZDIR.
           IF F = 13 AND KILIT = 0 GO EKSTRE-GORUNTU.
           IF F = 4  GO MIZAN-ARAMA.
           IF F = 11 GO XLS-CIKTI.
           GO TUS.
       XLS-CIKTI.
           MOVE TP-KEY TO T-KEY.
           ACCEPT EXLS-SAAT FROM TIME.
           MOVE 'MIZAN' TO EXLS-TANIM.
           OPEN OUTPUT EXLS.
           
           MOVE FT-DEPONO TO T-KOD.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV
                  BY CONTENT 0000 0000 0000 0000 1.
           CANCEL 'DEPSEC'.
           
           STRING FT-MAHALLE FT-ILCE FT-IL FT-ULKE BA-TARIH 
                  BT-TARIH T-ADI(1:20) DELIMITED BY SIZE INTO XLS.
           
           MOVE FT-PLNO TO T-KOD.
           CALL 'PLASSEC' USING 1 T-KOD T-ADI 
                  BY CONTENT 0000 0000 0000 0000.
           CANCEL 'PLASSEC'.
           
           STRING XLS(1:116) T-ADI(1:20) DELIMITED BY SIZE INTO XLS.
           
           MOVE FT-KOD TO T-KOD.
           CALL 'KODSEC' USING 1 T-KOD T-ADI 
                  BY CONTENT 0000 0000 0000 0000 1.
           CANCEL 'KODSEC'.
           
           STRING XLS(1:136) T-ADI(1:20) DELIMITED BY SIZE INTO XLS.
           
           WRITE XLS.
           
           MOVE SPACES TO TP-HESAPNO.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
       XLS-OKU.
           READ TEMPFILE NEXT AT END GO XLS-SON.
            
           MOVE TP-BAKIYE TO YZ-TUTAR.
           STRING TP-HESAPNO TP-DPADI(1:20) TP-HESAPADI 
                  TP-YETKILI TP-TEL TP-MAHALLE TP-ILCE 
                  TP-IL TP-ULKE TP-PLADI YZ-TUTAR
               DELIMITED BY SIZE INTO XLS.
           WRITE XLS.
           GO XLS-OKU.
       XLS-SON.
           CLOSE EXLS.
           MOVE T-KEY TO TP-KEY.
           READ TEMPFILE.
           MOVE 'mizan.php' to EXLS-PHP.
           MOVE 'VER˜LER EXCEL E AKTARILIYOR ...' TO ONAY-MESSAGE.
           PERFORM SHOW-LOAD-MESSAGE.
           CALL 'SYSTEM' USING EXLS-PARAM.
           CANCEL 'SYSTEM'.
           PERFORM DISPOSE-LOAD-MESSAGE.
           GO TUS.
           
       EKSTRE-GORUNTU.
           MOVE SPACES TO MAIN-ARGUMENT-VALUE.
           MOVE 0 TO ISLEM.
           MOVE 1 TO GORUNTU.
           STRING BA-TARIH DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  BT-TARIH DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  TP-DEPONO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  TP-HESAPNO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  GORUNTU DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  ISLEM DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
           END-STRING.       
           MOVE 100 TO MAIN-ARGUMENT-SIZE.
           CALL 'EKSTRE' USING MAIN-ARGUMENT.
           CANCEL 'EKSTRE'.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
           INTO BA-TARIH, BT-TARIH, TP-DEPONO, 
                TP-HESAPNO, GORUNTU, ISLEM
           END-UNSTRING.
           IF ISLEM = 1 CLOSE TEMPFILE
                        DELETE FILE TEMPFILE
                        GO BEGIN-PROGRAM.
           GO TUS.
       copy 'onayper.cpy'.
       MIZAN-ARAMA.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                ' F1 ' LINE 37 POSITION 1
                  'S˜C˜LLER' REVERSE LOW
           CALL 'GOLGE' USING BY CONTENT 0012 0002 0029 0078.
           CANCEL 'GOLGE'.
           DISPLAY SCR-FILTRELEME.
           
           MOVE FT-DEPONO TO T-KOD.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV
                  BY CONTENT 0000 0000 0000 0000 1.
           CANCEL 'DEPSEC'.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
 
           MOVE FT-KOD TO T-KOD.
           CALL 'KODSEC' USING 1 T-KOD T-ADI 
                  BY CONTENT 0000 0000 0000 0000 1.
           CANCEL 'KODSEC'.
           DISPLAY T-ADI LINE 26 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.

           MOVE FT-PLNO TO T-KOD.
           CALL 'PLASSEC' USING 1 T-KOD T-ADI 
                  BY CONTENT 0000 0000 0000 0000.
           CANCEL 'PLASSEC'.
           DISPLAY T-ADI LINE 27 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.

           DISPLAY FT-HESAPNO LINE 20 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI
                SC-YETKILI.
           CLOSE SICIL.
           DISPLAY SC-HESAPADI LINE 20 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY FT-MAHALLE LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
            FT-ILCE LINE 23 POSITION 16
            FT-IL LINE 24 POSITION 16
            FT-ULKE LINE 25 POSITION 16.
            
            
       FTARIH-AL.
           ACCEPT SCR-FILTRE-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 PERFORM MIZAN-ARAMA-SON GO TUS.
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
       FHESAPNO-AL.
           ACCEPT FT-HESAPNO LINE 19 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 20 POSITION 16 SIZE 50
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SPACES LINE 21 POSITION 16 SIZE 40
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           IF F = 27 OR F = 52 GO FDEPONO-AL.
           IF F = 1 CALL 'SICPEN' USING FT-HESAPNO
                CANCEL 'SICPEN' GO FHESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY CLOSE SICIL GO FMAHALLE-AL.
           CLOSE SICIL.
           DISPLAY SC-HESAPADI LINE 20 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
       FMAHALLE-AL.
           ACCEPT FT-MAHALLE LINE 22 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO FHESAPNO-AL.
       FILCE-AL.
           ACCEPT FT-ILCE LINE 23 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO FMAHALLE-AL.
       FIL-AL.
           ACCEPT FT-IL LINE 24 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO FILCE-AL.
       FULKE-AL.
           ACCEPT FT-ULKE LINE 25 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO FIL-AL.
       FKOD-AL.
           MOVE FT-KOD TO T-KOD.
           CALL 'KODSEC' USING 0 T-KOD T-ADI 
              BY CONTENT 0016 0026 0035 0031 1.
           CANCEL 'KODSEC'.
           IF T-KOD = -1 GO FULKE-AL.
           MOVE T-KOD TO FT-KOD.
           DISPLAY T-ADI LINE 26 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
            
       FPLAS-AL.
           MOVE FT-PLNO TO T-KOD.
           CALL 'PLASSEC' USING 0 T-KOD T-ADI 
                    BY CONTENT 0016 0027 0035 0032.
           CANCEL 'PLASSEC'.
           IF T-KOD = -1 GO FKOD-AL.
           MOVE T-KOD TO FT-PLNO.
           DISPLAY T-ADI LINE 27 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           CLOSE TEMPFILE.
           DELETE FILE TEMPFILE.
           PERFORM MIZAN-ARAMA-SON.
           GO BEGIN-PROGRAM.
       MIZAN-ARAMA-SON.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       
       MIZAN-YAZDIR.
           MOVE 'M˜ZAN L˜STES˜ YAZDIRILACAK EM˜N M˜S˜N˜Z ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CALL 'CM˜ZAN' USING TSAAT TIP.
           CANCEL 'CM˜ZAN'.
           GO TUS.
       
       CTHO-TUSU.
           INITIALIZE TP-KEY.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE ALL 'ÿ' TO TP-HESAPNO.
           MOVE 9999 TO TP-DEPONO.
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
           MOVE TP-DPADI TO B-DEPONO.
           MOVE TP-HESAPNO TO B-HESAPNO.
           MOVE TP-HESAPADI TO B-HESAPADI.
           MOVE TP-YETKILI TO B-YETKILI.
           MOVE TP-TEL TO BI-TEL.
           MOVE TP-MAHALLE TO B-MAHALLE.
           MOVE TP-IL TO B-IL.
           MOVE TP-ILCE TO B-ILCE.
           MOVE TP-ULKE TO B-ULKE.
           MOVE TP-PLADI TO B-PLADI.
           MOVE TP-KOD TO B-KOD.
           MOVE TP-BAKIYE TO B-TUTAR.
           
           
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
              
            