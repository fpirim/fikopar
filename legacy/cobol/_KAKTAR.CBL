       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KAKTAR'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
            SELECT OKU ASSIGN TO DISK, OKUDOS
                   ORGANIZATION LINE SEQUENTIAL
                   FILE STATUS IS ST-OKU.
            SELECT LOGFILE ASSIGN TO PRINT, YAZDOS.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
                    ORGANIZATION INDEXED
                    ACCESS MODE IS DYNAMIC
                    RECORD KEY IS TP-KEY.
       copy 'katlog.cpy'.
       copy 'stok.cpy'.
       copy 'hareket.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'katlog.cpz'.
       copy 'hareket.cpz'.
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-STNO               PIC X(15).
           02 TP-PRCNO                  PIC X(30).
           02 TP-OEMNO                  PIC X(30).
           02 TP-TIPI                   PIC X(30).
           02 TP-CINSI                  PIC X(60).
           02 TP-MARKA                  PIC X(30).
           02 TP-FIYAT                  PIC 9(10)V9999.
           02 TP-ESKIFIYAT              PIC 9(10)V9999.
           02 TP-TIP                PIC 99. 
       FD LOGFILE LABEL RECORD IS OMITTED.
       01 LGS                   PIC X(1000).
       FD OKU.
       01 DT-KAYIT.
           02 D-STOKNO          PIC X(15).
           02                   PIC X.
           02 D-PARCANO         PIC X(30).
           02                   PIC X.
           02 D-OEMNO           PIC X(30).
           02                   PIC X.
           02 D-TIPI            PIC X(30).
           02                   PIC X.
           02 D-STOKADI         PIC X(60).
           02                   PIC X.
           02 D-NOTE            PIC X(10).
           02                   PIC X.
           02 D-MARKA           PIC X(30).
           02                   PIC X.
           02 Z-FIYAT           PIC Z,ZZZ,ZZZ,ZZZ.ZZ.
           02                   PIC X.
           02 D-TARIH           PIC X(4).
           02                   PIC X.
           02 Z-PAKET           PIC ZZZZZ.
       
       WORKING-STORAGE SECTION.
       77 D-FIYAT               PIC 9(13)V9999.
       77 OKUDOS                PIC X(100).
       77 YAZDOS                PIC X(100).
       77 ST-OKU            PIC XX.
       77 SATIR             PIC 99.
       77 F             PIC 99.
       77 DUR           PIC X.
       77 SEC-KATNO         PIC 9999.
       77 SAYAC             PIC 9(10).
       77 Z-SAYAC           PIC Z,ZZZ,ZZZ,ZZZ.
       copy 'status.cpy'.
       copy 'onayvar.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       SCREEN SECTION.
       01 SCR.
           02 LINE 8 COLUMN 2 BACKGROUND WHITE FOREGROUND BLACK '…'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(25) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(15) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(6) FROM ALL 'Õ'. 02 'À'.
           02 PIC X(5) FROM ALL 'Õ'. 02 'ª'.
           02 LINE 9 COLUMN 2 '∫'.         
           02 PIC X(9) FROM 'STOK NO'.  02 '∫'.
           02 PIC X(9) FROM 'PARÄA NO'. 02 '∫'.
           02 PIC X(9) FROM 'OEM NO'. 02 '∫'.
           02 PIC X(9) FROM 'TòPò'.     02 '∫'.
           02 PIC X(25) FROM 'CòNSò'.    02 '∫'.
           02 PIC X(9) FROM 'NOT'.      02 '∫'.
           02 PIC X(9) FROM 'MARKA'.    02 '∫'.
           02 PIC X(15) FROM 'FòYAT'.    02 '∫'.
           02 PIC X(6) FROM 'TARòH'.    02 '∫'.
           02 PIC X(5) FROM 'PAKET'.    02 '∫'.
           02 LINE 10 COLUMN 2 'Ã'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(25) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(9) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(15) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(6) FROM ALL 'Õ'. 02 'Œ'.
           02 PIC X(5) FROM ALL 'Õ'. 02 'π'.
           02 LINE 30 COLUMN 2 PIC X(116) FROM ALL '.'.
           02 LINE 31 COLUMN 2 PIC X(116) FROM ALL '.'.
           02 LINE 32 COLUMN 2 PIC X(116) FROM ALL '.'.
       01 SCR-SATIR.
           02 LINE SATIR COLUMN 2 '∫'.         
           02 PIC X(9) USING ST-STNO.  02 '∫'.
           02 PIC X(9) USING ST-PRCNO. 02 '∫'.
           02 PIC X(9) USING ST-OEMNO.    02 '∫'.
           02 PIC X(9) USING ST-TIPI.    02 '∫'.
           02 PIC X(25) USING ST-CINSI. 02 '∫'.
           02 PIC X(9) USING ST-NOTE.    02 '∫'.
           02 PIC X(9) USING ST-MARKA.   02 '∫'.
           02 PIC ZZZZ,ZZZ,ZZZ.ZZ FROM ST-FIYAT. 02 '∫'.
           02 PIC ZZ USING ST-AY. 
           02 PIC ZZZZ USING ST-YIL. 02 '∫'.
           02 PIC ZZZZZ USING ST-PAKET. 02 '∫'.
           
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       DECLARATIVES.
       I-O-AYAR-ERROR SECTION.
           USE AFTER STANDARD EXCEPTION PROCEDURE ON OKU.
       I-O-AYAR-ROUTINE.
           IF ST-OKU = '35'
               STRING MAIN-ARGUMENT-VALUE DELIMITED BY SPACES
                        ' dosya bulunamadç. ! ' DELIMITED BY SIZE
                        INTO ONAY-MESSAGE
               PERFORM WARN-MESSAGE.
           EXIT PROGRAM.
           STOP RUN.
       copy 'onayper.cpy'.
       END DECLARATIVES.
       MAIN SECTION.
       START-PROGRAM.
           DISPLAY SPACES ERASE CONTROL 'BCOLOR=WHITE'.
           MOVE MAIN-ARGUMENT-VALUE(1:MAIN-ARGUMENT-SIZE) TO OKUDOS.
           MOVE 10 TO SATIR.
           OPEN INPUT OKU.
           DISPLAY SCR.
       DOSYA-OKU0.
           READ OKU NEXT AT END GO DOSYA-OKU-SON0.       
       TEKRAR-OKU.
           ADD 1 TO SATIR.
           PERFORM AKTAR.
           DISPLAY SCR-SATIR.
           IF SATIR = 28 GO ONAY-AL1.
           GO DOSYA-OKU0.
       DOSYA-OKU-SON0.
           CLOSE OKU. OPEN INPUT OKU.
       ONAY-AL1.
           DISPLAY 'ônizlemeyi onaylçyor musunuz ? E/H '
            LINE 4 POSITION 40 LOW.
           ACCEPT DUR LINE 4 POSITION 75 TAB PROMPT '_'.
           ACCEPT F FROM ESCAPE KEY.
           IF DUR = 'e' OR DUR = 'E' GO ONAY-AL1-SON.
           IF F = 27 OR DUR = 'h' OR DUR = 'H' 
                CLOSE OKU DELETE FILE OKU
                 EXIT PROGRAM STOP RUN.
           IF F = 68 DISPLAY SPACES ERASE CONTROL 'BCOLOR=WHITE'
                     MOVE 10 TO SATIR
                     DISPLAY SCR GO TEKRAR-OKU.
           GO ONAY-AL1.
       ONAY-AL1-SON.
           CLOSE OKU.
       KATNO-AL.
           MOVE ZEROS TO SEC-KATNO.
           CALL 'KATPEN' USING 1 SEC-KATNO.
           CANCEL 'KATPEN'.
           IF SEC-KATNO = ZEROS GO KATNO-AL.
           
           OPEN INPUT KATLOG.
           MOVE SEC-KATNO TO KT-KATNO.
           READ KATLOG INVALID KEY CLOSE KATLOG GO KATNO-AL.
           IF KT-HESAPNO = SPACES CLOSE KATLOG GO KATNO-AL.
           CLOSE KATLOG.
       KATNO-AL-SON.
       
       ONAY-AL2. 
           MOVE 'AKTARIM BAûLAYACAK ONAYLIYOR MUSUNUZ?'
            TO ONAY-MESSAGE.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'e' OR E-ONAY = 'E' GO ONAY-AL2-SON.
           IF F = 27 OR E-ONAY = 'h' OR E-ONAY = 'H' 
              DELETE FILE OKU
                    EXIT PROGRAM STOP RUN.
           GO ONAY-AL2.
       ONAY-AL2-SON.
           
           ACCEPT TSAAT FROM TIME.
           OPEN OUTPUT TEMPFILE. CLOSE TEMPFILE.
           OPEN I-O TEMPFILE.
           
           
           OPEN INPUT OKU I-O STOK STOKARA.
           MOVE ZEROS TO SAYAC.
       DOSYA-OKU.
           READ OKU NEXT AT END GO DOSYA-OKU-SON.
           MOVE SEC-KATNO TO ST-KATNO.
           MOVE D-STOKNO TO ST-STNO.
           READ STOK INVALID KEY PERFORM INSERT-STOK-OP GO DOSYA-OKU.
           PERFORM UPDATE-STOK-OP.
           ADD 1 TO SAYAC.
           MOVE SAYAC TO Z-SAYAC.
           DISPLAY SAYAC LINE 37 POSITION 1 ' kayçt aktarçldç .'.
           GO DOSYA-OKU.
       DOSYA-OKU-SON.
           CLOSE TEMPFILE OKU.
           
           OPEN I-O TEMPFILE.
           MOVE SEC-KATNO TO ST-KATNO.
           MOVE SPACES TO ST-STNO.
           START STOK KEY IS NOT LESS THAN ST-KEY
            INVALID KEY GO STOK-OKU-SON.
       STOK-OKU.
           READ STOK NEXT AT END GO STOK-OKU-SON.
           IF ST-KATNO NOT = SEC-KATNO GO STOK-OKU-SON.
           MOVE ST-STNO TO TP-STNO.
           READ TEMPFILE INVALID KEY PERFORM DELETE-STOK-OP.
           GO STOK-OKU.
       STOK-OKU-SON.
           CLOSE STOK STOKARA.
           
           MOVE OKUDOS TO YAZDOS.
           OPEN OUTPUT LOGFILE.
           
           MOVE SPACES TO TP-STNO.
           START TEMPFILE KEY IS NOT LESS THAN TP-KEY
                INVALID KEY GO LOG-OKU-SON.
       LOG-OKU.
           READ TEMPFILE NEXT AT END GO LOG-OKU-SON.
           IF TP-TIP = 0 GO LOG-OKU.
           WRITE LGS FROM TP-KAYIT.
           GO LOG-OKU.
       LOG-OKU-SON.
           CLOSE LOGFILE TEMPFILE.
           DELETE FILE TEMPFILE.
           GO STOP-PROGRAM.
       
           
       
       AKTAR.
           MOVE SEC-KATNO TO ST-KATNO.
           MOVE D-STOKNO TO ST-STNO.
           MOVE D-PARCANO TO ST-PRCNO.
           MOVE D-OEMNO TO ST-OEMNO.
           MOVE D-TIPI TO ST-TIPI.
           MOVE D-STOKADI TO ST-CINSI.
           MOVE D-NOTE TO ST-NOTE.
           MOVE D-MARKA TO ST-MARKA.
           INITIALIZE ST-DEG.
           IF D-TARIH NOT = SPACES
              MOVE 1 TO ST-GUN
              MOVE D-TARIH(1:2) TO ST-AY
              MOVE D-TARIH(3:2) TO ST-YIL
              ADD 2000 TO ST-YIL
           END-IF.
           MOVE Z-PAKET TO D-FIYAT.
           MOVE D-FIYAT TO ST-PAKET.
           
           MOVE ST-STNO TO TP-STNO.
           MOVE ST-PRCNO TO TP-PRCNO.
           MOVE ST-OEMNO TO TP-OEMNO.
           MOVE ST-TIPI TO TP-TIPI.
           MOVE ST-CINSI TO TP-CINSI.
           MOVE ST-MARKA TO TP-MARKA.
           
           MOVE Z-FIYAT TO D-FIYAT.
           MOVE D-FIYAT TO TP-FIYAT.
           
           MOVE ST-FIYAT TO TP-ESKIFIYAT.
           
           MOVE Z-FIYAT TO D-FIYAT.
           MOVE D-FIYAT TO ST-FIYAT.
           
           
       INSERT-STOK-OP.
           PERFORM AKTAR.
           MOVE ZEROS TO ST-MIN ST-MAX.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           STRING ST-KATNO ST-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING ST-KATNO ST-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING ST-KATNO ST-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING ST-KATNO ST-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           PERFORM WRITE-STOK.
           WRITE SA-KAYIT.
           MOVE 1 TO TP-TIP.
           WRITE TP-KAYIT.
       UPDATE-STOK-OP.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           STRING ST-KATNO ST-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING ST-KATNO ST-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING ST-KATNO ST-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING ST-KATNO ST-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           READ STOK.
           PERFORM AKTAR.
           PERFORM REWRITE-STOK.
           REWRITE SA-KAYIT.
           IF TP-ESKIFIYAT NOT = TP-FIYAT MOVE 2 TO TP-TIP
            ELSE MOVE 0 TO TP-TIP.
           WRITE TP-KAYIT.
       
       DELETE-STOK-OP.
           MOVE ST-STNO TO TP-STNO.
           MOVE ST-PRCNO TO TP-PRCNO.
           MOVE ST-OEMNO TO TP-OEMNO.
           MOVE ST-TIPI TO TP-TIPI.
           MOVE ST-CINSI TO TP-CINSI.
           MOVE ST-MARKA TO TP-MARKA.
           MOVE ST-FIYAT TO TP-FIYAT.
           MOVE 3 TO TP-TIP.
           WRITE TP-KAYIT.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           PERFORM DELETE-STOK.
		   DELETE STOKARA.
           
           
       STOP-PROGRAM.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==STOK==
                                  "%T%"  BY "STOK"
                                 ==%R%== BY ==ST-KAYIT==.
