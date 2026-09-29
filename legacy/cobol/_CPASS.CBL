       IDENTIFICATION DIVISION.
      *****************************************************************
      * This program is provided for demonstration
      * and educational purposes
      * only by Ryan McFarland Corporation (a division
      * of Liant Software).
      * It is neither supported nor warranted by Ryan
      * McFarland Corporation.
      * (c) 1992, 1995 Ryan McFarland Corporation.  All
      * rights reserved.
      *****************************************************************
       PROGRAM-ID. CPASS.
      * TO PASS THE RESULTS OF A SHELL COMMAND TO A COBOL PROGRAM
      * EXECUTE THIS PROGRAM AS FOLLOWS:
      * runcobol cpass -a "`tty`"
      * runcobol cpass -a "`echo $TERM`"
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77 dur           pic x.
       LINKAGE SECTION.
       01 MAIN-PARAMETER.
           02 PARAMETER-SIZE               PIC 9(4) BINARY.
           02 PARAMETER-STRING.
               03 FILLER PIC X OCCURS 1 TO 100
                  DEPENDING ON PARAMETER-SIZE.

       PROCEDURE DIVISION USING MAIN-PARAMETER.
       A.
           DISPLAY PARAMETER-STRING (1:PARAMETER-SIZE) LINE 22. 
           DISPLAY PARAMETER-SIZE LINE 23.
           accept dur no beep.
           STOP RUN.
