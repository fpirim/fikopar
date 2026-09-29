       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'OKU'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 DUR               PIC X.
       01 SQLFIND.
           02 SQLQUERY     PIC X(255).
           02 SQLRESULT    PIC X(255).
       
       PROCEDURE DIVISION.
       BASLA.
           STRING "select * from " DELIMITED BY SIZE
                  "windows.win_alimdetay" DELIMITED BY SIZE
                  "where stno = '"  DELIMITED BY SIZE
                  "N004810" DELIMITED BY SPACE
                  "'" DELIMITED BY SIZE
             INTO SQLQUERY
           END-STRING.

           CALL "executesql" USING "***remotedns****" "USER" "PASS"
             "COBOL" "%T%" "QUERY" SQLQUERY SQLRESULT.
           CANCEL "executesql".
       
           DISPLAY SQLQUERY.
           
           DISPLAY SQLRESULT.
           
           
           ACCEPT DUR NO BEEP.
           
           EXIT PROGRAM.
           STOP RUN.
