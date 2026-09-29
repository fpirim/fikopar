       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'CSNLOG'.
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
       copy 'makbuz.cpy'.
       copy 'ayar.cpy'.
       copy 'carhar.cpy'.
       copy 'kasa.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicil.cpz'.
       copy 'ceksenet.cpz'.
       copy 'makbuz.cpz'.
       copy 'ayar.cpz'.
       copy 'carhar.cpz'.
       copy 'kasa.cpz'.
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
           02 ILK                   PIC 9999 VALUE 23.
           02 SON                   PIC 9999 VALUE 31. 
           02 CINDX                 PIC 9.
           02 T-MASRAF          PIC 9(13)V99.
           02 TT                PIC 9(12)V9999.
           02 TSON              PIC 9(12)V9999.
           02 TSONSONUC         PIC 9999.
           02 TSONHESAPNO       PIC X(15).
           02 TSONOPTIME        PIC 9(16).
           02 T-HESAPNO         PIC X(15).
           02 T-ERROR           PIC 9.
           02 T-ACIKLAMA        PIC X(60).
           02 Z-TUTAR           PIC Z.ZZZ.ZZZ.ZZZ,ZZ.
           02 T-ODEME           PIC 9(13)V99.
           02 T-TOPLAM          PIC S9(13)V99.
           02 T-TUTAR           PIC S9(13)V99.
           02 TX-TARIH.
                03 TX-YIL        PIC 9999.
                03 TX-AY         PIC 99.
                03 TX-GUN        PIC 99.
           02 TX-GIDER           PIC S9(13)V99.
           02 TX-GELIR           PIC S9(13)V99.
           02 KS-FLAG           PIC 9.
           
           02 T-TARIH.
                03 T-YIL        PIC 9999.
                03 T-AY         PIC 99.
                03 T-GUN        PIC 99.
           
       01 BILGI.
           02 B-TARIH           PIC X(10).
           02 FILLER            PIC X VALUE '∫'.
           02 B-EVRAKNO         PIC X(15).
           02 FILLER            PIC X VALUE '∫'.
           02 B-HESAPNO         PIC X(10).
           02 FILLER            PIC X VALUE '∫'.
           02 B-HESAPADI        PIC X(15).
           02 FILLER            PIC X VALUE '∫'.
           02 B-YETKILI         PIC X(15).
           02 FILLER            PIC X VALUE '∫'.
           02 B-ACIKLAMA        PIC X(17).
           02 FILLER            PIC X VALUE '∫'.
           02 B-TUTAR           PIC -.---.---.---,--.
           02 FILLER            PIC X VALUE '∫'.
           02 B-DURUM           PIC X(9).
           
       copy 'chyaz.var'.
       copy 'numara.var'.
       copy 'trim.var'.
       copy 'onayvar.cpy'.
       copy 'status.cpy'.
       copy 'win.cpy'.
       LINKAGE SECTION.
       77 G-EVRAKNO         PIC X(15).
       77 G-TIP             PIC 99.
       77 ISLEM             PIC 9.
       SCREEN SECTION.
       01 SCR.
           02 LINE 8 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 9 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 
           02 PIC X(114) FROM ' ÄEK/SENET GôRöNTö ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 10 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 11 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(114) FROM ALL ' ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ EVRAK NO   [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(15) USING CS-EVRAKNO HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(40) FROM ALL ' '.
           02 'DEPO       [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(30) USING T-ADI HIGHLIGHT.
           02 FOREGROUND WHITE ']  ∫' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP NO   [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(15) USING CS-HESAPNO HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(40) FROM ALL ' '.
           02 'TARòH      [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC ZZ USING CS-GUN HIGHLIGHT. 
           02 FOREGROUND WHITE '/' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZ USING CS-AY HIGHLIGHT. 
           02 FOREGROUND WHITE  '/' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZZZ USING CS-YIL HIGHLIGHT. 
           02 FOREGROUND WHITE  ']' HIGHLIGHT.
           02 PIC X(22) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ HESAP ADI  [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(50) USING SC-HESAPADI HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(5) FROM ALL ' '.
           02 'VADE       [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC ZZ USING CS-VGUN HIGHLIGHT. 
           02 FOREGROUND WHITE '/' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZ USING CS-VAY HIGHLIGHT. 
           02 FOREGROUND WHITE  '/' HIGHLIGHT.
           02 FOREGROUND BROWN PIC ZZZZ USING CS-VYIL HIGHLIGHT. 
           02 FOREGROUND WHITE  ']' HIGHLIGHT.
           02 PIC X(22) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ YETKòLò    [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(40) USING SC-YETKILI HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(15) FROM ALL ' '.
           02 'TUTAR      [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC Z.ZZZ.ZZZ.ZZZ,ZZ USING CS-TUTAR
              HIGHLIGHT.
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(16) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           
           02 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.HESAP NO [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(15) USING CS-KHESAPNO HIGHLIGHT.
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(40) FROM ALL ' '.
           02 'BANKA/ûEHòR[' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(20) USING CS-KBANKASEHIR 
              HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(12) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           
           02 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.HESAP ADI[' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(50) USING CS-KHESAPADI HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(5) FROM ALL ' '.
           02 'ûUBE/òLÄE  [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(20) USING CS-KSUBEILCE HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(12) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ K.YETKòLò  [' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(40) USING CS-KYETKILI HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(15) FROM ALL ' '.
           02 'B.NO/S.NO  [' HIGHLIGHT.
           02 FOREGROUND BROWN PIC X(20) USING CS-KBANKANOTCNO 
                HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(12) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫ ÄEK NO/S.NO[' HIGHLIGHT. 
           02 FOREGROUND BROWN PIC X(20) USING CS-KCEKSENETNO HIGHLIGHT. 
           02 FOREGROUND WHITE ']' HIGHLIGHT.
           02 PIC X(80) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 20 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(17) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'À' HIGHLIGHT. 02 PIC X(9) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 21 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '∫' HIGHLIGHT. 02 PIC X(10) FROM ' TARòH ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(15) FROM 'MAK.NO.' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(10) FROM ' HESAP NO' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(15) FROM ' HESAP ADI ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(15) FROM ' YETKòLò' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(17) FROM ' AÄIKLAMA' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(16) FROM ' TUTAR ' HIGHLIGHT.
           02 '∫' HIGHLIGHT. 02 PIC X(9) FROM ' DURUM ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 22 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(17) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(9) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 LINE 32 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ã' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(17) FROM ALL 'Õ' HIGHLIGHT.
           02 'Œ' HIGHLIGHT. 02 PIC X(16) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(9) FROM ALL 'Õ' HIGHLIGHT.
           02 'π' HIGHLIGHT.
           02 SCR-ALT-TOPLAM.
               03 LINE 33 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
                 '∫' HIGHLIGHT. 03 PIC X(75) FROM ALL ' ' HIGHLIGHT.
               03 PIC X(12) FROM 'KALAN TUTAR' HIGHLIGHT.   
               03 '∫' HIGHLIGHT.
               03 PIC X(10) FROM ALL ' '.               
               03 FOREGROUND BROWN 
                  PIC -.---.---.---,-- USING T-TOPLAM HIGHLIGHT.
               03 FOREGROUND WHITE '∫' HIGHLIGHT.
           02 LINE 34 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(87) FROM ALL 'Õ' HIGHLIGHT.
           02 ' ' HIGHLIGHT. 02 PIC X(26) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-GIRISCIKIS-MENU.
           02 LINE 28 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(38) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 29 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 1. Bankaya Tahsile/Teminata Verildi.'
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 30 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 2. Bankadan Tahsil Edildi/ôdendi.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 31 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 3. Elden Tahsil Edildi/ôdendi.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 32 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 4. Hesaba Äçkçü(Ciro) Yapçldç.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 33 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 5. Karüçlçksçz/Protestolu Äçktç.' 
                HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 34 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 PIC X(38) FROM ' 6. òade Edildi/Alçndç. ' HIGHLIGHT.
           02 '∫' HIGHLIGHT.
           02 LINE 35 COLUMN 2 BACKGROUND RED FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(38) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-MASRAF.
           02 LINE 32 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(36) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 33 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 ' òülem Tarihi  [' HIGHLIGHT.
           02 PIC ZZ USING T-GUN HIGHLIGHT AUTO.
           02 '/' HIGHLIGHT.
           02 PIC ZZ USING T-AY HIGHLIGHT AUTO.
           02 '/' HIGHLIGHT.
           02 PIC ZZZZ USING T-YIL HIGHLIGHT.
           02 ']         ∫' HIGHLIGHT.
           02 LINE 34 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 ' òülem Masrafç [' HIGHLIGHT.
           02 PIC Z.ZZZ.ZZZ.ZZZ,ZZ USING T-MASRAF HIGHLIGHT.
           02 ']   ∫' HIGHLIGHT.
           02 LINE 35 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(36) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-IADE.
           02 LINE 32 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(36) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 LINE 33 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 ' òülem Tarihi  [' HIGHLIGHT.
           02 PIC ZZ USING T-GUN HIGHLIGHT AUTO.
           02 '/' HIGHLIGHT.
           02 PIC ZZ USING T-AY HIGHLIGHT AUTO.
           02 '/' HIGHLIGHT.
           02 PIC ZZZZ USING T-YIL HIGHLIGHT.
           02 ']         ∫' HIGHLIGHT.
           02 LINE 34 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(36) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-CIKIS.
           02 LINE 29 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(68) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 SCR-CIKIS-TARIH.
                03 LINE 30 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
                    '∫' HIGHLIGHT.
                03 ' òülem Tarihi  [' HIGHLIGHT.
                03 PIC ZZ USING T-GUN HIGHLIGHT AUTO.
                03 '/' HIGHLIGHT.
                03 PIC ZZ USING T-AY HIGHLIGHT AUTO.
                03 '/' HIGHLIGHT.
                03 PIC ZZZZ USING T-YIL HIGHLIGHT.
                03 ']' HIGHLIGHT.
                03 PIC X(41) FROM ALL ' '. 03 '∫' HIGHLIGHT.
           02 SCR-CIKIS-HESAPNO.
                03 LINE 31 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
                    '∫' HIGHLIGHT.
                03 ' Hesap No      [' HIGHLIGHT.
                03 PIC X(15) USING SC-HESAPNO HIGHLIGHT.
                03 ']' HIGHLIGHT.
                03 PIC X(36) FROM ALL ' '. 03 '∫' HIGHLIGHT.
           02 LINE 32 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 ' Hesap Adç     [' HIGHLIGHT.
           02 PIC X(50) USING SC-HESAPADI HIGHLIGHT.
           02 '] ∫' HIGHLIGHT.
           02 LINE 33 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '∫' HIGHLIGHT.
           02 ' Yekili        [' HIGHLIGHT.
           02 PIC X(40) USING SC-YETKILI HIGHLIGHT.
           02 ']' HIGHLIGHT.
           02 PIC X(11) FROM ALL ' '. 02 '∫' HIGHLIGHT.
           02 LINE 34 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(68) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       01 SCR-ODEME.
           02 LINE 29 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '…' HIGHLIGHT. 02 PIC X(68) FROM ALL 'Õ' HIGHLIGHT.
           02 'ª' HIGHLIGHT.
           02 SCR-ODEME-TARIH.
                03 LINE 30 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
                    '∫' HIGHLIGHT.
                03 ' òülem Tarihi  [' HIGHLIGHT.
                03 PIC ZZ USING T-GUN HIGHLIGHT AUTO.
                03 '/' HIGHLIGHT.
                03 PIC ZZ USING T-AY HIGHLIGHT AUTO.
                03 '/' HIGHLIGHT.
                03 PIC ZZZZ USING T-YIL HIGHLIGHT.
                03 ']' HIGHLIGHT.
                03 PIC X(41) FROM ALL ' '. 03 '∫' HIGHLIGHT.
           02 SCR-ODEME-TUTAR.
                03 LINE 31 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
                    '∫' HIGHLIGHT.
                03 ' ôdenen Tutar  [' HIGHLIGHT.
                03 PIC Z.ZZZ.ZZZ.ZZZ,ZZ USING T-ODEME HIGHLIGHT.
                03 ']' HIGHLIGHT. 
                03 PIC X(35) FROM ALL ' '. 03 '∫' HIGHLIGHT.
           02 LINE 32 COLUMN 42 BACKGROUND RED FOREGROUND WHITE
              '»' HIGHLIGHT. 02 PIC X(68) FROM ALL 'Õ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING G-EVRAKNO G-TIP ISLEM.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.

       BEGIN-PROGRAM.
           OPEN INPUT CEKSENET.
           MOVE G-TIP TO CS-TIP.
           MOVE G-EVRAKNO TO CS-EVRAKNO.
           READ CEKSENET INVALID KEY 
            CLOSE CEKSENET GO STOP-PROGRAM.
           CLOSE CEKSENET.
           
           
           OPEN INPUT CEKSENETLOG MAKBUZ SICIL.
           
           PERFORM ALT-TOPLAM-AL THRU ALT-TOPLAM-AL-SON.
           
           MOVE CS-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO SC-HESAPADI SC-YETKILI.
           MOVE CS-DEPONO TO T-KOD.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0000 0000 0000 0000 0.
           CANCEL 'DEPSEC'.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                 ' F4 ' LINE 37 POSITION 1
                 'ÄEK/SENET GòRòû/ÄIKIû òûLEMLERò' REVERSE LOW
                 ' F10 ' 'SON òûLEMò òPTAL ET' REVERSE LOW.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
                DISPLAY '∫' LINE CIZ POSITION 2
                    CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                     SPACES SIZE 10 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 10 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 15 '∫' SPACES SIZE 17 '∫' 
                     SPACES SIZE 16 '∫' SPACES SIZE 9 '∫'
           END-PERFORM.
           CALL 'GOLGE' USING BY CONTENT 0008 0002 0034 0117.
           CANCEL 'GOLGE'.
           MOVE ILK TO SATIR.
          
           MOVE ZEROS TO CSL-SIRA.
           MOVE ZEROS TO CSL-TARIH.
           MOVE G-TIP TO CSL-CTIP.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY.
           READ CEKSENETLOG NEXT.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ CEKSENETLOG NEXT AT END 
                    READ CEKSENETLOG PREVIOUS GO A-LISTE-SON.
           IF CSL-EVRAKNO NOT = G-EVRAKNO OR CSL-CTIP NOT = G-TIP
                    READ CEKSENETLOG PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                DISPLAY '∫' LINE CIZ POSITION 2
                    CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                     SPACES SIZE 10 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 10 '∫' SPACES SIZE 15 '∫' 
                     SPACES SIZE 15 '∫' SPACES SIZE 17 '∫' 
                     SPACES SIZE 16 '∫' SPACES SIZE 9 '∫' 
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE ZEROS TO CSL-SIRA.
           MOVE ZEROS TO CSL-TARIH.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE G-TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY.
           READ CEKSENETLOG NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE CEKSENETLOG MAKBUZ SICIL
                GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 GO SON-ISLEM-IPTAL.
           IF F = 4 GO GIRISCIKIS-AL.
           GO TUS.
       copy 'onayper.cpy'.
       copy 'trim.per'.
       copy 'numara.per'.
       copy 'chyaz.per'.
       ALT-TOPLAM-AL.
           MOVE ZEROS TO CSL-SIRA.
           MOVE ZEROS TO CSL-TARIH T-TOPLAM.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE G-TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY.
       ALT-TOPLAM-OKU.
           READ CEKSENETLOG NEXT AT END 
            READ CEKSENETLOG PREVIOUS GO ALT-TOPLAM-AL-SON.
           IF CSL-EVRAKNO NOT = G-EVRAKNO OR CSL-CTIP NOT = G-TIP
            READ CEKSENETLOG PREVIOUS GO ALT-TOPLAM-AL-SON.
           PERFORM AKTAR.
           EVALUATE CSL-SONUC
             WHEN 5 IF T-TUTAR > 0 
                        ADD T-TUTAR TO T-TOPLAM
                    END-IF
             WHEN 7 IF MK-HESAPNO = CS-HESAPNO
                        ADD T-TUTAR TO T-TOPLAM
                    END-IF
             WHEN OTHER MOVE ZEROS TO T-TOPLAM
           END-EVALUATE.
           GO ALT-TOPLAM-OKU.
       ALT-TOPLAM-AL-SON.
           COMPUTE TSON = CSL-GUN + CSL-AY * 30,41 + CSL-YIL * 365.
           MOVE CSL-SONUC TO TSONSONUC.
           MOVE CSL-OPTIME TO TSONOPTIME.
           MOVE CSL-MAKBUZNO TO MK-EVRAKNO.
           MOVE CSL-TIP TO MK-TIP.
           READ MAKBUZ INVALID KEY MOVE SPACES TO MK-HESAPNO.
           MOVE MK-HESAPNO TO TSONHESAPNO.
           DISPLAY SCR-ALT-TOPLAM.
           
       GIRISCIKIS-AL.
           MOVE WCB TO NESTED-WCB (3).
           DISPLAY NESTED-WCB (3)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0028 0002 0035 0041.
           CANCEL 'GOLGE'.
           MOVE 1 TO CINDX.
       GIRISCIKIS-MENU-AL.           
           IF CINDX = 0 MOVE 6 TO CINDX.
           IF CINDX = 7 MOVE 1 TO CINDX.
           DISPLAY SCR-GIRISCIKIS-MENU.
           EVALUATE CINDX
           WHEN 1
            DISPLAY ' 1. Bankaya Tahsile/Teminata Verildi.' SIZE 38
             LINE 29 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 2
            DISPLAY ' 2. Bankadan Tahsil Edildi/ôdendi.' SIZE 38
             LINE 30 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 3
            DISPLAY ' 3. Elden Tahsil Edildi/ôdendi.' SIZE 38
             LINE 31 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 4
            DISPLAY ' 4. Hesaba Äçkçü(Ciro) Yapçldç.' SIZE 38
             LINE 32 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 5
            DISPLAY ' 5. Karüçlçksçz/Protestolu Äçktç.' SIZE 38
             LINE 33 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           WHEN 6
            DISPLAY ' 6. òade Edildi/Alçndç. ' SIZE 38
             LINE 34 POSITION 3 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           END-EVALUATE.
       GIRISCIKIS-TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 SUBTRACT 1 FROM CINDX GO GIRISCIKIS-MENU-AL.
           IF F = 53 ADD 1 TO CINDX GO GIRISCIKIS-MENU-AL.
           IF F = 27 PERFORM GIRISCIKIS-AL-SON GO TUS.
           IF F = 13 AND CINDX = 1 GO BANKAYAVERME-AL.
           IF F = 13 AND CINDX = 2 GO BANKADANTAHSIL-AL.
           IF F = 13 AND CINDX = 3 GO ELDENTAHSIL-AL.
           IF F = 13 AND CINDX = 4 GO CIRO-AL.
           IF F = 13 AND CINDX = 5 GO PROTESTO-AL.
           IF F = 13 AND CINDX = 6 GO IADE-AL.
           GO GIRISCIKIS-TUS.
       GIRISCIKIS-AL-SON.   
           MOVE WCB TO NESTED-WCB (3).
           DISPLAY NESTED-WCB (3)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
      
       BANKAYAVERME-AL.
           IF TSONSONUC NOT = 0 GO GIRISCIKIS-MENU-AL.
           MOVE 'ÄEK/SENET BANKAYA ÄIKIû YAPILACAK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           PERFORM CIKIS-AL THRU CIKIS-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
           MOVE SC-HESAPNO TO T-HESAPNO.
           EVALUATE G-TIP
              WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
              WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE.
           PERFORM TRIM-BLOCK
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak bankaya áçkçü"
                DELIMITED BY SIZE INTO T-ACIKLAMA.
           PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF.
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.
       BANKADANTAHSIL-AL.
           IF TSONSONUC NOT = 1 AND G-TIP = 18 GO GIRISCIKIS-MENU-AL.
           MOVE 'ÄEK/SENET BANKADAN ôDENECEK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           PERFORM CIKIS-AL THRU CIKIS-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
           MOVE TSONHESAPNO TO T-HESAPNO.
           EVALUATE G-TIP
             WHEN 18 WHEN 20 MOVE 16 TO MK-TIP
             WHEN 19 WHEN 21 MOVE 17 TO MK-TIP
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak bankadan tahsil"
               DELIMITED BY SIZE INTO T-ACIKLAMA.
           PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF.
              
           MOVE SC-HESAPNO TO T-HESAPNO.
           EVALUATE G-TIP
              WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
              WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE
           PERFORM TRIM-BLOCK
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak îdemesi"
                DELIMITED BY SIZE INTO T-ACIKLAMA.
           PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF.
           
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.
           

       ELDENTAHSIL-AL.
           IF TSONSONUC NOT = 0 AND TSONSONUC NOT = 5 
             AND TSONSONUC NOT = 7 GO GIRISCIKIS-TUS.
           
           MOVE 'ÄEK/SENET ELDEN ôDEME YAPILACAK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           
           PERFORM ODEME-AL THRU ODEME-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           IF T-TOPLAM NOT = ZEROS AND T-ODEME > T-TOPLAM
                MOVE 'KALAN TUTARDAN BöYöK ôDEME YAPAMAZSINIZ' TO 
                    ONAY-MESSAGE
                PERFORM WARN-MESSAGE
                GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
           IF TSONSONUC = 0 AND CS-TUTAR NOT = T-ODEME
                MOVE CS-HESAPNO TO T-HESAPNO
                MOVE T-ODEME TO T-TUTAR
                MOVE CS-TUTAR TO T-ODEME
                EVALUATE G-TIP
                    WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
                    WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
                END-EVALUATE
                MOVE CS-EVRAKNO TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                STRING TRIM-VALUE(1:TRIM-SIZE) 
                   " no'lu evrak elden îdeme iáin iade"
                    DELIMITED BY SIZE INTO T-ACIKLAMA
                MOVE 7 TO CINDX TSONSONUC
                PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON
                IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF
                MOVE T-TUTAR TO T-ODEME
                
           END-IF.
           
           IF TSONSONUC = 7 OR TSONSONUC = 5
               MOVE CS-HESAPNO TO T-HESAPNO
               EVALUATE G-TIP
                    WHEN 18 WHEN 20 MOVE 16 TO MK-TIP
                    WHEN 19 WHEN 21 MOVE 17 TO MK-TIP
               END-EVALUATE
               MOVE CS-EVRAKNO TO TRIM-VALUE
               PERFORM TRIM-BLOCK
               STRING TRIM-VALUE(1:TRIM-SIZE) 
                   " no'lu evrak elden kçsmç îdeme"
                    DELIMITED BY SIZE INTO T-ACIKLAMA
                IF T-TOPLAM NOT = T-ODEME MOVE 7 TO CINDX END-IF
                PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON
                IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF
           END-IF. 
           

           OPEN I-O KASA.
           MOVE T-TARIH TO KS-TARIH.
           IF TSONSONUC = 0 AND CS-TUTAR = T-ODEME
               MOVE SPACES TO KS-HESAPNO KS-MEVRAKNO
               MOVE ZEROS TO KS-MTIP
               
           ELSE
               MOVE CS-HESAPNO TO KS-HESAPNO
               MOVE MK-TIP TO KS-MTIP
               MOVE MK-EVRAKNO TO KS-MEVRAKNO
           END-IF.
           MOVE ZEROS TO KS-GELIR KS-GIDER.
           EVALUATE G-TIP
             WHEN 18 WHEN 20 MOVE T-ODEME TO KS-GELIR
             WHEN 19 WHEN 21 MOVE T-ODEME TO KS-GIDER
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak elden îdemesi"
               DELIMITED BY SIZE INTO KS-ACIKLAMA.
           MOVE 1 TO KS-SIRA.
       KASA-YAZ.    
	       PERFORM WRITE-KASA.
		   IF WVAR = 0 ADD 1 TO KS-SIRA GO KASA-YAZ.
           
           IF KS-HESAPNO = SPACES 
              MOVE KS-KEY TO MK-EVRAKNO
              MOVE ZEROS TO MK-TIP
              PERFORM LOG-YAZ THRU LOG-YAZ-SON
           END-IF.
           
           MOVE KS-GELIR TO TX-GELIR.
           MOVE KS-GIDER TO TX-GIDER.
           MOVE KS-TARIH TO TX-TARIH.
           MOVE 1 TO KS-FLAG.
           PERFORM KASA-TOPLAM-KONTROL THRU KASA-TOPLAM-KONTROL-SON.
           
           CLOSE KASA.
           
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.

       CIRO-AL.
           IF TSONSONUC NOT = 0 GO GIRISCIKIS-TUS.
           MOVE 'ÄEK/SENET HESABA CòRO YAPILACAK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           PERFORM CIKIS-AL THRU CIKIS-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
           MOVE SC-HESAPNO TO T-HESAPNO.
           EVALUATE G-TIP
              WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
              WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak cirosu"
                DELIMITED BY SIZE INTO T-ACIKLAMA.
           PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF.
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.    
              
       PROTESTO-AL.
           IF TSONSONUC NOT = 0 AND TSONSONUC NOT = 1 AND 
              TSONSONUC NOT = 4 GO GIRISCIKIS-TUS.
           
           MOVE 'ÄEK/SENET PROTESTO EDòLECEK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           PERFORM MASRAF-AL THRU MASRAF-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.   
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
           IF TSONSONUC = 1 OR TSONSONUC = 4
                MOVE TSONHESAPNO TO T-HESAPNO
                EVALUATE G-TIP
                    WHEN 18 WHEN 20 MOVE 16 TO MK-TIP
                    WHEN 19 WHEN 21 MOVE 17 TO MK-TIP
                END-EVALUATE
                MOVE CS-EVRAKNO TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak protesto"
                    DELIMITED BY SIZE INTO T-ACIKLAMA
                PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON
                IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF
           END-IF.
           
           MOVE CS-HESAPNO TO T-HESAPNO.
           EVALUATE G-TIP
              WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
              WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
           END-EVALUATE.
           MOVE CS-EVRAKNO TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak protesto"
              DELIMITED BY SIZE INTO T-ACIKLAMA.
           PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.
       IADE-AL.
           IF TSONSONUC NOT = 0 AND TSONSONUC NOT = 1 AND
              TSONSONUC NOT = 4 AND TSONSONUC NOT = 5 AND
              TSONSONUC NOT = 3 GO GIRISCIKIS-TUS.
           IF TSONSONUC = 5 AND T-TOPLAM NOT = ZEROS
                MOVE 'ÄEK/SENET KALAN BORÄ VAR òADE EDòLEMEZ' TO
                ONAY-MESSAGE
                PERFORM WARN-MESSAGE
                GO GIRISCIKIS-TUS.
           MOVE 'ÄEK/SENET HESABA òADE EDòLECEK EMòN MòSòNòZ ?' TO
                ONAY-MESSAGE.
           PERFORM IADESCR-AL THRU IADESCR-AL-SON.
           IF T-ERROR = 1 GO GIRISCIKIS-TUS.
           CLOSE MAKBUZ SICIL CEKSENETLOG.
           ACCEPT CSL-OPTIME FROM DATE-AND-TIME.
           
      * BANKAYA VEYA CòROYA VERòLDòYSE TERS KAYIT SONUC=PORTFOY
           IF TSONSONUC = 1 OR TSONSONUC = 4
                MOVE TSONHESAPNO TO T-HESAPNO
                EVALUATE G-TIP
                    WHEN 18 WHEN 20 MOVE 16 TO MK-TIP
                    WHEN 19 WHEN 21 MOVE 17 TO MK-TIP
                END-EVALUATE
                MOVE CS-EVRAKNO TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak iadesi"
                    DELIMITED BY SIZE INTO T-ACIKLAMA
                PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON
                IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF
                MOVE SPACES TO MK-EVRAKNO
                MOVE ZEROS TO MK-TIP
                MOVE 0 TO CINDX
                PERFORM LOG-YAZ THRU LOG-YAZ-SON
           END-IF.
      * PORTFOYDE òSE TERS KAYIT 
           IF TSONSONUC = 0
                MOVE CS-HESAPNO TO T-HESAPNO
                EVALUATE G-TIP
                    WHEN 18 WHEN 20 MOVE 17 TO MK-TIP
                    WHEN 19 WHEN 21 MOVE 16 TO MK-TIP
                END-EVALUATE
                MOVE CS-EVRAKNO TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                STRING TRIM-VALUE(1:TRIM-SIZE) " no'lu evrak iadesi"
                    DELIMITED BY SIZE INTO T-ACIKLAMA
                PERFORM ISLEM-YAZ THRU ISLEM-YAZ-SON
                IF T-ERROR = 1 GO GIRISCIKIS-TUS END-IF
           END-IF.
      * PROTESTO òSE SADECE SONUC òADE
           IF TSONSONUC = 5 OR TSONSONUC = 3
              MOVE SPACES TO MK-EVRAKNO
              MOVE ZEROS TO MK-TIP
              PERFORM LOG-YAZ THRU LOG-YAZ-SON
           END-IF.
      
           PERFORM GIRISCIKIS-AL-SON.
           GO BEGIN-PROGRAM.
       HESAPTANODEME-AL.
           
       SON-ISLEM-IPTAL.
           MOVE 'EVRAKA YAPILAN SON òûLEM òPTAL EDòLECEK ?' TO
                ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY NOT = 'e' AND E-ONAY NOT = 'E' GO TUS.
           
           CLOSE CEKSENETLOG SICIL MAKBUZ.
           OPEN I-O CEKSENETLOG MAKBUZ KASA.
       SON-ISLEM-OKU.
           MOVE 99999999 TO CSL-TARIH.
           MOVE 9999999999 TO CSL-SIRA.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE G-TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT GREATER THAN CSL-KEY.
           READ CEKSENETLOG NEXT.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
                GO SON-ISLEM-SON.
           IF CSL-SIRA = 1 GO SON-ISLEM-SON.
           IF TSONOPTIME NOT = CSL-OPTIME GO SON-ISLEM-SON.
           IF CSL-SONUC = 3 AND CSL-TIP = ZEROS
            MOVE CSL-MAKBUZNO TO KS-KEY
            READ KASA INVALID KEY CONTINUE
                    NOT INVALID KEY 
                MOVE KS-TARIH TO TX-TARIH
                MOVE KS-GELIR TO TX-GELIR
                MOVE KS-GIDER TO TX-GIDER
                PERFORM DELETE-KASA 
                MOVE 0 TO KS-FLAG
                PERFORM KASA-TOPLAM-KONTROL THRU KASA-TOPLAM-KONTROL-SON
            END-READ
           END-IF.
           
           MOVE CSL-MAKBUZNO TO MK-EVRAKNO.
           MOVE CSL-TIP TO MK-TIP.
           READ MAKBUZ INVALID KEY PERFORM DELETE-CEKSENETLOG
             GO SON-ISLEM-OKU.
           MOVE MK-EVRAKNO TO KS-MEVRAKNO.
           MOVE MK-TIP TO KS-MTIP.
           START KASA KEY IS EQUAL TO KS-MAKBUZ
                INVALID KEY CONTINUE
                NOT INVALID KEY 
            READ KASA NEXT END-READ
            MOVE KS-TARIH TO TX-TARIH
            MOVE KS-GELIR TO TX-GELIR
            MOVE KS-GIDER TO TX-GIDER
            PERFORM DELETE-KASA 
            MOVE 0 TO KS-FLAG
            PERFORM KASA-TOPLAM-KONTROL THRU KASA-TOPLAM-KONTROL-SON
           END-START.
           MOVE MK-HESAPNO TO TCH-HESAPNO.
           MOVE MK-TARIH TO TCH-TARIH.
           MOVE MK-DEPONO TO TCH-DEPONO.
           MOVE MK-EVRAKNO TO TCH-EVRAKNO.
           MOVE MK-TIP TO TCH-TIP.
           PERFORM CARHAR-SIL.
           PERFORM DELETE-MAKBUZ.
           PERFORM DELETE-CEKSENETLOG.
           GO SON-ISLEM-OKU.
       SON-ISLEM-SON.
           CLOSE CEKSENETLOG MAKBUZ KASA.
           MOVE 1 TO ISLEM.
           GO BEGIN-PROGRAM.
       
       KASA-TOPLAM-KONTROL.
           MOVE TX-TARIH TO KS-TARIH.
           MOVE ZEROS TO KS-SIRA.
           READ KASA INVALID KEY  
            IF KS-FLAG = 0 GO KASA-TOPLAM-KONTROL-SON END-IF
            INITIALIZE KS-KAYIT
            MOVE T-TARIH TO KS-TARIH
			PERFORM WRITE-KASA
           END-READ.
           IF KS-FLAG = 0   
                SUBTRACT TX-GELIR FROM KS-GELIR
                SUBTRACT TX-GIDER FROM KS-GIDER
           ELSE
                ADD TX-GELIR TO KS-GELIR
                ADD TX-GIDER TO KS-GIDER
           END-IF.
		   PERFORM REWRITE-KASA.
       KASA-TOPLAM-KONTROL-SON.
           
       ODEME-AL.
           MOVE WCB TO NESTED-WCB (4).
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0029 0042 0032 0111.
           CANCEL 'GOLGE'.
           MOVE 0 TO T-ERROR.
           ACCEPT T-TARIH FROM CENTURY-DATE.
           MOVE ZEROS TO T-MASRAF.
           MOVE CS-TUTAR TO T-ODEME.
           MOVE SPACES TO SC-HESAPNO SC-HESAPADI SC-YETKILI.
           DISPLAY SCR-ODEME.
       ODEME-TARIH-AL.
           DISPLAY SCR-ODEME-TARIH.
           ACCEPT SCR-ODEME-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           MOVE WCB TO NESTED-WCB (4).
           IF F = 27 OR F = 52 
             DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
             GO GIRISCIKIS-TUS.
           COMPUTE TT = T-GUN + T-AY * 30,41 + T-YIL * 365.
           IF TT < TSON
               MOVE 'òûLEM TARòHò YAPILAN SON òûLEMDEN KöÄöK !!!'
                 TO ONAY-MESSAGE
               PERFORM WARN-MESSAGE
               GO ODEME-TARIH-AL.
       ODEME-TUTAR-AL.
           DISPLAY SCR-ODEME-TUTAR.
           ACCEPT SCR-ODEME-TUTAR.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-ODEME-TUTAR.
           IF F = 52 OR F = 27 GO ODEME-TARIH-AL.
           IF T-ODEME = ZEROS GO ODEME-TUTAR-AL.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF E-ONAY = 'H' OR E-ONAY = 'h' MOVE 1 TO T-ERROR.
       ODEME-AL-SON.
       
       CIKIS-AL.
           MOVE WCB TO NESTED-WCB (4).
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0032 0042 0034 0111.
           CANCEL 'GOLGE'.
           MOVE 0 TO T-ERROR.
           ACCEPT T-TARIH FROM CENTURY-DATE.
           MOVE ZEROS TO T-MASRAF.
           MOVE CS-TUTAR TO T-ODEME.
           MOVE SPACES TO SC-HESAPNO SC-HESAPADI SC-YETKILI.
           DISPLAY SCR-CIKIS.
       CIKIS-TARIH-AL.
           DISPLAY SCR-CIKIS-TARIH.
           ACCEPT SCR-CIKIS-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           MOVE WCB TO NESTED-WCB (4).
           IF F = 27 OR F = 52 
             DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
             GO GIRISCIKIS-TUS.
           COMPUTE TT = T-GUN + T-AY * 30,41 + T-YIL * 365.
           IF TT < TSON
               MOVE 'òûLEM TARòHò YAPILAN SON òûLEMDEN KöÄöK !!!'
                 TO ONAY-MESSAGE
               PERFORM WARN-MESSAGE
               GO CIKIS-TARIH-AL.
       CIKIS-HESAPNO-SOR.
           DISPLAY SCR-CIKIS-HESAPNO.
           ACCEPT SCR-CIKIS-HESAPNO.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO CIKIS-TARIH-AL.
           IF F = 1 CALL 'SICPEN' USING SC-HESAPNO
                CANCEL 'SICPEN' GO CIKIS-HESAPNO-SOR.
           IF SC-HESAPNO = CS-HESAPNO GO CIKIS-HESAPNO-SOR.
           READ SICIL INVALID KEY GO CIKIS-HESAPNO-SOR.
           DISPLAY SCR-CIKIS.           
      
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF E-ONAY = 'H' OR E-ONAY = 'h' MOVE 1 TO T-ERROR.
       CIKIS-AL-SON.
       
       IADESCR-AL.
           MOVE WCB TO NESTED-WCB (4).
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0032 0042 0034 0079.
           CANCEL 'GOLGE'.
           MOVE 0 TO T-ERROR.
           ACCEPT T-TARIH FROM CENTURY-DATE.
           MOVE ZEROS TO T-MASRAF.
           MOVE CS-TUTAR TO T-ODEME.
       IADESCR-SOR.
           DISPLAY SCR-IADE.
           ACCEPT SCR-IADE.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-IADE.
           MOVE WCB TO NESTED-WCB (4).
           IF F = 27 OR F = 52 
             DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
             GO GIRISCIKIS-TUS.
           COMPUTE TT = T-GUN + T-AY * 30,41 + T-YIL * 365.
           IF TT < TSON
               MOVE 'òûLEM TARòHò YAPILAN SON òûLEMDEN KöÄöK !!!'
                 TO ONAY-MESSAGE
               PERFORM WARN-MESSAGE
               GO IADESCR-SOR.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF E-ONAY = 'H' OR E-ONAY = 'h' MOVE 1 TO T-ERROR.
       IADESCR-AL-SON.
       
       MASRAF-AL.
           MOVE WCB TO NESTED-WCB (4).
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING BY CONTENT 0032 0042 0035 0079.
           CANCEL 'GOLGE'.
           MOVE 0 TO T-ERROR.
           ACCEPT T-TARIH FROM CENTURY-DATE.
           MOVE ZEROS TO T-MASRAF.
           MOVE CS-TUTAR TO T-ODEME.
       MASRAF-SOR.
           DISPLAY SCR-MASRAF.
           ACCEPT SCR-MASRAF.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-MASRAF.
           MOVE WCB TO NESTED-WCB (4).
           IF F = 27 OR F = 52 
             DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
             GO GIRISCIKIS-TUS.
           COMPUTE TT = T-GUN + T-AY * 30,41 + T-YIL * 365.
           IF TT < TSON
               MOVE 'òûLEM TARòHò YAPILAN SON òûLEMDEN KöÄöK !!!'
                 TO ONAY-MESSAGE
               PERFORM WARN-MESSAGE
               GO MASRAF-SOR.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           DISPLAY NESTED-WCB (4)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF E-ONAY = 'H' OR E-ONAY = 'h' MOVE 1 TO T-ERROR.
       MASRAF-AL-SON.
       
       ISLEM-YAZ.
           MOVE 0 TO T-ERROR.
           
           MOVE MK-TIP TO DX-TIP.
           MOVE CS-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           
           IF AY-EVRAKNO = SPACES
              MOVE 'KAFADAKò MAKBUZ EVRAK NUMARASI BOû !!!' 
                TO ONAY-MESSAGE
              PERFORM WARN-MESSAGE 
              MOVE 1 TO T-ERROR
              GO ISLEM-YAZ-SON
           END-IF.
           
           OPEN INPUT MAKBUZ.
           MOVE AY-EVRAKNO TO MK-EVRAKNO.
           READ MAKBUZ INVALID KEY CLOSE MAKBUZ
            NOT INVALID KEY CLOSE MAKBUZ
              MOVE 'KAFADAKò MAKBUZ EVRAK NO. YA AòT EVRAK VAR !!!' 
              TO ONAY-MESSAGE 
              PERFORM WARN-MESSAGE
              MOVE 1 TO T-ERROR
              GO ISLEM-YAZ-SON
           END-READ.
              
           IF T-MASRAF NOT = ZEROS
                MOVE AY-EVRAKNO TO MK-EVRAKNO
                MOVE T-HESAPNO TO MK-HESAPNO
                MOVE CS-DEPONO TO MK-DEPONO
                MOVE T-TARIH TO MK-TARIH
                MOVE 1 TO MK-SEKILNO
                MOVE CS-EVRAKNO TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                MOVE SPACES TO MK-ACIKLAMA
                STRING TRIM-VALUE(1:TRIM-SIZE)
                       " no'lu evrak masrafç"
                    DELIMITED BY SIZE INTO MK-ACIKLAMA
                END-STRING
                MOVE T-MASRAF TO MK-TUTAR
                PERFORM MAKBUZ-YAZ
                PERFORM LOG-YAZ THRU LOG-YAZ-SON
                OPEN I-O AYAR
                MOVE MK-TIP TO DX-TIP
                MOVE MK-DEPONO TO DX-DEPONO
                PERFORM NUMARA-ARTIR
                CLOSE AYAR
           END-IF.
           
           MOVE AY-EVRAKNO TO MK-EVRAKNO.
           MOVE T-HESAPNO TO MK-HESAPNO.
           MOVE CS-DEPONO TO MK-DEPONO.
           MOVE T-TARIH TO MK-TARIH.
           MOVE 1 TO MK-SEKILNO.
           MOVE SPACES TO MK-ACIKLAMA.
           MOVE T-ACIKLAMA TO MK-ACIKLAMA.
           MOVE T-ODEME TO MK-TUTAR.
           PERFORM MAKBUZ-YAZ.
           OPEN I-O AYAR.
           MOVE MK-TIP TO DX-TIP.
           MOVE MK-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-ARTIR.
           CLOSE AYAR.
           PERFORM LOG-YAZ THRU LOG-YAZ-SON.
       ISLEM-YAZ-SON.
           
       LOG-YAZ.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE T-TARIH TO CSL-TARIH.
           MOVE CINDX TO CSL-SONUC.
           MOVE MK-EVRAKNO TO CSL-MAKBUZNO.
           MOVE MK-TIP TO CSL-TIP.
           MOVE G-TIP TO CSL-CTIP.
           MOVE 1 TO CSL-SIRA.
           OPEN I-O CEKSENETLOG.
       LOG-YAZ-SON.
	       PERFORM WRITE-CEKSENETLOG.
		   IF WVAR = 0 ADD 1 TO CSL-SIRA GO LOG-YAZ-SON.
           CLOSE CEKSENETLOG.
           MOVE 1 TO ISLEM.
           
       MAKBUZ-YAZ.
           OPEN I-O MAKBUZ.
           PERFORM WRITE-MAKBUZ.
		   CLOSE MAKBUZ.
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
           STRING CS-VGUN '/' CS-VAY '/' CS-VYIL
            DELIMITED BY SIZE INTO TCH-VADE.
           PERFORM CARHAR-YAZ THRU CARHAR-YAZ-SON.
           
       
       CTHO-TUSU.
           MOVE ZEROS TO CSL-SIRA.
           MOVE ZEROS TO CSL-TARIH.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE G-TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT LESS THAN CSL-KEY.
           READ CEKSENETLOG NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
           
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999999 TO CSL-TARIH.
           MOVE G-EVRAKNO TO CSL-EVRAKNO.
           MOVE 9999999999 TO CSL-SIRA.
           MOVE G-TIP TO CSL-CTIP.
           START CEKSENETLOG KEY IS NOT GREATER THAN CSL-KEY.
           READ CEKSENETLOG NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ CEKSENETLOG NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
       PGUP-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ CEKSENETLOG PREVIOUS
             END-PERFORM.
           READ CEKSENETLOG PREVIOUS AT END
              READ CEKSENETLOG NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
              READ CEKSENETLOG NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ CEKSENETLOG PREVIOUS AT END 
                    READ CEKSENETLOG NEXT 
                    GO PG-OKU-SON.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
                    READ CEKSENETLOG NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ CEKSENETLOG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ CEKSENETLOG NEXT
           END-PERFORM.
           READ CEKSENETLOG NEXT AT END
              READ CEKSENETLOG PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
              READ CEKSENETLOG PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ CEKSENETLOG NEXT AT END 
             READ CEKSENETLOG PREVIOUS MOVE ILK TO SATIR.
           IF CSL-EVRAKNO NOT = G-EVRAKNO OR CSL-CTIP NOT = G-TIP
             READ CEKSENETLOG PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ CEKSENETLOG NEXT 
                 END-READ 
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ CEKSENETLOG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           READ CEKSENETLOG PREVIOUS AT END 
                READ CEKSENETLOG NEXT GO TUS.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
                READ CEKSENETLOG NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ CEKSENETLOG PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           READ CEKSENETLOG NEXT AT END 
                READ CEKSENETLOG PREVIOUS GO TUS.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
                READ CEKSENETLOG PREVIOUS GO TUS.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ CEKSENETLOG NEXT
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
           READ CEKSENETLOG PREVIOUS AT END READ CEKSENETLOG NEXT 
                GO Y-LISTE-SON.
           IF G-EVRAKNO NOT = CSL-EVRAKNO OR CSL-CTIP NOT = G-TIP
             READ CEKSENETLOG NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           STRING CSL-GUN '/' CSL-AY '/' CSL-YIL
            DELIMITED BY SIZE INTO B-TARIH.
           
           INITIALIZE MK-KAYIT.
           MOVE CSL-MAKBUZNO TO MK-EVRAKNO B-EVRAKNO.
           MOVE CSL-TIP TO MK-TIP.
           MOVE ZEROS TO MK-TUTAR.
           READ MAKBUZ INVALID KEY 
             IF (G-TIP = 18 OR G-TIP = 20) MOVE 16 TO MK-TIP
                    ELSE MOVE 17 TO MK-TIP END-IF
              MOVE SPACES TO MK-HESAPNO.
           IF MK-TIP = 16 AND (G-TIP = 18 OR G-TIP = 20)
                COMPUTE T-TUTAR = MK-TUTAR * -1.
           IF MK-TIP = 17 AND (G-TIP = 18 OR G-TIP = 20)
                MOVE MK-TUTAR TO T-TUTAR.
           IF MK-TIP = 16 AND (G-TIP = 19 OR G-TIP = 21)
                MOVE MK-TUTAR TO T-TUTAR.
           IF MK-TIP = 17 AND (G-TIP = 19 OR G-TIP = 21)
                COMPUTE T-TUTAR = MK-TUTAR * -1.
                
                
           INITIALIZE SC-KAYIT.
           MOVE MK-HESAPNO TO B-HESAPNO SC-HESAPNO.
           READ SICIL INVALID KEY CONTINUE.
           MOVE SC-HESAPADI TO B-HESAPADI.
           MOVE SC-YETKILI TO B-YETKILI.
           IF CSL-SONUC = 0
            MOVE 'Portfîyde.' TO MK-ACIKLAMA.
            
           MOVE MK-ACIKLAMA TO B-ACIKLAMA.
           MOVE T-TUTAR TO B-TUTAR.
           EVALUATE CSL-SONUC
             WHEN 0 MOVE 'Portfîyde' TO B-DURUM
             WHEN 1 MOVE 'B.Verme' TO B-DURUM
             WHEN 2 MOVE 'B.Tahsil' TO B-DURUM
             WHEN 3 MOVE 'E.Tahsil' TO B-DURUM
             WHEN 4 MOVE 'Ciro' TO B-DURUM
             WHEN 5 MOVE 'Protesto' TO B-DURUM
             WHEN 6 MOVE 'òade' TO B-DURUM
             WHEN 7 MOVE 'K.ôdeme' TO B-DURUM
           END-EVALUATE.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 3
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 3
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==CEKSENETLOG==
                                  "%T%"  BY "CEKSENETLOG"
                                 ==%R%== BY ==CSL-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==KASA==
                                  "%T%"  BY "KASA"
                                 ==%R%== BY ==KS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==MAKBUZ==
                                  "%T%"  BY "MAKBUZ"
                                 ==%R%== BY ==MK-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.

           