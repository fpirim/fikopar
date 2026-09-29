       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'TIPPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'sicilkod.cpy'.
       copy 'tipkod.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicilkod.cpz'.
       copy 'tipkod.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                 PIC 99.
           02 DUR               PIC X.
           02 YENI              PIC 9.
           02 SATIR             PIC 99.
           02 CIZ               PIC 99.
           02 LIS               PIC 99.
           02 ILK               PIC 99 VALUE 6.
           02 SON               PIC 99 VALUE 19.
           02 T-KOD             PIC 9999.
       01 BILGI.
           02 B-TIP             PIC X(25).
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       01 G-HESAPNO     PIC X(15).
       01 GOSTER        PIC 9.
       SCREEN SECTION.
       PROCEDURE DIVISION USING G-HESAPNO GOSTER.
       START-PROGRAM.
           IF GOSTER = 1
                PERFORM LISTE-GOSTER THRU LISTE-GOSTER-SON
                EXIT PROGRAM
                STOP RUN.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
                 ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           OPEN INPUT SICILKOD TIPKOD.
           MOVE G-HESAPNO TO SK-HESAPNO.
           START SICILKOD KEY IS EQUAL TO SK-HESAPNO
               INVALID KEY GO ILK-SERIT. 
           READ SICILKOD NEXT.
           MOVE ILK TO SATIR.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ SICILKOD NEXT AT END 
                READ SICILKOD PREVIOUS
                GO A-LISTE-SON.
           IF SK-HESAPNO NOT = G-HESAPNO 
                READ SICILKOD PREVIOUS
                GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY SPACES LINE CIZ POSITION 95
                   CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 25 
             END-PERFORM SUBTRACT 1 FROM SATIR.
           IF SATIR NOT = SON ADD 1 TO SATIR PERFORM YENI-SERITSIZ
                SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-HESAPNO TO SK-HESAPNO.
           START SICILKOD KEY IS EQUAL TO SK-HESAPNO
            INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ SICILKOD NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SICILKOD TIPKOD GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO KOD-DUZENLE.
           IF F = 10 AND YENI = 0 GO KOD-SIL.
           GO TUS.
       KOD-SIL.
           CLOSE SICILKOD. OPEN I-O SICILKOD.
           READ SICILKOD.
           PERFORM DELETE-SICILKOD.
           CLOSE SICILKOD TIPKOD.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
       
       KOD-DUZENLE.
           IF YENI = 1 
              MOVE 1 TO T-KOD
           ELSE
              MOVE SK-KOD TO T-KOD
           END-IF.
           CALL 'KODPEN' USING 1 T-KOD.
           CANCEL 'KODPEN'.
           IF T-KOD = ZEROS
				MOVE WCB TO NESTED-WCB (1)
				DISPLAY NESTED-WCB (1)
					LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'
				CLOSE SICILKOD TIPKOD GO START-PROGRAM.	
           CLOSE SICILKOD. OPEN I-O SICILKOD.
           IF YENI = 1 
                MOVE T-KOD TO SK-KOD
				PERFORM WRITE-SICILKOD
				IF WVAR = 0 GO KOD-VAR END-IF
	       ELSE
                READ SICILKOD END-READ
                PERFORM DELETE-SICILKOD
				MOVE T-KOD TO SK-KOD
				PERFORM WRITE-SICILKOD
				IF WVAR = 0 GO KOD-VAR END-IF
           END-IF.
       KOD-VAR.
           CLOSE SICILKOD. OPEN INPUT SICILKOD.
           READ SICILKOD.
           MOVE 0 TO YENI.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
           PERFORM SATIR TIMES
                 READ SICILKOD PREVIOUS
           END-PERFORM.
           MOVE ILK TO SATIR. PERFORM AKTAR.
           GO TUS.
           
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
              PERFORM AKTAR PERFORM SERITLI MOVE 0 TO YENI GO TUS.
           READ SICILKOD PREVIOUS AT END READ SICILKOD NEXT GO TUS.
           IF G-HESAPNO NOT = SK-HESAPNO READ SICILKOD NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ SICILKOD PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ SICILKOD NEXT AT END READ SICILKOD PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK
                   PERFORM F TIMES
                      READ SICILKOD NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   PERFORM YENI-SERITLI
                   GO TUS
                END-IF
                PERFORM SERITSIZ
                ADD 1 TO SATIR
                PERFORM YENI-SERITLI
                GO TUS.
           IF G-HESAPNO NOT = SK-HESAPNO READ SICILKOD PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK
                   PERFORM F TIMES
                      READ SICILKOD NEXT
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
                 PERFORM SON TIMES
                      READ SICILKOD NEXT
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
           READ SICILKOD PREVIOUS AT END 
                READ SICILKOD NEXT 
                GO Y-LISTE-SON.
           IF G-HESAPNO NOT = SK-HESAPNO READ 
                SICILKOD NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SK-KOD TO TK-KOD.
		   MOVE SK-KOD TO B-TIP.
           READ TIPKOD INVALID KEY
            MOVE '#ERROR#' TO TK-TIP.
           MOVE TK-TIP TO B-TIP.    
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 95
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 95
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       YENI-SERITLI.
	       DISPLAY SPACES 
                LINE SATIR POSITION 95 SIZE 25
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW 
              ' (YEN˜ KAYIT) ' LINE SATIR POSITION 95
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       YENI-SERITSIZ.
		   DISPLAY SPACES 
                LINE SATIR POSITION 95 SIZE 25
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' 
              ' (YEN˜ KAYIT) ' LINE SATIR POSITION 95
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       LISTE-GOSTER.
           OPEN INPUT SICILKOD TIPKOD.
           MOVE ILK TO SATIR.
           MOVE G-HESAPNO TO SK-HESAPNO.
           START SICILKOD KEY IS EQUAL TO SK-HESAPNO
               INVALID KEY GO LISTE-A-LISTE-SON.
           READ SICILKOD NEXT.             
       LISTE-A-LISTE.
           PERFORM AKTAR.
           PERFORM SERITSIZ.
           IF SATIR = SON GO LISTE-A-LISTE-SON.
           READ SICILKOD NEXT AT END 
                READ SICILKOD PREVIOUS
                GO LISTE-A-LISTE-SON.
           IF SK-HESAPNO NOT = G-HESAPNO 
                READ SICILKOD PREVIOUS
                GO LISTE-A-LISTE-SON.
           ADD 1 TO SATIR.
           GO LISTE-A-LISTE.
       LISTE-A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY SPACES LINE CIZ POSITION 95
                   CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 25
             END-PERFORM SUBTRACT 1 FROM SATIR.    
       LISTE-GOSTER-SON.
           CLOSE SICILKOD TIPKOD.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==SICILKOD==
                                  "%T%"  BY "SICILKOD"
                                 ==%R%== BY ==SK-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==TIPKOD==
                                  "%T%"  BY "TIPKOD"
                                 ==%R%== BY ==TK-KAYIT==.
              

