       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'FIKOPAR'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'users.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'users.cpz'.
       WORKING-STORAGE SECTION.
       01 SCREEN-VALS.
           02 SATIR         PIC 9999.
           02 T-PASS        PIC X(20).
           02 F             PIC 9999.
       01 TARIH.
           02 YIL               PIC 9999.
           02 AY                PIC 99.
           02 GUN               PIC 99.
       copy 'status.cpy'.
       copy 'onayvar.cpy'.
       copy 'win.cpy'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       SCREEN SECTION.
       01 SCR-TARIH.                
           02 LINE 3 COLUMN 95 BACKGROUND BLACK FOREGROUND WHITE
               'ษออออออออออออออออออป' HIGHLIGHT.
           02 LINE 4 COLUMN 95 BACKGROUND BLACK FOREGROUND WHITE
               'บ                  บ' HIGHLIGHT.
           02 LINE 5 COLUMN 95 BACKGROUND BLACK FOREGROUND WHITE
               'บ    ' HIGHLIGHT.
           02 PIC ZZ USING GUN HIGHLIGHT.  
           02 '/' HIGHLIGHT.
           02 PIC ZZ USING AY HIGHLIGHT.
           02 '/' HIGHLIGHT.
           02 PIC ZZZZ USING YIL HIGHLIGHT.
           02 '    บ' HIGHLIGHT.
           02 LINE 6 COLUMN 95 BACKGROUND BLACK FOREGROUND WHITE
               'บ                  บ' HIGHLIGHT.
           02 LINE 7 COLUMN 95 BACKGROUND BLACK FOREGROUND WHITE
               'ศออออออออออออออออออผ' HIGHLIGHT.
           
       01 SCR-USER.
           02  LINE 15 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'ษอออออออออออออออออออออออออออออออออออออออป' HIGHLIGHT.
           02  LINE 16 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'บ                                       บ' HIGHLIGHT.
           02  LINE 17 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'บ Kullanc Ad [                    ]  บ' HIGHLIGHT.
           02  LINE 18 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'บ ifre         [                    ]  บ' HIGHLIGHT.
           02  LINE 19 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'บ                                       บ' HIGHLIGHT.
           02  LINE 20 COLUMN 40 BACKGROUND BLACK FOREGROUND WHITE
              'ศอออออออออออออออออออออออออออออออออออออออผ' HIGHLIGHT.
           02  LINE 17 COLUMN 57 BACKGROUND BLACK FOREGROUND WHITE
                PIC X(20) USING US-NAME HIGHLIGHT.
           02  LINE 18 COLUMN 57 BACKGROUND BLACK FOREGROUND WHITE
                PIC X(20) USING T-PASS HIGHLIGHT SECURE.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           CALL 'FCONTROL'.
           CANCEL 'FCONTROL'.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           ACCEPT TARIH FROM CENTURY-DATE.
           DISPLAY SPACES ERASE CONTROL 'BCOLOR=BLUE'.
           PERFORM VARYING SATIR FROM 1 BY 22 UNTIL SATIR > 4100
               DISPLAY './ FiKoPaR EnTeGrE \._' LINE 2 POSITION SATIR
               CONTROL 'BCOLOR=BLUE, FCOLOR=BLUE' 
           END-PERFORM.
           DISPLAY ALL 'อ' LINE 1 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           DISPLAY ALL 'อ' LINE 36 POSITION 1 SIZE 120
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           
           DISPLAY SCR-TARIH.
       
           CALL 'GOLGE' USING 0003 0095 0007 0114.
           CANCEL 'GOLGE'.
       
           DISPLAY ' ESC ' LINE 37 POSITION 108
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                '€IKI' REVERSE.
           DISPLAY ' FKOPAR ENTEGRE V.2015 '
               LINE 1 POSITION 3 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
             
           GO MENU-CAGIR.
       USER-AL.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           CALL 'GOLGE' USING 0015 0040 0020 0080.
           CANCEL 'GOLGE'.
           OPEN INPUT USERS.
       SOR1.
           MOVE SPACES TO T-PASS.
           DISPLAY SCR-USER.
           ACCEPT SCR-USER.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE USERS PERFORM SOR-KALDIR GO STOP-PROGRAM.
           READ USERS INVALID KEY GO SOR1.
           IF T-PASS NOT EQUAL TO US-PASS GO SOR1.
           IF US-ACTIVE = 0 GO SOR1.
           CLOSE USERS.
       SOR-KALDIR.
           MOVE WCB TO NESTED-WCB (2).
           DISPLAY NESTED-WCB (2)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
       MENU-CAGIR.
           MOVE 21 TO MAIN-ARGUMENT-SIZE.
           MOVE 'MENU.CFG;0006;0003;' TO MAIN-ARGUMENT-VALUE.
           CALL 'MENU' USING BY CONTENT MAIN-ARGUMENT.
           CANCEL 'MENU'.
           MOVE 'PROGRAMDAN €IKILACAK EMN MSNZ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'E' OR E-ONAY = 'e' GO STOP-PROGRAM.
           GO MENU-CAGIR.
       copy 'onayper.cpy'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
             LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
