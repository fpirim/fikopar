       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'CEKSENET'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
            SELECT HAREKET-INDEX ASSIGN TO DISK, 'DATA\HARSIC.IDX'
                 ORGANIZATION INDEXED
                 ACCESS MODE IS DYNAMIC
                 ALTERNATE RECORD KEY IS HIDX-HESAPADI WITH DUPLICATES
                 ALTERNATE RECORD KEY IS HIDX-YETKILI WITH DUPLICATES
                 RECORD KEY IS HIDX-KEY.
            
            SELECT HAREKET-SICIL ASSIGN TO DISK, 'DATA\HARSIC.DAT'
                   ORGANIZATION INDEXED
                   ACCESS MODE IS DYNAMIC
                   ALTERNATE RECORD KEY IS HS-HESAPNO WITH DUPLICATES
                   RECORD KEY IS HS-KEY.
                   
            
       DATA DIVISION.
       FILE SECTION.
       
       FD HAREKET-SICIL DATA RECORD IS HS-KAYIT.
       01 HS-KAYIT.
           02 HS-KEY.
               03 HS-TIP                PIC 99.
               03 HS-EVRAKNO            PIC X(15).
           02 HS-TARIH.
               03 HS-DYIL               PIC 9999.
               03 HS-DAY                PIC 99.
               03 HS-DGUN               PIC 99.
           02 HS-SAAT.
               03 HS-SA                 PIC 99.
               03 HS-DD                 PIC 99.
               03 HS-SS                 PIC 99.                
           02 HS-FTARIH.
               03 HS-FYIL               PIC 9999.
               03 HS-FAY                PIC 99.
               03 HS-FGUN               PIC 99.
           02 HS-HESAPNO                PIC X(15).
           02 HS-HESAPADI               PIC X(50).
           02 HS-YETKILI                PIC X(40).
           02 HS-ADRES1                 PIC X(40).
           02 HS-ADRES2                 PIC X(40).
           02 HS-ADRES3                 PIC X(40).
           02 HS-MAHALLE                PIC X(20).
           02 HS-ILCE                   PIC X(20).
           02 HS-IL                     PIC X(20).
           02 HS-ULKE                   PIC X(20).
           02 HS-VDA                    PIC X(30).
           02 HS-VNO                    PIC X(10).
           02 HS-TCNO                   PIC X(11).
           02 HS-TSNO                   PIC X(11).
           02 HS-MERSIS                 PIC X(20).
           02 HS-GUN                    PIC 99999.
           02 HS-PLKOD                  PIC 9(4).
           02 HS-DEPONO                 PIC 9999.
           02 HS-DUZEN                  PIC 9(4).
           02 HS-BAG OCCURS 2 TIMES.
               03 HS-BAGTIPI                PIC 99.
               03 HS-BEVRAKNO               PIC X(15).
           02 HS-SONUC                  PIC 99.    
           
       FD HAREKET-INDEX DATA RECORD IS HIDX-KAYIT.
       01 HIDX-KAYIT.
           02 HIDX-KEY.
               03 HIDX-EVRAKNO          PIC X(17).
           02 HIDX-HESAPADI             PIC X(52).
           02 HIDX-YETKILI              PIC X(42).    
       
       
       WORKING-STORAGE SECTION.
       copy 'status.cpy'.
       PROCEDURE DIVISION.
       BASLA.
           OPEN INPUT HAREKET-SICIL OUTPUT HAREKET-INDEX.
           CLOSE HAREKET-INDEX. OPEN I-O HAREKET-INDEX.
       OKU.
           READ HAREKET-SICIL NEXT AT END GO SON.
           MOVE HS-KEY TO HIDX-KEY.
           MOVE HS-HESAPADI TO HIDX-HESAPADI.
           MOVE HS-YETKILI TO HIDX-YETKILI.
           WRITE HIDX-KAYIT.
           GO OKU.
       SON.
           CLOSE HAREKET-SICIL HAREKET-INDEX.
           EXIT PROGRAM.
           STOP RUN.