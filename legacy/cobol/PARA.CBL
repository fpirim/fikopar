       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'YAZIYLA'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 BIRLER.
           02 FILLER            PIC X(5) VALUE 'bir'.
           02 FILLER            PIC X(5) VALUE 'iki'.
           02 FILLER            PIC X(5) VALUE 'Åá'.
           02 FILLER            PIC X(5) VALUE 'dîrt'.
           02 FILLER            PIC X(5) VALUE 'beü'.
           02 FILLER            PIC X(5) VALUE 'altç'.
           02 FILLER            PIC X(5) VALUE 'yedi'.
           02 FILLER            PIC X(5) VALUE 'sekiz'.
           02 FILLER            PIC X(5) VALUE 'dokuz'.
       01 A REDEFINES BIRLER.
           02 TEKLI             PIC X(5) OCCURS 9 TIMES.
       01 ONLAR.
           02 FILLER            PIC X(6) VALUE 'on'.
           02 FILLER            PIC X(6) VALUE 'yirmi'.
           02 FILLER            PIC X(6) VALUE 'otuz'.
           02 FILLER            PIC X(6) VALUE 'kçrk'.
           02 FILLER            PIC X(6) VALUE 'elli'.
           02 FILLER            PIC X(6) VALUE 'altmçü'.
           02 FILLER            PIC X(6) VALUE 'yetmiü'.
           02 FILLER            PIC X(6) VALUE 'seksen'.
           02 FILLER            PIC X(6) VALUE 'doksan'.
       01 B REDEFINES ONLAR.
           02 IKILI             PIC X(6) OCCURS 9 TIMES.
       01 YUZLER.
           02 FILLER            PIC X(8) VALUE 'yÅz'.
           02 FILLER            PIC X(8) VALUE 'ikiyÅz'.
           02 FILLER            PIC X(8) VALUE 'ÅáyÅz'.
           02 FILLER            PIC X(8) VALUE 'dîrtyÅz'.
           02 FILLER            PIC X(8) VALUE 'beüyÅz'.
           02 FILLER            PIC X(8) VALUE 'altçyÅz'.
           02 FILLER            PIC X(8) VALUE 'yediyÅz'.
           02 FILLER            PIC X(8) VALUE 'sekizyÅz'.
           02 FILLER            PIC X(8) VALUE 'dokuzyÅz'.
       01 C REDEFINES YUZLER.
           02 UCLU              PIC X(8) OCCURS 9 TIMES.

       77 BIR           PIC X(8).
       77 TRILYON       PIC X(7) VALUE SPACES.
       77 IKI           PIC X(8).
       77 UC            PIC X(6).
       77 DORT          PIC X(5).
       77 MILYAR        PIC X(6) VALUE SPACES.
       77 BES           PIC X(8).
       77 ALTI          PIC X(6).
       77 YEDI          PIC X(5).
       77 MILYON        PIC X(6) VALUE SPACES.
       77 SEKIZ         PIC X(8).
       77 DOKUZ         PIC X(6).
       77 TEN           PIC X(5).
       77 BIN           PIC X(3) VALUE SPACES.
       77 ONBIR         PIC X(8).
       77 ONIKI         PIC X(6).
       77 ONUC          PIC X(5).
       77 LIRA          PIC X(4) VALUE SPACES.
       77 ONDORT        PIC X(6).
       77 ONBES         PIC X(5).
       77 KURUS         PIC X(5) VALUE SPACES.
       77 SS            PIC 9.
       77 DUR           PIC X.
       77 PARA          PIC 9(13)V99.
       01 D REDEFINES PARA.
           02 DIZI              PIC 9 OCCURS 15 TIMES.
       LINKAGE SECTION.
       77 SAYI          PIC 9(12)V99.
       77 YAZIYLA       PIC X(100).
       PROCEDURE DIVISION USING SAYI YAZIYLA.
       BASLA.
           MOVE SAYI TO PARA.
           IF DIZI(1) NOT = 0
                MOVE DIZI(1) TO SS
                MOVE UCLU(SS) TO BIR
                MOVE 'trilyon' TO TRILYON
                MOVE 'lira' TO LIRA.
           IF DIZI(2) NOT = 0
                MOVE DIZI(2) TO SS
                MOVE UCLU(SS) TO IKI
                MOVE 'milyar' TO MILYAR
                MOVE 'lira' TO LIRA.
           IF DIZI(3) NOT = 0
                MOVE DIZI(3) TO SS
                MOVE IKILI(SS) TO UC
                MOVE 'milyar' TO MILYAR
                MOVE 'lira' TO LIRA.
           IF DIZI(4) NOT = 0
                MOVE DIZI(4) TO SS
                MOVE TEKLI(SS) TO DORT
                MOVE 'milyar' TO MILYAR
                MOVE 'lira' TO LIRA.
           IF DIZI(5) NOT = 0
                MOVE DIZI(5) TO SS
                MOVE UCLU(SS) TO BES
                MOVE 'milyon' TO MILYON
                MOVE 'lira' TO LIRA.
           IF DIZI(6) NOT = 0
                MOVE DIZI(6) TO SS
                MOVE IKILI(SS) TO ALTI
                MOVE 'milyon' TO MILYON
                MOVE 'lira' TO LIRA.
           IF DIZI(7) NOT = 0
                MOVE DIZI(7) TO SS
                MOVE TEKLI(SS) TO YEDI
                MOVE 'milyon' TO MILYON
                MOVE 'lira' TO LIRA.
           IF DIZI(8) NOT = 0
                MOVE DIZI(8) TO SS
                MOVE UCLU(SS) TO SEKIZ
                MOVE 'bin' TO BIN
                MOVE 'lira' TO LIRA.
           IF DIZI(9) NOT = 0
                MOVE DIZI(9) TO SS
                MOVE IKILI(SS) TO DOKUZ
                MOVE 'bin' TO BIN
                MOVE 'lira' TO LIRA.
           IF DIZI(10) NOT = 0
                MOVE DIZI(10) TO SS
                MOVE TEKLI(SS) TO TEN  
                MOVE 'bin' TO BIN
                MOVE 'lira' TO LIRA.
           IF DIZI(11) NOT = 0
                MOVE DIZI(11) TO SS
                MOVE UCLU(SS) TO ONBIR
                MOVE 'lira' TO LIRA.
           IF DIZI(12) NOT = 0
                MOVE DIZI(12) TO SS
                MOVE IKILI(SS) TO ONIKI
                MOVE 'lira' TO LIRA.
           IF DIZI(13) NOT = 0
                MOVE DIZI(13) TO SS
                MOVE TEKLI(SS) TO ONUC
                MOVE 'lira' TO LIRA.
           IF DIZI(14) NOT = 0
                MOVE DIZI(14) TO SS
                MOVE IKILI(SS) TO ONDORT
                MOVE 'kuruü' TO KURUS.
           IF DIZI(15) NOT = 0
                MOVE DIZI(15) TO SS
                MOVE TEKLI(SS) TO ONBES
                MOVE 'kuruü' TO KURUS.

           STRING 'Y.' BIR TRILYON IKI UC DORT MILYAR
                BES ALTI YEDI MILYON SEKIZ DOKUZ TEN BIN
                ONBIR ONIKI ONUC LIRA ONDORT ONBES KURUS
                  DELIMITED BY SPACES INTO YAZIYLA.
