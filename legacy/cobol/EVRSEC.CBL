       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'EVRSEC'.
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
       copy 'carhar.cpy'.
       copy 'ayar.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'hareket.cpz'.
       copy 'carhar.cpz'.
       copy 'ayar.cpz'.
       WORKING-STORAGE SECTION.
       01 EKRAN-DEGIS.
           02 F                     PIC 9999.
           02 DUR                   PIC X.
           02 CIZ                   PIC 99.
           02 SATIR                 PIC 99.
           02 LIS                   PIC 99.
           02 IND                   PIC 9(5).
           02 I                     PIC 9(5).
           02 J                     PIC 9(5).
           02 T                     PIC 9(5).
           02 X                     PIC 9999 VALUE 16.
           02 WIDTH                 PIC 9999.
           02 LEN-OF-EVRAK-ARRAY    PIC 9(5).
           02 DP-SIRA               PIC 9(10).
           02 FLAG                  PIC 9.
           
       01 EVRAK-ARRAY.
           02 I-EVRAKNO             PIC X(15) OCCURS 1000 TIMES.
           02 I-TIPI                PIC 99 OCCURS 1000 TIMES.
           02 I-TARIH               PIC X(10) OCCURS 1000 TIMES.
           02 I-FIYAT               PIC 9(12)V99 OCCURS 1000 TIMES. 
       
       01 TEMP-TIP.
           02 T-TIP                 PIC 99.
           02 T-VAL                 PIC X(80).
       
       01 BILGI.
           02 B-EVRAKNO             PIC X(15).
           02 FILLER                PIC X VALUE 'º'.
           02 B-TIPI                PIC X(25).
           02 FILLER                PIC X VALUE 'º'.
           02 B-TARIH               PIC X(10).
           02 FILLER                PIC X VALUE 'º'.
           02 B-FIYAT               PIC ZZZ.ZZZ.ZZZ,ZZ.
       
       copy 'toplam.var'.
       copy 'numara.var'.
       copy 'onayvar.cpy'.
       copy 'status.cpy'.
       copy 'win.cpy'. 
       LINKAGE SECTION.
       77 G-TIP               PIC 99.
       77 G2-TIP              PIC 99.
       77 G-HESAPNO           PIC X(15).
       77 G-EVRAKNO           PIC X(15).
       SCREEN SECTION.
       copy 'onayscr.cpy'.
       PROCEDURE DIVISION USING G-TIP G2-TIP G-HESAPNO G-EVRAKNO.
       BEGIN-PROGRAM.
           MOVE 0 TO I.
           OPEN INPUT HAREKET-SICIL HAREKET-DETAY.
           MOVE G-HESAPNO TO HS-HESAPNO.
           START HAREKET-SICIL KEY IS EQUAL TO HS-HESAPNO
                INVALID KEY GO HAREKET-SON.
       HAREKET-OKU.
           READ HAREKET-SICIL NEXT AT END GO HAREKET-SON.
           IF HS-HESAPNO NOT = G-HESAPNO GO HAREKET-SON.
           IF G2-TIP = ZEROS
               IF HS-TIP NOT = G-TIP GO HAREKET-OKU END-IF
               IF HS-BEVRAKNO(1) NOT = SPACES GO HAREKET-OKU END-IF
           ELSE
               IF HS-TIP NOT = G-TIP AND HS-TIP NOT = G2-TIP 
                        GO HAREKET-OKU
               END-IF
               IF HS-BEVRAKNO(2) NOT = SPACES GO HAREKET-OKU END-IF
           END-IF.
           IF HS-TIP = G-TIP AND G-EVRAKNO = HS-EVRAKNO
                GO HAREKET-OKU.
           IF HS-TIP = G2-TIP AND G-EVRAKNO = HS-EVRAKNO
                GO HAREKET-OKU.
                        
           ADD 1 TO I.
           MOVE HS-EVRAKNO TO I-EVRAKNO(I).
           STRING HS-DGUN '/' HS-DAY '/' HS-DYIL
                DELIMITED BY SIZE INTO I-TARIH(I).
           MOVE HS-TIP TO I-TIPI(I).
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE ZEROS TO HD-SIRA.
           READ HAREKET-DETAY INVALID KEY MOVE ZEROS TO HD-FIYAT.
           MOVE HD-FIYAT TO I-FIYAT(I).
           GO HAREKET-OKU.
       HAREKET-SON.
           CLOSE HAREKET-SICIL HAREKET-DETAY.
           IF I = 0 EXIT PROGRAM STOP RUN.
           MOVE I TO LEN-OF-EVRAK-ARRAY.
       START-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           DISPLAY 'É' LINE 23 POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 15 'Ë' ALL 'Í' SIZE 25 'Ë'
                ALL 'Í' SIZE 10 'Ë' ALL 'Í' SIZE 14 '»'.
           PERFORM VARYING CIZ FROM 24 BY 1 UNTIL CIZ > 34
                DISPLAY 'º' LINE CIZ POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE' 
                   ALL ' ' SIZE 15 'º' ALL ' ' SIZE 25 'º' 
                   ALL ' ' SIZE 10 'º' ALL ' ' SIZE 14 'º'
           END-PERFORM.
           DISPLAY 'È' LINE 35 POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE'
                ALL 'Í' SIZE 15 'Ê' ALL 'Í' SIZE 25 'Ê' 
                ALL 'Í' SIZE 10 'Ê' ALL 'Í' SIZE 14 '¼'.
           
           COMPUTE WIDTH = X + LENGTH OF BILGI + 1.
           CALL 'GOLGE' USING BY CONTENT 0023 X 0035 WIDTH.
           CANCEL 'GOLGE'.
           
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
           
           MOVE 1 TO I T.
           MOVE 24 TO J.
           MOVE 24 TO SATIR.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = J PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF I = LEN-OF-EVRAK-ARRAY OR SATIR = 34 GO A-LISTE-SON.
           ADD 1 TO I SATIR.
           GO A-LISTE.
       A-LISTE-SON.
          
       TUS.
           MOVE ' ' TO DUR.
           ACCEPT DUR LINE 37 POSITION 1 UPDATE NO BEEP
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 MOVE SPACES TO G-EVRAKNO GO STOP-PROGRAM.
           IF F = 52 AND T NOT = 1 
                  IF J > 24 SUBTRACT 1 FROM J END-IF 
                  MOVE 24 TO SATIR
                  SUBTRACT 1 FROM T
                  COMPUTE I = T - (J - 24)
                  GO A-LISTE.
           IF F = 53 AND T NOT = LEN-OF-EVRAK-ARRAY
                  IF J < 34 ADD 1 TO J END-IF 
                  MOVE 24 TO SATIR
                  ADD 1 TO T
                  COMPUTE I = T - (J - 24)
                  GO A-LISTE.
           IF F = 13 GO AKTAR-KONTROL.
           GO TUS.
       AKTAR.
           MOVE I-EVRAKNO(I) TO B-EVRAKNO.
           MOVE I-TARIH(I) TO B-TARIH.
           MOVE I-FIYAT(I) TO B-FIYAT.
           EVALUATE I-TIPI(I)
            WHEN 1 MOVE '˜rs.li SatŸ Fat.' TO B-TIPI
            WHEN 2 MOVE 'SatŸ ˜rsaliyesi' TO B-TIPI
            WHEN 3 MOVE 'SatŸ Faturas' TO B-TIPI
            WHEN 4 MOVE '˜rs.li AlŸ Fat.' TO B-TIPI
            WHEN 5 MOVE 'AlŸ ˜rsaliyesi' TO B-TIPI
            WHEN 6 MOVE 'AlŸ Faturas' TO B-TIPI
            WHEN 7 MOVE '˜rs.liÿ˜ade €kŸÿFat.' TO B-TIPI
            WHEN 8 MOVE '˜ade €kŸÿ˜rs.' TO B-TIPI
            WHEN 9 MOVE '˜ade €kŸÿFat.' TO B-TIPI
            WHEN 10 MOVE '˜rs.ÿ˜ade AlŸ Fat.' TO B-TIPI
            WHEN 11 MOVE '˜ade AlŸÿ˜rs.' TO B-TIPI
            WHEN 12 MOVE '˜ade AlŸÿFat.' TO B-TIPI
           END-EVALUATE.
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
       copy 'onayper.cpy'.
       AKTAR-KONTROL.    
           MOVE '˜€ER˜KLER˜N AKTARILMASINI ˜ST˜YOR MUSUNUZ ?'
                TO ONAY-MESSAGE.
           MOVE 'E' TO E-ONAY.     
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           
           OPEN I-O HAREKET-SICIL HAREKET-DETAY.
                
      *  EVRAKLARI BA¦LA VE  ˜€ER˜KLER˜ AKTAR VE TOPLAMLARINI KAYDET
      *  VE HEPS˜ ˜€˜N EKSTRE KAYDINI DšZENLE
      
           PERFORM EVRAKLARI-BAGLA THRU EVRAKLARI-BAGLA-SON.
           IF E-ONAY = 'e' OR E-ONAY = 'E'
                PERFORM AKTAR-BASLAT THRU AKTAR-SON.
           
           CLOSE HAREKET-SICIL HAREKET-DETAY.           
           GO STOP-PROGRAM.
      
       EVRAKLARI-BAGLA.
      * BA¦ EVRAKLARI TANIMLAMADA SIRA ™NEML˜ SIRASINI DE¦˜žT˜RME !!!    
      * ˜RSAL˜YE BA¦ EVRAK TANIMLA     
      * ™NCE KARžI EVRAK BA¦LA
           MOVE I-TIPI(T) TO HS-TIP.
           MOVE I-EVRAKNO(T) TO HS-EVRAKNO.
           READ HAREKET-SICIL.
           EVALUATE G-TIP ALSO G2-TIP
            WHEN 1 ALSO 3 MOVE 3 TO HS-BAGTIPI(2)
                          MOVE G-EVRAKNO TO HS-BEVRAKNO(2)
            WHEN 2 ALSO 0 MOVE 3 TO HS-BAGTIPI(1)
                          MOVE G-EVRAKNO TO HS-BEVRAKNO(1)
            WHEN 4 ALSO 6  MOVE 6 TO HS-BAGTIPI(2)
                           MOVE G-EVRAKNO TO HS-BEVRAKNO(2)
            WHEN 5 ALSO 0 MOVE 6 TO HS-BAGTIPI(1)
                          MOVE G-EVRAKNO TO HS-BEVRAKNO(1)
            WHEN 7 ALSO 9 MOVE 9 TO HS-BAGTIPI(2)
                          MOVE G-EVRAKNO TO HS-BEVRAKNO(2)
            WHEN 8 ALSO 0 MOVE 9 TO HS-BAGTIPI(1)
                          MOVE G-EVRAKNO TO HS-BEVRAKNO(1)
            WHEN 10 ALSO 12 MOVE 12 TO HS-BAGTIPI(2)
                            MOVE G-EVRAKNO TO HS-BEVRAKNO(2)
            WHEN 11 ALSO 0 MOVE 12 TO HS-BAGTIPI(1)
                           MOVE G-EVRAKNO TO HS-BEVRAKNO(1)
           END-EVALUATE.
           ADD 1 TO HS-SONUC.
           PERFORM REWRITE-HAREKET-SICIL.
      * FATURA BA¦ EVRAK TANIMLA     
      
      * IF F = 5
      *          EVALUATE HS-TIP 
      *  SATIž FATURASI <=> SATIž ˜RSAL˜YES˜
      *          WHEN 3 CALL 'EVRSEC' USING 02 00 T-HESAPNO T-EVRAKNO
      *  ALIž FATURASI <=> ALIž ˜RSAL˜YES˜
      *          WHEN 6 CALL 'EVRSEC' USING 05 00 T-HESAPNO T-EVRAKNO
      *  ˜ADE €IKIž FATURASI <=> ˜ADE €IKIž ˜RSAL˜YES˜
      *          WHEN 9 CALL 'EVRSEC' USING 08 00 T-HESAPNO T-EVRAKNO
      *  ˜ADE ALIž FATURASI <=> ˜ADE ALIž ˜RSAL˜YES˜
      *          WHEN 12 CALL 'EVRSEC' USING 11 00 T-HESAPNO T-EVRAKNO
      *          END-EVALUATE
      *     END-IF.
      *     IF F = 6
      *          EVALUATE HS-TIP 
      *  SATIž FATURASI <=> ˜RSAL˜YEL˜ SATIž FATURASI 
      *  SATIž FATURASI <=> SATIž FATURASI 
      *          WHEN 3 CALL 'EVRSEC' USING 01 03 T-HESAPNO T-EVRAKNO
      *  ALIž FATURASI <=> ˜RSAL˜YEL˜ ALIž FATURASI 
      *  ALIž FATURASI <=> ALIž FATURASI 
      *          WHEN 6 CALL 'EVRSEC' USING 04 06 T-HESAPNO T-EVRAKNO
      *  ˜ADE €IKIž FATURASI <=> ˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI 
      *  ˜ADE €IKIž FATURASI <=> ˜ADE €IKIž FATURASI 
      *          WHEN 9 CALL 'EVRSEC' USING 07 09 T-HESAPNO T-EVRAKNO
      *  ˜ADE ALIž FATURASI <=> ˜RSAL˜YEL˜ ˜ADE ALIž FATURASI 
      *  ˜ADE ALIž FATURASI <=> ˜ADE ALIž FATURASI
      *          WHEN 12 CALL 'EVRSEC' USING 10 12 T-HESAPNO T-EVRAKNO
      *          END-EVALUATE
      *     END-IF.
   
      * ž˜MD˜ ASIL EVRAK BA¦LA
       
           EVALUATE G-TIP ALSO G2-TIP
            WHEN 1 ALSO 3 
            WHEN 2 ALSO 0 MOVE 3 TO HS-TIP
            WHEN 4 ALSO 6
            WHEN 5 ALSO 0 MOVE 6 TO HS-TIP
            WHEN 7 ALSO 9
            WHEN 8 ALSO 0 MOVE 9 TO HS-TIP
            WHEN 10 ALSO 12
            WHEN 11 ALSO 0 MOVE 12 TO HS-TIP
           END-EVALUATE.
           MOVE G-EVRAKNO TO HS-EVRAKNO.
           READ HAREKET-SICIL.
           
           EVALUATE G-TIP ALSO G2-TIP
            WHEN 2 ALSO 0 
            WHEN 5 ALSO 0 
            WHEN 8 ALSO 0 
            WHEN 11 ALSO 0 MOVE I-TIPI(T) TO HS-BAGTIPI(1)
                           MOVE I-EVRAKNO(T) TO HS-BEVRAKNO(1)
            WHEN 1 ALSO 3
            WHEN 4 ALSO 6
            WHEN 7 ALSO 9
            WHEN 10 ALSO 12 MOVE I-TIPI(T) TO HS-BAGTIPI(2)
                           MOVE I-EVRAKNO(T) TO HS-BEVRAKNO(2)
           END-EVALUATE.
           PERFORM REWRITE-HAREKET-SICIL.
      
       EVRAKLARI-BAGLA-SON.       
       
       AKTAR-BASLAT.    
           MOVE 1 TO DP-SIRA.
       AKTAR-OKU.    
           MOVE DP-SIRA TO HD-SIRA.
           MOVE I-EVRAKNO(T) TO HD-EVRAKNO.
           MOVE I-TIPI(T) TO HD-TIP.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
                INVALID KEY GO AKTAR-SON.
           READ HAREKET-DETAY NEXT.
           IF I-TIPI(T) NOT = HD-TIP OR HD-EVRAKNO NOT = I-EVRAKNO(T)
                GO AKTAR-SON.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
       AKTAR-YAZ.
	       PERFORM WRITE-HAREKET-DETAY.
		   IF WVAR = 0 ADD 1 TO HD-SIRA GO AKTAR-YAZ.
           ADD 1 TO DP-SIRA.
           GO AKTAR-OKU.
       AKTAR-SON.
           MOVE HS-TIP TO DX-TIP. 
           MOVE HS-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           PERFORM ALT-TOPLAM-AL THRU ALT-TOPLAM-AL-SON.
           PERFORM ALT-TOPLAM-KAYDET.
           
           
       copy 'toplam.per'.
       copy 'numara.per'.
       
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-SICIL==
                                  "%T%"  BY "HAREKET-SICIL"
                                 ==%R%== BY ==HS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-DETAY==
                                  "%T%"  BY "HAREKET-DETAY"
                                 ==%R%== BY ==HD-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
