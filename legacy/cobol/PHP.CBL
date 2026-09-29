       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'OKU'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'stok.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       WORKING-STORAGE SECTION.
       77 DUR               PIC X.
       77 SATIR             PIC 9999 VALUE 1.
       77 I                 PIC 9999999.
       77 J                 PIC 9(10).
       77 F                 PIC 99.
       77 PRM               PIC X(10) VALUE 'OSMAN'.
       77 BPTR              USAGE POINTER.
       77 MYVAL             PIC X(2).
       77 SAAT              PIC 99999999.

       
       01 FIELD.
           02 FIELD-HOLDER  PIC X(50) OCCURS 40.
       COPY 'STATUS.CPY'.
       copy 'win.cpy'.
       PROCEDURE DIVISION.
       BASLA.
           OPEN INPUT STOKADET.
           MOVE 1 TO STA-KATNO.
           MOVE '002220' TO STA-STNO.
           MOVE ZEROS TO STA-DEPONO STA-YIL.
           START STOKADET KEY IS NOT LESS THAN STA-KEY
            INVALID KEY GO SON.
           MOVE 1 TO J.
       OKU.
           READ STOKADET NEXT AT END GO SON.
           IF STA-STNO NOT = '002220' GO OKU.
           DISPLAY STA-KAYIT LINE I POSITION 1.
           ADD 1 TO J.
           GO OKU.
       SON.
           CLOSE STOKADET.
           
           ACCEPT DUR NO BEEP.
           
           
           EXIT PROGRAM.
           STOP RUN.
