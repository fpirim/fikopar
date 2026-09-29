/*************************************************************************/
/*		    CLIB.C						 */
/*									 */
/*   Program written by: Liant Software Corp.	   Date: 91.07.01	 */
/*   Description:  Sample program to illustrate non-COBOL		 */
/*		   library construction for RM/COBOL-85.		 */
/*									 */
/*************************************************************************/

typedef unsigned char	BIT8;
typedef signed	 char	SBIT8;
typedef unsigned short	BIT16;
typedef 	 short	SBIT16;
typedef unsigned long	BIT32;
typedef 	 long	SBIT32;

#define FAR _far

typedef struct ArgumentEntry
{
    char FAR *a_address;	/* pointer to start of arg */
    BIT32     a_length; 	/* length of argument */
    BIT16     a_type;		/* Arg type See Table E-1 */
    BIT8      a_digits; 	/* digit count 0x1-0x12 */
    SBIT8     a_scale;		/* implied decimal location */
    char FAR *a_picture;	/* pointer to edit picture */
} ARGUMENT_ENTRY;

typedef struct {
 BIT16		TextCount;
 char		Text[100];
} TEXT;

BIT8 FIRST();
BIT8 SECOND();
int MoveString();
int ValidateAndConvertParameters();
void ConvertArgumentAddress();

BIT8 FIRST(pCalledPgmName, ArgCount, ArgEntry, InitialStateFlag)
TEXT	       *pCalledPgmName;
BIT16		ArgCount;
ARGUMENT_ENTRY	ArgEntry[];
BIT16		InitialStateFlag;
{
    ARGUMENT_ENTRY    *pStringValInfo;
    ARGUMENT_ENTRY    *pStringLenInfo;
    char	      *string = "First String";

    if (ValidateAndConvertParameters(ArgCount, ArgEntry))
	return (BIT8) -1;

     pStringValInfo = &(ArgEntry[0]);
     pStringLenInfo = &(ArgEntry[1]);

     return((BIT8) MoveString(string, pStringValInfo, pStringLenInfo));
}

BIT8 SECOND(pCalledPgmName, ArgCount, ArgEntry, InitialStateFlag)
TEXT		*pCalledPgmName;
BIT16		 ArgCount;
ARGUMENT_ENTRY	 ArgEntry[];
BIT16		 InitialStateFlag;
{
    ARGUMENT_ENTRY    *pStringValInfo;
    ARGUMENT_ENTRY    *pStringLenInfo;
    char	      *string = "Second String";

    if (ValidateAndConvertParameters(ArgCount, ArgEntry))
	return (BIT8) -1;

     pStringValInfo = &(ArgEntry[0]);
     pStringLenInfo = &(ArgEntry[1]);

     return((BIT8) MoveString(string, pStringValInfo, pStringLenInfo));
}

int ValidateAndConvertParameters(ArgCount, ArgEntry)
BIT16		ArgCount;
ARGUMENT_ENTRY	ArgEntry[];
{
    ARGUMENT_ENTRY	*pStringValInfo;
    ARGUMENT_ENTRY	*pStringLenInfo;

    if (ArgCount != 2)
	return -1;

    pStringValInfo = &(ArgEntry[0]);
    pStringLenInfo = &(ArgEntry[1]);

    if (pStringValInfo->a_length > 65280)
	return -1;

    if (pStringLenInfo->a_length != 2)
	return -1;

    ConvertArgumentAddress(&(pStringValInfo->a_address));
    ConvertArgumentAddress(&(pStringLenInfo->a_address));

    return 0;
}

void ConvertArgumentAddress(pAddress)
BIT32 *pAddress;
{
    BIT16	segment, offset;

    segment = (*pAddress & 0x000FFF00) >> 4;
    offset = *pAddress & 0x000000FF;
    *pAddress = ((long) segment << 16) | offset;
    return;
}

int MoveString(SrcString, pStringValInfo, pStringLenInfo)
char	       *SrcString;
ARGUMENT_ENTRY *pStringValInfo;
ARGUMENT_ENTRY *pStringLenInfo;
{
    char       *DestString;
    int 	i;
    int 	DestLen;
    int        *cp;

    DestString = pStringValInfo->a_address;
    DestLen = pStringValInfo->a_length;
    for (i = 0; *(SrcString); i++)
    {
	if (i > DestLen)
	    return -1;
	*(DestString++) = *(SrcString++);
    }

    cp = (int *) pStringLenInfo->a_address;
    *cp = (i >> 8) | (i << 8);
    for (; i < pStringValInfo->a_length; i++)
	*(DestString++) = ' ';

    return 0;
}