       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'LISAKT2'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
            SELECT OKU ASSIGN TO DISK, OKUDOS
                   ORGANIZATION LINE SEQUENTIAL.
       copy 'stok.cpy'.
       copy 'hareket.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'hareket.cpz'.
       FD OKU.
       01 OKU-CONTENT.
           02 OK-OP             PIC 9.
           02 OK-KATNO              PIC 9999.
           02 OK-STNO               PIC X(15).
           02 OK-PRCNO                   PIC X(30).
		   02 OK-OEMNO               PIC X(30).
           02 OK-TIPI                    PIC X(30).
           02 OK-CINSI                   PIC X(60).
           02 OK-NOTE                    PIC X(10).
           02 OK-MARKA                   PIC X(30).
           02 OK-FIYAT                   PIC 9(10)V9999.
		   02 OK-MINADET                 PIC 9(10)V9999.
           
       
       WORKING-STORAGE SECTION.
       77 OKUDOS                PIC X(100).
       77 VAR                   PIC 9.
       77 DUR               PIC X.
       01 SW-HIDE PIC 9(4) BINARY VALUE 0.
       copy 'status.cpy'.
       copy 'fileop.var'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
      *     CALL 'C$SHOW' USING SW-HIDE.
      *     CANCEL 'C$SHOW'.
      
           MOVE MAIN-ARGUMENT-VALUE TO OKUDOS.
           OPEN INPUT OKU I-O STOK STOKARA PASSTOK 
             STOKADET STOKBAG INPUT HAREKET-DETAY.
       DOSYA-OKU.
           READ OKU NEXT AT END GO STOP-PROGRAM.

      *     DISPLAY OK-OP LINE 1 POSITION 1
      *            OK-HDKOD LINE 2 POSITION 1
      *             OK-KATNO LINE 3 POSITION 1
      *             OK-STNO LINE 4 POSITION 1
      *             OK-PRCNO LINE 5 POSITION 1
      *             OK-OEMNO LINE 6 POSITION 1
      *             OK-TIPI LINE 7 POSITION 1
      *             OK-CINSI LINE 8 POSITION 1
      *             OK-MARKA LINE 9 POSITION 1
      *             OK-MIN LINE 10 POSITION 1
      *             OK-MAX LINE 11 POSITION 1
      *             OK-FIYAT LINE 12 POSITION 1
      *             OK-NOTE LINE 13 POSITION 1
      *             OK-DEG LINE 14 POSITION 1
      *             OK-PAKET LINE 15 POSITION 1.
      *     ACCEPT DUR NO BEEP.
           IF OK-STNO = SPACES GO DOSYA-OKU.
           IF OK-OP = 0 PERFORM DELETE-STOK-OP.
           IF OK-OP = 1 PERFORM INSERT-STOK-OP.
           IF OK-OP = 2 PERFORM UPDATE-STOK-OP.
           
           GO DOSYA-OKU.
       
       INSERT-STOK-OP.
           INITIALIZE ST-KAYIT.
           MOVE OK-KATNO TO ST-KATNO.
           MOVE OK-STNO  TO ST-STNO.
		   MOVE OK-OEMNO  TO ST-OEMNO.
           MOVE OK-PRCNO TO ST-PRCNO.
           MOVE OK-TIPI  TO ST-TIPI.
           MOVE OK-CINSI TO ST-CINSI.
           MOVE OK-NOTE  TO ST-NOTE.
           MOVE OK-MARKA TO ST-MARKA.
           MOVE OK-FIYAT TO ST-FIYAT.
		   MOVE OK-MINADET TO ST-MIN.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           STRING ST-KATNO ST-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING ST-KATNO ST-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING ST-KATNO ST-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING ST-KATNO ST-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           PERFORM WRITE-STOK.
           WRITE SA-KAYIT INVALID KEY 
              REWRITE SA-KAYIT END-REWRITE END-WRITE.
                
       BAG-SIL.
           MOVE ST-KATNO TO SB-KATNO.
           MOVE ST-STNO TO SB-STNO.
           MOVE ZEROS TO SB-SIRA.
           START STOKBAG KEY IS NOT LESS THAN SB-KEY
                INVALID KEY GO BAG-SIL-SON.
       BAG-SIL-OKU.
           READ STOKBAG NEXT AT END GO BAG-SIL-SON.
           IF ST-STNO NOT = SB-STNO OR ST-KATNO NOT = SB-KATNO 
                    GO BAG-SIL-SON.
           PERFORM DELETE-STOKBAG.
           GO BAG-SIL-OKU.
       BAG-SIL-SON.
       
       UPDATE-STOK-OP.
           STRING OK-KATNO OK-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           MOVE OK-KATNO TO ST-KATNO.
           MOVE OK-STNO TO ST-STNO.
           STRING OK-KATNO OK-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING OK-KATNO OK-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING OK-KATNO OK-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING OK-KATNO OK-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           READ STOK.
           MOVE OK-KATNO TO ST-KATNO.
           MOVE OK-STNO  TO ST-STNO.
		   MOVE OK-OEMNO TO ST-OEMNO.
           MOVE OK-PRCNO TO ST-PRCNO.
           MOVE OK-TIPI  TO ST-TIPI.
           MOVE OK-CINSI TO ST-CINSI.
           MOVE OK-NOTE  TO ST-NOTE.
           MOVE OK-MARKA TO ST-MARKA.
           MOVE OK-FIYAT TO ST-FIYAT.
		   MOVE OK-MINADET TO ST-MIN.
           PERFORM REWRITE-STOK.
           REWRITE SA-KAYIT.
           
       DELETE-STOK-OP.
           MOVE OK-KATNO TO ST-KATNO.
           MOVE OK-STNO TO ST-STNO.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           READ STOK.
           PERFORM HAREKET-KONTROL.
           IF VAR = 1 PERFORM PASIF-TASI.
           PERFORM DELETE-STOK.
           DELETE STOKARA.
           IF VAR = 0 PERFORM DELETE-ADET THRU DELETE-ADET-SON
                      PERFORM BAG-SIL THRU BAG-SIL-SON.
       
       HAREKET-KONTROL.
           MOVE 0 TO VAR.
           MOVE OK-KATNO TO HD-KATNO.
           MOVE OK-STNO TO HD-STNO.
           START HAREKET-DETAY KEY IS EQUAL TO HD-KATKEY
            INVALID KEY MOVE 0 TO VAR
            NOT INVALID KEY MOVE 1 TO VAR
           END-START.
           
       DELETE-ADET.
           MOVE ST-STNO TO STA-STNO.
           MOVE ST-KATNO TO STA-KATNO.
           MOVE ZEROS TO STA-YIL STA-DEPONO.
           START STOKADET KEY IS NOT LESS THAN STA-KEY
            INVALID KEY GO DELETE-ADET-SON.
       DELETE-ADET-OKU.
           READ STOKADET NEXT AT END GO DELETE-ADET-SON.
           IF STA-KATNO NOT = ST-KATNO OR STA-STNO NOT = ST-STNO
                    GO DELETE-ADET-SON.
           PERFORM DELETE-STOKADET.
           GO DELETE-ADET-OKU.
       DELETE-ADET-SON.
            
       PASIF-TASI.
           MOVE ST-KAYIT TO PAS-KAYIT.
           ACCEPT PAS-TARIH FROM CENTURY-DATE.
           ACCEPT PAS-ZAMAN FROM TIME.
           PERFORM WRITE-PASSTOK.
           
       STOP-PROGRAM.
           CLOSE OKU STOK STOKARA STOKADET PASSTOK 
                HAREKET-DETAY STOKBAG.
      *     DELETE FILE OKU.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==STOK==
                                  "%T%"  BY "STOK"
                                 ==%R%== BY ==ST-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==PASSTOK==
                                  "%T%"  BY "PASSTOK"
                                 ==%R%== BY ==PAS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKBAG==
                                  "%T%"  BY "STOKBAG"
                                 ==%R%== BY ==SB-KAYIT==.
       