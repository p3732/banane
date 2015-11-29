

module AbsCPP where

-- Haskell module generated by the BNF converter




newtype Id = Id String deriving (Eq,Ord,Show,Read)
data Program =
   PDefs [Def]
  deriving (Eq,Ord,Show,Read)

data Def =
   DFun Type Id [Arg] [Stm]
  deriving (Eq,Ord,Show,Read)

data Arg =
   ADecl Type Id
  deriving (Eq,Ord,Show,Read)

data Stm =
   SExp Exp
 | SDecls Type [Id]
 | SInit Type Id Exp
 | SReturn Exp
 | SWhile Exp Stm
 | SBlock [Stm]
 | SIfElse Exp Stm Stm
  deriving (Eq,Ord,Show,Read)

data Exp =
   ETrue
 | EFalse
 | EInt Integer
 | EDouble Double
 | EId Id
 | EApp Id [Exp]
 | EPostIncr Exp
 | EPostDecr Exp
 | EPreIncr Exp
 | EPreDecr Exp
 | ETimes Exp Exp
 | EDiv Exp Exp
 | EPlus Exp Exp
 | EMinus Exp Exp
 | ELt Exp Exp
 | EGt Exp Exp
 | ELtEq Exp Exp
 | EGtEq Exp Exp
 | EEq Exp Exp
 | ENEq Exp Exp
 | EAnd Exp Exp
 | EOr Exp Exp
 | EAss Exp Exp
  deriving (Eq,Ord,Show,Read)

data Type =
   Type_bool
 | Type_int
 | Type_double
 | Type_void
  deriving (Eq,Ord,Show,Read)

