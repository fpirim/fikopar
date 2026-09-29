       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'READSQL'.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 HD-KAYIT.
           02 HD-KEY.
               03 HD-TIP                PIC 99.
               03 HD-EVRAKNO            PIC X(15).
               03 HD-SIRA               PIC 9(10).
           02 HD-KATKEY.
               03 HD-KATNO              PIC 9999.
               03 HD-STNO               PIC X(15).
           02 HD-PRCNO                  PIC X(30).
           02 HD-TIPI                   PIC X(30).
           02 HD-CINSI                  PIC X(60).
           02 HD-MARKA                  PIC X(30).
           02 HD-ISKONTO1               PIC 9(4)V9999.
           02 HD-ISKONTO2               PIC 9(4)V9999.
           02 HD-KDV                    PIC 9(4)V9999.
           02 HD-ADET                   PIC 9(12)V9999.
           02 HD-FIYAT                  PIC 9(10)V9999.
       
       77 RVAR  PIC 9.	   
       77 DUR   PIC X.
       PROCEDURE DIVISION.
       BASLA.
           INITIALIZE HD-KAYIT.
		   MOVE 0 TO RVAR.
       OKU.
           CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "HAREKET-DETAY" "READNEXT" HD-KAYIT RVAR.
		   CANCEL "executesql".
           DISPLAY HD-KAYIT.
		   IF RVAR = 1 GO SON.
           ACCEPT DUR NO BEEP.
           GO OKU.
       SON.
           EXIT PROGRAM.
           STOP RUN.
       