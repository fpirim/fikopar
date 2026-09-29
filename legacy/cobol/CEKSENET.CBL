       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'CEKSENET'.
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
       copy 'ceksenet.cpy'.
       copy 'carhar.cpy'.
       copy 'ayar.cpy'.
       copy 'makbuz.cpy'.
       copy 'depo.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicil.cpz'.
       copy 'ceksenet.cpz'.
       copy 'carhar.cpz'.
       copy 'ayar.cpz'.
       copy 'makbuz.cpz'.
       copy 'depo.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-VADE.
                    04 TP-VYIL          PIC 9999.
                    04 TP-VAY           PIC 99.
                    04 TP-VGUN          PIC 99.
               03 TP-SIRA           PIC 9(15).
           02 TP-EVRAKNO           PIC X(15).
           02 TP-HESAPNO           PIC X(15).           
           02 TP-HESAPADI          PIC X(50).
           02 TP-YETKILI           PIC X(40).
           02 TP-DEPONO            PIC 9999.
           02 TP-DPADI              PIC X(30).
           02 TP-TARIH.
               03 TP-YIL           PIC 9999.
               03 TP-AY            PIC 99.
               03 TP-GUN           PIC 99.
           02 TP-TUTAR             PIC S9(13)V99.
           02 TP-KHESAPNO          PIC X(15).
           02 TP-KHESAPADI         PIC X(50).
           02 TP-KYETKILI          PIC X(40).
           02 TP-KBANKASEHIR       PIC X(20).
           02 TP-KSUBEILCE         PIC X(20).
           02 TP-KBANKANOTCNO      PIC X(20).
           02 TP-KCEKSENETNO       PIC X(20).
           02 TP-SONUC             PIC 9999.
           
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
           02 CINDX             PIC 9999.
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
               03 FT-CHESAPNO           PIC X(15).
               03 FT-DEPONO         PIC 9999.
               03 FT-SEKILNO        PIC 9999.
               03 FT-ACIKLAMA       PIC X(40).
               03 TT                PIC 9(12)V9999.
               03 T1                PIC 9(12)V9999.
               03 T2                PIC 9(12)V9999.
               
           02 T-TOPLAM            PIC S9(13)V99.
           02 T-TUTAR             PIC S9(13)V99.
           02 ZI-TUTAR          PIC ZZZZZZZZZZZZZ,ZZ.
           02 Z-TUTAR           PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           
               
       01 BILGI.
           02 B-VADE            PIC X(10).
           02 FILLER            PIC X VALUE '∫'.
           02 B-HESAPNO         PIC X(10).
           02 FILLER            PIC X VALUE '∫'.
           02 B-HESAPADI        PIC X(15).
           02 FILLER            PIC X VALUE '∫'.
           02 B-YETKILI         PIC X(15).
           02 FILLER            PIC X VALUE '∫'.
           02 B-DEPONO          PIC X(7).
           02 FILLER            PIC X VALUE '∫'.
           02 B-TARIH           PIC X(10).
           02 FILLER            PIC X VALUE '∫'.
           02 B-KHESAPADI       PIC X(14).
           02 FILLER            PIC X VALUE '∫'.
           02 B-DURUM           PIC X(13).
           02 FILLER            PIC X VALUE '∫'.
           02 B-TUTAR           PIC -.---.---.---,--.
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
              '…' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 2 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              '∫' HIGHLIGHT. 
           02 PIC X(118) USING BASLIK HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ã' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 4 COLUMN 2 BACKGROUND BLUE FOREGROUND WHITE
               PIC X(10) FROM ' VADE' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(15) FROM ' HESAP ADI ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(15) FROM ' YETKòLò ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(10) FROM ' TARòH ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 
           02 PIC X(14) FROM ' K.HESAP ADI ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(13) FROM ' DURUM ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(16) FROM ' TUTAR ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 
           02 LINE 5 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ã' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 34 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ã' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(13) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 35 COLUMN 90 BACKGROUND BLUE FOREGROUND WHITE
              'TOPLAM' HIGHLIGHT.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(101) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-TOPLAM.
           02 LINE 35 COLUMN 104 BACKGROUND BLUE FOREGROUND BROWN
              PIC -.---.---.---,-- USING T-TOPLAM HIGHLIGHT.
       01 SCR-CEKSENET.
           02 LINE 8 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 9 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 
           02 PIC X(75) FROM ' ÄEK/SENET ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 10 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 11 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ EVRAK NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP ADI  [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ YETKòLò    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ DEPO       [' HIGHLIGHT. 02 PIC X(30) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(31) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ TARòH      [  /  /    ]' HIGHLIGHT. 
           02 PIC X(51) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ VADE TARòH [  /  /    ]' HIGHLIGHT. 
           02 PIC X(51) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ TUTAR      [' HIGHLIGHT. 
           02 PIC X(16) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(45) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.HESAP NO [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.HESAP ADI[' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.YETKòLò  [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ BANKA/ûEHòR[' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 24 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ ûUBE/òLÄE  [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 25 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ B.NO/S.NO  [' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 26 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ ÄEK NO/S.NO[' HIGHLIGHT. 02 PIC X(20) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(41) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 27 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 28 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 SCR-TARIH.
               03 LINE 17 COLUMN 16 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING CS-GUN HIGHLIGHT AUTO.
               03 LINE 17 COLUMN 19 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING CS-AY HIGHLIGHT AUTO.
               03 LINE 17 COLUMN 22 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZZZ USING CS-YIL HIGHLIGHT AUTO.
           02 SCR-VADE.
               03 LINE 18 COLUMN 16 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING CS-VGUN HIGHLIGHT AUTO.
               03 LINE 18 COLUMN 19 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZ USING CS-VAY HIGHLIGHT AUTO.
               03 LINE 18 COLUMN 22 BACKGROUND BLACK FOREGROUND WHITE
                    PIC ZZZZ USING CS-VYIL HIGHLIGHT AUTO.
       01 SCR-FILTRELEME.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 
           02 PIC X(75) FROM ' ÄEK/SENET FòLTRELE' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 SCR-FILTRE-TARIH.
               03 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
                 '∫ VADE BAû.  [' HIGHLIGHT. 
               03 PIC ZZ USING BA-GUN HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZ USING BA-AY HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZZZ USING BA-YIL HIGHLIGHT AUTO.
               03 ']' HIGHLIGHT.
               03 PIC X(51) FROM ALL ' '. 03 '∫' HIGHLIGHT.
               03 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
                 '∫ VADE BòTòû [' HIGHLIGHT. 
               03 PIC ZZ USING BT-GUN HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZ USING BT-AY HIGHLIGHT AUTO. 03 '/' HIGHLIGHT.
               03 PIC ZZZZ USING BT-YIL HIGHLIGHT AUTO.
               03 ']' HIGHLIGHT.
               03 PIC X(51) FROM ALL ' '. 03 '∫' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ DEPO NO    [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(31) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ DURUM      [' HIGHLIGHT. 
           02 PIC X(38) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(23) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP NO   [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP ADI  [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ YETKòLò    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 23 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ Ä.HESAP NO [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(46) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 24 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ Ä.HESAP ADI[' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 25 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ Ä.YETKòLò  [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(21) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 26 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(75) FROM ALL ' ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 27 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(75) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-DURUM-MENU.
           02 LINE 19 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(38) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 20 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 1. Portfîyde.'  HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 21 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 2. Bankaya Tahsile/Teminata Verildi.'
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 22 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 3. Bankadan Tahsil Edildi/ôdendi.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 23 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 4. Elden Tahsil Edildi/ôdendi.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 24 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 5. Hesaba Äçkçü(Ciro) Yapçldç.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 25 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 6. Karüçlçksçz/Protestolu Äçktç.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 26 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 7. òade Edildi/Alçndç. ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 27 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 8. Kçsmi Tahsil Edildi/ôdendi. ' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 28 COLUMN 16 BACKGROUND GREEN FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(38) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
               INTO TIP, EVRAKNO, GORUNTU, ISLEM
           END-UNSTRING.
                
           IF GORUNTU = 1 
                CALL 'CSNLOG' USING BY CONTENT EVRAKNO TIP 
                        BY REFERENCE ISLEM
                CANCEL 'CSNLOG'
                MOVE SPACES TO MAIN-ARGUMENT-VALUE
                STRING TIP ';' EVRAKNO 
                  ';' GORUNTU ';' ISLEM ';' 
                  DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
                END-STRING
                MOVE 100 TO MAIN-ARGUMENT-SIZE
                EXIT PROGRAM
                STOP RUN.
           
           EVALUATE TIP
           WHEN 18 MOVE ' ALACAK ÄEKò òûLEMLERò ' TO BASLIK
           WHEN 19 MOVE ' BORÄ ÄEKò òûLEMLERò ' TO BASLIK
           WHEN 20 MOVE ' ALACAK SENETò òûLEMLERò ' TO BASLIK
           WHEN 21 MOVE ' BORÄ SENETò òûLEMLERò ' TO BASLIK
           END-EVALUATE.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
           INITIALIZE FILTRE-VARIABLES.
           ACCEPT BA-TARIH FROM CENTURY-DATE.
           MOVE BA-TARIH TO BT-TARIH.
           MOVE 1 TO BA-AY BA-GUN.
           MOVE 31 TO BT-GUN.
           MOVE 12 TO BT-AY.
           MOVE 0 TO CINDX.
           
       BEGIN-PROGRAM.
           PERFORM VARYING CIZ FROM 4 BY 1 UNTIL CIZ > 35
                IF CIZ < 35
                 DISPLAY '∫' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 10 '∫' SPACES SIZE 10 '∫' 
                     SPACES SIZE 15 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 7 '∫' SPACES SIZE 10 '∫' 
                     SPACES SIZE 14 '∫' SPACES SIZE 13 '∫' 
                     SPACES SIZE 16 '∫'
                ELSE
                DISPLAY '∫' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    SPACES SIZE 101 '∫' SPACES SIZE 16 '∫'
                END-IF
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
                ' F2 ' LINE 37 POSITION 1 'YENò ÄEK/SENET' REVERSE LOW
                ' F3 ' 'AYARLAR' REVERSE LOW
                ' F4 ' 'FòLTRE' REVERSE LOW
                ' F8 ' 'ÄEK/SENET LòSTE YAZDIR' REVERSE LOW
                ' F10 ' 'ÄEK/SENET SòL' REVERSE LOW.
           
           
           COMPUTE T1 = BA-GUN + BA-AY * 30,41 + BA-YIL * 365.
           COMPUTE T2 = BT-GUN + BT-AY * 30,41 + BT-YIL * 365.
           
           OPEN INPUT CEKSENET CEKSENETLOG MAKBUZ DEPO SICIL.

           MOVE ZEROS TO T-TOPLAM.
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
           
           MOVE BA-TARIH TO CS-VADE.
           START CEKSENET KEY IS NOT LESS THAN CS-VADE
            INVALID KEY GO CEKSENET-OKU-SON.
       CEKSENET-OKU.
           READ CEKSENET NEXT AT END GO CEKSENET-OKU-SON.
           IF CS-TIP NOT = TIP GO CEKSENET-OKU.
           COMPUTE TT =  CS-VGUN + CS-VAY * 30,41 + CS-VYIL * 365.
           IF TT > T2 GO CEKSENET-OKU-SON.
           IF FT-HESAPNO NOT = SPACES AND CS-HESAPNO NOT = FT-HESAPNO
                GO CEKSENET-OKU.
           IF FT-DEPONO NOT = ZEROS AND CS-DEPONO NOT = FT-DEPONO
                GO CEKSENET-OKU.
           MOVE CS-EVRAKNO TO CSL-EVRAKNO.
           MOVE ZEROS TO TP-TUTAR.
           PERFORM ALT-TOPLAM-AL THRU ALT-TOPLAM-AL-SON.
           IF FT-CHESAPNO NOT = SPACES AND MK-HESAPNO NOT = FT-CHESAPNO
                    GO CEKSENET-OKU.
           IF CSL-SONUC NOT = CINDX GO CEKSENET-OKU.
           MOVE CS-VADE TO TP-VADE.
           MOVE CS-EVRAKNO TO TP-EVRAKNO.
           MOVE CS-HESAPNO TO TP-HESAPNO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           MOVE SC-HESAPADI TO TP-HESAPADI.
           MOVE SC-YETKILI TO TP-YETKILI.
           MOVE CS-DEPONO TO TP-DEPONO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE DP-ADI TO TP-DPADI.
           
           MOVE CS-TARIH TO TP-TARIH.
           MOVE CS-KHESAPNO TO TP-KHESAPNO.
           MOVE CS-KHESAPADI TO TP-KHESAPADI.
           MOVE CS-KYETKILI TO TP-KYETKILI.
           MOVE CS-KBANKASEHIR TO TP-KBANKASEHIR.
           MOVE CS-KSUBEILCE TO TP-KSUBEILCE.
           MOVE CS-KBANKANOTCNO TO TP-KBANKANOTCNO.
           MOVE CS-KCEKSENETNO TO TP-KCEKSENETNO.
           MOVE CS-EVRAKNO TO CSL-EVRAKNO.
          
           ADD TP-TUTAR TO T-TOPLAM.
           MOVE 1 TO TP-SIRA.
       TEMP-YAZ1.
           WRITE TP-KAYIT INVALID KEY ADD 1 TO TP-SIRA GO TEMP-YAZ1.
           GO CEKSENET-OKU.
           
       ALT-TOPLAM-AL.
           MOVE ZEROS TO CSL-SIRA.
           MOVE ZEROS TO CSL-TARIH TP-TUTAR.
           MOVE TIP TO CSL-CTIP.
           MOVE CS-EVRAKNO TO CSL-EVRAKNO.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY.
      *      INVALID KEY MOVE ZEROS TO CSL-SONUC GO ALT-TOPLAM-AL-SON.
       ALT-TOPLAM-OKU.
           READ CEKSENETLOG NEXT AT END
            READ CEKSENETLOG PREVIOUS GO ALT-TOPLAM-AL-SON.
           IF CSL-EVRAKNO NOT = CS-EVRAKNO OR CSL-CTIP NOT = TIP
            READ CEKSENETLOG PREVIOUS AT END CONTINUE END-READ
              GO ALT-TOPLAM-AL-SON.
            
           MOVE CSL-MAKBUZNO TO MK-EVRAKNO.
           MOVE CSL-TIP TO MK-TIP.
           READ MAKBUZ INVALID KEY MOVE ZEROS TO MK-TUTAR
            IF (TIP = 18 OR TIP = 20) MOVE 16 TO MK-TIP
                  ELSE MOVE 17 TO MK-TIP END-IF
            MOVE ZEROS TO MK-TUTAR.
              
           IF MK-TIP = 16 AND (TIP = 18 OR TIP = 20)
                COMPUTE T-TUTAR = MK-TUTAR * -1.
           IF MK-TIP = 17 AND (TIP = 18 OR TIP = 20)
                MOVE MK-TUTAR TO T-TUTAR.
           IF MK-TIP = 16 AND (TIP = 19 OR TIP = 21)
                MOVE MK-TUTAR TO T-TUTAR.
           IF MK-TIP = 17 AND (TIP = 19 OR TIP = 21)
                COMPUTE T-TUTAR = MK-TUTAR * -1.
          
           EVALUATE CSL-SONUC
             WHEN 5 IF T-TUTAR > 0 
                        ADD T-TUTAR TO TP-TUTAR
                    END-IF
             WHEN 7 IF MK-HESAPNO = CS-HESAPNO
                        ADD T-TUTAR TO TP-TUTAR
                    END-IF
             WHEN OTHER MOVE ZEROS TO TP-TUTAR
           END-EVALUATE.
           GO ALT-TOPLAM-OKU.
       ALT-TOPLAM-AL-SON.
           MOVE CSL-SONUC TO TP-SONUC.
           EVALUATE CSL-SONUC
           WHEN 5 WHEN 7
             MOVE T-TOPLAM TO TP-TUTAR
           WHEN OTHER
             MOVE CS-TUTAR TO TP-TUTAR
           END-EVALUATE.
           
           
           
       CEKSENET-OKU-SON.
           CLOSE CEKSENET TEMPFILE CEKSENETLOG MAKBUZ DEPO SICIL.
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
               DISPLAY '∫' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 10 '∫' SPACES SIZE 10 '∫' 
                     SPACES SIZE 15 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 7 '∫' SPACES SIZE 10 '∫' 
                     SPACES SIZE 14 '∫' SPACES SIZE 13 '∫' 
                     SPACES SIZE 16 '∫'
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
             MOVE 0 TO ISLEM
            CALL 'CSNLOG' USING BY CONTENT TP-EVRAKNO TIP 
                        BY REFERENCE ISLEM
            CANCEL 'CSNLOG'
            IF ISLEM = 1 CLOSE TEMPFILE DELETE FILE TEMPFILE
               GO BEGIN-PROGRAM END-IF
           END-IF.
           IF F = 8 AND KILIT = 0 GO CEKSENET-YAZDIR.
           IF F = 3 CALL 'AYAR' USING TIP CANCEL 'AYAR' GO TUS.
           IF F = 10 AND KILIT = 0 GO CEKSENET-SIL.
           IF F = 2 GO YENI-CEKSENET.
           IF F = 4 GO CEKSENET-ARAMA.
           GO TUS.
       copy 'onayper.cpy'.
       CEKSENET-ARAMA.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                ' F1 ' LINE 37 POSITION 1
                  'SòCòLLER' REVERSE LOW
           CALL 'GOLGE' USING BY CONTENT 0012 0002 0027 0078.
           CANCEL 'GOLGE'.
           DISPLAY SCR-FILTRELEME.
           MOVE FT-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0018 0030 0025 1.
           DISPLAY T-ADI LINE 18 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           PERFORM FDURUM-AL-SON.
           DISPLAY FT-HESAPNO LINE 20 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           DISPLAY FT-HESAPNO LINE 20 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           MOVE FT-CHESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           DISPLAY FT-CHESAPNO LINE 23 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-HESAPADI LINE 24 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 25 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           CLOSE SICIL.
       FTARIH-AL.
           ACCEPT SCR-FILTRE-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 PERFORM CEKSENET-ARAMA-SON GO TUS.
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
       FDURUM-AL.
           MOVE WCB TO NESTED-WCB (3).
           DISPLAY NESTED-WCB (3)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0019 0016 0028 0055.
           CANCEL 'GOLGE'.
       FDURUM-MENU-AL.           
           DISPLAY SCR-DURUM-MENU.
           EVALUATE CINDX
           WHEN 0
            DISPLAY ' 1. Portfîyde.' SIZE 38
            LINE 20 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 1
            DISPLAY ' 2. Bankaya Tahsile/Teminata Verildi.' SIZE 38
            LINE 21 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 2
            DISPLAY ' 3. Bankadan Tahsil Edildi/ôdendi.' SIZE 38
            LINE 22 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 3
            DISPLAY ' 4. Elden Tahsil Edildi/ôdendi.' SIZE 38
            LINE 23 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 4
            DISPLAY ' 5. Hesaba Äçkçü(Ciro) Yapçldç.' SIZE 38
            LINE 24 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 5
            DISPLAY ' 6. Karüçlçksçz/Protestolu Äçktç.' SIZE 38
            LINE 25 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 6
            DISPLAY ' 7. òade Edildi/Alçndç. ' SIZE 38
            LINE 26 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 7
            DISPLAY ' 8. Kçsmi Tahsil Edildi/ôdendi. ' SIZE 38
            LINE 27 POSITION 17 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           END-EVALUATE.
       FDURUM-TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 IF CINDX = 0 MOVE 7 TO CINDX ELSE
                SUBTRACT 1 FROM CINDX END-IF GO FDURUM-MENU-AL.
           IF F = 53 IF CINDX = 7 MOVE 0 TO CINDX ELSE
                ADD 1 TO CINDX END-IF GO FDURUM-MENU-AL.
           IF F = 27 PERFORM FDURUM-MENU-AL-SON 
                       PERFORM FDURUM-AL-SON GO FDEPONO-AL.
           IF F = 13 GO FDURUM-MENU-AL-SON.
           GO FDURUM-TUS.
       FDURUM-MENU-AL-SON.
           MOVE WCB TO NESTED-WCB (3).
           DISPLAY NESTED-WCB (3)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       FDURUM-AL-SON.
           EVALUATE CINDX
           WHEN 0
            DISPLAY '1. Portfîyde.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 1
            DISPLAY '2. Bankaya Tahsile/Teminata Verildi.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 2
            DISPLAY '3. Bankadan Tahsil Edildi/ôdendi.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 3
            DISPLAY '4. Elden Tahsil Edildi/ôdendi.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 4
            DISPLAY '5. Hesaba Äçkçü(Ciro) Yapçldç.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 5
            DISPLAY '6. Karüçlçksçz/Protestolu Äçktç.' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 6
            DISPLAY '7. òade Edildi/Alçndç. ' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           WHEN 7
            DISPLAY '8. Kçsmi Tahsil Edildi/ôdendi. ' SIZE 38
             LINE 19 POSITION 16 CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN' 
           END-EVALUATE.
       
           
       FHESAPNO-AL.
           ACCEPT FT-HESAPNO LINE 20 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 21 POSITION 16 SIZE 50
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SPACES LINE 22 POSITION 16 SIZE 40
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           IF F = 27 OR F = 52 GO FDURUM-AL.
           IF F = 1 CALL 'SICPEN' USING FT-HESAPNO
                CANCEL 'SICPEN' GO FHESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE FT-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           CLOSE SICIL.
        FCHESAPNO-AL.
           ACCEPT FT-CHESAPNO LINE 23 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 24 POSITION 16 SIZE 50
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SPACES LINE 25 POSITION 16 SIZE 40
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           IF F = 27 OR F = 52 GO FHESAPNO-AL.
           IF F = 1 CALL 'SICPEN' USING FT-CHESAPNO
                CANCEL 'SICPEN' GO FCHESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE FT-CHESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           DISPLAY SC-HESAPADI LINE 24 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 25 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           CLOSE TEMPFILE SICIL.
           DELETE FILE TEMPFILE.
           PERFORM CEKSENET-ARAMA-SON.
           GO BEGIN-PROGRAM.
       CEKSENET-ARAMA-SON.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       
 
       CEKSENET-SIL.
           MOVE 'ÄEK/SENET SòLòNECEK EMòN MòSòNòZ ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           PERFORM CEKSENETLOG-KONTROL THRU CEKSENETLOG-KONTROL-SON.
           IF F > 1
            MOVE 'PORTFôYDE OLMAYAN EVRAK SòLòNEMEZ.' TO ONAY-MESSAGE
            PERFORM WARN-MESSAGE
            GO TUS.
           MOVE TP-HESAPNO TO TCH-HESAPNO.
           MOVE TP-TARIH TO TCH-TARIH.
           MOVE TP-DEPONO TO TCH-DEPONO.
           MOVE TP-EVRAKNO TO TCH-EVRAKNO.
           MOVE TIP TO TCH-TIP.
           PERFORM CARHAR-SIL.
           OPEN I-O CEKSENET CEKSENETLOG.
           MOVE TP-EVRAKNO TO CS-EVRAKNO.
           MOVE TIP TO CSL-CTIP CS-TIP.
           PERFORM CEKSENETLOG-SIL THRU CEKSENETLOG-SIL-SON.
           READ CEKSENET INVALID KEY CONTINUE
            NOT INVALID KEY PERFORM DELETE-CEKSENET
           END-READ.
           CLOSE CEKSENET CEKSENETLOG TEMPFILE.
           DELETE FILE TEMPFILE.
           GO BEGIN-PROGRAM.
       CEKSENETLOG-KONTROL.    
           OPEN INPUT CEKSENETLOG.
           MOVE ZEROS TO F.
           MOVE TP-EVRAKNO TO CSL-EVRAKNO.
           MOVE TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS EQUAL TO CSL-EVRAKNO
            INVALID KEY GO CEKSENETLOG-KONTROL-SON.
       CEKSENETLOG-KONTROL-OKU.
           READ CEKSENETLOG NEXT AT END GO CEKSENETLOG-KONTROL-SON.
           IF CSL-EVRAKNO NOT = TP-EVRAKNO OR CSL-CTIP NOT = TIP
              GO CEKSENETLOG-KONTROL-OKU.
           ADD 1 TO F.
           IF F > 1 GO CEKSENETLOG-KONTROL-SON.
           GO CEKSENETLOG-KONTROL-OKU.
       CEKSENETLOG-KONTROL-SON.
           CLOSE CEKSENETLOG.
           
       CEKSENETLOG-SIL.
           MOVE ZEROS TO CSL-SIRA.
           MOVE TP-EVRAKNO TO CSL-EVRAKNO.
           MOVE TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY
            INVALID KEY GO CEKSENETLOG-SIL-SON.
       CEKSENETLOG-OKU.
           READ CEKSENETLOG NEXT AT END GO CEKSENETLOG-SIL-SON.
           IF CSL-EVRAKNO NOT = TP-EVRAKNO OR TIP NOT = CSL-CTIP
             GO CEKSENETLOG-SIL-SON.
           PERFORM DELETE-CEKSENETLOG.
           GO CEKSENETLOG-OKU.
       CEKSENETLOG-SIL-SON.
       YENI-CEKSENET.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.

           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                ' F1 ' LINE 37 POSITION 1
                  'SòCòLLER' REVERSE LOW
           CALL 'GOLGE' USING BY CONTENT 0009 0002 0028 0078.
           CANCEL 'GOLGE'.
           INITIALIZE CS-KAYIT.
           ACCEPT CS-TARIH FROM CENTURY-DATE.
           DISPLAY SCR-CEKSENET.
           DISPLAY '(YENò KAYIT)' LINE 12 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
      *     OPEN INPUT AYAR.
      *     MOVE TIP TO AY-TIP.
      *     READ AYAR.
      *     MOVE AY-EVRAKNO TO CS-EVRAKNO.
      *     CLOSE AYAR.
      * EVRAKNO-AL.
      *     ACCEPT CS-EVRAKNO LINE 12 POSITION 16 UPDATE
      *      NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
      *     ACCEPT F FROM ESCAPE KEY.
      *     IF F = 27 PERFORM YENI-CEKSENET-SON GO TUS.
      *     OPEN INPUT CEKSENET.
      *     MOVE TIP TO CS-TIP.
      *     READ CEKSENET INVALID KEY CLOSE CEKSENET GO HESAPNO-AL.
      *     CLOSE CEKSENET.
      *     GO EVRAKNO-AL.
       HESAPNO-AL.
           ACCEPT CS-HESAPNO LINE 13 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 OR F = 52 PERFORM YENI-CEKSENET-SON GO TUS.
           IF F = 1 CALL 'SICPEN' USING CS-HESAPNO
                CANCEL 'SICPEN' GO HESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE CS-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY CLOSE SICIL GO HESAPNO-AL.
		   IF SC-PLKOD = 5 THEN
		    MOVE 'HESAP AKTòF DE¶òL !!!' TO
                ONAY-MESSAGE
            PERFORM WARN-MESSAGE
			CLOSE SICIL GO HESAPNO-AL.
           
           DISPLAY SC-HESAPADI LINE 14 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SC-YETKILI LINE 15 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           CLOSE SICIL.
       DEPONO-AL.
           MOVE CS-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0016 0030 0025 0.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO HESAPNO-AL.
           MOVE T-KOD TO CS-DEPONO.
           DISPLAY T-ADI LINE 16 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.      
       TARIH-AL.
           DISPLAY SCR-TARIH.
           ACCEPT SCR-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO DEPONO-AL.
       VADE-AL.
           DISPLAY SCR-VADE.
           ACCEPT SCR-VADE.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO TARIH-AL.
       TUTAR-AL.
           MOVE CS-TUTAR TO ZI-TUTAR.
           ACCEPT ZI-TUTAR LINE 19 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           MOVE ZI-TUTAR TO CS-TUTAR Z-TUTAR.
           DISPLAY Z-TUTAR LINE 19 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 OR F = 52 GO VADE-AL.
       KHESAPNO-AL.
           ACCEPT CS-KHESAPNO LINE 20 POSITION 16 UPDATE
            NO BEEP CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 OR F = 52 GO TUTAR-AL.
           IF F = 1 CALL 'SICPEN' USING CS-KHESAPNO
                CANCEL 'SICPEN' GO KHESAPNO-AL.
           OPEN INPUT SICIL.
           MOVE CS-KHESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           MOVE SC-HESAPADI TO CS-KHESAPADI.
           MOVE SC-YETKILI TO CS-KYETKILI.
           DISPLAY SC-HESAPADI LINE 21 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           DISPLAY SC-YETKILI LINE 22 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           CLOSE SICIL.
        KHESAPADI-AL.
           ACCEPT CS-KHESAPADI LINE 21 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KHESAPNO-AL.     
        KYETKILI-AL.
           ACCEPT CS-KYETKILI LINE 22 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KHESAPADI-AL.     
        KBANKASEHIR-AL.
           ACCEPT CS-KBANKASEHIR LINE 23 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KYETKILI-AL.     
        KSUBEILCE-AL.
           ACCEPT CS-KSUBEILCE LINE 24 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KBANKASEHIR-AL.     
        KBANKANOTCNO-AL.
           ACCEPT CS-KBANKANOTCNO LINE 25 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KSUBEILCE-AL.     
        KCEKSENETNO-AL.
           ACCEPT CS-KCEKSENETNO LINE 26 POSITION 16 NO BEEP UPDATE
            CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO KBANKANOTCNO-AL.     
           
           MOVE TIP TO DX-TIP.
           MOVE CS-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           IF AY-EVRAKNO = SPACES GO KCEKSENETNO-AL.
           MOVE AY-EVRAKNO TO CS-EVRAKNO.
           
           MOVE TIP TO CS-TIP.
           OPEN I-O CEKSENET CEKSENETLOG.
           MOVE CS-EVRAKNO TO CSL-EVRAKNO.
           MOVE CS-TARIH TO CSL-TARIH.
           MOVE SPACES TO CSL-MAKBUZNO.
           MOVE ZEROS TO CSL-TIP.
           MOVE ZEROS TO CSL-SONUC.
           MOVE TIP TO CSL-CTIP.
           MOVE 1 TO CSL-SIRA.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           PERFORM WRITE-CEKSENET.
           PERFORM WRITE-CEKSENETLOG.
           CLOSE CEKSENET CEKSENETLOG TEMPFILE.
           MOVE CS-HESAPNO TO TCH-HESAPNO.
           MOVE CS-DEPONO TO TCH-DEPONO.
           MOVE CS-TIP TO TCH-TIP.
           MOVE CS-EVRAKNO TO TCH-EVRAKNO.
           MOVE CS-TARIH TO TCH-TARIH.
           MOVE ZEROS TO TCH-BORC TCH-ALACAK.
           MOVE AY-ACIKLAMA TO TCH-ACIKLAMA.
           EVALUATE CS-TIP
             WHEN 18 WHEN 20 MOVE CS-TUTAR TO TCH-ALACAK
             WHEN 19 WHEN 21 MOVE CS-TUTAR TO TCH-BORC
           END-EVALUATE.
           STRING CS-VGUN '/' CS-VAY '/' CS-VYIL
                DELIMITED BY SIZE INTO TCH-VADE.
           PERFORM CARHAR-YAZ THRU CARHAR-YAZ-SON.
           DELETE FILE TEMPFILE.
           OPEN I-O AYAR.
           MOVE TIP TO DX-TIP.
           MOVE CS-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-ARTIR.
           CLOSE AYAR.
           DISPLAY CS-EVRAKNO LINE 12 POSITION 16
            CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           ACCEPT DUR NO BEEP LINE 37 POSITION 1.
           PERFORM YENI-CEKSENET-SON.
           GO BEGIN-PROGRAM.
       copy 'trim.per'.
       copy 'numara.per'.
       copy 'chyaz.per'.    
       
       YENI-CEKSENET-SON.    
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           
       CEKSENET-YAZDIR.
           MOVE 'ÄEK/SENET LòSTESò YAZDIRILACAK EMòN MòSòNòZ ?' TO 
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CALL 'CCSNLIS' USING TSAAT TIP.
           CANCEL 'CCSNLIS'.
           GO TUS.
       
       CTHO-TUSU.
           MOVE ZEROS TO TP-KEY.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 99999999 TO TP-VADE.
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
           STRING TP-VGUN '/' TP-VAY '/' TP-VYIL
            DELIMITED BY SIZE INTO B-VADE.
           MOVE TP-HESAPNO TO B-HESAPNO.
           MOVE TP-HESAPADI TO B-HESAPADI.
           MOVE TP-YETKILI TO B-YETKILI.
           MOVE TP-DPADI TO B-DEPONO.
           STRING TP-GUN '/' TP-AY '/' TP-YIL
            DELIMITED BY SIZE INTO B-TARIH.
           MOVE TP-KHESAPADI TO B-KHESAPADI.
           MOVE SPACES TO B-DURUM.
           EVALUATE TP-SONUC
             WHEN 0 MOVE 'Portfîyde' TO B-DURUM
             WHEN 1 MOVE 'Bankaya Verme' TO B-DURUM
             WHEN 2 MOVE 'Bankadan Tahsil' TO B-DURUM
             WHEN 3 MOVE 'Elden Tahsil' TO B-DURUM
             WHEN 4 MOVE 'Ciro Edildi' TO B-DURUM
             WHEN 5 MOVE 'Protesto Edildi' TO B-DURUM
             WHEN 6 MOVE 'òade Edildi' TO B-DURUM
             WHEN 7 MOVE 'Kçsmi ôdeme' TO B-DURUM
           END-EVALUATE.
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
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==CEKSENET==
                                  "%T%"  BY "CEKSENET"
                                 ==%R%== BY ==CS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==CEKSENETLOG==
                                  "%T%"  BY "CEKSENETLOG"
                                 ==%R%== BY ==CSL-KAYIT==.

           