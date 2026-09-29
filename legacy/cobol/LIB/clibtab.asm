         PAGE      58,132
;**************************************************************************
;*                  CLIBTAB.ASM                                           *
;*                                                                        *
;*   Program written by: Liant Software Corp.     Date: 91.07.01          *
;*   Description:  Sample program to illustrate non-COBOL                 *
;*                 library construction for RM/COBOL-85.                  *
;*                                                                        *
;**************************************************************************

       EXTRN   _FIRST:far
       EXTRN   _SECOND:far

LIB_DIRECTORY   SEGMENT BYTE    PUBLIC
        ASSUME  CS:LIB_DIRECTORY

PROCEDURE_TABLE:

        DB      'ASMLIB'

L1L     DB      L1A - L1L - 1
        DB      'FIRST-STRING'
L1A     DD      _FIRST

L2L     DB      L2A - L2L - 1
        DB      'SECOND-STRING'
L2A     DD      _SECOND

        DB      0

LIB_DIRECTORY   ENDS

        END     PROCEDURE_TABLE