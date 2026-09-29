       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'AYAR'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'AYAR.cpy'.
       copy 'depo.cpy'.
       copy 'tip.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'AYAR.cpz'.
       copy 'depo.cpz'.
       copy 'tip.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 SATIR                 PIC 9999.
           02 LIS                   PIC 99.
           02 F                     PIC 99.
           02 DUR                   PIC X.
           02 YENI                  PIC 9.
           02 ILK                   PIC 9999 VALUE 8.
           02 SON                   PIC 9999 VALUE 22.
           02 YSON                  PIC 9999.
           
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.

           02 E-TIP                 PIC 99.
           02 E-DEPONO              PIC 9999.
           02 E-ACIKLAMA            PIC X(60).
           02 E-EVRAKNO             PIC X(15).
           02 E-EVRAKFORMAT         PIC X(30).
           02 E-YAZDOS              PIC X(12).
           02 E-YAZPORT             PIC X(12).
       01 BILGI.
           02 B-ACIKLAMA              PIC X(20).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-TIP                   PIC X(14).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-DEPONO                PIC X(7).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-EVRAKNO               PIC X(15).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-EVRAKFORMAT           PIC X(30).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-YAZDOS                PIC X(12).
           02 FILLER                  PIC X VALUE 'º'.
           02 B-YAZPORT               PIC X(12).
           
       copy 'status.cpy'.
       copy 'win.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       77 G-TIP             PIC 99.
       SCREEN SECTION.
       01 SCR.
           02 LINE 5 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Ë' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 6 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'º' HIGHLIGHT. 02 PIC X(20) FROM ' A€IKLAMA' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(14) FROM ' EVRAK T˜P˜' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(7) FROM ' DEPO ' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(15) FROM ' EVRAK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 PIC X(30) FROM ' EVRAK FORMAT' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(12) FROM ' FOR.DOSYA' HIGHLIGHT.
           02 'º' HIGHLIGHT. 02 PIC X(12) FROM ' PORT ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 7 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Î' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 23 COLUMN 1 BACKGROUND BLACK FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(20) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(14) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(7) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(15) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(30) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 'Ê' HIGHLIGHT. 02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT.
       
       PROCEDURE DIVISION USING G-TIP.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING SATIR FROM ILK BY 1 UNTIL SATIR > SON
                PERFORM YENI-SERITSIZ
           END-PERFORM.
           DISPLAY SCR.
           CALL 'GOLGE' USING BY CONTENT 0005 0001 0023 0118.
           CANCEL 'GOLGE'.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120
               ' F10 ' LINE 37 POSITION 1 'S˜L' REVERSE LOW.
           MOVE ILK TO SATIR.
           OPEN INPUT AYAR DTIP DEPO.
		   MOVE G-TIP TO AY-TIP.
           START AYAR KEY IS NOT LESS THAN AY-TIP
             INVALID KEY GO A-LISTE-SON.
           READ AYAR NEXT.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ AYAR NEXT AT END 
					READ AYAR PREVIOUS GO A-LISTE-SON.
		   ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING SATIR FROM SATIR BY 1 UNTIL SATIR > SON
                 PERFORM YENI-SERITSIZ
             END-PERFORM MOVE LIS TO SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE G-TIP TO AY-TIP.
           START AYAR KEY IS NOT LESS THAN AY-TIP
                INVALID KEY PERFORM YENI-SERITLI MOVE 1 TO YENI GO TUS.
           READ AYAR NEXT.
           PERFORM AKTAR.
       TUS.
           IF YENI = 1 
              MOVE ZEROS TO E-TIP E-DEPONO 
              MOVE SPACES TO E-ACIKLAMA E-EVRAKNO E-EVRAKFORMAT
              MOVE SPACES TO E-YAZDOS E-YAZPORT
           ELSE 
              MOVE AY-ACIKLAMA TO E-ACIKLAMA
              MOVE AY-DEPONO TO E-DEPONO
              MOVE AY-TIP TO E-TIP
              MOVE AY-EVRAKNO TO E-EVRAKNO
              MOVE AY-EVRAKFORMAT TO E-EVRAKFORMAT
              MOVE AY-YAZDOS TO E-YAZDOS
              MOVE AY-YAZPORT TO E-YAZPORT
           END-IF.
       ACIKLAMA-AL.
           ACCEPT E-ACIKLAMA LINE SATIR POSITION 2 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZEN-GOSTER.
           IF F = 27 CLOSE AYAR DTIP DEPO GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 GO YENI-AYAR.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 10 AND YENI = 0 GO DELETE-KOD.
           GO TUS.
       YENI-AYAR.
       TIP-AL.
           MOVE E-TIP TO T-KOD.
           COMPUTE YSON = SATIR + 10.
           CALL 'TIPSEC' USING 0 T-KOD T-ADI
                  BY CONTENT 0023 SATIR 0055 YSON.
           CANCEL 'TIPSEC'.
           IF T-KOD = -1 MOVE ZEROS TO E-TIP
                ELSE MOVE T-KOD TO E-TIP.
           PERFORM DUZEN-GOSTER.
           IF T-KOD = -1 GO ACIKLAMA-AL.
           
       DEPO-AL.
           MOVE E-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           COMPUTE YSON = SATIR + 5.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0038 SATIR 0047 YSON 0.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 MOVE ZEROS TO E-DEPONO
                ELSE MOVE T-KOD TO E-DEPONO.
           PERFORM DUZEN-GOSTER.
           IF T-KOD = -1 GO TIP-AL.
           
       EVRAKNO-AL.
           ACCEPT E-EVRAKNO LINE SATIR POSITION 46 UPDATE NO BEEP
            PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZEN-GOSTER.
           IF F = 52 OR F = 27 GO DEPO-AL.
       EVRAKFORMAT-AL.
           ACCEPT E-EVRAKFORMAT LINE SATIR POSITION 62 UPDATE NO BEEP
            PROMPT '.'  CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZEN-GOSTER.
           IF F = 52 OR F = 27 GO EVRAKNO-AL.
       YAZDOS-AL.
           ACCEPT E-YAZDOS LINE SATIR POSITION 93 UPDATE NO BEEP
             PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZEN-GOSTER.
           IF F = 52 OR F = 27 GO EVRAKFORMAT-AL.
       YAZPORT-AL.
           ACCEPT E-YAZPORT LINE SATIR POSITION 106 UPDATE NO BEEP
             PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZEN-GOSTER.
           IF F = 52 OR F = 27 GO YAZDOS-AL.
           
           MOVE E-TIP TO AY-TIP.
           MOVE E-DEPONO TO AY-DEPONO.
           
           READ AYAR INVALID KEY CONTINUE
            NOT INVALID KEY IF YENI = 0 CONTINUE 
                ELSE GO YAZPORT-AL END-IF
           END-READ.
           
           MOVE E-ACIKLAMA TO AY-ACIKLAMA.
           MOVE E-TIP TO AY-TIP.
           MOVE E-DEPONO TO AY-DEPONO.
           MOVE E-EVRAKNO TO AY-EVRAKNO.
           MOVE E-EVRAKFORMAT TO AY-EVRAKFORMAT.
           MOVE E-YAZDOS TO AY-YAZDOS.
           MOVE E-YAZPORT TO AY-YAZPORT.
           
           CLOSE AYAR. OPEN I-O AYAR.
           IF YENI = 1
               PERFORM WRITE-AYAR
           ELSE
               PERFORM REWRITE-AYAR
           END-IF.
           CLOSE AYAR. OPEN INPUT AYAR.
           READ AYAR.
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       YENI-AYAR-SON. 
       

       DUZEN-GOSTER.
           MOVE E-DEPONO TO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           IF DP-NO = ZEROS MOVE SPACES TO DP-ADI.
           MOVE E-TIP TO DTIP-NO.
           READ DTIP INVALID KEY MOVE SPACES TO DTIP-ADI.
           DISPLAY E-ACIKLAMA  LINE SATIR POSITION 2 SIZE 20
                   CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK'
                'º' DTIP-ADI SIZE 14 'º' 
                DP-ADI SIZE 7 'º' E-EVRAKNO SIZE 15 'º'
                E-EVRAKFORMAT SIZE 30 'º' E-YAZDOS SIZE 12 'º'
                E-YAZPORT SIZE 12.
       
        
       DELETE-KOD.
           CLOSE AYAR. OPEN I-O AYAR.
           READ AYAR.
           PERFORM DELETE-AYAR.
           CLOSE AYAR DTIP DEPO.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           GO START-PROGRAM.
           
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI.
           MOVE 1 TO AY-TIP.
           START AYAR KEY IS NOT LESS THAN AY-TIP.
           READ AYAR NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE 99 TO AY-TIP.
           START AYAR KEY IS NOT GREATER THAN AY-TIP.
           READ AYAR NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ AYAR NEXT
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
               READ AYAR PREVIOUS
             END-PERFORM.
           READ AYAR PREVIOUS AT END
              READ AYAR NEXT 
              MOVE ILK TO SATIR
              PERFORM AKTAR PERFORM SERITLI
              GO TUS.
		   MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ AYAR PREVIOUS AT END 
                    READ AYAR NEXT 
                    GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ AYAR PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ AYAR NEXT
           END-PERFORM.
           READ AYAR NEXT AT END
              READ AYAR PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ AYAR NEXT AT END 
             READ AYAR PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ AYAR NEXT
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
                  READ AYAR PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI PERFORM SERITLI GO TUS.
           READ AYAR PREVIOUS AT END READ AYAR NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ AYAR PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ AYAR NEXT AT END 
		       READ AYAR PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ AYAR NEXT
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
                      READ AYAR NEXT
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
           READ AYAR PREVIOUS AT END READ AYAR NEXT 
                GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE AY-TIP TO DTIP-NO.
           READ DTIP INVALID KEY MOVE SPACES TO DTIP-ADI.
           MOVE DTIP-ADI TO B-TIP.
           MOVE AY-DEPONO TO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           MOVE DP-ADI TO B-DEPONO.
           MOVE AY-ACIKLAMA TO B-ACIKLAMA.
           MOVE AY-EVRAKNO TO B-EVRAKNO.
           MOVE AY-EVRAKFORMAT TO B-EVRAKFORMAT.
           MOVE AY-YAZDOS TO B-YAZDOS.
           MOVE AY-YAZPORT TO B-YAZPORT.
           
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'.
       YENI-SERITLI.
           DISPLAY SPACES SIZE 20 LINE SATIR POSITION 2
                   CONTROL 'BCOLOR=WHITE, FCOLOR=BLACK'
                'º' SPACES SIZE 14 'º' 
                SPACES SIZE 7 'º' SPACES SIZE 15 'º'
                SPACES SIZE 30 'º' SPACES SIZE 12 'º'
                SPACES SIZE 12.
       YENI-SERITSIZ.
           DISPLAY 'º' LINE SATIR POSITION 1
                   CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                SPACES SIZE 20 'º' SPACES SIZE 14 'º' 
                SPACES SIZE 7 'º' SPACES SIZE 15 'º'
                SPACES SIZE 30 'º' SPACES SIZE 12 'º'
                SPACES SIZE 12 'º'.
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
