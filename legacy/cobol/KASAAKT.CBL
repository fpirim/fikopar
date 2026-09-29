       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KASAAKT'.
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
       copy 'kasa.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'kasa.cpz'.
       FD OKU.
       01 OKU-KAYIT.
           02 OK-OP                 PIC 9.
           02 OK-KAYIT.
                03 OK-TARIH.
                     04 OK-YIL           PIC 9999.
                     04 OK-AY            PIC 99.
                     04 OK-GUN           PIC 99.
                03 OK-GELIR         PIC 9(13)V99.
                03 OK-GIDER         PIC 9(13)V99.    
               
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
           OPEN INPUT OKU I-O KASA.
       DOSYA-OKU.
           READ OKU NEXT AT END GO DOSYA-OKU-SON.
           IF OK-OP = 0 INITIALIZE KS-KAYIT
                        MOVE OK-TARIH TO KS-TARIH
                        MOVE OK-GELIR TO KS-GELIR
                        MOVE OK-GIDER TO KS-GIDER
						PERFORM WRITE-KASA
           END-IF.
           IF OK-OP = 1 MOVE OK-TARIH TO KS-TARIH
                        MOVE ZEROS TO KS-SIRA
                        READ KASA END-READ
                        MOVE OK-GELIR TO KS-GELIR
                        MOVE OK-GIDER TO KS-GIDER
                        PERFORM REWRITE-KASA
           END-IF.
           GO DOSYA-OKU.
       DOSYA-OKU-SON.
           CLOSE OKU KASA.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==KASA==
                                  "%T%"  BY "KASA"
                                 ==%R%== BY ==KS-KAYIT==.
  
