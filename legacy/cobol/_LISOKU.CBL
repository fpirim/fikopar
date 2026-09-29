       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'LISOKU'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT STOK ASSIGN TO DISK, 'C:\WINDOWS\TEMP\STOK.DAT'
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS ST-KEY
               FILE STATUS IS ST-STOK.
       copy 'exls.cpy'.
           SELECT HAREKET-DETAY ASSIGN TO DISK, 
                'C:\WINDOWS\TEMP\HARDET.DAT'
                   ORGANIZATION INDEXED
                   ACCESS MODE IS DYNAMIC
                   ALTERNATE RECORD KEY IS HD-KATKEY WITH DUPLICATES
                   RECORD KEY IS HD-KEY
                   FILE STATUS IS ST-HAREKET-DETAY.
           SELECT PASSTOK ASSIGN TO DISK, 'C:\WINDOWS\TEMP\PASSTOK.DAT'
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS PAS-KEY
               FILE STATUS IS ST-PASSTOK.
       DATA DIVISION.
       FILE SECTION.
       copy 'exls.cpz'.
        FD PASSTOK DATA RECORD IS PAS-KAYIT.
       01 PAS-KAYIT.
           02 PAS-KEY.
               03 PAS-KATNO              PIC 9999.
               03 PAS-STNO               PIC X(15).
           02 PAS-PRCNO                  PIC X(30).
           02 PAS-OEMNO                  PIC X(30).
           02 PAS-TIPI                   PIC X(30).
           02 PAS-CINSI                  PIC X(60).
           02 PAS-MARKA                  PIC X(30).
           02 PAS-MIN                    PIC 9(12)V9999.
           02 PAS-MAX                    PIC 9(12)V9999.
           02 PAS-FIYAT                  PIC 9(10)V9999.
           02 PAS-NOTE                   PIC X(10).
           02 PAS-DEG.
                 03 PAS-YIL          PIC 9999.
                 03 PAS-AY           PIC 99.
                 03 PAS-GUN          PIC 99.
           02 PAS-PAKET                  PIC 9(5).
           02 PAS-TARIH.
                 03 PAS-IYIL        PIC 9999.
                 03 PAS-IAY         PIC 99.
                 03 PAS-IGUN        PIC 99.
           02 PAS-ZAMAN.
                 03 PAS-SA      PIC 99.
                 03 PAS-DD      PIC 99.
                 03 PAS-SS      PIC 99.
       FD HAREKET-DETAY DATA RECORD IS HD-KAYIT.
       01 HD-KAYIT.
           02 HD-KEY.
               03 HD-TIP                PIC 99.
               03 HD-EVRAKNO            PIC X(15).
               03 HD-SIRA               PIC 9(10).
           02 HD-KATKEY.
               03 HD-KATNO              PIC 9999.
               03 HD-STNO               PIC X(15).
           02 HD-PRCNO                  PIC X(30).
           02 HD-TIPI                   PIC X(30).
           02 HD-CINSI                  PIC X(60).
           02 HD-MARKA                  PIC X(30).
           02 HD-ISKONTO1               PIC 9(4)V9999.
           02 HD-ISKONTO2               PIC 9(4)V9999.
           02 HD-KDV                    PIC 9(4)V9999.
           02 HD-ADET                   PIC 9(12)V9999.
           02 HD-FIYAT                  PIC 9(10)V9999.
           
       FD STOK DATA RECORD IS ST-KAYIT.
       01 ST-KAYIT.
           02 ST-KEY.
               03 ST-KATNO              PIC 9999.
               03 ST-STNO               PIC X(15).
           02 ST-PRCNO                  PIC X(30).
           02 ST-OEMNO                  PIC X(30).
           02 ST-TIPI                   PIC X(30).
           02 ST-CINSI                  PIC X(60).
           02 ST-MARKA                  PIC X(30).
           02 ST-MIN                    PIC 9(12)V9999.
           02 ST-MAX                    PIC 9(12)V9999.
           02 ST-FIYAT                  PIC 9(10)V9999.
           02 ST-NOTE                   PIC X(10).
           02 ST-DEG.
                 03 ST-YIL          PIC 9999.
                 03 ST-AY           PIC 99.
                 03 ST-GUN          PIC 99.
           02 ST-PAKET                  PIC 9(5).
       WORKING-STORAGE SECTION.
       77 SAAT          PIC 9(8).
       77 KATNO         PIC 9999.
       77 HD-KOD        PIC XX.
       01 SW-HIDE PIC 9(4) BINARY VALUE 0.
       copy 'status.cpy'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           CALL 'C$SHOW' USING SW-HIDE.
           CANCEL 'C$SHOW'.
           UNSTRING MAIN-ARGUMENT-VALUE
                DELIMITED BY ';' INTO SAAT, KATNO
           END-UNSTRING.
           MOVE SAAT TO EXLS-SAAT.
           MOVE 'LISAK' TO EXLS-TANIM.
           OPEN OUTPUT EXLS.
           OPEN INPUT STOK HAREKET-DETAY.
           MOVE KATNO TO ST-KATNO.
           MOVE SPACES TO ST-STNO.
           START STOK KEY IS NOT LESS THAN ST-KEY
            INVALID KEY GO STOK-SON.
       STOK-OKU.
           READ STOK NEXT AT END GO STOK-SON.
           IF ST-KATNO NOT = KATNO GO STOK-SON.
           MOVE ST-KATNO TO HD-KATNO.
           MOVE ST-STNO TO HD-STNO.
           
           START HAREKET-DETAY KEY IS EQUAL TO HD-KATKEY
            INVALID KEY MOVE '00' TO HD-KOD
            NOT INVALID KEY 
                MOVE '01' TO HD-KOD
           END-START.
           
           MOVE SPACES TO XLS.
           STRING HD-KOD ST-KAYIT
                DELIMITED BY SIZE INTO XLS.
           WRITE XLS.
           GO STOK-OKU.
       STOK-SON.
           CLOSE STOK HAREKET-DETAY.
           OPEN INPUT PASSTOK.
           MOVE KATNO TO PAS-KATNO.
           MOVE SPACES TO PAS-STNO.
           START PASSTOK KEY IS NOT LESS THAN PAS-KEY
                INVALID KEY GO PAS-STOK-SON.
       PAS-STOK-OKU.
           READ PASSTOK NEXT AT END GO PAS-STOK-SON.
           IF PAS-KATNO NOT = KATNO GO PAS-STOK-SON.
           MOVE '02' TO HD-KOD.
           STRING HD-KOD PAS-KAYIT
                DELIMITED BY SIZE INTO XLS.
           WRITE XLS.
           GO PAS-STOK-OKU.
       PAS-STOK-SON.
           CLOSE PASSTOK EXLS.
           EXIT PROGRAM.
           STOP RUN.
           