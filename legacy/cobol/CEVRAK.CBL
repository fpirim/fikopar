       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'CEVRAK'.
       AUTHOR. FIKRET.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           CLASS DIGIT IS "0123456789"
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'cformat.cpy'.
       copy 'hareket.cpy'.
       copy 'ayar.cpy'.
       copy 'siciltel.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'cformat.cpz'.
       copy 'hareket.cpz'.    
       copy 'ayar.cpz'.
       copy 'siciltel.cpz'.
       WORKING-STORAGE SECTION.
           
       01 TOPLAM-VARIABLES.
           02 I-TOPLAM          PIC 9(12)V99999999.
           02 I-TOPLAM-R        PIC 9(12)V99.
           02 I-ISKTOPLAM       PIC 9(12)V99999999.
           02 I-ISKTOPLAM-R     PIC 9(12)V99.
           02 I-KDVTOPLAM       PIC 9(12)V99999999.
           02 I-KDVTOPLAM-R     PIC 9(12)V99.
           02 I-ARATOPLAM       PIC 9(12)V99.
           02 I-GENELTOPLAM     PIC 9(12)V99.
           02 I-ISK1            PIC 9(12)V99999999.
           02 I-ISK2            PIC 9(12)V99999999.
           02 I-KDV             PIC 9(12)V99999999.
           02 I-TUTAR           PIC 9(12)V99.
           02 I-ADRES4          PIC X(60).
           02 I-TEL             PIC X(60).
           02 B-TEL             PIC 9(12).
           02 B-CEP             PIC 9(12).
           02 B-FAX             PIC 9(12).
           02 Z-TEL             PIC +ZZBZZZBZZZZZZZ.
           02 V-TEL             PIC 9999 VALUE ZEROS.
           02 V-FAX             PIC 9999 VALUE ZEROS.
           02 V-CEP             PIC 9999 VALUE ZEROS.
           02 A-SIZE            PIC 999.
           02 NAK               PIC 999.
           
       copy 'trim.var'.
       copy 'cformat.var'.
       copy 'numara.var'.
       copy 'status.cpy'.
	   copy 'fileop.var'.
       LINKAGE SECTION.
       77 G-TIP                PIC 99.
       77 G-EVRAKNO            PIC X(15).
       PROCEDURE DIVISION USING G-TIP G-EVRAKNO.
       copy 'cformat.err'.
       START-PROGRAM.
           
           OPEN INPUT HAREKET-SICIL HAREKET-DETAY.
           MOVE G-TIP TO HS-TIP.
           MOVE G-EVRAKNO TO HS-EVRAKNO HD-EVRAKNO.
           READ HAREKET-SICIL INVALID KEY
                CLOSE HAREKET-SICIL HAREKET-DETAY
                GO STOP-PROGRAM.
                
           PERFORM AKTAR-SICIL.
           
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE HS-TIP TO HD-TIP.
           MOVE 1 TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
             INVALID KEY GO FORMAT-SON.
                
                
           MOVE HS-TIP TO DX-TIP.
           MOVE HS-DEPONO TO DX-DEPONO.
           PERFORM NUMARA-AL.
           IF AY-YAZDOS = SPACES OR AY-YAZPORT = SPACES
            CLOSE HAREKET-SICIL HAREKET-DETAY           
            GO STOP-PROGRAM.
           
           MOVE AY-YAZDOS TO FORMATDOS.
           MOVE AY-YAZPORT TO YAZDOS.
           
           INITIALIZE TOPLAM-VARIABLES.
           MOVE 1 TO NAK.
           
       copy 'cformat.per'.
       
       FORMAT-SON.
           CLOSE HAREKET-SICIL HAREKET-DETAY.
           
           IF HS-SONUC = 0 OPEN I-O HAREKET-SICIL MOVE 1 TO HS-SONUC
                    PERFORM REWRITE-HAREKET-SICIL CLOSE HAREKET-SICIL.
           IF AY-EVRAKNO NOT = HS-EVRAKNO GO STOP-PROGRAM.
           
           MOVE HS-TIP TO DX-TIP.
           MOVE HS-DEPONO TO DX-DEPONO.
           OPEN I-O AYAR.
           PERFORM NAK TIMES
                PERFORM NUMARA-ARTIR
           END-PERFORM.
           CLOSE AYAR.
           GO STOP-PROGRAM.
       
       
       copy 'numara.per'.    
       copy 'trim.per'.
       AKTAR-SICIL.
           CALL 'C$SetEnv' USING 'HS-EVRAKNO' HS-EVRAKNO.
          
           MOVE SPACES TO ENV-VAL.
           STRING HS-DGUN '/' HS-DAY '/' HS-DYIL
             DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'HS-TARIH' ENV-VAL.
          
           MOVE SPACES TO ENV-VAL.
           STRING HS-SA ':' HS-DD ':' HS-SS
             DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'HS-SAAT' ENV-VAL.
          
           MOVE SPACES TO ENV-VAL.
           STRING HS-FGUN '/' HS-FAY '/' HS-FYIL
             DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING  'HS-FTARIH' ENV-VAL.
           
           OPEN INPUT SICILTEL.
           MOVE ZEROS TO SCT-SIRA.
           MOVE HS-HESAPNO TO SCT-HESAPNO.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL.
           COMPUTE V-TEL = SCT-TEL / 100000000.
           COMPUTE V-FAX = (SCT-TEL - V-TEL * 100000000) / 10000.
           COMPUTE V-CEP = SCT-TEL - V-TEL * 100000000 - V-FAX * 10000.
           IF V-TEL NOT = ZEROS MOVE V-TEL TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
				MOVE SCT-TEL TO B-TEL.
           IF V-FAX NOT = ZEROS MOVE V-FAX TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
				MOVE SCT-TEL TO B-FAX.
           IF V-CEP NOT = ZEROS MOVE V-CEP TO SCT-SIRA
                READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL END-READ
				MOVE SCT-TEL TO B-CEP.
           CLOSE SICILTEL.
           
           MOVE SPACES TO I-TEL.
           MOVE B-TEL TO Z-TEL.
           MOVE Z-TEL TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           IF V-TEL NOT = ZEROS 
              STRING 'TEL:' TRIM-VALUE(1:TRIM-SIZE)
               DELIMITED BY SIZE INTO I-TEL
           END-IF.
           
           MOVE I-TEL TO TRIM-VALUE.
           PERFORM TRIM-BLOCK. MOVE TRIM-SIZE TO A-SIZE.
           MOVE B-CEP TO Z-TEL.
           MOVE Z-TEL TO TRIM-VALUE. PERFORM TRIM-BLOCK.
           
           IF V-CEP NOT = ZEROS 
              EVALUATE A-SIZE
              WHEN 0 
               STRING 'CEP:' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-TEL
              WHEN OTHER 
               STRING I-TEL(1:A-SIZE) ' CEP:' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-TEL
              END-EVALUATE
           END-IF.
           
           MOVE I-TEL TO TRIM-VALUE.
           PERFORM TRIM-BLOCK. MOVE TRIM-SIZE TO A-SIZE.
           MOVE B-FAX TO Z-TEL.
           MOVE Z-TEL TO TRIM-VALUE. PERFORM TRIM-BLOCK.
           
           IF V-FAX NOT = ZEROS
              EVALUATE A-SIZE
              WHEN 0 
               STRING 'FAX:' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-TEL
              WHEN OTHER 
               STRING I-TEL(1:A-SIZE) ' FAX:' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-TEL
              END-EVALUATE
           END-IF.
           
           MOVE SPACES TO I-ADRES4.
           MOVE HS-MAHALLE TO TRIM-VALUE.
           PERFORM TRIM-BLOCK.
           IF HS-MAHALLE NOT = SPACES MOVE HS-MAHALLE TO I-ADRES4.
           
           MOVE I-ADRES4 TO TRIM-VALUE.
           PERFORM TRIM-BLOCK. MOVE TRIM-SIZE TO A-SIZE.
           MOVE HS-ILCE TO TRIM-VALUE. PERFORM TRIM-BLOCK.
           IF HS-ILCE NOT = SPACES
              EVALUATE A-SIZE
              WHEN 0 MOVE HS-ILCE TO I-ADRES4
              WHEN OTHER 
               STRING I-ADRES4(1:A-SIZE) '/' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-ADRES4
              END-EVALUATE
           END-IF.
           
           MOVE I-ADRES4 TO TRIM-VALUE.
           PERFORM TRIM-BLOCK. MOVE TRIM-SIZE TO A-SIZE.
           MOVE HS-IL TO TRIM-VALUE. PERFORM TRIM-BLOCK.
           IF HS-IL NOT = SPACES
              EVALUATE A-SIZE
              WHEN 0 MOVE HS-IL TO I-ADRES4
              WHEN OTHER 
               STRING I-ADRES4(1:A-SIZE) '/' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-ADRES4
              END-EVALUATE
           END-IF.
           
           MOVE I-ADRES4 TO TRIM-VALUE.
           PERFORM TRIM-BLOCK. MOVE TRIM-SIZE TO A-SIZE.
           MOVE HS-ULKE TO TRIM-VALUE. PERFORM TRIM-BLOCK.
           IF HS-ULKE NOT = SPACES
              EVALUATE A-SIZE
              WHEN 0 MOVE HS-ULKE TO I-ADRES4
              WHEN OTHER 
               STRING I-ADRES4(1:A-SIZE) '/' TRIM-VALUE(1:TRIM-SIZE)
                DELIMITED BY SIZE INTO I-ADRES4
              END-EVALUATE
           END-IF.
          
           CALL 'C$SetEnv' USING 'I-ADRES4' I-ADRES4.
           CALL 'C$SetEnv' USING 'I-TEL' I-TEL.
           CALL 'C$SetEnv' USING 'HS-HESAPNO' HS-HESAPNO.
           CALL 'C$SetEnv' USING 'HS-HESAPADI' HS-HESAPADI.
           CALL 'C$SetEnv' USING 'HS-YETKILI' HS-YETKILI.
           CALL 'C$SetEnv' USING 'HS-ADRES1' HS-ADRES1.
           CALL 'C$SetEnv' USING 'HS-ADRES2' HS-ADRES2.
           CALL 'C$SetEnv' USING 'HS-ADRES3' HS-ADRES3.
           CALL 'C$SetEnv' USING 'HS-MAHALLE' HS-MAHALLE.
           CALL 'C$SetEnv' USING 'HS-ILCE' HS-ILCE.
           CALL 'C$SetEnv' USING 'HS-IL' HS-IL.
           CALL 'C$SetEnv' USING 'HS-ILCE' HS-ULKE.
           CALL 'C$SetEnv' USING 'HS-VDA' HS-VDA.
           CALL 'C$SetEnv' USING 'HS-VNO' HS-VNO.
           CALL 'C$SetEnv' USING 'HS-TCNO' HS-TCNO.
           CALL 'C$SetEnv' USING 'HS-TSNO' HS-TSNO.
           CALL 'C$SetEnv' USING 'HS-MERSIS' HS-MERSIS.
           MOVE HS-GUN TO Z-ENV. 
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HS-GUN' ENV-VAL.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           
       SONRAKI-VARMI.
           READ HAREKET-DETAY NEXT AT END READ HAREKET-DETAY PREVIOUS
             MOVE 0 TO VAR GO SONRAKI-VARMI-SON.
           IF HD-EVRAKNO NOT = HS-EVRAKNO OR HD-TIP NOT = HS-TIP
             MOVE 0 TO VAR GO SONRAKI-VARMI-SON.
           READ HAREKET-DETAY PREVIOUS AT END CONTINUE.
           MOVE 1 TO VAR.
       SONRAKI-VARMI-SON.
       
       SONRAKI-AKTAR.
           READ HAREKET-DETAY NEXT AT END 
             MOVE 1 TO BITTI GO SONRAKI-AKTAR-SON.
           IF HD-EVRAKNO NOT = HS-EVRAKNO OR HD-TIP NOT = HS-TIP
             MOVE 1 TO BITTI GO SONRAKI-AKTAR-SON.
           
           CALL 'C$SetEnv' USING 'HD-EVRAKNO' HD-EVRAKNO.
           MOVE SPACES TO ENV-VAL. MOVE HD-KATNO TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-KATNO' ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-STNO' HD-STNO.
           CALL 'C$SetEnv' USING 'HD-PRCNO' HD-PRCNO.
           CALL 'C$SetEnv' USING 'HD-TIPI' HD-TIPI.
           CALL 'C$SetEnv' USING 'HD-CINSI' HD-CINSI.
           CALL 'C$SetEnv' USING 'HD-MARKA' HD-MARKA.
           MOVE HD-ISKONTO1 TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-ISKONTO1' ENV-VAL.
           MOVE HD-ISKONTO2 TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-ISKONTO2' ENV-VAL.
           MOVE HD-KDV TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-KDV' ENV-VAL.
           MOVE HD-ADET TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-ADET' ENV-VAL.
           MOVE HD-FIYAT TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-FIYAT' ENV-VAL.
           
           PERFORM ALT-TOPLAM-EKLE.
           
           MOVE I-TUTAR TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-TUTAR' ENV-VAL.
           MOVE I-TOPLAM-R TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-TOPLAM' ENV-VAL.
           MOVE I-ISKTOPLAM-R TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-ISKTOPLAM' ENV-VAL.
           MOVE I-ARATOPLAM TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-ARATOPLAM' ENV-VAL.
           MOVE I-KDVTOPLAM-R TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-KDVTOPLAM' ENV-VAL.
           MOVE I-GENELTOPLAM TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'I-GENELTOPLAM' ENV-VAL.
           MOVE SPACES TO ENV-VAL
           CALL 'PARA' USING I-GENELTOPLAM ENV-VAL.
           CANCEL 'PARA'.
           CALL 'C$SetEnv' USING 'I-YAZIYLA' ENV-VAL.
           
        SONRAKI-AKTAR-SON.
        
        AKTAR-NAKLIYEKUN.
           CALL 'C$SetEnv' USING 'HD-EVRAKNO' HD-EVRAKNO.
           MOVE SPACES TO ENV-VAL. MOVE HD-KATNO TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-KATNO'.
           CALL 'C$SetEnv' USING 'HD-STNO'.
           CALL 'C$SetEnv' USING 'HD-PRCNO'.
           CALL 'C$SetEnv' USING 'HD-TIPI'.
           CALL 'C$SetEnv' USING 'HD-CINSI' 'NAKL˜ YEKšN'.
           CALL 'C$SetEnv' USING 'HD-MARKA'.
           CALL 'C$SetEnv' USING 'HD-ISKONTO1'.
           CALL 'C$SetEnv' USING 'HD-ISKONTO2'.
           CALL 'C$SetEnv' USING 'HD-KDV'.
           MOVE 1 TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-ADET' ENV-VAL.
           MOVE I-TOPLAM TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'HD-FIYAT' ENV-VAL.
           CALL 'C$SetEnv' USING 'I-TUTAR' ENV-VAL.
           ADD 1 TO NAK.
           
       ALT-TOPLAM-EKLE.
           COMPUTE I-ISK1 = HD-FIYAT * HD-ISKONTO1 / 100.
           COMPUTE I-ISK2 = (HD-FIYAT - I-ISK1) 
                        * HD-ISKONTO2 / 100.
           COMPUTE I-KDV = (HD-FIYAT - I-ISK1 - I-ISK2) 
                        * HD-KDV / 100.
           COMPUTE I-ISKTOPLAM = I-ISKTOPLAM +
                    (I-ISK1 * HD-ADET) + (I-ISK2 * HD-ADET).
           COMPUTE I-KDVTOPLAM = I-KDVTOPLAM + (I-KDV * HD-ADET).
           COMPUTE I-TOPLAM =  I-TOPLAM + (HD-FIYAT * HD-ADET).
           
           COMPUTE I-TOPLAM-R ROUNDED = I-TOPLAM + 0.
           COMPUTE I-ISKTOPLAM-R ROUNDED = I-ISKTOPLAM + 0.
           COMPUTE I-KDVTOPLAM-R ROUNDED = I-KDVTOPLAM + 0.
           COMPUTE I-ARATOPLAM = I-TOPLAM-R - I-ISKTOPLAM-R.
           COMPUTE I-GENELTOPLAM = I-ARATOPLAM + I-KDVTOPLAM-R.
           COMPUTE I-TUTAR ROUNDED = HD-FIYAT * HD-ADET.
           
       ENVIRONMENT-TEMIZLE.
           CALL 'C$SetEnv' USING 'HS-EVRAKNO'.
           CALL 'C$SetEnv' USING 'HS-TARIH'.
           CALL 'C$SetEnv' USING 'HS-SAAT'.
           CALL 'C$SetEnv' USING 'HS-FTARIH'.
           CALL 'C$SetEnv' USING 'HS-HESAPNO'.
           CALL 'C$SetEnv' USING 'HS-HESAPADI'.
           CALL 'C$SetEnv' USING 'HS-YETKILI'.
           CALL 'C$SetEnv' USING 'HS-ADRES1'.
           CALL 'C$SetEnv' USING 'HS-ADRES2'.
           CALL 'C$SetEnv' USING 'HS-ADRES3'.
           CALL 'C$SetEnv' USING 'HS-MAHALLE'.
           CALL 'C$SetEnv' USING 'HS-ILCE'.
           CALL 'C$SetEnv' USING 'HS-IL'.
           CALL 'C$SetEnv' USING 'HS-ILCE'.
           CALL 'C$SetEnv' USING 'HS-VDA'.
           CALL 'C$SetEnv' USING 'HS-VNO'.
           CALL 'C$SetEnv' USING 'HS-TCNO'.
           CALL 'C$SetEnv' USING 'HS-TSNO'.
           CALL 'C$SetEnv' USING 'HS-MERSIS'.
           CALL 'C$SetEnv' USING 'HS-GUN'.
           CALL 'C$SetEnv' USING 'HD-EVRAKNO'.
           CALL 'C$SetEnv' USING 'HD-KATNO'.
           CALL 'C$SetEnv' USING 'HD-STNO'.
           CALL 'C$SetEnv' USING 'HD-PRCNO'.
           CALL 'C$SetEnv' USING 'HD-TIPI'.
           CALL 'C$SetEnv' USING 'HD-CINSI'.
           CALL 'C$SetEnv' USING 'HD-MARKA'.
           CALL 'C$SetEnv' USING 'HD-ISKONTO1'.
           CALL 'C$SetEnv' USING 'HD-ISKONTO2'.
           CALL 'C$SetEnv' USING 'HD-KDV'.
           CALL 'C$SetEnv' USING 'HD-ADET'.
           CALL 'C$SetEnv' USING 'HD-FIYAT'.
           CALL 'C$SetEnv' USING 'I-ADRES4'.
           CALL 'C$SetEnv' USING 'I-TEL'.
           CALL 'C$SetEnv' USING 'I-TUTAR'.
           CALL 'C$SetEnv' USING 'I-TOPLAM'.
           CALL 'C$SetEnv' USING 'I-ISKTOPLAM'.
           CALL 'C$SetEnv' USING 'I-ARATOPLAM'.
           CALL 'C$SetEnv' USING 'I-KDVTOPLAM'.
           CALL 'C$SetEnv' USING 'I-GENELTOPLAM'.
           
       STOP-PROGRAM.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-SICIL==
                                  "%T%"  BY "HAREKET-SICIL"
                                 ==%R%== BY ==HS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
