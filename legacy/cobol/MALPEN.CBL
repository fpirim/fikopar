       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'MALPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'sicilpos.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicilpos.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                 PIC 99.
           02 E-MAIL            PIC X(51).
           02 DUR               PIC X.
           02 YENI              PIC 9.
           02 SATIR             PIC 99.
           02 CIZ               PIC 99.
           02 LIS               PIC 99.
           02 ILK               PIC 99 VALUE 23.
           02 SON               PIC 99 VALUE 33.
       01 BILGI.
           02 B-MAIL             PIC X(51).
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
           OPEN INPUT SICILPOS.
           MOVE G-HESAPNO TO SP-HESAPNO.
           START SICILPOS KEY IS EQUAL TO SP-HESAPNO
               INVALID KEY GO ILK-SERIT. 
           READ SICILPOS NEXT.
           MOVE ILK TO SATIR.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ SICILPOS NEXT AT END 
                READ SICILPOS PREVIOUS
                GO A-LISTE-SON.
           IF SP-HESAPNO NOT = G-HESAPNO 
                READ SICILPOS PREVIOUS
                GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION 68
                   CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                   SPACES SIZE 51 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.
           IF SATIR NOT = SON ADD 1 TO SATIR PERFORM YENI-SERITSIZ
                SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-HESAPNO TO SP-HESAPNO.
           START SICILPOS KEY IS EQUAL TO SP-HESAPNO
            INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ SICILPOS NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-MAIL
           ELSE MOVE SP-MAIL TO E-MAIL
           END-IF.
           ACCEPT E-MAIL LINE 35 POSITION 69 UPDATE NO BEEP TAB
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SICILPOS GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 AND E-MAIL NOT = SPACES GO POS-DUZENLE.
           IF F = 10 AND YENI = 0 GO POS-SIL.
           GO TUS.
       POS-SIL.
           CLOSE SICILPOS. OPEN I-O SICILPOS.
           READ SICILPOS.
           PERFORM DELETE-SICILPOS.
           CLOSE SICILPOS.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
       
       POS-DUZENLE.
           CLOSE SICILPOS. OPEN I-O SICILPOS.
           IF YENI = 1 
                MOVE E-MAIL TO SP-MAIL
                MOVE 1 TO SP-SIRA
                PERFORM YENI-YAZ
           ELSE
                MOVE E-MAIL TO SP-MAIL
				PERFORM REWRITE-SICILPOS
           END-IF.
           CLOSE SICILPOS. OPEN INPUT SICILPOS.
           READ SICILPOS.
           MOVE 0 TO YENI.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
           PERFORM SATIR TIMES
                 READ SICILPOS PREVIOUS
           END-PERFORM.
           MOVE ILK TO SATIR. PERFORM AKTAR.
           GO TUS.
       YENI-YAZ.
	       PERFORM WRITE-SICILPOS.
		   IF WVAR = 0 ADD 1 TO SP-SIRA GO YENI-YAZ.
           
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
              PERFORM AKTAR PERFORM SERITLI MOVE 0 TO YENI GO TUS.
           READ SICILPOS PREVIOUS AT END READ SICILPOS NEXT GO TUS.
           IF G-HESAPNO NOT = SP-HESAPNO READ SICILPOS NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ SICILPOS PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ SICILPOS NEXT AT END READ SICILPOS PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK
                   PERFORM F TIMES
                      READ SICILPOS NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   PERFORM YENI-SERITLI
                   GO TUS
                END-IF
                PERFORM SERITSIZ
                ADD 1 TO SATIR
                PERFORM YENI-SERITLI
                GO TUS.
           IF G-HESAPNO NOT = SP-HESAPNO READ SICILPOS PREVIOUS
                MOVE 1 TO YENI
                IF SATIR = SON
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK
                   PERFORM F TIMES
                      READ SICILPOS NEXT
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
                      READ SICILPOS NEXT
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
           READ SICILPOS PREVIOUS AT END 
                READ SICILPOS NEXT 
                GO Y-LISTE-SON.
           IF G-HESAPNO NOT = SP-HESAPNO READ 
                SICILPOS NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SP-MAIL TO B-MAIL.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 69
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 69
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       YENI-SERITLI.
           DISPLAY SPACES 
                LINE SATIR POSITION 69 SIZE 51
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW 
              ' (YEN˜ KAYIT) ' LINE SATIR POSITION 69 
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       YENI-SERITSIZ.
           DISPLAY SPACES 
                LINE SATIR POSITION 69 SIZE 51
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' 
              ' (YEN˜ KAYIT) ' LINE SATIR POSITION 69 
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       LISTE-GOSTER.
           OPEN INPUT SICILPOS.
           MOVE ILK TO SATIR.
           MOVE G-HESAPNO TO SP-HESAPNO.
           START SICILPOS KEY IS EQUAL TO SP-HESAPNO
               INVALID KEY GO LISTE-A-LISTE-SON.
           READ SICILPOS NEXT.             
       LISTE-A-LISTE.
           PERFORM AKTAR.
           PERFORM SERITSIZ.
           IF SATIR = SON GO LISTE-A-LISTE-SON.
           READ SICILPOS NEXT AT END 
                READ SICILPOS PREVIOUS
                GO LISTE-A-LISTE-SON.
           IF SP-HESAPNO NOT = G-HESAPNO 
                READ SICILPOS PREVIOUS
                GO LISTE-A-LISTE-SON.
           ADD 1 TO SATIR.
           GO LISTE-A-LISTE.
       LISTE-A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION 68
                   CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                   SPACES SIZE 51 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.    
       LISTE-GOSTER-SON.
           CLOSE SICILPOS.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==SICILPOS==
                                  "%T%"  BY "SICILPOS"
                                 ==%R%== BY ==SP-KAYIT==.
       

