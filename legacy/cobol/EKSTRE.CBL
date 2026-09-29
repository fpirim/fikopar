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
       copy 'sicil.cpy'.
       copy 'siciltel.cpy'.
       copy 'carhar.cpy'.
       copy 'depo.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicil.cpz'.
       copy 'siciltel.cpz'.
       copy 'carhar.cpz'.
       copy 'depo.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-TARIH.
                    04 TP-YIL            PIC 9999.
                    04 TP-AY             PIC 99.
                    04 TP-GUN            PIC 99.
               03 TP-DEPONO           PIC 9999.
               03 TP-SIRA           PIC 9(10).
           02 TP-CSIRA              PIC 9(10).
           02 TP-DPADI              PIC X(30).    
           02 TP-TIP                PIC 99.
           02 TP-EVRAKNO            PIC X(15).
           02 TP-ACIKLAMA           PIC X(60).
           02 TP-VADE               PIC X(15).
           02 TP-BORC               PIC 9(13)V99.
           02 TP-ALACAK             PIC 9(13)V99.
           02 TP-BAKIYE             PIC S9(13)V99.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                 PIC 99.
           02 CIZ               PIC 99.
           02 DUR               PIC X.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
           02 T-DEPONO          PIC 9999.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999 VALUE 12.
           02 SON                   PIC 9999 VALUE 32. 
           02 ISLEM             PIC 9.
           02 GORUNTU           PIC 9.
           02 ARRLEN            PIC 9(10).
           02 BA-TARIH.
               03 BA-YIL            PIC 9999.
               03 BA-AY             PIC 99.
               03 BA-GUN            PIC 99.
           02 BT-TARIH.
               03 BT-YIL            PIC 9999.
               03 BT-AY             PIC 99.
               03 BT-GUN            PIC 99.
           02 TT                PIC 9(12)V9999.
           02 T1                PIC 9(12)V9999.
           02 T2                PIC 9(12)V9999.
           02 T-BORC            PIC 9(13)V99.
           02 T-ALACAK          PIC 9(13)V99.
           02 B-TEL             PIC 9(12).
           02 B-CEP             PIC 9(12).
           02 B-FAX             PIC 9(12).
           02 V-TEL             PIC 9999 VALUE ZEROS.
           02 V-FAX             PIC 9999 VALUE ZEROS.
           02 V-CEP             PIC 9999 VALUE ZEROS.
           02 DP-BAKIYE         PIC S9(12)V99 OCCURS 2 TIMES.
           02 DPI               PIC 9.
       01 BILGI-KEY.
           02 BK-HESAPNO            PIC X(15).
           02 BK-DEPONO             PIC 9999.    
           02 BK-TARIH.
               03 BK-YIL               PIC 9999.
               03 BK-AY                PIC 99.
               03 BK-GUN               PIC 99.
           03 BK-SIRA               PIC 9(10).
       01 BILGI.
           02 B-TARIH           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TIP             PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-EVRAKNO         PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-VADE            PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-DEPONO          PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ACIKLAMA        PIC X(26).
           02 FILLER            PIC X VALUE 'º'.
           02 B-BORC            PIC ZZZ.ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-ALACAK          PIC ZZZ.ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-BAKIYE          PIC ---.---.---,--.
       copy 'fileop.var'.
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
           02 PIC X(118) FROM ' S˜C˜L HESAP KART EKSTRES˜ ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 4 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE 
              'HESAP NO  [' HIGHLIGHT. 02 PIC X(15) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(65) FROM ALL ' '. 
           02 'BAžLANGI€ [  /  /    ]' HIGHLIGHT.
           02 LINE 5 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'HESAP ADI [' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(30) FROM ALL ' '.
           02 'B˜T˜ž     [  /  /    ]' HIGHLIGHT.
           02 LINE 6 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'YETK˜L˜   [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(40) FROM ALL ' '.
           02 'DEPO      [          ]' HIGHLIGHT.
           02 LINE 7 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'TEL/CEP/FX[' HIGHLIGHT. 02 PIC X(15) FROM ALL ' '.
           02 '] - [' HIGHLIGHT. 02 PIC X(15) FROM ALL ' '.
           02 '] - [' HIGHLIGHT. 02 PIC X(15) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 LINE 8 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'E-MAIL    [' HIGHLIGHT. 02 PIC X(40) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 LINE 9 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(26) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 10 COLUMN 2 BACKGROUND BLUE FOREGROUND WHITE
               PIC X(10) FROM ' TAR˜H' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM 'T˜P˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' EVRAK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM ' VADE' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM 'DEPO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(26) FROM ' A€IKLAMA' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(14) FROM ' BOR€' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(14) FROM ' ALACAK' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(14) FROM ' BAK˜YE' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 LINE 11 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(26) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 33 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(26) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 34 COLUMN 65 BACKGROUND BLUE FOREGROUND WHITE
              'TOPLAM' HIGHLIGHT.
           02 LINE 35 COLUMN 65 BACKGROUND BLUE FOREGROUND WHITE
              'BAK˜YE' HIGHLIGHT.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(73) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       01 SCR-TOPLAM.
           02 LINE 34 COLUMN 76 BACKGROUND BLUE FOREGROUND BROWN
              PIC ZZZ.ZZZ.ZZZ,ZZ USING T-BORC HIGHLIGHT.
           02 LINE 34 COLUMN 91 BACKGROUND BLUE FOREGROUND BROWN
              PIC ZZZ.ZZZ.ZZZ,ZZ USING T-ALACAK HIGHLIGHT.
                 
       01 SCR-BASLIK.
           02 LINE 5 COLUMN 14 BACKGROUND BLUE FOREGROUND BROWN
                PIC X(50) USING SC-HESAPADI HIGHLIGHT.
           02 LINE 6 COLUMN 14 BACKGROUND BLUE FOREGROUND BROWN
                PIC X(40) USING SC-YETKILI HIGHLIGHT.
           02 LINE 7 COLUMN 14 BACKGROUND BLUE FOREGROUND BROWN
                PIC +ZZBZZZBZZZZZZZ USING B-TEL HIGHLIGHT.
           02 LINE 7 COLUMN 34 BACKGROUND BLUE FOREGROUND BROWN
                PIC +ZZBZZZBZZZZZZZ USING B-CEP HIGHLIGHT.
           02 LINE 7 COLUMN 54 BACKGROUND BLUE FOREGROUND BROWN
                PIC +ZZBZZZBZZZZZZZ USING B-FAX HIGHLIGHT.
           02 SCR-TARIH.
               03 LINE 4 COLUMN 106 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZ USING BA-GUN HIGHLIGHT AUTO.
               03 LINE 4 COLUMN 109 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZ USING BA-AY HIGHLIGHT AUTO.
               03 LINE 4 COLUMN 112 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZZZ USING BA-YIL HIGHLIGHT AUTO.
               03 LINE 5 COLUMN 106 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZ USING BT-GUN HIGHLIGHT AUTO.
               03 LINE 5 COLUMN 109 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZ USING BT-AY HIGHLIGHT AUTO.
               03 LINE 5 COLUMN 112 BACKGROUND BLUE FOREGROUND WHITE
                    PIC ZZZZ USING BT-YIL HIGHLIGHT AUTO.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
           INTO BA-TARIH, BT-TARIH, T-DEPONO, SC-HESAPNO, GORUNTU, ISLEM
           END-UNSTRING.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
             
       BEGIN-PROGRAM.
           PERFORM VARYING CIZ FROM 4 BY 1 UNTIL CIZ > 35
                IF CIZ > 11 AND CIZ < 33
                    DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 10 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 10 'º' SPACES SIZE 10 'º' 
                     SPACES SIZE 7 'º' SPACES SIZE 26 'º' 
                     SPACES SIZE 14 'º' SPACES SIZE 14 'º'
                     SPACES SIZE 14 'º'
                ELSE
                   IF CIZ = 34 OR CIZ = 35
                    DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                     SPACES SIZE 73 'º' 
                     SPACES SIZE 14 'º' SPACES SIZE 14 'º'
                     SPACES SIZE 14 'º'
                   ELSE 
                    DISPLAY 'º' LINE CIZ POSITION 1
                    CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                        SPACES SIZE 118 'º'
                   END-IF
                END-IF
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
                ' F1 ' LINE 37 POSITION 1 'S˜C˜LLER' REVERSE LOW
                ' F2 ' ' AYARLAR ' REVERSE LOW
                ' F10 ' 'YAZDIR' REVERSE LOW.
           OPEN INPUT SICIL.
           IF GORUNTU = 1 DISPLAY SC-HESAPNO LINE 4 POSITION 14
                            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
                          PERFORM BASLIK-GOSTER
                          DISPLAY SCR-TARIH
                          MOVE T-DEPONO TO T-KOD
                          MOVE ZEROS TO T-KDV
                          CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                                    BY CONTENT 0106 0006 0115 0010 1
                          CANCEL 'DEPSEC'
                          DISPLAY T-ADI LINE 6 POSITION 106 SIZE 10
                            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
                          GO START-EKSTRE.
           MOVE SPACES TO SC-HESAPNO.
       HESAPNO-AL.
           MOVE ZEROS TO T-BORC T-ALACAK B-TEL B-CEP B-FAX.
           DISPLAY SCR-TOPLAM.
           ACCEPT BA-TARIH FROM CENTURY-DATE.
           MOVE BA-TARIH TO BT-TARIH.
           MOVE 1 TO BA-GUN BA-AY.
           ACCEPT SC-HESAPNO NO BEEP LINE 4 POSITION 14 UPDATE
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SICIL GO STOP-PROGRAM.
           IF F = 1 CALL 'SICPEN' USING SC-HESAPNO
                    CANCEL 'SICPEN' GO HESAPNO-AL.
           IF F = 2 CALL 'AYAR' USING 15 CANCEL 'AYAR' GO HESAPNO-AL.
       BASLIK-GOSTER.
           READ SICIL INVALID KEY GO HESAPNO-AL.
           CLOSE SICIL.
       
           
           OPEN INPUT SICILTEL.
           MOVE ZEROS TO SCT-SIRA.
           MOVE SC-HESAPNO TO SCT-HESAPNO.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL.
           COMPUTE V-TEL = SCT-TEL / 100000000.
           COMPUTE V-FAX = (SCT-TEL - V-TEL * 100000000) / 10000.
           COMPUTE V-CEP = SCT-TEL - V-TEL * 100000000 - V-FAX * 10000.
           IF V-TEL NOT = ZEROS MOVE V-TEL TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-TEL.
           IF V-FAX NOT = ZEROS MOVE V-FAX TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-FAX.
           IF V-CEP NOT = ZEROS MOVE V-CEP TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
                MOVE SCT-TEL TO B-CEP.
           CLOSE SICILTEL.
           
           
           
           DISPLAY SCR-BASLIK.
       TARIH-AL.
           DISPLAY SCR-TARIH.
           ACCEPT SCR-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO BEGIN-PROGRAM.

       DEPO-AL.
           MOVE ZEROS TO T-DEPONO.
           MOVE T-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0106 0006 0115 0010 1.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO TARIH-AL.
           MOVE T-KOD TO T-DEPONO.
           DISPLAY T-ADI LINE 6 POSITION 106 SIZE 10
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'.
       
       START-EKSTRE.
           MOVE ZEROS TO T-BORC T-ALACAK.
           OPEN INPUT CARHAR DEPO.

           COMPUTE T1 = BA-GUN + BA-AY * 30,41 + BA-YIL * 365.
           COMPUTE T2 = BT-GUN + BT-AY * 30,41 + BT-YIL * 365.
          
           MOVE SC-HESAPNO TO CH-HESAPNO.
           START CARHAR KEY IS EQUAL TO CH-HESAPNO
            INVALID KEY CLOSE CARHAR DEPO
                IF GORUNTU = 1 GO STOP-PROGRAM END-IF
                GO BEGIN-PROGRAM.
           
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
       CARHAR-OKU.
           READ CARHAR NEXT AT END GO CARHAR-OKU-SON.
           IF CH-HESAPNO NOT = SC-HESAPNO GO CARHAR-OKU-SON.
           IF CH-SIRA = ZEROS GO CARHAR-OKU.
           IF T-DEPONO NOT = ZEROS AND CH-DEPONO NOT = T-DEPONO 
                GO CARHAR-OKU.
           COMPUTE TT =  CH-GUN + CH-AY * 30,41 + CH-YIL * 365.
           IF TT > T2 GO CARHAR-OKU.
           IF TT < T1 PERFORM DEVIR-EKLE GO CARHAR-OKU.
           MOVE CH-TARIH TO TP-TARIH.
           MOVE CH-EVRAKNO TO TP-EVRAKNO.
           MOVE CH-TIP TO TP-TIP.
           MOVE CH-DEPONO TO TP-DEPONO DP-NO.
           MOVE CH-SIRA TO TP-CSIRA.
           
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           
           MOVE DP-ADI TO TP-DPADI.
           MOVE CH-ACIKLAMA TO TP-ACIKLAMA.
           MOVE CH-BORC TO TP-BORC.
           MOVE CH-ALACAK TO TP-ALACAK.
           MOVE CH-VADE TO TP-VADE.
           MOVE 1 TO TP-SIRA.
       TEMP-YAZ1.
           WRITE TP-KAYIT INVALID KEY ADD 1 TO TP-SIRA GO TEMP-YAZ1.
           GO CARHAR-OKU.
       CARHAR-OKU-SON.
           CLOSE CARHAR DEPO.
           GO BAKIYE-AL.
           
       DEVIR-EKLE.
           MOVE BA-TARIH TO TP-TARIH.
           MOVE ZEROS TO TP-SIRA.
           MOVE CH-DEPONO TO TP-DEPONO.
           READ TEMPFILE INVALID KEY 
              MOVE SPACES TO TP-VADE
              MOVE 'Devir' TO TP-EVRAKNO
              MOVE '™nceki D”nemden Devir' TO TP-ACIKLAMA
              MOVE ZEROS TO TP-TIP
              MOVE CH-DEPONO TO TP-DEPONO DP-NO
              READ DEPO INVALID KEY MOVE SPACES TO DP-ADI END-READ
              MOVE DP-ADI TO TP-DPADI
              MOVE ZEROS TO TP-BORC TP-ALACAK TP-BAKIYE
              WRITE TP-KAYIT
           END-READ.
           ADD CH-BORC TO TP-BORC.
           ADD CH-ALACAK TO TP-ALACAK.
           REWRITE TP-KAYIT.

       BAKIYE-AL.
           MOVE ZEROS TO TP-TARIH TP-DEPONO TP-SIRA.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
            INVALID KEY 
             CLOSE TEMPFILE DELETE FILE TEMPFILE 
               IF GORUNTU = 1 GO STOP-PROGRAM END-IF
               GO BEGIN-PROGRAM.
       BAKIYE-OKU.
           READ TEMPFILE NEXT AT END GO BAKIYE-AL-SON.
           ADD TP-BORC TO T-BORC.
           ADD TP-ALACAK TO T-ALACAK.
           COMPUTE TP-BAKIYE = T-BORC - T-ALACAK.
           REWRITE TP-KAYIT.
           GO BAKIYE-OKU.
       BAKIYE-AL-SON.
           CLOSE TEMPFILE.
           DISPLAY SCR-TOPLAM.
           
           PERFORM CBAKIYE-OKU.
           MOVE DP-BAKIYE(1) TO B-BAKIYE.
           DISPLAY '1.HESAP :' LINE 34 POSITION 2
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
            B-BAKIYE CONTROL 'FCOLOR=WHITE'.
           MOVE DP-BAKIYE(2) TO B-BAKIYE.
           DISPLAY '2.HESAP :' LINE 35 POSITION 2
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
            B-BAKIYE CONTROL 'FCOLOR=WHITE'.
           
           MOVE TP-BAKIYE TO B-BAKIYE.
           IF T-BORC > T-ALACAK
               DISPLAY B-BAKIYE LINE 35 POSITION 76
                CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
           ELSE
               DISPLAY B-BAKIYE LINE 35 POSITION 91
           END-IF.
           MOVE ILK TO SATIR.
           OPEN INPUT TEMPFILE.
           MOVE ZEROS TO TP-TARIH TP-DEPONO TP-SIRA.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
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
                     SPACES SIZE 10 'º' SPACES SIZE 5 'º' 
                     SPACES SIZE 10 'º' SPACES SIZE 10 'º' 
                     SPACES SIZE 7 'º' SPACES SIZE 26 'º' 
                     SPACES SIZE 14 'º' SPACES SIZE 14 'º'
                     SPACES SIZE 14 'º'
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE ZEROS TO TP-TARIH TP-DEPONO TP-SIRA.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           DISPLAY F LINE 36 POSITION 1.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO EKSTRE-CIKIS.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 GO EKSTRE-YAZDIR.
           IF F = 38 GO EKSTRE-SATIR-SIL.
           IF F = 13 GO GORUNTU.
           IF F = 2 CALL 'AYAR' USING 15 CANCEL 'AYAR' GO TUS.
           GO TUS.
       copy 'onayper.cpy'.
       EKSTRE-CIKIS.
           CLOSE TEMPFILE. DELETE FILE TEMPFILE.
           MOVE SPACES TO MAIN-ARGUMENT-VALUE.
           STRING BA-TARIH DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  BT-TARIH DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  T-DEPONO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  SC-HESAPNO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  GORUNTU DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  ISLEM DELIMITED BY SIZE
                  ';'  DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
           END-STRING.       
           MOVE 100 TO MAIN-ARGUMENT-SIZE.
           IF GORUNTU = 1 GO STOP-PROGRAM.
           GO BEGIN-PROGRAM.
       GORUNTU.
           MOVE 1 TO GORUNTU.
           MOVE SPACES TO MAIN-ARGUMENT-VALUE.
           STRING TP-TIP ';' TP-EVRAKNO 
                  ';' GORUNTU ';' ISLEM ';' 
                  DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
           END-STRING.
           MOVE 100 TO MAIN-ARGUMENT-SIZE.
           EVALUATE TP-TIP
           WHEN 1 WHEN 4 WHEN 7 WHEN 10 
           WHEN 3 WHEN 6 WHEN 9 WHEN 12 WHEN 13 WHEN 14
               CALL 'HAREKET' USING MAIN-ARGUMENT
               CANCEL 'HAREKET'
           WHEN 16 WHEN 17
               CALL 'MAKBUZ' USING MAIN-ARGUMENT
               CANCEL 'MAKBUZ'
           WHEN 18 WHEN 19 WHEN 20 WHEN 21
               MOVE 0 TO ISLEM
               CALL 'CEKSENET' USING MAIN-ARGUMENT 
               CANCEL 'CEKSENET'
           END-EVALUATE.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
               INTO TP-TIP, TP-EVRAKNO, GORUNTU, ISLEM
           END-UNSTRING.
           IF ISLEM = 1 CLOSE TEMPFILE DELETE FILE TEMPFILE
                        GO START-EKSTRE.
           GO TUS.
       EKSTRE-YAZDIR.
           MOVE 'EKSTRE YAZDIRILACAK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           
           MOVE 'HER HESAP AYRI YAZDIRILSIN MI ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF F = 27 GO TUS.
           
           IF E-ONAY = 'H' OR E-ONAY = 'h'
            CALL 'CEKSTRE' USING TSAAT SC-HESAPNO T-DEPONO 
                        BA-TARIH BT-TARIH
            CANCEL 'CEKSTRE'
           ELSE PERFORM AYRI-YAZDIR THRU AYRI-YAZDIR-SON
           END-IF.
           GO TUS.
       AYRI-YAZDIR.
           OPEN INPUT DEPO.
       AYRI-OKU.
           READ DEPO NEXT AT END GO AYRI-YAZDIR-SON.
           CALL 'CEKSTRE' USING TSAAT SC-HESAPNO DP-NO 
                        BA-TARIH BT-TARIH.
           CANCEL 'CEKSTRE'.
           GO AYRI-OKU.
       AYRI-YAZDIR-SON.
           CLOSE DEPO.
           
       CTHO-TUSU.
           MOVE ZEROS TO TP-TARIH TP-DEPONO TP-SIRA.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999999 TO TP-TARIH.
           MOVE 9999 TO TP-DEPONO.
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
           MOVE SPACES TO B-TIP.
           EVALUATE TP-TIP
           WHEN 1 WHEN 4 WHEN 7 WHEN 10 
                MOVE '˜ft.' TO B-TIP
           WHEN 3 WHEN 6 WHEN 9 WHEN 12 WHEN 13 WHEN 14
               MOVE 'Fat.' TO B-TIP
           WHEN 16 WHEN 17
               MOVE 'Mak.' TO B-TIP
           WHEN 18 WHEN 19
               MOVE '€ek' TO B-TIP
           WHEN 20 WHEN 21
               MOVE 'Senet' TO B-TIP
           END-EVALUATE.
           MOVE TP-DPADI TO B-DEPONO.
           MOVE TP-EVRAKNO TO B-EVRAKNO.
           MOVE TP-VADE TO B-VADE.
           MOVE TP-ACIKLAMA TO B-ACIKLAMA.
           MOVE TP-BORC TO B-BORC.
           MOVE TP-ALACAK TO B-ALACAK
           MOVE TP-BAKIYE TO B-BAKIYE.
           
           MOVE TP-GUN TO BK-GUN.
           MOVE TP-AY TO BK-AY.
           MOVE TP-YIL TO BK-YIL.
           MOVE TP-DEPONO TO BK-DEPONO.
           MOVE TP-CSIRA TO BK-SIRA.
           MOVE SC-HESAPNO TO BK-HESAPNO.
           
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       EKSTRE-SATIR-SIL.
           MOVE 0 TO ISLEM.
           MOVE 'EKSTRE SADECE SATIR S˜LS˜N M˜ ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           IF F = 27 GO TUS.
           OPEN I-O CARHAR.
           MOVE BILGI-KEY TO CH-KEY.
           READ CARHAR NOT INVALID KEY
                PERFORM DELETE-CARHAR
                MOVE 1 TO ISLEM
           END-READ.
           CLOSE CARHAR.
           IF ISLEM = 1 CLOSE TEMPFILE DELETE FILE TEMPFILE
                        GO START-EKSTRE.
           GO TUS.
           
       CBAKIYE-OKU.
           OPEN INPUT CARHAR.
           PERFORM VARYING DPI FROM 1 BY 1 UNTIL DPI = 3
            MOVE SC-HESAPNO TO CH-HESAPNO
            MOVE ZEROS TO CH-SIRA CH-TARIH
            MOVE BT-YIL TO CH-YIL
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
                  READ CARHAR INVALID KEY 
                    MOVE ZEROS TO CH-BORC CH-ALACAK
                  END-READ
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
       copy fileop.cpy REPLACING ==%T%== BY ==CARHAR==
                                  "%T%"  BY "CARHAR"
                                 ==%R%== BY ==CH-KAYIT==.   
           