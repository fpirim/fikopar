       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KATPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'plasiyer.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'plasiyer.cpz'.
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
           OPEN INPUT PLASIYER.
		   IF G-KOD NOT = ZEROS MOVE G-KOD TO PL-NO
		    ELSE MOVE 1 TO PL-NO.
           START PLASIYER KEY IS NOT LESS THAN PL-NO
             INVALID KEY GO A-LISTE-SON.
           READ PLASIYER NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ PLASIYER NEXT AT END 
					READ PLASIYER PREVIOUS GO A-LISTE-SON.
		   IF PL-NO = ZEROS
		            READ PLASIYER PREVIOUS GO A-LISTE-SON.
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
           IF G-KOD NOT = ZEROS MOVE G-KOD TO PL-NO
		    ELSE MOVE 1 TO PL-NO.
           START PLASIYER KEY IS NOT LESS THAN PL-NO
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ PLASIYER NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-ADI
               ELSE MOVE PL-ADI TO E-ADI
           END-IF.
           ACCEPT E-ADI LINE SATIR POSITION 43 UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE PLASIYER MOVE ZEROS TO G-KOD GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 58 AND SEC = 1 AND YENI = 0
                MOVE PL-NO TO G-KOD CLOSE PLASIYER 
                GO STOP-PROGRAM.
           IF F = 13 AND YENI = 1 GO YENI-PLASIYER.
           IF F = 13 AND YENI = 0 GO YENI-PLASIYER.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-PLASIYER.
           CLOSE PLASIYER. OPEN I-O PLASIYER.
           IF YENI = 1
               MOVE E-ADI TO PL-ADI
               MOVE 1 TO PL-NO
               PERFORM YENI-YAZ
           ELSE
               MOVE E-ADI TO PL-ADI
               PERFORM REWRITE-PLASIYER
           END-IF.
           CLOSE PLASIYER. OPEN INPUT PLASIYER.
           READ PLASIYER.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
           PERFORM WRITE-PLASIYER.
		   IF WVAR = 0 ADD 1 TO PL-NO GO YENI-YAZ.
       YENI-PLASIYER-SON. 
            
        
       DELETE-KOD.
           CLOSE PLASIYER. OPEN I-O PLASIYER.
           READ PLASIYER.
           PERFORM DELETE-PLASIYER.
           CLOSE PLASIYER.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF G-KOD = PL-NO MOVE 1 TO G-KOD.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE 1 TO PL-NO.
           START PLASIYER KEY IS NOT LESS THAN PL-NO.
           READ PLASIYER NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO PL-NO.
           START PLASIYER KEY IS NOT GREATER THAN PL-NO.
           READ PLASIYER NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ PLASIYER NEXT
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
               READ PLASIYER PREVIOUS
             END-PERFORM.
           READ PLASIYER PREVIOUS AT END
              READ PLASIYER NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF PL-NO = ZEROS
		      READ PLASIYER NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ PLASIYER PREVIOUS AT END 
                    READ PLASIYER NEXT 
                    GO PG-OKU-SON.
		   IF PL-NO = ZEROS READ PLASIYER NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ PLASIYER PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ PLASIYER NEXT
           END-PERFORM.
           READ PLASIYER NEXT AT END
              READ PLASIYER PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF PL-NO = ZEROS 
		      READ PLASIYER PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ PLASIYER NEXT AT END 
             READ PLASIYER PREVIOUS MOVE ILK TO SATIR.
		   IF PL-NO = ZEROS
		     READ PLASIYER PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ PLASIYER NEXT
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
                  READ PLASIYER PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ PLASIYER PREVIOUS AT END READ PLASIYER NEXT GO TUS.
		   IF PL-NO = ZEROS READ PLASIYER NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ PLASIYER PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ PLASIYER NEXT AT END 
		       READ PLASIYER PREVIOUS GO ASAGI-YENI-AL.
		   IF PL-NO = ZEROS READ PLASIYER PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ PLASIYER NEXT
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
                      READ PLASIYER NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
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
           READ PLASIYER PREVIOUS AT END READ PLASIYER NEXT 
                GO Y-LISTE-SON.
		   IF PL-NO = ZEROS READ PLASIYER NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE PL-NO TO B-KOD.
           MOVE PL-ADI TO B-ADI.
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
           MOVE B-KOD TO PL-NO.
           ADD 1 TO PL-NO.
           MOVE PL-NO TO B-KOD.
           MOVE B-ADI TO PL-ADI.
           MOVE SPACES TO B-ADI.
           PERFORM SERITLI.
           SUBTRACT 1 FROM PL-NO.
           MOVE PL-NO TO B-KOD.
           MOVE PL-ADI TO B-ADI.
       YENI-SERITSIZ.
           MOVE B-KOD TO PL-NO.
           MOVE B-ADI TO PL-ADI.
           MOVE SPACES TO B-ADI.
           MOVE ZEROS TO B-KOD
           PERFORM SERITSIZ.
           MOVE PL-NO TO B-KOD.
           MOVE PL-ADI TO B-ADI.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==PLASIYER==
                                  "%T%"  BY "PLASIYER"
                                 ==%R%== BY ==PL-KAYIT==.
       