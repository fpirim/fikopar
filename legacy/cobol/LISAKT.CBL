       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'LISAKT'.
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
           02 OK-KAYIT.
               03 OK-KEY.
                    04 OK-KATNO              PIC 9999.
                    04 OK-STNO               PIC X(15).
               03 OK-PRCNO                  PIC X(30).
               03 OK-OEMNO                  PIC X(30).
               03 OK-TIPI                   PIC X(30).
               03 OK-CINSI                  PIC X(60).
               03 OK-MARKA                  PIC X(30).
               03 OK-MIN                    PIC 9(12)V9999.
               03 OK-MAX                    PIC 9(12)V9999.
               03 OK-FIYAT                  PIC 9(10)V9999.
               03 OK-NOTE                   PIC X(10).
               03 OK-DEG.
                     04 OK-YIL          PIC 9999.
                     04 OK-AY           PIC 99.
                     04 OK-GUN          PIC 99.
               03 OK-PAKET                  PIC 9(5).
               03 OK-SIRA                   PIC 9(15).
           02 OK-BKATNO             PIC 9999.
           02 OK-BSTNO              PIC X(15).
           02 OK-BISKONTO           PIC 9(4)V9999.
           02 OK-BKDV               PIC 9(4)V9999.
           

       
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
           CALL 'C$SHOW' USING SW-HIDE.
           CANCEL 'C$SHOW'.
      
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
           MOVE OK-KAYIT TO ST-KAYIT.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           STRING ST-KATNO ST-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING ST-KATNO ST-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING ST-KATNO ST-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING ST-KATNO ST-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           PERFORM WRITE-STOK.
           WRITE SA-KAYIT.
		   PERFORM BAG-STOK THRU BAG-STOK-SON.
           
       BAG-STOK.
           IF OK-BKATNO = ZEROS OR OK-BSTNO = SPACES 
                GO BAG-STOK-SON.
           MOVE OK-BKATNO TO SB-KATNO.
           MOVE OK-BSTNO TO SB-STNO.
           MOVE ZEROS TO SB-SIRA.
           START STOKBAG KEY IS NOT LESS THAN SB-KEY
                INVALID KEY GO BAG-YAZ.
       BAG-ARA. 
           READ STOKBAG NEXT AT END GO BAG-YAZ.
           IF SB-KATNO NOT = OK-BKATNO OR SB-STNO NOT = OK-BSTNO
                GO BAG-YAZ.
           IF SB-BAG-KATNO = OK-KATNO AND SB-BAG-STNO = OK-STNO
                MOVE OK-BISKONTO TO SB-ISK
                MOVE OK-BKDV TO SB-KDV
				PERFORM REWRITE-STOKBAG
                GO BAG-STOK-SON.
           GO BAG-ARA.
       BAG-YAZ.
           MOVE OK-BKATNO TO SB-KATNO ST-KATNO.
           MOVE OK-BSTNO TO SB-STNO ST-STNO.
           READ STOK INVALID KEY GO BAG-STOK-SON.
           MOVE OK-KATNO TO SB-BAG-KATNO.
           MOVE OK-STNO TO SB-BAG-STNO.
           MOVE OK-BISKONTO TO SB-ISK.
           MOVE OK-BKDV TO SB-KDV.
           MOVE 1 TO SB-SIRA.
       BAG-YAZ-SON.
           PERFORM WRITE-STOKBAG.
		   IF WVAR = 0 ADD 1 TO SB-SIRA GO BAG-YAZ-SON.
       BAG-STOK-SON.         
                
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
           MOVE OK-KAYIT TO ST-KAYIT.
           STRING ST-KATNO ST-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           MOVE OK-KAYIT TO ST-KAYIT.
           STRING ST-KATNO ST-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING ST-KATNO ST-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING ST-KATNO ST-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING ST-KATNO ST-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           READ STOK.
           MOVE OK-KAYIT TO ST-KAYIT.
           PERFORM REWRITE-STOK.
		   REWRITE SA-KAYIT.
           PERFORM BAG-STOK THRU BAG-STOK-SON.
           
       DELETE-STOK-OP.
           MOVE OK-KAYIT TO ST-KAYIT.
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
       