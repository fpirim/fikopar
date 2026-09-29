       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'TELPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'siciltel.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'siciltel.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                 PIC 9999.
           02 E-TEL             PIC 9(12).
           02 E-TIP             PIC 9.
           02 E-DAHILI          PIC X(5).
           02 DUR               PIC X.
           02 YENI              PIC 9.
           02 SATIR             PIC 9999.
           02 CIZ               PIC 9999.
           02 LIS               PIC 9999.
           02 Z-TEL             PIC ZZBZZZBZZZZZZZ.
           02 ILK               PIC 9999 VALUE 6.
           02 SON               PIC 9999 VALUE 19.
           02 X                 PIC 9999 VALUE 69.
           02 B-SIRA            PIC 9(10).
           02 B-TIP             PIC 99.
           02 V-TEL             PIC 9999 VALUE ZEROS.
           02 V-FAX             PIC 9999 VALUE ZEROS.
           02 V-CEP             PIC 9999 VALUE ZEROS.
       01 BILGI.
           02 B-VAR             PIC X VALUE '+'.
           02 B-TEL             PIC ZZBZZZBZZZZZZZ.
           02 FILLER            PIC X VALUE ' '.
           02 BS-TIP            PIC XXX.
           02 FILLER            PIC X VALUE ' '.
           02 B-DAHILI          PIC X(5).
           
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       01 G-HESAPNO     PIC X(15).
       01 GOSTER        PIC 9.   
       PROCEDURE DIVISION USING G-HESAPNO GOSTER.
       START-PROGRAM.
           IF GOSTER = 1
                OPEN INPUT SICILTEL
                PERFORM VARSAYILAN-AL
                MOVE ILK TO SATIR
                MOVE 1 TO SCT-SIRA
                MOVE G-HESAPNO TO SCT-HESAPNO
                START SICILTEL KEY IS NOT LESS THAN SCT-KEY
                    INVALID KEY SUBTRACT 1 FROM SATIR
                        PERFORM A-LISTE-SON
                NOT INVALID KEY READ SICILTEL NEXT
                    IF G-HESAPNO = SCT-HESAPNO 
                        PERFORM A-LISTE THRU A-LISTE-SON
                    ELSE 
                        SUBTRACT 1 FROM SATIR
                        PERFORM A-LISTE-SON
                    END-IF
                END-START
                CLOSE SICILTEL
                EXIT PROGRAM
                STOP RUN.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
           DISPLAY SPACES LINE CIZ POSITION X SIZE 25
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
           END-PERFORM.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW
               ' F3 ' 'VARSAYILAN YAP' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT SICILTEL.
           PERFORM VARSAYILAN-AL.
		   MOVE 1 TO SCT-SIRA.
           MOVE G-HESAPNO TO SCT-HESAPNO.
           START SICILTEL KEY IS NOT LESS THAN SCT-KEY
             INVALID KEY GO A-LISTE-SON.
           READ SICILTEL NEXT.
           IF G-HESAPNO NOT = SCT-HESAPNO GO A-LISTE-SON.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK AND GOSTER = 0
              PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ SICILTEL NEXT AT END 
					READ SICILTEL PREVIOUS GO A-LISTE-SON.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS
		            READ SICILTEL PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY SPACES LINE CIZ POSITION X SIZE 25
                   CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE 1 TO SCT-SIRA.
           MOVE G-HESAPNO TO SCT-HESAPNO.
           START SICILTEL KEY IS NOT LESS THAN SCT-KEY
             INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ SICILTEL NEXT.
           IF G-HESAPNO NOT = SCT-HESAPNO 
             PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE ZEROS TO E-TEL MOVE 1 TO E-TIP
               ELSE MOVE SCT-TEL TO E-TEL MOVE SCT-DAHILI TO E-DAHILI
                  MOVE SCT-TIP TO E-TIP
           END-IF.
           ADD 1 TO X.
           DISPLAY SPACES LINE SATIR POSITION X SIZE 15
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           ACCEPT E-TEL LINE SATIR POSITION X UPDATE NO BEEP TAB
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           MOVE E-TEL TO Z-TEL.
           DISPLAY Z-TEL LINE SATIR POSITION X
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           SUBTRACT 1 FROM X. 
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SICILTEL GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO YENI-TEL.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 3 AND YENI = 0 GO VARSAYILAN-YAP.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       VARSAYILAN-AL.
           MOVE ZEROS TO SCT-SIRA.
           MOVE G-HESAPNO TO SCT-HESAPNO.
           START SICILTEL KEY IS EQUAL TO SCT-KEY
             INVALID KEY MOVE ZEROS TO V-TEL V-FAX V-CEP
            NOT INVALID KEY 
              READ SICILTEL NEXT
              COMPUTE V-TEL = SCT-TEL / 100000000
              COMPUTE V-FAX = (SCT-TEL - V-TEL * 100000000) / 10000
              COMPUTE V-CEP = SCT-TEL - V-TEL * 100000000 - V-FAX * 10000
           END-START.
       VARSAYILAN-YAP.
           MOVE ZEROS TO SCT-SIRA.
           CLOSE SICILTEL. OPEN I-O SICILTEL.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL B-TIP
                    MOVE SPACES TO B-DAHILI
                PERFORM WRITE-SICILTEL
           END-READ.
           EVALUATE B-TIP
           WHEN 1 MOVE B-SIRA TO V-TEL
           WHEN 2 MOVE B-SIRA TO V-CEP
           WHEN 3 MOVE B-SIRA TO V-FAX
           END-EVALUATE.
           COMPUTE SCT-TEL = V-TEL * 100000000 + V-FAX * 10000 + V-CEP.
           PERFORM REWRITE-SICILTEL.
		   CLOSE SICILTEL.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
       YENI-TEL.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           COMPUTE F = SATIR + 5.
           CALL 'GOLGE' USING SATIR 0083 F 0087.
           CANCEL 'GOLGE'.
       TEL-TIP-AL.
           ADD 15 TO X.
           COMPUTE F = SATIR + 1.
           DISPLAY 'ÉÍÍÍ»' LINE F POSITION X 
               CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ADD 1 TO F.
           DISPLAY 'ºTELº' LINE F POSITION X
               CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ADD 1 TO F.
           DISPLAY 'ºCEPº' LINE F POSITION X
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ADD 1 TO F.
           DISPLAY 'ºFAXº' LINE F POSITION X
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ADD 1 TO F.
           DISPLAY 'ÈÍÍÍ¼' LINE F POSITION X
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           COMPUTE F = SATIR + E-TIP + 1.
           ADD 1 TO X.
           IF E-TIP = 1 DISPLAY 'TEL' LINE F POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
                        'TEL' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           IF E-TIP = 2 DISPLAY 'CEP' LINE F POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
                        'CEP' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           IF E-TIP = 3 DISPLAY 'FAX' LINE F POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW
                        'FAX' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           SUBTRACT 1 FROM X.
           MOVE ' ' TO DUR.
           ACCEPT DUR NO BEEP LINE 37 POSITION 1 UPDATE.
           ACCEPT F FROM ESCAPE KEY.
           SUBTRACT 15 FROM X.
           IF F = 52 AND E-TIP > 1 SUBTRACT 1 FROM E-TIP
              GO TEL-TIP-AL.
           IF F = 53 AND E-TIP < 3 ADD 1 TO E-TIP
              GO TEL-TIP-AL.
           IF F = 52 OR F = 53 GO TEL-TIP-AL.
           IF F = 27 PERFORM TEL-TIP-AL-SON GO TUS.
           
       TEL-TIP-AL-SON.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       DAHILI-AL.
           ADD 16 TO X.
           IF E-TIP = 1 DISPLAY 'TEL' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           IF E-TIP = 2 DISPLAY 'CEP' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           IF E-TIP = 3 DISPLAY 'FAX' LINE SATIR POSITION X
                        CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           SUBTRACT 16 FROM X.
           ADD 20 TO X.
           ACCEPT E-DAHILI LINE SATIR POSITION X NO BEEP UPDATE
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           ACCEPT F FROM ESCAPE KEY.
           SUBTRACT 20 FROM X.
           IF F = 52 OR F = 27 GO YENI-TEL.
           
           CLOSE SICILTEL. OPEN I-O SICILTEL.
           
           IF YENI = 1 
                MOVE E-TEL TO SCT-TEL
                MOVE G-HESAPNO TO SCT-HESAPNO
                MOVE E-TIP TO SCT-TIP
                MOVE E-DAHILI TO SCT-DAHILI
                MOVE 1 TO SCT-SIRA
                PERFORM YENI-YAZ
           ELSE
                MOVE E-TEL TO SCT-TEL
                MOVE G-HESAPNO TO SCT-HESAPNO
                MOVE E-TIP TO SCT-TIP
                MOVE E-DAHILI TO SCT-DAHILI
                PERFORM REWRITE-SICILTEL
           END-IF.
           
           MOVE SCT-SIRA TO B-SIRA.
           MOVE ZEROS TO SCT-SIRA.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL B-TIP
                    MOVE SPACES TO B-DAHILI
                PERFORM WRITE-SICILTEL
           END-READ.
           EVALUATE E-TIP
           WHEN 1 MOVE B-SIRA TO V-TEL
           WHEN 2 MOVE B-SIRA TO V-CEP
           WHEN 3 MOVE B-SIRA TO V-FAX
           END-EVALUATE.
           COMPUTE SCT-TEL = V-TEL * 100000000 + V-FAX * 10000 + V-CEP.
           PERFORM REWRITE-SICILTEL.
           MOVE B-SIRA TO SCT-SIRA.
           
           CLOSE SICILTEL. OPEN INPUT SICILTEL.
           READ SICILTEL.
           PERFORM AKTAR.
           
           MOVE 0 TO YENI.
           IF SATIR NOT = SON
              MOVE SATIR TO LIS
              PERFORM Y-LISTE THRU Y-LISTE-SON
              COMPUTE F = LIS - ILK
              PERFORM F TIMES
                 READ SICILTEL NEXT
              END-PERFORM
              MOVE LIS TO SATIR PERFORM AKTAR 
              MOVE SATIR TO LIS     
           END-IF.
           GO ASAGI-AL.
       YENI-YAZ.
           PERFORM WRITE-SICILTEL.
		   IF WVAR = 0 ADD 1 TO SCT-SIRA GO YENI-YAZ.
		   
       DELETE-KOD.
           CLOSE SICILTEL. OPEN I-O SICILTEL.
           READ SICILTEL.
           PERFORM DELETE-SICILTEL.
           IF (B-SIRA = V-TEL AND V-TEL NOT = ZEROS) 
                MOVE ZEROS TO V-TEL.
           IF (B-SIRA = V-FAX AND V-FAX NOT = ZEROS)
                MOVE ZEROS TO V-FAX.
           IF (B-SIRA = V-CEP AND V-CEP NOT = ZEROS)
                MOVE ZEROS TO V-CEP.
           MOVE ZEROS TO SCT-SIRA.
           READ SICILTEL INVALID KEY CONTINUE
             NOT INVALID KEY
            COMPUTE SCT-TEL = V-TEL * 100000000 + V-FAX * 10000 + V-CEP
            PERFORM REWRITE-SICILTEL
           END-READ.
           CLOSE SICILTEL.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE 1 TO SCT-SIRA.
           MOVE G-HESAPNO TO SCT-HESAPNO
           START SICILTEL KEY IS NOT LESS THAN SCT-KEY.
           READ SICILTEL NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 9999 TO SCT-SIRA.
           MOVE G-HESAPNO TO SCT-HESAPNO
           START SICILTEL KEY IS NOT GREATER THAN SCT-KEY.
           READ SICILTEL NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ SICILTEL NEXT
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
               READ SICILTEL PREVIOUS
             END-PERFORM.
           READ SICILTEL PREVIOUS AT END
              READ SICILTEL NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS
		      READ SICILTEL NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ SICILTEL PREVIOUS AT END 
                    READ SICILTEL NEXT 
                    GO PG-OKU-SON.
           IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS
                    READ SICILTEL NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ SICILTEL PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ SICILTEL NEXT
           END-PERFORM.
           READ SICILTEL NEXT AT END
              READ SICILTEL PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS
		      READ SICILTEL PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ SICILTEL NEXT AT END 
             READ SICILTEL PREVIOUS MOVE ILK TO SATIR.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS
		     READ SICILTEL PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ SICILTEL NEXT
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
                  READ SICILTEL PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ SICILTEL PREVIOUS AT END READ SICILTEL NEXT GO TUS.
		   IF SCT-SIRA = ZEROS OR SCT-HESAPNO NOT = G-HESAPNO 
               READ SICILTEL NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ SICILTEL PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ SICILTEL NEXT AT END 
		       READ SICILTEL PREVIOUS GO ASAGI-YENI-AL.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS 
                READ SICILTEL PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ SICILTEL NEXT
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
                      READ SICILTEL NEXT
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
           READ SICILTEL PREVIOUS AT END READ SICILTEL NEXT 
                GO Y-LISTE-SON.
		   IF G-HESAPNO NOT = SCT-HESAPNO OR SCT-SIRA = ZEROS 
                READ SICILTEL NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SCT-SIRA TO B-SIRA.
           MOVE SCT-TEL TO B-TEL.
           MOVE SCT-TIP TO B-TIP.
           MOVE SCT-DAHILI TO B-DAHILI.
       SERITLI.
           PERFORM BS-AKTAR.
           DISPLAY BILGI LINE SATIR POSITION X
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           PERFORM BS-AKTAR.
           IF (B-SIRA = V-TEL AND V-TEL NOT = ZEROS) OR
              (B-SIRA = V-FAX AND V-FAX NOT = ZEROS) OR
              (B-SIRA = V-CEP AND V-CEP NOT = ZEROS)
            DISPLAY BILGI LINE SATIR POSITION X
              CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
           ELSE
            DISPLAY BILGI LINE SATIR POSITION X
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
           END-IF.
       BS-AKTAR.
           MOVE SPACES TO BS-TIP.
           EVALUATE B-TIP
           WHEN 1 MOVE 'TEL' TO BS-TIP
           WHEN 2 MOVE 'CEP' TO BS-TIP
           WHEN 3 MOVE 'FAX' TO BS-TIP
           END-EVALUATE.
       YENI-SERITLI.
           MOVE B-SIRA TO SCT-SIRA.
           ADD 1 TO SCT-SIRA.
           MOVE SCT-SIRA TO B-SIRA.
           MOVE B-TEL TO SCT-TEL.
           MOVE B-TIP TO SCT-TIP.
           MOVE B-DAHILI TO SCT-DAHILI.
           MOVE SPACES TO B-DAHILI.
           MOVE ZEROS TO B-TIP B-TEL.
           PERFORM SERITLI.
           SUBTRACT 1 FROM SCT-SIRA.
           MOVE SCT-SIRA TO B-SIRA.
           MOVE SCT-TEL TO B-TEL.
           MOVE SCT-TIP TO B-TIP.
           MOVE SCT-DAHILI TO B-DAHILI.
       YENI-SERITSIZ.
           MOVE B-SIRA TO SCT-SIRA.
           MOVE B-TEL TO SCT-TEL.
           MOVE B-DAHILI TO SCT-DAHILI.
           MOVE B-TIP TO SCT-TIP.
           MOVE SPACES TO B-DAHILI.
           MOVE ZEROS TO B-SIRA B-TIP B-TEL.
           PERFORM SERITSIZ.
           MOVE SCT-SIRA TO B-SIRA.
           MOVE SCT-TEL TO B-TEL.
           MOVE SCT-DAHILI TO B-DAHILI.
           MOVE SCT-TIP TO B-TIP.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
	   copy fileop.cpy REPLACING ==%T%== BY ==SICILTEL==
                                  "%T%"  BY "SICILTEL"
                                 ==%R%== BY ==SCT-KAYIT==.
       