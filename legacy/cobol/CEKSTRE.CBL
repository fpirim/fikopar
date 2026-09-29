       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'EKSTRE'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           CLASS DIGIT IS "1234567890"
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'cformat.cpy'.
       copy 'ayar.cpy'.
       copy 'sicil.cpy'.
       copy 'depo.cpy'.
       copy 'siciltel.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'siciltel.cpz'.
       copy 'cformat.cpz'.
       copy 'sicil.cpz'.
       copy 'ayar.cpz'.
       copy 'depo.cpz'.
       
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-TARIH.
                    04 TP-YIL            PIC 9999.
                    04 TP-AY             PIC 99.
                    04 TP-GUN            PIC 99.
               03 TP-DEPONO           PIC 9999.
               03 TP-SIRA           PIC 9(10).
           02 TP-DPADI              PIC X(30).
           02 TP-TIP                PIC 99.
           02 TP-EVRAKNO            PIC X(15).
           02 TP-ACIKLAMA           PIC X(60).
           02 TP-VADE               PIC X(15).
           02 TP-BORC               PIC 9(13)V99.
           02 TP-ALACAK             PIC 9(13)V99.
           02 TP-BAKIYE             PIC S9(13)V99.
       WORKING-STORAGE SECTION.
       01 TOPLAM-VARIABLES.
           02 T-BORC            PIC 9(13)V99.
           02 T-ALACAK          PIC 9(13)V99.
           02 T-BAKIYE          PIC S9(13)V99.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
           02 B-TEL             PIC 9(12).
           02 B-CEP             PIC 9(12).
           02 B-FAX             PIC 9(12).
           02 V-TEL             PIC 9999 VALUE ZEROS.
           02 V-FAX             PIC 9999 VALUE ZEROS.
           02 V-CEP             PIC 9999 VALUE ZEROS.
           02 Z-TEL             PIC +ZZBZZZBZZZZZZZ.
           02 Z-FAX             PIC +ZZBZZZBZZZZZZZ.
           02 Z-CEP             PIC +ZZBZZZBZZZZZZZ.
           
           
       copy 'numara.var'.       
       copy 'trim.var'.
       copy 'cformat.var'.
       copy 'status.cpy'.    
       copy 'win.cpy'.
	   copy 'fileop.var'.
	   LINKAGE SECTION.
	   77 GSAAT         PIC 9(8).
       77 G-HESAPNO     PIC X(15).
       77 G-DEPONO      PIC 9999.
       01 BA-TARIH.
           02 BA-YIL        PIC 9999.
           02 BA-AY         PIC 99.
           02 BA-GUN        PIC 99.
       01 BT-TARIH.
           02 BT-YIL        PIC 9999.
           02 BT-AY         PIC 99.
           02 BT-GUN        PIC 99.
       PROCEDURE DIVISION USING GSAAT G-HESAPNO G-DEPONO 
            BA-TARIH BT-TARIH.
       copy 'cformat.err'.
       START-PROGRAM.
           MOVE GSAAT TO TSAAT.
           OPEN INPUT SICIL TEMPFILE.
           MOVE G-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY GO FORMAT-SON.
           
           MOVE 15 TO DX-TIP.
           MOVE 1 TO DX-DEPONO.
           PERFORM NUMARA-AL.
           IF AY-YAZDOS = SPACES OR AY-YAZPORT = SPACES
                    GO FORMAT-SON.
           MOVE AY-YAZDOS TO FORMATDOS.
           MOVE AY-YAZPORT TO YAZDOS.
           
           PERFORM AKTAR-SICIL.
           
           INITIALIZE TOPLAM-VARIABLES.
           
       copy 'cformat.per'.
       
       FORMAT-SON.
           CLOSE SICIL TEMPFILE.
           GO STOP-PROGRAM.
       copy 'trim.per'.
       AKTAR-SICIL.
           CALL 'C$SetEnv' USING 'SC-HESAPNO' SC-HESAPNO.
           CALL 'C$SetEnv' USING 'SC-HESAPADI' SC-HESAPADI.
           CALL 'C$SetEnv' USING 'SC-YETKILI' SC-YETKILI.
           CALL 'C$SetEnv' USING 'TITLE' AY-ACIKLAMA.
           
           MOVE SPACES TO ENV-VAL.
           STRING BA-GUN '/' BA-AY '/' BA-YIL DELIMITED BY SIZE
            INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'BA-TARIH' ENV-VAL.
           
           MOVE SPACES TO ENV-VAL.
           STRING BT-GUN '/' BT-AY '/' BT-YIL DELIMITED BY SIZE
            INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'BT-TARIH' ENV-VAL.
           
           OPEN INPUT DEPO.
           MOVE G-DEPONO TO DP-NO.
           READ DEPO INVALID KEY MOVE SPACES TO DP-ADI.
           CALL 'C$SetEnv' USING 'T-DEPO' DP-ADI.
           CLOSE DEPO.
           
           OPEN INPUT SICILTEL.
           MOVE ZEROS TO SCT-SIRA.
           MOVE SC-HESAPNO TO SCT-HESAPNO.
           READ SICILTEL INVALID KEY MOVE ZEROS TO SCT-TEL.
           COMPUTE V-TEL = SCT-TEL / 100000000.
           COMPUTE V-FAX = (SCT-TEL - V-TEL * 100000000) / 10000.
           COMPUTE V-CEP = SCT-TEL - V-TEL * 100000000 - V-FAX * 10000.
           IF V-TEL NOT = ZEROS MOVE V-TEL TO SCT-SIRA
                READ SICILTEL END-READ MOVE SCT-TEL TO B-TEL.
           IF V-FAX NOT = ZEROS MOVE V-FAX TO SCT-SIRA
                READ SICILTEL END-READ MOVE SCT-TEL TO B-FAX.
           IF V-CEP NOT = ZEROS MOVE V-CEP TO SCT-SIRA
                READ SICILTEL END-READ MOVE SCT-TEL TO B-CEP.
           CLOSE SICILTEL.
           MOVE B-TEL TO Z-TEL. MOVE B-CEP TO Z-CEP. 
           MOVE B-FAX TO Z-FAX.
           MOVE SPACES TO ENV-VAL.
           STRING 'Tel:' Z-TEL ' Cep:' Z-CEP ' Faks:' Z-FAX
            DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'SC-TELEFON' ENV-VAL.
            
           
       SONRAKI-VARMI.
           READ TEMPFILE NEXT AT END MOVE 0 TO VAR 
            READ TEMPFILE PREVIOUS GO SONRAKI-VARMI-SON.
           READ TEMPFILE PREVIOUS AT END CONTINUE.
           MOVE 1 TO VAR.
       SONRAKI-VARMI-SON.
       
       copy 'numara.per'.
       
       SONRAKI-AKTAR.
           READ TEMPFILE NEXT AT END READ TEMPFILE PREVIOUS 
             MOVE 1 TO BITTI GO SONRAKI-AKTAR-SON.
           IF G-DEPONO NOT = ZEROS AND TP-DEPONO NOT = G-DEPONO
                GO SONRAKI-AKTAR.
           
           MOVE SPACES TO ENV-VAL.
           STRING TP-GUN '/' TP-AY '/' TP-YIL
             DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-TARIH' ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-DEPONO' TP-DPADI.
           
           MOVE SPACES TO ENV-VAL.
           EVALUATE TP-TIP
           WHEN 1 WHEN 4 WHEN 7 WHEN 10 
                MOVE 'òft.' TO ENV-VAL
           WHEN 3 WHEN 6 WHEN 9 WHEN 12 WHEN 13 WHEN 14
               MOVE 'Fat.' TO ENV-VAL
           WHEN 16 WHEN 17
               MOVE 'Mak.' TO ENV-VAL           
           END-EVALUATE.
           CALL 'C$SetEnv' USING 'TP-TIP' ENV-VAL.
           
           CALL 'C$SetEnv' USING 'TP-EVRAKNO' TP-EVRAKNO.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA' TP-ACIKLAMA.
           CALL 'C$SetEnv' USING 'TP-VADE' TP-VADE.

           MOVE TP-BORC TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-BORC' ENV-VAL.
           
           MOVE TP-ALACAK TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-ALACAK' ENV-VAL.
           
           PERFORM ALT-TOPLAM-EKLE.

           MOVE T-BORC TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'T-BORC' ENV-VAL.
           
           MOVE T-ALACAK TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'T-ALACAK' ENV-VAL.
           
           COMPUTE T-BAKIYE = T-BORC - T-ALACAK.
           
           MOVE T-BAKIYE TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-BAKIYE' ENV-VAL.
           
           CALL 'C$SetEnv' USING 'T-BAKIYE1'.
           CALL 'C$SetEnv' USING 'T-BAKIYE2'.
           IF T-BORC > T-ALACAK
             CALL 'C$SetEnv' USING 'T-BAKIYE1' ENV-VAL
           ELSE
             CALL 'C$SetEnv' USING 'T-BAKIYE2' ENV-VAL
           END-IF.
           
       SONRAKI-AKTAR-SON.
        
       AKTAR-NAKLIYEKUN.
           CALL 'C$SetEnv' USING 'TP-TARIH'.
           CALL 'C$SetEnv' USING 'TP-DEPONO'.
           CALL 'C$SetEnv' USING 'TP-TIP'.
           CALL 'C$SetEnv' USING 'TP-EVRAKNO'.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA' 'Nakli YekÅn'.
           CALL 'C$SetEnv' USING 'TP-VADE'.

           MOVE T-BORC TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-BORC' ENV-VAL.
           
           MOVE T-ALACAK TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-ALACAK' ENV-VAL.
           
           COMPUTE T-BAKIYE = T-BORC - T-ALACAK.
           MOVE T-BAKIYE TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-BAKIYE' ENV-VAL.
           
       ALT-TOPLAM-EKLE.
           ADD TP-BORC TO T-BORC.
           ADD TP-ALACAK TO T-ALACAK.
           
       ENVIRONMENT-TEMIZLE.
           CALL 'C$SetEnv' USING 'SC-HESAPNO'.
           CALL 'C$SetEnv' USING 'SC-HESAPADI'.
           CALL 'C$SetEnv' USING 'SC-YETKILI'.
           CALL 'C$SetEnv' USING 'TP-TARIH'.
           CALL 'C$SetEnv' USING 'TP-DEPONO'.
           CALL 'C$SetEnv' USING 'TP-TIP'.
           CALL 'C$SetEnv' USING 'TP-EVRAKNO'.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA'.
           CALL 'C$SetEnv' USING 'TP-VADE'.
           CALL 'C$SetEnv' USING 'TP-BORC'.
           CALL 'C$SetEnv' USING 'TP-ALACAK'.
           CALL 'C$SetEnv' USING 'TP-BAKIYE'.
           CALL 'C$SetEnv' USING 'T-BORC'.
           CALL 'C$SetEnv' USING 'T-ALACAK'.
           CALL 'C$SetEnv' USING 'T-BAKIYE1'.
           CALL 'C$SetEnv' USING 'T-BAKIYE2'.
           CALL 'C$SetEnv' USING 'BA-TARIH'.
           CALL 'C$SetEnv' USING 'BT-TARIH'.
           CALL 'C$SetEnv' USING 'T-DEPO'.
           CALL 'C$SetEnv' USING 'SC-TELEFON'.
           CALL 'C$SetEnv' USING 'TITLE'.

           
       STOP-PROGRAM.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
       