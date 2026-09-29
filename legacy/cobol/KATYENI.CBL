       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'KATYENI'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
	   copy 'stok.cpy'.
       copy 'katlog.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       copy 'katlog.cpz'.
	   WORKING-STORAGE SECTION.
       01 E-KAYIT.
           02 E-KEY.
               03 E-KATNO              PIC 9999.
               03 E-STNO               PIC X(15).
           02 E-PRCNO                  PIC X(30).
           02 E-OEMNO                  PIC X(30).
           02 E-TIPI                   PIC X(30).
           02 E-CINSI                  PIC X(60).
           02 E-MARKA                  PIC X(30).
           02 E-MIN                    PIC 9(12)V9999.
           02 E-MAX                    PIC 9(12)V9999.
           02 E-FIYAT                  PIC 9(10)V9999.
           02 E-NOTE                   PIC X(10).
           02 E-DEG.
                 03 E-YIL          PIC 9999.
                 03 E-AY           PIC 99.
                 03 E-GUN          PIC 99.
           02 E-PAKET                  PIC 9(5).
       01 EKRAN-DEGIS.
           02 S-GIR             PIC 9(10)V9999.
           02 S-CIK             PIC 9(10)V9999.
           02 S-ADET            PIC S9(10)V9999.
           02 Z-FORMAT          PIC Z.ZZZ.ZZZ.ZZZ,ZZZZ.
           02 ZS-FORMAT         PIC -.---.---.---,----.
           02 FLAG              PIC 9.
           02 CIZ                   PIC 9999.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 20.
           02 SON                   PIC 9999 VALUE 31.
           02 E-ISK                 PIC 9(4)V9999.
           02 E-KDV                 PIC 9(4)V9999.
           02 E-FARK                PIC S9(4)V99.
           02 T-FARK                PIC S9(4)V99.
           02 Z-VAL                 PIC ZZ.ZZ.
           02 Z-PAKET               PIC Z,ZZZ.
           
       01 BILGI.
           02 B-KATNO                 PIC X(10).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-STNO                  PIC X(15).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-PRCNO                 PIC X(12).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-TIPI                  PIC X(11).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-CINSI                 PIC X(25).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ISK                   PIC ZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-KDV                   PIC ZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-FIYAT                 PIC ZZ.ZZZ.ZZZ,ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-FARK                  PIC -----,--.
       01 TST-KAYIT.
           02 TST-KEY.
               03 TST-KATNO              PIC 9999.
               03 TST-STNO               PIC X(15).
           02 TST-PRCNO                  PIC X(30).
           02 TST-OEMNO                  PIC X(30).
           02 TST-TIPI                   PIC X(30).
           02 TST-CINSI                  PIC X(60).
           02 TST-MARKA                  PIC X(30).
           02 TST-ADET                    PIC S9(12)V9999.
           02 TST-FIYAT                  PIC 9(10)V9999.
       copy 'onayvar.cpy'.
       copy 'status.cpy'.    
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       01 SEC               PIC 9.
       01 G-KATNO           PIC 9999.
       01 G-STNO            PIC X(15).
       01 MAX-YIL           PIC 9999.
       SCREEN SECTION.
       01 SCR.
           02 LINE 6 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 7 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(114) FROM ' STOK DšZENLE' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 8 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(114) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 9 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º STOK NO    [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(53) FROM ALL ' '.
           02 'MINIMUM [                  ]    º' HIGHLIGHT.
           02 LINE 10 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º PAR€A NO   [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(38) FROM ALL ' '. 
           02 'MAXIMUM [                  ]    º' HIGHLIGHT.
           02 LINE 11 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º OEM NO     [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(38) FROM ALL ' '.
           02 'PAKET A.[     ]' HIGHLIGHT.
           02 PIC X(17) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 12 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º T˜P˜       [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(38) FROM ALL ' '.
           02 'D.TAR˜H [  /  /    ]' HIGHLIGHT.
           02 PIC X(12) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 13 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º STOK C˜NS˜ [' HIGHLIGHT. 02 PIC X(60) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(40) FROM ALL ' '. 02 'º' HIGHLIGHT.
           02 LINE 14 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º MARKASI    [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(38) FROM ALL ' '. 
           02 'G˜R˜LEN [                  ]    º' HIGHLIGHT.
           02 LINE 15 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º NOT        [' HIGHLIGHT. 02 PIC X(10) FROM ALL ' '.
           02 ']' HIGHLIGHT.
           02 PIC X(58) FROM ALL ' '. 
           02 '€IKILAN [                  ]    º' HIGHLIGHT.
           02 LINE 16 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º F˜YATI     [' HIGHLIGHT. 
           02 PIC X(18) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 PIC X(50) FROM ALL ' '. 
           02 'ADET    [                  ]    º' HIGHLIGHT.
           02 LINE 17 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(25) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(8) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 18 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(10) FROM 'KAT.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(15) FROM 'STOK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(12) FROM 'PAR€A NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(11) FROM 'T˜P˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(25) FROM 'C˜NS˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM '%˜.' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(5) FROM '%K.' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(15) FROM 'F˜YAT' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(8) FROM ' %FARK' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 19 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(25) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(8) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           
           02 LINE 32 COLUMN 2 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(11) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(25) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(5) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(8) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
           02 SCR-TARIH.
               03  LINE 12 COLUMN 94 BACKGROUND BLACK FOREGROUND WHITE
                PIC ZZ USING E-GUN HIGHLIGHT.
               03  LINE 12 COLUMN 97 BACKGROUND BLACK FOREGROUND WHITE
                PIC ZZ USING E-AY HIGHLIGHT.
               03  LINE 12 COLUMN 100 BACKGROUND BLACK FOREGROUND WHITE
                PIC ZZZZ USING E-YIL HIGHLIGHT.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING SEC G-KATNO G-STNO MAX-YIL.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY SPACES LINE 37 POSITION 1 SIZE 120
                        CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           PERFORM VARYING CIZ FROM ILK BY 1 UNTIL CIZ > SON
           DISPLAY 'º' LINE CIZ POSITION 2
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                SPACES SIZE 10 'º' SPACES SIZE 15 'º' 
                SPACES SIZE 12 'º' SPACES SIZE 11 'º'
                SPACES SIZE 25 'º' SPACES SIZE 5 'º'
                SPACES SIZE 5 'º' SPACES SIZE 15 'º'
                SPACES SIZE 8 'º'
           END-PERFORM.
           DISPLAY SCR.
           CALL 'GOLGE' USING 0006 0002 0032 0117.
           CANCEL 'GOLGE'.
           MOVE G-KATNO TO E-KATNO.
           OPEN INPUT STOK STOKBAG KATLOG.
           IF SEC = 1 MOVE G-KATNO TO E-KATNO
                      MOVE G-STNO TO E-STNO
                DISPLAY E-STNO LINE 9 POSITION 16
                        CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'
                    GO STNO-KONTROL.
       STNO-AL.
           ACCEPT E-STNO LINE 9 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE STOK STOKBAG KATLOG GO STOP-PROGRAM.
           IF E-STNO = SPACES GO STNO-AL.
       STNO-KONTROL.
           MOVE E-KATNO TO ST-KATNO.
           MOVE E-STNO TO ST-STNO.
           READ STOK INVALID KEY INITIALIZE E-KAYIT
               MOVE G-KATNO TO E-KATNO MOVE ST-STNO TO E-STNO
               MOVE 1 TO FLAG
               GO PRCNO-AL.
           MOVE 2 TO FLAG.
           MOVE ST-KAYIT TO E-KAYIT.
           DISPLAY E-PRCNO LINE 10 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                   E-OEMNO LINE 11 POSITION 16
                   E-TIPI LINE 12 POSITION 16
                   E-CINSI LINE 13 POSITION 16
                   E-MARKA LINE 14 POSITION 16
                   E-NOTE LINE 15 POSITION 16.
           MOVE E-FIYAT TO Z-FORMAT.       
           DISPLAY Z-FORMAT LINE 16 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           MOVE E-MIN TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 9 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           MOVE E-MAX TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 10 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           MOVE S-GIR TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 14 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           MOVE S-CIK TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 15 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           COMPUTE S-ADET = S-GIR - S-CIK.
           MOVE S-ADET TO ZS-FORMAT.
           DISPLAY ZS-FORMAT LINE 16 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           MOVE E-PAKET TO Z-PAKET.
           DISPLAY Z-PAKET LINE 11 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=BROWN'.
           DISPLAY SCR-TARIH.
           PERFORM DETAY-AL THRU A-LISTE-SON.
       PRCNO-AL.
           ACCEPT E-PRCNO LINE 10 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF (F = 52 OR F = 27) AND SEC = 1
               CLOSE STOK STOKBAG KATLOG GO STOP-PROGRAM.
           IF (F = 52 OR F = 27) AND SEC = 0
               CLOSE STOK STOKBAG KATLOG GO STOP-PROGRAM.
           IF F = 53 GO OEMNO-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       OEMNO-AL.
           ACCEPT E-OEMNO LINE 11 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO PRCNO-AL.
           IF F = 53 GO TIPI-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       TIPI-AL.
           ACCEPT E-TIPI LINE 12 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO OEMNO-AL.
           IF F = 53 GO CINSI-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       CINSI-AL.
           ACCEPT E-CINSI LINE 13 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO TIPI-AL.
           IF F = 53 GO MARKA-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       MARKA-AL.
           ACCEPT E-MARKA LINE 14 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO CINSI-AL.
           IF F = 53 GO FIYAT-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       NOTE-AL.
           ACCEPT E-NOTE LINE 15 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO MARKA-AL.
           IF F = 53 GO FIYAT-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       FIYAT-AL.
           ACCEPT E-FIYAT LINE 16 POSITION 16 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE E-FIYAT TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 16 POSITION 16
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           IF F = 52 OR F = 27 GO MARKA-AL.
           IF F = 53 GO MIN-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       MIN-AL.
           ACCEPT E-MIN LINE 9 POSITION 94 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE E-MIN TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 9 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           IF F = 52 OR F = 27 GO FIYAT-AL.
           IF F = 53 GO MIN-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       MAX-AL.
           ACCEPT E-MAX LINE 10 POSITION 94 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE E-MAX TO Z-FORMAT.
           DISPLAY Z-FORMAT LINE 10 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           IF F = 52 OR F = 27 GO MIN-AL.
           IF F = 53 GO MIN-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       PAKET-AL.
           ACCEPT E-PAKET LINE 11 POSITION 94 UPDATE NO BEEP
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY E-PAKET LINE 11 POSITION 94
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           IF F = 52 OR F = 27 GO MAX-AL.
           IF F = 53 GO MIN-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
       DEG-TARIH.
           DISPLAY SCR-TARIH.
           ACCEPT SCR-TARIH.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 52 OR F = 27 GO PAKET-AL.
           IF F = 58 GO DETAY-AL.
           IF F = 10 AND FLAG = 2 MOVE 3 TO FLAG GO STOK-ISLEM.
           
       STOK-ISLEM.    
      *     1 insert, 2 update, 3 delete
           IF FLAG = 3 
                MOVE 'STOK S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE
                MOVE 'H' TO E-ONAY
                PERFORM ONAY-AL THRU ONAY-AL-SON
                IF E-ONAY = 'H' OR E-ONAY = 'h' GO PRCNO-AL.
           
      
           MOVE SPACES TO ONAY-MESSAGE.
           CALL 'STYAZ' USING ONAY-MESSAGE FLAG E-KAYIT.
           CANCEL 'STYAZ'.
           IF ONAY-MESSAGE NOT = SPACES
              PERFORM WARN-MESSAGE.
           
           IF FLAG = 1 AND ONAY-MESSAGE NOT = SPACES
                    GO STNO-AL.
             
           IF FLAG = 3 CLOSE STOK STOKBAG KATLOG GO STOP-PROGRAM.
           
       DETAY-AL.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F3 ' LINE 37 POSITION 1 'SATIR S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           MOVE G-STNO TO SB-STNO.
           MOVE G-KATNO TO SB-KATNO.
		   MOVE 1 TO SB-SIRA.
           START STOKBAG KEY IS NOT LESS THAN SB-KEY
             INVALID KEY GO A-LISTE-SON.
           READ STOKBAG NEXT.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS GO A-LISTE-SON.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ STOKBAG NEXT AT END 
                READ STOKBAG PREVIOUS GO A-LISTE-SON.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS           
		        READ STOKBAG PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY 'º' LINE CIZ POSITION 2
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                SPACES SIZE 10 'º' SPACES SIZE 15 'º' 
                SPACES SIZE 12 'º' SPACES SIZE 11 'º'
                SPACES SIZE 25 'º' SPACES SIZE 5 'º'
                SPACES SIZE 5 'º' SPACES SIZE 15 'º'
                SPACES SIZE 8 'º'
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-STNO TO SB-STNO.
           MOVE G-KATNO TO SB-KATNO.
		   MOVE 1 TO SB-SIRA.
           START STOKBAG KEY IS NOT LESS THAN SB-KEY
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ STOKBAG NEXT.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS 
                 PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 MOVE SPACES TO E-STNO
               MOVE ZEROS TO E-ISK E-KDV
           ELSE 
               MOVE SB-BAG-STNO TO E-STNO
               MOVE SB-KDV TO E-KDV
               MOVE SB-ISK TO E-ISK
           END-IF.
           ACCEPT E-STNO LINE SATIR POSITION 14 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY E-STNO LINE SATIR POSITION 14
                CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           IF F = 27 OR F = 58
             IF YENI = 1 PERFORM YENI-SERITSIZ
              ELSE PERFORM SERITSIZ
             END-IF
             IF SEC = 1 MOVE G-KATNO TO E-KATNO MOVE G-STNO TO E-STNO
                        GO STNO-KONTROL 
             END-IF
             GO STNO-AL.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO YENI-STOKBAG.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 3 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       copy 'onayper.cpy'.
       YENI-STOKBAG.
           INITIALIZE TST-KAYIT.
           MOVE E-STNO TO TST-CINSI.
           MOVE G-KATNO TO TST-KATNO.
           CALL 'STOKSEC' USING TST-KAYIT MAX-YIL BY CONTENT 0017 1.
           CANCEL 'STOKSEC'.
           IF TST-STNO = SPACES GO TUS.
           IF TST-KATNO = G-KATNO GO TUS.
           MOVE TST-KATNO TO SB-BAG-KATNO.
           MOVE TST-STNO TO SB-BAG-STNO.
           MOVE TST-KATNO TO KT-KATNO.
           READ KATLOG INVALID KEY MOVE SPACES TO KT-ACIKLAMA.
           DISPLAY KT-ACIKLAMA LINE SATIR POSITION 3 SIZE 10
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW 'º' LOW
              TST-STNO SIZE 15 LOW 'º' LOW TST-PRCNO SIZE 12 LOW 'º' LOW
              TST-TIPI SIZE 11 LOW 'º' LOW TST-CINSI SIZE 25 LOW.
       ISKONTO-AL.
           MOVE E-ISK TO Z-VAL.
           ACCEPT Z-VAL LINE SATIR POSITION 81 UPDATE NO BEEP PROMPT '.'
            CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE Z-VAL TO E-ISK.
           DISPLAY Z-VAL LINE SATIR POSITION 81
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           IF F = 27 OR F = 52 GO TUS.
       ISKONTO-AL.
           MOVE E-KDV TO Z-VAL.
           ACCEPT Z-VAL LINE SATIR POSITION 87 UPDATE NO BEEP PROMPT '.'
            CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE Z-VAL TO E-KDV.
           DISPLAY Z-VAL LINE SATIR POSITION 87
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           IF F = 27 OR F = 52 GO TUS.
           
           CLOSE STOKBAG. OPEN I-O STOKBAG.
           IF YENI = 1
               MOVE E-ISK TO SB-ISK
               MOVE E-KDV TO SB-KDV
               MOVE G-STNO TO SB-STNO
               MOVE G-KATNO TO SB-KATNO
               MOVE 1 TO SB-SIRA
               PERFORM YENI-YAZ
           ELSE
               MOVE E-ISK TO SB-ISK
               MOVE E-KDV TO SB-KDV
               MOVE G-STNO TO SB-STNO
               MOVE G-KATNO TO SB-KATNO
               PERFORM REWRITE-STOKBAG
           END-IF.
           CLOSE STOKBAG. OPEN INPUT STOKBAG.
           READ STOKBAG.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-STOKBAG.
		   IF WVAR = 0 ADD 1 TO SB-SIRA GO YENI-YAZ.
           
       YENI-STOKBAG-SON. 
        
       DELETE-KOD.
           MOVE 'SATIR S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CLOSE STOKBAG. OPEN I-O STOKBAG.
           MOVE G-STNO TO SB-STNO.
           MOVE G-KATNO TO SB-KATNO.
           READ STOKBAG.
           PERFORM DELETE-STOKBAG.
           CLOSE STOKBAG. OPEN INPUT STOKBAG.
           GO DETAY-AL.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE ST-STNO TO SB-STNO.
           MOVE ST-KATNO TO SB-KATNO.
		   MOVE 1 TO SB-SIRA.
           START STOKBAG KEY IS NOT LESS THAN SB-KEY.
           READ STOKBAG NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE ST-STNO TO SB-STNO.
           MOVE ST-KATNO TO SB-KATNO.
		   MOVE 9999999999 TO SB-SIRA.
           START STOKBAG KEY IS NOT GREATER THAN SB-KEY.
           READ STOKBAG NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ STOKBAG NEXT
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
               READ STOKBAG PREVIOUS
             END-PERFORM.
           READ STOKBAG PREVIOUS AT END
              READ STOKBAG NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS 
		      READ STOKBAG NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ STOKBAG PREVIOUS AT END 
                    READ STOKBAG NEXT 
                    GO PG-OKU-SON.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS  READ STOKBAG NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ STOKBAG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ STOKBAG NEXT
           END-PERFORM.
           READ STOKBAG NEXT AT END
              READ STOKBAG PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS 
              READ STOKBAG PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ STOKBAG NEXT AT END 
             READ STOKBAG PREVIOUS MOVE ILK TO SATIR.
		   IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS 
            READ STOKBAG PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ STOKBAG NEXT
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
                  READ STOKBAG PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ STOKBAG PREVIOUS AT END READ STOKBAG NEXT GO TUS.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS READ STOKBAG NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ STOKBAG PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ STOKBAG NEXT AT END 
		       READ STOKBAG PREVIOUS GO ASAGI-YENI-AL.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS 
              READ STOKBAG PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ STOKBAG NEXT
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
                      READ STOKBAG NEXT
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
           READ STOKBAG PREVIOUS AT END READ STOKBAG NEXT 
                GO Y-LISTE-SON.
           IF G-STNO NOT = SB-STNO OR G-KATNO NOT = SB-KATNO
              OR SB-SIRA = ZEROS READ STOKBAG NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SB-BAG-KATNO TO ST-KATNO KT-KATNO.
           MOVE SB-BAG-STNO TO ST-STNO.
           READ STOK INVALID KEY
              MOVE SPACES TO ST-STNO ST-PRCNO
                ST-TIPI ST-CINSI
              MOVE ZEROS TO ST-FIYAT.
           READ KATLOG INVALID KEY MOVE SPACES TO B-KATNO.
           MOVE KT-ACIKLAMA TO B-KATNO.
           MOVE SB-BAG-STNO TO B-STNO.
           MOVE ST-PRCNO TO B-PRCNO.
           MOVE ST-TIPI TO B-TIPI.
           MOVE ST-CINSI TO B-CINSI.
           MOVE SB-ISK TO B-ISK.
           MOVE SB-KDV TO B-KDV.
           MOVE ST-FIYAT TO B-FIYAT.
           IF ST-FIYAT NOT = ZEROS
           COMPUTE E-FARK = 
               (E-FIYAT -
               (ST-FIYAT - ST-FIYAT * SB-ISK / 100)) * 100
               / (ST-FIYAT - ST-FIYAT * SB-ISK / 100)
           ELSE 
              MOVE ZEROS TO E-FARK
           END-IF.
           MOVE E-FARK TO B-FARK.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 3
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 3
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       YENI-SERITLI.
           DISPLAY SPACES LINE SATIR POSITION 3 SIZE 10
                 CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK'
                'º' SPACES SIZE 15 'º' SPACES SIZE 12 
                'º' SPACES SIZE 11 'º' SPACES SIZE 25 
                'º' SPACES SIZE 5  'º' SPACES SIZE 5 
                'º' SPACES SIZE 15 'º' SPACES SIZE 8.
       YENI-SERITSIZ.
           DISPLAY SPACES LINE SATIR POSITION 3 SIZE 10
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                'º' SPACES SIZE 15 'º' SPACES SIZE 12 
                'º' SPACES SIZE 11 'º' SPACES SIZE 25 
                'º' SPACES SIZE 5  'º' SPACES SIZE 5 
                'º' SPACES SIZE 15 'º' SPACES SIZE 8.
       
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKBAG==
                                  "%T%"  BY "STOKBAG"
                                 ==%R%== BY ==SB-KAYIT==.
              

