#include "querygen/query.h"
#include <cstring>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <cstring>

using namespace Semantic;
using namespace std;

// Query assignment constructor
Query& Query::operator=(const Query& q){

	// 	Xing: For deep copy
	char *temp;
		if (this == &q)
		return *this;
		// Query type
        kind = q.kind;
        // Keyword distinct
        bDistinct = q.bDistinct;

		   // From clause
		   for (int i = 0; i < q.numRefTables; i++)
		       refTables[i] = q.refTables[i];

		   numRefTables = q.numRefTables;

		   for (int i = 0; i < q.numFromClauseTables; i++)
		       fromClauseTables[i] = q.fromClauseTables[i];

		   numFromClauseTables = q.numFromClauseTables;

		   // Window spec
		   for (int i = 0; i < q.numRefTables; i++){
			   winSpec[i].type = q.winSpec[i].type;
			   winSpec[i].u = q.winSpec[i].u;
		   }

		   // Select clause
		   numProjExprs = q.numProjExprs;
		   for (int i = 0; i < q.numProjExprs; i++){
			   projExprs[i] = new Expr;
			//   projExprs[i]->type =  q.projExprs[i]->type;
			   projExprs[i] = q.projExprs[i];
		   }


		   // Where clause
		   numPreds = q.numPreds;
		   for(int i=0; i< q.numPreds ; i++){
			      preds[i].op = q.preds[i].op;
			      preds[i].left = new Expr;
			      if( q.preds[i].left->exprType == E_ATTR_REF){
			    	  preds[i].left->type = q.preds[i].left->type;
			    	  preds[i].left->exprType = q.preds[i].left->exprType;
			    	  preds[i].left->u.attr.attrId = q.preds[i].left->u.attr.attrId;
			    	  preds[i].left->u.attr.varId = q.preds[i].left->u.attr.varId;
			      }
			      else
			      preds[i].left = q.preds[i].left;

			      preds[i].right = new Expr;

			      if(q.preds[i].right->exprType == E_CONST_VAL){
			    	  preds[i].right->type = q.preds[i].right->type;
			    	  preds[i].right->exprType = q.preds[i].right->exprType;
			      switch(q.preds[i].right->type){
			      case BYTE:
			      case INT:
			      case FLOAT:
			    	  preds[i].right->u = q.preds[i].right->u;
			    	  break;
			      case CHAR:
    	  			  temp = new char(strlen(q.preds[i].right->u.sval)+1);
    	  			  strncpy(temp, q.preds[i].right->u.sval, strlen(q.preds[i].right->u.sval)+1);
			    	  preds[i].right->u.sval = temp;
			    	  break;
			      default: break;
			      }
			      }

			      else
			    	  preds[i].right = q.preds[i].right;
		   }

		   // Group by clause
		   numGbyAttrs = q.numGbyAttrs;
		   for(int i = 0; i< q.numGbyAttrs; i++){
			  gbyAttrs[i].attrId = q.gbyAttrs[i].attrId;
			  gbyAttrs[i].varId = q.gbyAttrs[i].varId;
		   }

		   return *this;
}

//Query copy constructor
//Query& Query(const Query& q){
//
//    if (this == &q)
//    		return *this;
//    cout << "I am in query copy constructor" << endl;
//    		   kind = q.kind;
//    		   // From clause
//    		   for (int i = 0; i < q.numRefTables; i++)
//    		       refTables[i] = q.refTables[i];
//
//    		   numRefTables = q.numRefTables;
//
//    		   for (int i = 0; i < q.numFromClauseTables; i++)
//    		       fromClauseTables[i] = q.fromClauseTables[i];
//
//    		   numFromClauseTables = q.numFromClauseTables;
//
//    		   // Window spec
//    		   for (int i = 0; i < q.numRefTables; i++){
//    			   winSpec[i] = q.winSpec[i];
//    		   }
//
//    		   // Select clause
//    		   numProjExprs = q.numProjExprs;
//    		   for (int i = 0; i < q.numProjExprs; i++){
//    			   projExprs[i] = new Expr;
//    			//   projExprs[i]->type =  q.projExprs[i]->type;
//    			   projExprs[i] = q.projExprs[i];
//    		   }
//
//
//    		   // Where clause
//    		   numPreds = q.numPreds;
//    		   for(int i=0; i< q.numPreds ; i++){
//    			      preds[i].op = q.preds[i].op;
//    			      preds[i].left = new Expr;
//    			      preds[i].left = q.preds[i].left;
//    			      preds[i].right = new Expr;
//    			      if(q.preds[i].right->type == CHAR and q.preds[i].right->exprType == E_CONST_VAL){
//    			    	  preds[i].right->type = q.preds[i].right->type;
//    			    	  preds[i].right->exprType = q.preds[i].right->exprType;
//    			    	  strcmp(preds[i].right->u.sval, q.preds[i].right->u.sval);
//    			      }
//    			      else
//    			    	  preds[i].right = q.preds[i].right;
//    			//   preds[i] = q.preds[i];
//    		   }
//
//    		   return *this;
//}

//// Assignment constructor for BExpr
//BExpr& Query::operator=(const BExpr& b){
//	   cout << "I am in BExpr assignment constructor" << endl;
//		      if (this == &b)
//		         return *this;
//
//		      op = b.op;
//		      left = new Expr;
//		      left = b.left;
//		      right = new Expr;
//		      right = b.right;
//		      cout << "I am out BExpr assignment constructor" << endl;
//		      return *this;
//
//}

//// Copy Constructor for BExpr
//BExpr& BExpr(const BExpr& q) {
//    cout << "I am in BExpr copy constructor" << endl;
//		      op = b.op;
//		      left = new Expr;
//		      left = b.left;
//		      right = new Expr;
//		      right = b.right;
//}
//

