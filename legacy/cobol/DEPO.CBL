       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'DEPO'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'depo.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'depo.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 CIZ                   PIC 9999.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 22.
           02 X                     PIC 9999 VALUE 31.
           02 E-ADI            PIC X(30).
           02 E-KDV                 PIC 9(4)V9999.
           02 Z-KDV             PIC Z,ZZZ.ZZZZ.
       01 BILGI.
           02 B-KOD                   PIC ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ADI                   PIC X(30).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-KDV                   PIC Z,ZZZ.ZZZZ.
       
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       SCREEN SECTION.
       01 SCR.
           02 LINE 5 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(4) FROM 'D.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(30) FROM 'DEPO ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM 'K%' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 7 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 23 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
             
       PROCEDURE DIVISION.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
           DISPLAY 'º' LINE CIZ POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                SPACES SIZE 4 'º' SPACES SIZE 30 'º' 
                SPACES SIZE 10 'º'
           END-PERFORM.
           DISPLAY SCR.
           COMPUTE CIZ = X + LENGTH OF BILGI + 1.
           CALL 'GOLGE' USING 0005 X 0023 CIZ.
           CANCEL 'GOLGE'.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT DEPO.
		   MOVE 1 TO DP-NO.
           START DEPO KEY IS NOT LESS THAN DP-NO
             INVALID KEY GO A-LISTE-SON.
           READ DEPO NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ DEPO NEXT AT END 
					READ DEPO PREVIOUS GO A-LISTE-SON.
		   IF DP-NO = ZEROS
		            READ DEPO PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION X
                   CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                   SPACES SIZE 4 'º' SPACES SIZE 30 'º' 
                   SPACES SIZE 10 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE 1 TO DP-NO.
           START DEPO KEY IS NOT LESS THAN DP-NO
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ DEPO NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-ADI MOVE ZEROS TO E-KDV
               ELSE MOVE DP-ADI TO E-ADI MOVE DP-KDV TO E-KDV
           END-IF.
           ADD 6 TO X.
           ACCEPT E-ADI LINE SATIR POSITION X UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           SUBTRACT 6 FROM X. 
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE DEPO GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 AND YENI = 1 GO YENI-DEPO.
           IF F = 13 AND YENI = 0 GO YENI-DEPO.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-DEPO.
           ADD 37 TO X.
           DISPLAY SPACES LINE SATIR POSITION X SIZE 10
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK'.
           ACCEPT E-KDV LINE SATIR POSITION X UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           MOVE E-KDV TO Z-KDV.
           DISPLAY Z-KDV LINE SATIR POSITION X
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           SUBTRACT 37 FROM X.
           
           IF F = 27 OR F = 52 GO TUS.
           CLOSE DEPO. OPEN I-O DEPO.
           IF YENI = 1
               MOVE E-ADI TO DP-ADI
               MOVE E-KDV TO DP-KDV
               MOVE 1 TO DP-NO
               PERFORM YENI-YAZ
           ELSE
               MOVE E-ADI TO DP-ADI
               MOVE E-KDV TO DP-KDV
               PERFORM REWRITE-DEPO
           END-IF.
           CLOSE DEPO. OPEN INPUT DEPO.
           READ DEPO.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-DEPO.
		   IF WVAR = 0 ADD 1 TO DP-NO GO YENI-YAZ.
       YENI-DEPO-SON. 
            
        
       DELETE-KOD.
           CLOSE DEPO. OPEN I-O DEPO.
           READ DEPO.
           PERFORM DELETE-DEPO.
           CLOSE DEPO.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE 1 TO DP-NO.
           START DEPO KEY IS NOT LESS THAN DP-NO.
           READ DEPO NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO DP-NO.
           START DEPO KEY IS NOT GREATER THAN DP-NO.
           READ DEPO NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ DEPO NEXT
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
               READ DEPO PREVIOUS
             END-PERFORM.
           READ DEPO PREVIOUS AT END
              READ DEPO NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF DP-NO = ZEROS
		      READ DEPO NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ DEPO PREVIOUS AT END 
                    READ DEPO NEXT 
                    GO PG-OKU-SON.
		   IF DP-NO = ZEROS READ DEPO NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ DEPO PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ DEPO NEXT
           END-PERFORM.
           READ DEPO NEXT AT END
              READ DEPO PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF DP-NO = ZEROS 
		      READ DEPO PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ DEPO NEXT AT END 
             READ DEPO PREVIOUS MOVE ILK TO SATIR.
		   IF DP-NO = ZEROS
		     READ DEPO PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ DEPO NEXT
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
                  READ DEPO PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ DEPO PREVIOUS AT END READ DEPO NEXT GO TUS.
		   IF DP-NO = ZEROS READ DEPO NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ DEPO PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ DEPO NEXT AT END 
		       READ DEPO PREVIOUS GO ASAGI-YENI-AL.
		   IF DP-NO = ZEROS READ DEPO PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ DEPO NEXT
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
                      READ DEPO NEXT
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
           READ DEPO PREVIOUS AT END READ DEPO NEXT 
                GO Y-LISTE-SON.
		   IF DP-NO = ZEROS READ DEPO NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE DP-NO TO B-KOD.
           MOVE DP-ADI TO B-ADI.
           MOVE DP-KDV TO B-KDV.
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
           MOVE B-KOD TO DP-NO.
           ADD 1 TO DP-NO.
           MOVE DP-NO TO B-KOD.
           MOVE B-ADI TO DP-ADI.
           MOVE B-KDV TO DP-KDV.
           MOVE SPACES TO B-ADI.
           MOVE ZEROS TO B-KDV.
           PERFORM SERITLI.
           SUBTRACT 1 FROM DP-NO.
           MOVE DP-NO TO B-KOD.
           MOVE DP-ADI TO B-ADI.
           MOVE DP-KDV TO B-KDV.
       YENI-SERITSIZ.
           MOVE B-KOD TO DP-NO.
           MOVE B-ADI TO DP-ADI.
           MOVE B-KDV TO DP-KDV.
           MOVE SPACES TO B-ADI.
           MOVE ZEROS TO B-KOD B-KDV.
           PERFORM SERITSIZ.
           MOVE DP-NO TO B-KOD.
           MOVE DP-ADI TO B-ADI.
           MOVE DP-KDV TO B-KDV.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==DEPO==
                                  "%T%"  BY "DEPO"
                                 ==%R%== BY ==DP-KAYIT==.
        