       IDENTIFICATION DIVISION.
       PROGRAM-ID. '_START'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'carhar.cpy'.
       copy 'stok.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'carhar.cpz'.
       copy 'stok.cpz'.
           
       WORKING-STORAGE SECTION.
       77 DUR           PIC X.
       77 SATIR         PIC 9999999.
       copy 'status.cpy'.
       PROCEDURE DIVISION.
       BASLA.
           DISPLAY SPACES ERASE.
      *     OPEN INPUT CARHAR.
           OPEN INPUT STOK.
           MOVE 1 TO SATIR.
           DISPLAY ALL '-' SIZE 10 LINE 1 POSITION 1 ' '
                   ALL '-' SIZE 8 ' ' ALL '-' SIZE 4 ' '
                   ALL '-' SIZE 10 ' ' ALL '-' SIZE 2 ' '
                   ALL '-' SIZE 15 ' ' ALL '-' SIZE 15 ' '
                   ALL '-' SIZE 15 ' ' ALL '-' SIZE 15 ' '
                   ALL '-' SIZE 15.
       OKU. 
           READ STOK NEXT AT END GO SON.
      *     DISPLAY CH-HESAPNO(1:10) LINE SATIR POSITION 1
      *          ' ' CH-TARIH ' ' CH-DEPONO ' ' CH-SIRA ' '
      *          CH-TIP ' ' CH-EVRAKNO ' ' CH-ACIKLAMA(1:15) ' '
      *          CH-VADE ' ' CH-BORC ' ' CH-ALACAK.
           DISPLAY SATIR LINE 25 POSITION 1.
           ADD 1 TO SATIR.
           GO OKU.
       SON.
      *    CLOSE CARHAR.
           CLOSE STOK.
           ACCEPT DUR NO BEEP.
           IF DUR = 'X' STOP RUN.
           GO BASLA.