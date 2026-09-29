       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'SEKIL'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'odeme.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'odeme.cpz'.
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
           02 E-ADI            PIC X(40).
       01 BILGI.
           02 B-KOD                   PIC ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ADI              PIC X(40).
       copy 'status.cpy'.   
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
	   77 SEC		      PIC 9.
       77 G-KOD           PIC 9999.
       SCREEN SECTION.
       01 SCR.
           02 LINE 5 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(4) FROM 'K.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(40) FROM 'PLAS˜YER' HIGHLIGHT.
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
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT ODEME.
		   IF G-KOD NOT = ZEROS MOVE G-KOD TO OD-NO
		    ELSE MOVE 1 TO OD-NO.
           START ODEME KEY IS NOT LESS THAN OD-NO
             INVALID KEY GO A-LISTE-SON.
           READ ODEME NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ ODEME NEXT AT END 
					READ ODEME PREVIOUS GO A-LISTE-SON.
		   IF OD-NO = ZEROS
		            READ ODEME PREVIOUS GO A-LISTE-SON.
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
           IF G-KOD NOT = ZEROS MOVE G-KOD TO OD-NO
		    ELSE MOVE 1 TO OD-NO.
           START ODEME KEY IS NOT LESS THAN OD-NO
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ ODEME NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-ADI
               ELSE MOVE OD-ADI TO E-ADI
           END-IF.
           ACCEPT E-ADI LINE SATIR POSITION 43 UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE ODEME MOVE ZEROS TO G-KOD GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 58 AND SEC = 1 AND YENI = 0
                MOVE OD-NO TO G-KOD CLOSE ODEME 
                GO STOP-PROGRAM.
           IF F = 13 GO YENI-ODEME.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-ODEME.
           IF YENI = 0 AND OD-NO = 1 GO TUS.
           CLOSE ODEME. OPEN I-O ODEME.
           IF YENI = 1
               MOVE E-ADI TO OD-ADI
               MOVE 1 TO OD-NO
               PERFORM YENI-YAZ
           ELSE
               MOVE E-ADI TO OD-ADI
               PERFORM REWRITE-ODEME
           END-IF.
           CLOSE ODEME. OPEN INPUT ODEME.
           READ ODEME.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-ODEME.
		   IF WVAR = 0 ADD 1 TO OD-NO GO YENI-YAZ.
       YENI-ODEME-SON. 
            
        
       DELETE-KOD.
           IF OD-NO = 1 GO TUS.
           CLOSE ODEME. OPEN I-O ODEME.
           READ ODEME.
           PERFORM DELETE-ODEME.
           CLOSE ODEME.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF G-KOD = OD-NO MOVE 1 TO G-KOD.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE 1 TO OD-NO.
           START ODEME KEY IS NOT LESS THAN OD-NO.
           READ ODEME NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO OD-NO.
           START ODEME KEY IS NOT GREATER THAN OD-NO.
           READ ODEME NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ ODEME NEXT
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
               READ ODEME PREVIOUS
             END-PERFORM.
           READ ODEME PREVIOUS AT END
              READ ODEME NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF OD-NO = ZEROS
		      READ ODEME NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ ODEME PREVIOUS AT END 
                    READ ODEME NEXT 
                    GO PG-OKU-SON.
		   IF OD-NO = ZEROS READ ODEME NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ ODEME PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ ODEME NEXT
           END-PERFORM.
           READ ODEME NEXT AT END
              READ ODEME PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF OD-NO = ZEROS 
		      READ ODEME PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ ODEME NEXT AT END 
             READ ODEME PREVIOUS MOVE ILK TO SATIR.
		   IF OD-NO = ZEROS
		     READ ODEME PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ ODEME NEXT
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
                  READ ODEME PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ ODEME PREVIOUS AT END READ ODEME NEXT GO TUS.
		   IF OD-NO = ZEROS READ ODEME NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ ODEME PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ ODEME NEXT AT END 
		       READ ODEME PREVIOUS GO ASAGI-YENI-AL.
		   IF OD-NO = ZEROS READ ODEME PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ ODEME NEXT
               END-PERFORM
                 MOVE SON TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
	   ASAGI-YENI-AL.
	       MOVE 1 TO YENI.
           IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ ODEME NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR
                   COMPUTE LIS = SON - 1
                   PERFORM YENI-SERITLI
                   GO TUS
           END-IF.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM YENI-SERITLI.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = SON PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = ILK GO Y-LISTE-SON.
           READ ODEME PREVIOUS AT END READ ODEME NEXT 
                GO Y-LISTE-SON.
		   IF OD-NO = ZEROS READ ODEME NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE OD-NO TO B-KOD.
           MOVE OD-ADI TO B-ADI.
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
           MOVE B-KOD TO OD-NO.
           ADD 1 TO OD-NO.
           MOVE OD-NO TO B-KOD.
           MOVE B-ADI TO OD-ADI.
           MOVE SPACES TO B-ADI.
           PERFORM SERITLI.
           SUBTRACT 1 FROM OD-NO.
           MOVE OD-NO TO B-KOD.
           MOVE OD-ADI TO B-ADI.
       YENI-SERITSIZ.
           MOVE B-KOD TO OD-NO.
           MOVE B-ADI TO OD-ADI.
           MOVE SPACES TO B-ADI.
           MOVE ZEROS TO B-KOD
           PERFORM SERITSIZ.
           MOVE OD-NO TO B-KOD.
           MOVE OD-ADI TO B-ADI.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
	       EXIT PROGRAM.
		   STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==ODEME==
                                  "%T%"  BY "ODEME"
                                 ==%R%== BY ==OD-KAYIT==.
       