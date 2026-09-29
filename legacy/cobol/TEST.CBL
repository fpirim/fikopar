       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'TEST-CBL'.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 XL-CODE               PIC X(255).
       01 XL-BINDING.
           02 XL-STNO               PIC X(15).
           02 XL-PRCNO              PIC X(30).
           02 XL-TIPI               PIC X(30).
           02 XL-CINSI              PIC X(60).
           02 XL-NOTE               PIC X(10).
           02 XL-MARKA              PIC X(30).
           02 XL-FIYAT              PIC 9(10)V9999.
           
       01 XL-RECORD.
           02 hEnv              PIC S9(9) BINARY VALUE 0.
           02 hDbc              PIC S9(9) BINARY VALUE 0.
           02 hStmt             PIC S9(9) BINARY VALUE 0.
           02 pFirstBinding     PIC S9(9) BINARY VALUE 0.
           02 nFlag             PIC 9 BINARY VALUE 0.
           02 filePath          PIC X(100).
           02 fileName          PIC X(100).
           02 wzsInput          PIC X(255).
           02 errMessage        PIC X(100).
           
       77 DUR   PIC X(10).
       77 SAYAC  PIC 9(10) VALUE 0.
       77 F     PIC 99.
       PROCEDURE DIVISION.
       BASLA.
           INITIALIZE XL-RECORD.
           MOVE '\\192.168.1.2\Fiyat Listeleri' TO filePath.
           MOVE 'L˜STE.xls' TO fileName.
           MOVE 'SELECT F2,F3,F5,F6,F7,F8,F9 from 
      -         '[M & U$]' TO wzsInput.
           DISPLAY '"' wzsInput '"'.
		   DISPLAY 'xlOpen calling ...'.
           CALL 'xlOpen' USING 
                hEnv hDbc hStmt filePath fileName errMessage.
           IF errMessage NOT = SPACES   
                DISPLAY 'xlOpen,Error :[' 
                   LINE 1 POSITION 1 errMessage ']'
                GO SON
           END-IF.
           
           DISPLAY 'xlExecute calling ...'.
           CALL 'xlExecute' USING hStmt pFirstBinding wzsInput.
           IF errMessage NOT = SPACES
                DISPLAY errMessage
                GO SON
           END-IF.
           DISPLAY 'xlNext calling ...'.
           
       OKU.
           ADD 1 TO SAYAC.
           CALL 'xlNext' USING hStmt pFirstBinding nFlag errMessage 
                                XL-STNO XL-PRCNO XL-TIPI 
                                XL-CINSI XL-MARKA XL-NOTE XL-FIYAT.
           
           IF errMessage NOT = SPACES
                DISPLAY 'xlNext,Error :[' 
                   LINE 1 POSITION 1 errMessage ']'
                GO SON
           END-IF.
           DISPLAY nFlag LINE 8 POSITION 1 ',' SAYAC.
           IF SAYAC < 12800  GO OKU.
           ACCEPT DUR.
           DISPLAY 'STNO  :[' LINE 10 POSITION 1 XL-STNO ']'.
           DISPLAY 'PRCNO :[' LINE 11 POSITION 1 XL-PRCNO ']'.
           DISPLAY 'TIPI  :[' LINE 12 POSITION 1 XL-TIPI ']'.
           DISPLAY 'CINSI :[' LINE 13 POSITION 1 XL-CINSI ']'.
           DISPLAY 'MARKA :[' LINE 14 POSITION 1 XL-MARKA ']'.
           DISPLAY 'NOTE  :[' LINE 15 POSITION 1 XL-NOTE ']'.
           DISPLAY 'FIYAT :[' LINE 16 POSITION 1 XL-FIYAT ']'.
           IF nFlag = 1 GO SON.
           GO OKU.
       SON.
           DISPLAY 'xlClose calling ...'.
           CALL 'xlClose' USING hEnv hDbc hStmt pFirstBinding errMessage.
           IF errMessage NOT = SPACES 
                DISPLAY 'xlClose,Error :[' 
                   LINE 1 POSITION 1 errMessage ']'.
           ACCEPT DUR.
           EXIT PROGRAM.
           STOP RUN.
           