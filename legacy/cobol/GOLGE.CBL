       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'GOLGE'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 ws-screen-area.
            03 ws-screen-line      pic X(120) occurs 37 times.
       01 ws-char-line.
           03  ws-char       PIC X OCCURS 120 TIMES.
       01 DISPLAY-DESCRIPTION USAGE BINARY (2).
            03 DISPLAY-VERSION PIC 9(4) VALUE 0.
            03 DISPLAY-UNIT PIC 9(4) VALUE 0.
            03 DISPLAY-LINE PIC 9(4).
            03 DISPLAY-POSITION PIC 9(4).
            03 DISPLAY-LENGTH PIC 9(4).
            03 DISPLAY-EXCEPTION-CODE PIC 9(4).
            03 DISPLAY-EXCEPTION-VALUE PIC 9(4).
       
       77 X   PIC 9999.
       77 Y   PIC 9999.
       77 YSON PIC 9999.
       LINKAGE SECTION.
       01 Y1            PIC 9999.
       01 X1            PIC 9999.
       01 Y2            PIC 9999.
       01 X2            PIC 9999.
       PROCEDURE DIVISION USING Y1 X1 Y2 X2.
       START-PROGRAM.
           CALL "c$scrd" using ws-screen-area.
           CANCEL "c$scrd".
           
		   COMPUTE Y = Y2 + 1.
           move ws-screen-line(Y) to ws-char-line.
           
           COMPUTE X = X1 + 2. 
           PERFORM VARYING X FROM X BY 1 UNTIL X > X2
               DISPLAY ws-char(X) LINE Y POSITION X
                CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE, LOW' 
           END-PERFORM.
           
           COMPUTE Y = Y1 + 1.
           COMPUTE YSON = Y2 + 2.
           PERFORM VARYING Y FROM Y BY 1 UNTIL Y >= YSON
               MOVE ws-screen-line(Y) TO ws-char-line
               COMPUTE X = X2 + 1
               DISPLAY ws-char(X) LINE Y POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE, LOW' 
               ADD 1 TO X
               DISPLAY ws-char(X) LINE Y POSITION X
                 CONTROL 'BCOLOR=BLACK, FCOLOR=WHITE, LOW' 
           END-PERFORM.
           
           
           
      *     MOVE STARTLINE TO DISPLAY-LINE.
      *   MOVE STARTPOS TO DISPLAY-POSITION.
      *    MOVE WIDTH TO DISPLAY-LENGTH.
      *     call "c$scwr" using DISPLAY-DESCRIPTION ws-screen-line.
      *     cancel "c$scwr".
           EXIT PROGRAM.
           STOP RUN.
           
