/* A Bison parser, made by GNU Bison 1.875c.  */

/* Skeleton parser for Yacc-like parsing with Bison,
   Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003 Free Software Foundation, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

/* As a special exception, when this file is copied by Bison into a
   Bison output file, you may use that output file without restriction.
   This special exception was added by the Free Software Foundation
   in version 1.24 of Bison.  */

/* Written by Richard Stallman by simplifying the original so called
   ``semantic'' parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output.  */
#define YYBISON 1

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 0

/* Using locations.  */
#define YYLSP_NEEDED 0



/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     RW_REGISTER = 258,
     RW_STREAM = 259,
     RW_RELATION = 260,
     RW_ISTREAM = 261,
     RW_DSTREAM = 262,
     RW_RSTREAM = 263,
     RW_SELECT = 264,
     RW_DISTINCT = 265,
     RW_FROM = 266,
     RW_WHERE = 267,
     RW_GROUP = 268,
     RW_BY = 269,
     RW_AND = 270,
     RW_AS = 271,
     RW_UNION = 272,
     RW_EXCEPT = 273,
     RW_AVG = 274,
     RW_MIN = 275,
     RW_MAX = 276,
     RW_COUNT = 277,
     RW_SUM = 278,
     RW_ROWS = 279,
     RW_RANGE = 280,
     RW_NOW = 281,
     RW_PARTITION = 282,
     RW_UNBOUNDED = 283,
     RW_SECOND = 284,
     RW_MINUTE = 285,
     RW_HOUR = 286,
     RW_DAY = 287,
     T_EQ = 288,
     T_LT = 289,
     T_LE = 290,
     T_GT = 291,
     T_GE = 292,
     T_NE = 293,
     RW_INTEGER = 294,
     RW_FLOAT = 295,
     RW_CHAR = 296,
     RW_BYTE = 297,
     NOTOKEN = 298,
     T_INT = 299,
     T_REAL = 300,
     T_STRING = 301,
     T_QSTRING = 302
   };
#endif
#define RW_REGISTER 258
#define RW_STREAM 259
#define RW_RELATION 260
#define RW_ISTREAM 261
#define RW_DSTREAM 262
#define RW_RSTREAM 263
#define RW_SELECT 264
#define RW_DISTINCT 265
#define RW_FROM 266
#define RW_WHERE 267
#define RW_GROUP 268
#define RW_BY 269
#define RW_AND 270
#define RW_AS 271
#define RW_UNION 272
#define RW_EXCEPT 273
#define RW_AVG 274
#define RW_MIN 275
#define RW_MAX 276
#define RW_COUNT 277
#define RW_SUM 278
#define RW_ROWS 279
#define RW_RANGE 280
#define RW_NOW 281
#define RW_PARTITION 282
#define RW_UNBOUNDED 283
#define RW_SECOND 284
#define RW_MINUTE 285
#define RW_HOUR 286
#define RW_DAY 287
#define T_EQ 288
#define T_LT 289
#define T_LE 290
#define T_GT 291
#define T_GE 292
#define T_NE 293
#define RW_INTEGER 294
#define RW_FLOAT 295
#define RW_CHAR 296
#define RW_BYTE 297
#define NOTOKEN 298
#define T_INT 299
#define T_REAL 300
#define T_STRING 301
#define T_QSTRING 302




/* Copy the first part of user declarations.  */
#line 1 "parse.yy"

/**
 * parser.y: yacc specification for CQL
 *
 * Contributors: 
 *           Mark McAuliffe (University of Wisconsin - Madison, 1991)
 *           Dallan Quass, Jan Jannink, Jason McHugh (for Redbase)
 *           Shivnath Babu, Arvind Arasu (for STREAM)
 *
 */

#include <iostream>
#include <assert.h>
#include "parser/scan.h"
#include "parser/parser.h"
#include "parser/nodes.h"

static NODE* parse_tree; 




/* Enabling traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 0
#endif

#if ! defined (YYSTYPE) && ! defined (YYSTYPE_IS_DECLARED)
#line 23 "parse.yy"
typedef union YYSTYPE {
  int   ival;
  float rval;
  char *sval;
  NODE *node;
} YYSTYPE;
/* Line 191 of yacc.c.  */
#line 199 "parse.cc"
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
# define YYSTYPE_IS_TRIVIAL 1
#endif



/* Copy the second part of user declarations.  */


/* Line 214 of yacc.c.  */
#line 211 "parse.cc"

#if ! defined (yyoverflow) || YYERROR_VERBOSE

# ifndef YYFREE
#  define YYFREE free
# endif
# ifndef YYMALLOC
#  define YYMALLOC malloc
# endif

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   define YYSTACK_ALLOC alloca
#  endif
# else
#  if defined (alloca) || defined (_ALLOCA_H)
#   define YYSTACK_ALLOC alloca
#  else
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's `empty if-body' warning. */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (0)
# else
#  if defined (__STDC__) || defined (__cplusplus)
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   define YYSIZE_T size_t
#  endif
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
# endif
#endif /* ! defined (yyoverflow) || YYERROR_VERBOSE */


#if (! defined (yyoverflow) \
     && (! defined (__cplusplus) \
	 || (defined (YYSTYPE_IS_TRIVIAL) && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  short yyss;
  YYSTYPE yyvs;
  };

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (sizeof (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (sizeof (short) + sizeof (YYSTYPE))				\
      + YYSTACK_GAP_MAXIMUM)

/* Copy COUNT objects from FROM to TO.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined (__GNUC__) && 1 < __GNUC__
#   define YYCOPY(To, From, Count) \
      __builtin_memcpy (To, From, (Count) * sizeof (*(From)))
#  else
#   define YYCOPY(To, From, Count)		\
      do					\
	{					\
	  register YYSIZE_T yyi;		\
	  for (yyi = 0; yyi < (Count); yyi++)	\
	    (To)[yyi] = (From)[yyi];		\
	}					\
      while (0)
#  endif
# endif

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack)					\
    do									\
      {									\
	YYSIZE_T yynewbytes;						\
	YYCOPY (&yyptr->Stack, Stack, yysize);				\
	Stack = &yyptr->Stack;						\
	yynewbytes = yystacksize * sizeof (*Stack) + YYSTACK_GAP_MAXIMUM; \
	yyptr += yynewbytes / sizeof (*yyptr);				\
      }									\
    while (0)

#endif

#if defined (__STDC__) || defined (__cplusplus)
   typedef signed char yysigned_char;
#else
   typedef short yysigned_char;
#endif

/* YYFINAL -- State number of the termination state. */
#define YYFINAL  38
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   176

/* YYNTOKENS -- Number of terminals. */
#define YYNTOKENS  59
/* YYNNTS -- Number of nonterminals. */
#define YYNNTS  29
/* YYNRULES -- Number of rules. */
#define YYNRULES  81
/* YYNRULES -- Number of states. */
#define YYNSTATES  157

/* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
#define YYUNDEFTOK  2
#define YYMAXUTOK   302

#define YYTRANSLATE(YYX) 						\
  ((unsigned int) (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[YYLEX] -- Bison symbol number corresponding to YYLEX.  */
static const unsigned char yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
      53,    54,    50,    48,    55,    49,    56,    51,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,    52,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,    57,     2,    58,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47
};

#if YYDEBUG
/* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
   YYRHS.  */
static const unsigned short yyprhs[] =
{
       0,     0,     3,     6,     8,    10,    12,    19,    26,    30,
      32,    35,    38,    41,    47,    49,    54,    56,    61,    63,
      65,    67,    72,    76,    79,    83,    86,    89,    92,    94,
      98,   100,   104,   106,   108,   110,   115,   120,   125,   130,
     135,   140,   144,   146,   150,   152,   156,   158,   163,   170,
     172,   176,   179,   181,   184,   187,   193,   195,   198,   201,
     204,   207,   211,   213,   217,   221,   225,   229,   233,   237,
     239,   241,   245,   249,   253,   257,   261,   263,   265,   267,
     271,   275
};

/* YYRHS -- A `-1'-separated list of the rules' RHS. */
static const yysigned_char yyrhs[] =
{
      60,     0,    -1,    61,    52,    -1,    66,    -1,    62,    -1,
      63,    -1,     3,     4,    46,    53,    64,    54,    -1,     3,
       5,    46,    53,    64,    54,    -1,    65,    55,    64,    -1,
      65,    -1,    46,    39,    -1,    46,    40,    -1,    46,    42,
      -1,    46,    41,    53,    44,    54,    -1,    68,    -1,    67,
      53,    68,    54,    -1,    86,    -1,    67,    53,    86,    54,
      -1,     6,    -1,     7,    -1,     8,    -1,    69,    70,    71,
      72,    -1,     9,    10,    73,    -1,     9,    73,    -1,     9,
      10,    50,    -1,     9,    50,    -1,    11,    78,    -1,    12,
      82,    -1,    87,    -1,    13,    14,    77,    -1,    87,    -1,
      74,    55,    73,    -1,    74,    -1,    84,    -1,    75,    -1,
      22,    53,    76,    54,    -1,    22,    53,    50,    54,    -1,
      23,    53,    76,    54,    -1,    19,    53,    76,    54,    -1,
      21,    53,    76,    54,    -1,    20,    53,    76,    54,    -1,
      46,    56,    46,    -1,    46,    -1,    76,    55,    77,    -1,
      76,    -1,    79,    55,    78,    -1,    79,    -1,    46,    57,
      80,    58,    -1,    46,    57,    80,    58,    16,    46,    -1,
      46,    -1,    46,    16,    46,    -1,    25,    81,    -1,    26,
      -1,    24,    44,    -1,    25,    28,    -1,    27,    14,    77,
      24,    44,    -1,    44,    -1,    44,    29,    -1,    44,    30,
      -1,    44,    31,    -1,    44,    32,    -1,    83,    15,    82,
      -1,    83,    -1,    84,    34,    84,    -1,    84,    35,    84,
      -1,    84,    36,    84,    -1,    84,    37,    84,    -1,    84,
      33,    84,    -1,    84,    38,    84,    -1,    76,    -1,    85,
      -1,    84,    48,    84,    -1,    84,    49,    84,    -1,    84,
      50,    84,    -1,    84,    51,    84,    -1,    53,    84,    54,
      -1,    47,    -1,    44,    -1,    45,    -1,    46,    17,    46,
      -1,    46,    18,    46,    -1,    -1
};

/* YYRLINE[YYN] -- source line where rule number YYN was defined.  */
static const unsigned short yyrline[] =
{
       0,   117,   117,   121,   124,   127,   133,   138,   143,   146,
     151,   154,   157,   160,   166,   169,   172,   175,   180,   183,
     186,   191,   196,   199,   202,   205,   210,   215,   218,   223,
     226,   231,   234,   239,   242,   247,   250,   253,   256,   259,
     262,   267,   270,   275,   278,   283,   286,   291,   294,   297,
     300,   305,   308,   311,   314,   317,   322,   325,   328,   331,
     334,   339,   342,   348,   351,   354,   357,   360,   363,   369,
     372,   375,   378,   381,   384,   387,   392,   395,   398,   403,
     405,   409
};
#endif

#if YYDEBUG || YYERROR_VERBOSE
/* YYTNME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals. */
static const char *const yytname[] =
{
  "$end", "error", "$undefined", "RW_REGISTER", "RW_STREAM",
  "RW_RELATION", "RW_ISTREAM", "RW_DSTREAM", "RW_RSTREAM", "RW_SELECT",
  "RW_DISTINCT", "RW_FROM", "RW_WHERE", "RW_GROUP", "RW_BY", "RW_AND",
  "RW_AS", "RW_UNION", "RW_EXCEPT", "RW_AVG", "RW_MIN", "RW_MAX",
  "RW_COUNT", "RW_SUM", "RW_ROWS", "RW_RANGE", "RW_NOW", "RW_PARTITION",
  "RW_UNBOUNDED", "RW_SECOND", "RW_MINUTE", "RW_HOUR", "RW_DAY", "T_EQ",
  "T_LT", "T_LE", "T_GT", "T_GE", "T_NE", "RW_INTEGER", "RW_FLOAT",
  "RW_CHAR", "RW_BYTE", "NOTOKEN", "T_INT", "T_REAL", "T_STRING",
  "T_QSTRING", "'+'", "'-'", "'*'", "'/'", "';'", "'('", "')'", "','",
  "'.'", "'['", "']'", "$accept", "start", "command", "registerstream",
  "registerrelation", "non_mt_attrspec_list", "attrspec", "query",
  "xstream_clause", "sfw_block", "select_clause", "from_clause",
  "opt_where_clause", "opt_group_by_clause", "non_mt_projterm_list",
  "projterm", "aggr_expr", "attr", "non_mt_attr_list",
  "non_mt_relation_list", "relation_variable", "window_type", "time_spec",
  "non_mt_cond_list", "condition", "arith_expr", "const_value",
  "binary_op", "nothing", 0
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[YYLEX-NUM] -- Internal token number corresponding to
   token YYLEX-NUM.  */
static const unsigned short yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,    43,    45,
      42,    47,    59,    40,    41,    44,    46,    91,    93
};
# endif

/* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const unsigned char yyr1[] =
{
       0,    59,    60,    61,    61,    61,    62,    63,    64,    64,
      65,    65,    65,    65,    66,    66,    66,    66,    67,    67,
      67,    68,    69,    69,    69,    69,    70,    71,    71,    72,
      72,    73,    73,    74,    74,    75,    75,    75,    75,    75,
      75,    76,    76,    77,    77,    78,    78,    79,    79,    79,
      79,    80,    80,    80,    80,    80,    81,    81,    81,    81,
      81,    82,    82,    83,    83,    83,    83,    83,    83,    84,
      84,    84,    84,    84,    84,    84,    85,    85,    85,    86,
      86,    87
};

/* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
static const unsigned char yyr2[] =
{
       0,     2,     2,     1,     1,     1,     6,     6,     3,     1,
       2,     2,     2,     5,     1,     4,     1,     4,     1,     1,
       1,     4,     3,     2,     3,     2,     2,     2,     1,     3,
       1,     3,     1,     1,     1,     4,     4,     4,     4,     4,
       4,     3,     1,     3,     1,     3,     1,     4,     6,     1,
       3,     2,     1,     2,     2,     5,     1,     2,     2,     2,
       2,     3,     1,     3,     3,     3,     3,     3,     3,     1,
       1,     3,     3,     3,     3,     3,     1,     1,     1,     3,
       3,     0
};

/* YYDEFACT[STATE-NAME] -- Default rule to reduce with in state
   STATE-NUM when YYTABLE doesn't specify something else to do.  Zero
   means the default is an error.  */
static const unsigned char yydefact[] =
{
       0,     0,    18,    19,    20,     0,     0,     0,     0,     4,
       5,     3,     0,    14,     0,    16,     0,     0,     0,     0,
       0,     0,     0,     0,    77,    78,    42,    76,    25,     0,
      23,    32,    34,    69,    33,    70,     0,     0,     1,     2,
       0,     0,    81,     0,     0,    24,    22,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    79,
      80,     0,     0,    49,    26,    46,     0,    81,    28,     0,
       0,     0,     0,     0,     0,     0,     0,    41,    75,    31,
      71,    72,    73,    74,    15,    17,     0,     0,     0,    27,
      62,     0,     0,    21,    30,     0,     0,     9,     0,    38,
      40,    39,    36,    35,    37,    50,     0,     0,    52,     0,
       0,    45,     0,     0,     0,     0,     0,     0,     0,     0,
      10,    11,     0,    12,     6,     0,     7,    53,    54,    56,
      51,     0,    47,    61,    67,    63,    64,    65,    66,    68,
      44,    29,     0,     8,    57,    58,    59,    60,     0,     0,
       0,     0,     0,    48,    43,    13,    55
};

/* YYDEFGOTO[NTERM-NUM]. */
static const short yydefgoto[] =
{
      -1,     7,     8,     9,    10,    96,    97,    11,    12,    13,
      14,    42,    67,    93,    30,    31,    32,    33,   141,    64,
      65,   110,   130,    89,    90,    34,    35,    15,    68
};

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
#define YYPACT_NINF -124
static const short yypact[] =
{
       4,    31,  -124,  -124,  -124,    -1,    30,    17,   -22,  -124,
    -124,  -124,     3,  -124,    56,  -124,   -18,    23,    18,    28,
      33,    34,    42,    43,  -124,  -124,    27,  -124,  -124,    13,
    -124,    47,  -124,  -124,    66,  -124,    39,    51,  -124,  -124,
       7,    58,    93,    53,    69,  -124,  -124,    89,    89,    89,
      32,    89,    90,   -25,    54,    13,    13,    13,    13,  -124,
    -124,    83,    84,    -2,  -124,    85,    13,   126,  -124,    95,
      95,    91,    92,    96,    97,    98,    99,  -124,  -124,  -124,
      29,    29,  -124,  -124,  -124,  -124,   101,    94,    58,  -124,
     127,    75,   129,  -124,  -124,    88,   100,   103,   105,  -124,
    -124,  -124,  -124,  -124,  -124,  -124,   104,    26,  -124,   130,
     106,  -124,    13,    13,    13,    13,    13,    13,    13,    89,
    -124,  -124,   107,  -124,  -124,    95,  -124,  -124,  -124,   102,
    -124,    89,   133,  -124,    66,    66,    66,    66,    66,    66,
     108,  -124,   111,  -124,  -124,  -124,  -124,  -124,   132,   115,
      89,   112,   113,  -124,  -124,  -124,  -124
};

/* YYPGOTO[NTERM-NUM].  */
static const short yypgoto[] =
{
    -124,  -124,  -124,  -124,  -124,   -64,  -124,  -124,  -124,   122,
    -124,  -124,  -124,  -124,    -3,  -124,  -124,   -47,  -123,    77,
    -124,  -124,  -124,    55,  -124,   -24,  -124,   128,   109
};

/* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule which
   number is the opposite.  If zero, do what YYDEFACT says.
   If YYTABLE_NINF, syntax error.  */
#define YYTABLE_NINF -1
static const unsigned char yytable[] =
{
      71,    72,    73,    75,    76,    53,    98,     1,   148,    18,
       2,     3,     4,     5,    86,    46,     5,    38,    19,    20,
      21,    22,    23,    55,    56,    57,    58,   154,    43,    78,
      39,    80,    81,    82,    83,    16,    17,    19,    20,    21,
      22,    23,    91,    24,    25,    26,    27,    36,    37,    28,
       6,    79,    29,     6,   128,    87,    40,    24,    25,    26,
      27,   143,    24,    25,    26,    27,    29,    41,    45,    44,
     129,    29,   140,    19,    20,    21,    22,    23,    26,    57,
      58,    47,    74,    52,   140,    59,    48,    49,    91,   134,
     135,   136,   137,   138,   139,    50,    51,    60,    24,    25,
      26,    27,    54,   140,    63,    66,    69,    29,   113,   114,
     115,   116,   117,   118,    55,    56,    57,    58,   106,   107,
     108,   109,    70,    55,    56,    57,    58,   120,   121,   122,
     123,   144,   145,   146,   147,    26,    77,    84,    85,    92,
      88,    95,   112,   119,   131,    99,   100,   105,   127,   149,
     101,   102,   103,   104,   124,   151,   152,   156,   125,   126,
     142,   153,    61,   150,   132,   111,   155,   133,    62,     0,
       0,     0,     0,     0,     0,     0,    94
};

static const short yycheck[] =
{
      47,    48,    49,    50,    51,    29,    70,     3,   131,    10,
       6,     7,     8,     9,    16,    18,     9,     0,    19,    20,
      21,    22,    23,    48,    49,    50,    51,   150,    46,    54,
      52,    55,    56,    57,    58,     4,     5,    19,    20,    21,
      22,    23,    66,    44,    45,    46,    47,    17,    18,    50,
      46,    54,    53,    46,    28,    57,    53,    44,    45,    46,
      47,   125,    44,    45,    46,    47,    53,    11,    50,    46,
      44,    53,   119,    19,    20,    21,    22,    23,    46,    50,
      51,    53,    50,    56,   131,    46,    53,    53,   112,   113,
     114,   115,   116,   117,   118,    53,    53,    46,    44,    45,
      46,    47,    55,   150,    46,    12,    53,    53,    33,    34,
      35,    36,    37,    38,    48,    49,    50,    51,    24,    25,
      26,    27,    53,    48,    49,    50,    51,    39,    40,    41,
      42,    29,    30,    31,    32,    46,    46,    54,    54,    13,
      55,    46,    15,    14,    14,    54,    54,    46,    44,    16,
      54,    54,    54,    54,    54,    44,    24,    44,    55,    54,
      53,    46,    40,    55,    58,    88,    54,   112,    40,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    67
};

/* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
   symbol of state STATE-NUM.  */
static const unsigned char yystos[] =
{
       0,     3,     6,     7,     8,     9,    46,    60,    61,    62,
      63,    66,    67,    68,    69,    86,     4,     5,    10,    19,
      20,    21,    22,    23,    44,    45,    46,    47,    50,    53,
      73,    74,    75,    76,    84,    85,    17,    18,     0,    52,
      53,    11,    70,    46,    46,    50,    73,    53,    53,    53,
      53,    53,    56,    84,    55,    48,    49,    50,    51,    46,
      46,    68,    86,    46,    78,    79,    12,    71,    87,    53,
      53,    76,    76,    76,    50,    76,    76,    46,    54,    73,
      84,    84,    84,    84,    54,    54,    16,    57,    55,    82,
      83,    84,    13,    72,    87,    46,    64,    65,    64,    54,
      54,    54,    54,    54,    54,    46,    24,    25,    26,    27,
      80,    78,    15,    33,    34,    35,    36,    37,    38,    14,
      39,    40,    41,    42,    54,    55,    54,    44,    28,    44,
      81,    14,    58,    82,    84,    84,    84,    84,    84,    84,
      76,    77,    53,    64,    29,    30,    31,    32,    77,    16,
      55,    44,    24,    46,    77,    54,    44
};

#if ! defined (YYSIZE_T) && defined (__SIZE_TYPE__)
# define YYSIZE_T __SIZE_TYPE__
#endif
#if ! defined (YYSIZE_T) && defined (size_t)
# define YYSIZE_T size_t
#endif
#if ! defined (YYSIZE_T)
# if defined (__STDC__) || defined (__cplusplus)
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# endif
#endif
#if ! defined (YYSIZE_T)
# define YYSIZE_T unsigned int
#endif

#define yyerrok		(yyerrstatus = 0)
#define yyclearin	(yychar = YYEMPTY)
#define YYEMPTY		(-2)
#define YYEOF		0

#define YYACCEPT	goto yyacceptlab
#define YYABORT		goto yyabortlab
#define YYERROR		goto yyerrorlab


/* Like YYERROR except do call yyerror.  This remains here temporarily
   to ease the transition to the new meaning of YYERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */

#define YYFAIL		goto yyerrlab

#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)					\
do								\
  if (yychar == YYEMPTY && yylen == 1)				\
    {								\
      yychar = (Token);						\
      yylval = (Value);						\
      yytoken = YYTRANSLATE (yychar);				\
      YYPOPSTACK;						\
      goto yybackup;						\
    }								\
  else								\
    { 								\
      yyerror ("syntax error: cannot back up");\
      YYERROR;							\
    }								\
while (0)

#define YYTERROR	1
#define YYERRCODE	256

/* YYLLOC_DEFAULT -- Compute the default location (before the actions
   are run).  */

#ifndef YYLLOC_DEFAULT
# define YYLLOC_DEFAULT(Current, Rhs, N)		\
   ((Current).first_line   = (Rhs)[1].first_line,	\
    (Current).first_column = (Rhs)[1].first_column,	\
    (Current).last_line    = (Rhs)[N].last_line,	\
    (Current).last_column  = (Rhs)[N].last_column)
#endif

/* YYLEX -- calling `yylex' with the right arguments.  */

#ifdef YYLEX_PARAM
# define YYLEX yylex (YYLEX_PARAM)
#else
# define YYLEX yylex ()
#endif

/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)			\
do {						\
  if (yydebug)					\
    YYFPRINTF Args;				\
} while (0)

# define YYDSYMPRINT(Args)			\
do {						\
  if (yydebug)					\
    yysymprint Args;				\
} while (0)

# define YYDSYMPRINTF(Title, Token, Value, Location)		\
do {								\
  if (yydebug)							\
    {								\
      YYFPRINTF (stderr, "%s ", Title);				\
      yysymprint (stderr, 					\
                  Token, Value);	\
      YYFPRINTF (stderr, "\n");					\
    }								\
} while (0)

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

#if defined (__STDC__) || defined (__cplusplus)
static void
yy_stack_print (short *bottom, short *top)
#else
static void
yy_stack_print (bottom, top)
    short *bottom;
    short *top;
#endif
{
  YYFPRINTF (stderr, "Stack now");
  for (/* Nothing. */; bottom <= top; ++bottom)
    YYFPRINTF (stderr, " %d", *bottom);
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)				\
do {								\
  if (yydebug)							\
    yy_stack_print ((Bottom), (Top));				\
} while (0)


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

#if defined (__STDC__) || defined (__cplusplus)
static void
yy_reduce_print (int yyrule)
#else
static void
yy_reduce_print (yyrule)
    int yyrule;
#endif
{
  int yyi;
  unsigned int yylno = yyrline[yyrule];
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %u), ",
             yyrule - 1, yylno);
  /* Print the symbols being reduced, and their result.  */
  for (yyi = yyprhs[yyrule]; 0 <= yyrhs[yyi]; yyi++)
    YYFPRINTF (stderr, "%s ", yytname [yyrhs[yyi]]);
  YYFPRINTF (stderr, "-> %s\n", yytname [yyr1[yyrule]]);
}

# define YY_REDUCE_PRINT(Rule)		\
do {					\
  if (yydebug)				\
    yy_reduce_print (Rule);		\
} while (0)

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args)
# define YYDSYMPRINT(Args)
# define YYDSYMPRINTF(Title, Token, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef	YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   SIZE_MAX < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#if defined (YYMAXDEPTH) && YYMAXDEPTH == 0
# undef YYMAXDEPTH
#endif

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif



#if YYERROR_VERBOSE

# ifndef yystrlen
#  if defined (__GLIBC__) && defined (_STRING_H)
#   define yystrlen strlen
#  else
/* Return the length of YYSTR.  */
static YYSIZE_T
#   if defined (__STDC__) || defined (__cplusplus)
yystrlen (const char *yystr)
#   else
yystrlen (yystr)
     const char *yystr;
#   endif
{
  register const char *yys = yystr;

  while (*yys++ != '\0')
    continue;

  return yys - yystr - 1;
}
#  endif
# endif

# ifndef yystpcpy
#  if defined (__GLIBC__) && defined (_STRING_H) && defined (_GNU_SOURCE)
#   define yystpcpy stpcpy
#  else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
static char *
#   if defined (__STDC__) || defined (__cplusplus)
yystpcpy (char *yydest, const char *yysrc)
#   else
yystpcpy (yydest, yysrc)
     char *yydest;
     const char *yysrc;
#   endif
{
  register char *yyd = yydest;
  register const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
#  endif
# endif

#endif /* !YYERROR_VERBOSE */



#if YYDEBUG
/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

#if defined (__STDC__) || defined (__cplusplus)
static void
yysymprint (FILE *yyoutput, int yytype, YYSTYPE *yyvaluep)
#else
static void
yysymprint (yyoutput, yytype, yyvaluep)
    FILE *yyoutput;
    int yytype;
    YYSTYPE *yyvaluep;
#endif
{
  /* Pacify ``unused variable'' warnings.  */
  (void) yyvaluep;

  if (yytype < YYNTOKENS)
    {
      YYFPRINTF (yyoutput, "token %s (", yytname[yytype]);
# ifdef YYPRINT
      YYPRINT (yyoutput, yytoknum[yytype], *yyvaluep);
# endif
    }
  else
    YYFPRINTF (yyoutput, "nterm %s (", yytname[yytype]);

  switch (yytype)
    {
      default:
        break;
    }
  YYFPRINTF (yyoutput, ")");
}

#endif /* ! YYDEBUG */
/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

#if defined (__STDC__) || defined (__cplusplus)
static void
yydestruct (int yytype, YYSTYPE *yyvaluep)
#else
static void
yydestruct (yytype, yyvaluep)
    int yytype;
    YYSTYPE *yyvaluep;
#endif
{
  /* Pacify ``unused variable'' warnings.  */
  (void) yyvaluep;

  switch (yytype)
    {

      default:
        break;
    }
}


/* Prevent warnings from -Wmissing-prototypes.  */

#ifdef YYPARSE_PARAM
# if defined (__STDC__) || defined (__cplusplus)
int yyparse (void *YYPARSE_PARAM);
# else
int yyparse ();
# endif
#else /* ! YYPARSE_PARAM */
#if defined (__STDC__) || defined (__cplusplus)
int yyparse (void);
#else
int yyparse ();
#endif
#endif /* ! YYPARSE_PARAM */



/* The lookahead symbol.  */
int yychar;

/* The semantic value of the lookahead symbol.  */
YYSTYPE yylval;

/* Number of syntax errors so far.  */
int yynerrs;



/*----------.
| yyparse.  |
`----------*/

#ifdef YYPARSE_PARAM
# if defined (__STDC__) || defined (__cplusplus)
int yyparse (void *YYPARSE_PARAM)
# else
int yyparse (YYPARSE_PARAM)
  void *YYPARSE_PARAM;
# endif
#else /* ! YYPARSE_PARAM */
#if defined (__STDC__) || defined (__cplusplus)
int
yyparse (void)
#else
int
yyparse ()

#endif
#endif
{
  
  register int yystate;
  register int yyn;
  int yyresult;
  /* Number of tokens to shift before error messages enabled.  */
  int yyerrstatus;
  /* Lookahead token as an internal (translated) token number.  */
  int yytoken = 0;

  /* Three stacks and their tools:
     `yyss': related to states,
     `yyvs': related to semantic values,
     `yyls': related to locations.

     Refer to the stacks thru separate pointers, to allow yyoverflow
     to reallocate them elsewhere.  */

  /* The state stack.  */
  short	yyssa[YYINITDEPTH];
  short *yyss = yyssa;
  register short *yyssp;

  /* The semantic value stack.  */
  YYSTYPE yyvsa[YYINITDEPTH];
  YYSTYPE *yyvs = yyvsa;
  register YYSTYPE *yyvsp;



#define YYPOPSTACK   (yyvsp--, yyssp--)

  YYSIZE_T yystacksize = YYINITDEPTH;

  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;


  /* When reducing, the number of symbols on the RHS of the reduced
     rule.  */
  int yylen;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY;		/* Cause a token to be read.  */

  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */

  yyssp = yyss;
  yyvsp = yyvs;

  goto yysetstate;

/*------------------------------------------------------------.
| yynewstate -- Push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
 yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed. so pushing a state here evens the stacks.
     */
  yyssp++;

 yysetstate:
  *yyssp = yystate;

  if (yyss + yystacksize - 1 <= yyssp)
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYSIZE_T yysize = yyssp - yyss + 1;

#ifdef yyoverflow
      {
	/* Give user a chance to reallocate the stack. Use copies of
	   these so that the &'s don't force the real ones into
	   memory.  */
	YYSTYPE *yyvs1 = yyvs;
	short *yyss1 = yyss;


	/* Each stack pointer address is followed by the size of the
	   data in use in that stack, in bytes.  This used to be a
	   conditional around just the two extra args, but that might
	   be undefined if yyoverflow is a macro.  */
	yyoverflow ("parser stack overflow",
		    &yyss1, yysize * sizeof (*yyssp),
		    &yyvs1, yysize * sizeof (*yyvsp),

		    &yystacksize);

	yyss = yyss1;
	yyvs = yyvs1;
      }
#else /* no yyoverflow */
# ifndef YYSTACK_RELOCATE
      goto yyoverflowlab;
# else
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
	goto yyoverflowlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
	yystacksize = YYMAXDEPTH;

      {
	short *yyss1 = yyss;
	union yyalloc *yyptr =
	  (union yyalloc *) YYSTACK_ALLOC (YYSTACK_BYTES (yystacksize));
	if (! yyptr)
	  goto yyoverflowlab;
	YYSTACK_RELOCATE (yyss);
	YYSTACK_RELOCATE (yyvs);

#  undef YYSTACK_RELOCATE
	if (yyss1 != yyssa)
	  YYSTACK_FREE (yyss1);
      }
# endif
#endif /* no yyoverflow */

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;


      YYDPRINTF ((stderr, "Stack size increased to %lu\n",
		  (unsigned long int) yystacksize));

      if (yyss + yystacksize - 1 <= yyssp)
	YYABORT;
    }

  YYDPRINTF ((stderr, "Entering state %d\n", yystate));

  goto yybackup;

/*-----------.
| yybackup.  |
`-----------*/
yybackup:

/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* yyresume: */

  /* First try to decide what to do without reference to lookahead token.  */

  yyn = yypact[yystate];
  if (yyn == YYPACT_NINF)
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either YYEMPTY or YYEOF or a valid lookahead symbol.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token: "));
      yychar = YYLEX;
    }

  if (yychar <= YYEOF)
    {
      yychar = yytoken = YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YYDSYMPRINTF ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yyn == 0 || yyn == YYTABLE_NINF)
	goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  if (yyn == YYFINAL)
    YYACCEPT;

  /* Shift the lookahead token.  */
  YYDPRINTF ((stderr, "Shifting token %s, ", yytname[yytoken]));

  /* Discard the token being shifted unless it is eof.  */
  if (yychar != YYEOF)
    yychar = YYEMPTY;

  *++yyvsp = yylval;


  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  yystate = yyn;
  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- Do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     `$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];


  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
        case 2:
#line 117 "parse.yy"
    { parse_tree = yyvsp[-1].node; YYACCEPT; }
    break;

  case 3:
#line 122 "parse.yy"
    { yyval.node = yyvsp[0].node;}
    break;

  case 4:
#line 125 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 5:
#line 128 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 6:
#line 134 "parse.yy"
    {yyval.node = str_spec_node(yyvsp[-3].sval, yyvsp[-1].node);}
    break;

  case 7:
#line 139 "parse.yy"
    {yyval.node = rel_spec_node(yyvsp[-3].sval, yyvsp[-1].node);}
    break;

  case 8:
#line 144 "parse.yy"
    {yyval.node = prepend(yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 9:
#line 147 "parse.yy"
    {yyval.node = list_node(yyvsp[0].node);}
    break;

  case 10:
#line 152 "parse.yy"
    {yyval.node = int_attr_spec_node(yyvsp[-1].sval);}
    break;

  case 11:
#line 155 "parse.yy"
    {yyval.node = float_attr_spec_node(yyvsp[-1].sval);}
    break;

  case 12:
#line 158 "parse.yy"
    {yyval.node = byte_attr_spec_node(yyvsp[-1].sval);}
    break;

  case 13:
#line 161 "parse.yy"
    {yyval.node = char_attr_spec_node(yyvsp[-4].sval, yyvsp[-1].ival);}
    break;

  case 14:
#line 167 "parse.yy"
    {yyval.node = sfw_cont_query_node (yyvsp[0].node, 0);}
    break;

  case 15:
#line 170 "parse.yy"
    {yyval.node = sfw_cont_query_node (yyvsp[-1].node, yyvsp[-3].node);}
    break;

  case 16:
#line 173 "parse.yy"
    {yyval.node = bin_cont_query_node (yyvsp[0].node, 0);}
    break;

  case 17:
#line 176 "parse.yy"
    {yyval.node = bin_cont_query_node (yyvsp[-1].node, yyvsp[-3].node);}
    break;

  case 18:
#line 181 "parse.yy"
    {yyval.node = istream_node();}
    break;

  case 19:
#line 184 "parse.yy"
    {yyval.node = dstream_node();}
    break;

  case 20:
#line 187 "parse.yy"
    {yyval.node = rstream_node();}
    break;

  case 21:
#line 192 "parse.yy"
    {yyval.node = sfw_block_node(yyvsp[-3].node,yyvsp[-2].node,yyvsp[-1].node,yyvsp[0].node);}
    break;

  case 22:
#line 197 "parse.yy"
    {yyval.node = select_clause_node(true, yyvsp[0].node);}
    break;

  case 23:
#line 200 "parse.yy"
    {yyval.node = select_clause_node(false, yyvsp[0].node);}
    break;

  case 24:
#line 203 "parse.yy"
    {yyval.node = select_clause_node(true, 0);}
    break;

  case 25:
#line 206 "parse.yy"
    {yyval.node = select_clause_node(false, 0);}
    break;

  case 26:
#line 211 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 27:
#line 216 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 28:
#line 219 "parse.yy"
    {yyval.node = 0;}
    break;

  case 29:
#line 224 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 30:
#line 227 "parse.yy"
    {yyval.node = 0;}
    break;

  case 31:
#line 232 "parse.yy"
    {yyval.node = prepend(yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 32:
#line 235 "parse.yy"
    {yyval.node = list_node(yyvsp[0].node);}
    break;

  case 33:
#line 240 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 34:
#line 243 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 35:
#line 248 "parse.yy"
    {yyval.node = aggr_expr_node(COUNT, yyvsp[-1].node);}
    break;

  case 36:
#line 251 "parse.yy"
    {yyval.node = aggr_expr_node (COUNT, 0);}
    break;

  case 37:
#line 254 "parse.yy"
    {yyval.node = aggr_expr_node(SUM, yyvsp[-1].node);}
    break;

  case 38:
#line 257 "parse.yy"
    {yyval.node = aggr_expr_node(AVG, yyvsp[-1].node);}
    break;

  case 39:
#line 260 "parse.yy"
    {yyval.node = aggr_expr_node(MAX, yyvsp[-1].node);}
    break;

  case 40:
#line 263 "parse.yy"
    {yyval.node = aggr_expr_node(MIN, yyvsp[-1].node);}
    break;

  case 41:
#line 268 "parse.yy"
    {yyval.node = attr_ref_node(yyvsp[-2].sval, yyvsp[0].sval);}
    break;

  case 42:
#line 271 "parse.yy"
    {yyval.node = attr_ref_node(0, yyvsp[0].sval);}
    break;

  case 43:
#line 276 "parse.yy"
    {yyval.node = prepend(yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 44:
#line 279 "parse.yy"
    {yyval.node = list_node(yyvsp[0].node);}
    break;

  case 45:
#line 284 "parse.yy"
    {yyval.node = prepend(yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 46:
#line 287 "parse.yy"
    {yyval.node = list_node(yyvsp[0].node);}
    break;

  case 47:
#line 292 "parse.yy"
    {yyval.node = rel_var_node(yyvsp[-3].sval, 0, yyvsp[-1].node);}
    break;

  case 48:
#line 295 "parse.yy"
    {yyval.node = rel_var_node(yyvsp[-5].sval, yyvsp[0].sval, yyvsp[-3].node);}
    break;

  case 49:
#line 298 "parse.yy"
    {yyval.node = rel_var_node(yyvsp[0].sval, 0, 0);}
    break;

  case 50:
#line 301 "parse.yy"
    {yyval.node = rel_var_node(yyvsp[-2].sval, yyvsp[0].sval, 0);}
    break;

  case 51:
#line 306 "parse.yy"
    {yyval.node = time_win_node(yyvsp[0].node);}
    break;

  case 52:
#line 309 "parse.yy"
    {yyval.node = now_win_node();}
    break;

  case 53:
#line 312 "parse.yy"
    {yyval.node = row_win_node(yyvsp[0].ival);}
    break;

  case 54:
#line 315 "parse.yy"
    {yyval.node = unbounded_win_node();}
    break;

  case 55:
#line 318 "parse.yy"
    {yyval.node = part_win_node(yyvsp[-2].node, yyvsp[0].ival);}
    break;

  case 56:
#line 323 "parse.yy"
    {yyval.node = time_spec_node(Parser::NOTIMEUNIT, yyvsp[0].ival);}
    break;

  case 57:
#line 326 "parse.yy"
    {yyval.node = time_spec_node(Parser::SECOND, yyvsp[-1].ival);}
    break;

  case 58:
#line 329 "parse.yy"
    {yyval.node = time_spec_node(Parser::MINUTE, yyvsp[-1].ival);}
    break;

  case 59:
#line 332 "parse.yy"
    {yyval.node = time_spec_node(Parser::HOUR, yyvsp[-1].ival);}
    break;

  case 60:
#line 335 "parse.yy"
    {yyval.node = time_spec_node(Parser::DAY, yyvsp[-1].ival);}
    break;

  case 61:
#line 340 "parse.yy"
    {yyval.node = prepend(yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 62:
#line 343 "parse.yy"
    {yyval.node = list_node(yyvsp[0].node);}
    break;

  case 63:
#line 349 "parse.yy"
    {yyval.node = condn_node(LT, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 64:
#line 352 "parse.yy"
    {yyval.node = condn_node(LE, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 65:
#line 355 "parse.yy"
    {yyval.node = condn_node(GT, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 66:
#line 358 "parse.yy"
    {yyval.node = condn_node(GE, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 67:
#line 361 "parse.yy"
    {yyval.node = condn_node(EQ, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 68:
#line 364 "parse.yy"
    {yyval.node = condn_node(NE, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 69:
#line 370 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 70:
#line 373 "parse.yy"
    {yyval.node = yyvsp[0].node;}
    break;

  case 71:
#line 376 "parse.yy"
    {yyval.node = arith_expr_node(ADD, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 72:
#line 379 "parse.yy"
    {yyval.node = arith_expr_node(SUB, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 73:
#line 382 "parse.yy"
    {yyval.node = arith_expr_node(MUL, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 74:
#line 385 "parse.yy"
    {yyval.node = arith_expr_node(DIV, yyvsp[-2].node, yyvsp[0].node);}
    break;

  case 75:
#line 388 "parse.yy"
    {yyval.node = yyvsp[-1].node;}
    break;

  case 76:
#line 393 "parse.yy"
    {yyval.node = str_val_node(yyvsp[0].sval);}
    break;

  case 77:
#line 396 "parse.yy"
    {yyval.node = int_val_node(yyvsp[0].ival);}
    break;

  case 78:
#line 399 "parse.yy"
    {yyval.node = flt_val_node(yyvsp[0].rval);}
    break;

  case 79:
#line 404 "parse.yy"
    {yyval.node = union_node (yyvsp[-2].sval, yyvsp[0].sval);}
    break;

  case 80:
#line 406 "parse.yy"
    {yyval.node = except_node (yyvsp[-2].sval, yyvsp[0].sval);}
    break;


    }

/* Line 1000 of yacc.c.  */
#line 1641 "parse.cc"

  yyvsp -= yylen;
  yyssp -= yylen;


  YY_STACK_PRINT (yyss, yyssp);

  *++yyvsp = yyval;


  /* Now `shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */

  yyn = yyr1[yyn];

  yystate = yypgoto[yyn - YYNTOKENS] + *yyssp;
  if (0 <= yystate && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTOKENS];

  goto yynewstate;


/*------------------------------------.
| yyerrlab -- here on detecting error |
`------------------------------------*/
yyerrlab:
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
#if YYERROR_VERBOSE
      yyn = yypact[yystate];

      if (YYPACT_NINF < yyn && yyn < YYLAST)
	{
	  YYSIZE_T yysize = 0;
	  int yytype = YYTRANSLATE (yychar);
	  const char* yyprefix;
	  char *yymsg;
	  int yyx;

	  /* Start YYX at -YYN if negative to avoid negative indexes in
	     YYCHECK.  */
	  int yyxbegin = yyn < 0 ? -yyn : 0;

	  /* Stay within bounds of both yycheck and yytname.  */
	  int yychecklim = YYLAST - yyn;
	  int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
	  int yycount = 0;

	  yyprefix = ", expecting ";
	  for (yyx = yyxbegin; yyx < yyxend; ++yyx)
	    if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR)
	      {
		yysize += yystrlen (yyprefix) + yystrlen (yytname [yyx]);
		yycount += 1;
		if (yycount == 5)
		  {
		    yysize = 0;
		    break;
		  }
	      }
	  yysize += (sizeof ("syntax error, unexpected ")
		     + yystrlen (yytname[yytype]));
	  yymsg = (char *) YYSTACK_ALLOC (yysize);
	  if (yymsg != 0)
	    {
	      char *yyp = yystpcpy (yymsg, "syntax error, unexpected ");
	      yyp = yystpcpy (yyp, yytname[yytype]);

	      if (yycount < 5)
		{
		  yyprefix = ", expecting ";
		  for (yyx = yyxbegin; yyx < yyxend; ++yyx)
		    if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR)
		      {
			yyp = yystpcpy (yyp, yyprefix);
			yyp = yystpcpy (yyp, yytname[yyx]);
			yyprefix = " or ";
		      }
		}
	      yyerror (yymsg);
	      YYSTACK_FREE (yymsg);
	    }
	  else
	    yyerror ("syntax error; also virtual memory exhausted");
	}
      else
#endif /* YYERROR_VERBOSE */
	yyerror ("syntax error");
    }



  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
	 error, discard it.  */

      if (yychar <= YYEOF)
        {
          /* If at end of input, pop the error token,
	     then the rest of the stack, then return failure.  */
	  if (yychar == YYEOF)
	     for (;;)
	       {
		 YYPOPSTACK;
		 if (yyssp == yyss)
		   YYABORT;
		 YYDSYMPRINTF ("Error: popping", yystos[*yyssp], yyvsp, yylsp);
		 yydestruct (yystos[*yyssp], yyvsp);
	       }
        }
      else
	{
	  YYDSYMPRINTF ("Error: discarding", yytoken, &yylval, &yylloc);
	  yydestruct (yytoken, &yylval);
	  yychar = YYEMPTY;

	}
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:

#ifdef __GNUC__
  /* Pacify GCC when the user code never invokes YYERROR and the label
     yyerrorlab therefore never appears in user code.  */
  if (0)
     goto yyerrorlab;
#endif

  yyvsp -= yylen;
  yyssp -= yylen;
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;	/* Each real token shifted decrements this.  */

  for (;;)
    {
      yyn = yypact[yystate];
      if (yyn != YYPACT_NINF)
	{
	  yyn += YYTERROR;
	  if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYTERROR)
	    {
	      yyn = yytable[yyn];
	      if (0 < yyn)
		break;
	    }
	}

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
	YYABORT;

      YYDSYMPRINTF ("Error: popping", yystos[*yyssp], yyvsp, yylsp);
      yydestruct (yystos[yystate], yyvsp);
      YYPOPSTACK;
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  if (yyn == YYFINAL)
    YYACCEPT;

  YYDPRINTF ((stderr, "Shifting error token, "));

  *++yyvsp = yylval;


  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturn;

/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturn;

#ifndef yyoverflow
/*----------------------------------------------.
| yyoverflowlab -- parser overflow comes here.  |
`----------------------------------------------*/
yyoverflowlab:
  yyerror ("parser stack overflow");
  yyresult = 2;
  /* Fall through.  */
#endif

yyreturn:
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
  return yyresult;
}


#line 413 "parse.yy"


using namespace Parser;

NODE* Parser::parseCommand(const char *queryBuffer, unsigned int queryLen) 
{
  int rc;

  // Initialize the scanner to scan the buffer
  init_scanner(queryBuffer, queryLen);  
  
  // Initialize the parser
  init_parser();
  
  // Parse
  rc = yyparse();
    
  // Error parsing: return a null node
  if(rc != 0)
    return 0;
  
  // return the parse tree
  return parse_tree;  
}

void yyerror(char const *s) {
  std::cerr << "YYError: " << s << std::endl;
}

