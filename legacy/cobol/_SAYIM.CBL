       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'DEPO'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
            DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'sayim.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'sayim.cpz'.    
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 22.
           02 T-TUTAR               PIC 9(13)V99.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
              
       01 E-KAYIT.
           02 E-KEY.
               03 E-YIL                PIC 9999 VALUE 2015.
               03 E-KATNO              PIC 9999.
               03 E-STNO               PIC X(15).
               03 E-SIRA               PIC 9(10).
           02 E-DEPONO             PIC 9999.    
           02 E-PRCNO              PIC X(30).
           02 E-OEMNO              PIC X(30).
           02 E-TIPI               PIC X(30).
           02 E-CINSI              PIC X(60).
           02 E-MARKA              PIC X(30).
           02 E-FIYAT              PIC 9(15)V9999.
           02 E-ADET               PIC 9(10)V9999.
           02 E-KDV                PIC 9(5)V9999.
           02 E-RAF                PIC X(15).
       01 BILGI. 
           02 B-KATNO                 PIC ZZZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-STNO                  PIC X(6).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-DEPONO                PIC X(5).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-PRCNO                 PIC X(8).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-OEMNO                 PIC X(8).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-TIPI                  PIC X(8).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-CINSI                 PIC X(15).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-MARKA                 PIC X(8).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-RAF                   PIC X(7).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-ADET                  PIC Z.ZZZ.ZZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-KDV                   PIC ZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-FIYAT                 PIC Z.ZZZ.ZZZ,ZZ.
           02 FILLER                  PIC X VALUE 'º'.
           02 B-TUTAR                 PIC Z.ZZZ.ZZZ,ZZ.
           
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       SCREEN SECTION.
       01 SCR.
           02 LINE 5 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(4) FROM 'D.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(30) FROM 'DEPO ADI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(10) FROM 'K%' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 7 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 23 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(4) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(10) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
             
       PROCEDURE DIVISION.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING SATIR FROM ILK BY 1 UNTIL SATIR > SON
                PERFORM YENI-SERITSIZ
           END-PERFORM.
           DISPLAY SCR.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT SAYIM.
		   MOVE E-YIL TO SY-YIL.
           MOVE ZEROS TO SY-KATNO SY-SIRA.
           MOVE SPACES TO SY-STNO.
           START SAYIM KEY IS NOT LESS THAN SY-KEY
             INVALID KEY GO A-LISTE-SON.
           READ SAYIM NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ SAYIM NEXT AT END 
					READ SAYIM PREVIOUS GO A-LISTE-SON.
		   IF SY-YIL NOT = E-YIL
		            READ SAYIM PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING SATIR FROM SATIR BY 1 UNTIL SATIR > SON
                 PERFORM SERITSIZ
             END-PERFORM MOVE LIS TO SATIR SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE E-YIL TO SY-YIL.
           MOVE ZEROS TO SY-KATNO SY-SIRA.
           MOVE SPACES TO SY-STNO.
           START SAYIM KEY IS NOT LESS THAN SY-KEY
             INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ SAYIM NEXT.
           IF SY-YIL NOT = E-YIL 
              PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 INITIALIZE E-KAYIT
               ELSE MOVE SY-KAYIT TO E-KAYIT
           END-IF.
           ACCEPT E-STNO LINE SATIR POSITION 7 UPDATE NO BEEP
            CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 CLOSE SAYIM GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO YENI-SAYIM.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-SAYIM.
           CLOSE SAYIM. OPEN INPUT SAYIM.
           READ SAYIM.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-YAZ.
	       PERFORM WRITE-SAYIM.
		   IF WVAR = 0 ADD 1 TO SY-SIRA GO YENI-YAZ.
           
       YENI-SAYIM-SON. 
            
        
       DELETE-KOD.
           CLOSE SAYIM. OPEN I-O SAYIM.
           READ SAYIM.
           PERFORM DELETE-SAYIM.
           CLOSE SAYIM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE E-YIL TO SY-YIL.
           MOVE ZEROS TO SY-KATNO SY-SIRA.
           MOVE SPACES TO SY-STNO.
           START SAYIM KEY IS NOT LESS THAN SY-KEY.
           READ SAYIM NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE E-YIL TO SY-YIL.
           MOVE 9999 TO SY-KATNO.
           MOVE 9999999999 TO SY-SIRA.
           MOVE ALL 'ÿ' TO SY-STNO.
           START SAYIM KEY IS NOT GREATER THAN SY-KEY.
           READ SAYIM NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ SAYIM NEXT
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
               READ SAYIM PREVIOUS
             END-PERFORM.
           READ SAYIM PREVIOUS AT END
              READ SAYIM NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   IF SY-YIL NOT = E-YIL
		      READ SAYIM NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ SAYIM PREVIOUS AT END 
                    READ SAYIM NEXT 
                    GO PG-OKU-SON.
		   IF E-YIL NOT = SY-YIL READ SAYIM NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ SAYIM PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ SAYIM NEXT
           END-PERFORM.
           READ SAYIM NEXT AT END
              READ SAYIM PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
		   IF SY-YIL NOT = E-YIL
		      READ SAYIM PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ SAYIM NEXT AT END 
             READ SAYIM PREVIOUS MOVE ILK TO SATIR.
		   IF SY-YIL NOT = E-YIL
		     READ SAYIM PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ SAYIM NEXT
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
                  READ SAYIM PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ SAYIM PREVIOUS AT END READ SAYIM NEXT GO TUS.
		   IF SY-YIL NOT = E-YIL READ SAYIM NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ SAYIM PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ SAYIM NEXT AT END 
		       READ SAYIM PREVIOUS GO ASAGI-YENI-AL.
		   IF SY-YIL NOT = E-YIL 
               READ SAYIM PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ SAYIM NEXT
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
                      READ SAYIM NEXT
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
           READ SAYIM PREVIOUS AT END READ SAYIM NEXT 
                GO Y-LISTE-SON.
		   IF SY-YIL NOT = E-YIL READ SAYIM NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE SY-KATNO TO B-KATNO.
           MOVE SY-STNO TO B-STNO.
           MOVE SY-DEPONO TO T-KOD.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0000 0000 0000 0000 0.
           CANCEL 'DEPSEC'.
           MOVE T-ADI TO B-DEPONO.
           MOVE SY-PRCNO TO B-PRCNO.
           MOVE SY-OEMNO TO B-OEMNO.
           MOVE SY-TIPI TO B-TIPI.
           MOVE SY-CINSI TO B-CINSI.
           MOVE SY-MARKA TO B-MARKA.
           MOVE SY-RAF TO B-RAF.
           MOVE SY-ADET TO B-ADET.
           MOVE SY-FIYAT TO B-FIYAT.
           MOVE SY-KDV TO B-KDV.
           COMPUTE T-TUTAR = SY-ADET * SY-FIYAT.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
           
       YENI-SERITLI.
           DISPLAY SPACES LINE SATIR POSITION 2 SIZE 4
             CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK'
             'º' SPACES SIZE 6 'º' SPACES SIZE 5
             'º' SPACES SIZE 7 'º' SPACES SIZE 6
             'º' SPACES SIZE 8 'º' SPACES SIZE 13
             'º' SPACES SIZE 8 'º' SPACES SIZE 7
             'º' SPACES SIZE 12 'º' SPACES SIZE 12
             'º' SPACES SIZE 12.
       YENI-SERITSIZ.
           DISPLAY SPACES LINE SATIR POSITION 2 SIZE 4
             CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
             'º' SPACES SIZE 6 'º' SPACES SIZE 5
             'º' SPACES SIZE 7 'º' SPACES SIZE 6
             'º' SPACES SIZE 8 'º' SPACES SIZE 13
             'º' SPACES SIZE 8 'º' SPACES SIZE 7
             'º' SPACES SIZE 12 'º' SPACES SIZE 12
             'º' SPACES SIZE 12.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==SAYIM==
                                  "%T%"  BY "SAYIM"
                                 ==%R%== BY ==SY-KAYIT==.
              