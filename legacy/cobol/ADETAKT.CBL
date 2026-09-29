       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'ADETAKT'.
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
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       FD OKU.
       01 OKU-KAYIT.
           02 OK-OP                     PIC 9.
           02 OK-KAYIT.
                03 OK-KEY.
                    04 OK-KATNO         PIC 9999.
                    04 OK-STNO          PIC X(15).
                    04 OK-YIL           PIC 9999.
                    04 OK-DEPONO        PIC 9999.
                03 OK-GIR               PIC 9(12)V9999.
                03 OK-CIK               PIC 9(12)V9999.    
               
       WORKING-STORAGE SECTION.
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
           OPEN INPUT OKU I-O STOKADET.
       DOSYA-OKU.
           READ OKU NEXT AT END GO DOSYA-OKU-SON.
           IF OK-OP = 0 PERFORM DELETE-ADET.
           IF OK-OP = 1 PERFORM INSERT-ADET.
           IF OK-OP = 2 PERFORM UPDATE-ADET.
           GO DOSYA-OKU.
       DOSYA-OKU-SON.
           CLOSE OKU STOKADET.
           EXIT PROGRAM.
           STOP RUN.
       
       INSERT-ADET.
           MOVE OK-KAYIT TO STA-KAYIT.
           PERFORM WRITE-STOKADET.
        
       DELETE-ADET.
           MOVE OK-KAYIT TO STA-KAYIT.
           READ STOKADET.
           PERFORM DELETE-STOKADET.
           
       UPDATE-ADET.
           MOVE OK-KAYIT TO STA-KAYIT.
           READ STOKADET.
           MOVE OK-KAYIT TO STA-KAYIT.
           PERFORM REWRITE-STOKADET.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
         