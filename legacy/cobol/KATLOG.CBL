       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KATLOG'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'stok.cpy'.
       copy 'katlog.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'katlog.cpz'.
       WORKING-STORAGE SECTION.
       77 V-ADET          PIC S9(12)V9999.
       77 MAX-YIL           PIC 9999.
       01 BILGI.
           02 B-STNO            PIC X(6).
           02 FILLER            PIC X VALUE 'º'.
           02 B-PRCNO           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TIPI            PIC X(11).
           02 FILLER            PIC X VALUE 'º'.
           02 B-OEMNO           PIC X(8).
           02 FILLER            PIC X VALUE 'º'.
           02 B-CINSI           PIC X(29).
           02 FILLER            PIC X VALUE 'º'.
           02 B-MARKA           PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-NOTE            PIC X(5).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ADET            PIC ---.---,--.
           02 FILLER            PIC X VALUE 'º'.
           02 B-FIYAT           PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-MIN             PIC Z.ZZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-DEG             PIC XXXX.
           

       77 A-STNO        PIC X(15).
       77 A-PRCNO       PIC X(30).
       77 A-OEMNO       PIC X(30).
       77 A-TIPI        PIC X(30).
       77 A-CINSI       PIC X(40).
       77 KILIT         PIC 9.
       77 F             PIC 99.
       77 SATIR         PIC 99.
       77 SAYAC         PIC 9999.
       77 VR-STOK               PIC X(8).
       77 DUR           PIC X.
       77 KZXXX         PIC 9.       
       77 DP-STNO               PIC X(15).
       77 LIS           PIC 99.
       77 DOS-ICIN              PIC X(12).
       77 ST1                   PIC X(15) VALUE ALL ' '.
       77 ST2                   PIC X(15) VALUE ALL ' '.
       77 V-KATNO               PIC 9999.
       77 MYCONTROL             PIC X(60).
       77 TEMP-INDEX            PIC 9.
       01 TEMP-KATNO.
           02 T-KATNO           PIC 9999.
           02 T-STNO            PIC X(15).
       01 SON-KAYIT.
           02 S-KATNO           PIC 9999.
           02 FILLER            PIC X(100) VALUE ALL 'ÿ'.
       77 SEC                 PIC 9.
       77 G-KATNO             PIC 9999.
       77 G-STNO              PIC X(15).
           
       copy 'status.cpy'.
       copy 'win.cpy'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       SCREEN SECTION.
       01 SCR.
           02 LINE 1 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE 'É' 
           HIGHLIGHT.
           02 PIC X(6) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(8) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(29) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 '»' HIGHLIGHT.
           02 LINE 2 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE 'º' 
           HIGHLIGHT.
           02 PIC X(6) FROM 'STOK NO' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(10) FROM 'PAR€A NO' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(11) FROM 'T˜P˜' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(8) FROM 'OEM NO' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(29) FROM 'C˜NS˜' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(10) FROM 'MARKA' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(5) FROM 'NOT' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(10) FROM 'ADET' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(10) FROM 'F˜YAT' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(5) FROM 'M˜N' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 PIC X(4) FROM 'DE¦.' HIGHLIGHT. 02 'º' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE 'Ì' 
           HIGHLIGHT.
           02 PIC X(6) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(8) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(29) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 '¹' HIGHLIGHT.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE 'È' 
           HIGHLIGHT.
           02 PIC X(6) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(8) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(29) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(5) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 '¼' HIGHLIGHT.
       
       
       
       01 SCR-ARAMA.
           02 LINE 16 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'É' HIGHLIGHT. 02 PIC X(58) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 17 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'º Stok No' HIGHLIGHT. 02 PIC X(50) FROM ALL ' '.
           02 'º' HIGHLIGHT.
           02 LINE 18 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'º Par‡a No' HIGHLIGHT. 02 PIC X(49) FROM ALL ' '.
           02 'º' HIGHLIGHT.
           02 LINE 19 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'º Oem No' HIGHLIGHT. 02 PIC X(51) FROM ALL ' '.
           02 'º' HIGHLIGHT.
           02 LINE 20 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'º Tipi' HIGHLIGHT. 02 PIC X(53) FROM ALL ' '.
           02 'º' HIGHLIGHT.
           02 LINE 21 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'º Stok Cinsi' HIGHLIGHT. 02 PIC X(47) FROM ALL ' '.
           02 'º' HIGHLIGHT.
           02 LINE 22 COLUMN 5 BACKGROUND BLACK FOREGROUND WHITE
            'È' HIGHLIGHT. 02 PIC X(58) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
               INTO SEC, G-KATNO, G-STNO
           END-UNSTRING.
           OPEN INPUT KATLOG.
       KATLOG-LOAD.       
           IF G-KATNO = ZEROS MOVE 1 TO G-KATNO.
           MOVE G-KATNO TO KT-KATNO.
           READ KATLOG INVALID KEY
               CALL 'KATPEN' USING 1 G-KATNO  
               CANCEL 'KATPEN' GO KATLOG-LOAD.
           MOVE KT-KATNO TO V-KATNO.
           CLOSE KATLOG.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
       BASLA.
           DISPLAY SPACES ERASE CONTROL 'BCOLOR=BLUE'.
           PERFORM VARYING SATIR FROM 4 BY 1 UNTIL SATIR > 35
                 DISPLAY 'º' LINE SATIR POSITION 1
                        CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                         SPACES SIZE 6 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 11 'º' SPACES SIZE 8 'º'
                         SPACES SIZE 29 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 5 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 10 'º' SPACES SIZE 5 'º'
                         SPACES SIZE 4 'º' 
           END-PERFORM.
           DISPLAY SCR.
           
           DISPLAY ' F1 ' LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                'KATALOG' REVERSE LOW ' F2 ' 'YEN˜' REVERSE LOW
                ' F4 ' 'ARA' REVERSE LOW
                ' F9 ' 'ANAL˜Z' REVERSE LOW.
                
           MOVE 0 TO TEMP-INDEX.
           
      *   DISPLAY '[' LINE 37 POSITION 30 SEC '],[' 
      *            G-KATNO '],[' G-STNO ']'.
           
           OPEN INPUT KATLOG.
           MOVE V-KATNO TO KT-KATNO.
           READ KATLOG INVALID KEY CLOSE KATLOG GO DOSYA-GIRIS.
           DISPLAY '[' LINE 1 POSITION 2
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                SPACES SIZE 60 ']'
                KT-ACIKLAMA LINE 1 POSITION 3
                  CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           CLOSE KATLOG.
       DOSYA-GIRIS.
           OPEN INPUT STOKARA STOK STOKADET.
           MOVE SPACES TO SA-STNO.
           MOVE V-KATNO TO SA-STNO.  
           PERFORM MAX-YIL-AL THRU MAX-YIL-AL-SON.
           
           START STOKARA KEY IS NOT LESS THAN SA-STNO
                INVALID KEY MOVE 1 TO KILIT GO TUS.
           READ STOKARA NEXT.
           MOVE ZEROS TO KILIT.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO GO TUS.
           
           MOVE 4 TO SATIR.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = 4 PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = 35 GO A-LISTE-SON.
           READ STOKARA NEXT AT END
                READ STOKARA PREVIOUS GO A-LISTE-SON.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO
               READ STOKARA PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = 35 ADD 1 TO SATIR
              PERFORM VARYING SATIR FROM SATIR BY 1 UNTIL SATIR > 35
                   DISPLAY 'º' LINE SATIR POSITION 1
                        CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                         SPACES SIZE 6 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 11 'º' SPACES SIZE 8 'º'
                         SPACES SIZE 29 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 5 'º' SPACES SIZE 10 'º' 
                         SPACES SIZE 10 'º' SPACES SIZE 5 'º'
                         SPACES SIZE 4 'º' 
              END-PERFORM MOVE LIS TO SATIR.

       ILK-SERIT.
           MOVE SPACES TO SA-STNO.
           MOVE V-KATNO TO SA-STNO.  
           START STOKARA KEY IS NOT LESS THAN SA-STNO.
           READ STOKARA NEXT.
           MOVE 4 TO SATIR.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR NO BEEP LINE 37 POSITION 1 
            UPDATE CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY F LINE 36 POSITION 1.
           IF F = 1 CALL 'KATPEN' USING 1 V-KATNO CANCEL 'KATPEN' 
                    CLOSE STOK STOKARA STOKADET GO BASLA.
           IF F = 54 AND KILIT = 0 GO HOME-TUSU.
           IF F = 82 AND KILIT = 0 GO END-TUSU.
           IF F = 68 AND KILIT = 0 GO PGDN-TUSU.
           IF F = 67 AND KILIT = 0 GO PGUP-TUSU.
           IF F = 81 AND KILIT = 0 GO CTHO-TUSU.
           IF F = 83 AND KILIT = 0 GO CTEN-TUSU.
           IF F = 52 AND KILIT = 0 GO Y-AL.
           IF F = 53 AND KILIT = 0 GO A-AL.
           IF F = 9 CALL 'STKANZ' USING V-KATNO ST-STNO MAX-YIL 0
                    CANCEL 'STKANZ' GO TUS.
           IF F = 2 MOVE SPACES TO ST-STNO
                    CALL 'KATYENI' USING 0 V-KATNO ST-STNO MAX-YIL
                    CANCEL 'KATYENI' 
                    GO TAR-STNO-DIREK.
           IF F = 13 AND SEC = 0 
                    MOVE ST-STNO TO A-STNO
                    CALL 'KATYENI' USING 1 V-KATNO A-STNO MAX-YIL
                    CANCEL 'KATYENI' 
                    GO TAR-STNO-DIREK.
           IF F = 13 AND SEC = 1 CLOSE STOKARA STOK STOKADET
                    MOVE V-KATNO TO G-KATNO
                    MOVE ST-STNO TO G-STNO
                    MOVE SPACES TO MAIN-ARGUMENT-VALUE
                    STRING SEC DELIMITED BY SIZE 
                           ';' DELIMITED BY SIZE
                           G-KATNO DELIMITED BY SIZE
                           ';' DELIMITED BY SIZE
                           G-STNO DELIMITED BY SIZE
                           ';' DELIMITED BY SIZE
                           INTO MAIN-ARGUMENT-VALUE
                    END-STRING
                    MOVE 100 TO MAIN-ARGUMENT-SIZE
                    GO STOP-PROGRAM.
           IF F = 4  GO TARAMA.
           IF F = 27 CLOSE STOKARA STOK STOKADET
                    MOVE ZEROS TO G-KATNO
                    MOVE SPACES TO G-STNO MAIN-ARGUMENT-VALUE
                    STRING SEC DELIMITED BY SIZE 
                           ';' DELIMITED BY SIZE
                           G-KATNO DELIMITED BY SIZE
                           ';' DELIMITED BY SIZE
                           G-STNO DELIMITED BY SIZE
                           ';' DELIMITED BY SIZE
                           INTO MAIN-ARGUMENT-VALUE
                    END-STRING
                    MOVE 100 TO MAIN-ARGUMENT-SIZE
                    GO STOP-PROGRAM.
           GO TUS.
       
       CTHO-TUSU.
           MOVE SPACES TO SA-STNO SA-PRCNO SA-OEMNO SA-TIPI SA-CINSI.
           MOVE V-KATNO TO SA-STNO SA-PRCNO SA-OEMNO SA-TIPI SA-CINSI.
           IF TEMP-INDEX = 0
              START STOKARA KEY IS NOT LESS THAN SA-STNO
           ELSE IF TEMP-INDEX = 1
              START STOKARA KEY IS NOT LESS THAN SA-PRCNO
           ELSE IF TEMP-INDEX = 2
              START STOKARA KEY IS NOT LESS THAN SA-OEMNO
           ELSE IF TEMP-INDEX = 3
              START STOKARA KEY IS NOT LESS THAN SA-TIPI
           ELSE IF TEMP-INDEX = 4
              START STOKARA KEY IS NOT LESS THAN SA-CINSI
           END-IF.
           READ STOKARA NEXT.
           MOVE 4 TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = 35 GO END-TUSU.
           MOVE V-KATNO TO S-KATNO.
           MOVE SON-KAYIT TO SA-STNO SA-PRCNO SA-OEMNO SA-TIPI SA-CINSI.
           IF TEMP-INDEX = 0
              START STOKARA KEY IS NOT GREATER THAN SA-STNO
           ELSE IF TEMP-INDEX = 1
              START STOKARA KEY IS NOT GREATER THAN SA-PRCNO
           ELSE IF TEMP-INDEX = 2
              START STOKARA KEY IS NOT GREATER THAN SA-OEMNO
           ELSE IF TEMP-INDEX = 3
              START STOKARA KEY IS NOT GREATER THAN SA-TIPI
           ELSE IF TEMP-INDEX = 4
              START STOKARA KEY IS NOT GREATER THAN SA-CINSI
           END-IF.
           READ STOKARA NEXT.
           MOVE 35 TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
             PERFORM 31 TIMES
               READ STOKARA NEXT
             END-PERFORM.
           PERFORM AKTAR.
           MOVE 35 TO SATIR LIS.
           GO TUS.
           
       PGUP-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - 4.
             PERFORM SATIR TIMES
               READ STOK PREVIOUS
             END-PERFORM.
           READ STOKARA PREVIOUS AT END READ STOKARA NEXT
              MOVE 4 TO SATIR PERFORM AKTAR PERFORM SERITLI GO TUS.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO READ STOKARA NEXT
              MOVE 4 TO SATIR PERFORM AKTAR PERFORM SERITLI GO TUS.
           MOVE 35 TO SATIR.
       PG-OKU.
           IF SATIR = 4 GO PG-OKU-SON.
           READ STOKARA PREVIOUS AT END READ STOKARA NEXT GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - 4.
               PERFORM SATIR TIMES
                   READ STOKARA PREVIOUS
               END-PERFORM.
           MOVE 4 TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
                PERFORM SATIR TIMES
                    READ STOKARA NEXT
                END-PERFORM.
           READ STOKARA NEXT AT END READ STOKARA PREVIOUS
              MOVE LIS TO SATIR PERFORM AKTAR PERFORM SERITLI GO TUS.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO
              MOVE LIS TO SATIR PERFORM AKTAR PERFORM SERITLI GO TUS.
           MOVE 4 TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           MOVE 5 TO SATIR.
           READ STOKARA NEXT AT END READ STOKARA PREVIOUS
              MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ STOKARA NEXT
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - 4.
               PERFORM SATIR TIMES
                  READ STOKARA PREVIOUS
               END-PERFORM.
           MOVE 4 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       A-AL.
           READ STOKARA NEXT AT END READ STOKARA PREVIOUS GO TUS.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO READ STOKARA PREVIOUS GO TUS.
           IF SATIR = 35 PERFORM Y-LISTE THRU Y-LISTE-SON
                PERFORM 31 TIMES READ STOKARA NEXT END-PERFORM
                PERFORM AKTAR MOVE 35 TO SATIR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR.
           PERFORM SERITLI.
           GO TUS.
       Y-AL.
           READ STOKARA PREVIOUS AT END READ STOKARA NEXT GO TUS.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO READ STOKARA NEXT GO TUS.
           IF SATIR = 4 PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - 4
                PERFORM SATIR TIMES READ STOKARA PREVIOUS END-PERFORM
                PERFORM AKTAR MOVE 4 TO SATIR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR.
           PERFORM SERITLI.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = 35 PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = 4 GO Y-LISTE-SON.
           READ STOKARA PREVIOUS AT END
               READ STOKARA NEXT GO Y-LISTE-SON.
           PERFORM KATNO-AL.
           IF T-KATNO NOT = V-KATNO READ STOKARA NEXT GO TUS.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SA-STNO TO TEMP-KATNO.
           MOVE T-KATNO TO ST-KATNO.
           MOVE T-STNO TO ST-STNO.
           READ STOK.
           MOVE ST-STNO TO B-STNO.
           MOVE ST-PRCNO TO B-PRCNO.
           MOVE ST-TIPI TO B-TIPI.
           MOVE ST-CINSI TO B-CINSI.
           MOVE ST-MARKA TO B-MARKA.
           MOVE ST-OEMNO TO B-OEMNO.
           MOVE ST-NOTE TO B-NOTE.
           PERFORM ADET-AKTAR THRU ADET-AKTAR-SON.
           MOVE V-ADET TO B-ADET.
           MOVE ST-FIYAT TO B-FIYAT.
           MOVE ST-MIN TO B-MIN.
           STRING ST-AY ST-YIL(3:2) 
            DELIMITED BY SIZE INTO B-DEG.
           IF B-DEG = '0000' MOVE SPACES TO B-DEG.
           IF V-ADET > 0
            MOVE 'BCOLOR=BLUE, FCOLOR=WHITE' TO MYCONTROL.
           IF V-ADET = 0
            MOVE 'BCOLOR=BLUE, FCOLOR=BLACK' TO MYCONTROL.
           IF V-ADET < 0
            MOVE 'BCOLOR=BLUE, FCOLOR=RED' TO MYCONTROL.
           
       ADET-AKTAR.
           MOVE ST-KATNO TO STA-KATNO.
           MOVE MAX-YIL TO STA-YIL.
           MOVE ST-STNO TO STA-STNO.
           MOVE ZEROS TO STA-DEPONO V-ADET.
           START STOKADET KEY IS NOT LESS THAN STA-KEY
            INVALID KEY GO ADET-AKTAR-SON.
       ADET-AKTAR-OKU.
           READ STOKADET NEXT AT END GO ADET-AKTAR-SON.
           IF STA-KATNO NOT = ST-KATNO OR STA-STNO NOT = ST-STNO
                        GO ADET-AKTAR-SON.
           IF STA-YIL NOT = MAX-YIL GO ADET-AKTAR-SON.
           COMPUTE V-ADET = V-ADET + (STA-GIR - STA-CIK).
           GO ADET-AKTAR-OKU.
       ADET-AKTAR-SON. 
       MAX-YIL-AL.
            MOVE 9999 TO STA-YIL.
            START STOKADET KEY IS NOT GREATER THAN STA-YIL
                INVALID KEY MOVE ZEROS TO MAX-YIL GO MAX-YIL-AL-SON.
            READ STOKADET NEXT.
            MOVE STA-YIL TO MAX-YIL.
       MAX-YIL-AL-SON.
            
       SERITSIZ.
           DISPLAY B-STNO LINE SATIR POSITION 2
              CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-PRCNO CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-TIPI CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-OEMNO CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-CINSI CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-MARKA CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-NOTE CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-ADET CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-FIYAT CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-MIN CONTROL MYCONTROL
              'º' CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
              B-DEG CONTROL MYCONTROL.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE'.
              
       TARAMA.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SCR-ARAMA.
           CALL 'GOLGE' USING 0016 0005 0022 0064.
           CANCEL 'GOLGE'.
       TAR-STNO.
           DISPLAY SPACES LINE 17 POSITION 18
                CONTROL 'BCOLOR=WHITE' SIZE 17.
           ACCEPT A-STNO NO BEEP LINE 17 POSITION 19
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 17 POSITION 18
                CONTROL 'BCOLOR=BLACK' SIZE 17.
           IF F = 52 GO TAR-CINSI.
           IF F = 53 GO TAR-PRCNO.
           IF F = 27 PERFORM TARAMA-CIKIS GO TUS.
           IF F NOT = 13 GO TAR-STNO.
           PERFORM TARAMA-CIKIS.
       TAR-STNO-DIREK.   
           STRING V-KATNO A-STNO DELIMITED BY SIZE
            INTO SA-STNO.
           START STOKARA KEY IS NOT LESS THAN SA-STNO
            INVALID KEY CLOSE STOK STOKARA STOKADET GO BASLA.
           READ STOKARA NEXT.
           MOVE 4 TO SATIR.
           MOVE 0 TO TEMP-INDEX.
           PERFORM A-LISTE THRU A-LISTE-SON.
           GO HOME-TUSU.
       TAR-PRCNO.
           DISPLAY SPACES LINE 18 POSITION 18
                CONTROL 'BCOLOR=WHITE' SIZE 32.
           ACCEPT A-PRCNO NO BEEP LINE 18 POSITION 19
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 18 POSITION 18
                CONTROL 'BCOLOR=BLACK' SIZE 32.
           IF F = 52 GO TAR-STNO.
           IF F = 53 GO TAR-OEMNO.
           IF F = 27 PERFORM TARAMA-CIKIS GO TUS.
           IF F NOT = 13 GO TAR-PRCNO.
           PERFORM TARAMA-CIKIS.
           STRING V-KATNO A-PRCNO DELIMITED BY SIZE
            INTO SA-PRCNO.
           START STOKARA KEY IS NOT LESS THAN SA-PRCNO
            INVALID KEY CLOSE STOK STOKARA STOKADET GO BASLA.
           READ STOKARA NEXT.
           MOVE 1 TO TEMP-INDEX.
           MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           GO HOME-TUSU.
       TAR-OEMNO.
           DISPLAY SPACES LINE 19 POSITION 18
                CONTROL 'BCOLOR=WHITE' SIZE 32.
           ACCEPT A-OEMNO NO BEEP LINE 19 POSITION 19
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 19 POSITION 18
                CONTROL 'BCOLOR=BLACK' SIZE 32.
           IF F = 52 GO TAR-PRCNO.
           IF F = 53 GO TAR-TIPI.
           IF F = 27 PERFORM TARAMA-CIKIS GO TUS.
           IF F NOT = 13 GO TAR-OEMNO.
           PERFORM TARAMA-CIKIS.
           STRING V-KATNO A-OEMNO DELIMITED BY SIZE
            INTO SA-OEMNO.
           START STOKARA KEY IS NOT LESS THAN SA-OEMNO
            INVALID KEY CLOSE STOK STOKARA STOKADET GO BASLA.
           READ STOKARA NEXT.
           MOVE 2 TO TEMP-INDEX.
           MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           GO HOME-TUSU.
       TAR-TIPI.
           DISPLAY SPACES LINE 20 POSITION 18
                CONTROL 'BCOLOR=WHITE' SIZE 32.
           ACCEPT A-TIPI NO BEEP LINE 20 POSITION 19
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 20 POSITION 18
                CONTROL 'BCOLOR=BLACK' SIZE 32.
           IF F = 52 GO TAR-OEMNO.
           IF F = 53 GO TAR-CINSI.
           IF F = 27 PERFORM TARAMA-CIKIS GO TUS.
           IF F NOT = 13 GO TAR-OEMNO.
           PERFORM TARAMA-CIKIS.
           STRING V-KATNO A-TIPI DELIMITED BY SIZE
            INTO SA-TIPI.
           START STOKARA KEY IS NOT LESS THAN SA-TIPI
            INVALID KEY CLOSE STOK STOKARA STOKADET GO BASLA.
           READ STOKARA NEXT.
           MOVE 3 TO TEMP-INDEX.
           MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           GO HOME-TUSU.
       TAR-CINSI.
           DISPLAY SPACES LINE 21 POSITION 18
                CONTROL 'BCOLOR=WHITE' SIZE 42.
           ACCEPT A-CINSI NO BEEP LINE 21 POSITION 19
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SPACES LINE 21 POSITION 18
                CONTROL 'BCOLOR=BLACK' SIZE 42.
           IF F = 52 GO TAR-TIPI.
           IF F = 53 GO TAR-STNO.
           IF F = 27 PERFORM TARAMA-CIKIS GO TUS.
           IF F NOT = 13 GO TAR-CINSI.
           PERFORM TARAMA-CIKIS.
           STRING V-KATNO A-CINSI DELIMITED BY SIZE
            INTO SA-CINSI.
           START STOKARA KEY IS NOT LESS THAN SA-CINSI
            INVALID KEY CLOSE STOK STOKARA STOKADET GO BASLA.
           READ STOKARA NEXT.
           MOVE 4 TO TEMP-INDEX.
           MOVE 4 TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           GO HOME-TUSU.
            
       TARAMA-CIKIS.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       KATNO-AL.
           MOVE ZEROS TO TEMP-KATNO.
           IF TEMP-INDEX = 0
              MOVE SA-STNO TO TEMP-KATNO
           ELSE IF TEMP-INDEX = 1
              MOVE SA-PRCNO TO TEMP-KATNO
           ELSE IF TEMP-INDEX = 2
              MOVE SA-OEMNO TO TEMP-KATNO
           ELSE IF TEMP-INDEX = 3
              MOVE SA-TIPI TO TEMP-KATNO
           ELSE IF TEMP-INDEX = 4
              MOVE SA-CINSI TO TEMP-KATNO
           END-IF. 

       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
           
