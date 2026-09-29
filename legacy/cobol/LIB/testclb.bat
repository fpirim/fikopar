REM This batch file illustrates the steps necessary
REM to create a library of non-COBOL subprograms
REM written in C.

cl /c /Alfu /Gs clib.c
masm clibtab,,,,;
link clib+clibtab,,,,,
runcobol clibtest l=clib.exe