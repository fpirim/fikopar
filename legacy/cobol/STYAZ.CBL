       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'STYAZ'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
	   copy 'stok.cpy'.
       copy 'hareket.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'hareket.cpz'.
	   WORKING-STORAGE SECTION.
       77 OK            PIC 9.
       77 PASIF         PIC 9.
       
       copy 'status.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       01 ONAY-MESSAGE      PIC X(43).
       01 G-OP                  PIC 9.
      *  1 insert
      *  2 update
      *  3 delete
       01 G-KAYIT.
           02 G-KEY.
               03 G-KATNO              PIC 9999.
               03 G-STNO               PIC X(15).
           02 G-PRCNO                  PIC X(30).
           02 G-OEMNO                  PIC X(30).
           02 G-TIPI                   PIC X(30).
           02 G-CINSI                  PIC X(60).
           02 G-MARKA                  PIC X(30).
           02 G-MIN                    PIC 9(12)V9999.
           02 G-MAX                    PIC 9(12)V9999.
           02 G-FIYAT                  PIC 9(10)V9999.
           02 G-NOTE                   PIC X(10).
           02 G-DEG.
                 03 G-YIL          PIC 9999.
                 03 G-AY           PIC 99.
                 03 G-GUN          PIC 99.
           02 G-PAKET                  PIC 9(5).
       
       PROCEDURE DIVISION USING ONAY-MESSAGE G-OP G-KAYIT.
       START-PROGRAM.
      *     OPEN OUTPUT STOK STOKARA. CLOSE STOK STOKARA.
      *     GO STOP-PROGRAM.
           OPEN I-O STOK STOKARA PASSTOK STOKADET.
           OPEN INPUT HAREKET-DETAY.
           IF G-OP = 1 PERFORM INSERT-STOK.
           IF G-OP = 2 PERFORM UPDATE-STOK.
           IF G-OP = 3 PERFORM DELETE-STOK-PRC.
           CLOSE STOK STOKARA HAREKET-DETAY PASSTOK STOKADET.
           GO STOP-PROGRAM.
       INSERT-STOK.
           MOVE G-KAYIT TO ST-KAYIT.
           STRING G-KATNO G-STNO DELIMITED BY SIZE INTO SA-STNO.
           STRING G-KATNO G-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING G-KATNO G-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING G-KATNO G-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING G-KATNO G-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           PERFORM PASIF-KONTROL.
           IF OK = 1
            PERFORM WRITE-STOK
            WRITE SA-KAYIT
           ELSE
            MOVE ' koda ait aktif veya pasif kod var aktarçlmadç.' 
             TO ONAY-MESSAGE
           END-IF.
           
       PASIF-KONTROL.
           MOVE G-STNO TO PAS-STNO.
           MOVE G-KATNO TO PAS-KATNO.
           START PASSTOK KEY IS EQUAL TO PAS-KEY
            INVALID KEY MOVE 1 TO OK
            NOT INVALID KEY MOVE 0 TO OK
           END-START.
       UPDATE-STOK.
           STRING G-KATNO G-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           STRING G-KATNO G-PRCNO DELIMITED BY SIZE INTO SA-PRCNO.
           STRING G-KATNO G-OEMNO DELIMITED BY SIZE INTO SA-OEMNO.
           STRING G-KATNO G-TIPI DELIMITED BY SIZE INTO SA-TIPI.
           STRING G-KATNO G-CINSI DELIMITED BY SIZE INTO SA-CINSI.
           MOVE G-KAYIT TO ST-KAYIT.
           REWRITE SA-KAYIT.
           PERFORM REWRITE-STOK.
           
       DELETE-STOK-PRC.
           MOVE G-KATNO TO HD-KATNO ST-KATNO.
           MOVE G-STNO TO HD-STNO ST-STNO.
           START HAREKET-DETAY KEY EQUAL TO HD-KATKEY
                INVALID KEY MOVE 0 TO PASIF
                NOT INVALID KEY MOVE 1 TO PASIF
           END-START.
           STRING G-KATNO G-STNO DELIMITED BY SIZE INTO SA-STNO.
           START STOKARA KEY IS EQUAL TO SA-STNO.
           READ STOKARA.
           READ STOK.
           IF PASIF = 1 PERFORM PASIF-TASI.
           DELETE STOKARA.
           PERFORM DELETE-STOK.
           IF PASIF = 0 PERFORM DELETE-ADET THRU DELETE-ADET-SON.
       PASIF-TASI.
           MOVE ST-KAYIT TO PAS-KAYIT.
           ACCEPT PAS-TARIH FROM CENTURY-DATE.
           ACCEPT PAS-ZAMAN FROM TIME.
           PERFORM WRITE-PASSTOK.
           MOVE ' ait stok silinmiü hareket var pasif stoklara taüçndç.'
            TO ONAY-MESSAGE.
           
       DELETE-ADET.
           MOVE ST-STNO TO STA-STNO.
           MOVE ST-KATNO TO STA-KATNO.
           MOVE ZEROS TO STA-YIL STA-DEPONO.
           START STOKADET KEY IS EQUAL TO STA-KEY
            INVALID KEY GO DELETE-ADET-SON.
       DELETE-ADET-OKU.
           READ STOKADET NEXT AT END GO DELETE-ADET-SON.
           IF STA-KATNO NOT = G-KATNO OR STA-STNO NOT = G-STNO
                    GO DELETE-ADET-SON.
           PERFORM DELETE-STOKADET.
           GO DELETE-ADET-OKU.
       DELETE-ADET-SON.

       STOP-PROGRAM.
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
       