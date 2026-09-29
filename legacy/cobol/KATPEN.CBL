       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KATPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'katlog.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'katlog.cpz'.
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
           02 X                     PIC 9999 VALUE 22.
           02 E-ACIKLAMA            PIC X(40).
           02 E-HESAPNO             PIC X(15).
       01 BILGI.
           02 B-KOD                   PIC ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ACIKLAMA              PIC X(40).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-HESAPNO               PIC X(15).
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
           02 'Ë' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(4) FROM 'K.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(40) FROM 'A€IKLAMA' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(15) FROM 'HESAP NO' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 7 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 23 COLUMN X BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(40) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
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
                SPACES SIZE 15 'º'
           END-PERFORM.
           DISPLAY SCR.
           CALL 'GOLGE' USING 0005 X 0023 0083.
           CANCEL 'GOLGE'.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' TAB ' LINE 37 POSITION 1 ' SE€ ' REVERSE LOW
               ' F10 ' 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT KATLOG.
           MOVE G-KOD TO KT-KATNO.
           START KATLOG KEY IS NOT LESS THAN KT-KATNO
             INVALID KEY GO A-LISTE-SON.
           READ KATLOG NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ KATLOG NEXT AT END READ KATLOG PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION X
                   CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                   SPACES SIZE 4 'º' SPACES SIZE 40 'º' 
                   SPACES SIZE 15 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-KOD TO KT-KATNO.
           START KATLOG KEY IS NOT LESS THAN KT-KATNO
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ KATLOG NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-ACIKLAMA E-HESAPNO
               ELSE MOVE KT-ACIKLAMA TO E-ACIKLAMA 
                    MOVE KT-HESAPNO TO E-HESAPNO
           END-IF.
           ADD 6 TO X.
           ACCEPT E-ACIKLAMA LINE SATIR POSITION X UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           SUBTRACT 6 FROM X.
           IF F = 27 CLOSE KATLOG GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 58 AND SEC = 1 AND YENI = 0
                MOVE KT-KATNO TO G-KOD CLOSE KATLOG 
                GO STOP-PROGRAM.
           IF F = 13 GO YENI-KATLOG.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-KATLOG.
       HESAPNO-AL.
           ADD 47 TO X.
           ACCEPT E-HESAPNO LINE SATIR POSITION X UPDATE
            NO BEEP CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
           ACCEPT F FROM ESCAPE KEY.
           SUBTRACT 47 FROM X.
           IF F = 27 OR F = 52 GO TUS.
           IF F = 1 CALL 'SICPEN' USING E-HESAPNO
                    CANCEL 'SICPEN' GO HESAPNO-AL.
           CLOSE KATLOG. OPEN I-O KATLOG.
           IF YENI = 1
               MOVE E-HESAPNO TO KT-HESAPNO
               MOVE E-ACIKLAMA TO KT-ACIKLAMA
               MOVE 1 TO KT-KATNO
               PERFORM YENI-YAZ
           ELSE
               MOVE E-HESAPNO TO KT-HESAPNO
               MOVE E-ACIKLAMA TO KT-ACIKLAMA
               PERFORM REWRITE-KATLOG
           END-IF.
           CLOSE KATLOG. OPEN INPUT KATLOG.
           READ KATLOG.
           PERFORM AKTAR.
		   MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-KATLOG.
		   IF WVAR = 0 ADD 1 TO KT-KATNO GO YENI-YAZ.
           
       YENI-KATLOG-SON. 
            
        
       DELETE-KOD.
           CLOSE KATLOG. OPEN I-O KATLOG.
           READ KATLOG.
           PERFORM DELETE-KATLOG.
           CLOSE KATLOG.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           IF G-KOD = KT-KATNO MOVE 1 TO G-KOD.
           GO START-PROGRAM.
           
       CTHO-TUSU.
		   IF YENI = 1 AND SATIR = ILK GO TUS.
		   IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI.
           MOVE 1 TO KT-KATNO.
           START KATLOG KEY IS NOT LESS THAN KT-KATNO.
           READ KATLOG NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO KT-KATNO.
           START KATLOG KEY IS NOT GREATER THAN KT-KATNO.
           READ KATLOG NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ KATLOG NEXT
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
               READ KATLOG PREVIOUS
             END-PERFORM.
           READ KATLOG PREVIOUS AT END
              READ KATLOG NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
			  
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ KATLOG PREVIOUS AT END 
                    READ KATLOG NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ KATLOG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ KATLOG NEXT
           END-PERFORM.
           READ KATLOG NEXT AT END
              READ KATLOG PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ KATLOG NEXT AT END 
             READ KATLOG PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ KATLOG NEXT
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
                  READ KATLOG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
		       MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ KATLOG PREVIOUS AT END READ KATLOG NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ KATLOG PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ KATLOG NEXT AT END READ KATLOG PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ KATLOG NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR
                   COMPUTE LIS = SON - 1                   
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
                      READ KATLOG NEXT
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
           READ KATLOG PREVIOUS AT END 
                READ KATLOG NEXT AT END CONTINUE END-READ
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE KT-KATNO TO B-KOD.
           MOVE KT-ACIKLAMA TO B-ACIKLAMA.
           MOVE KT-HESAPNO TO B-HESAPNO.
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
           MOVE B-KOD TO KT-KATNO.
           ADD 1 TO KT-KATNO.
           MOVE KT-KATNO TO B-KOD.
           MOVE B-ACIKLAMA TO KT-ACIKLAMA.
           MOVE B-HESAPNO TO KT-HESAPNO.
           MOVE SPACES TO B-ACIKLAMA B-HESAPNO.
           PERFORM SERITLI.
           SUBTRACT 1 FROM KT-KATNO.
           MOVE KT-KATNO TO B-KOD.
           MOVE KT-ACIKLAMA TO B-ACIKLAMA.
           MOVE KT-HESAPNO TO B-HESAPNO.
       YENI-SERITSIZ.
           MOVE B-KOD TO KT-KATNO.
           MOVE B-ACIKLAMA TO KT-ACIKLAMA.
           MOVE B-HESAPNO TO KT-HESAPNO.
           MOVE SPACES TO B-ACIKLAMA B-HESAPNO.
		   MOVE ZEROS TO B-KOD
           PERFORM SERITSIZ.
           MOVE KT-KATNO TO B-KOD.
           MOVE KT-ACIKLAMA TO B-ACIKLAMA.
           MOVE KT-HESAPNO TO B-HESAPNO.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
		   EXIT PROGRAM.
		   STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==KATLOG==
                                  "%T%"  BY "KATLOG"
                                 ==%R%== BY ==KT-KAYIT==.
			  
