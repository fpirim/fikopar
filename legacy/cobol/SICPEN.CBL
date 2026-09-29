       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'SICPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'sicil.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sicil.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 32. 
           02 E-HESAPADI            PIC X(40).
           02 E-YETKILI             PIC X(40).
           02 GORE                  PIC 9 VALUE 0.
           02 B-PLKOD               PIC 9(4).
       01 BILGI.
           02 B-HESAPNO             PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-HESAPADI            PIC X(40).
           02 FILLER                PIC X VALUE 'º'.
           02 B-YETKILI             PIC X(30).
           02 FILLER                PIC X VALUE 'º'.
           02 B-ADRES               PIC X(25).
           
       copy 'status.cpy'.
       copy 'win.cpy'.    
       LINKAGE SECTION.
       77 G-HESAPNO           PIC X(15).
       PROCEDURE DIVISION USING G-HESAPNO.
       START-PROGRAM.
      *     OPEN OUTPUT SICIL. 
      *    MOVE ZEROS TO SC-HESAPNO.
      *    MOVE '(BOž)' TO PL-ADI.
      *    WRITE PL-KAYIT.
      *     CLOSE SICIL.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
           DISPLAY 'É' LINE 5 POSITION 6
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 10 'Ë' 
                ALL 'Í' SIZE 40 'Ë' 
                ALL 'Í' SIZE 30 'Ë' 
                ALL 'Í' SIZE 25 '»'
                'È' LINE 33 POSITION 6
                ALL 'Í' SIZE 10 'Ê' 
                ALL 'Í' SIZE 40 'Ê' 
                ALL 'Í' SIZE 30 'Ê' 
                ALL 'Í' SIZE 25 '¼'.
           
           PERFORM VARYING CIZ FROM 6 BY 1 UNTIL CIZ > 32
                DISPLAY 'º' LINE CIZ POSITION 6
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 10 'º' 
                   ALL ' ' SIZE 40 'º' 
                   ALL ' ' SIZE 30 'º'
                   ALL ' ' SIZE 25 'º'
           END-PERFORM.
           
           DISPLAY 'Ì' LINE 7 POSITION 6
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 10 'Î' 
                ALL 'Í' SIZE 40 'Î' 
                ALL 'Í' SIZE 30 'Î' 
                ALL 'Í' SIZE 25 '¹'
                ' HESAP NO ' LINE 6 POSITION 7
                ' HESAP ADI ' LINE 6 POSITION 18
                ' YETK˜L˜ ' LINE 6 POSITION 59
                ' YETK˜L˜ ' LINE 6 POSITION 90.
           
           CALL 'GOLGE' USING 0005 0006 0033 0115.
           CANCEL 'GOLGE'.
           
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
             ' F1 ' LINE 37 POSITION 1 
             'HESAP ADINA G™RE ARAMA' REVERSE LOW
             ' F2 ' 'YETK˜L˜YE G™RE ARAMA' REVERSE LOW.
           
           MOVE ILK TO SATIR.
           OPEN INPUT SICIL.
           MOVE SPACES TO E-HESAPADI E-YETKILI.
           EVALUATE GORE
           WHEN 0 MOVE SPACES TO SC-HESAPNO
                  START SICIL KEY IS NOT LESS THAN SC-HESAPNO
                    INVALID KEY CLOSE SICIL GO STOP-PROGRAM
                  END-START
           WHEN 1 MOVE SPACES TO SC-HESAPADI
                  START SICIL KEY IS NOT LESS THAN SC-HESAPADI
                    INVALID KEY CLOSE SICIL GO STOP-PROGRAM
                  END-START
           WHEN 2 MOVE SPACES TO SC-YETKILI
                  START SICIL KEY IS NOT LESS THAN SC-YETKILI
                    INVALID KEY CLOSE SICIL GO STOP-PROGRAM
                  END-START
           END-EVALUATE.         
           READ SICIL NEXT.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ SICIL NEXT AT END 
                    READ SICIL PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION 6
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 10 'º' 
                   ALL ' ' SIZE 40 'º' 
                   ALL ' ' SIZE 30 'º'
                   ALL ' ' SIZE 25 'º'
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           EVALUATE GORE
           WHEN 0 MOVE SPACES TO SC-HESAPNO
                  START SICIL KEY IS NOT LESS THAN SC-HESAPNO
                    INVALID KEY GO TUS
                  END-START
           WHEN 1 MOVE E-HESAPADI TO SC-HESAPADI
                  START SICIL KEY IS NOT LESS THAN SC-HESAPADI
                    INVALID KEY GO TUS
                  END-START
           WHEN 2 MOVE E-YETKILI TO SC-YETKILI
                  START SICIL KEY IS NOT LESS THAN SC-YETKILI
                    INVALID KEY GO TUS
                  END-START
           END-EVALUATE.         
           READ SICIL NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SICIL GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 CLOSE SICIL 
               MOVE SC-HESAPNO TO G-HESAPNO GO STOP-PROGRAM.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 1 GO HESAPADI-ARAMA.
           IF F = 2 GO YETKILI-ARAMA.
           GO TUS.
       HESAPADI-ARAMA.
           ACCEPT E-HESAPADI LINE 33 POSITION 27 
            PROMPT '.' UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY ALL 'Í' LINE 33 POSITION 27 SIZE 40
           IF F = 27 GO TUS.
           MOVE E-HESAPADI TO SC-HESAPADI.
           START SICIL KEY IS NOT LESS THAN SC-HESAPADI
            INVALID KEY CLOSE SICIL OPEN INPUT SICIL.
           READ SICIL NEXT.
           MOVE ILK TO SATIR.
           MOVE 1 TO GORE.
           GO A-LISTE.
       YETKILI-ARAMA.
           ACCEPT E-YETKILI LINE 33 POSITION 68
            PROMPT '.' UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY ALL 'Í' LINE 33 POSITION 68 SIZE 40.
           IF F = 27 GO TUS.
           MOVE E-YETKILI TO SC-YETKILI.
           START SICIL KEY IS NOT LESS THAN SC-YETKILI
            INVALID KEY CLOSE SICIL OPEN INPUT SICIL.
           READ SICIL NEXT.
           MOVE ILK TO SATIR.
           MOVE 2 TO GORE.
           GO A-LISTE.
       
       CTHO-TUSU.
           EVALUATE GORE
           WHEN 0 MOVE SPACES TO SC-HESAPNO
                  START SICIL KEY IS NOT LESS THAN SC-HESAPNO
                  END-START
           WHEN 1 MOVE SPACES TO SC-HESAPADI
                  START SICIL KEY IS NOT LESS THAN SC-HESAPADI
                  END-START
           WHEN 2 MOVE SPACES TO SC-YETKILI
                  START SICIL KEY IS NOT LESS THAN SC-YETKILI
                  END-START
           END-EVALUATE.         
           READ SICIL NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           EVALUATE GORE
           WHEN 0 MOVE ALL 'ÿ' TO SC-HESAPNO
                  START SICIL KEY IS NOT GREATER THAN SC-HESAPNO
                  END-START
           WHEN 1 MOVE ALL 'ÿ' TO SC-HESAPADI
                  START SICIL KEY IS NOT GREATER THAN SC-HESAPADI
                  END-START
           WHEN 2 MOVE ALL 'ÿ' TO SC-YETKILI
                  START SICIL KEY IS NOT GREATER THAN SC-YETKILI
                  END-START
           END-EVALUATE.         
           READ SICIL NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ SICIL NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
       PGUP-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ SICIL PREVIOUS
             END-PERFORM.
           READ SICIL PREVIOUS AT END
              READ SICIL NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ SICIL PREVIOUS AT END 
                    READ SICIL NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ SICIL PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ SICIL NEXT
           END-PERFORM.
           READ SICIL NEXT AT END
              READ SICIL PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ SICIL NEXT AT END 
             READ SICIL PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ SICIL NEXT 
                 END-READ 
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ SICIL PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           READ SICIL PREVIOUS AT END READ SICIL NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ SICIL PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           READ SICIL NEXT AT END READ SICIL PREVIOUS GO TUS.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ SICIL NEXT
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
           READ SICIL PREVIOUS AT END READ SICIL NEXT 
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SC-HESAPNO TO B-HESAPNO.
           MOVE SC-HESAPADI TO B-HESAPADI.
           MOVE SC-YETKILI TO B-YETKILI.
		   MOVE SC-PLKOD TO B-PLKOD.
           MOVE SPACES TO B-ADRES.
           STRING SC-MAHALLE '/' SC-ILCE '/' SC-IL '/' SC-ULKE
                DELIMITED BY SPACES INTO B-ADRES.
       SERITLI.
           IF B-PLKOD = 5
		     DISPLAY BILGI LINE SATIR POSITION 7
                CONTROL 'BCOLOR=RED, FCOLOR=BLACK' LOW
		   ELSE
		     DISPLAY BILGI LINE SATIR POSITION 7
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
		   END-IF.
       SERITSIZ.
	       IF B-PLKOD = 5 
             DISPLAY BILGI LINE SATIR POSITION 7
                CONTROL 'BCOLOR=BLACK, FCOLOR=RED'
		   ELSE
		     DISPLAY BILGI LINE SATIR POSITION 7
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
		   END-IF.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
              