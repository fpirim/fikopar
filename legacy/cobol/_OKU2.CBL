       IDENTIFICATION DIVISION.
       PROGRAM-ID. '_OKU2'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 DUR           PIC X.
       77 F             PIC 9999.
       77 DELIM         PIC X(20).
       77 C             PIC X VALUE '1'.
       77 X-VAR         PIC X(120).
       77 OPTIME        PIC 9(16).
       copy 'trim.var'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       BASLA.
           
           CALL 'SYSTEM' USING 'php aktar.php' GIVING X-VAR.
           CANCEL 'SYSTEM'.
           DISPLAY X-VAR LINE 25 POSITION 1.
           ACCEPT DUR NO BEEP LINE 4 POSITION 2.
           
           EXIT PROGRAM.
           STOP RUN.
       copy 'trim.per'.
