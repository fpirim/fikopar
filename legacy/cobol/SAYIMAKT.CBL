       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'SAYIMAKT'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
            SELECT OKU ASSIGN TO DISK, OKUDOS
                   ORGANIZATION LINE SEQUENTIAL.
       copy 'stok.cpy'.
       copy 'sayim.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'sayim.cpz'.
       FD OKU.
       01 OKU-KAYIT.
           02 OK-OP                     PIC 9.
           02 OK-KAYIT.
               03 OK-KEY.
                    04 OK-YIL                PIC 9999.
                    04 OK-KATNO              PIC 9999.
                    04 OK-STNO               PIC X(15).
                    04 OK-SIRA               PIC 9(10).
               03 OK-DEPONO             PIC 9999.    
               03 OK-PRCNO              PIC X(30).
               03 OK-OEMNO              PIC X(30).
               03 OK-TIPI               PIC X(30).
               03 OK-CINSI              PIC X(60).
               03 OK-MARKA              PIC X(30).
               03 OK-FIYAT              PIC 9(15)V9999.
               03 OK-ADET               PIC 9(10)V9999.
               03 OK-KDV                PIC 9(5)V9999.
               03 OK-RAF                PIC X(15).
       WORKING-STORAGE SECTION.
       77 FLAG                  PIC 9.
       77 OKUDOS                PIC X(100).
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
           MOVE MAIN-ARGUMENT-VALUE TO OKUDOS.
           OPEN INPUT OKU I-O SAYIM I-O STOKADET.
       DOSYA-OKU.
           READ OKU NEXT AT END GO DOSYA-OKU-SON.
           IF OK-OP = 0 PERFORM DELETE-SAYIM-OP.
           IF OK-OP = 1 PERFORM INSERT-SAYIM-OP.
           IF OK-OP = 2 PERFORM UPDATE-SAYIM-OP.
           GO DOSYA-OKU.
       DOSYA-OKU-SON.
           CLOSE OKU SAYIM STOKADET.
           EXIT PROGRAM.
           STOP RUN.
       
       INSERT-SAYIM-OP.
           MOVE OK-KAYIT TO SY-KAYIT.
           MOVE 1 TO FLAG. PERFORM STOK-ADET THRU STOK-ADET-SON.
           PERFORM WRITE-SAYIM.
        
       DELETE-SAYIM-OP.
           MOVE OK-KEY TO SY-KEY.
           READ SAYIM.
           MOVE 0 TO FLAG. PERFORM STOK-ADET THRU STOK-ADET-SON.
           PERFORM DELETE-SAYIM.
       
       UPDATE-SAYIM-OP.
           MOVE OK-KEY TO SY-KEY.
           READ SAYIM.
           MOVE 0 TO FLAG. PERFORM STOK-ADET THRU STOK-ADET-SON.
           MOVE OK-KAYIT TO SY-KAYIT.
           MOVE 1 TO FLAG. PERFORM STOK-ADET THRU STOK-ADET-SON.
           PERFORM REWRITE-SAYIM.
      
       STOK-ADET.
           MOVE SY-KATNO TO STA-KATNO.
           MOVE SY-STNO TO STA-STNO.
           MOVE SY-DEPONO TO STA-DEPONO.
           MOVE SY-YIL TO STA-YIL.
           READ STOKADET INVALID KEY 
            IF FLAG = 0 GO STOK-ADET-SON END-IF
            MOVE ZEROS TO STA-GIR STA-CIK
			PERFORM WRITE-STOKADET
            
           END-READ.
           IF FLAG = 0
             SUBTRACT SY-ADET FROM STA-GIR
           ELSE 
             ADD SY-ADET TO STA-GIR 
           END-IF.
           IF STA-GIR = ZEROS AND STA-CIK = ZEROS
            PERFORM DELETE-STOKADET
           ELSE
            PERFORM REWRITE-STOKADET
           END-IF.
       STOK-ADET-SON.     
       copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==SAYIM==
                                  "%T%"  BY "SAYIM"
                                 ==%R%== BY ==SY-KAYIT==.
       
