       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'STOKAKT'.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT NWSTOK ASSIGN TO DISK, 'DATA\STOKNW.DAT'
               ORGANIZATION INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS NW-KEY.

       copy 'stok.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       FD NWSTOK DATA RECORD IS NW-KAYIT.
       01 NW-KAYIT.
           02 NW-KEY.
               03 NW-KATNO              PIC 9999.
               03 NW-STNO               PIC X(15).
           02 NW-PRCNO                  PIC X(30).
           02 NW-OEMNO                  PIC X(30).
           02 NW-TIPI                   PIC X(30).
           02 NW-CINSI                  PIC X(60).
           02 NW-MARKA                  PIC X(30).
           02 NW-MIN                    PIC 9(12)V9999.
           02 NW-MAX                    PIC 9(12)V9999.
           02 NW-FIYAT                  PIC 9(10)V9999.
           02 NW-NOTE                   PIC X(10).
           02 NW-DEG.
                 03 NW-YIL          PIC 9999.
                 03 NW-AY           PIC 99.
                 03 NW-GUN          PIC 99.
           02 NW-PAKET                  PIC 9(5).
       
       WORKING-STORAGE SECTION.
       copy 'status.cpy'.
       PROCEDURE DIVISION.
       START-PROGRAM.
           OPEN OUTPUT STOK INPUT NWSTOK.
       BASLA.
           READ NWSTOK NEXT AT END CLOSE STOK NWSTOK GO SON.
           MOVE NW-KAYIT TO ST-KAYIT.
           MOVE ZEROS TO ST-SIRA.
           WRITE ST-KAYIT.
           GO BASLA.
       SON.
           STOP RUN.
           