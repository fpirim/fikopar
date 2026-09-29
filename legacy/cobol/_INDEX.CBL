       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'SICIL'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'stok.cpy'.
            SELECT IDX-FILE ASSIGN TO DISK, 'DATA\IDXCINSI.DAT'
                    ORGANIZATION INDEXED
                    ACCESS MODE IS DYNAMIC
                    ALTERNATE RECORD KEY IS IDX-KEY WITH DUPLICATES
                    RECORD KEY IS IDX-REC.                  
       DATA DIVISION.
       FILE SECTION.
       copy 'stok.cpz'.
       FD IDX-FILE DATA RECORD IS IDX-KAYIT.
       01 IDX-KAYIT.
           02 IDX-REC.
               03 IDX-VALUE     PIC X(30).
               03 IDX-KEY       PIC X(15).
       WORKING-STORAGE SECTION.
       77 DUR       PIC X.
       77 I         PIC 99.
       77 J         PIC 99.
       77 S         PIC 9(15) VALUE 0.
       77 T         PIC 9(15) VALUE 0.
       77 CUR       PIC 9(15) VALUE 0.
       77 SZ        PIC 9(15).
       
       01 SAAT1.
               02 SS1       PIC 99.
               02 DD1       PIC 99.
               02 SA1       PIC 99.
               02 SSS1      PIC 99.
       01 SAAT2.
               02 SS2       PIC 99.
               02 DD2       PIC 99.
               02 SA2       PIC 99.
               02 SSS2      PIC 99.
       77 RS                PIC 9(10)V9999999.       
       77 F                 PIC 9(10)V9999999.
       77 S1            PIC 9(10)V9999999.
       77 S2            PIC 9(10)V9999999.
       77 Z-RS          PIC Z,ZZZ,ZZZ.ZZZZZZ.
       copy 'status.cpy'.
       PROCEDURE DIVISION.
       BASLA.
      *     MOVE RECORD LENGTH OF STOK TO SZ.
           MOVE ZEROS TO Z-RS.
           ACCEPT SAAT1 FROM TIME.
           OPEN OUTPUT IDX-FILE. CLOSE IDX-FILE.
           OPEN INPUT STOK I-O IDX-FILE.
       OKU.    
           READ STOK NEXT AT END GO SON.
           MOVE ST-STNO TO IDX-KEY.
           PERFORM VARYING J FROM 1 BY 1 UNTIL J > 30
            PERFORM VARYING I FROM 1 BY J UNTIL I > 30
                MOVE ST-CINSI(I:J) TO IDX-VALUE
                WRITE IDX-KAYIT INVALID KEY CONTINUE END-WRITE
                ADD 1 TO S
            END-PERFORM
           END-PERFORM.
           ACCEPT SAAT2 FROM TIME.
           COMPUTE S2 = SS2 * 3600 + DD2 * 60 + SA2 + SS2 / 60.
           COMPUTE S1 = SS1 * 3600 + DD1 * 60 + SA1 + SS1 / 60.
           COMPUTE F = (S2 - S1).
           IF F > 1 
                 COMPUTE RS = (T - CUR) / F
                 MOVE RS TO Z-RS
                 MOVE SAAT2 TO SAAT1
                 MOVE T TO CUR
           END-IF.
           ADD 1 TO T.
           DISPLAY S LINE 25 POSITION 1 ' ' T ' ' Z-RS ' record/second.'.
           GO OKU.
       SON. 
           CLOSE IDX-FILE STOK.
           EXIT PROGRAM.
           STOP RUN.