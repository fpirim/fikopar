       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KAMPANYA'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'katlog.cpy'.
       copy 'stok.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'katlog.cpz'.
       copy 'stok.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 9999.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 I                     PIC 9(5).
           02 J                     PIC 9(5).
           02 T                     PIC 9(5).
           02 WIDTH                 PIC 9999.
           02 YSON                  PIC 9999.
           02 LEN-OF-ARRAY          PIC 9(5).
           
       01 KAT-ARRAY.
           02 I-KATNO               PIC 9999 OCCURS 1000 TIMES.
           02 I-KATADI              PIC X(20) OCCURS 1000 TIMES.
           02 I-FIYAT               PIC 9(10)V9999 OCCURS 1000 TIMES. 
       
       
       01 BILGI.
           02 B-KATNO               PIC 9999.
           02 FILLER                PIC X VALUE 'º'.
           02 B-KATADI              PIC X(30).
           02 FILLER                PIC X VALUE 'º'.
           02 B-FIYAT               PIC ZZZ.ZZZ.ZZZ,ZZZZ.
       
       copy 'status.cpy'.
       copy 'win.cpy'. 
       LINKAGE SECTION.
       77 G-KATNO             PIC 9999.
       77 G-STNO              PIC X(15).
       77 G-FIYAT             PIC 9(10)V9999.
       77 Y1                  PIC 9999.
       77 X1                  PIC 9999.
       PROCEDURE DIVISION USING G-KATNO G-STNO G-FIYAT Y1 X1.
       BEGIN-PROGRAM.
           ADD 1 TO Y1.
           MOVE 0 TO I.
           OPEN INPUT KATLOG STOK.
           MOVE ZEROS TO KT-KATNO.
           START KATLOG KEY IS NOT LESS THAN KT-KATNO
                INVALID KEY CLOSE KATLOG STOK EXIT PROGRAM STOP RUN.
       KATLOG-OKU.
           READ KATLOG NEXT AT END GO KATLOG-SON.
           MOVE G-STNO TO ST-STNO.
           MOVE KT-KATNO TO ST-KATNO.
           READ STOK INVALID KEY GO KATLOG-OKU.
           IF KT-KATNO NOT = G-KATNO AND KT-HESAPNO NOT = 'KAMPANYA'
                    GO KATLOG-OKU.
           ADD 1 TO I.
           MOVE KT-KATNO TO I-KATNO(I).
           MOVE KT-ACIKLAMA TO I-KATADI(I).
           MOVE ST-FIYAT TO I-FIYAT(I).
           GO KATLOG-OKU.
       KATLOG-SON.
           CLOSE KATLOG STOK.
           IF I = 1 MOVE I-FIYAT(1) TO G-FIYAT EXIT PROGRAM STOP RUN.
           MOVE I TO LEN-OF-ARRAY.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           COMPUTE YSON = Y1 + 5.
           DISPLAY 'É' LINE Y1 POSITION X1
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 4 'Ë' ALL 'Í' SIZE 30 'Ë'
                ALL 'Í' SIZE 16 '»'.
           COMPUTE CIZ = Y1 + 1.
           PERFORM VARYING CIZ FROM CIZ BY 1 UNTIL CIZ > YSON
                DISPLAY 'º' LINE CIZ POSITION X1
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 4 'º' ALL ' ' SIZE 30 'º' 
                   ALL ' ' SIZE 16 'º'
           END-PERFORM.
           DISPLAY 'È' LINE YSON POSITION X1
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 4 'Ê' ALL 'Í' SIZE 30 'Ê' 
                ALL 'Í' SIZE 16 '¼'.
           
           COMPUTE WIDTH = X1 + LENGTH OF BILGI + 1.
           CALL 'GOLGE' USING BY CONTENT Y1 X1 YSON WIDTH.
           CANCEL 'GOLGE'.
           
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
           
           ADD 1 TO Y1.
           SUBTRACT 1 FROM YSON.
           MOVE 1 TO I T.
           MOVE Y1 TO SATIR J.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = J PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF I = LEN-OF-ARRAY OR SATIR = YSON GO A-LISTE-SON.
           ADD 1 TO I SATIR.
           GO A-LISTE.
       A-LISTE-SON.
          
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 GO STOP-PROGRAM.
           IF F = 52 AND T NOT = 1 
                  IF J > Y1 SUBTRACT 1 FROM J END-IF 
                  MOVE Y1 TO SATIR
                  SUBTRACT 1 FROM T
                  COMPUTE I = T - (J - Y1)
                  GO A-LISTE.
           IF F = 53 AND T NOT = LEN-OF-ARRAY
                  IF J < YSON ADD 1 TO J END-IF 
                  MOVE Y1 TO SATIR
                  ADD 1 TO T
                  COMPUTE I = T - (J - Y1)
                  GO A-LISTE.
           IF F = 13 MOVE I-FIYAT(T) TO G-FIYAT GO STOP-PROGRAM.
           GO TUS.
       AKTAR.
           MOVE I-KATNO(I) TO B-KATNO.
           MOVE I-KATADI(I) TO B-KATADI.
           MOVE I-FIYAT(I) TO B-FIYAT.
       SERITLI.
           ADD 1 TO X1.
           DISPLAY BILGI LINE SATIR POSITION X1
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           SUBTRACT 1 FROM X1.
       SERITSIZ.
           ADD 1 TO X1.
           DISPLAY BILGI LINE SATIR POSITION X1
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           SUBTRACT 1 FROM X1.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
