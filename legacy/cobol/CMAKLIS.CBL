       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'CMAKLIS'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'cformat.cpy'.
       copy 'ayar.cpy'.
       copy 'sicil.cpy'.
            SELECT TEMPFILE ASSIGN TO DISK, TEMP-DOSYA
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TP-KEY.
       DATA DIVISION.
       FILE SECTION.
       copy 'cformat.cpz'.
       copy 'sicil.cpz'.
       copy 'ayar.cpz'.
       
       FD TEMPFILE DATA RECORD IS TP-KAYIT.
       01 TP-KAYIT.
           02 TP-KEY.
               03 TP-TARIH.
                    04 TP-YIL            PIC 9999.
                    04 TP-AY             PIC 99.
                    04 TP-GUN            PIC 99.
               03 TP-SIRA           PIC 9(10).
           02 TP-SEKILNO                PIC 9999.    
           02 TP-SEKILADI               PIC X(60).
           02 TP-HESAPNO            PIC X(15).
           02 TP-HESAPADI           PIC X(50).
           02 TP-YETKILI            PIC X(40).
           02 TP-EVRAKNO           PIC X(15).
           02 TP-DEPONO             PIC 9999.
           02 TP-DPADI              PIC X(30).
           02 TP-ACIKLAMA            PIC X(15).
           02 TP-TUTAR              PIC 9(13)V99.
       WORKING-STORAGE SECTION.
       01 TOPLAM-VARIABLES.
           02 T-TUTAR          PIC 9(13)V99.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
       
       copy 'numara.var'.       
       copy 'trim.var'.
       copy 'cformat.var'.
       copy 'status.cpy'.    
       copy 'win.cpy'.
	   LINKAGE SECTION.
	   77 GSAAT         PIC 9(8).
       77 TIP           PIC 99.
       PROCEDURE DIVISION USING GSAAT TIP.
       copy 'cformat.err'.
       START-PROGRAM.
           MOVE GSAAT TO TSAAT.
           OPEN INPUT TEMPFILE AYAR.
           
           MOVE TIP TO AY-TIP.
           READ AYAR.
           MOVE AY-YAZDOS TO FORMATDOS.
           MOVE AY-YAZPORT TO YAZDOS.
           
           PERFORM AKTAR-SICIL.
           
           INITIALIZE TOPLAM-VARIABLES.
           
       copy 'cformat.per'.
       
       FORMAT-SON.
           CLOSE TEMPFILE AYAR.
           GO STOP-PROGRAM.
       copy 'trim.per'.
       AKTAR-SICIL.
           CALL 'C$SetEnv' USING 'TITLE' AY-ACIKLAMA.
           
           
       SONRAKI-VARMI.
           READ TEMPFILE NEXT AT END MOVE 0 TO VAR 
            READ TEMPFILE PREVIOUS GO SONRAKI-VARMI-SON.
           READ TEMPFILE PREVIOUS AT END CONTINUE.
           MOVE 1 TO VAR.
       SONRAKI-VARMI-SON.
       
       SONRAKI-AKTAR.
           READ TEMPFILE NEXT AT END READ TEMPFILE PREVIOUS 
             MOVE 1 TO BITTI GO SONRAKI-AKTAR-SON.
           
           MOVE SPACES TO ENV-VAL.
           STRING TP-GUN '/' TP-AY '/' TP-YIL
             DELIMITED BY SIZE INTO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-TARIH' ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-SEKILNO' TP-SEKILADI.
           CALL 'C$SetEnv' USING 'TP-HESAPNO' TP-HESAPNO.
           CALL 'C$SetEnv' USING 'TP-HESAPADI' TP-HESAPADI.
           CALL 'C$SetEnv' USING 'TP-YETKILI' TP-YETKILI.
           CALL 'C$SetEnv' USING 'TP-EVRAKNO' TP-EVRAKNO.
           CALL 'C$SetEnv' USING 'TP-DEPONO' TP-DPADI.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA' TP-ACIKLAMA.
           MOVE TP-TUTAR TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-TUTAR' ENV-VAL.
           
           PERFORM ALT-TOPLAM-EKLE.

           MOVE T-TUTAR TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'T-TUTAR' ENV-VAL.
           
       SONRAKI-AKTAR-SON.
        
       AKTAR-NAKLIYEKUN.
           CALL 'C$SetEnv' USING 'TP-TARIH'.
           CALL 'C$SetEnv' USING 'TP-SEKILNO'.
           CALL 'C$SetEnv' USING 'TP-HESAPNO'.
           CALL 'C$SetEnv' USING 'TP-HESAPADI'.
           CALL 'C$SetEnv' USING 'TP-YETKILI'.
           CALL 'C$SetEnv' USING 'TP-EVRAKNO'.
           CALL 'C$SetEnv' USING 'TP-DEPONO'.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA' 'Nakli YekÅn'.
           MOVE T-TUTAR TO Z-ENV.
           MOVE SPACES TO ENV-VAL. MOVE Z-ENV TO ENV-VAL.
           CALL 'C$SetEnv' USING 'TP-TUTAR' ENV-VAL.
           
       ALT-TOPLAM-EKLE.
           ADD TP-TUTAR TO T-TUTAR.
           
       ENVIRONMENT-TEMIZLE.
           CALL 'C$SetEnv' USING 'TP-TARIH'.
           CALL 'C$SetEnv' USING 'TP-SEKILNO'.
           CALL 'C$SetEnv' USING 'TP-HESAPNO'.
           CALL 'C$SetEnv' USING 'TP-HESAPADI'.
           CALL 'C$SetEnv' USING 'TP-YETKILI'.
           CALL 'C$SetEnv' USING 'TP-EVRAKNO'.
           CALL 'C$SetEnv' USING 'TP-DEPONO'.
           CALL 'C$SetEnv' USING 'TP-ACIKLAMA'.
           CALL 'C$SetEnv' USING 'TP-TUTAR'.
           CALL 'C$SetEnv' USING 'TITLE'.
           CALL 'C$SetEnv' USING 'T-TUTAR'.

           
       STOP-PROGRAM.
           EXIT PROGRAM.
           STOP RUN.
       