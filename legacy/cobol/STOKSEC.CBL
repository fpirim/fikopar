       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'STOKSEC'.
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
       copy 'hareket.cpy'.
       copy 'katlog.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'hareket.cpz'.
       copy 'katlog.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
                03 TP-KATNO              PIC 9999.
                03 TP-STNO               PIC X(15).
           02 TP-PRCNO                  PIC X(30).
           02 TP-OEMNO                  PIC X(30).
           02 TP-TIPI                   PIC X(30).
           02 TP-CINSI                  PIC X(60).
           02 TP-MARKA                  PIC X(30).
           02 TP-ADET                   PIC S9(12)V9999.
           02 TP-FIYAT                  PIC 9(10)V9999.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999.
           02 SON                   PIC 9999.
           02 WIDTH                 PIC 9999.
           02 I                     PIC 9999.
           02 J                     PIC 9999.
           02 S-ADET                PIC S9(12)V9999.
           02 BUL                  PIC 9.
           02 T-KEY.
               03 T-KATNO           PIC 9999.
               03 T-STR             PIC X(60).
       01 BILGI.
           02 B-KATNO               PIC ZZZZ.
           02 FILLER                PIC X VALUE 'º'.
           02 B-STNO                PIC X(7).
           02 FILLER                PIC X VALUE 'º'.
           02 B-PRCNO               PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-OEMNO               PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-TIPI                PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-CINSI               PIC X(30).
           02 FILLER                PIC X VALUE 'º'.
           02 B-MARKA               PIC X(9).
           02 FILLER                PIC X VALUE 'º'.
           02 B-ADET                PIC ---.---,----.
           02 FILLER                PIC X VALUE 'º'.
           02 B-FIYAT               PIC Z.ZZZ.ZZZ,ZZZZ.
       copy 'trim.var'.
       copy 'status.cpy'.
       copy 'win.cpy'.
       copy 'onayvar.cpy'.
       LINKAGE SECTION.
       01 G-KAYIT.
           02 G-KEY.
               03 G-KATNO              PIC 9999.
               03 G-STNO               PIC X(15).
           02 G-PRCNO                  PIC X(30).
           02 G-OEMNO                  PIC X(30).
           02 G-TIPI                   PIC X(30).
           02 G-CINSI                  PIC X(60).
           02 G-MARKA                  PIC X(30).
           02 G-ADET                    PIC S9(12)V9999.
           02 G-FIYAT                  PIC 9(10)V9999.
           
       77 GE-YIL         PIC 9999.
       77 Y              PIC 9999.
       77 KNT            PIC 9.
	   copy 'fileop.var'.
       SCREEN SECTION.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING G-KAYIT GE-YIL Y KNT.
       START-PROGRAM.
           MOVE 'STOKLAR YšKLEN˜YOR ... (ESC - DURDUR)' TO ONAY-MESSAGE.
           PERFORM SHOW-LOAD-MESSAGE.
           MOVE G-CINSI TO TRIM-VALUE.
           PERFORM RTRIM-BLOCK.
           MOVE TRIM-SIZE TO I.
           
           ACCEPT TSAAT FROM TIME.
           OPEN INPUT KATLOG STOK STOKARA STOKADET OUTPUT TEMPFILE.
           CLOSE TEMPFILE. OPEN I-O TEMPFILE.
       KATLOG-OKU.    
           READ KATLOG NEXT AT END GO KATLOG-OKU-SON.
           IF KNT = 0 AND KT-HESAPNO NOT = SPACES GO KATLOG-OKU.
           IF KNT = 1 AND KT-HESAPNO = SPACES GO KATLOG-OKU.
           IF KNT = 1 AND KT-KATNO = G-KATNO GO KATLOG-OKU.
        
           PERFORM STNO-GORE.
           IF BUL = 1 PERFORM STOK-TARA THRU STOK-TARA-SON.
           PERFORM PRCNO-GORE.
           IF BUL = 2 PERFORM STOK-TARA THRU STOK-TARA-SON.
           PERFORM OEMNO-GORE.
           IF BUL = 3 PERFORM STOK-TARA THRU STOK-TARA-SON.
           PERFORM TIPI-GORE.
           IF BUL = 4 PERFORM STOK-TARA THRU STOK-TARA-SON.
           PERFORM CINSI-GORE.
           IF BUL = 5 PERFORM STOK-TARA THRU STOK-TARA-SON.
           GO KATLOG-OKU.
       copy 'trim.per'.
       copy 'onayper.cpy'.
       STOK-TARA.
           READ STOKARA NEXT AT END GO STOK-TARA-SON.
           PERFORM IS-INTERRUPT.
           IF INTERRUPT = 1 GO KATLOG-OKU-SON.
           EVALUATE BUL
           WHEN 1 MOVE SA-STNO TO T-KEY
           WHEN 2 MOVE SA-PRCNO TO T-KEY
           WHEN 3 MOVE SA-OEMNO TO T-KEY
           WHEN 4 MOVE SA-TIPI TO T-KEY
           WHEN 5 MOVE SA-CINSI TO T-KEY
           END-EVALUATE.
           IF T-KATNO NOT = KT-KATNO GO STOK-TARA-SON.
           MOVE T-STR TO TRIM-VALUE.
           PERFORM RTRIM-BLOCK.
           IF I > TRIM-SIZE GO STOK-TARA.
           IF G-CINSI(1:I) NOT = T-STR(1:I) GO STOK-TARA-SON.
           
           MOVE SA-STNO TO T-KEY.
           MOVE T-KATNO TO ST-KATNO.
           MOVE T-STR TO ST-STNO.
           READ STOK.
           MOVE ST-KATNO TO TP-KATNO.
           MOVE ST-STNO TO TP-STNO.
           MOVE ST-PRCNO TO TP-PRCNO.
           MOVE ST-OEMNO TO TP-OEMNO.
           MOVE ST-TIPI TO TP-TIPI.
           MOVE ST-CINSI TO TP-CINSI.
           MOVE ST-MARKA TO TP-MARKA.
           MOVE ST-FIYAT TO TP-FIYAT.
           PERFORM ADET-AKTAR THRU ADET-AKTAR-SON.
           MOVE S-ADET TO TP-ADET.
           WRITE TP-KAYIT INVALID KEY CONTINUE.
           GO STOK-TARA.
       STOK-TARA-SON.
       
       ADET-AKTAR.
           MOVE ST-KATNO TO STA-KATNO.
           MOVE GE-YIL TO STA-YIL.
           MOVE ST-STNO TO STA-STNO.
           MOVE ZEROS TO STA-DEPONO S-ADET.
           START STOKADET KEY IS NOT LESS THAN STA-KEY
            INVALID KEY GO ADET-AKTAR-SON.
       ADET-AKTAR-OKU.
           READ STOKADET NEXT AT END GO ADET-AKTAR-SON.
           IF STA-KATNO NOT = ST-KATNO OR STA-STNO NOT = ST-STNO
                        GO ADET-AKTAR-SON.
           IF STA-YIL NOT = GE-YIL GO ADET-AKTAR-SON.
           COMPUTE S-ADET = S-ADET + (STA-GIR - STA-CIK).
           GO ADET-AKTAR-OKU.
       ADET-AKTAR-SON.    
           
           
       STNO-GORE.
           STRING KT-KATNO G-CINSI DELIMITED BY SIZE
                INTO SA-STNO.
           START STOKARA KEY IS NOT LESS THAN SA-STNO
                INVALID KEY MOVE 0 TO BUL
              NOT INVALID KEY MOVE 1 TO BUL
           END-START.
           
       PRCNO-GORE.
           STRING KT-KATNO G-CINSI DELIMITED BY SIZE
                INTO SA-PRCNO.
           START STOKARA KEY IS NOT LESS THAN SA-PRCNO
                INVALID KEY MOVE 0 TO BUL
              NOT INVALID KEY MOVE 2 TO BUL
           END-START.
           
       OEMNO-GORE.
           STRING KT-KATNO G-CINSI DELIMITED BY SIZE
                INTO SA-OEMNO.
           START STOKARA KEY IS NOT LESS THAN SA-OEMNO
                INVALID KEY MOVE 0 TO BUL
              NOT INVALID KEY MOVE 3 TO BUL
           END-START.
           
       TIPI-GORE.
           STRING KT-KATNO G-CINSI DELIMITED BY SIZE
                INTO SA-TIPI.
           START STOKARA KEY IS NOT LESS THAN SA-TIPI
                INVALID KEY MOVE 0 TO BUL
              NOT INVALID KEY MOVE 4 TO BUL
           END-START.
           
       CINSI-GORE.
           STRING KT-KATNO G-CINSI DELIMITED BY SIZE
                INTO SA-CINSI.
           START STOKARA KEY IS NOT LESS THAN SA-CINSI
                INVALID KEY MOVE 0 TO BUL
              NOT INVALID KEY MOVE 5 TO BUL
           END-START.
       KATLOG-OKU-SON.         
           CLOSE KATLOG STOK STOKARA STOKADET TEMPFILE.
           PERFORM DISPOSE-LOAD-MESSAGE.
           OPEN INPUT TEMPFILE.
           MOVE ZEROS TO TP-KATNO.
           MOVE SPACES TO TP-STNO.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
            INVALID KEY 
             CLOSE TEMPFILE DELETE FILE TEMPFILE EXIT PROGRAM STOP RUN.
           READ TEMPFILE NEXT.
           READ TEMPFILE NEXT AT END MOVE TP-KAYIT TO G-KAYIT
             CLOSE TEMPFILE DELETE FILE TEMPFILE EXIT PROGRAM STOP RUN.
           READ TEMPFILE PREVIOUS.
       BEGIN-PROGRAM.      
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           COMPUTE ILK = Y + 1.
           COMPUTE SON = Y + 10.
           ADD 1 TO SON.
           DISPLAY 'É' LINE Y POSITION 2
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 4 'Ë' ALL 'Í' SIZE 7 'Ë' 
                ALL 'Í' SIZE 10 'Ë' ALL 'Í' SIZE 10 'Ë' 
                ALL 'Í' SIZE 10 'Ë' ALL 'Í' SIZE 30 'Ë' 
                ALL 'Í' SIZE 9 'Ë' ALL 'Í' SIZE 12 'Ë' 
                ALL 'Í' SIZE 14 '»'
                'È' LINE SON POSITION 2 
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 4 'Ê' ALL 'Í' SIZE 7 'Ê'
                ALL 'Í' SIZE 10 'Ê' ALL 'Í' SIZE 10 'Ê'
                ALL 'Í' SIZE 10 'Ê' ALL 'Í' SIZE 30 'Ê'
                ALL 'Í' SIZE 9 'Ê' ALL 'Í' SIZE 12 'Ê'
                ALL 'Í' SIZE 14 '¼'.
           SUBTRACT 1 FROM SON.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
              DISPLAY 'º' LINE CIZ POSITION 2
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   SPACES SIZE 4 'º' SPACES SIZE 7 'º'
                   SPACES SIZE 10 'º' SPACES SIZE 10 'º'
                   SPACES SIZE 10 'º' SPACES SIZE 30 'º'
                   SPACES SIZE 9 'º' SPACES SIZE 12 'º'
                   SPACES SIZE 14 'º'
           END-PERFORM.
           ADD 1 TO SON.
           CALL 'GOLGE' USING BY CONTENT Y 0002 SON 0117.
           CANCEL 'GOLGE'.
           SUBTRACT 1 FROM SON.
           
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
          
           MOVE ILK TO SATIR.
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
                 DISPLAY 'º' LINE CIZ POSITION 2
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   SPACES SIZE 4 'º' SPACES SIZE 7 'º'
                   SPACES SIZE 10 'º' SPACES SIZE 10 'º'
                   SPACES SIZE 10 'º' SPACES SIZE 30 'º'
                   SPACES SIZE 9 'º' SPACES SIZE 12 'º'
                   SPACES SIZE 14 'º'
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE ZEROS TO TP-KATNO.
           MOVE SPACES TO TP-STNO.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE TEMPFILE DELETE FILE TEMPFILE
                    GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 CLOSE TEMPFILE DELETE FILE TEMPFILE
                 MOVE TP-KAYIT TO G-KAYIT GO STOP-PROGRAM.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           GO TUS.
       CTHO-TUSU.
           MOVE ZEROS TO TP-KATNO.
           MOVE SPACES TO TP-STNO.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY.
           READ TEMPFILE NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO TP-KATNO.
           MOVE ALL 'ÿ' TO TP-STNO.
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
           MOVE TP-KATNO TO B-KATNO.
           MOVE TP-STNO TO B-STNO.
           MOVE TP-PRCNO TO B-PRCNO.
           MOVE TP-OEMNO TO B-OEMNO.
           MOVE TP-TIPI TO B-TIPI.
           MOVE TP-CINSI TO B-CINSI.
           MOVE TP-MARKA TO B-MARKA.
           MOVE TP-ADET TO B-ADET.
           MOVE TP-FIYAT TO B-FIYAT.
       
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
       