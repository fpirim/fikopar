       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KODPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'tipkod.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'tipkod.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 22.
           02 X                     PIC 9999 VALUE 37.
           02 E-TIP                 PIC X(40).
       01 BILGI.
           02 B-KOD                   PIC ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-TIP                   PIC X(40).
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       77 SEC             PIC 9.
	   77 G-KOD           PIC 9999.
       SCREEN SECTION.
       01 SCR.
           02 LINE 5 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(4) FROM 'K.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(40) FROM 'A€IKLAMA' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 7 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 23 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
             
       PROCEDURE DIVISION USING SEC G-KOD.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
           DISPLAY 'º' LINE CIZ POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                SPACES SIZE 4 'º' SPACES SIZE 40 'º'
           END-PERFORM.
           DISPLAY SCR.
           CALL 'GOLGE' USING 0005 X 0023 0083.
           CANCEL 'GOLGE'.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' TAB ' LINE 37 POSITION 1 ' SE€ ' REVERSE LOW
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT TIPKOD.
           MOVE G-KOD TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD
             INVALID KEY GO A-LISTE-SON.
           READ TIPKOD NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ TIPKOD NEXT AT END READ TIPKOD PREVIOUS GO A-LISTE-SON.
           IF TK-KOD = ZEROS READ TIPKOD PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION X
                   CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                   SPACES SIZE 4 'º' SPACES SIZE 40 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-KOD TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ TIPKOD NEXT. 
           IF TK-KOD = ZEROS PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-TIP
               ELSE MOVE TK-TIP TO E-TIP
           END-IF.
           ACCEPT E-TIP LINE SATIR POSITION 43 UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE TIPKOD MOVE ZEROS TO G-KOD GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 58 AND SEC = 1 AND YENI = 0
                MOVE TK-KOD TO G-KOD CLOSE TIPKOD 
                GO STOP-PROGRAM.
           IF F = 13 GO YENI-TIPKOD.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-TIPKOD.
           CLOSE TIPKOD. OPEN I-O TIPKOD.
           IF YENI = 1
               MOVE E-TIP TO TK-TIP
               MOVE 1 TO TK-KOD
               PERFORM YENI-YAZ
           ELSE
               MOVE E-TIP TO TK-TIP
               PERFORM REWRITE-TIPKOD
           END-IF.
           CLOSE TIPKOD. OPEN INPUT TIPKOD.
           READ TIPKOD.
           PERFORM AKTAR.
		   MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-TIPKOD.
		   IF WVAR = 0 ADD 1 TO TK-KOD GO YENI-YAZ.
           
       YENI-TIPKOD-SON. 
            
        
       DELETE-KOD.
           CLOSE TIPKOD. OPEN I-O TIPKOD.
           READ TIPKOD.
           PERFORM DELETE-TIPKOD.
           CLOSE TIPKOD.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF G-KOD = TK-KOD MOVE 1 TO G-KOD.
           GO START-PROGRAM.
           
       CTHO-TUSU.
		   IF YENI = 1 AND SATIR = ILK GO TUS.
		   IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI.
           MOVE 1 TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD.
           READ TIPKOD NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO TK-KOD.
           START TIPKOD KEY IS NOT GREATER THAN TK-KOD.
           READ TIPKOD NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ TIPKOD NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
           
       PGUP-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
		   IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI
			ELSE
		      PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ TIPKOD PREVIOUS
             END-PERFORM.
           READ TIPKOD PREVIOUS AT END
              READ TIPKOD NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           IF TK-KOD = ZEROS
              READ TIPKOD NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ TIPKOD PREVIOUS AT END 
                    READ TIPKOD NEXT 
                    GO PG-OKU-SON.
           IF TK-KOD = ZEROS 
                    READ TIPKOD NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ TIPKOD PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ TIPKOD NEXT
           END-PERFORM.
           READ TIPKOD NEXT AT END
              READ TIPKOD PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           IF TK-KOD = ZEROS 
              READ TIPKOD PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ TIPKOD NEXT AT END 
             READ TIPKOD PREVIOUS MOVE ILK TO SATIR.
           IF TK-KOD = ZEROS
             READ TIPKOD PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ TIPKOD NEXT
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
	       IF YENI = 1 AND SATIR = ILK GO TUS.
		   IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI
		   ELSE
              PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ TIPKOD PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ TIPKOD PREVIOUS AT END READ TIPKOD NEXT GO TUS.
           IF TK-KOD = ZEROS READ TIPKOD NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ TIPKOD PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ TIPKOD NEXT AT END READ TIPKOD PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ TIPKOD NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   PERFORM YENI-SERITLI
                   GO TUS
                END-IF
                PERFORM SERITSIZ
                ADD 1 TO SATIR
                PERFORM YENI-SERITLI
                GO TUS.
           IF TK-KOD = ZEROS READ TIPKOD PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ TIPKOD NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   PERFORM YENI-SERITLI
                   GO TUS
                END-IF
                PERFORM SERITSIZ
                ADD 1 TO SATIR
                PERFORM YENI-SERITLI
                GO TUS.
           
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ TIPKOD NEXT
               END-PERFORM
                 MOVE SON TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = SON PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = ILK GO Y-LISTE-SON.
           READ TIPKOD PREVIOUS AT END 
                READ TIPKOD NEXT AT END CONTINUE END-READ
                GO Y-LISTE-SON.
           IF TK-KOD = ZEROS 
                READ TIPKOD NEXT AT END CONTINUE END-READ
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE TK-KOD TO B-KOD.
           MOVE TK-TIP TO B-TIP.
       SERITLI.
           ADD 1 TO X.
           DISPLAY BILGI LINE SATIR POSITION X
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           SUBTRACT 1 FROM X.
       SERITSIZ.
           ADD 1 TO X.
           DISPLAY BILGI LINE SATIR POSITION X
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           SUBTRACT 1 FROM X.
       YENI-SERITLI.
           MOVE B-KOD TO TK-KOD.
           ADD 1 TO TK-KOD.
           MOVE TK-KOD TO B-KOD.
           MOVE B-TIP TO TK-TIP.
           MOVE SPACES TO B-TIP.
           PERFORM SERITLI.
           SUBTRACT 1 FROM TK-KOD.
           MOVE TK-KOD TO B-KOD.
           MOVE TK-TIP TO B-TIP.
       YENI-SERITSIZ.
           MOVE B-KOD TO TK-KOD.
           MOVE B-TIP TO TK-TIP.
           MOVE SPACES TO B-TIP.
		   MOVE ZEROS TO B-KOD
           PERFORM SERITSIZ.
           MOVE TK-KOD TO B-KOD.
           MOVE TK-TIP TO B-TIP.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
           copy fileop.cpy REPLACING ==%T%== BY ==TIPKOD==
                                  "%T%"  BY "TIPKOD"
                                 ==%R%== BY ==TK-KAYIT==.
		  