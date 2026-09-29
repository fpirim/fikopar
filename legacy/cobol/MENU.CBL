       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'MENU'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
            SELECT MENUOKU ASSIGN TO DISK, DOSYAADI
                   ORGANIZATION LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD MENUOKU.
       01 MO-KAYIT.
           02 MO-ACIKLAMA           PIC X(45).
           02                       PIC X.
           02 MO-PROGRAM            PIC X(8).
           02                       PIC X.
           02 MO-PARAM              PIC X(80).
       WORKING-STORAGE SECTION.
       01 SCREEN-VALS.
           02 F             PIC 9999.
           02 DUR           PIC 99.
           02 I             PIC 9999.
           02 SATIR         PIC 9999.
           02 SON           PIC 9999.
           02 LEN           PIC 9999.
           02 X         PIC 9999.
           02 Y         PIC 9999.
           02 DOSYAADI  PIC X(12).
       77 MENU-LEN          PIC 9999.
       01 MENU-VAR.
           02 MENULIST      OCCURS 1000 TIMES.
               03 MENU-DESC         PIC X(45).
               03 MENU-LINE         PIC 99.
               03 MENU-SATIR        PIC 9999.
               03 MENU-PROGRAM      PIC X(8).
               03 MENU-PARAM        PIC X(80).
       copy 'win.cpy'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
               INTO DOSYAADI, Y, X
           END-UNSTRING.
           OPEN INPUT MENUOKU.
           READ MENUOKU NEXT.
           READ MENUOKU NEXT.
           MOVE 1 TO I.
           COMPUTE SATIR = Y + 2.
       MENU-AL.
           MOVE SPACES TO MO-ACIKLAMA MO-PROGRAM MO-PARAM.
           READ MENUOKU NEXT AT END CLOSE MENUOKU GO MENU-SON.
           IF MO-ACIKLAMA = SPACES ADD 1 TO SATIR GO MENU-AL.           
           MOVE MO-ACIKLAMA TO MENU-DESC(I).
           MOVE MO-PROGRAM TO MENU-PROGRAM(I).
           MOVE MO-PARAM TO MENU-PARAM(I).
           MOVE SATIR TO MENU-SATIR(I).
           ADD 1 TO I. ADD 1 TO SATIR.
           GO MENU-AL.
       MENU-SON.
           MOVE I TO LEN.
           SUBTRACT 1 FROM LEN.
       BEGIN-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           
           DISPLAY  '…' LINE Y POSITION X
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                  ALL 'Õ' SIZE 45 'ª'.
           ADD 1 TO Y.
           DISPLAY '∫' LINE Y POSITION X
                      CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                             SPACES SIZE 45 '∫'.
                             
           COMPUTE F = MENU-SATIR(LEN) + 2.
           PERFORM VARYING I FROM Y BY 1 UNTIL I > F
                   DISPLAY '∫' LINE I POSITION X
                      CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                            SPACES SIZE 45 '∫'
           END-PERFORM.
            
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > LEN
                DISPLAY '∫' LINE MENU-SATIR(I) POSITION X
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' MENU-DESC(I) '∫'
           END-PERFORM.
           COMPUTE SON = MENU-SATIR(LEN) + 2.
           DISPLAY  '»Õ[  ]' LINE SON POSITION X
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                  ALL 'Õ' SIZE 40 'º'.
           
           MOVE 1 TO DUR.
           SUBTRACT 1 FROM Y.
           COMPUTE F = X + 46.
           CALL 'GOLGE' USING BY CONTENT Y X SON F.
           CANCEL 'GOLGE'.
           
       SOR.
           MOVE DUR TO I.
           PERFORM SERITLI.
           COMPUTE F = X + 3.
           ACCEPT DUR NO BEEP LINE SON POSITION F UPDATE
               CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM SERITSIZ.
           IF F = 27 GO STOP-PROGRAM.
           IF DUR < 1 OR DUR > LEN GO SOR.
           IF F = 52
              IF DUR = 1 MOVE LEN TO DUR ELSE SUBTRACT 1 FROM DUR END-IF
           END-IF.
           IF F = 53 
              IF DUR = LEN MOVE 1 TO DUR ELSE ADD 1 TO DUR END-IF 
           END-IF.
           IF F = 13 AND MENU-PROGRAM(I) NOT = SPACES
                    MOVE MENU-PARAM(I) TO MAIN-ARGUMENT-VALUE
                    MOVE LENGTH OF MENU-PARAM(I) TO MAIN-ARGUMENT-SIZE
                     CALL MENU-PROGRAM(I) USING BY CONTENT MAIN-ARGUMENT
                     CANCEL MENU-PROGRAM(I).
            GO SOR.
       SERITSIZ.
           ADD 1 TO X.
           DISPLAY MENU-DESC(I) LINE MENU-SATIR(I) POSITION X
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           SUBTRACT 1 FROM X.
       SERITLI.
           ADD 1 TO X.
           DISPLAY MENU-DESC(I) LINE MENU-SATIR(I) POSITION X
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
           SUBTRACT 1 FROM X.
       STOP-PROGRAM.
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
