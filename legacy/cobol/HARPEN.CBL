       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'HARPEN'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'hareket.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'hareket.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 9999.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 32. 
           02 E-HESAPADI            PIC X(30).
           02 E-YETKILI             PIC X(30).
           02 X                     PIC 9999.
           02 IND-ARA               PIC 9.
       01 TEMP-TIP.
           02 T-TIP                 PIC 99.
           02 T-VAL                 PIC X(80).
       01 BILGI.
           02 B-EVRAKNO             PIC X(15).
           02 FILLER                PIC X VALUE 'º'.
           02 B-TARIH               PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-HESAPADI            PIC X(30).
           02 FILLER                PIC X VALUE 'º'.
           02 B-YETKILI             PIC X(30).
           02 FILLER                PIC X VALUE 'º'.
           02 B-TUTAR               PIC ZZZ.ZZZ.ZZZ,ZZ.
       copy 'status.cpy'.    
       copy 'win.cpy'.    
       LINKAGE SECTION.
       77 G-TIP               PIC 99.
       77 G-EVRAKNO           PIC X(15).
       PROCEDURE DIVISION USING G-TIP G-EVRAKNO.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
           MOVE LENGTH OF BILGI TO X.
           COMPUTE X = (120 - X) / 2.
           
           SUBTRACT 3 FROM ILK.
           DISPLAY 'É' LINE ILK POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 15 'Ë' ALL 'Í' SIZE 10 'Ë' 
                ALL 'Í' SIZE 30 'Ë' ALL 'Í' SIZE 30 'Ë'             
                ALL 'Í' SIZE 14 '»'.
           ADD 1 TO ILK.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
                DISPLAY 'º' LINE CIZ POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 15 'º' ALL ' ' SIZE 10 'º' 
                   ALL ' ' SIZE 30 'º' ALL ' ' SIZE 30 'º' 
                   ALL ' ' SIZE 14 'º'
           END-PERFORM.
           ADD 1 TO X.
           DISPLAY ' EVRAK NO ' LINE ILK POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ADD 16 TO X.
           DISPLAY ' TAR˜H ' LINE ILK POSITION X.
           ADD 11 TO X.
           DISPLAY ' HESAP ADI ' LINE ILK POSITION X.
           ADD 31 TO X.
           DISPLAY ' YETK˜L˜ ' LINE ILK POSITION X.
           ADD 31 TO X.
           DISPLAY ' TUTAR ' LINE ILK POSITION X.
           COMPUTE X = X - 1 - 16 - 11 - 31 - 31.
           ADD 1 TO ILK.
           DISPLAY 'Ì' LINE ILK POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 15 'Î' ALL 'Í' SIZE 10 'Î' 
                ALL 'Í' SIZE 30 'Î' ALL 'Í' SIZE 30 'Î' 
                ALL 'Í' SIZE 14 '¹'.
           ADD 1 TO ILK.
           ADD 1 TO SON.
           DISPLAY 'È' LINE 33 POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 15 'Ê' ALL 'Í' SIZE 10 'Ê' 
                ALL 'Í' SIZE 30 'Ê' ALL 'Í' SIZE 30 'Ê' 
                ALL 'Í' SIZE 14 '¼'.
           SUBTRACT 3 FROM ILK.
           COMPUTE F = (X + LENGTH OF BILGI + 1).
           CALL 'GOLGE' USING BY CONTENT ILK X SON F.
           CANCEL 'GOLGE'.
           SUBTRACT 1 FROM SON.
           ADD 3 TO ILK.
           
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
             ' F1 ' LINE 37 POSITION 1 
             'HESAP ADINA G™RE ARAMA' REVERSE LOW
             ' F2 ' 'YETK˜L˜YE G™RE ARAMA' REVERSE LOW.
             
       GIRIS.    
           MOVE 1 TO IND-ARA.
           MOVE ILK TO SATIR.
           OPEN INPUT HAREKET-INDEX HAREKET-SICIL HAREKET-DETAY.
           MOVE G-TIP TO T-TIP.
           MOVE SPACES TO T-VAL E-HESAPADI E-YETKILI.
           MOVE TEMP-TIP TO HIDX-EVRAKNO.
           START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-EVRAKNO
             INVALID KEY CLOSE HAREKET-SICIL HAREKET-DETAY HAREKET-INDEX
               GO STOP-PROGRAM.
           READ HAREKET-INDEX NEXT.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP CLOSE HAREKET-SICIL HAREKET-DETAY
                    HAREKET-INDEX GO STOP-PROGRAM.           
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ HAREKET-INDEX NEXT AT END 
                    READ HAREKET-INDEX PREVIOUS GO A-LISTE-SON.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP
                READ HAREKET-INDEX PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 15 'º' ALL ' ' SIZE 10 'º' 
                   ALL ' ' SIZE 30 'º' ALL ' ' SIZE 30 'º' 
                   ALL ' ' SIZE 14 'º'
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           EVALUATE IND-ARA
           WHEN 1 MOVE G-TIP TO T-TIP
                MOVE SPACES TO T-VAL MOVE TEMP-TIP TO HIDX-EVRAKNO
                START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-EVRAKNO
           WHEN 2 MOVE G-TIP TO T-TIP
                MOVE E-HESAPADI TO T-VAL MOVE TEMP-TIP TO HIDX-HESAPADI
                START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-HESAPADI
           WHEN 3 MOVE G-TIP TO T-TIP
                MOVE E-YETKILI TO T-VAL MOVE TEMP-TIP TO HIDX-YETKILI
                START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-YETKILI
           END-EVALUATE.
           READ HAREKET-INDEX NEXT.
           PERFORM AKTAR.
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
                    GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
               MOVE HS-EVRAKNO TO G-EVRAKNO GO STOP-PROGRAM.
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
           ACCEPT E-HESAPADI LINE 33 POSITION 36
            PROMPT '.' UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY ALL 'Í' LINE 33 POSITION 36 SIZE 30
           IF F = 27 GO TUS.
           STRING G-TIP E-HESAPADI DELIMITED BY SIZE INTO HIDX-HESAPADI.
           START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-HESAPADI
            INVALID KEY CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
              GO GIRIS.
           READ HAREKET-INDEX NEXT.
           MOVE 2 TO IND-ARA.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP 
             CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
             GO GIRIS.
           MOVE ILK TO SATIR.
           
           GO A-LISTE.
       YETKILI-ARAMA.
           ACCEPT E-YETKILI LINE 33 POSITION 67
            PROMPT '.' UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY ALL 'Í' LINE 33 POSITION 67 SIZE 30.
           IF F = 27 GO TUS.
           STRING G-TIP E-YETKILI DELIMITED BY SIZE INTO HIDX-YETKILI.
           START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-YETKILI
            INVALID KEY CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
              GO GIRIS.
           READ HAREKET-INDEX NEXT.
           MOVE 3 TO IND-ARA.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP 
             CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
             GO GIRIS.
           MOVE ILK TO SATIR.
           GO A-LISTE.       
       CTHO-TUSU.
           EVALUATE IND-ARA
           WHEN 1 MOVE G-TIP TO T-TIP
                  MOVE SPACES TO T-VAL MOVE TEMP-TIP TO HIDX-EVRAKNO
                  START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-EVRAKNO
           WHEN 2 MOVE G-TIP TO T-TIP
                  MOVE SPACES TO T-VAL MOVE TEMP-TIP TO HIDX-HESAPADI
                  START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-HESAPADI
           WHEN 3 MOVE G-TIP TO T-TIP
                  MOVE SPACES TO T-VAL MOVE TEMP-TIP TO HIDX-YETKILI
                  START HAREKET-INDEX KEY IS NOT LESS THAN HIDX-YETKILI
           END-EVALUATE.       
           READ HAREKET-INDEX NEXT.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP 
             CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
             GO GIRIS.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF LIS NOT = SON GO END-TUSU.
           EVALUATE IND-ARA
           WHEN 1 MOVE G-TIP TO T-TIP MOVE ALL 'ÿ' TO T-VAL
                  MOVE TEMP-TIP TO HIDX-EVRAKNO
             START HAREKET-INDEX KEY IS NOT GREATER THAN HIDX-EVRAKNO
           WHEN 2 MOVE G-TIP TO T-TIP MOVE ALL 'ÿ' TO T-VAL
                  MOVE TEMP-TIP TO HIDX-HESAPADI
             START HAREKET-INDEX KEY IS NOT GREATER THAN HIDX-HESAPADI
           WHEN 3 MOVE G-TIP TO T-TIP MOVE ALL 'ÿ' TO T-VAL
                  MOVE TEMP-TIP TO HIDX-YETKILI
             START HAREKET-INDEX KEY IS NOT GREATER THAN HIDX-YETKILI
           END-EVALUATE.       
           READ HAREKET-INDEX NEXT.
           PERFORM TIP-AL.
           IF T-TIP NOT = G-TIP 
             CLOSE HAREKET-INDEX HAREKET-DETAY HAREKET-SICIL
             GO GIRIS.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ HAREKET-INDEX NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
       PGUP-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
           PERFORM SATIR TIMES
               READ HAREKET-INDEX PREVIOUS
           END-PERFORM.
           READ HAREKET-INDEX PREVIOUS AT END
              READ HAREKET-INDEX NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP
              READ HAREKET-INDEX NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ HAREKET-INDEX PREVIOUS AT END 
                    READ HAREKET-INDEX NEXT 
                    GO PG-OKU-SON.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP 
                    READ HAREKET-INDEX NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ HAREKET-INDEX PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ HAREKET-INDEX NEXT
           END-PERFORM.
           READ HAREKET-INDEX NEXT AT END
              READ HAREKET-INDEX PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP
              READ HAREKET-INDEX PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ HAREKET-INDEX NEXT AT END 
             READ HAREKET-INDEX PREVIOUS MOVE ILK TO SATIR.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP
             READ HAREKET-INDEX PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
                 READ HAREKET-INDEX NEXT 
           END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
               PERFORM SATIR TIMES
                  READ HAREKET-INDEX PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           READ HAREKET-INDEX PREVIOUS AT END 
                READ HAREKET-INDEX NEXT GO TUS.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP
                READ HAREKET-INDEX NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ HAREKET-INDEX PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-AL.
           READ HAREKET-INDEX NEXT AT END 
                READ HAREKET-INDEX PREVIOUS GO TUS.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP
                READ HAREKET-INDEX PREVIOUS GO TUS.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ HAREKET-INDEX NEXT
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
           READ HAREKET-INDEX PREVIOUS AT END READ HAREKET-INDEX NEXT 
                GO Y-LISTE-SON.
           PERFORM TIP-AL.
           IF G-TIP NOT = T-TIP READ HAREKET-INDEX NEXT 
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       TIP-AL.
            EVALUATE IND-ARA
            WHEN 1 MOVE HIDX-EVRAKNO  TO TEMP-TIP
            WHEN 2 MOVE HIDX-HESAPADI TO TEMP-TIP
            WHEN 3 MOVE HIDX-YETKILI  TO TEMP-TIP
            END-EVALUATE.
       AKTAR.
           MOVE HIDX-EVRAKNO(3:) TO HS-EVRAKNO.
           MOVE HIDX-EVRAKNO(1:2) TO HS-TIP.
           READ HAREKET-SICIL INVALID KEY 
               INITIALIZE HS-KAYIT.
           MOVE HS-EVRAKNO TO B-EVRAKNO.
           STRING HS-DGUN DELIMITED BY SIZE
                  '/' DELIMITED BY SIZE
                  HS-DAY DELIMITED BY SIZE
                  '/' DELIMITED BY SIZE
                  HS-DYIL DELIMITED BY SIZE
                  INTO B-TARIH.
           MOVE HS-HESAPADI TO B-HESAPADI.
           MOVE HS-YETKILI TO B-YETKILI.
           MOVE ZEROS TO HD-SIRA.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           READ HAREKET-DETAY INVALID KEY MOVE ZEROS TO HD-FIYAT.
           MOVE HD-FIYAT TO B-TUTAR.
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
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.