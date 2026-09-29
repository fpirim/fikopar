       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KODSEC'.
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
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999.
           02 SON                   PIC 9999.
           02 WIDTH                 PIC 9999.
           02 I                     PIC 9999.
           02 J                     PIC 9999.
       01 BILGI.
           02 B-ADI                 PIC X(118).
       copy 'status.cpy'.
       copy 'win.cpy'.    
       LINKAGE SECTION.
       77 OKU             PIC 9.
       77 G-KOD           PIC S9999.
       77 G-ADI           PIC X(30).
       77 X1              PIC 9999.
       77 Y1              PIC 9999.
       77 X2              PIC 9999.
       77 Y2              PIC 9999.
       77 BOSIZIN         PIC 9.
       PROCEDURE DIVISION USING OKU G-KOD G-ADI X1 Y1 X2 Y2 
            BOSIZIN.
       START-PROGRAM.
           IF OKU = 1 
                  OPEN INPUT TIPKOD
                  MOVE G-KOD TO TK-KOD
                  READ TIPKOD INVALID KEY 
				     MOVE SPACES TO TK-TIP
			      END-READ
                  MOVE TK-TIP TO G-ADI
                  IF BOSIZIN = 0 AND TK-KOD = ZEROS
                    MOVE SPACES TO G-ADI
                  END-IF
                  CLOSE TIPKOD
                  EXIT PROGRAM
                  STOP RUN.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           COMPUTE ILK = Y1 + 1.
           COMPUTE SON = Y2 - 1.
           COMPUTE WIDTH = X2 - X1 - 1.
           
           DISPLAY 'É' LINE Y1 POSITION X1
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE WIDTH '»'
                'È' LINE Y2 POSITION X1 ALL 'Í' SIZE WIDTH '¼'.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
           DISPLAY 'º' LINE CIZ POSITION X1
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE WIDTH 'º'
           END-PERFORM.
           CALL 'GOLGE' USING Y1 X1 Y2 X2.
           CANCEL 'GOLGE'.
           ADD 1 TO X1.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
           
           MOVE ILK TO SATIR.
           OPEN INPUT TIPKOD.
           MOVE G-KOD TO TK-KOD.
           IF BOSIZIN = 0 MOVE 1 TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD
             INVALID KEY MOVE -1 TO G-KOD CLOSE TIPKOD GO STOP-PROGRAM.
           READ TIPKOD NEXT.
           
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ TIPKOD NEXT AT END 
                    READ TIPKOD PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY ALL ' ' LINE CIZ POSITION X1 SIZE WIDTH
                   CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-KOD TO TK-KOD.
           IF BOSIZIN = 0 MOVE 1 TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD
                INVALID KEY GO TUS.
           READ TIPKOD NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE TIPKOD MOVE -1 TO G-KOD GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 CLOSE TIPKOD 
               MOVE TK-KOD TO G-KOD 
               MOVE TK-TIP TO G-ADI
               GO STOP-PROGRAM.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           GO TUS.
       CTHO-TUSU.
           MOVE ZEROS TO TK-KOD.
           IF G-KOD = ZEROS AND BOSIZIN = 0
                MOVE 1 TO TK-KOD
           ELSE MOVE G-KOD TO TK-KOD.
           START TIPKOD KEY IS NOT LESS THAN TK-KOD.
           READ TIPKOD NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
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
           IF BOSIZIN = 0 AND TK-KOD = ZEROS
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
           IF BOSIZIN = 0 AND TK-KOD = ZEROS READ TIPKOD NEXT
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
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ TIPKOD NEXT AT END 
             READ TIPKOD PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ TIPKOD NEXT 
                 END-READ 
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ TIPKOD PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           READ TIPKOD PREVIOUS AT END READ TIPKOD NEXT GO TUS.
           IF BOSIZIN = 0 AND TK-KOD = ZEROS READ TIPKOD NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ TIPKOD PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           READ TIPKOD NEXT AT END READ TIPKOD PREVIOUS GO TUS.
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
           READ TIPKOD PREVIOUS AT END READ TIPKOD NEXT 
                GO Y-LISTE-SON.
           IF BOSIZIN = 0 AND TK-KOD = ZEROS READ TIPKOD NEXT 
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE TK-TIP TO B-ADI.
       SERITLI.
           DISPLAY B-ADI LINE SATIR POSITION X1 SIZE WIDTH
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
       SERITSIZ.
           DISPLAY B-ADI LINE SATIR POSITION X1 SIZE WIDTH
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
              